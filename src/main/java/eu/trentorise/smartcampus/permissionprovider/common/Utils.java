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

package eu.trentorise.smartcampus.permissionprovider.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.util.StringUtils;

import eu.trentorise.smartcampus.network.JsonUtils;
import eu.trentorise.smartcampus.network.RemoteException;
import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.ResourceDeclaration;
import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.ResourceMapping;
import eu.trentorise.smartcampus.permissionprovider.model.ServiceDescriptor;

/**
 * Common methods and functions
 * 
 * @author raman
 *
 */
public class Utils {

	/**
	 * Generate set of strings out of specified delimited string. Remove also
	 * leading/trailing spaces around the elements.
	 * 
	 * @param input
	 * @param delimiter
	 * @return
	 */
	public static Set<String> delimitedStringToSet(String input, String delimiter) {
		HashSet<String> res = new HashSet<String>();
		String[] arr = null;
		if (delimiter != null) {
			arr = input.split(delimiter);
			for (String s : arr) {
				res.add(s.trim());
			}
		}
		return res;
	}

	/**
	 * Correct values of the specified comma-separated string: remove redundant
	 * spaces
	 * 
	 * @param in
	 * @return
	 */
	public static String normalizeValues(String in) {
		return StringUtils.trimAllWhitespace(in);
	}

	/**
	 * Convert
	 * {@link eu.trentorise.smartcampus.permissionprovider.jaxbmodel.Service}
	 * object to {@link ServiceDescriptor} persisted entity
	 * 
	 * @param s
	 * @return converted {@link ServiceDescriptor} entity
	 */
	public static ServiceDescriptor toServiceEntity(eu.trentorise.smartcampus.permissionprovider.jaxbmodel.Service s) {
		ServiceDescriptor res = new ServiceDescriptor();
		res.setDescription(s.getDescription());
		res.setServiceName(s.getName());
		res.setServiceId(s.getId());
		res.setResourceDefinitions(JsonUtils.toJSON(s.getResource()));
		res.setResourceMappings(JsonUtils.toJSON(s.getResourceMapping()));
		return res;
	}

	/**
	 * Convert {@link ServiceDescriptor} entity to
	 * {@link eu.trentorise.smartcampus.permissionprovider.jaxbmodel.Service}
	 * object
	 * 
	 * @param s
	 * @return converted
	 *         {@link eu.trentorise.smartcampus.permissionprovider.jaxbmodel.Service}
	 *         object
	 */
	public static eu.trentorise.smartcampus.permissionprovider.jaxbmodel.Service toServiceObject(ServiceDescriptor s) {
		eu.trentorise.smartcampus.permissionprovider.jaxbmodel.Service res = new eu.trentorise.smartcampus.permissionprovider.jaxbmodel.Service();
		res.setDescription(s.getDescription());
		res.setId(s.getServiceId());
		res.setName(s.getServiceName());
		res.getResource().clear();
		res.getResource().addAll(JsonUtils.toObjectList(s.getResourceDefinitions(), ResourceDeclaration.class));
		res.getResourceMapping().clear();
		res.getResourceMapping().addAll(JsonUtils.toObjectList(s.getResourceMappings(), ResourceMapping.class));
		return res;
	}

	private static HttpClient getDefaultHttpClient(HttpParams inParams) {
		if (inParams != null) {
			return new DefaultHttpClient(inParams);
		} else {
			return new DefaultHttpClient();
		}
	}

	// HTTP POST request
	public static String callPOST(String url, Map<String, Object> body, Map<String, String> headers)
			throws RemoteException {
		final HttpResponse resp;
		final HttpPost post = new HttpPost(url);

		post.setHeader("Accept", "application/json");
		post.setHeader("Content-Type", "application/json");
		if (headers != null) {
			for (String key : headers.keySet()) {
				post.setHeader(key, headers.get(key));
			}
		}

		try {
			String bodyStr = new ObjectMapper().writeValueAsString(body);
			StringEntity input = new StringEntity(bodyStr, "UTF-8");

			input.setContentType("application/json; charset=UTF-8");
			post.setEntity(input);

			resp = getDefaultHttpClient(null).execute(post);
			String response = EntityUtils.toString(resp.getEntity(), "UTF-8");
			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				Map<String, Object> respMap = new ObjectMapper().readValue(response, HashMap.class);
				if (respMap != null && (StringUtils.hasText((String) respMap.get("exception"))
						|| Boolean.TRUE.equals(respMap.get("error")))) {
					throw new RemoteException((String) respMap.get("exception"));
				}
				return response;
			}
			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_FORBIDDEN
					|| resp.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
				throw new SecurityException();
			}

			String msg = "";
			try {
				msg = response.substring(response.indexOf("<h1>") + 4,
						response.indexOf("</h1>", response.indexOf("<h1>")));
			} catch (Exception e) {
				msg = resp.getStatusLine().toString();
			}
			throw new RemoteException(msg);
		} catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
	}

}
