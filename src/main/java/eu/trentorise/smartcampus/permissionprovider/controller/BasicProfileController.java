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

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.basicprofile.model.BasicProfile;
import eu.trentorise.smartcampus.basicprofile.model.BasicProfiles;
import eu.trentorise.smartcampus.permissionprovider.manager.BasicProfileManager;

/**
 * @author raman
 *
 */
@Controller
public class BasicProfileController extends AbstractController {

	private Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private BasicProfileManager profileManager;

	@RequestMapping(method = RequestMethod.GET, value = "/basicprofile/{userId}")
	public @ResponseBody
	BasicProfile getUser(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("userId") String userId) throws IOException {
		try {
			return profileManager.getBasicProfileById(userId);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}

	}

	@RequestMapping(method = RequestMethod.GET, value = "/basicprofile")
	public @ResponseBody
	BasicProfiles searchUsers(
			HttpServletRequest request,
			HttpServletResponse response,
			HttpSession session,
			@RequestParam(value = "filter", required = false) String fullNameFilter)
			throws IOException {

		try {
			List<BasicProfile> list;
			if (fullNameFilter != null && !fullNameFilter.isEmpty()) {
				list = profileManager.getUsers(fullNameFilter);

			} else {
				list = profileManager.getUsers();
			}

			BasicProfiles profiles = new BasicProfiles();
			profiles.setProfiles(list);
			return profiles;

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/basicprofile/me")
	public @ResponseBody
	BasicProfile findProfile(HttpServletResponse response)
			throws IOException {
		try {
			Long user = getUserId();
			if (user == null) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return null;
			}
			return profileManager.getBasicProfileById(user.toString());
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/basicprofile/profiles")
	public @ResponseBody
	BasicProfiles findProfiles(HttpServletRequest request, HttpServletResponse response, @RequestParam List<String> userIds) {
		try {
			BasicProfiles profiles = new BasicProfiles();
			profiles.setProfiles(profileManager.getUsers(userIds));
			return profiles;
		} catch (Exception e) {
			logger.error(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}
	}

}
