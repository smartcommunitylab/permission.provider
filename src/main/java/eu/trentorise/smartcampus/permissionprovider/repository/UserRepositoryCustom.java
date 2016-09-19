package eu.trentorise.smartcampus.permissionprovider.repository;

import java.util.List;

import eu.trentorise.smartcampus.permissionprovider.model.Attribute;
import eu.trentorise.smartcampus.permissionprovider.model.User;

/**
 * extension of the repository interface to perform custom user search
 * @author raman
 *
 */
public interface UserRepositoryCustom {

	List<User> getUsersByAttributes(List<Attribute> list);

}
