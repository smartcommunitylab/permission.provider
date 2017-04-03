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
package eu.trentorise.smartcampus.permissionprovider.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import eu.trentorise.smartcampus.permissionprovider.Config;
import eu.trentorise.smartcampus.permissionprovider.model.Attribute;
import eu.trentorise.smartcampus.permissionprovider.model.Authority;
import eu.trentorise.smartcampus.permissionprovider.model.SocialEngineException;
import eu.trentorise.smartcampus.permissionprovider.model.User;
import eu.trentorise.smartcampus.permissionprovider.repository.AttributeRepository;
import eu.trentorise.smartcampus.permissionprovider.repository.AuthorityRepository;
import eu.trentorise.smartcampus.permissionprovider.repository.UserRepository;

/**
 * This class manages operations of the service
 * 
 */
@Component
@Transactional
public class ProviderServiceAdapter {
	
	private Log normalLogger = LogFactory.getLog(ProviderServiceAdapter.class);
	
	@Autowired
	private WeLiveLogger logger;

	@Value("${mode.testing}")
	private boolean testMode;

	@Autowired
	private AttributesAdapter attrAdapter;
	@Autowired
	private AuthorityRepository authorityRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AttributeRepository attributeRepository;
	@Autowired
	private SecurityAdapter secAdapter;
	@Autowired
	private SocialEngine socialEngine;
	
	
	@PostConstruct
	private void init() throws JAXBException, IOException {
		attrAdapter.init();
		secAdapter.init();
	}

	/**
	 * Updates of user attributes using the values obtained from http request
	 * 
	 * @param authorityUrl
	 *            the url of authority used from user to authenticate himself
	 * @param map 
	 * @param req
	 *            the http request
	 * @return the authentication token of the user (renew if it's expired)
	 * @throws AcServiceException
	 */
	public User updateUser(String authorityUrl, Map<String, String> map, HttpServletRequest req) {
		
		Authority auth = authorityRepository.findByRedirectUrl(authorityUrl);
		if (auth == null) {
			throw new IllegalArgumentException("Unknown authority URL: " + authorityUrl);
		}
		// read received attribute values
		Map<String, String> attributes = attrAdapter.getAttributes(auth.getName(), map, req);
		List<Attribute> list = extractIdentityAttributes(auth, attributes, true);
		
		// find user by identity attributes
		List<User> users = userRepository.getUsersByAttributes(list);
		if (users == null)
			users = new ArrayList<User>();
		if (users.size() > 1) {
			list = extractIdentityAttributes(auth, attributes, false);
			users = userRepository.getUsersByAttributes(list);
			if (users == null) users = new ArrayList<User>();
			if (users.size() > 1) {
				throw new IllegalArgumentException("The request attributes identify more than one user");
			}
		}
		// fillin attribute list
		list.clear();
		populateAttributes(auth, attributes, list, users.isEmpty() ? null : users.get(0).getAttributeEntities());

		// check the access rights for the user with respect to the whitelist
		if (!secAdapter.access(auth.getName(), new ArrayList<String>(attributes.keySet()), attributes)) {
			throw new SecurityException("Access denied to user");
		}

		User user = null;
		if (users.isEmpty()) {
			String socialId = "1";
			user = new User(socialId, attributes.get(Config.NAME_ATTR), attributes.get(Config.SURNAME_ATTR), new HashSet<Attribute>(list), System.currentTimeMillis());
			if (authorityUrl.equalsIgnoreCase("welive")) {
				userRepository.save(user);
				// WELIVE authority store.
				Map<String,Object> logMap = new HashMap<String, Object>();
				logMap.put("userid", ""+user.getId());
				logMap.put("authority", authorityUrl);
				logger.log(WeLiveLogger.USER_CREATED, logMap);
			}
		} else {
			user = users.get(0);
			attributeRepository.deleteInBatch(user.getAttributeEntities());
			user.setAttributeEntities(new HashSet<Attribute>(list));
			user.updateNames(attributes.get(Config.NAME_ATTR), attributes.get(Config.SURNAME_ATTR));
			userRepository.save(user);
			
			Map<String,Object> logMap = new HashMap<String, Object>();
			logMap.put("userid", ""+user.getId());
			logMap.put("authority", authorityUrl);
			logger.log(WeLiveLogger.USER_UPDATED, logMap);
		}
		
		return user;
	}

	private void populateAttributes(Authority auth, Map<String, String> attributes, List<Attribute> list, Set<Attribute> old) {
		if (old != null) {
			for (Attribute a : old) {
				if (!a.getAuthority().equals(auth)) {
					Attribute attr = new Attribute();
					attr.setAuthority(a.getAuthority());
					attr.setKey(a.getKey());
					attr.setValue(a.getValue());
					list.add(attr);
				} else {
					// if no value for old attribute, use the old value
					if (!StringUtils.hasText(attributes.get(a.getKey())) || "null".equals(attributes.get(a.getKey()))) {
						attributes.put(a.getKey(), a.getValue());
					}
				}
			}
		}

		
		for (String key : attributes.keySet()) {
			String value = attributes.get(key);
			Attribute attr = new Attribute();
			attr.setAuthority(auth);
			attr.setKey(key);
			attr.setValue(value);
			list.add(attr);
		}
	}


	/**
	 * Extract identity attribute values from all the attributes received for the specified authority.
	 * @param auth
	 * @param attributes
	 * @param all search for all atrribute matches or only for own identity attributes
	 * @return
	 */
	private List<Attribute> extractIdentityAttributes(Authority auth, Map<String, String> attributes, boolean all) {
		return attrAdapter.findAllIdentityAttributes(auth, attributes, all);
	}
}
