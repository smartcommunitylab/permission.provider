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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.trentorise.smartcampus.permissionprovider.adapters;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.Attributes;
import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.Authorities;
import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.AuthorityMapping;
import eu.trentorise.smartcampus.permissionprovider.model.Attribute;
import eu.trentorise.smartcampus.permissionprovider.model.Authority;
import eu.trentorise.smartcampus.permissionprovider.repository.AuthorityRepository;

/**
 * This class manages all operations on attributes.
 * 
 */
@Component
public class AttributesAdapter {

	@Autowired
	private AuthorityRepository authorityRepository;
	private Map<String, AuthorityMapping> authorities;
	private Map<String, Set<String>> identityAttributes;

	/**
	 * Load attributes from the XML descriptor.
	 * @throws JAXBException
	 */
	protected void init() throws JAXBException {
		JAXBContext jaxb = JAXBContext.newInstance(AuthorityMapping.class,
				Authorities.class);
		Unmarshaller unm = jaxb.createUnmarshaller();
		JAXBElement<Authorities> element = (JAXBElement<Authorities>) unm
				.unmarshal(
						new StreamSource(getClass().getResourceAsStream(
								"authorities.xml")), Authorities.class);
		Authorities auths = element.getValue();
		authorities = new HashMap<String, AuthorityMapping>();
		identityAttributes = new HashMap<String, Set<String>>();
		for (AuthorityMapping mapping : auths.getAuthorityMapping()) {
			Authority auth = authorityRepository.findOne(mapping.getName());
			if (auth == null) {
				auth = new Authority();
				auth.setName(mapping.getName());
				auth.setRedirectUrl(mapping.getUrl());
				authorityRepository.saveAndFlush(auth);
			}
			authorities.put(mapping.getName(), mapping);
			Set<String> identities = new HashSet<String>();
			if (mapping.getIdentifyingAttributes() != null) {
				identities.addAll(mapping.getIdentifyingAttributes());
			}
			identityAttributes.put(auth.getName(), identities);
		}
	}

	/**
	 * Retrieve from http request the attribute values of a specified authority
	 * 
	 * @param authority
	 *            the authority specified
	 * @param request
	 *            the http request to process
	 * @return a map of user attributes
	 */
	public Map<String, String> getAttributes(String authority,
			HttpServletRequest request) {
		AuthorityMapping mapping = authorities.get(authority);
		if (mapping == null) {
			throw new IllegalArgumentException("Unsupported authority: "
					+ authority);
		}
		Map<String, String> attrs = new HashMap<String, String>();
		for (String key : mapping.getIdentifyingAttributes()) {
			Object value = readAttribute(request, key, mapping.isUseParams());
			if (value != null) {
				attrs.put(key, value.toString());
			}
		}
		for (Attributes attribute : mapping.getAttributes()) {
			// used alias if present to set attribute in map
			String key = (attribute.getAlias() != null && !attribute.getAlias()
					.isEmpty()) ? attribute.getAlias() : attribute.getValue();
			Object value = readAttribute(request,attribute.getValue(), mapping.isUseParams());
			if (value != null) {
				attrs.put(key, value.toString());
			}
		}
		return attrs;
	}

	/**
	 * Read either request attribute or a request parameter from HTTP request
	 * @param request
	 * @param key
	 * @param useParams whether to extract parameter instead of attribute 
	 * @return
	 */
	private Object readAttribute(HttpServletRequest request, String key, boolean useParams) {
		if (useParams) return request.getParameter(key);
		return request.getAttribute(key);
	}

	/**
	 * Returns the list of the of the identity attributes of an authority as declared in configuration.
	 * 
	 * @param authority
	 *            the authority
	 * @return the list of identifying attributes for the given authority
	 */
	public List<String> getIdentifyingAttributes(String authority) {
		AuthorityMapping mapping = authorities.get(authority);
		if (mapping == null) {
			throw new IllegalArgumentException("Unsupported authority: "
					+ authority);
		}
		return mapping.getIdentifyingAttributes();
	}

	/**
	 * Returns the authorities available
	 * 
	 * @return the map of authorities, the key is authority name and the value
	 *         is its url
	 */

	public Map<String, String> getAuthorityUrls() {
		Map<String, String> map = new HashMap<String, String>();
		for (AuthorityMapping mapping : authorities.values()) {
			map.put(mapping.getName(), mapping.getUrl());
		}
		return map;
	}

	/**
	 * @param a
	 * @return true if the specified attribute is the identity attribute for the authority
	 */
	public boolean isIdentityAttr(Attribute a) {
		return identityAttributes.containsKey(a.getAuthority().getName()) && 
				identityAttributes.get(a.getAuthority().getName()).contains(a.getKey());
	}
}
