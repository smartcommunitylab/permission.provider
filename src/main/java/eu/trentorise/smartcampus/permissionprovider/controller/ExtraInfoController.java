package eu.trentorise.smartcampus.permissionprovider.controller;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.support.RequestContextUtils;

import eu.trentorise.smartcampus.permissionprovider.beans.ExtraInfoBean;
import eu.trentorise.smartcampus.permissionprovider.manager.BasicProfileManager;
import eu.trentorise.smartcampus.permissionprovider.manager.ExtraInfoManager;
import eu.trentorise.smartcampus.profile.model.AccountProfile;
import eu.trentorise.smartcampus.profile.model.BasicProfile;

@Controller
@Transactional
public class ExtraInfoController extends AbstractController {

	private Log logger = LogFactory.getLog(getClass());

	@Autowired
	private ExtraInfoManager infoManager;

	@Autowired
	private BasicProfileManager profileManager;

	@RequestMapping(value = "/terms", method = RequestMethod.GET)
	public String terms(Model model, HttpServletRequest req) {
		Locale locale = RequestContextUtils.getLocale(req);
        model.addAttribute("language", locale.getLanguage());
        return "terms";
	}
	
	@RequestMapping(value = "/collect-info", method = RequestMethod.GET)
	public String load(Model model, HttpServletRequest req) {
		BasicProfile profile = profileManager.getBasicProfileById(Long
				.toString(getUserId()));
		ExtraInfoBean info = new ExtraInfoBean();
		
		AccountProfile accProfile = profileManager.getAccountProfileById(profile.getUserId());
		info.setEmail(getEmail(accProfile));
		
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
		if (email != null) return email;
		return null;
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
			info.setDeveloper(true);
			try {
				infoManager.collectInfoForUser(info, getUserId());
			} catch (Exception e) {
				e.printStackTrace();
				model.addAttribute("genericError", "error_error");
				return "collect_info";
			}
			logger.info(String.format("Collected info for user "));
			String redirectURL = (String) req.getSession().getAttribute(
					"redirect");
			logger.info(String.format("Redirected to url %s", redirectURL));
			return "redirect:" + redirectURL;
		}
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
