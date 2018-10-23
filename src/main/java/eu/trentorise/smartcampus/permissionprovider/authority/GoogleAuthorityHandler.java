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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.client.RestTemplate;

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
	private Log logger = LogFactory.getLog(getClass());
	
	public GoogleAuthorityHandler(String googleClientIdsString) {
		super();
		if (googleClientIdsString == null) googleClientIds = Collections.emptySet();
		String[] array = googleClientIdsString.split(",");
		this.googleClientIds = new HashSet<String>(Arrays.asList(array));
	}


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
			Map<String, Object> result = null;
			try {
				result = validateV3(token);
			} catch (Exception e) {
				e.printStackTrace();
				// invalid token or invalid token version
			}
			if (result == null) {
				result = validateV1(token);
			}
			
			return extractAttributes(result, mapping);
		} catch (RemoteException e) {
			throw new SecurityException("Error validating google token " +token + ": " + e.getMessage());
		}
	}


	/**
	 * @param token
	 * @return
	 * @throws RemoteException 
	 * @throws SecurityException 
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> validateV3(String token) throws SecurityException, RemoteException {
		String s = new RestTemplate().getForObject("https://www.googleapis.com/oauth2/v3/tokeninfo?id_token="+token, String.class);
//		String s = RemoteConnector.getJSON("https://www.googleapis.com", "/oauth2/v3/tokeninfo?id_token="+token, null);
		logger.error("s = " + s);
		Map<String,Object> result = JsonUtils.toObject(s, Map.class);
		if (result == null || !validAuidence(result) || !result.containsKey("sub")) {
			throw new SecurityException("Incorrect google token "+ token+": "+s);
		}
		result.put("id", result.get("sub"));
		return result;
	}


	/**
	 * Check whether the token client audience matches what is expected
	 * @param result
	 * @return
	 */
	public boolean validAuidence(Map<String, Object> result) {
		return true;//googleClientIds.contains(result.get("audience"));
	}


	/**
	 * Validate Google token against API v1
	 * @param token
	 * @return
	 * @throws RemoteException
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> validateV1(String token) throws SecurityException, RemoteException {
		// first, we have to validate that the token is a correct platform token
		String s = new RestTemplate().getForObject("https://www.googleapis.com/oauth2/v1/tokeninfo?access_token="+token, String.class);
		logger.error("s1 = " + s);
		Map<String,Object> result = JsonUtils.toObject(s, Map.class);
		if (result == null || !validAuidence(result)) {
			throw new SecurityException("Incorrect google token "+ token+": "+s);
		}
		// second, we have to get the user information
		s = RemoteConnector.getJSON("https://www.googleapis.com", "/oauth2/v1/userinfo", token);
		result = JsonUtils.toObject(s, Map.class);
		if (result == null || !result.containsKey("id")) {
			throw new SecurityException("Incorrect google token "+ token+": "+s);
		}
		return result;
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
	
	public static void main(String[] args) throws SecurityException, RemoteException {
		new GoogleAuthorityHandler("")
		.validateV3("eyJhbGciOiJSUzI1NiIsImtpZCI6IjcyOGY0MDE2NjUyMDc5YjllZDk5ODYxYmIwOWJhZmM1YTQ1YmFhODYiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI0NTM2MDE4MTY0NDYtdGUwdTd0NGRsZjYxMTVjZTl0MGk5b2l1ZW9ocWU0NGMuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI0NTM2MDE4MTY0NDYtYnM5dnRpa3Z1Ym5tNGszYWE3bjV2aDl2ZGwxN3Y4OTMuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTUxNjIyMjAxMjQ1NzAzODEwODQiLCJoZCI6ImZiay5ldSIsImVtYWlsIjoibWF0dGVvLmNoaW5pQGZiay5ldSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJuYW1lIjoiTWF0dGVvIENoaW5pIiwicGljdHVyZSI6Imh0dHBzOi8vbGg1Lmdvb2dsZXVzZXJjb250ZW50LmNvbS8tdGtxWWRfalA4SFEvQUFBQUFBQUFBQUkvQUFBQUFBQUFBSFkvc3kwWERNN0l5STgvczk2LWMvcGhvdG8uanBnIiwiZ2l2ZW5fbmFtZSI6Ik1hdHRlbyIsImZhbWlseV9uYW1lIjoiQ2hpbmkiLCJsb2NhbGUiOiJlbiIsImlhdCI6MTU0MDI5MjYwMywiZXhwIjoxNTQwMjk2MjAzfQ.eRvP2ByupBU2bkGNmqIeBZxVujlgpGXVN-gaUCEAV3aYjq6em0mvbrGC8jeuKbB9qqospMaTzkmYH3mXjMA4BcFn3-XEHxgEa-vaoYT7eX9XW73DxHABajW5VN3klfyVU_KyHpVs3Okq2IDEZ28j-AutuDjwmksjj-mHdXEv9Y0dXjCbzN7c8IpqvEKSZ5QQI8qSkzzXQVrSp5KhLEw5fdI4DFHp9yUlAkklKq3Yy3o4NUqr6WpBMsEAVStG044MFHkASoIoCQEHSWz_PkdZnECOgI35eRu1tYgBxS40XXINhE2tnkF1hTniXBqiYonTqJFe2CJNhuaEMpNVWlH6Lw");
	}
}
