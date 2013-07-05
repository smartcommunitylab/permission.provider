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

package eu.trentorise.smartcampus.permissionprovider.adapters;

import java.net.URI;
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
import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.ResourceDeclaration;
import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.ResourceMapping;
import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.Service;
import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.Services;
import eu.trentorise.smartcampus.permissionprovider.model.ClientDetailsEntity;
import eu.trentorise.smartcampus.permissionprovider.model.Resource;
import eu.trentorise.smartcampus.permissionprovider.model.ResourceParameter;
import eu.trentorise.smartcampus.permissionprovider.model.ResourceParameterKey;
import eu.trentorise.smartcampus.permissionprovider.oauth.ResourceStorage;
import eu.trentorise.smartcampus.permissionprovider.repository.ClientDetailsRepository;
import eu.trentorise.smartcampus.permissionprovider.repository.ResourceParameterRepository;
import eu.trentorise.smartcampus.permissionprovider.repository.ResourceRepository;

/**
 * Class used to operate resource model.
 * @author raman
 *
 */
@Component
@Transactional
public class ResourceAdapter {

	private static final String RP_ROOT = "_ROOT";
	
	private static Log logger = LogFactory.getLog(ResourceAdapter.class);
	@Autowired
	private ResourceStorage resourceStorage;
	@Autowired
	private ResourceParameterRepository resourceParameterRepository;
	@Autowired
	private ResourceRepository resourceRepository;
	@Autowired
	private ClientDetailsRepository clientDetailsRepository;
	
	private Map<String,Service> serviceMap = new HashMap<String, Service>(); 
	private Map<String,ResourceDeclaration> resourceDeclarationMap = new HashMap<String, ResourceDeclaration>();
	private Map<String,ResourceMapping> resourceMappingMap = new HashMap<String, ResourceMapping>();
	private Map<String,String> resourceTreeMap = new HashMap<String, String>();
	private Map<String,String> resourceServiceMap = new HashMap<String, String>();
	private Map<String,List<ResourceMapping>> flatServiceMappings = new HashMap<String, List<ResourceMapping>>();
	
	@PostConstruct 
	public void init() {
		processServiceResourceTemplates();
	}

	/**
	 * Save resource parameter. Check the uniqueness of the parameter value across all the
	 * parameters with the same resource parameter definition ID. Instantiate 
	 * all the derived resources. 
	 * @param rp
	 */
	public void storeResourceParameter(ResourceParameter rp) {
		ResourceParameterKey pk = new ResourceParameterKey();
		pk.resourceId = rp.getResourceId();
		pk.value = rp.getValue();
		
		ResourceParameter rpold = resourceParameterRepository.findOne(pk);
		// check uniqueness
		if (rpold != null && !rp.getClientId().equals(rp.getClientId())) {
			throw new IllegalArgumentException("A parameter already used by another app");
		} else if (rpold == null) {
			if (rp.getParentResource() == null || rp.getParentResource().isEmpty()) {
				rp.setParentResource(RP_ROOT);
			}
			resourceParameterRepository.save(rp);
			// derived resources
			Map<String, ResourceMapping> mappings = findResourceURIs(rp);
			// store new resources entailed by the resource parameter
			if (mappings != null) {
				Set<String> newSet = new HashSet<String>();
				Set<String> newScopes = new HashSet<String>();
				for (String uri : mappings.keySet()) {
					Resource r = new Resource();
					r.setAccessibleByOthers(mappings.get(uri).isAccessibleByOthers());
					r.setApprovalRequired(mappings.get(uri).isApprovalRequired());
					r.setAuthority(AUTHORITY.valueOf(mappings.get(uri).getAuthority()));
					r.setClientId(rp.getClientId());
					r.setDescription(mappings.get(uri).getDescription());
					r.setName(mappings.get(uri).getName());
					r.setResourceType(mappings.get(uri).getId());
					r.setResourceUri(uri);
					r.setVisibility(RESOURCE_VISIBILITY.PUBLIC);
					resourceRepository.save(r);
					newSet.add(r.getResourceId().toString());
					newScopes.add(r.getResourceUri());
				}
				// add automatically the resources entailed by own resource parameters to the client resourceIds
				ClientDetailsEntity cd = clientDetailsRepository.findByClientId(rp.getClientId());
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
	 * @param resourceId id of the changed resource parameter definition
	 * @param value parameter value
	 * @param clientId owning client id
	 * @param visibility new visibility value
	 * @return changed parameter
	 */
	public ResourceParameter updateResourceParameterVisibility(String resourceId, String value, String clientId, RESOURCE_VISIBILITY visibility) {
		assert visibility != null;
		
		ResourceParameterKey pk = new ResourceParameterKey();
		pk.resourceId = resourceId;
		pk.value = value;
		
		ResourceParameter rpdb = resourceParameterRepository.findOne(pk);
		if (rpdb != null && !rpdb.getClientId().equals(clientId)) {
			throw new IllegalArgumentException("Can delete only own resource parameters");
		} if (rpdb != null) {
			ClientDetailsEntity client = clientDetailsRepository.findByClientId(clientId);
			
			ResourceParameter parent = findParentResourceParameter(rpdb);
			
			if (parent != null && !Config.checkVisibility(parent.getVisibility(),visibility)) {
				throw new IllegalArgumentException("Can not increase visibility with respect to the parent resource");
			} 
			
			Map<String, RESOURCE_VISIBILITY> visibilityMap = new HashMap<String, Config.RESOURCE_VISIBILITY>();
			rpdb.setVisibility(visibility);
			for (String uri : findResourceURIs(rpdb).keySet()) {
				visibilityMap.put(uri, visibility);
				Resource res = resourceRepository.findByResourceUri(uri);
				res.setVisibility(visibility);
				resourceRepository.save(res);
			}
			
			List<ResourceParameter> children = findChildResourceParameters(rpdb);

			for (ResourceParameter child : children) {
				RESOURCE_VISIBILITY newVis = Config.alignVisibility(visibility,child.getVisibility()); 
				if (newVis != child.getVisibility()) {
					for (String uri : findResourceURIs(child).keySet()) {
						visibilityMap.put(uri, newVis);
						Resource res = resourceRepository.findByResourceUri(uri);
						res.setVisibility(newVis);
						resourceRepository.save(res);
					}
				}
				child.setVisibility(newVis);
				resourceParameterRepository.save(child);
			}
			
			if (!checkUsages(visibilityMap, clientId, client.getDeveloperId())) {
				throw new IllegalArgumentException("Resource is in use, cannot reduce visibility");
			}
			
			
			ResourceDeclaration rd = resourceDeclarationMap.get(rpdb.getResourceId());
			List<Resource> resources = resourceRepository.findByClientId(clientId);
			if (resources != null) {
				for (Resource r : resources) {
					ResourceMapping rm = resourceMappingMap.get(r.getResourceType());
					Map<String,String> params = new UriTemplate(rm.getUri()).match(r.getResourceUri());
					if (params != null && rpdb.getValue().equals(params.get(rd.getName()))) {
						r.setVisibility(rpdb.getVisibility());
					}
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
	 * @param resourceId
	 * @param value
	 * @param clientId
	 */
	public void removeResourceParameter(String resourceId, String value, String clientId) {
		ResourceParameterKey pk = new ResourceParameterKey();
		pk.resourceId = resourceId;
		pk.value = value;
		// main parameter
		ResourceParameter rpdb = resourceParameterRepository.findOne(pk);
		if (rpdb != null && !rpdb.getClientId().equals(clientId)) {
			throw new IllegalArgumentException("Can delete only own resource parameters");
		} if (rpdb != null) {
			// main and all children
			List<ResourceParameter> all = new ArrayList<ResourceParameter>();
			all.add(rpdb);
			all.addAll(findChildResourceParameters(rpdb));
			Set<String> ids = new HashSet<String>();
			// aggregate all derived resource uris
			for (ResourceParameter rp : all) {
				Collection<String> uris = findResourceURIs(rp).keySet();
				for (String uri : uris) {
					Resource r = resourceRepository.findByResourceUri(uri);
					if (r != null) {
						ids.add(r.getResourceId().toString());
					}
				}
			}
			// check the resource uri usages
			for (ClientDetailsEntity cd : clientDetailsRepository.findAll()) {
				if (cd.getClientId().equals(clientId)) continue;
				if (!Collections.disjoint(cd.getResourceIds(), ids)) {
					throw new IllegalArgumentException("Resource is in use by other client app.");
				}
			} 
			// delete main and its children
			for (String id : ids){
				resourceRepository.delete(Long.parseLong(id));
			}
			for (ResourceParameter rp : all) {
				resourceParameterRepository.delete(rp);
			}
		}	
	}
	
	/**
	 * Find all the resource uris derived from the specified resource parameter and its parent parameters.
	 * @param rpdb
	 * @return map with URIs as the keys and mapping definitions as values.
	 */
	private Map<String,ResourceMapping> findResourceURIs(ResourceParameter rpdb) {
		Map<String, ResourceMapping> res = new HashMap<String, ResourceMapping>();
		Map<String,String> params = new HashMap<String, String>();
		ResourceParameter rp = rpdb;
		// the service where parameter is defined
		Service service = serviceMap.get(rpdb.getServiceId());
		if (service == null) {
			throw new IllegalArgumentException("Service "+rpdb.getServiceId() +" is not found.");
		}
		// find all the parent parameters
		while (true) {
			params.put(rp.getResourceId(), rp.getValue());
			rp = findParentResourceParameter(rp);
			if (rp == null) break;
		}	
		// all the service resource mappings
		List<ResourceMapping> list = flatServiceMappings.get(service.getId());
		if (list != null) {
			for (ResourceMapping rm : list) {
				UriTemplate template = new UriTemplate(rm.getUri());
				// if the extracted parameters contain all the template parameters, the mapping is updated
				if (template.getVariableNames() != null) {
					if (new HashSet<String>(template.getVariableNames()).equals(params.keySet())) {
						URI uri = template.expand(params);
						res.put(uri.toString(), rm);
					}
				}
			}
		}
		
		return res;
	}
	/**
	 * @param child
	 * @return parant resource parameter of the specified one
	 */
	private ResourceParameter findParentResourceParameter(ResourceParameter child) {
		ResourceParameterKey rpk = new ResourceParameterKey();
		rpk.resourceId = resourceTreeMap.get(child.getResourceId());
		if (rpk.resourceId == null) {
			return null;
		}
		rpk.value = child.getParentResource();
		return resourceParameterRepository.findOne(rpk);
	}
	
	/**
	 * Find all the children parameters of the specified parameter.
	 * @param parent
	 * @return
	 */
	private List<ResourceParameter> findChildResourceParameters(ResourceParameter parent) {
		ResourceDeclaration rd = resourceDeclarationMap.get(parent.getResourceId());
		List<ResourceParameter> result = new ArrayList<ResourceParameter>();
		if (rd != null && rd.getResource() != null) {
			// child declarations
			for (ResourceDeclaration childDeclaration : rd.getResource()) {
				// child instances
				List<ResourceParameter> children = resourceParameterRepository.findByResourceIdAndParentResource(childDeclaration.getId(), parent.getValue());
				result.addAll(children);
				for (ResourceParameter child : children) {
					// recursion
					result.addAll(findChildResourceParameters(child));
				}
			}
		}
		return result;
	}


	/**
	 * Read resource parameters owned by the specified client, optionally restricting 
	 * to the specified service and resource parameter ID
	 * @param clientId
	 * @param serviceId
	 * @param resourceId
	 * @return
	 */
	public List<ResourceParameter> getOwnResourceParameters(String clientId, String serviceId, String resourceId) {
		if (clientId == null) {
			return Collections.emptyList();
		}
		if (resourceId != null) {
			return resourceParameterRepository.findByClientIdAndResourceId(clientId,resourceId);
		}
		if (serviceId != null) {
			return resourceParameterRepository.findByClientIdAndServiceId(clientId,serviceId);
		}
		return resourceParameterRepository.findByClientId(clientId);
	}
	
	/**
	 * Read the resources from the XML descriptor
	 * @return
	 */
	private List<Service> loadResourceTemplates() {
		try {
			JAXBContext jaxb = JAXBContext.newInstance(Service.class, Services.class, ResourceMapping.class, ResourceDeclaration.class);
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

	/**
	 * Parse the resource template descriptor
	 */
	private void processServiceResourceTemplates() {
		List<Service> services = loadResourceTemplates();
		for (Service s : services) {
			// service descriptor
			serviceMap.put(s.getId(), s);
			if (s.getResource() != null) {
				// process resource parameter declarations
				for (ResourceDeclaration rd : s.getResource()) {
					// resource parameter declaration
					resourceDeclarationMap.put(rd.getId(), rd);
					// map resource parameter to service
					resourceServiceMap.put(rd.getId(), s.getId());
					// extract parameters recursively
					extractRDs(rd.getResource(),rd.getId(),s.getId());
				}
			}
			// process resource mappings
			if (s.getResourceMapping() != null) {
				List<Resource> resources = new ArrayList<Resource>();
				for (ResourceMapping  rm : s.getResourceMapping()) {
					// extract resource mappings recursively
					extractResources(rm,resources, s);
				}
				// store the extracted non-parametric resource mappings
				if (!resources.isEmpty()) {
					resourceStorage.storeResources(resources);
				}
			}
			// TODO validate service data
		}
	}
	
	/**
	 * Extract resource mappings recursively
	 * @param rm
	 * @param resources
	 * @param s 
	 */
	private void extractResources(ResourceMapping rm, List<Resource> resources, Service s) {
		// resource mapping
		resourceMappingMap.put(rm.getId(), rm);
		// flat list of resource mappings associated to the service
		List<ResourceMapping> list = flatServiceMappings.get(s.getId());
		if (list == null) {
			list = new ArrayList<ResourceMapping>();
			flatServiceMappings.put(s.getId(), list);
		}
		list.add(rm);
		
		// add non-parametric resources to the target list
		if (!isParametric(rm)) {
			resources.add(createResource(rm));
		}
		// recursion
		if (rm.getResourceMapping() != null) {
			for (ResourceMapping child : rm.getResourceMapping()) {
				extractResources(child, resources, s);
			}
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
	 * Create resource entity to be stored in DB from the non-parametric resource definition
	 * @param rm
	 * @return
	 */
	private Resource createResource(ResourceMapping rm) {
		Resource r = new Resource();
		r.setAccessibleByOthers(rm.isAccessibleByOthers());
		r.setApprovalRequired(rm.isApprovalRequired());
		r.setAuthority(AUTHORITY.valueOf(rm.getAuthority()));
		r.setClientId(null);
		r.setDescription(rm.getDescription());
		r.setName(rm.getName());
		r.setResourceType(rm.getId());
		r.setResourceUri(rm.getUri());
		r.setVisibility(RESOURCE_VISIBILITY.PUBLIC);
		return r;
	}

	/**
	 * Recursively extract resource parameter declarations
	 * @param list
	 * @param id
	 */
	private void extractRDs(List<ResourceDeclaration> list, String parent, String serviceId) {
		if (list != null) {
			for (ResourceDeclaration rd : list) {
				// resource declaration map
				resourceDeclarationMap.put(rd.getId(), rd);
				// map property to its parent if any
				resourceTreeMap.put(rd.getId(), parent);
				// map resource to the service
				resourceServiceMap.put(rd.getId(), serviceId);
				// recursion
				extractRDs(rd.getResource(),rd.getId(),serviceId);
			}
		}
	}

	/**
	 * @return
	 */
	public Collection<Service> getServices() {
		return Collections.unmodifiableCollection(serviceMap.values());
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
