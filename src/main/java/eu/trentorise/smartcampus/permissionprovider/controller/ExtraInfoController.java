package eu.trentorise.smartcampus.permissionprovider.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.trentorise.smartcampus.permissionprovider.beans.ExtraInfoBean;
import eu.trentorise.smartcampus.permissionprovider.manager.BasicProfileManager;
import eu.trentorise.smartcampus.permissionprovider.manager.ExtraInfoManager;
import eu.trentorise.smartcampus.profile.model.BasicProfile;

@Controller
@RequestMapping(value = "/collect-info")
public class ExtraInfoController extends AbstractController {

	private Log logger = LogFactory.getLog(getClass());

	@Autowired
	private ExtraInfoManager infoManager;

	@Autowired
	private BasicProfileManager profileManager;

	@RequestMapping(method = RequestMethod.GET)
	public String load(Model model) {
		BasicProfile profile = profileManager.getBasicProfileById(Long
				.toString(getUserId()));
		ExtraInfoBean info = new ExtraInfoBean();
		info.setName(profile.getName() != null ? profile.getName() : "");
		info.setSurname(profile.getSurname() != null ? profile.getSurname()
				: "");
		model.addAttribute("info", info);
		return "collect_info";
	}

	// bind name of bean in ModelAttribute annotation should be defined. If not
	// error are not shown in view
	@RequestMapping(method = RequestMethod.POST)
	public String collectInfo(
			@ModelAttribute("info") @Valid ExtraInfoBean info,
			BindingResult result, Model model, HttpServletRequest req,
			HttpServletResponse res) {
		if (result.hasErrors()) {
			return "collect_info";
		} else {
			infoManager.collectInfoForUser(info, getUserId());
			logger.info(String.format("Collected info for user "));
			String redirectURL = (String) req.getSession().getAttribute(
					"redirect");
			logger.info(String.format("Redirected to url %s", redirectURL));
			return "redirect:" + redirectURL;
		}
	}

}
