package eu.trentorise.smartcampus.permissionprovider.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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

	@Value("${api.token}")
	private String token;

	
	@RequestMapping(value="/create", method = RequestMethod.POST)
	public @ResponseBody String createuser(@RequestHeader("Authorization") String token,  @RequestBody ExtUser user, HttpServletRequest req, HttpServletResponse res) {
		
		logger.info(user);
		
		if (token == null || !token.matches(getAPICredentials())) {
			res.setStatus(HttpStatus.UNAUTHORIZED.value());
			return "";
		} 
		
		if (
				!StringUtils.hasText(user.getName()) ||
				!StringUtils.hasText(user.getEmail()))
		{
			res.setStatus(400);
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("name", user.getName());
		map.put("surname", user.getSurname());
		
		// trim email for issue (#639)
		String email = user.getEmail().trim();
		map.put("email", email);
		
		if (StringUtils.hasText(user.getUsername())) {
			map.put("username", user.getUsername());
		} else {
			map.put("username", email);
		}  
		User updateUser = provider.updateUser("welive", map, req);
		return ""+updateUser.getId();
	}

	/**
	 * @return
	 */
	private String getAPICredentials() {
		return "Basic "+ token;
	}


}
