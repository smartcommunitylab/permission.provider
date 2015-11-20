package eu.trentorise.smartcampus.permissionprovider.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import eu.trentorise.smartcampus.permissionprovider.model.ExtraInfo;
import eu.trentorise.smartcampus.permissionprovider.model.User;

@Repository
public interface ExtraInfoRepository extends CrudRepository<ExtraInfo, Long> {

	public ExtraInfo findByUser(User u);
}
