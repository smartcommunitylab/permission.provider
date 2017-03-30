package eu.trentorise.smartcampus.permissionprovider.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.support.RequestContextUtils;

import eu.trentorise.smartcampus.permissionprovider.beans.ExtraInfoBean;
import eu.trentorise.smartcampus.permissionprovider.manager.BasicProfileManager;
import eu.trentorise.smartcampus.permissionprovider.manager.ExtraInfoManager;
import eu.trentorise.smartcampus.permissionprovider.manager.WeLiveLogger;
import eu.trentorise.smartcampus.permissionprovider.model.User;
import eu.trentorise.smartcampus.profile.model.AccountProfile;

@Controller
public class ExtraInfoController extends AbstractController {

	@Autowired
	private WeLiveLogger weLiveLogger;

	private Log logger = LogFactory.getLog(getClass());

	@Autowired
	private ExtraInfoManager infoManager;

	@Autowired
	private BasicProfileManager profileManager;

	@RequestMapping(value = "/terms", method = RequestMethod.GET)
	public String terms(Model model, HttpServletRequest req) {
		Locale locale = RequestContextUtils.getLocale(req);
		String languageCode = locale.getLanguage();
		if (languageCode.length() > 2) {
			logger.warn("cookie resolver locale language issue" + languageCode);
			languageCode = locale.getDefault().getLanguage();
		}
        model.addAttribute("language", languageCode);
        return "terms";
	}
	
	@RequestMapping(value = "/collect-info", method = RequestMethod.GET)
	public String load(Model model, HttpServletRequest req) {
//		BasicProfile profile = profileManager.getBasicProfileById(Long
//				.toString(getUserId()));
		User profile = (User) req.getSession().getAttribute("NEW_USER");
		ExtraInfoBean info = new ExtraInfoBean();
		
//		AccountProfile accProfile = profileManager.getAccountProfileById(profile.getUserId());
		info.setEmail(profile.email());
		
		info.setName(profile.getName() != null ? profile.getName() : "");
		info.setSurname(profile.getSurname() != null ? profile.getSurname()
				: "");
		model.addAttribute("info", info);
		Locale locale = RequestContextUtils.getLocale(req);
        model.addAttribute("language", locale);
		
		return "collect_info";
	}

	/**
	 * @param accProfile
	 * @return
	 */
	private String getEmail(AccountProfile accProfile) {
		String email = accProfile.getAttribute("google", "OIDC_CLAIM_email");
		
		if (email == null) {
			//facebook.
			email = accProfile.getAttribute("facebook", "email");
		}
		
		return email;
	}

	// bind name of bean in ModelAttribute annotation should be defined. If not
	// error are not shown in view
	@RequestMapping(value = "/collect-info", method = RequestMethod.POST, params = "save")
	public String collectInfo(
			@ModelAttribute("info") @Valid ExtraInfoBean info,
			BindingResult result, Model model, HttpServletRequest req,
			HttpServletResponse res) {
		
		if (result.hasErrors()) {
			return "collect_info";
		} else {
			if(info.getPilot().compareTo("Novisad") == 0){
				req.getSession().setAttribute("extra_info_data", info);
				return "extra_info_confirm";
			}
			try {
				/** extra user from request. issue#344**/
				User newUser = (User) req.getSession().getAttribute("NEW_USER");
				saveInfo(info, newUser);
			} catch (Exception e) {
				e.printStackTrace();
				model.addAttribute("genericError", "error_error");
				return "collect_info";
			}
			String redirectURL = (String) req.getSession().getAttribute(
					"redirect");
			logger.info(String.format("Redirected to url %s", redirectURL));
			return "redirect:" + redirectURL;
		}
	}
	
	@RequestMapping(value = "/collect-info", method = RequestMethod.POST, params = "accept")
	public String acceptInfo(
			@ModelAttribute("info") @Valid ExtraInfoBean info,
			BindingResult result, Model model, HttpServletRequest req,
			HttpServletResponse res) {
		info = (ExtraInfoBean)req.getSession().getAttribute("extra_info_data");
		try {
			/** extra user from request. issue#344**/
			User newUser = (User) req.getSession().getAttribute("NEW_USER");
			saveInfo(info, newUser);
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("genericError", "error_error");
			return "collect_info";
		}
		String redirectURL = (String) req.getSession().getAttribute(
				"redirect");
		logger.info(String.format("Redirected to url %s", redirectURL));
		return "redirect:" + redirectURL;
		
	}

	@RequestMapping(value = "/collect-info", method = RequestMethod.POST, params = "reject")
	public String rejectInfo(
			@ModelAttribute("info") @Valid ExtraInfoBean info,
			BindingResult result, Model model, HttpServletRequest req,
			HttpServletResponse res) {
		info = (ExtraInfoBean)req.getSession().getAttribute("extra_info_data");
		model.addAttribute("info", info);
		return "collect_info";
	}
	
	private void saveInfo(ExtraInfoBean info, User newUser) throws Exception {
		info.setDeveloper(true);
		newUser = infoManager.collectInfoForUser(info, newUser);
		List<GrantedAuthority> list = Collections
				.<GrantedAuthority> singletonList(new SimpleGrantedAuthority("ROLE_USER"));
		UserDetails user = new org.springframework.security.core.userdetails.User(newUser.getId().toString(), "", list);

		AbstractAuthenticationToken a = new UsernamePasswordAuthenticationToken(user, null, list);
		a.setDetails(SecurityContextHolder.getContext().getAuthentication().getDetails());

		SecurityContextHolder.getContext().setAuthentication(a);

		Map<String,Object> logMap = new HashMap<String, Object>();
		logMap.put("userid", ""+newUser.getId());
		logMap.put("authority", SecurityContextHolder.getContext().getAuthentication().getDetails());
		weLiveLogger.log(WeLiveLogger.USER_CREATED, logMap);
		
		logger.info(String.format("Collected info for user "));
	}
	
//	@RequestMapping(params = "skip", method = RequestMethod.POST)
//	public String skipCollectInfo(HttpServletRequest request, Model model) {
//		String redirectURL = (String) request.getSession().getAttribute(
//				"redirect");
//		
//		BasicProfile profile = profileManager.getBasicProfileById(Long
//				.toString(getUserId()));
//		ExtraInfoBean info = new ExtraInfoBean();
//		
//		AccountProfile accProfile = profileManager.getAccountProfileById(profile.getUserId());
//		info.setEmail(getEmail(accProfile));
//		
//		info.setName(profile.getName() != null ? profile.getName() : "");
//		info.setSurname(profile.getSurname() != null ? profile.getSurname()
//				: "");
//
//		
//		try {
//			infoManager.collectInfoForUser(info, getUserId());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		logger.info("Skipped collection info for user " + getUserId());
//		return "redirect:" + redirectURL;
//	}

}
