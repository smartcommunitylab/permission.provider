package eu.trentorise.smartcampus.permissionprovider.model;

import java.util.List;

public class Client {

	private ClientAppBasic clientAppBasic;
	private Permissions permissions;
	private List<String> serviceIds;

	public ClientAppBasic getClientAppBasic() {
		return clientAppBasic;
	}

	public void setClientAppBasic(ClientAppBasic clientAppBasic) {
		this.clientAppBasic = clientAppBasic;
	}

	public Permissions getPermissions() {
		return permissions;
	}

	public void setPermissions(Permissions permissions) {
		this.permissions = permissions;
	}

	public List<String> getServiceIds() {
		return serviceIds;
	}

	public void setServiceIds(List<String> serviceIds) {
		this.serviceIds = serviceIds;
	}

}
