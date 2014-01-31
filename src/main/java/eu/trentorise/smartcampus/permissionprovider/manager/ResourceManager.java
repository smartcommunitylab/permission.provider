/**
 *    Copyright 2012-2013 Trento RISE
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package eu.trentorise.smartcampus.permissionprovider.manager;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriTemplate;

import eu.trentorise.smartcampus.permissionprovider.Config;
import eu.trentorise.smartcampus.permissionprovider.Config.AUTHORITY;
import eu.trentorise.smartcampus.permissionprovider.Config.RESOURCE_VISIBILITY;
import eu.trentorise.smartcampus.permissionprovider.common.ResourceException;
import eu.trentorise.smartcampus.permissionprovider.common.Utils;
import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.ResourceDeclaration;
import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.ResourceMapping;
import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.Service;
import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.Services;
import eu.trentorise.smartcampus.permissionprovider.model.ClientDetailsEntity;
import eu.trentorise.smartcampus.permissionprovider.model.Resource;
import eu.trentorise.smartcampus.permissionprovider.model.ResourceParameter;
import eu.trentorise.smartcampus.permissionprovider.model.ServiceDescriptor;
import eu.trentorise.smartcampus.permissionprovider.oauth.ResourceStorage;
import eu.trentorise.smartcampus.permissionprovider.repository.ClientDetailsRepository;
import eu.trentorise.smartcampus.permissionprovider.repository.ResourceParameterRepository;
import eu.trentorise.smartcampus.permissionprovider.repository.ResourceRepository;
import eu.trentorise.smartcampus.permissionprovider.repository.ServiceRepository;

/**
 * Class used to operate resource model.
 * @author raman
 *
 */
@Component
@Transactional
public class ResourceManager {

	private static Log logger = LogFactory.getLog(ResourceManager.class);
	@Autowired
	private ResourceStorage resourceStorage;
	@Autowired
	private ResourceParameterRepository resourceParameterRepository;
	@Autowired
	private ResourceRepository resourceRepository;
	@Autowired
	private ClientDetailsRepository clientDetailsRepository;
	@Autowired
	private ServiceRepository serviceRepository;
	
//	/** map serviceId to {@link ServiceDescriptor} instance */
//	private Map<String,ServiceDescriptor> serviceMap = new HashMap<String, ServiceDescriptor>(); 
//	/** map resource declaration Id to {@link ResourceDeclaration} instance */
//	private Map<String,ResourceDeclaration> resourceDeclarationMap = new HashMap<String, ResourceDeclaration>();
//	/** map resource mapping Id to {@link ResourceMapping} instance */
//	private Map<String,ResourceMapping> resourceMappingMap = new HashMap<String, ResourceMapping>();
//	/** map resource declaration Id to service Id */
//	private Map<String,String> resourceServiceMap = new HashMap<String, String>();
//	/** map service Id to resource mappings of the service */ 
//	private Map<String,List<ResourceMapping>> flatServiceMappings = new HashMap<String, List<ResourceMapping>>();
	
	@PostConstruct 
	public void init() throws ResourceException {
		List<Service> services = loadResourceTemplates();
		processServiceObjects(services, null);
	}

	/**
	 * Save resource parameter. Check the uniqueness of the parameter value across all the
	 * parameters with the same resource parameter definition ID. Instantiate 
	 * all the derived resources. 
	 * @param rp
	 */
	public void storeResourceParameter(ResourceParameter rp) {
		ResourceParameter rpold = resourceParameterRepository.findOne(rp.getId());
		// check uniqueness
		String clientId = rp.getClientId();
		if (rpold != null && !clientId.equals(clientId)) {
			throw new IllegalArgumentException("A parameter already used by another app");
		} else if (rpold == null) {
			resourceParameterRepository.save(rp);
			// derived resources
			Map<String, ResourceMapping> mappings = findResourceURIs(rp);
			// store new resources entailed by the resource parameter
			if (mappings != null) {
				Set<String> newSet = new HashSet<String>();
				Set<String> newScopes = new HashSet<String>();
				for (String uri : mappings.keySet()) {
					ResourceMapping resourceMapping = mappings.get(uri);

					Resource r = prepareResource(clientId, rp,  uri, resourceMapping, rp.getVisibility());
					resourceRepository.save(r);
					newSet.add(r.getResourceId().toString());
					newScopes.add(r.getResourceUri());
				}
				// add automatically the resources entailed by own resource parameters to the client resourceIds
				ClientDetailsEntity cd = clientDetailsRepository.findByClientId(clientId);
				Set<String> oldSet = cd.getResourceIds();
				if (oldSet != null) newSet.addAll(oldSet);
				cd.setResourceIds(StringUtils.collectionToCommaDelimitedString(newSet));
				// add automatically the resources entailed by own resource parameters to the client scope
				oldSet = cd.getScope();
				if (oldSet != null) newScopes.addAll(oldSet);
				cd.setScope(StringUtils.collectionToCommaDelimitedString(newScopes));
				clientDetailsRepository.save(cd);
			}
		} else {
			throw new IllegalArgumentException("A parameter already exists");
		}
	}

	/**
	 * Update the visibility of the resource parameter. This only affects the child resources
	 * in case of more restrictive policy applied. If the changes are in conflict with the
	 * clients that use the corresponding resources, an exception is thrown. Also, if the new
	 * visibility is more relaxing than that of the parent, the exception is thrown.
	 * 
	 * @param id id of the changed resource parameter
	 * @param visibility new visibility value
	 * @return changed parameter
	 */
	public ResourceParameter updateResourceParameterVisibility(Long id, RESOURCE_VISIBILITY visibility) {
		assert visibility != null;
		ResourceParameter rpdb = resourceParameterRepository.findOne(id);
		if (rpdb != null) {
			String clientId = rpdb.getClientId();
			ClientDetailsEntity client = clientDetailsRepository.findByClientId(clientId);
			
			Map<String, RESOURCE_VISIBILITY> visibilityMap = new HashMap<String, Config.RESOURCE_VISIBILITY>();
			rpdb.setVisibility(visibility);
			for (String uri : findResourceURIs(rpdb).keySet()) {
				visibilityMap.put(uri, visibility);
				Resource res = resourceRepository.findByResourceUri(uri);
				res.setVisibility(visibility);
				resourceRepository.save(res);
			}
			
			if (!checkUsages(visibilityMap, clientId, client.getDeveloperId())) {
				throw new IllegalArgumentException("Resource is in use, cannot reduce visibility");
			}
			
			List<Resource> resources = resourceRepository.findByResourceParameter(rpdb);//.findByClientId(clientId);
			if (resources != null) {
				for (Resource r : resources) {
					r.setVisibility(rpdb.getVisibility());
//					ResourceMapping rm = resourceMappingMap.get(r.getResourceType());
//					Map<String,String> params = new UriTemplate(rm.getUri()).match(r.getResourceUri());
//					if (params != null && rpdb.getValue().equals(params.get(rd.getName()))) {
//						r.setVisibility(rpdb.getVisibility());
//					}
				}
			}
			
		} else {
			throw new IllegalArgumentException("No resource parameter found");
		}
		return rpdb;
	}
	
	/**
	 * Check the visibility of the specified resources managed by the specified client and its owner by all the clients registered
	 * @param visibilityMap contains the resource URI and its target visibility
	 * @return true if at least one of the specified resources violates visibility constraint
	 */
	private boolean checkUsages(Map<String, RESOURCE_VISIBILITY> visibilityMap, String clientId, Long developerId) {
		List<ClientDetailsEntity> clients = clientDetailsRepository.findAll();
		for (ClientDetailsEntity client : clients) {
			// owned resources are visible, skip
			if (clientId.equals(client.getClientId())) continue;
			// 
			Set<String> uris = client.getScope();
			for (String uri : visibilityMap.keySet()) {
				if (uris.contains(uri)) {
					switch (visibilityMap.get(uri)) {
					// if should be visible only by the owning client app - violation
					case CLIENT_APP:
						return false;
						// if should be visible only by the owning developer - violation
					case DEVELOPER:
						if (!client.getDeveloperId().equals(developerId)) {
							return false;
						}
					default:
						break;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Try to remove the resource parameter and its children. Operation is recursive.
	 * If one of the derived resources is already in use, an exception is thrown.
	 * @param rpId
	 */
	public void removeResourceParameter(Long rpId) {
		// main parameter
		ResourceParameter rpdb = resourceParameterRepository.findOne(rpId);
		if (rpdb != null) {
			String clientId = rpdb.getClientId();
			Set<String> ids = new HashSet<String>();
			Set<String> scopes = new HashSet<String>();
			// aggregate all derived resource uris
			Collection<String> uris = findResourceURIs(rpdb).keySet();
			for (String uri : uris) {
				Resource r = resourceRepository.findByResourceUri(uri);
				if (r != null) {
					ids.add(r.getResourceId().toString());
					scopes.add(r.getResourceUri());
				}
			}
			ClientDetailsEntity owner = null;
			// check the resource uri usages
			for (ClientDetailsEntity cd : clientDetailsRepository.findAll()) {
				if (cd.getClientId().equals(clientId)) {
					owner = cd;
					continue;
				}
				if (!Collections.disjoint(cd.getResourceIds(), ids)) {
					throw new IllegalArgumentException("Resource is in use by other client app.");
				}
			} 
			// delete main and its children
			for (String id : ids){
				resourceRepository.delete(Long.parseLong(id));
			}
			if (owner != null) {
				Set<String> oldScopes = new HashSet<String>(owner.getScope());
				oldScopes.removeAll(scopes);
				owner.setScope(StringUtils.collectionToCommaDelimitedString(oldScopes));
				Set<String> oldIds = new HashSet<String>(owner.getResourceIds());
				oldIds.removeAll(ids);
				owner.setResourceIds(StringUtils.collectionToCommaDelimitedString(oldIds));
				clientDetailsRepository.save(owner);
				
			}
			resourceParameterRepository.delete(rpdb);
		}	
	}
	
	/**
	 * Find all the resource uris derived from the specified resource parameter and its parent parameters.
	 * @param rpdb
	 * @return map with URIs as the keys and mapping definitions as values.
	 */
	private Map<String,ResourceMapping> findResourceURIs(ResourceParameter rpdb) {
		Map<String, ResourceMapping> res = new HashMap<String, ResourceMapping>();
//		Map<String,String> params = new HashMap<String, String>();
//		params.put(rpdb.getResourceId(), rpdb.getValue());
//		// the service where parameter is defined
//		ServiceDescriptor service = serviceMap.get(rpdb.getServiceId());
//		if (service == null) {
//			throw new IllegalArgumentException("ServiceDescriptor "+rpdb.getServiceId() +" is not found.");
//		}
//		// all the service resource mappings
//		List<ResourceMapping> list = flatServiceMappings.get(service.getId());
//		if (list != null) {
//			for (ResourceMapping rm : list) {
//				UriTemplate template = new UriTemplate(rm.getUri());
//				// if the extracted parameters contain all the template parameters, the mapping is updated
//				if (template.getVariableNames() != null) {
//					if (new HashSet<String>(template.getVariableNames()).equals(params.keySet())) {
//						URI uri = template.expand(params);
//						res.put(uri.toString(), rm);
//					}
//				}
//			}
//		}
		
		return res;
	}

	/**
	 * Read resource parameters owned by the specified client, optionally restricting 
	 * to the specified service and resource parameter ID
	 * @param clientId
	 * @return
	 */
	public List<ResourceParameter> getOwnResourceParameters(String clientId) {
		if (clientId == null) {
			return Collections.emptyList();
		}
//		if (resourceId != null) {
//			return resourceParameterRepository.findByClientIdAndResourceId(clientId,resourceId);
//		}
//		if (serviceId != null) {
//			return resourceParameterRepository.findByClientIdAndServiceId(clientId,serviceId);
//		}
		return resourceParameterRepository.findByClientId(clientId);
	}
	
	/**
	 * Read the resources from the XML descriptor
	 * @return
	 */
	private List<Service> loadResourceTemplates() {
		try {
			JAXBContext jaxb = JAXBContext.newInstance(ServiceDescriptor.class, Services.class, ResourceMapping.class, ResourceDeclaration.class);
			Unmarshaller unm = jaxb.createUnmarshaller();
			JAXBElement<Services> element = (JAXBElement<Services>) unm
					.unmarshal(
							new StreamSource(getClass().getResourceAsStream(
									"resourceTemplates.xml")), Services.class);
			return element.getValue().getService();
		} catch (JAXBException e) {
			logger.error("Failed to load resource templates: "+e.getMessage(),e);
			return Collections.emptyList();
		}
	}

	private void processServiceObjects(List<Service> services, String ownerId) throws ResourceException {
		List<ServiceDescriptor> dbServices = serviceRepository.findByOwnerId(ownerId);
		Map<String,ServiceDescriptor> dbServiceMap = new HashMap<String, ServiceDescriptor>();
		for (ServiceDescriptor s : dbServices) {
			dbServiceMap.put(s.getServiceId(), s); 
		}
		// split objects in created/updated/deleted
		Set<ServiceDescriptor> deleted  = new HashSet<ServiceDescriptor>();
		Map<Service,ServiceDescriptor> updated = new HashMap<Service,ServiceDescriptor>(); 
		Set<Service> created = new HashSet<Service>();
		Set<String> newOnes = new HashSet<String>();
		for (Service s : services) {
			if (!dbServiceMap.containsKey(s.getId())) {
				created.add(s);
			} else {
				updated.put(s, dbServiceMap.get(s.getId()));
			}
			newOnes.add(s.getId());
		}
		for (ServiceDescriptor s : dbServices) {
			if (!newOnes.contains(s.getServiceId())) {
				deleted.add(s);
			}
		}
		
		// process changes
		for (ServiceDescriptor s : deleted) {
			deleteService(s, ownerId);
		}
		for (Service service: updated.keySet()) {
			updateService(service, updated.get(service), ownerId);
		}
		for (Service service: created) {
			createService(service, ownerId);
		}
	}
	
	/**
	 * @param newService
	 * @param oldService
	 * @param ownerId
	 * @return 
	 */
	private Service updateService(Service newService, ServiceDescriptor oldService, String ownerId) {
		// TODO
		// - resource param/resource mapping change => delete/add
		// - if is in use throw exception, otherwise delete
		// TODO extract non-parametric resources and store if no conflicts
		// TODO delete unused resources 
		return newService;
	}

	/**
	 * @param s
	 * @param ownerId
	 * @throws ResourceException 
	 */
	private void deleteService(ServiceDescriptor s, String ownerId) throws ResourceException {
		List<Resource> resources = resourceRepository.findByService(s);
		// check the service resources are in use by the clients
		if (resources != null && ! resources.isEmpty()) {
			Set<String> ids = new HashSet<String>();
			for (Resource r : resources) {
				ids.add(""+r.getResourceId());
			}
			List<ClientDetailsEntity> clients = clientDetailsRepository.findAll();
			for (ClientDetailsEntity c : clients) {
				if (!Collections.disjoint(ids, c.getResourceIds())) {
					throw new ResourceException("Resource in use by client: "+c.getClientId());
				}
			}
		}
		resourceRepository.delete(resources);
		resourceParameterRepository.delete(resourceParameterRepository.findByService(s));
		// delete service
		serviceRepository.delete(s);
	}
	
	/**
	 * Create new service for the specified owner
	 * @param service
	 * @param ownerId
	 * @return created {@link Service} object
	 * @throws ResourceException 
	 */
	public Service createService(Service service, String ownerId) throws ResourceException {
		ServiceDescriptor entity = Utils.toServiceEntity(service);
		entity.setOwnerId(ownerId);
		// saving new service
		entity = serviceRepository.save(entity);
		// read non-parametric service resources 
		List<Resource> resourcesToStore = extractResources(service);
		if (resourcesToStore != null && !resourcesToStore.isEmpty()) {
			for (Iterator<Resource> iterator = resourcesToStore.iterator(); iterator.hasNext();) {
				Resource r = iterator.next();
				Resource existing = resourceRepository.findByResourceUri(r.getResourceUri());
				// if resource already exists and belongs to a different service, throw an exception
				if (existing != null && !existing.getService().getServiceId().equals(service.getId())) {
					throw new ResourceException("resource not unique: "+r.getResourceUri());
				} else if (existing != null) {
					iterator.remove();
				} else {
					r.setService(entity);
				}
			}
			// store new non-parametric resources
			resourceStorage.storeResources(resourcesToStore);
		}
		return Utils.toServiceObject(entity); 
	}

	/**
	 * Update the specified service object
	 * @param s
	 * @param ownerId
	 * @return updated {@link Service} object
	 */
	public Service updateService(Service s, String ownerId) {
		ServiceDescriptor old = serviceRepository.findOne(s.getId());
		return updateService(s, old, ownerId);
	} 
	
	/**
	 * Delete the specified service
	 * @param serviceId
	 * @param ownerId
	 * @throws ResourceException 
	 */
	public void deleteService(String serviceId, String ownerId) throws ResourceException {
		ServiceDescriptor old = serviceRepository.findOne(serviceId);
		deleteService(old, ownerId);
	}

	private List<Resource> extractResources(Service s) {
		List<Resource> resources = new ArrayList<Resource>();
		// process resource mappings
		if (s.getResourceMapping() != null) {
			for (ResourceMapping  rm : s.getResourceMapping()) {
				// extract resource mappings recursively
				extractResources(rm,resources, s);
			}
//			// store the extracted non-parametric resource mappings
//			if (!resources.isEmpty()) {
//				resourceStorage.storeResources(resources);
//			}
		}
		return resources;
	}

	
	/**
	 * Extract resource mappings recursively
	 * @param rm
	 * @param resources
	 * @param s 
	 */
	private void extractResources(ResourceMapping rm, List<Resource> resources, Service s) {
		// add non-parametric resources to the target list
		if (!isParametric(rm)) {
			resources.add(prepareResource(null, null, rm.getUri(),rm, RESOURCE_VISIBILITY.PUBLIC));
		}
	}

	/**
	 * @param rm
	 * @return true if the mapping definition is parametric to the service resource parameters
	 */
	private boolean isParametric(ResourceMapping rm) {
		UriTemplate template = new UriTemplate(rm.getUri());
		return template.getVariableNames() != null && template.getVariableNames().size() > 0;
	}

	/**
	 * @param clientId
	 * @param rp 
	 * @param uri
	 * @param rm
	 * @param visibility 
	 * @return {@link Resource} instance out of mapping, clientID, and resource URI.
	 */
	protected Resource prepareResource(String clientId, ResourceParameter rp, String uri, ResourceMapping rm, RESOURCE_VISIBILITY visibility) {
		Resource r = new Resource();
		r.setAccessibleByOthers(rm.isAccessibleByOthers());
		r.setApprovalRequired(rm.isApprovalRequired());
		r.setAuthority(AUTHORITY.valueOf(rm.getAuthority()));
		r.setClientId(clientId);
		r.setResourceParameter(rp);
		UriTemplate template = new UriTemplate(rm.getUri());
		Map<String,String> params = template.match(uri);
		template = new UriTemplate(rm.getDescription());
		try {
			r.setDescription(URLDecoder.decode(template.expand(params).toString(),"utf8"));
		} catch (UnsupportedEncodingException e) {
			r.setDescription(rm.getDescription());
		}
		template = new UriTemplate(rm.getName());
		try {
			r.setName(URLDecoder.decode(template.expand(params).toString(),"utf8"));
		} catch (UnsupportedEncodingException e) {
			r.setName(rm.getName());
		}
		r.setResourceType(rm.getId());
		r.setResourceUri(uri);
		r.setVisibility(visibility);
		return r;
	}

	/**
	 * @return
	 */
	public Collection<Service> getServiceObjects() {
		List<ServiceDescriptor> services = serviceRepository.findAll();
		List<Service> res = new ArrayList<Service>();
		for (ServiceDescriptor s : services) {
			res.add(Utils.toServiceObject(s));
		}
		return res;
	}

	/**
	 * read the resource that the client app may request permissions for
	 * @param clientId
	 * @return
	 */
	public List<Resource> getAvailableResources(String clientId, Long userId) {
		if (clientId != null) {
			// check all the resources
			List<Resource> list = resourceRepository.findAll();
			for (Iterator<Resource> iterator = list.iterator(); iterator.hasNext();) {
				Resource resource = iterator.next();
				// interested only in non-owned resources
				if (!clientId.equals(resource.getClientId()) && resource.getClientId() != null) {
					if (!resource.isAccessibleByOthers()) {
						iterator.remove();
						continue;
					}
					// check the resource visibility for the current client
					switch (resource.getVisibility()) {
					case CLIENT_APP:
						iterator.remove();
						break;
					case DEVELOPER:
						ClientDetailsEntity cd = clientDetailsRepository.findByClientId(resource.getClientId());
						if (cd == null || !cd.getDeveloperId().equals(userId)) {
							iterator.remove();
						}
					default:
						break;
					}
				}
			}
			return list;
		}
		return Collections.emptyList();
	}
}
