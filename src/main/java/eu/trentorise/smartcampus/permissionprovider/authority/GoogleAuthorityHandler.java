/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
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
 ******************************************************************************/

package eu.trentorise.smartcampus.permissionprovider.authority;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import eu.trentorise.smartcampus.network.JsonUtils;
import eu.trentorise.smartcampus.network.RemoteConnector;
import eu.trentorise.smartcampus.network.RemoteException;
import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.Attributes;
import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.AuthorityMapping;

/**
 * Extract user attributes for the user identified by the request parameter 'token'. 
 * 
 * The token is checked to belong to the specified Google clientIds
 * provided as constructor argument for the bean (comma-separated list of client IDs)
 * 
 * The user attributes are extracted from the google userinfo API.
 * 
 * @author raman
 *
 */
public class GoogleAuthorityHandler implements AuthorityHandler {

	private static final String TOKEN_PARAM = "token";
	
	private Set<String> googleClientIds = null;
	
	public GoogleAuthorityHandler(String googleClientIdsString) {
		super();
		if (googleClientIdsString == null) googleClientIds = Collections.emptySet();
		String[] array = googleClientIdsString.split(",");
		this.googleClientIds = new HashSet<String>(Arrays.asList(array));
	}


	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String> extractAttributes(HttpServletRequest request, Map<String,String> map, AuthorityMapping mapping) {
		String token = request.getParameter(TOKEN_PARAM);
		if (token == null) {
			token = map.get(TOKEN_PARAM);
		}
		if (token == null) {
			throw new IllegalArgumentException("Empty token");
		}
		
		try {
			// first, we have to validate that the token is a correct platform token
			String s = RemoteConnector.getJSON("https://www.googleapis.com", "/oauth2/v1/tokeninfo?access_token="+token, null);
			Map<String,Object> result = JsonUtils.toObject(s, Map.class);
			if (result == null || !googleClientIds.contains(result.get("audience"))) {
				throw new SecurityException("Incorrect google token "+ token+": "+s);
			}
			// second, we have to get the user information
			s = RemoteConnector.getJSON("https://www.googleapis.com", "/oauth2/v1/userinfo", token);
			result = JsonUtils.toObject(s, Map.class);
			if (result == null || !result.containsKey("id")) {
				throw new SecurityException("Incorrect google token "+ token+": "+s);
			}
			
			return extractAttributes(result, mapping);
		} catch (RemoteException e) {
			throw new SecurityException("Error validating google token " +token + ": " + e.getMessage());
		}
	}

	/**
	 * @param result
	 * @return
	 */
	private Map<String, String> extractAttributes(Map<String, Object> result, AuthorityMapping mapping) {
		Map<String, String> attrs = new HashMap<String, String>(); 
		for (String key : mapping.getIdentifyingAttributes()) {
			Object value = result.get(key);
			if (value != null) {
				attrs.put(key, value.toString());
			}
		}
		for (Attributes attribute : mapping.getAttributes()) {
			// used alias if present to set attribute in map
			Object value = result.get(attribute.getValue());
			if (value != null) {
				String key = (attribute.getAlias() != null && !attribute.getAlias()
						.isEmpty()) ? attribute.getAlias() : attribute.getValue();
				attrs.put(key, value.toString());
			}
		}
		return attrs;	
	}
}
