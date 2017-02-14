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

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

import javax.xml.namespace.QName;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.saml2.core.SessionIndex;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml2.core.impl.SessionIndexBuilder;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

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

	private static XMLObjectBuilderFactory builderFactory;

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

	/**
	 * Build the logout request
	 * 
	 * @param subject
	 *            name of the user
	 * @param reason
	 *            reason for generating logout request.
	 * @return LogoutRequest object
	 */
	public static LogoutRequest buildLogoutRequest(String subject, String sessionIndexId, String reason,
			String issuerId) {
		LogoutRequest logoutReq = new org.opensaml.saml2.core.impl.LogoutRequestBuilder().buildObject();
		logoutReq.setID(UUID.randomUUID().toString());

		DateTime issueInstant = new DateTime();
		logoutReq.setIssueInstant(issueInstant);
		logoutReq.setNotOnOrAfter(new DateTime(issueInstant.getMillis() + 5 * 60 * 1000));

		IssuerBuilder issuerBuilder = new IssuerBuilder();
		Issuer issuer = issuerBuilder.buildObject();
		issuer.setValue(issuerId);
		logoutReq.setIssuer(issuer);

		NameID nameId = new NameIDBuilder().buildObject();
		// nameId.setFormat(SSOConstants.SAML2_NAME_ID_POLICY);
		nameId.setValue(subject);
		logoutReq.setNameID(nameId);

		SessionIndex sessionIndex = new SessionIndexBuilder().buildObject();
		sessionIndex.setSessionIndex(sessionIndexId);
		logoutReq.getSessionIndexes().add(sessionIndex);

		logoutReq.setReason(reason);

		return logoutReq;
	}

	public static String encodeRequestMessage(RequestAbstractType requestMessage) {

		String encodedRequestMessage = "";
		try {

			Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(requestMessage);

			Element authDOM = (Element) marshaller.marshall(requestMessage);

			// Deflater deflater = new Deflater(Deflater.DEFLATED, true);
			/** BLOCKER: resolved problem with incorrect-header.**/
			Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);

			StringWriter rspWrt = new StringWriter();
			XMLHelper.writeNode(authDOM, rspWrt);
			deflaterOutputStream.write(rspWrt.toString().getBytes("UTF-8"));
			deflaterOutputStream.close();

			/* Encoding the compressed message */
			encodedRequestMessage = Base64.encodeBase64String(byteArrayOutputStream.toByteArray());

			return URLEncoder.encode(encodedRequestMessage, "UTF-8").trim();

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		return encodedRequestMessage;

	}

	public static LogoutRequest genererateLogoutRequest(final String name, final String sessionIndex) throws Exception {

		NameID nameId = Utils.buildSAMLObjectWithDefaultName(NameID.class);
		nameId.setFormat("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress");
		nameId.setValue(name);

		LogoutRequest logoutRequest = buildSAMLObjectWithDefaultName(LogoutRequest.class);

		logoutRequest.setID(UUID.randomUUID().toString());

		logoutRequest.setDestination("xxxxxxx/sso/saml");
		logoutRequest.setIssueInstant(new DateTime());

		Issuer issuer = buildSAMLObjectWithDefaultName(Issuer.class);
		issuer.setValue("xxxxx");
		logoutRequest.setIssuer(issuer);

		SessionIndex sessionIndexElement = buildSAMLObjectWithDefaultName(SessionIndex.class);

		sessionIndexElement.setSessionIndex(sessionIndex);
		logoutRequest.getSessionIndexes().add(sessionIndexElement);

		logoutRequest.setNameID(nameId);
		return logoutRequest;
	}

	public static <T> T buildSAMLObjectWithDefaultName(final Class<T> clazz) throws IllegalArgumentException,
			IllegalAccessException, NoSuchFieldException, SecurityException, ConfigurationException {
		XMLObjectBuilderFactory builderFactory = getBuilderFactory();

		QName defaultElementName = (QName) clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
		T object = (T) builderFactory.getBuilder(defaultElementName).buildObject(defaultElementName);

		return object;
	}

	private static XMLObjectBuilderFactory getBuilderFactory() throws ConfigurationException {
		if (builderFactory == null) {
			// OpenSAML 2.3
			DefaultBootstrap.bootstrap();
			builderFactory = Configuration.getBuilderFactory();
		}

		return builderFactory;
	}

	public static void main(String[] args) {
		try {
			// Encode a String into bytes
			String inputString = "phela nasha.";
			byte[] input = inputString.getBytes("UTF-8");

			// Compress the bytes
			byte[] output1 = new byte[input.length];
			Deflater compresser = new Deflater();
			compresser.setInput(input);
			compresser.finish();
			int compressedDataLength = compresser.deflate(output1);
			compresser.end();

			byte[] str = Base64.encodeBase64(output1);
			System.out.println("Deflated String:" + str);

			byte[] output2 = Base64.decodeBase64(str);

			// Decompress the bytes
			Inflater decompresser = new Inflater();
			decompresser.setInput(output2);
			// byte[] result = str.getBytes();
			int resultLength = decompresser.inflate(str);
			decompresser.end();

			// Decode the bytes into a String
			String outputString = new String(str, 0, resultLength, "UTF-8");
			System.out.println("Deflated String:" + outputString);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
