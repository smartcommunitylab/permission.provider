package eu.trentorise.smartcampus.permissionprovider.controller;



import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


import eu.trentorise.smartcampus.permissionprovider.exceptions.AlreadyExistException;
import eu.trentorise.smartcampus.permissionprovider.exceptions.SmartCampusException;
import eu.trentorise.smartcampus.permissionprovider.model.App;
import eu.trentorise.smartcampus.permissionprovider.repository.AppRepository;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;


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
			throws SmartCampusException, NotFoundException {
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

    