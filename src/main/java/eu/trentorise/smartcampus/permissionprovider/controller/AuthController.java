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
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import eu.trentorise.smartcampus.permissionprovider.manager.AttributesAdapter;
import eu.trentorise.smartcampus.permissionprovider.manager.ProviderServiceAdapter;


/**
 * Controller for developer console entry points
 */
@Controller
public class AuthController {

	@Autowired
	private ProviderServiceAdapter providerServiceAdapter;
	@Autowired 
	private AttributesAdapter attributesAdapter;
	@Value("${mode.testing}")
	private boolean testMode;

	@Autowired
	private TokenStore tokenStore;
	
	/**
	 * Redirect to the login type selection page
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/eauth/admin")
	public ModelAndView admin(HttpServletRequest req) throws Exception {
		Map<String,Object> model = new HashMap<String, Object>();
		Map<String, String> authorities = attributesAdapter.getAuthorityUrls();
		model.put("authorities", authorities);
		String target = prepareRedirect(req,"/admin");
		req.getSession().setAttribute("redirect", target);
		return new ModelAndView("authorities", model);
	}

	/**
	 * Redirect to the login type selection page
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/eauth/dev")
	public ModelAndView developer(HttpServletRequest req) throws Exception {
		Map<String,Object> model = new HashMap<String, Object>();
		Map<String, String> authorities = attributesAdapter.getAuthorityUrls();
		model.put("authorities", authorities);
		String target = prepareRedirect(req,"/dev");
		req.getSession().setAttribute("redirect", target);
		return new ModelAndView("authorities", model);
	}
	
	/**
	 * Entry point for resource access authorization request. Redirects to the login page
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/eauth/authorize")
	public ModelAndView authorise(HttpServletRequest req) throws Exception {
		Map<String,Object> model = new HashMap<String, Object>();
		Map<String, String> authorities = attributesAdapter.getAuthorityUrls();
		model.put("authorities", authorities);
		String target = prepareRedirect(req,"/oauth/authorize");
		req.getSession().setAttribute("redirect", target);
		
		return new ModelAndView("authorities", model);
	}

	/**
	 * Entry point for resource access authorization request. Redirects to the login page of 
	 * the specific identity provider
	 * @param req
	 * @param authority identity provider alias
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/eauth/authorize/{authority}")
	public ModelAndView authoriseWithAuthority(@PathVariable String authority, HttpServletRequest req) throws Exception {
		String target = prepareRedirect(req,"/oauth/authorize");
		req.getSession().setAttribute("redirect", target);
		
		return new ModelAndView("redirect:/eauth/"+authority);
	}

	/**
	 * Generate redirect string parameter
	 * @param req
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	protected String prepareRedirect(HttpServletRequest req, String path)
			throws UnsupportedEncodingException {
		String target = path+(req.getQueryString()==null?"":"?"+req.getQueryString());
		return target;
	}
	
	/**
	 * Handles the redirection to the specified target after the login has been performed.
	 * Given the user data collected during the login, updates the user information in DB
	 * and populates the security context with the user credentials.
	 * 
	 * @param authorityUrl the authority used by the user to sign in.
	 * @param target target functionality address.
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/eauth/{authorityUrl}")
	public ModelAndView forward(@PathVariable String authorityUrl, @RequestParam(required=false) String target, HttpServletRequest req) throws Exception {
		List<GrantedAuthority> list = Collections.<GrantedAuthority>singletonList(new SimpleGrantedAuthority("ROLE_USER"));
		
		
		String nTarget = (String)req.getSession().getAttribute("redirect");
		if (nTarget == null) return new ModelAndView("redirect:/logout");

		// HOOK for testing
		if (testMode && target == null) {
			return new ModelAndView("redirect:/eauth/"+authorityUrl+"?target="+URLEncoder.encode(nTarget, "UTF8")+"&openid.ext1.value.email=my@mail&openid.ext1.value.name=name&openid.ext1.value.surname=surname");
		}
		
		if (!testMode && nTarget != null) target = nTarget;

		eu.trentorise.smartcampus.permissionprovider.model.User userEntity = providerServiceAdapter.updateUser(authorityUrl, req);
		
		UserDetails user = new User(userEntity.getId().toString(),"", list);
		
		AbstractAuthenticationToken a = new UsernamePasswordAuthenticationToken(user, null, list);
		//ExternalAuthenticationToken(user, list, authorityUrl);
		a.setDetails(authorityUrl);

		SecurityContextHolder.getContext().setAuthentication(a);

		return new ModelAndView("redirect:"+target);
	}
	
	/**
	 * Revoke the access token and the associated refresh token.
	 * 
	 * @param token
	 */
	@RequestMapping("/eauth/revoke/{token}")
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
}