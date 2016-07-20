/*******************************************************************************
 * Copyright 2015 Fondazione Bruno Kessler
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
 ******************************************************************************/
package eu.trentorise.smartcampus.permissionprovider.model;

import eu.trentorise.smartcampus.permissionprovider.Config.AUTHORITY;

/**
 * @author raman
 *
 */
public class Scope {

	public enum ACCESS_TYPE {
		U,C,UC;

		public static ACCESS_TYPE fromAuthority(AUTHORITY authority) {
			switch (authority) {
			case ROLE_USER: return U;
			case ROLE_CLIENT: return C;
			case ROLE_CLIENT_TRUSTED: return C;
			default: return UC;
			}
		}
	};
	
	private String id;
	private String description;
	private  ACCESS_TYPE access_type;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public ACCESS_TYPE getAccess_type() {
		return access_type;
	}
	public void setAccess_type(ACCESS_TYPE access_type) {
		this.access_type = access_type;
	}
	
	
}
