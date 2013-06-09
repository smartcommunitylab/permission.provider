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
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import eu.trentorise.smartcampus.permissionprovider.adapters.ClientDetailsAdapter;
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
	
	@RequestMapping("/dev")
	public ModelAndView developer() {
		Map<String,Object> model = new HashMap<String, Object>();
		User user =  userRepository.findOne(getUserId());
		String username = getUserName(user);
		model.put("username",username);
		return new ModelAndView("index", model);
	}
	
	@RequestMapping("/dev/apps")
	public @ResponseBody Response getAppList() {
		Response response = new Response();
		response.setResponseCode(RESPONSE.OK);
		try {
			List<ClientAppBasic> list = clientDetailsAdapter.convertToClientApps(clientDetailsRepository.findByDeveloperId(getUserId()));
			response.setData(list);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			response.setResponseCode(RESPONSE.ERROR);
			response.setErrorMessage(e.getMessage());
		}
		return response;
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/dev/apps")
	public @ResponseBody Response saveEmpty(@RequestBody ClientAppBasic appData) throws Exception {
		Response response = new Response();
		response.setResponseCode(RESPONSE.OK);
		try {
			ClientDetailsEntity entity = new ClientDetailsEntity();
			ClientAppInfo info = new ClientAppInfo();
			info.setName(appData.getName());
			entity.setAdditionalInformation(info.toJson());
			entity.setClientId(clientDetailsAdapter.generateClientId());
			entity.setAuthorities(clientDetailsAdapter.defaultAuthorities());
			entity.setAuthorizedGrantTypes(clientDetailsAdapter.defaultGrantTypes());
			entity.setDeveloperId(getUserId());
			entity.setClientSecret(clientDetailsAdapter.generateClientSecret());

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
		return reset(clientId, "clientId".equals(reset));
	}

	/**
	 * Reset clientId or client secret
	 * @param clientId
	 * @param resetClientId true to reset clientId, false to reset clientSecret
	 * @return
	 */
	protected Response reset(String clientId, boolean resetClientId) {
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
			if (resetClientId) {
				client.setClientId(clientDetailsAdapter.generateClientId());
			} else {
				client.setClientSecret(clientDetailsAdapter.generateClientSecret());
			}
			clientDetailsRepository.save(client);
			response.setData(clientDetailsAdapter.convertToClientApp(client));
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			response.setResponseCode(RESPONSE.ERROR);
			response.setErrorMessage(e.getMessage());
		}
		return response;
	}

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

	protected String getUserName(User user) {
		Map<String,Object> attrs = new HashMap<String, Object>();
		String authority = getUserAuthority();
		for (Attribute a : user.getAttributeEntities()) {
			if (a.getAuthority().getRedirectUrl().equals(authority)) {
				attrs.put(a.getKey(), a.getValue());
			}
		}
		String username = attrs.containsKey("eu.trentorise.smartcampus.givenname")?attrs.get("eu.trentorise.smartcampus.givenname").toString():"";
		username += " "+(attrs.containsKey("eu.trentorise.smartcampus.surname")?attrs.get("eu.trentorise.smartcampus.surname").toString():"");
		return username;
	}

}
