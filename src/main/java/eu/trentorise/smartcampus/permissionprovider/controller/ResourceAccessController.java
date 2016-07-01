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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.permissionprovider.model.BasicClientInfo;
import eu.trentorise.smartcampus.permissionprovider.model.ClientDetailsEntity;
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
	public @ResponseBody BasicClientInfo getCLientInfo(@RequestHeader("Authorization") String token, HttpServletRequest request, HttpServletResponse response) {
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
	private static class ResourceFilterHelper extends OAuth2AuthenticationProcessingFilter {
		public String parseTokenFromRequest(HttpServletRequest request) {
			return parseToken(request);
		} 
	}
}
