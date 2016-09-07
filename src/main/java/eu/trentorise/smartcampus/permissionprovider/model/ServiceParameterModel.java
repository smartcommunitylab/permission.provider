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

import eu.trentorise.smartcampus.permissionprovider.Config.RESOURCE_VISIBILITY;

/**
 * @author raman
 *
 */
public class ServiceParameterModel {

	private String name;
	private String value;
	private String service;
	private RESOURCE_VISIBILITY visibility;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public RESOURCE_VISIBILITY getVisibility() {
		return visibility;
	}
	public void setVisibility(RESOURCE_VISIBILITY visibility) {
		this.visibility = visibility;
	}
	
	
}
