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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.util.StringUtils;

/**
 * DB entity representing the user: user ID, social ID, and the attributes
 * @author raman
 *
 */
@Entity
@Table(name="user")
public class User implements Serializable {

	private static final long serialVersionUID = 1067996326671906278L;

	@Id
	@GeneratedValue
	private Long id;

	@OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST,
			CascadeType.REMOVE, CascadeType.MERGE })
	@JoinColumn(name = "USER_ID", nullable=false)
	private Set<Attribute> attributeEntities;

	@Column(name = "social_id")
	private String socialId;

	private String name; 
	private String surname;
	private String fullName;
	
	public User() {
		super();
	}
	
	
	/**
	 * Create user with the specified parameters
	 * @param id
	 * @param socialId
	 * @param name
	 * @param surname
	 * @param attrs 
	 */
	public User(String socialId, String name, String surname, HashSet<Attribute> attrs) {
		super();
		this.socialId = socialId;
		updateNames(name, surname);
		this.attributeEntities = attrs;
	}



	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Set<Attribute> getAttributeEntities() {
		return attributeEntities;
	}

	public void setAttributeEntities(Set<Attribute> attributeEntities) {
		this.attributeEntities = attributeEntities;
	}


	@Override
	public String toString() {
		return name + " " + surname;
	}

	public String getSocialId() {
		return socialId;
	}

	public void setSocialId(String socialId) {
		this.socialId = socialId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}


	/**
	 * Update name/surname params
	 * @param name
	 * @param surname
	 */
	public void updateNames(String name, String surname) {
		if (name != null) setName(name);
		if (surname != null) setSurname(surname);
		setFullName((getName()+" "+getSurname()).trim().toLowerCase());
	}
	
	public void updateEmail(String email) {
		if (attributeEntities != null) {
			Map<String, Authority> authorities = new HashMap<String,Authority>();
			for (Attribute a : attributeEntities) {
				if ("google".equals(a.getAuthority().getName()) && 
					"OIDC_CLAIM_email".equals(a.getKey())) 
				{
					a.setValue(email);
					return;
				}
				if ("welive".equals(a.getAuthority().getName()) && 
						"email".equals(a.getKey())) 
				{
					a.setValue(email);
					return;
				}
				if ("welive".equals(a.getAuthority().getName()) && 
						"username".equals(a.getKey())) 
				{
					a.setValue(email);
					return;
				}
				if ("googlelocal".equals(a.getAuthority().getName()) && 
						"email".equals(a.getKey())) 
				{
					a.setValue(email);
					return;
				}
				if ("facebook".equals(a.getAuthority().getName()) && 
						"email".equals(a.getKey())) 
				{
					a.setValue(email);
					return;
				}
				if ("facebooklocal".equals(a.getAuthority().getName()) && 
						"email".equals(a.getKey())) 
				{
					a.setValue(email);
					return;
				}
				authorities.put(a.getAuthority().getName(), a.getAuthority());
			}
			
			Attribute newAttr = new Attribute();
			newAttr.setValue(email);
			if (authorities.containsKey("facebook")) {
				newAttr.setAuthority(authorities.get("facebook"));
				newAttr.setKey("email");
			} else if (authorities.containsKey("facebooklocal")) {
				newAttr.setAuthority(authorities.get("facebooklocal"));
				newAttr.setKey("email");
			} else if (authorities.containsKey("welive")) {
				newAttr.setAuthority(authorities.get("welive"));
				newAttr.setKey("username");
			} else if (authorities.containsKey("googlelocal")) {
				newAttr.setAuthority(authorities.get("googlelocal"));
				newAttr.setKey("email");
			} else if (authorities.containsKey("google")) {
				newAttr.setAuthority(authorities.get("google"));
				newAttr.setKey("OIDC_CLAIM_email");
			}
			attributeEntities.add(newAttr);
		}
	}
	
	private String findEmail() {
		for (Attribute a : attributeEntities) {
			if ("google".equals(a.getAuthority().getName()) && 
				"OIDC_CLAIM_email".equals(a.getKey())) 
			{
				return a.getValue();
			}
			if ("welive".equals(a.getAuthority().getName()) && 
					"email".equals(a.getKey())) 
			{
				return a.getValue();
			}
			if ("welive".equals(a.getAuthority().getName()) && 
					"username".equals(a.getKey())) 
			{
				return a.getValue();
			}
			if ("googlelocal".equals(a.getAuthority().getName()) && 
					"email".equals(a.getKey())) 
			{
				return a.getValue();
			}
			if ("facebook".equals(a.getAuthority().getName()) && 
					"email".equals(a.getKey())) 
			{
				return a.getValue();
			}
			if ("facebooklocal".equals(a.getAuthority().getName()) && 
					"email".equals(a.getKey())) 
			{
				return a.getValue();
			}
		}
		return null;
	}
	public String email() {
		if (attributeEntities != null) {
			String res = findEmail();
			if (StringUtils.hasText(res) && !"null".equals(res)) return res;
		}
		return null;
	}
}
