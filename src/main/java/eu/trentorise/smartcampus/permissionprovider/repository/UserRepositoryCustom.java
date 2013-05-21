package eu.trentorise.smartcampus.permissionprovider.repository;

import java.util.List;

import eu.trentorise.smartcampus.permissionprovider.model.Attribute;
import eu.trentorise.smartcampus.permissionprovider.model.User;

public interface UserRepositoryCustom {

	List<User> getUsersByAttributes(List<Attribute> list);

}
