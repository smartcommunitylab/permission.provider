package eu.trentorise.smartcampus.permissionprovider.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import eu.trentorise.smartcampus.permissionprovider.model.User;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
