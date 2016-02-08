package eu.trentorise.smartcampus.permissionprovider.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.permissionprovider.beans.ExtUser;
import eu.trentorise.smartcampus.permissionprovider.manager.BasicProfileManager;
import eu.trentorise.smartcampus.permissionprovider.manager.ProviderServiceAdapter;
import eu.trentorise.smartcampus.permissionprovider.model.User;

@Controller
@RequestMapping(value = "/extuser")
public class ExtUserController extends AbstractController {

	private Log logger = LogFactory.getLog(getClass());

	@Autowired
	private BasicProfileManager profileManager;
	@Autowired
	private ProviderServiceAdapter provider;

	@RequestMapping(value="/create", method = RequestMethod.POST)
	public @ResponseBody String createuser(@RequestBody ExtUser user, HttpServletRequest req, HttpServletResponse res) {
		if (!StringUtils.hasText(user.getUsername()) ||
				!StringUtils.hasText(user.getName()) ||
				!StringUtils.hasText(user.getEmail()))
		{
			res.setStatus(400);
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("username", user.getUsername());
		map.put("name", user.getName());
		map.put("surname", user.getSurname());
		map.put("email", user.getEmail());
		User updateUser = provider.updateUser("welive", map, req);
		return ""+updateUser.getId();
	}


}
