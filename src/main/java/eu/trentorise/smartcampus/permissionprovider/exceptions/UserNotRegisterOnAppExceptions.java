package eu.trentorise.smartcampus.permissionprovider.exceptions;

public class UserNotRegisterOnAppExceptions extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8981902868678130463L;
	
	public UserNotRegisterOnAppExceptions(String appname){
		super("User is not registered on app "+ appname);
	}

}
