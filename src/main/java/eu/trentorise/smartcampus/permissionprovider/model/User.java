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
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

/**
 * DB entity representing the user: user ID, social ID, and the attributes
 * @author raman
 *
 */
@Entity
public class User implements Serializable {

	private static final long serialVersionUID = 1067996326671906278L;

	@Id
	@GeneratedValue
	private Long id;

	@OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST,
			CascadeType.REMOVE, CascadeType.MERGE })
	@JoinColumn(name = "USER_ID", nullable=false)
	private Set<Attribute> attributeEntities;

	@Column(name = "SOCIAL_ID")
	private Long socialId;

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
		return "UserEntity{id=" + id + ",socialId=" + socialId + "}";
	}

	public Long getSocialId() {
		return socialId;
	}

	public void setSocialId(Long socialId) {
		this.socialId = socialId;
	}

}
