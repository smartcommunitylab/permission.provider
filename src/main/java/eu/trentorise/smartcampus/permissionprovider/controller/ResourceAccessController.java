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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.permissionprovider.oauth.ResourceServices;
import eu.trentorise.smartcampus.permissionprovider.repository.ClientDetailsRepository;

/**
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

	@RequestMapping("/resources/{resourceUri}/access")
	public @ResponseBody Boolean canAccessResource(@RequestHeader("Authorization") String token, @PathVariable String resourceUri, HttpServletRequest request) {
		try {
			String parsedToken = resourceFilterHelper.parseTokenFromRequest(request);
			OAuth2Authentication auth = resourceServerTokenServices.loadAuthentication(parsedToken);
			Collection<String> scope = auth.getAuthorizationRequest().getScope();
			if (scope != null && !scope.isEmpty() && scope.contains(resourceUri)) {
				return true;
			}
		} catch (AuthenticationException e) {
			logger.error("Error validating token: "+e.getMessage());
		}
		return false;
	}
	
	private static class ResourceFilterHelper extends OAuth2AuthenticationProcessingFilter {
		public String parseTokenFromRequest(HttpServletRequest request) {
			return parseToken(request);
		} 
	}
}
