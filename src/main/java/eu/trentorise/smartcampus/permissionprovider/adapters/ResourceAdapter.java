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

import eu.trentorise.smartcampus.permissionprovider.Config.AUTHORITY;
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

	public void storeResourceParameter(ResourceParameter rp) {
		ResourceParameterKey pk = new ResourceParameterKey();
		pk.resourceId = rp.getResourceId();
		pk.value = rp.getValue();
		
		ResourceParameter rpold = resourceParameterRepository.findOne(pk);
		if (rpold != null && !rp.getClientId().equals(rp.getClientId())) {
			throw new IllegalArgumentException("A parameter already used by another app");
		} else if (rpold == null) {
			if (rp.getParentResource() == null || rp.getParentResource().isEmpty()) {
				rp.setParentResource(RP_ROOT);
			}
			resourceParameterRepository.save(rp);
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

	public void removeResourceParameter(String resourceId, String value, String clientId) {
		ResourceParameterKey pk = new ResourceParameterKey();
		pk.resourceId = resourceId;
		pk.value = value;
		ResourceParameter rpdb = resourceParameterRepository.findOne(pk);
		if (rpdb != null && !rpdb.getClientId().equals(clientId)) {
			throw new IllegalArgumentException("Can delete only own resource parameters");
		} if (rpdb != null) {
			Collection<String> uris = findResourceURIs(rpdb).keySet();
			Set<String> ids = new HashSet<String>();
			for (String uri : uris) {
				Resource r = resourceRepository.findByResourceUri(uri);
				if (r != null) {
					ids.add(r.getResourceId().toString());
				}
			}
			for (ClientDetailsEntity cd : clientDetailsRepository.findAll()) {
				if (!Collections.disjoint(cd.getResourceIds(), ids)) {
					throw new IllegalArgumentException("Resource is in use by other client app.");
				}
			} 
			for (String id : ids ){
				resourceRepository.delete(Long.parseLong(id));
			}
			deleteElements(pk);
		}	
	}
	
	/**
	 * @param rpdb
	 * @return
	 */
	private Map<String,ResourceMapping> findResourceURIs(ResourceParameter rpdb) {
		Map<String, ResourceMapping> res = new HashMap<String, ResourceMapping>();
		Map<String,String> params = new HashMap<String, String>();
		ResourceParameter rp = rpdb;
		Service service = serviceMap.get(rpdb.getServiceId());
		if (service == null) {
			throw new IllegalArgumentException("Service "+rpdb.getServiceId() +" is not found.");
		}
		
		while (true) {
			params.put(rp.getResourceId(), rp.getValue());
			ResourceParameterKey rpk = new ResourceParameterKey();
			rpk.resourceId = resourceTreeMap.get(rp.getResourceId());
			if (rpk.resourceId == null) {
				break;
			}
			rpk.value = rp.getParentResource();
			rp = resourceParameterRepository.findOne(rpk);
		}	
		
		List<ResourceMapping> list = flatServiceMappings.get(service.getId());
		if (list != null) {
			for (ResourceMapping rm : list) {
				UriTemplate template = new UriTemplate(rm.getUri());
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
	 * @param rpdb
	 */
	private void deleteElements(ResourceParameterKey rpKey) {
		String parentResource = rpKey.value;
		resourceParameterRepository.delete(rpKey);
		ResourceDeclaration rd = resourceDeclarationMap.get(rpKey.resourceId);
		if (rd != null && rd.getResource() != null) {
			for (ResourceDeclaration subRd : rd.getResource()) {
				List<ResourceParameter> instances = resourceParameterRepository.findByResourceIdAndParentResource(subRd.getId(), parentResource);
				if (instances != null) {
					for (ResourceParameter subRp : instances) {
						ResourceParameterKey subKey = new ResourceParameterKey();
						subKey.resourceId = subRp.getResourceId();
						subKey.value = subRp.getValue();
						resourceParameterRepository.delete(subKey);
					}
				}
			}
		}
	}

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

	private void processServiceResourceTemplates() {
		List<Service> services = loadResourceTemplates();
		for (Service s : services) {
			serviceMap.put(s.getId(), s);
			if (s.getResource() != null) {
				for (ResourceDeclaration rd : s.getResource()) {
					resourceDeclarationMap.put(rd.getId(), rd);
					resourceServiceMap.put(rd.getId(), s.getId());
					extractRDs(rd.getResource(),rd.getId(),s.getId());
				}
			}
			if (s.getResourceMapping() != null) {
				List<Resource> resources = new ArrayList<Resource>();
				for (ResourceMapping  rm : s.getResourceMapping()) {
					extractResources(rm,resources, s);
				}
				if (!resources.isEmpty()) {
					resourceStorage.storeResources(resources);
				}
			}
			// TODO validate service data
		}
	}
	
	/**
	 * @param rm
	 * @param resources
	 * @param s 
	 */
	private void extractResources(ResourceMapping rm, List<Resource> resources, Service s) {
		resourceMappingMap.put(rm.getId(), rm);
		List<ResourceMapping> list = flatServiceMappings.get(s.getId());
		if (list == null) {
			list = new ArrayList<ResourceMapping>();
			flatServiceMappings.put(s.getId(), list);
		}
		list.add(rm);
		
		if (!isParametric(rm)) {
			resources.add(createResource(rm));
			if (rm.getResourceMapping() != null) {
				for (ResourceMapping child : rm.getResourceMapping()) {
					extractResources(child, resources, s);
				}
			}
		}
	}

	/**
	 * @param rm
	 * @return
	 */
	private boolean isParametric(ResourceMapping rm) {
		UriTemplate template = new UriTemplate(rm.getUri());
		return template.getVariableNames() != null && template.getVariableNames().size() > 0;
	}

	/**
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
		return r;
	}

	/**
	 * @param list
	 * @param id
	 */
	private void extractRDs(List<ResourceDeclaration> list, String parent, String serviceId) {
		if (list != null) {
			for (ResourceDeclaration rd : list) {
				resourceDeclarationMap.put(rd.getId(), rd);
				resourceTreeMap.put(rd.getId(), parent);
				resourceServiceMap.put(rd.getId(), serviceId);
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
	 * @param clientId
	 * @return
	 */
	public List<Resource> getAvailableResources(String clientId) {
		if (clientId != null) {
			List<Resource> list = resourceRepository.findAll();
			for (Iterator<Resource> iterator = list.iterator(); iterator.hasNext();) {
				Resource resource = iterator.next();
				if ((!clientId.equals(resource.getClientId()) && resource.getClientId() != null && !resource.isAccessibleByOthers())) {
					iterator.remove();
				}
			}
			return list;
		}
		return Collections.emptyList();
	}
}
