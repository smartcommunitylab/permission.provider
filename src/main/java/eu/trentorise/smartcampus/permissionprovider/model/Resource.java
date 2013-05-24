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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import eu.trentorise.smartcampus.permissionprovider.Config.AUTHORITY;

/**
 * 
 * @author raman
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resource")
@Entity
public class Resource {
	@Id
	@GeneratedValue
	private Long resourceId;
	/**
	 * Resource category used to group different resources
	 */
	@XmlAttribute(name="resourceType", required = false)
	private String resourceType;
	/**
	 * Resource-specific symbolic name
	 */
	@XmlAttribute(name="resourceUri", required = true)
	private String resourceUri;
	
	/**
	 * Human-readable resource name
	 */
	@XmlAttribute(name="name", required = true)
	private String name;
	/**
	 * Resource description
	 */
	private String description;
	/**
	 * Application that created this resource (if applicable)
	 */
	@XmlAttribute(name="clientId", required = false)
	private String clientId;
	/**
	 * Authority that can access this resource
	 */
	@Enumerated(EnumType.STRING)
	@XmlAttribute(name="authority", required = true)
	private AUTHORITY authority; 

	/**
	 * Whether explicit manual approval required
	 */
	@XmlAttribute(name="approvalRequired", required = false)
	private boolean approvalRequired = false;

	/**
	 * Whether non-owning clients can request and access this resource
	 */
	@XmlAttribute(name="accessibleByClient", required = false)
	private boolean accessibleByClient = true;
	
	/**
	 * @return the resourceId
	 */
	public Long getResourceId() {
		return resourceId;
	}

	/**
	 * @param resourceId the resourceId to set
	 */
	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}

	/**
	 * @return the resourceType
	 */
	public String getResourceType() {
		return resourceType;
	}

	/**
	 * @param resourceType the resourceType to set
	 */
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	/**
	 * @return the resourceUri
	 */
	public String getResourceUri() {
		return resourceUri;
	}

	/**
	 * @param resourceUri the resourceUri to set
	 */
	public void setResourceUri(String resourceUri) {
		this.resourceUri = resourceUri;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
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
	 * @return the authority
	 */
	public AUTHORITY getAuthority() {
		return authority;
	}

	/**
	 * @param authority the authority to set
	 */
	public void setAuthority(AUTHORITY authority) {
		this.authority = authority;
	}

	/**
	 * @return the approvalRequired
	 */
	public boolean isApprovalRequired() {
		return approvalRequired;
	}

	/**
	 * @param approvalRequired the approvalRequired to set
	 */
	public void setApprovalRequired(boolean approvalRequired) {
		this.approvalRequired = approvalRequired;
	}

	/**
	 * @return the accessibleByClient
	 */
	public boolean isAccessibleByClient() {
		return accessibleByClient;
	}

	/**
	 * @param accessibleByClient the accessibleByClient to set
	 */
	public void setAccessibleByClient(boolean accessibleByClient) {
		this.accessibleByClient = accessibleByClient;
	}

}
