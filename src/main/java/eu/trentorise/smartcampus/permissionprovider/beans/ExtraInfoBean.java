package eu.trentorise.smartcampus.permissionprovider.beans;

import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;
import org.springframework.format.annotation.DateTimeFormat;

public class ExtraInfoBean {

	@NotNull
	@Size(min = 1, message = "Required field")
	private String name;

	@NotNull
	@Size(min = 1, message = "Required field")
	private String surname;

	@Size(min = 1, message = "Required field")
	@Email(message = "Not valid email")
	private String email;

	@NotNull
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private Date birthdate;

	@NotNull
	@Size(min = 1, message = "Required field")
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
