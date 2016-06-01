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

import javax.servlet.http.HttpServletRequest;

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
import eu.trentorise.smartcampus.permissionprovider.manager.AdminManager.ROLE;
import eu.trentorise.smartcampus.permissionprovider.manager.AttributesAdapter;
import eu.trentorise.smartcampus.permissionprovider.manager.ClientDetailsManager;
import eu.trentorise.smartcampus.permissionprovider.manager.WeLiveLogger;
import eu.trentorise.smartcampus.permissionprovider.model.Attribute;
import eu.trentorise.smartcampus.permissionprovider.model.ClientAppBasic;
import eu.trentorise.smartcampus.permissionprovider.model.Identity;
import eu.trentorise.smartcampus.permissionprovider.model.Response;
import eu.trentorise.smartcampus.permissionprovider.model.Response.RESPONSE;
import eu.trentorise.smartcampus.permissionprovider.model.User;
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

	@Autowired
	private WeLiveLogger weliveLogger;
	
	private Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private ClientDetailsManager clientDetailsAdapter;
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
	public ModelAndView developer(HttpServletRequest request) {
		User user =  userRepository.findOne(getUserId());
		Map<String,Object> model = new HashMap<String, Object>();

		if (accessMode) {
			String authority = getUserAuthority();
			Set<Identity> identityAttrs = new HashSet<Identity>();
			for (Attribute a : user.getAttributeEntities()) {
				if (a.getAuthority().getName().equals(authority) && 
					attributesAdapter.isIdentityAttr(a)) 
				{
					identityAttrs.add(new Identity(authority, a.getKey(), a.getValue()));
				}
			}
			
			try {
				if (!adminManager.checkAccount(identityAttrs, ROLE.developer)) {
					model.put("error", "Not authorized");
//					return new ModelAndView("redirect:/logout");
					return new ModelAndView("accesserror",model);
				}
			} catch (Exception e) {
				model.put("error", e.getMessage());
				logger.error("Problem checking user account: "+e.getMessage());
//				return new ModelAndView("redirect:/logout");
				return new ModelAndView("accesserror",model);
			}
		}
		
		Map<String,Object> logMap = new HashMap<String, Object>();
		logMap.put("userid", ""+user.getId());
		weliveLogger.log(WeLiveLogger.USER_DEVELOPER_ACCESS, logMap);
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
			List<ClientAppBasic> list = clientDetailsAdapter.getByDeveloperId(getUserId());
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
			response.setData(clientDetailsAdapter.create(appData, getUserId()));
			Map<String,Object> logMap = new HashMap<String, Object>();
			logMap.put("userid", ""+getUserId());
			logMap.put("clientapp", ""+appData.getName());
			weliveLogger.log(WeLiveLogger.CLIENT_APP_CREATED, logMap);
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
				response.setData(clientDetailsAdapter.resetClientSecretMobile(clientId));
			} else {
				response.setData(clientDetailsAdapter.resetClientSecret(clientId));
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
			ClientAppBasic appData = clientDetailsAdapter.delete(clientId);
			response.setData(appData);
			Map<String,Object> logMap = new HashMap<String, Object>();
			logMap.put("userid", ""+getUserId());
			logMap.put("clientapp", ""+appData.getName());
			weliveLogger.log(WeLiveLogger.CLIENT_APP_DELETED, logMap);

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
			response.setData(clientDetailsAdapter.update(clientId, data));
			Map<String,Object> logMap = new HashMap<String, Object>();
			logMap.put("userid", ""+getUserId());
			logMap.put("clientapp", ""+data.getName());
			weliveLogger.log(WeLiveLogger.CLIENT_APP_DELETED, logMap);

		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			response.setResponseCode(RESPONSE.ERROR);
			response.setErrorMessage(e.getMessage());
		}

		return response;
	}

}
