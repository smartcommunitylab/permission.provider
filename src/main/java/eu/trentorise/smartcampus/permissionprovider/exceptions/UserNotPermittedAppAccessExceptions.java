package eu.trentorise.smartcampus.permissionprovider.exceptions;

public class UserNotPermittedAppAccessExceptions extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8981902868678130463L;
	
	public UserNotPermittedAppAccessExceptions(String appname){
		super("App has not permission on this user. App: "+ appname);
	}

}
