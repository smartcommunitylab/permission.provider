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

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.saml2.core.LogoutRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import edu.yale.tp.cas.AuthenticationFailureType;
import edu.yale.tp.cas.AuthenticationSuccessType;
import edu.yale.tp.cas.ObjectFactory;
import edu.yale.tp.cas.ServiceResponseType;
import eu.trentorise.smartcampus.permissionprovider.cas.CASException;
import eu.trentorise.smartcampus.permissionprovider.cas.CASException.ERROR_CODE;
import eu.trentorise.smartcampus.permissionprovider.cas.TicketManager;
import eu.trentorise.smartcampus.permissionprovider.cas.TicketManager.Ticket;
import eu.trentorise.smartcampus.permissionprovider.common.Utils;
import eu.trentorise.smartcampus.permissionprovider.model.AuthProtocolType;
import eu.trentorise.smartcampus.permissionprovider.model.SingleSignoutData;
import eu.trentorise.smartcampus.permissionprovider.model.User;
import eu.trentorise.smartcampus.permissionprovider.repository.UserRepository;

/**
 * Controller for performing the basic operations over the client apps.
 * 
 * @author raman
 *
 */
@Controller
@Transactional
public class CASController extends AbstractController {

	private Log logger = LogFactory.getLog(getClass());

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private TicketManager ticketManager;

	private static ObjectFactory factory = new ObjectFactory();

	ConcurrentHashMap<String, SingleSignoutData> stateMap = null;

	@Value("${welive.cas.callback.slo}")
	private String callBackSingleLogoutUrl;

	/**
	 * After the user authenticated redirect to the requested service URL with
	 * the ticket.
	 * 
	 * @return
	 */
	// @RequestMapping("/cas/login")
	// public ModelAndView casLogin(HttpServletRequest req, HttpServletResponse
	// res, @RequestParam String service) {
	//
	// /** 1. extract service and logout url of client. **/
	// String[] sessionParams = service.split("&LogoutUrl=");
	// req.getSession().setAttribute("_service", sessionParams[0]);
	//
	// /** 2. create/update stateMap<stateKey, logoutUrl>. **/
	// if (req.getSession().getAttribute("stateMap") != null) {
	// stateMap = (java.util.concurrent.ConcurrentHashMap <String, String>)
	// req.getSession().getAttribute("stateMap");
	// } else {
	// stateMap = new java.util.concurrent.ConcurrentHashMap<String, String>();
	// req.getSession().setAttribute("stateMap", stateMap);
	// }
	//
	// /** 3. update stateMap<stateKey, logoutUrl>. **/
	// stateMap.put(UUID.randomUUID().toString(), sessionParams[1]); // logout
	// url.
	//
	// /** 4. redirect back to client homepage. **/
	// return new ModelAndView("redirect:/cas/loginsuccess");
	// }

	@RequestMapping("/cas/login")
	public ModelAndView casLogin(HttpServletRequest req, HttpServletResponse res, @RequestParam String service) {

		/** 1. extract service and logout url of client. **/
		req.getSession().setAttribute("_service", service);

		/** 2. redirect back to client homepage. **/
		return new ModelAndView("redirect:/cas/loginsuccess");
	}

	@RequestMapping("/cas/logout")
	public ModelAndView singleLogout(HttpServletRequest req, HttpServletResponse res,
			@RequestParam(required = false) String service, @RequestParam(required = false) String RelayState)
			throws Exception {

		SingleSignoutData temp = null;
		
		/** 1. determine and make redirect to other SPs in session. **/
		if (req.getSession().getAttribute("stateMap") != null && RelayState == null) {

			req.getSession().setAttribute("redirectUrl", service);
			
			stateMap = (java.util.concurrent.ConcurrentHashMap<String, SingleSignoutData>) req.getSession()
					.getAttribute("stateMap");

			Map.Entry<String, SingleSignoutData> entry = stateMap.entrySet().iterator().next();
			String key = entry.getKey();
			SingleSignoutData singleSignoutData = entry.getValue();
			String ticket = singleSignoutData.getSessionIdentifier();

			// generate SAML logout request.
			LogoutRequest logoutRequest = Utils.genererateLogoutRequest("xxxx", ticket);

			return new ModelAndView("redirect:" + singleSignoutData.getRedirectUrl() + "?RelayState=" + key
					+ "&SAMLRequest=" + Utils.encodeRequestMessage(logoutRequest));

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
				LogoutRequest logoutRequest = Utils.genererateLogoutRequest("xxxx", nextSSOData.getSessionIdentifier());
				
				return new ModelAndView("redirect:" + nextSSOData.getRedirectUrl() + "?RelayState=" + key
						+ "&SAMLRequest=" + Utils.encodeRequestMessage(logoutRequest));

			} else {
				String redirectUrl = (String) req.getSession().getAttribute("redirectUrl");
				// clear local session.
				req.getSession().invalidate();
				return new ModelAndView("redirect:" + redirectUrl);

			}
		}
		/**
		 * 3. make redirectUrl to other SPs passing as params (callBackUrl,
		 * stateKey).
		 **/
		return new ModelAndView("redirect:" + service);
	}

	/**
	 * After the user authenticated redirect to the requested service URL with
	 * the ticket.
	 * 
	 * @return
	 */
	@RequestMapping("/cas/loginsuccess")
	public ModelAndView casLoginsuccess(HttpServletRequest req, HttpServletResponse res,
			@RequestParam(required = false) String service) {
		try {
			if (service == null) {
				service = (String) req.getSession().getAttribute("_service");
				if (service == null) {
					logger.error("CAS login error: no service URL specified");
					res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					return new ModelAndView("error");
				}
			}

			checkService(req, res, service);
			User user = userRepository.findOne(getUserId());
			String ticket = ticketManager.getTicket(user.getId().toString(), service);

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
			SingleSignoutData ssData = new SingleSignoutData(AuthProtocolType.CAS.toString(), ticket, service);
			/** 3. update stateMap<stateKey, logoutUrl>. **/
			stateMap.put(UUID.randomUUID().toString(), ssData); // logout url.

			return new ModelAndView("redirect:" + service + "?ticket=" + ticket);

		} catch (CASException e) {
			logger.error("CAS login error: " + e.getMessage());
			res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return new ModelAndView("redirect:" + service);
		}
	}

	/**
	 * Retrieve the with the user data: currently on the username is added.
	 * 
	 * @return
	 */
	@RequestMapping("/cas/serviceValidate")
	public @ResponseBody String validateService(HttpServletRequest req, HttpServletResponse res,
			@RequestParam String service, @RequestParam String ticket) {
		try {
			res.setContentType("application/xml");
			String[] sessionParams = service.split("&LogoutUrl=");
			// checkService(req, res, service);
			checkService(req, res, sessionParams[0]);
			// Ticket obj = ticketManager.checkTicket(service, ticket);
			Ticket obj = ticketManager.checkTicket(sessionParams[0], ticket);
			User user = userRepository.findOne(Long.parseLong(obj.getId()));
			return generateSuccess(user, obj.isFromNewLogin());// new
																// ModelAndView("redirect:"+service);
		} catch (CASException e) {
			logger.error("CAS login error: " + e.getMessage());
			res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			try {
				return generateFailure(e.getCode().toString(), e.getMessage());
			} catch (Exception e1) {
				logger.error("CAS login error: " + e.getMessage());
				res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("CAS login error: " + e.getMessage());
			res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}

	}

	/**
	 * @param req
	 * @param res
	 * @param service
	 * @return true if the service is a valid HTTPS URL.
	 */
	private void checkService(HttpServletRequest req, HttpServletResponse res, String service) throws CASException {
		try {
			// URI url =
			URI.create(service);
			// if (!"https".equals(url.getScheme())) throw new
			// CASException(ERROR_CODE.INVALID_PROXY_CALLBACK, "Non HTTPS
			// callback");
		} catch (Exception e) {
			logger.error("Incorrect service URL : " + service);
			throw new CASException(ERROR_CODE.INVALID_PROXY_CALLBACK, "Invalid callback address");
		}
	}

	private static String generateSuccess(User user, boolean isNew) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ServiceResponseType value = factory.createServiceResponseType();

		AuthenticationSuccessType success = factory.createAuthenticationSuccessType();
		success.setUser("" + user.email());
		// AttributesType attrs = factory.createAttributesType();
		// attrs.setIsFromNewLogin(isNew);
		// attrs.getAny().add(createElement("name", user.getName()));
		// attrs.getAny().add(createElement("surname", user.getSurname()));
		// if (user.getAttributeEntities() != null) {
		// for(Attribute a : user.getAttributeEntities()) {
		// attrs.getAny().add(createElement(a.getKey(), a.getValue()));
		// }
		// }
		// success.setAttributes(attrs);
		value.setAuthenticationSuccess(success);

		JAXBElement<ServiceResponseType> createServiceResponse = factory.createServiceResponse(value);

		JAXB.marshal(createServiceResponse, os);
		return os.toString();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static JAXBElement<Object> createElement(String key, String value) {
		return new JAXBElement(new QName("http://www.yale.edu/tp/cas", key, "cas"), String.class,
				value == null ? "" : value);
	}

	/**
	 * @param string
	 * @return
	 * @throws JAXBException
	 */
	private static String generateFailure(String code, String codeValue) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ServiceResponseType value = factory.createServiceResponseType();

		AuthenticationFailureType failure = factory.createAuthenticationFailureType();
		failure.setValue(codeValue);
		failure.setCode(code);
		value.setAuthenticationFailure(failure);
		JAXBElement<ServiceResponseType> createServiceResponse = factory.createServiceResponse(value);

		JAXB.marshal(createServiceResponse, os);

		return os.toString();
	}
}
