package eu.trentorise.smartcampus.permissionprovider.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import eu.trentorise.smartcampus.ac.provider.AcService;
import eu.trentorise.smartcampus.ac.provider.AcServiceException;
import eu.trentorise.smartcampus.ac.provider.model.User;
import eu.trentorise.smartcampus.exceptions.NotFoundException;
import eu.trentorise.smartcampus.permissionprovider.exceptions.UserNotPermittedAppAccessExceptions;
import eu.trentorise.smartcampus.permissionprovider.exceptions.UserNotRegisterOnAppExceptions;
import eu.trentorise.smartcampus.permissionprovider.manager.AppAccountManager;
import eu.trentorise.smartcampus.permissionprovider.manager.UserAccountManager;
import eu.trentorise.smartcampus.permissionprovider.model.AppAccount;
import eu.trentorise.smartcampus.permissionprovider.model.UserAccount;

@Component("serviceAuthority")
public class ServiceAuthority {

	protected static Logger logger = Logger.getLogger("security");

	@Autowired
	private AppAccountManager appAccountManager;

	@Autowired
	private UserAccountManager userAccountManager;

	@Autowired
	private AcService acService;

	// @PreAuthorize(@serviceAuthority.isPermittedByUser(#auth_token,#appToken,#entity,'WRITE')")//

	public boolean isPermittedByUser(String auth_token, String app_token,
			Object entity, String permission)  {

		AppAccount appAccount = null;
		UserAccount userAccount = null;
		User user = null;

		try {
			appAccount = appAccountManager.getAppAccountByToken(app_token);
		} catch (NotFoundException e) {
			logger.error("No AppAccount found");
			e.printStackTrace();
		}
		try {
			user = acService.getUserByToken(auth_token);
		} catch (AcServiceException e) {
			logger.error("No User found");
			e.printStackTrace();
		}

		if (user != null && appAccount != null) {
			userAccount = userAccountManager.findByUserIdAndAppName(
					user.getId(), appAccount.getAppName());
			try{
			return hasPermission(userAccount, appAccount, entity.getClass(),
					permission);
			}catch(Exception e){
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}

	}

	private boolean hasPermission(UserAccount userAccount,
			AppAccount appAccount, Class<? extends Object> class1,
			String permission) throws UserNotRegisterOnAppExceptions, UserNotPermittedAppAccessExceptions {

		// control app permission
		Map<String, List<String>> mapPermission = appAccount.getPermission();
		if (mapPermission.containsKey(class1)) {
			List<String> listPermessi = mapPermission.get(class1);
			if (listPermessi.contains(permission)) {
				//the app has the permission to work with
				logger.info("the app has the permission to work with");
				return hasUserPermission(userAccount,appAccount.getAppName(),class1,permission);
			} else {
				//the app hasn't the permission to work with
				logger.info("the app hasn't the permission to work with");
				return false;
			}

		} else {
			//the app hasn't the domain object to work on
			logger.info("the app hasn't the domain object to work on");
			return false;
		}

	}

	private boolean hasUserPermission(UserAccount userAccount,
			String appname, Class<? extends Object> class1,
			String permission) throws UserNotRegisterOnAppExceptions, UserNotPermittedAppAccessExceptions {
		
		 Map<String, Map<String, List<String>>> appPermission =userAccount.getPermission();
	
			if (appPermission.containsKey(appname)) {
				Map<String, List<String>> userPermission =appPermission.get(appname);
				if (userPermission.containsKey(userAccount.getUser().getId())) {
					List<String> listPermessi = userPermission.get(userAccount.getUser().getId());
					if(!listPermessi.isEmpty()){
						return true; //token presente
					}else{
						//richiesta token
						throw new UserNotPermittedAppAccessExceptions(appname);
					}
					
				}else{
					//utente non ha dato permessi ad app
					throw new UserNotPermittedAppAccessExceptions(appname);
				}
				
			}else{
				throw new UserNotRegisterOnAppExceptions(appname);
				//utente non registrato con questa app
				
			}
		
		
		
	}

}
