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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.Attributes;
import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.AuthorityMapping;

/**
 * Extract user attributes for the user identified by the request parameter
 * 'token'.
 * 
 * The token is checked to belong to the specified Google clientIds provided as
 * constructor argument for the bean (comma-separated list of client IDs)
 * 
 * The user attributes are extracted from the google userinfo API.
 * 
 * @author raman
 *
 */
public class AppleAuthorityHandler implements AuthorityHandler {

	private static final String TOKEN_PARAM = "token";

	private Log logger = LogFactory.getLog(getClass());

	private static ConfigurableJWTProcessor<SecurityContext> jwtProcessor = null;
	
	@Override
	public Map<String, String> extractAttributes(HttpServletRequest request, Map<String, String> map,
			AuthorityMapping mapping) {
		String token = request.getParameter(TOKEN_PARAM);
		if (token == null) {
			token = map.get(TOKEN_PARAM);
		}
		if (token == null) {
			throw new IllegalArgumentException("Empty token");
		}

		try {
			ConfigurableJWTProcessor<SecurityContext> jwtProcessor = getProcessor();
			JWTClaimsSet claimsSet = jwtProcessor.process(token, null);

			Map<String, Object> result = claimsSet.getClaims();
			return extractAttributes(result, mapping);
		} catch (Exception e) {
			throw new SecurityException("Error validating apple token " + token + ": " + e.getMessage());
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
				String key = (attribute.getAlias() != null && !attribute.getAlias().isEmpty()) ? attribute.getAlias()
						: attribute.getValue();
				attrs.put(key, value.toString());
			}
		}
		return attrs;
	}
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected static synchronized ConfigurableJWTProcessor<SecurityContext> getProcessor() throws Exception {
		if (jwtProcessor == null) {
			jwtProcessor = new DefaultJWTProcessor<SecurityContext>();
			
			URL url = new URL("https://appleid.apple.com/auth/keys");
			SSLContext sslContext = SSLContext.getInstance("TLSv1.2"); 
			sslContext.init(null, null, new SecureRandom());
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setSSLSocketFactory(sslContext.getSocketFactory());
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String input = null;
            StringBuilder result = new StringBuilder();
            while ((input = br.readLine()) != null) {
            	result.append(input);
            }
			JWKSet jwkSet = JWKSet.parse(result.toString());
		
			JWKSource<SecurityContext> keySource = new ImmutableJWKSet<SecurityContext>(jwkSet);
			JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
			JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<SecurityContext>(expectedJWSAlg, keySource);
			jwtProcessor.setJWSKeySelector(keySelector);
	
			// Set the required JWT claims for access tokens issued by the Connect2id
			// server, may differ with other servers
			jwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier(
			    new JWTClaimsSet.Builder().issuer("https://appleid.apple.com").build(),
			    new HashSet<String>(Arrays.asList("sub", "iat", "exp", "iss", "aud", "email"))));
		}
		return jwtProcessor;
		
	}


	public static void main(String[] args) throws Exception {
//		String s = "-----BEGIN PRIVATE KEY-----\r\n"
//				+ "-----END PRIVATE KEY-----";
//
//		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
//		Reader rdr = new StringReader(s);
//		PemObject parsed = new org.bouncycastle.openssl.PEMParser(rdr).readPemObject();
//		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(parsed.getContent());
//		KeyFactory factory = KeyFactory.getInstance("ECDSA");
//		PrivateKey privateKey = factory.generatePrivate(spec);
//
//		final JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256).keyID("").build();
//
//		Date when = new Date();
//		
//		final JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().issuer("").issueTime(when)
//				.expirationTime(new Date(when.toInstant().plusSeconds(6*30*24*60*60).toEpochMilli()))
//				.audience("https://appleid.apple.com")
//				.subject("").build();
//
//		SignedJWT signedJWT = new SignedJWT(header, claimsSet);
//		JWSSigner signer = new ECDSASigner((java.security.interfaces.ECPrivateKey) privateKey);
//		signedJWT.sign(signer);
//		String token = signedJWT.serialize();
//
//		System.out.println(token);
		
		String accessToken = "";

		ConfigurableJWTProcessor<SecurityContext> jwtProcessor = getProcessor();
		JWTClaimsSet claimsSet = jwtProcessor.process(accessToken, null);

		// Print out the token claims set
		System.out.println(claimsSet.toJSONObject());


	}
}
