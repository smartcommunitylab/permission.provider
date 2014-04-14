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

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import eu.trentorise.smartcampus.permissionprovider.Config.RESOURCE_VISIBILITY;

/**
 *
 * DB entity for resource parameters
 * @author raman
 *
 */
@Entity
@IdClass(ResourceParameterKey.class)
@Table(name="resource_parameter")
public class ResourceParameter {
	@Id
	private String resourceId;
	/**
	 * Service
	 */
	private String serviceId;
	/**
	 * parameter value
	 */
	@Id
	private String value;
	/**
	 * Owning client app
	 */
	private String clientId;
	/**
	 * Visibility of the resource parameter for the other apps
	 */
	@Enumerated(EnumType.STRING)
	private RESOURCE_VISIBILITY visibility = RESOURCE_VISIBILITY.CLIENT_APP;
	/**
	 * @return the resourceId
	 */
	public String getResourceId() {
		return resourceId;
	}
	/**
	 * @param resourceId the resourceId to set
	 */
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	/**
	 * @return the serviceId
	 */
	public String getServiceId() {
		return serviceId;
	}
	/**
	 * @param serviceId the serviceId to set
	 */
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}
	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	/**
	 * @return the visibility
	 */
	public RESOURCE_VISIBILITY getVisibility() {
		return visibility;
	}
	/**
	 * @param visibility the visibility to set
	 */
	public void setVisibility(RESOURCE_VISIBILITY visibility) {
		this.visibility = visibility;
	}
}
