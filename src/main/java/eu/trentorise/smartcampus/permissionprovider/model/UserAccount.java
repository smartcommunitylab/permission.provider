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

package eu.trentorise.smartcampus.permissionprovider.model;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import eu.trentorise.smartcampus.ac.provider.model.User;

/**
 * User storage account informations
 * 
 * @author mirko perillo
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserAccount {
	/**
	 * id of the account
	 */
	private String id;
	/**
	 * id of the user
	 */
	private User user;

	private String appName;

	/**
	 * list of the configurations of the account storage
	 */
	@XmlElementWrapper
	@XmlElement(name = "configuration")
	private List<Configuration> configurations;
	
	//@XmlElementWrapper
	//@XmlElement(name = "permission")
	

	
	public List<Configuration> getConfigurations() {
		return configurations;
	}

	public void setConfigurations(List<Configuration> configurations) {
		this.configurations = configurations;
	}

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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	private  Map<String, Map<String, List<String>>> permission;

	public  Map<String, Map<String, List<String>>> getPermission() {
		return permission;
	}

	public void setPermission( Map<String, Map<String, List<String>>> permission) {
		this.permission = permission;
	}
}
