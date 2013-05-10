package eu.trentorise.smartcampus.permissionprovider.model;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import eu.trentorise.smartcampus.permissionprovider.security.SecurityRole;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AppAccount {
	private String id;
	private String appName;
	private String appToken;
	private SecurityRole role;

	@XmlElementWrapper
	@XmlElement(name = "configuration")
	private List<Configuration> configurations;

	@XmlElementWrapper
	@XmlElement(name = "permission")
	private Map<String, List<String>> permission;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public List<Configuration> getConfigurations() {
		return configurations;
	}

	public void setConfigurations(List<Configuration> configurations) {
		this.configurations = configurations;
	}

	public String getAppToken() {
		return appToken;
	}

	public void setAppToken(String appToken) {
		this.appToken = appToken;
	}

	public Map<String, List<String>> getPermission() {
		return permission;
	}

	public void setPermission(Map<String, List<String>> permission) {
		this.permission = permission;
	}

	public SecurityRole getRole() {
		return role;
	}

	public void setRole(SecurityRole role) {
		this.role = role;
	}

}
