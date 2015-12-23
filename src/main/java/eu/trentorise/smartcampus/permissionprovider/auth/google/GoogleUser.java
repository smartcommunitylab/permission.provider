/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.permissionprovider.auth.google;

/**
 * Model for user's info logged by Google.
 * 
 * @author Giulia Canobbio
 *
 */
public class GoogleUser{
	
	private String id;
	
	private String email;
	
	private boolean verified_email;
	
	private String name;

	private String given_name;
	
	private String family_name;
	
	private String picture;
	
	private String locale;
	
	/**
	 * Get google user id.
	 * 
	 * @return String id
	 */
	public String getId() {
		return id;
	}
	/**
	 * Set google user id.
	 * 
	 * @param id 
	 * 			: String
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * Get email address.
	 * 
	 * @return String email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * Set email address.
	 * 
	 * @param email 
	 * 			: String
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 * Check if email is verified.
	 * 
	 * @return boolean true if email is verified in Google o.w. false
	 */
	public boolean isVerified_email() {
		return verified_email;
	}
	/**
	 * Set email verified value.
	 * 
	 * @param verified_email 
	 * 			: boolean
	 */
	public void setVerified_email(boolean verified_email) {
		this.verified_email = verified_email;
	}
	/**
	 * Get username.
	 * 
	 * @return String name : username in Google
	 */
	public String getName() {
		return name;
	}
	/**
	 * Set username.
	 * 
	 * @param name 
	 * 			: String username
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * Get first name.
	 * 
	 * @return String first name
	 */
	public String getGiven_name() {
		return given_name;
	}
	/**
	 * Set first name.
	 * 
	 * @param given_name 
	 * 			: String first name
	 */
	public void setGiven_name(String given_name) {
		this.given_name = given_name;
	}
	/**
	 * Get surname.
	 * 
	 * @return String family name
	 */
	public String getFamily_name() {
		return family_name;
	}
	/**
	 * Set surname.
	 * 
	 * @param family_name 
	 * 			: String
	 */
	public void setFamily_name(String family_name) {
		this.family_name = family_name;
	}
	/**
	 * Get image.
	 * 
	 * @return String link to picture
	 */
	public String getPicture() {
		return picture;
	}
	/**
	 * Set image.
	 * 
	 * @param picture 
	 * 			: String link to picture
	 */
	public void setPicture(String picture) {
		this.picture = picture;
	}
	/**
	 * Get locale.
	 * 
	 * @return String locale
	 */
	public String getLocale() {
		return locale;
	}
	/**
	 * Set locale.
	 * 
	 * @param locale 
	 * 			: String
	 */
	public void setLocale(String locale) {
		this.locale = locale;
	}
	
}
