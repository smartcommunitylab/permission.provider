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

package eu.trentorise.smartcampus.permissionprovider.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.permissionprovider.adapters.ResourceAdapter;
import eu.trentorise.smartcampus.permissionprovider.model.ClientAppInfo;
import eu.trentorise.smartcampus.permissionprovider.model.ClientDetailsEntity;
import eu.trentorise.smartcampus.permissionprovider.model.Permissions;
import eu.trentorise.smartcampus.permissionprovider.model.Resource;
import eu.trentorise.smartcampus.permissionprovider.model.ResourceParameter;
import eu.trentorise.smartcampus.permissionprovider.model.Response;
import eu.trentorise.smartcampus.permissionprovider.model.Response.RESPONSE;
import eu.trentorise.smartcampus.permissionprovider.repository.ClientDetailsRepository;
import eu.trentorise.smartcampus.permissionprovider.repository.ResourceRepository;

/**
 * @author raman
 *
 */
@Controller
public class PermissionController extends AbstractController {

	private static final Integer RA_NONE = 0;
	private static final Integer RA_APPROVED = 1;
	private static final Integer RA_REJECTED = 2;
	private static final Integer RA_PENDING = 3;
	private static Log logger = LogFactory.getLog(PermissionController.class);
	@Autowired
	private ResourceAdapter resourceAdapter;
	@Autowired
	private ClientDetailsRepository clientDetailsRepository;
	@Autowired
	private ResourceRepository resourceRepository;
	
	@RequestMapping(value="/dev/permissions/{clientId}",method=RequestMethod.PUT)
	public @ResponseBody Response savePermissions(@RequestBody Permissions permissions, @PathVariable String clientId) {
		Response response = new Response();
		response.setResponseCode(RESPONSE.OK);
		try {
			checkClientIdOwnership(clientId);
			ClientDetailsEntity clientDetails = clientDetailsRepository.findByClientId(clientId);
			ClientAppInfo info = ClientAppInfo.convert(clientDetails.getAdditionalInformation());
			if (info.getResourceApprovals() == null) info.setResourceApprovals(new HashMap<String, Boolean>());
			if (!clientDetails.getDeveloperId().toString().equals(getUser().getUsername())) {
				response.setResponseCode(RESPONSE.ERROR);
				response.setErrorMessage("Attempting to access non-owned data.");
			} else {
				Collection<String> resourceIds = new HashSet<String>(clientDetails.getResourceIds());
				Collection<String> scopes = new HashSet<String>(clientDetails.getScope());
				for (String r : permissions.getSelectedResources().keySet()) {
					Resource resource = resourceRepository.findOne(Long.parseLong(r));
					// if not checked, remove from permissions and from pending requests
					if (!permissions.getSelectedResources().get(r)) {
						info.getResourceApprovals().remove(r);
						resourceIds.remove(r);
						scopes.remove(resource.getResourceUri());
					// if checked but requires approval, check whether  
				    // - already approved (i.e., included in client resourceIds)
					// - already requested (i.e., incuded in additional info approval requests map)	
					} else if (resource.isApprovalRequired()) {
						if (!resourceIds.contains(r) && ! info.getResourceApprovals().containsKey(r)) {
							info.getResourceApprovals().put(r, true);
						}
					// if approval is not required, include directly in client resource ids	
					} else {
						resourceIds.add(r);
						scopes.add(resource.getResourceUri());
					}
				}
				clientDetails.setResourceIds(StringUtils.collectionToCommaDelimitedString(resourceIds));
				clientDetails.setScope(StringUtils.collectionToCommaDelimitedString(scopes));
				clientDetails.setAdditionalInformation(info.toJson());
				clientDetailsRepository.save(clientDetails);
				response.setData(buildPermissions(clientDetails));
			}
		} catch (Exception e) {
			logger.error("Failure saving permissions model: "+e.getMessage(),e);
			response.setErrorMessage("Failure saving permissions model: "+e.getMessage());
			response.setResponseCode(RESPONSE.ERROR);
		}
		
		return response;
		
	}
	

	@RequestMapping(value="/dev/permissions/{clientId}",method=RequestMethod.GET)
	public @ResponseBody Response getPermissions(@PathVariable String clientId) {
		Response response = new Response();
		response.setResponseCode(RESPONSE.OK);
		try {
			checkClientIdOwnership(clientId);
			ClientDetailsEntity clientDetails = clientDetailsRepository.findByClientId(clientId);
			if (!clientDetails.getDeveloperId().toString().equals(getUser().getUsername())) {
				response.setResponseCode(RESPONSE.ERROR);
				response.setErrorMessage("Attempting to access non-owned data.");
			} else {
				Permissions permissions = buildPermissions(clientDetails);
				response.setData(permissions);
			}
		} catch (Exception e) {
			logger.error("Failure reading permissions model: "+e.getMessage(),e);
			response.setErrorMessage("Failure reading permissions model: "+e.getMessage());
			response.setResponseCode(RESPONSE.ERROR);
		}
		
		return response;
	}


	/**
	 * @param clientId
	 * @param clientDetails
	 * @return
	 */
	protected Permissions buildPermissions(ClientDetailsEntity clientDetails) {
		Permissions permissions = new Permissions();
		permissions.setServices(resourceAdapter.getServices());
		Map<String, List<ResourceParameter>> ownResources = new HashMap<String, List<ResourceParameter>>();
		// read resource parameters owned by the client and create 'parameter-values' map
		List<ResourceParameter> resourceParameters = resourceAdapter.getOwnResourceParameters(clientDetails.getClientId(), null, null);
		if (resourceParameters != null) {
			for (ResourceParameter resourceParameter : resourceParameters) {
				List<ResourceParameter> sublist = ownResources.get(resourceParameter.getResourceId());
				if (sublist == null) {
					sublist = new ArrayList<ResourceParameter>();
					ownResources.put(resourceParameter.getResourceId(), sublist);
				}
				sublist.add(resourceParameter);
			}
		}
		permissions.setOwnResources(ownResources);
		// read all available resources and assign permission status
		Map<String, List<Resource>> otherResourcesMap = new HashMap<String, List<Resource>>();
		// map resources selected by the client
		Map<String,Boolean> selectedResources = new HashMap<String, Boolean>();
		// map resource approval state
		Map<String,Integer> resourceApprovals = new HashMap<String, Integer>();
		Set<String> set = clientDetails.getResourceIds();
		if (set == null) set = Collections.emptySet();
		
		List<Resource> otherResources = resourceAdapter.getAvailableResources(clientDetails.getClientId());
		// read approval status for the resources that require approval explicit
		ClientAppInfo info = ClientAppInfo.convert(clientDetails.getAdditionalInformation());
		if (info.getResourceApprovals() == null) info.setResourceApprovals(Collections.<String,Boolean>emptyMap());
		
		if (otherResources != null) {
			for (Resource resource : otherResources) {
				String rId = resource.getResourceId().toString();
				List<Resource> sublist = otherResourcesMap.get(resource.getResourceType());
				if (sublist == null) {
					sublist = new ArrayList<Resource>();
					otherResourcesMap.put(resource.getResourceType(), sublist);
				}
				sublist.add(resource);
				selectedResources.put(rId, set.contains(rId) || info.getResourceApprovals().containsKey(rId));
				if (selectedResources.containsKey(rId) && selectedResources.get(rId)) {
					if (set.contains(rId)) resourceApprovals.put(rId, RA_APPROVED);
					else if (!info.getResourceApprovals().get(rId)) resourceApprovals.put(rId, RA_REJECTED);
					else resourceApprovals.put(rId, RA_PENDING);
				} else {
					resourceApprovals.put(rId, RA_NONE);
				}
			}
		}
		permissions.setResourceApprovals(resourceApprovals);
		permissions.setSelectedResources(selectedResources);
		permissions.setAvailableResources(otherResourcesMap);
		return permissions;
	}
	
	@RequestMapping(value="/dev/resourceparams",method=RequestMethod.POST)
	public @ResponseBody Response createProperty(@RequestBody ResourceParameter rp) {
		Response response = new Response();
		response.setResponseCode(RESPONSE.OK);
		try {
			checkClientIdOwnership(rp.getClientId());
			resourceAdapter.storeResourceParameter(rp);
			response.setData(rp);
		} catch (Exception e) {
			logger.error("Failure reading permissions model: "+e.getMessage(),e);
			response.setErrorMessage("Failure reading permissions model: "+e.getMessage());
			response.setResponseCode(RESPONSE.ERROR);
		}
		return response;
	}

	@RequestMapping(value="/dev/resourceparams/{clientId}/{resourceId}/{value}",method=RequestMethod.DELETE)
	public @ResponseBody Response deleteProperty(@PathVariable String clientId, @PathVariable String resourceId, @PathVariable String value) {
		Response response = new Response();
		response.setResponseCode(RESPONSE.OK);
		try {
			checkClientIdOwnership(clientId);
			resourceAdapter.removeResourceParameter(resourceId, value, clientId);
		} catch (Exception e) {
			logger.error("Failure reading permissions model: "+e.getMessage(),e);
			response.setErrorMessage("Failure reading permissions model: "+e.getMessage());
			response.setResponseCode(RESPONSE.ERROR);
		}
		return response;
	}

}
