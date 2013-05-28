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
import eu.trentorise.smartcampus.permissionprovider.repository.ClientDetailsRepository;

/**
 * @author raman
 *
 */
@Controller
@Transactional
public class AppController {

	@Autowired
	private ClientDetailsRepository clientDetailsRepository;
	@Autowired
	private ClientDetailsAdapter clientDetailsAdapter;
	
	@RequestMapping("/dev")
	public ModelAndView developer() {
		return new ModelAndView("index");
	}
	
	@RequestMapping("/dev/apps")
	public @ResponseBody List<ClientAppBasic> getAppList() {
		List<ClientAppBasic> list = clientDetailsAdapter.convertClientApps(clientDetailsRepository.findByDeveloperId(getUserId()));
		return list;
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/dev/apps")
	public @ResponseBody ClientAppBasic saveEmpty(@RequestBody ClientAppBasic appData) throws Exception {
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

		return clientDetailsAdapter.convertClientApp(entity);
	}

	@RequestMapping(method=RequestMethod.DELETE,value="/dev/apps/{clientId}")
	public @ResponseBody ClientAppBasic delete(@PathVariable String clientId) {
		ClientDetailsEntity client = clientDetailsRepository.findByClientId(clientId);
		if (client == null) return null;
		clientDetailsRepository.delete(client);
		return clientDetailsAdapter.convertClientApp(client);
	}

	private UserDetails getUser(){
		return (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}
	
	private Long getUserId() {
		return Long.parseLong(getUser().getUsername());
	}
}
