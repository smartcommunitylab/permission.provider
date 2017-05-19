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

import java.io.IOException;
import java.util.ArrayList;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.trentorise.smartcampus.network.JsonUtils;
import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.Service;
import eu.trentorise.smartcampus.permissionprovider.manager.ClientDetailsManager;
import eu.trentorise.smartcampus.permissionprovider.manager.ResourceManager;
import eu.trentorise.smartcampus.permissionprovider.manager.WeLiveLogger;
import eu.trentorise.smartcampus.permissionprovider.model.BasicClientInfo;
import eu.trentorise.smartcampus.permissionprovider.model.ClientDetailsEntity;
import eu.trentorise.smartcampus.permissionprovider.model.ClientModel;
import eu.trentorise.smartcampus.permissionprovider.model.Permission;
import eu.trentorise.smartcampus.permissionprovider.model.PermissionData;
import eu.trentorise.smartcampus.permissionprovider.model.Resource;
import eu.trentorise.smartcampus.permissionprovider.model.ResourceParameter;
import eu.trentorise.smartcampus.permissionprovider.model.Response;
import eu.trentorise.smartcampus.permissionprovider.model.Response.RESPONSE;
import eu.trentorise.smartcampus.permissionprovider.model.Scope;
import eu.trentorise.smartcampus.permissionprovider.model.Scope.ACCESS_TYPE;
import eu.trentorise.smartcampus.permissionprovider.model.ServiceParameterModel;
import eu.trentorise.smartcampus.permissionprovider.oauth.AutoJdbcTokenStore;
import eu.trentorise.smartcampus.permissionprovider.repository.ClientDetailsRepository;

/**
 * Controller for remote check the access to the resource
 * 
 * @author raman
 *
 */
@Controller
public class ResourceAccessController extends AbstractController {

	private static Log logger = LogFactory.getLog(ResourceAccessController.class);
	@Autowired
	private WeLiveLogger weliveLogger;
	@Autowired
	private ResourceServerTokenServices resourceServerTokenServices;
	@Autowired
	private ClientDetailsRepository clientDetailsRepository;
	@Autowired
	private ResourceManager resourceManager;
	@Autowired
	private ClientDetailsManager clientDetailsManager;
	@Autowired
	private AutoJdbcTokenStore autoJdbcTokenStore;
	@Value("${api.token}")
	private String token;
	private ObjectMapper mapper = new ObjectMapper();

	private static ResourceFilterHelper resourceFilterHelper = new ResourceFilterHelper();

	/**
	 * Check the access to the specified resource using the client app token
	 * header
	 * 
	 * @param token
	 * @param resourceUri
	 * @param request
	 * @return
	 */
	@RequestMapping("/resources/access")
	public @ResponseBody Boolean canAccessResource(@RequestHeader("Authorization") String token,
			@RequestParam String scope, HttpServletRequest request) {
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
			logger.error("Error validating token: " + e.getMessage());
		}
		return false;
	}

	/**
	 * Get information about the client handling the specified token.
	 * 
	 * @param token
	 * @param resourceUri
	 * @param request
	 * @return
	 */
	@RequestMapping("/resources/clientinfo")
	public @ResponseBody BasicClientInfo getClientInfo(@RequestHeader("Authorization") String token,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			String parsedToken = resourceFilterHelper.parseTokenFromRequest(request);
			OAuth2Authentication auth = resourceServerTokenServices.loadAuthentication(parsedToken);
			String clientId = auth.getAuthorizationRequest().getClientId();
			if (clientId != null) {
				ClientDetailsEntity client = clientDetailsRepository.findByClientId(clientId);
				if (client != null) {
					BasicClientInfo info = new BasicClientInfo();
					info.setClientId(clientId);
					info.setClientName((String) client.getAdditionalInformation().get("name"));
					return info;
				}
			}
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);

		} catch (AuthenticationException e) {
			logger.error("Error getting information about client: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		}
		return null;
	}

	/**
	 * Get all authorization about the client handling the specified token.
	 * 
	 * @param token
	 * @param resourceUri
	 * @param request
	 * @return
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/resources/clientinfo/oauth")
	public @ResponseBody List<BasicClientInfo> getClientOAuthInfo(@RequestHeader("Authorization") String token,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			String parsedToken = resourceFilterHelper.parseTokenFromRequest(request);
			OAuth2Authentication auth = resourceServerTokenServices.loadAuthentication(parsedToken);

			if (auth.getName() != null && !auth.getName().isEmpty()) {
				List<BasicClientInfo> infos = new ArrayList<BasicClientInfo>();
				String userId = auth.getName();
				// get different client_id for user in oauth_access_token
				// collection.
				List<Map<String, Object>> oAuthTokens = autoJdbcTokenStore.findClientIdsByUserName(userId);
				// loop through client_id and create info obj for each client_id
				// and add to list.
				for (Map<String, Object> oAuth2AccessTokenMap : oAuthTokens) {
					if (oAuth2AccessTokenMap.containsKey("client_id")) {
						String json = (String) oAuth2AccessTokenMap.get("additional_information");
						Map<String, Object> clientDetails = mapper.readValue(json, Map.class);
						String clientId = String.valueOf(oAuth2AccessTokenMap.get("client_id"));
						BasicClientInfo info = new BasicClientInfo();
						info.setClientId(clientId);
						info.setClientName(String.valueOf(clientDetails.get("name")));
						infos.add(info);
					}
				}
				return infos;
			} else {
				logger.error("Error getting information about client");
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			}

		} catch (AuthenticationException e) {
			logger.error("Error getting information about client: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		} catch (JsonParseException e) {
			logger.error(e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (JsonMappingException e) {
			logger.error(e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			logger.error(e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		return null;
	}

	/**
	 * Get all authorization about the client handling the user name.
	 * 
	 * @param token
	 * @param resourceUri
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/resources/clientinfo/oauth/{userId}")
	public @ResponseBody List<BasicClientInfo> getClientOAuthInfoByUserName(
			@RequestHeader("Authorization") String token, @PathVariable String userId, HttpServletRequest request,
			HttpServletResponse response) {

		try {
			if (token == null || !token.matches(getAPICredentials())) {
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				return null;
			}

			if (userId != null && !userId.isEmpty()) {
				List<BasicClientInfo> infos = new ArrayList<BasicClientInfo>();
				// get different client_id for user in oauth_access_token
				// collection.
				List<Map<String, Object>> oAuthTokens = autoJdbcTokenStore.findClientIdsByUserName(userId);
				// loop through client_id and create info obj per client_id.
				for (Map<String, Object> oAuth2AccessTokenMap : oAuthTokens) {
					if (oAuth2AccessTokenMap.containsKey("client_id")) {
						String json = (String) oAuth2AccessTokenMap.get("additional_information");
						Map<String, Object> clientDetails = mapper.readValue(json, Map.class);
						String clientId = String.valueOf(oAuth2AccessTokenMap.get("client_id"));
						BasicClientInfo info = new BasicClientInfo();
						info.setClientId(clientId);
						info.setClientName(String.valueOf(clientDetails.get("name")));
						infos.add(info);
					}
				}

				return infos;

			} else {
				logger.error("Error getting information about client");
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			}

		} catch (AuthenticationException e) {
			logger.error("Error getting information about client: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		} catch (JsonParseException e) {
			logger.error(e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (JsonMappingException e) {
			logger.error(e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			logger.error(e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		return null;
	}

	/**
	 * Get information about the client handling the specified token.
	 * 
	 * @param token
	 * @param resourceUri
	 * @param request
	 * @return
	 */
	@RequestMapping("/resources/clientspec")
	public @ResponseBody ClientModel getClientSpec(@RequestHeader("Authorization") String token,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			String parsedToken = resourceFilterHelper.parseTokenFromRequest(request);
			OAuth2Authentication auth = resourceServerTokenServices.loadAuthentication(parsedToken);
			if (auth.isClientOnly()) {
				String clientId = auth.getAuthorizationRequest().getClientId();
				if (clientId != null) {
					ClientDetailsEntity client = clientDetailsRepository.findByClientId(clientId);
					if (client != null) {
						ClientModel model = ClientModel.fromClient(client);
						// Set<String> scopes = model.getScopes();
						// for (String scope: scopes) {
						// Resource r = resourceManager.getResource(scope);
						// if (r != null) {
						//
						// }
						// }
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
			} else {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			}

		} catch (AuthenticationException e) {
			logger.error("Error getting information about client: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}

		return null;
	}

	@RequestMapping(value = "/resources/clientspec", method = RequestMethod.POST)
	public @ResponseBody ClientModel createClientSpec(@RequestHeader("Authorization") String token,
			@RequestBody ClientModel model, HttpServletRequest request, HttpServletResponse response) {
		try {
			String parsedToken = resourceFilterHelper.parseTokenFromRequest(request);
			OAuth2Authentication auth = resourceServerTokenServices.loadAuthentication(parsedToken);
			String clientId = auth.getAuthorizationRequest().getClientId();
			// client only.
			if (auth.isClientOnly()) { // client only.
				if (clientId != null) {
					try {
						model = clientDetailsManager.createNewFromModel(model,
								clientDetailsRepository.findByClientId(clientId).getDeveloperId());
					} catch (Exception e) {
						logger.error("Error creating client: " + e.getMessage());
						response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
						response.setContentType(MediaType.APPLICATION_JSON_VALUE);
						Response r = new Response();
						r.setErrorMessage(e.getMessage());
						r.setResponseCode(RESPONSE.ERROR);
						try {
							response.getWriter().print(JsonUtils.toJSON(r));
						} catch (IOException e1) {
						}
						return null;
					}
					return model;
				}				
			} else {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return null;
			}
		} catch (AuthenticationException e) {
			logger.error("Error creating client: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return null;
	}

	@RequestMapping(value = "/resources/clientspec", method = RequestMethod.PUT)
	public @ResponseBody ClientModel updateClientSpec(@RequestHeader("Authorization") String token,
			@RequestBody ClientModel model, HttpServletRequest request, HttpServletResponse response) {
		try {
			String parsedToken = resourceFilterHelper.parseTokenFromRequest(request);
			OAuth2Authentication auth = resourceServerTokenServices.loadAuthentication(parsedToken);
			if (auth.isClientOnly()) { //client only.
				String clientId = auth.getAuthorizationRequest().getClientId();

				if (clientId != null) {
					ClientDetailsEntity ownerClient = clientDetailsRepository.findByClientId(clientId);
					ClientDetailsEntity updatedClient = clientDetailsRepository.findByClientId(model.getClientId());
					if (ownerClient == null || updatedClient == null)
						throw new BadCredentialsException("No client found");

					if (!ownerClient.getDeveloperId().equals(updatedClient.getDeveloperId())) {
						throw new BadCredentialsException("Not authorized");
					}
					try {
						model = clientDetailsManager.updateFromModel(model,
								clientDetailsRepository.findByClientId(clientId).getDeveloperId());
					} catch (Exception e) {
						logger.error("Error creating client: " + e.getMessage());
						response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
						response.setContentType(MediaType.APPLICATION_JSON_VALUE);
						Response r = new Response();
						r.setErrorMessage(e.getMessage());
						r.setResponseCode(RESPONSE.ERROR);
						try {
							response.getWriter().print(JsonUtils.toJSON(r));
						} catch (IOException e1) {
						}
						return null;
					}
					return model;
				}
			} else {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return null;
			}

		} catch (AuthenticationException e) {
			logger.error("Error creating client: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}

		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return null;
	}

	@RequestMapping("/resources/permissions")
	public @ResponseBody PermissionData getServicePermissions(HttpServletRequest request,
			HttpServletResponse response) {
		PermissionData result = new PermissionData();
		result.setPermissions(new LinkedList<Permission>());

		Map<String, List<Scope>> map = new HashMap<String, List<Scope>>();
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
   /** disabled on 10-04-17 as potentially powerful operation
	@RequestMapping(method = RequestMethod.DELETE, value = "/user/me")
	public @ResponseBody Response deleteUser(@RequestHeader("Authorization") String token,
			@RequestParam(required = false) Boolean cascade, HttpServletRequest req, HttpServletResponse res) {

    Long userId = null;
    Response result = new Response();
		result.setResponseCode(RESPONSE.OK);
		result.setCode(HttpStatus.OK.value());
		result.setErrorMessage("Action completed correctly.");

		try {
			userId = getUserId();
			if (userId == null) {
				result.setErrorMessage("Action failed because the ccUserID does not exist into the DB.");
				result.setResponseCode(RESPONSE.ERROR);
				result.setCode(HttpStatus.NOT_FOUND.value());
			}
			resourceManager.deleteUserData(cascade, userId, result, res);
			
		} catch (Exception e) {
			// 500 - Action failed, no more details are provided
			logger.error(e.getMessage());
			res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			result.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			result.setErrorMessage("Action failed, no more details are provided");
			result.setResponseCode(RESPONSE.ERROR);
		}

		return result;
	}**/

	@RequestMapping(method = RequestMethod.DELETE, value = "/user/{ccUserId}")
	public @ResponseBody Response deleteUserDataByUserId(@RequestHeader("Authorization") String token,
			@PathVariable Long ccUserId, @RequestParam(required = false) Boolean cascade, HttpServletRequest req,
			HttpServletResponse res) {

		Response result = new Response();
		result.setResponseCode(RESPONSE.OK);
		result.setCode(HttpStatus.OK.value());
		result.setErrorMessage("Action completed correctly.");
		
		try {

			if (token == null || !token.matches(getAPICredentials())) {
				res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				result.setErrorMessage("Action failed, authorization error.");
				result.setResponseCode(RESPONSE.ERROR);
				result.setCode(HttpServletResponse.SC_UNAUTHORIZED);
			} else {
				resourceManager.deleteUserData(cascade, ccUserId, result, res);
				Map<String,Object> logMap = new HashMap<String, Object>();
				logMap.put("userid", ""+ccUserId);
				logMap.put("cascade", String.valueOf(cascade));
				weliveLogger.log(WeLiveLogger.USER_DELETED, logMap);
			}
		} catch (Exception e) {
			// 500 - Action failed, no more details are provided
			logger.error(e.getMessage());
			res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			result.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			result.setErrorMessage("Action failed, no more details are provided");
			result.setResponseCode(RESPONSE.ERROR);
		}

		return result;

	}

	/**
	 * @return
	 */
	private String getAPICredentials() {
		return "Basic " + token;
	}
}
