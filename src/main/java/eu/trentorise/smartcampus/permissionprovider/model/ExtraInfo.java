package eu.trentorise.smartcampus.permissionprovider.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "extra_info")
public class ExtraInfo implements Serializable {

	private static final long serialVersionUID = 5230059903077624168L;

	public ExtraInfo() {
		super();
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

}
