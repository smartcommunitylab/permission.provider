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
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import eu.trentorise.smartcampus.permissionprovider.Config.AUTHORITY;
import eu.trentorise.smartcampus.permissionprovider.model.Resource;
import eu.trentorise.smartcampus.permissionprovider.repository.ResourceRepository;

/**
 * Controller for retrieving the model for and displaying the confirmation page for access to a protected resource.
 * 
 */
@Controller
@SessionAttributes("authorizationRequest")
public class AccessConfirmationController {

	private static Log logger = LogFactory.getLog(AccessConfirmationController.class);
	
	@Autowired
	private ClientDetailsService clientDetailsService;
	@Autowired
	private ResourceRepository resourceRepository;

	@RequestMapping("/oauth/confirm_access")
	public ModelAndView getAccessConfirmation(Map<String, Object> model) throws Exception {
		AuthorizationRequest clientAuth = (AuthorizationRequest) model.remove("authorizationRequest");
		ClientDetails client = clientDetailsService.loadClientByClientId(clientAuth.getClientId());
		List<Resource> resources = new ArrayList<Resource>();
		if (clientAuth.getResourceIds() != null) {
			for (String rId : client.getResourceIds()) {
				try {
					Resource r = resourceRepository.findOne(Long.parseLong(rId));
					if (r.getAuthority().equals(AUTHORITY.USER)) {
						resources.add(r);
					}
				} catch (Exception e) {
					logger.error("Error reading resource with id "+rId+": "+e.getMessage());
				}
			}
		}
		model.put("resources", resources);
		model.put("auth_request", clientAuth);
		model.put("client", client);
		return new ModelAndView("access_confirmation", model);
	}

	@RequestMapping("/oauth/error")
	public String handleError(Map<String,Object> model) throws Exception {
		model.put("message", "There was a problem with the OAuth2 protocol");
		return "oauth_error";
	}

	@Autowired
	public void setClientDetailsService(ClientDetailsService clientDetailsService) {
		this.clientDetailsService = clientDetailsService;
	}
}
