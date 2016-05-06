package eu.trentorise.smartcampus.permissionprovider.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import eu.trentorise.smartcampus.permissionprovider.beans.ExtraInfoBean;

@Entity
@Transactional
@Table(name = "extra_info")
public class ExtraInfo implements Serializable {

	private static final long serialVersionUID = 5230059903077624168L;

	public ExtraInfo() {
		super();
	}

	public ExtraInfo(ExtraInfoBean bean) {
		super();
		BeanUtils.copyProperties(bean, this);
	}

	@Id
	@GeneratedValue
	private Long id;

	@OneToOne
	private User user;

	@Column
	private String name;

	@Column
	private String surname;

	@Column
	private String email;
	@Column
	private Date birthdate;
	@Column
	private String keywords;
	//@Column
	//private String[] language;

	@Column
	private String role;
	
	@Column(length = 1)
	private String gender;

	@Column
	private String address;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	//public String[] getLanguage() {
	//	return language;
	//}

	//public void setLanguage(String[] language) {
	//	this.language = language;
	//}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

}
