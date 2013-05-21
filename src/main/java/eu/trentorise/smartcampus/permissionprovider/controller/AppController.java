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



import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.permissionprovider.exceptions.AlreadyExistException;
import eu.trentorise.smartcampus.permissionprovider.exceptions.SmartCampusException;
import eu.trentorise.smartcampus.permissionprovider.model.App;
import eu.trentorise.smartcampus.permissionprovider.repository.AppRepository;


@Controller
@RequestMapping("/app")
public class AppController {
	@Autowired
    private AppRepository appRepository;
	
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/add")
	public @ResponseBody
	App create(HttpServletRequest request,
			@RequestBody App app) throws SmartCampusException,
			AlreadyExistException {

		return appRepository.save(app);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/update")
	public @ResponseBody
	App update(HttpServletRequest request,
			@RequestBody App app, @PathVariable String appName)
			throws SmartCampusException {
		return appRepository.save(app);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/delete")
	public @ResponseBody
	boolean delete(HttpServletRequest request, @PathVariable String appName,
			@PathVariable App app) throws SmartCampusException {
		appRepository.delete(app);
		return true;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/")
	public @ResponseBody
	List getAppAccounts(HttpServletRequest request,
			@PathVariable String appName) throws SmartCampusException {
		List result = new ArrayList();
		result=(appRepository.findAll());
		return result;
	}

}

    