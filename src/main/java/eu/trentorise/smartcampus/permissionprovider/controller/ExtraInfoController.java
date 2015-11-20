package eu.trentorise.smartcampus.permissionprovider.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import eu.trentorise.smartcampus.permissionprovider.beans.ExtraInfoBean;
import eu.trentorise.smartcampus.permissionprovider.manager.ExtraInfoManager;

@Controller
public class ExtraInfoController extends AbstractController {

	private Log logger = LogFactory.getLog(getClass());

	@Autowired
	private ExtraInfoManager infoManager;

	@RequestMapping(value = "/extra-info", method = RequestMethod.POST)
	public ModelAndView collectInfo(@ModelAttribute ExtraInfoBean info,
			HttpServletRequest req, HttpServletResponse res) {
		infoManager.collectInfoForUser(info, getUserId());
		logger.info(String.format("Collected info for user "));
		String redirectURL = (String) req.getSession().getAttribute("redirect");
		logger.info(String.format("Redirected to url %s", redirectURL));
		return new ModelAndView("redirect:" + redirectURL);
	}
}
