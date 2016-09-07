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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.Service;
import eu.trentorise.smartcampus.permissionprovider.manager.ClientDetailsManager;
import eu.trentorise.smartcampus.permissionprovider.manager.ResourceManager;
import eu.trentorise.smartcampus.permissionprovider.model.BasicClientInfo;
import eu.trentorise.smartcampus.permissionprovider.model.ClientAppBasic;
import eu.trentorise.smartcampus.permissionprovider.model.ClientDetailsEntity;
import eu.trentorise.smartcampus.permissionprovider.model.ClientModel;
import eu.trentorise.smartcampus.permissionprovider.model.Permission;
import eu.trentorise.smartcampus.permissionprovider.model.PermissionData;
import eu.trentorise.smartcampus.permissionprovider.model.Resource;
import eu.trentorise.smartcampus.permissionprovider.model.ResourceParameter;
import eu.trentorise.smartcampus.permissionprovider.model.Scope;
import eu.trentorise.smartcampus.permissionprovider.model.Scope.ACCESS_TYPE;
import eu.trentorise.smartcampus.permissionprovider.model.ServiceParameterModel;
import eu.trentorise.smartcampus.permissionprovider.oauth.ResourceServices;
import eu.trentorise.smartcampus.permissionprovider.repository.ClientDetailsRepository;

/**
 * Controller for remote check the access to the resource
 * 
 * @author raman
 *
 */
@Controller
public class ResourceAccessController {

	private static Log logger = LogFactory.getLog(ResourceAccessController.class);
	@Autowired
	private ResourceServices resourceServices;
	@Autowired
	private ResourceServerTokenServices resourceServerTokenServices;
	@Autowired
	private ClientDetailsRepository clientDetailsRepository;
	@Autowired
	private ResourceManager resourceManager;
	@Autowired
	private ClientDetailsManager clientDetailsManager;
	

	private static ResourceFilterHelper resourceFilterHelper = new ResourceFilterHelper();

	/**
	 * Check the access to the specified resource using the client app token header
	 * @param token
	 * @param resourceUri
	 * @param request
	 * @return
	 */
	@RequestMapping("/resources/access")
	public @ResponseBody Boolean canAccessResource(@RequestHeader("Authorization") String token, @RequestParam String scope, HttpServletRequest request) {
		try {
			String parsedToken = resourceFilterHelper.parseTokenFromRequest(request);
			OAuth2Authentication auth = resourceServerTokenServices.loadAuthentication(parsedToken);
			Collection<String> actualScope = auth.getAuthorizationRequest().getScope();
			String asString = StringUtils.collectionToCommaDelimitedString(actualScope);
			actualScope = StringUtils.commaDelimitedListToSet(asString.toLowerCase());
			Collection<String> scopeSet = StringUtils.commaDelimitedListToSet(scope.toLowerCase());
			if (actualScope != null && !actualScope.isEmpty() && actualScope.containsAll(scopeSet)) {
				return true;
			}
		} catch (AuthenticationException e) {
			logger.error("Error validating token: "+e.getMessage());
		}
		return false;
	}
	
	/**
	 * Get information about the client handling the specified token.
	 * @param token
	 * @param resourceUri
	 * @param request
	 * @return
	 */
	@RequestMapping("/resources/clientinfo")
	public @ResponseBody BasicClientInfo getClientInfo(@RequestHeader("Authorization") String token, HttpServletRequest request, HttpServletResponse response) {
		try {
			String parsedToken = resourceFilterHelper.parseTokenFromRequest(request);
			OAuth2Authentication auth = resourceServerTokenServices.loadAuthentication(parsedToken);
			String clientId = auth.getAuthorizationRequest().getClientId();
			if (clientId != null) {
				ClientDetailsEntity client = clientDetailsRepository.findByClientId(clientId);
				if (client != null) {
					BasicClientInfo info = new BasicClientInfo();
					info.setClientId(clientId);
					info.setClientName((String)client.getAdditionalInformation().get("name"));
					return info;
				}
			}
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
			
		} catch (AuthenticationException e) {
			logger.error("Error getting information about client: "+e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}
	}	
	
	/**
	 * Get information about the client handling the specified token.
	 * @param token
	 * @param resourceUri
	 * @param request
	 * @return
	 */
	@RequestMapping("/resources/clientspec")
	public @ResponseBody ClientModel getClientSpec(@RequestHeader("Authorization") String token, HttpServletRequest request, HttpServletResponse response) {
		try {
			String parsedToken = resourceFilterHelper.parseTokenFromRequest(request);
			OAuth2Authentication auth = resourceServerTokenServices.loadAuthentication(parsedToken);
			String clientId = auth.getAuthorizationRequest().getClientId();
			if (clientId != null) {
				ClientDetailsEntity client = clientDetailsRepository.findByClientId(clientId);
				if (client != null) {
					ClientModel model = ClientModel.fromClient(client);
//					Set<String> scopes = model.getScopes();
//					for (String scope: scopes) {
//						Resource r = resourceManager.getResource(scope);
//						if (r != null) {
//							
//						}
//					}
					List<ResourceParameter> params = resourceManager.getOwnResourceParameters(clientId);
					if (params != null) {
						model.setOwnParameters(new HashSet<ServiceParameterModel>());
						for (ResourceParameter rp : params) {
							ServiceParameterModel spm = new ServiceParameterModel();
							spm.setName(rp.getParameter());
							spm.setService(rp.getService().getServiceName());
							spm.setValue(rp.getValue());
							spm.setVisibility(rp.getVisibility());
							model.getOwnParameters().add(spm);
						}
					}
					return model;
				}
			}
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
			
		} catch (AuthenticationException e) {
			logger.error("Error getting information about client: "+e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}
	}		
	
	@RequestMapping(value = "/resources/clientspec", method=RequestMethod.POST)
	public @ResponseBody ClientModel createClientSpec(@RequestHeader("Authorization") String token, @RequestBody ClientModel model, HttpServletRequest request, HttpServletResponse response) {
		try {
			String parsedToken = resourceFilterHelper.parseTokenFromRequest(request);
			OAuth2Authentication auth = resourceServerTokenServices.loadAuthentication(parsedToken);
			String clientId = auth.getAuthorizationRequest().getClientId();
			if (clientId != null) {
				try {
					clientDetailsManager.createNew(model, clientDetailsRepository.findByClientId(clientId).getDeveloperId());
				} catch (Exception e) {
					logger.error("Error creating client: "+e.getMessage());
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
				return model;
			}
		} catch (AuthenticationException e) {
			logger.error("Error getting information about client: "+e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}

		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return null;
	}

	
	@RequestMapping("/resources/permissions")
	public @ResponseBody PermissionData getServicePermissions(HttpServletRequest request, HttpServletResponse response) {
		PermissionData result = new PermissionData();
		result.setPermissions(new LinkedList<Permission>());
		
		Map<String,List<Scope>> map = new HashMap<String, List<Scope>>();
		List<Resource> resources = resourceManager.getAllAvailableResources();
		for (Resource r : resources) {
			String id = r.getService().getServiceId();
			List<Scope> list = map.get(id);
			if (list == null) {
				list = new LinkedList<Scope>();
				map.put(id, list);
			}
			Scope s = new Scope();
			s.setId(r.getResourceUri());
			s.setDescription(r.getDescription());
			s.setAccess_type(ACCESS_TYPE.fromAuthority(r.getAuthority()));
			list.add(s);
		}
		
		List<Service> serviceObjects = resourceManager.getServiceObjects();
		for (Service s : serviceObjects) {
			Permission permission = new Permission();
			permission.setName(s.getName());
			permission.setDescription(s.getDescription());
			permission.setScopes(map.get(s.getId()));
			result.getPermissions().add(permission);
		}
		
		return result;
	}

	
	private static class ResourceFilterHelper extends OAuth2AuthenticationProcessingFilter {
		public String parseTokenFromRequest(HttpServletRequest request) {
			return parseToken(request);
		} 
	}
}
