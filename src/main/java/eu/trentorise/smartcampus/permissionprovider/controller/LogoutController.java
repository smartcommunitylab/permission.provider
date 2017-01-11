/*******************************************************************************
 * Copyright 2015 Fondazione Bruno Kessler
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
package eu.trentorise.smartcampus.permissionprovider.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensaml.saml2.core.LogoutRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.trentorise.smartcampus.permissionprovider.common.Utils;
import eu.trentorise.smartcampus.permissionprovider.model.SingleSignoutData;

/**
 * @author raman
 *
 */
@Controller
public class LogoutController {

	@Value("${default.redirect.url}")
	private String defaultRedirect;
		
	@Value("${welive.cas.server}")
	private String casServer;
	
	/** stateMap for holding single signout data. **/
	ConcurrentHashMap<String, SingleSignoutData> stateMap = null;

	/**
	 * Logout from CAS protocol.
	 * @return
	 */
	@RequestMapping("/caslogout")
	public ModelAndView casLogout(HttpServletRequest req, HttpServletResponse res, @RequestParam(required=false) String service) {
		return logoutCommon(req, service);
	}
	/**
	 * Logout from SSO.
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping("/ssologout")
	public ModelAndView ssoLogout(HttpServletRequest req, HttpServletResponse res, @RequestParam(required=false) String RelayState,
			@RequestParam(required=false) String redirect) throws Exception {
		
		SingleSignoutData temp = null;
		
		/** 1. determine and make redirect to other SPs in session. **/
		if (req.getSession().getAttribute("stateMap") != null && RelayState == null) {

			req.getSession().setAttribute("redirectUrl", redirect);
			
			stateMap = (java.util.concurrent.ConcurrentHashMap<String, SingleSignoutData>) req.getSession()
					.getAttribute("stateMap");

			Map.Entry<String, SingleSignoutData> entry = stateMap.entrySet().iterator().next();
			String key = entry.getKey();
			SingleSignoutData singleSignoutData = entry.getValue();
//			String ticket = singleSignoutData.getSessionIdentifier();

			// generate SAML logout request.
//			LogoutRequest logoutRequest = Utils.genererateLogoutRequest("xxxx", ticket);

			return new ModelAndView("redirect:" + singleSignoutData.getRedirectUrl() + "?RelayState=" + key
					+ "&callback=" + "/ssologout");

		}

		/** 2. if its callback response with RelayState, do the cleaning **/
		if (RelayState != null && !RelayState.isEmpty()) {
			
			// 1. remove it from local session.
			if (req.getSession().getAttribute("stateMap") != null) {

				stateMap = (java.util.concurrent.ConcurrentHashMap<String, SingleSignoutData>) req.getSession()
						.getAttribute("stateMap");
				
				// copy SSOData in temp.
				temp = stateMap.get(RelayState);
				
				// remove
				stateMap.remove(RelayState);
			}
			
			// 2. proceed with next logout. **/
			if (!stateMap.isEmpty()) {
				
				Map.Entry<String, SingleSignoutData> entry = stateMap.entrySet().iterator().next();
				String key = entry.getKey();
				SingleSignoutData nextSSOData = entry.getValue();
//				LogoutRequest logoutRequest = Utils.genererateLogoutRequest("xxxx", nextSSOData.getSessionIdentifier());
				
				return new ModelAndView("redirect:" + nextSSOData.getRedirectUrl() + "?RelayState=" + key
						+ "&callback=" + "/ssologout");

			} else {
				String redirectUrl = (String) req.getSession().getAttribute("redirectUrl");
				// clear local session.
				req.getSession().invalidate();
				return new ModelAndView("redirect:" + redirectUrl);

			}
		}
		
		
		return logoutCommon(req, redirect);
	}

	private ModelAndView logoutCommon(HttpServletRequest req, String service) {
		String redirect = StringUtils.hasText(service) ? service : defaultRedirect;

		Authentication old = SecurityContextHolder.getContext().getAuthentication();
		if (old != null && old instanceof UsernamePasswordAuthenticationToken) {
			if ("welive".equals(old.getDetails())) {
				redirect = casServer+"/logout?service="+redirect;
			}
		}
		req.getSession().invalidate();
		
		return new ModelAndView("redirect:"+redirect);
	}	
}
