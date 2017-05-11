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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import eu.trentorise.smartcampus.permissionprovider.common.Utils;
import eu.trentorise.smartcampus.permissionprovider.manager.AttributesAdapter;
import eu.trentorise.smartcampus.permissionprovider.manager.ClientDetailsManager;
import eu.trentorise.smartcampus.permissionprovider.manager.ExtraInfoManager;
import eu.trentorise.smartcampus.permissionprovider.manager.ProviderServiceAdapter;
import eu.trentorise.smartcampus.permissionprovider.manager.WeLiveLogger;
import eu.trentorise.smartcampus.permissionprovider.model.AuthProtocolType;
import eu.trentorise.smartcampus.permissionprovider.model.ClientAppBasic;
import eu.trentorise.smartcampus.permissionprovider.model.SingleSignoutData;
import eu.trentorise.smartcampus.permissionprovider.oauth.AutoJdbcTokenStore;
import eu.trentorise.smartcampus.permissionprovider.repository.UserRepository;

/**
 * Controller for developer console entry points
 */
@Controller
public class AuthController extends AbstractController {

	private static Log normalLogger = LogFactory.getLog(AuthController.class);
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ProviderServiceAdapter providerServiceAdapter;
	@Autowired
	private AttributesAdapter attributesAdapter;
	@Autowired
	private AutoJdbcTokenStore autoJdbcTokenStore;
	@Value("${mode.testing}")
	private boolean testMode;
	@Value("${mode.collectInfo:false}")
	private boolean collectInfoMode;
	@Autowired
	private ClientDetailsManager clientDetailsAdapter;
	@Autowired
	private TokenStore tokenStore;
	@Autowired
	private ExtraInfoManager infoManager;
	@Autowired
	private WeLiveLogger logger;
	@Value("${api.token}")
	private String token;
	@Value("${swagger.server}")
	private String swaggerServerUri;
	/** stateMap for holding single signout data. **/
	ConcurrentHashMap<String, SingleSignoutData> stateMap = null;

	/** constants required in access token conversion. **/
	final String AUD = "aud";
	final String CLIENT_ID = "client_id";
	final String EXP = "exp";
	final String JTI = "jti";
	final String GRANT_TYPE = "grant_type";
	final String ATI = "ati";
	final String SCOPE = OAuth2AccessToken.SCOPE;
	final String AUTHORITIES = "authorities";
	final String USERNAME = "user_name";

	/**
	 * welive login
	 * 
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/login/welive-login")
	public ModelAndView weliveLogin(HttpServletRequest req, String username, String password) throws Exception {

		Map<String, Object> model = new HashMap<String, Object>();
		Map<String, String> header = new HashMap<String, String>();
		header.put("Authorization", getAPICredentials());

		// call post swagger.
		try {
			normalLogger.info("Invoking API: " + swaggerServerUri + "/lum/check-login/email/" + username + "/pwd/");
			String response = Utils.callPOST(
					swaggerServerUri + "/lum/check-login/email/" + username + "/pwd/" + URLEncoder.encode(password, "UTF-8"), null, header);
			JSONObject responseJSON = new JSONObject(response);
			if (!responseJSON.getBoolean("error")) {
				return new ModelAndView("redirect:/eauth/welive?username=" + username);
			} else {
				throw new Exception(response);
			}

		} catch (Exception e) {
			// false.
			// redirect it back to authority page
			// take the authorities from session. if not present take all
			Map<String, String> authorities = req.getSession().getAttribute("authorities") == null
					? attributesAdapter.getWebAuthorityUrls()
					: (Map<String, String>) req.getSession().getAttribute("authorities");
			// add an error message
			req.getSession().setAttribute("error", "true");
			model.put("authorities", authorities);
			return new ModelAndView("authorities", model);

		}
	}

	/**
	 * Redirect to the login type selection page
	 * 
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/eauth/admin")
	public ModelAndView admin(HttpServletRequest req) throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		Map<String, String> authorities = attributesAdapter.getWebAuthorityUrls();
		model.put("authorities", authorities);
		String target = prepareRedirect(req, "/admin");
		req.getSession().setAttribute("redirect", target);
		return new ModelAndView("authorities", model);
	}

	/**
	 * Redirect to the login type selection page.
	 * 
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/eauth/dev")
	public ModelAndView developer(HttpServletRequest req) throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		Map<String, String> authorities = attributesAdapter.getWebAuthorityUrls();
		model.put("authorities", authorities);
		String target = prepareRedirect(req, "/dev");
		req.getSession().setAttribute("redirect", target);
		return new ModelAndView("authorities", model);
	}

	/**
	 * Redirect to the login type selection page.
	 * 
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/eauth/sso")
	public ModelAndView sso(HttpServletRequest req) throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		Map<String, String> authorities = attributesAdapter.getWebAuthorityUrls();
		model.put("authorities", authorities);
		String target = prepareRedirect(req, "/sso");
		req.getSession().setAttribute("redirect", target);
		return new ModelAndView("authorities", model);
	}

	/**
	 * Redirect to the login type selection page.
	 * 
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/eauth/cas")
	public ModelAndView cas(HttpServletRequest req) throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		Map<String, String> authorities = attributesAdapter.getWebAuthorityUrls();
		model.put("authorities", authorities);
		String target = prepareRedirect(req, "/cas/loginsuccess");
		req.getSession().setAttribute("redirect", target);
		return new ModelAndView("authorities", model);
	}

	@RequestMapping("/eauth/start")
	public ModelAndView reAuthorise(HttpServletRequest req) {
		Map<String, Object> model = new HashMap<String, Object>();

		Map<String, String> authorities = attributesAdapter.getWebAuthorityUrls();
		String clientId = (String) req.getSession().getAttribute("client_id");
		if (clientId != null) {
			Set<String> idps = clientDetailsAdapter.getIdentityProviders(clientId);

			Set<String> all = new HashSet<String>(authorities.keySet());
			Map<String, String> resultAuthorities = new HashMap<String, String>();
			for (String idp : all) {
				if (authorities.containsKey(idp) && idps.contains(idp))
					resultAuthorities.put(idp, authorities.get(idp));
			}
			if (resultAuthorities.size() == 1) {
				return new ModelAndView("redirect:/eauth/" + resultAuthorities.keySet().iterator().next());
			}
			model.put("authorities", resultAuthorities);
		} else {
			model.put("authorities", authorities);
		}

		return new ModelAndView("authorities", model);

	}

	/**
	 * Entry point for resource access authorization request. Redirects to the
	 * login page. In addition to standard OAuth parameters, it is possible to
	 * specify a comma-separated list of authorities to be used for login as
	 * 'authorities' parameter
	 * 
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/eauth/authorize")
	public ModelAndView authorise(HttpServletRequest req,
			@RequestParam(value = "authorities", required = false) String loginAuthorities) throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		Map<String, String> authorities = attributesAdapter.getWebAuthorityUrls();

		String clientId = req.getParameter("client_id");
		if (clientId == null || clientId.isEmpty()) {
			model.put("message", "Missing client_id");
			return new ModelAndView("oauth_error", model);
		}
		Set<String> idps = clientDetailsAdapter.getIdentityProviders(clientId);

		Set<String> all = null;
		if (loginAuthorities != null && !loginAuthorities.isEmpty()) {
			all = new HashSet<String>(Arrays.asList(loginAuthorities.split(",")));
		} else {
			all = new HashSet<String>(authorities.keySet());
		}
		Map<String, String> resultAuthorities = new HashMap<String, String>();
		for (String idp : all) {
			if (authorities.containsKey(idp) && idps.contains(idp))
				resultAuthorities.put(idp, authorities.get(idp));
		}

		if (resultAuthorities.isEmpty()) {
			model.put("message", "No Identity Providers assigned to the app");
			return new ModelAndView("oauth_error", model);
		}

		String target = prepareRedirect(req, "/oauth/authorize");
		req.getSession().setAttribute("redirect", target);
		req.getSession().setAttribute("client_id", clientId);

		/** 1. create/update stateMap<stateKey, SingleSignoutData>. **/
		if (req.getSession().getAttribute("stateMap") != null) {
			stateMap = (java.util.concurrent.ConcurrentHashMap<String, SingleSignoutData>) req.getSession()
					.getAttribute("stateMap");
		} else {
			stateMap = new java.util.concurrent.ConcurrentHashMap<String, SingleSignoutData>();
			req.getSession().setAttribute("stateMap", stateMap);
		}

		/**
		 * save in session a serialized object, key-> {protocol,
		 * sessionIndex[ticket|token], service[in case of OAUTH read from
		 * configuration]}.
		 **/
		SingleSignoutData ssData = new SingleSignoutData(AuthProtocolType.OAUTH.toString(), clientId,
				clientDetailsAdapter.get(clientId).getSloUrl());
		/** 3. update stateMap<stateKey, SingleSignoutData>. **/
		stateMap.put(UUID.randomUUID().toString(), ssData); // logout url.

		Authentication old = SecurityContextHolder.getContext().getAuthentication();
		if (old != null && old instanceof UsernamePasswordAuthenticationToken) {
			String authorityUrl = (String) old.getDetails();
			if (resultAuthorities.containsKey(authorityUrl)) {
				return new ModelAndView("redirect:/eauth/" + authorityUrl);
			}
		}

		req.getSession().setAttribute("authorities", resultAuthorities);

		if (resultAuthorities.size() == 1) {
			String a = resultAuthorities.keySet().iterator().next();
			if (!"welive".equals(a)) {
				return new ModelAndView("redirect:/eauth/" + a);
			}
		}
		model.put("authorities", resultAuthorities);

		return new ModelAndView("authorities", model);
	}

	/**
	 * Entry point for resource access authorization request. Redirects to the
	 * login page of the specific identity provider
	 * 
	 * @param req
	 * @param authority
	 * identity provider alias
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/eauth/authorize/{authority}")
	public ModelAndView authoriseWithAuthority(@PathVariable String authority, HttpServletRequest req)
			throws Exception {
		String clientId = req.getParameter("client_id");
		if (clientId == null || clientId.isEmpty()) {
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("message", "Missing client_id");
			return new ModelAndView("oauth_error", model);
		}
		Set<String> idps = clientDetailsAdapter.getIdentityProviders(clientId);
		if (!idps.contains(authority)) {
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("message", "incorrect identity provider for the app");
			return new ModelAndView("oauth_error", model);
		}

		String target = prepareRedirect(req, "/oauth/authorize");
		req.getSession().setAttribute("redirect", target);
		req.getSession().setAttribute("client_id", clientId);

		if (!"welive".equals(authority)) {
			return new ModelAndView("redirect:/eauth/" + authority);
		}

		Map<String, Object> model = new HashMap<String, Object>();
		Map<String, String> authorities = attributesAdapter.getWebAuthorityUrls();
		Map<String, String> resultAuthorities = Collections.singletonMap("welive", authorities.get("welive"));
		req.getSession().setAttribute("authorities", resultAuthorities);

		model.put("authorities", resultAuthorities);
		return new ModelAndView("authorities", model);
	}

	/**
	 * Generate redirect string parameter
	 * 
	 * @param req
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	protected String prepareRedirect(HttpServletRequest req, String path) throws UnsupportedEncodingException {
		String target = path + (req.getQueryString() == null ? "" : "?" + req.getQueryString());
		return target;
	}

	/**
	 * Handles the redirection to the specified target after the login has been
	 * performed. Given the user data collected during the login, updates the
	 * user information in DB and populates the security context with the user
	 * credentials.
	 * 
	 * @param authorityUrl
	 *            the authority used by the user to sign in.
	 * @param target
	 *            target functionality address.
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/eauth/{authorityUrl}")
	public ModelAndView forward(@PathVariable String authorityUrl, @RequestParam(required = false) String target,
			HttpServletRequest req, HttpServletResponse res) throws Exception {

		List<GrantedAuthority> list = Collections
				.<GrantedAuthority> singletonList(new SimpleGrantedAuthority("ROLE_USER"));

		String nTarget = (String) req.getSession().getAttribute("redirect");
		if (nTarget == null)
			return new ModelAndView("redirect:/logout");

		String clientId = (String) req.getSession().getAttribute("client_id");
		if (clientId != null) {
			Set<String> idps = clientDetailsAdapter.getIdentityProviders(clientId);
			if (!idps.contains(authorityUrl)) {
				Map<String, Object> model = new HashMap<String, Object>();
				model.put("message", "incorrect identity provider for the app");
				return new ModelAndView("oauth_error", model);
			}
		}

		// HOOK for testing
		if (testMode && target == null) {
			target = "/eauth/" + authorityUrl + "?target=" + URLEncoder.encode(nTarget, "UTF8")
					+ "&OIDC_CLAIM_email=my@mail&OIDC_CLAIM_given_name=name&OIDC_CLAIM_family_name=surname";
		} else {

			if (!testMode && nTarget != null) {
				target = nTarget;
			}

			Authentication old = SecurityContextHolder.getContext().getAuthentication();
			if (old != null && old instanceof UsernamePasswordAuthenticationToken) {
				if (!authorityUrl.equals(old.getDetails())) {
					new SecurityContextLogoutHandler().logout(req, res, old);
					SecurityContextHolder.getContext().setAuthentication(null);

					req.getSession().setAttribute("redirect", target);
					req.getSession().setAttribute("client_id", clientId);

					return new ModelAndView("redirect:/eauth/" + authorityUrl);
					// return new ModelAndView("redirect:/logout");
				}
			}

			List<NameValuePair> pairs = URLEncodedUtils.parse(URI.create(nTarget), "UTF-8");

			eu.trentorise.smartcampus.permissionprovider.model.User userEntity = null;
			if (old != null && old instanceof UsernamePasswordAuthenticationToken) {
				String userId = old.getName();
				userEntity = userRepository.findOne(Long.parseLong(userId));
			} else {
				userEntity = providerServiceAdapter.updateUser(authorityUrl, toMap(pairs), req);
			}

			if (clientId != null) {
				Map<String, Object> logMap = new HashMap<String, Object>();
				ClientAppBasic clientAppBasic = clientDetailsAdapter.get(clientId);
				logMap.put("userid", "" + userEntity.getId());
				logMap.put("clientapp", "" + clientAppBasic.getName());
				logger.log(WeLiveLogger.USER_CLIENT_AUTHORIZATION, logMap);

			}

//			UserDetails user = new User(userEntity.getId().toString(), "", list);
			UserDetails user = new User(userEntity.getId() == null ? UUID.randomUUID().toString() : userEntity.getId().toString(), "", list);

			AbstractAuthenticationToken a = new UsernamePasswordAuthenticationToken(user, null, list);
			a.setDetails(authorityUrl);

			SecurityContextHolder.getContext().setAuthentication(a);

			if (collectInfoMode && !authorityUrl.equals("welive")) {
				if (userEntity.getId() == null) {
					req.getSession().setAttribute("NEW_USER", userEntity);
				//if (!infoManager.infoAlreadyCollected(getUserId())) {
					req.getSession().setAttribute("redirect", target);
					return new ModelAndView("redirect:/terms");
				}
			}
		}
		return new ModelAndView("redirect:" + target);
	}

	/**
	 * @param pairs
	 * @return
	 */
	private Map<String, String> toMap(List<NameValuePair> pairs) {
		if (pairs == null)
			return Collections.emptyMap();
		Map<String, String> map = new HashMap<String, String>();
		for (NameValuePair nvp : pairs) {
			map.put(nvp.getName(), nvp.getValue());
		}
		return map;
	}

	/**
	 * Revoke the access token and the associated refresh token.
	 * 
	 * @param token
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/eauth/revoke/{token}")
	public @ResponseBody String revokeToken(@PathVariable String token) {
		OAuth2AccessToken accessTokenObj = tokenStore.readAccessToken(token);
		if (accessTokenObj != null) {
			if (accessTokenObj.getRefreshToken() != null) {
				tokenStore.removeRefreshToken(accessTokenObj.getRefreshToken());
			}
			tokenStore.removeAccessToken(accessTokenObj);
		}
		return "";
	}

	/**
	 * Revoke the access token using clientId and userId.
	 * @param token
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/eauth/revoke/{clientId}/{userId}")
	public @ResponseBody String revokeToken(@RequestHeader("Authorization") String token, @PathVariable String clientId,
			@PathVariable String userId, HttpServletRequest req, HttpServletResponse res) {

		try {
			if (token == null || !token.matches(getAPICredentials())) {
				res.setStatus(HttpStatus.UNAUTHORIZED.value());
				return null;
			}

			autoJdbcTokenStore.deleteAccessTokenUsingClientIdUserId(clientId, userId);

		} catch (Exception e) {
			normalLogger.error(e.getMessage());
			res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return null;
	}

	/** This end point is created to support token validation w.r.t scope
	 * following the spring example<https://github.com/spring-projects/spring-security-oauth.git>. With this end point and RemoteTokenService placed in aac client,
	 * we obtain the overall support to filter operation implemented in starter template project based on token scope in
	 * a nice way using annotations ..anyRequest().access("#oauth2.hasScope('profile.basicprofile.me')"); **/
	@RequestMapping(value = "/eauth/check_token")
	@ResponseBody
	public Map<String, ?> checkToken(@RequestParam("token") String value) {

		Map<String, ?> response = new HashMap<String, Object>();
		OAuth2AccessToken accessTokenObj = tokenStore.readAccessToken(value);
		if (accessTokenObj != null) {
			OAuth2Authentication authentication = tokenStore.readAuthentication(accessTokenObj.getValue());
			response = convertAccessToken(accessTokenObj, authentication);
		}
		return response;
	}

	private Map<String, ?> convertAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {

		Map<String, Object> response = new HashMap<String, Object>();
		AuthorizationRequest clientToken = authentication.getAuthorizationRequest();

		if (!authentication.isClientOnly()) {
			response.put(USERNAME, authentication.getName());
			if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
				response.put(AUTHORITIES, AuthorityUtils.authorityListToSet(authentication.getAuthorities()));
			}
		} else {
			if (clientToken.getAuthorities() != null && !clientToken.getAuthorities().isEmpty()) {
				response.put(AUTHORITIES, AuthorityUtils.authorityListToSet(clientToken.getAuthorities()));
			}
		}

		if (token.getScope() != null) {
			response.put(SCOPE, token.getScope());
		}
		if (token.getAdditionalInformation().containsKey(JTI)) {
			response.put(JTI, token.getAdditionalInformation().get(JTI));
		}

		if (token.getExpiration() != null) {
			response.put(EXP, token.getExpiration().getTime() / 1000);
		}

		response.putAll(token.getAdditionalInformation());

		response.put(CLIENT_ID, clientToken.getClientId());
		if (clientToken.getResourceIds() != null && !clientToken.getResourceIds().isEmpty()) {
			response.put(AUD, clientToken.getResourceIds());
		}
		return response;
	}

	/**
	 * @return
	 */
	private String getAPICredentials() {
		return "Basic " + token;
	}

}