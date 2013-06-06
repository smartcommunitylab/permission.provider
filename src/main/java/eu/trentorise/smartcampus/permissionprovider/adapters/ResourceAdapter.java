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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

import eu.trentorise.smartcampus.permissionprovider.Config.AUTHORITY;
import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.ResourceDeclaration;
import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.ResourceMapping;
import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.Service;
import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.Services;
import eu.trentorise.smartcampus.permissionprovider.model.Resource;
import eu.trentorise.smartcampus.permissionprovider.model.ResourceParameter;
import eu.trentorise.smartcampus.permissionprovider.model.ResourceParameterKey;
import eu.trentorise.smartcampus.permissionprovider.oauth.ResourceStorage;
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
	
	private Map<String,Service> serviceMap = new HashMap<String, Service>(); 
	private Map<String,ResourceDeclaration> resourceDeclarationMap = new HashMap<String, ResourceDeclaration>();
	private Map<String,String> resourceTreeMap = new HashMap<String, String>();
	private Map<String,String> resourceServiceMap = new HashMap<String, String>();
	
	@PostConstruct 
	public void init() {
		processServiceResourceTemplates();
	}

	public void storeResourceParameter(ResourceParameter rp) {
		ResourceParameterKey pk = new ResourceParameterKey();
		pk.resourceId = rp.getResourceId();
		pk.value = rp.getValue();
		pk.parentResource = rp.getParentResource();
		if (pk.parentResource == null || pk.parentResource.trim().length() == 0) {
			pk.parentResource = RP_ROOT;
		}
		
		ResourceParameter rpold = resourceParameterRepository.findOne(pk);
		if (rpold != null && !rp.getClientId().equals(rp.getClientId())) {
			throw new IllegalArgumentException("A parameter already used by another app");
		} else if (rpold == null) {
			resourceParameterRepository.save(rp);
			//TODO : instantiate matching resources
			//TODO add resources to the client? 

		} else {
			throw new IllegalArgumentException("A parameter already exists");
		}
	}

	public void removeResourceParameter(String resourceId, String parentResource, String value, String clientId) {
		//TODO : check the usage?
		ResourceParameterKey pk = new ResourceParameterKey();
		pk.resourceId = resourceId;
		pk.value = value;
		pk.parentResource = parentResource;
		ResourceParameter rpdb = resourceParameterRepository.findOne(pk);
		if (rpdb != null && !rpdb.getClientId().equals(clientId)) {
			throw new IllegalArgumentException("Can delete only own resource parameters");
		}	
		deleteElements(pk);
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
						subKey.parentResource = subRp.getParentResource();
						subKey.value = subRp.getValue();
						resourceParameterRepository.delete(subKey);
					}
				}
			}
		}
	}

	public List<ResourceParameter> getOwnResourceParameters(String clientId, String serviceId, String parentId, String resourceId) {
		if (clientId == null) {
			return Collections.emptyList();
		}
		if (resourceId != null) {
			return resourceParameterRepository.findByClientIdAndResourceId(clientId,resourceId);
		}
		if (serviceId != null && parentId != null) {
			return resourceParameterRepository.findByClientIdAndServiceIdAndParentResource(clientId,serviceId,parentId);
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
					extractDefaultResources(rm,resources);
				}
				if (!resources.isEmpty()) {
					resourceStorage.storeResources(resources);
				}
			}
		}
	}
	
	/**
	 * @param rm
	 * @param resources
	 */
	private void extractDefaultResources(ResourceMapping rm, List<Resource> resources) {
		if (!isParametric(rm)) {
			resources.add(createResource(rm));
			if (rm.getResourceMapping() != null) {
				for (ResourceMapping child : rm.getResourceMapping()) {
					extractDefaultResources(child, resources);
				}
			}
		}
	}

	/**
	 * @param rm
	 * @return
	 */
	private boolean isParametric(ResourceMapping rm) {
		// TODO Auto-generated method stub
		return rm.getUri().indexOf('{')>=0;
	}

	/**
	 * @param rm
	 * @return
	 */
	private Resource createResource(ResourceMapping rm) {
		Resource r = new Resource();
		r.setAccessibleByClient(rm.isAccessibleByOthers());
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
				if ((resource.getClientId() != null && !resource.isAccessibleByClient())) {
					iterator.remove();
				}
			}
			return list;
		}
		return Collections.emptyList();
	}
}
