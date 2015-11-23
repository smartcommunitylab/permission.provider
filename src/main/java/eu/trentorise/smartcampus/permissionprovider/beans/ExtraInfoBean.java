package eu.trentorise.smartcampus.permissionprovider.beans;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

public class ExtraInfoBean {
	private String name;
	private String surname;
	private String email;
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private Date birthdate;
	private String keywords;

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

}
