package eu.trentorise.smartcampus.permissionprovider.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import eu.trentorise.smartcampus.permissionprovider.model.AppResourceUser;
@Repository
public interface AppResourceUserRepository extends
		JpaRepository<AppResourceUser, Long> {

}
