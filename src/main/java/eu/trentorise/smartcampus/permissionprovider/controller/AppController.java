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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import eu.trentorise.smartcampus.permissionprovider.manager.AdminManager;
import eu.trentorise.smartcampus.permissionprovider.manager.AttributesAdapter;
import eu.trentorise.smartcampus.permissionprovider.manager.ClientDetailsAdapter;
import eu.trentorise.smartcampus.permissionprovider.model.Attribute;
import eu.trentorise.smartcampus.permissionprovider.model.ClientAppBasic;
import eu.trentorise.smartcampus.permissionprovider.model.ClientAppInfo;
import eu.trentorise.smartcampus.permissionprovider.model.ClientDetailsEntity;
import eu.trentorise.smartcampus.permissionprovider.model.Response;
import eu.trentorise.smartcampus.permissionprovider.model.Response.RESPONSE;
import eu.trentorise.smartcampus.permissionprovider.model.User;
import eu.trentorise.smartcampus.permissionprovider.repository.ClientDetailsRepository;
import eu.trentorise.smartcampus.permissionprovider.repository.UserRepository;

/**
 * Controller for performing the basic operations over the 
 * client apps.
 * @author raman
 *
 */
@Controller
@Transactional
public class AppController extends AbstractController {

	private Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private ClientDetailsRepository clientDetailsRepository;
	@Autowired
	private ClientDetailsAdapter clientDetailsAdapter;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AttributesAdapter attributesAdapter;
	@Autowired
	private AdminManager adminManager;
	@Value("${mode.restricted}")
	private boolean accessMode;

	/**
	 * Retrieve the with the user data: currently on the username is added.
	 * @return
	 */
	@RequestMapping("/dev")
	public ModelAndView developer() {
		User user =  userRepository.findOne(getUserId());
		Map<String,Object> model = new HashMap<String, Object>();

		if (accessMode) {
			String authority = getUserAuthority();
			Set<String> identityAttrs = new HashSet<String>();
			for (Attribute a : user.getAttributeEntities()) {
				if (a.getAuthority().getName().equals(authority) && 
					attributesAdapter.isIdentityAttr(a)) {
					identityAttrs.add(authority+";"+a.getKey()+";"+a.getValue());
				}
			}
			
			try {
				if (!adminManager.checkAccount(identityAttrs)) {
					model.put("error", "Not authorized");
					return new ModelAndView("redirect:/logout");
				}
			} catch (Exception e) {
				model.put("error", e.getMessage());
				logger.error("Problem checking user account: "+e.getMessage());
				return new ModelAndView("redirect:/logout");
			}
		}
		
		String username = getUserName(user);
		model.put("username",username);
		return new ModelAndView("index", model);
	}
	
	/**
	 * Read the 
	 * @return {@link Response} entity containing the list of client app {@link ClientAppBasic} descriptors
	 */
	@RequestMapping("/dev/apps")
	public @ResponseBody Response getAppList() {
		Response response = new Response();
		response.setResponseCode(RESPONSE.OK);
		try {
			// read all the apps associated to the signed user
			List<ClientAppBasic> list = clientDetailsAdapter.convertToClientApps(clientDetailsRepository.findByDeveloperId(getUserId()));
			response.setData(list);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			response.setResponseCode(RESPONSE.ERROR);
			response.setErrorMessage(e.getMessage());
		}
		return response;
	}
	
	/**
	 * create a new client app given a container with the name only
	 * @param appData
	 * @return {@link Response} entity containing the stored app {@link ClientAppBasic} descriptor
	 * @throws Exception
	 */
	@RequestMapping(method=RequestMethod.POST,value="/dev/apps")
	public @ResponseBody Response saveEmpty(@RequestBody ClientAppBasic appData) throws Exception {
		Response response = new Response();
		response.setResponseCode(RESPONSE.OK);
		try {
			ClientDetailsEntity entity = new ClientDetailsEntity();
			ClientAppInfo info = new ClientAppInfo();
			info.setName(appData.getName());
			for (ClientDetailsEntity cde : clientDetailsRepository.findAll()) {
				if (ClientAppInfo.convert(cde.getAdditionalInformation()).getName().equals(appData.getName())) {
					throw new IllegalArgumentException("An app with the same name already exists");
				}
			}
			entity.setAdditionalInformation(info.toJson());
			entity.setClientId(clientDetailsAdapter.generateClientId());
			entity.setAuthorities(clientDetailsAdapter.defaultAuthorities());
			entity.setAuthorizedGrantTypes(clientDetailsAdapter.defaultGrantTypes());
			entity.setDeveloperId(getUserId());
			entity.setClientSecret(clientDetailsAdapter.generateClientSecret());
			entity.setClientSecretMobile(clientDetailsAdapter.generateClientSecret());

			entity = clientDetailsRepository.save(entity);
			response.setData(clientDetailsAdapter.convertToClientApp(entity));
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			response.setResponseCode(RESPONSE.ERROR);
			response.setErrorMessage(e.getMessage());
		}
		return response;
	}

	@RequestMapping(method=RequestMethod.POST,value="/dev/apps/{clientId}")
	public @ResponseBody Response resetClientData(@PathVariable String clientId,@RequestParam String reset) {
		return reset(clientId, "clientSecretMobile".equals(reset));
	}

	/**
	 * Reset clientId or client secret
	 * @param clientId
	 * @param resetClientSecretMobile true to reset clientSecretMobile, false to reset clientSecret
	 * @return {@link Response} entity containing the stored app {@link ClientAppBasic} descriptor
	 */
	protected Response reset(String clientId, boolean resetClientSecretMobile) {
		Response response = new Response();
		response.setResponseCode(RESPONSE.OK);
		try {
			checkClientIdOwnership(clientId);
			if (resetClientSecretMobile) {
				response.setData(clientDetailsAdapter.convertToClientApp(clientDetailsAdapter.resetClientSecretMobile(clientId)));
			} else {
				response.setData(clientDetailsAdapter.convertToClientApp(clientDetailsAdapter.resetClientSecret(clientId)));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			response.setResponseCode(RESPONSE.ERROR);
			response.setErrorMessage(e.getMessage());
		}
		return response;
	}

	/**
	 * Delete the specified app
	 * @param clientId
	 * @return {@link Response} entity containing the deleted app {@link ClientAppBasic} descriptor
	 */
	@RequestMapping(method=RequestMethod.DELETE,value="/dev/apps/{clientId}")
	public @ResponseBody Response delete(@PathVariable String clientId) {
		Response response = new Response();
		response.setResponseCode(RESPONSE.OK);
		try {
			checkClientIdOwnership(clientId);
			ClientDetailsEntity client = clientDetailsRepository.findByClientId(clientId);
			if (client == null) {
				response.setResponseCode(RESPONSE.ERROR);
				response.setErrorMessage("client app not found");
				return response;
			}
			clientDetailsRepository.delete(client);
			response.setData(clientDetailsAdapter.convertToClientApp(client));
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			response.setResponseCode(RESPONSE.ERROR);
			response.setErrorMessage(e.getMessage());
		}
		return response;
	}

	/**
	 * Update the client app
	 * @param data
	 * @param clientId
	 * @return {@link Response} entity containing the updated app {@link ClientAppBasic} descriptor
	 */
	@RequestMapping(method=RequestMethod.PUT,value="/dev/apps/{clientId}")
	public @ResponseBody Response update(@RequestBody ClientAppBasic data, @PathVariable String clientId) {
		Response response = new Response();
		response.setResponseCode(RESPONSE.OK);
		try {
			checkClientIdOwnership(clientId);
			ClientDetailsEntity client = clientDetailsRepository.findByClientId(clientId);
			String error = null;
			if  ((error = clientDetailsAdapter.validate(client,data)) != null) {
				response.setResponseCode(RESPONSE.ERROR);
				response.setErrorMessage(error);
				return response;
			}
			client = clientDetailsAdapter.convertFromClientApp(client,data);
			if (client != null) {
				clientDetailsRepository.save(client);
				response.setData(clientDetailsAdapter.convertToClientApp(client));
			} else {
				logger.error("Problem converting the client");
				response.setResponseCode(RESPONSE.ERROR);
				response.setErrorMessage("internal error");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			response.setResponseCode(RESPONSE.ERROR);
			response.setErrorMessage(e.getMessage());
		}
		return response;
	}

}
