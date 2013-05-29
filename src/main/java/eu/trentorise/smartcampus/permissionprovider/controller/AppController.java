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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import eu.trentorise.smartcampus.permissionprovider.adapters.ClientDetailsAdapter;
import eu.trentorise.smartcampus.permissionprovider.model.ClientAppBasic;
import eu.trentorise.smartcampus.permissionprovider.model.ClientAppInfo;
import eu.trentorise.smartcampus.permissionprovider.model.ClientDetailsEntity;
import eu.trentorise.smartcampus.permissionprovider.model.Response;
import eu.trentorise.smartcampus.permissionprovider.model.Response.RESPONSE;
import eu.trentorise.smartcampus.permissionprovider.repository.ClientDetailsRepository;

/**
 * @author raman
 *
 */
@Controller
@Transactional
public class AppController {

	private Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private ClientDetailsRepository clientDetailsRepository;
	@Autowired
	private ClientDetailsAdapter clientDetailsAdapter;
	
	@RequestMapping("/dev")
	public ModelAndView developer() {
		return new ModelAndView("index");
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
			entity.setScope(clientDetailsAdapter.defaultScope());
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

	@RequestMapping(method=RequestMethod.DELETE,value="/dev/apps/{clientId}")
	public @ResponseBody Response delete(@PathVariable String clientId) {
		Response response = new Response();
		response.setResponseCode(RESPONSE.OK);
		try {
			ClientDetailsEntity client = clientDetailsRepository.findByClientId(clientId);
			if (client == null) return null;
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

	private UserDetails getUser(){
		return (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}
	
	private Long getUserId() {
		return Long.parseLong(getUser().getUsername());
	}
}
