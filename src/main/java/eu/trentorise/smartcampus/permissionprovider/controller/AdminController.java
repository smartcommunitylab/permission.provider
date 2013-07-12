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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import eu.trentorise.smartcampus.permissionprovider.manager.AdminManager;
import eu.trentorise.smartcampus.permissionprovider.manager.AttributesAdapter;
import eu.trentorise.smartcampus.permissionprovider.model.ApprovalData;
import eu.trentorise.smartcampus.permissionprovider.model.Attribute;
import eu.trentorise.smartcampus.permissionprovider.model.ClientAppInfo;
import eu.trentorise.smartcampus.permissionprovider.model.ClientDetailsEntity;
import eu.trentorise.smartcampus.permissionprovider.model.Resource;
import eu.trentorise.smartcampus.permissionprovider.model.Response;
import eu.trentorise.smartcampus.permissionprovider.model.Response.RESPONSE;
import eu.trentorise.smartcampus.permissionprovider.model.User;
import eu.trentorise.smartcampus.permissionprovider.repository.ClientDetailsRepository;
import eu.trentorise.smartcampus.permissionprovider.repository.ResourceRepository;
import eu.trentorise.smartcampus.permissionprovider.repository.UserRepository;

/**
 * Access to the administration resources.
 * @author raman
 *
 */
@Controller
@Transactional
public class AdminController extends AbstractController{

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ClientDetailsRepository clientDetailsRepository;
	@Autowired
	private ResourceRepository resourceRepository;
	@Autowired
	private AttributesAdapter attributesAdapter;
	@Autowired
	private AdminManager adminManager;
	
	private Log logger = LogFactory.getLog(getClass());
	/**
	 * Retrieve the with the user data: currently on the username is added.
	 * @return
	 */
	@RequestMapping("/admin")
	public ModelAndView admin() {
		User user = userRepository.findOne(getUserId());
		String authority = getUserAuthority();
		Map<String,Object> model = new HashMap<String, Object>();

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
//				return new ModelAndView("adminerror",model);
				return new ModelAndView("redirect:/admin/logout");
			}
		} catch (Exception e) {
			model.put("error", e.getMessage());
			logger.error("Problem checking admin account: "+e.getMessage());
//			return new ModelAndView("adminerror");
			return new ModelAndView("redirect:/admin/logout");
		}
		
		String username = getUserName(user);
		model.put("username",username);
		return new ModelAndView("admin", model);
	}
	
	@RequestMapping("/admin/approvals")
	public @ResponseBody Response getApprovals() {
		Response result = new Response();
		result.setResponseCode(RESPONSE.OK);
		
		try {
			List<ClientDetailsEntity> clients = clientDetailsRepository.findAll();
			List<ApprovalData> list = new ArrayList<ApprovalData>();
			for (ClientDetailsEntity e : clients) {
				ClientAppInfo info = ClientAppInfo.convert(e.getAdditionalInformation());
				if (!info.getResourceApprovals().isEmpty()) {
					ApprovalData data = new ApprovalData();
					data.setClientId(e.getClientId());
					data.setName(info.getName());
					data.setOwner(userRepository.findOne(e.getDeveloperId()).toString());
					data.setResources(new ArrayList<Resource>());
					for (String rId : info.getResourceApprovals().keySet()) {
						Resource resource = resourceRepository.findOne(Long.parseLong(rId));
						data.getResources().add(resource);
					}
					list.add(data);
				}
			}
			result.setData(list);
		} catch (Exception e) {
			result.setResponseCode(RESPONSE.ERROR);
			result.setErrorMessage(e.getMessage());
		}
		return result;
	}

	@RequestMapping("/admin/approvals/{clientId}")
	public @ResponseBody Response approve(@PathVariable String clientId) {
		try {
			ClientDetailsEntity e = clientDetailsRepository.findByClientId(clientId);
			ClientAppInfo info = ClientAppInfo.convert(e.getAdditionalInformation());
			if (!info.getResourceApprovals().isEmpty()) {
				Set<String> idSet = new HashSet<String>();
				if (e.getResourceIds() != null) idSet.addAll(e.getResourceIds());
				Set<String> uriSet = new HashSet<String>();
				if (e.getScope() != null) uriSet.addAll(e.getScope());
				for (String rId : info.getResourceApprovals().keySet()) {
					Resource resource = resourceRepository.findOne(Long.parseLong(rId));
					idSet.add(rId);
					uriSet.add(resource.getResourceUri());
				}
				e.setResourceIds(StringUtils.collectionToCommaDelimitedString(idSet));
				e.setScope(StringUtils.collectionToCommaDelimitedString(uriSet));
				info.setResourceApprovals(Collections.<String,Boolean>emptyMap());
				e.setAdditionalInformation(info.toJson());
				clientDetailsRepository.save(e);
			}
			return getApprovals();
		} catch (Exception e) {
			Response result = new Response();
			result.setResponseCode(RESPONSE.ERROR);
			result.setErrorMessage(e.getMessage());
			return result;
		}
	}

}
