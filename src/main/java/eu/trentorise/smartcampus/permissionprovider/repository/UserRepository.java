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
package eu.trentorise.smartcampus.permissionprovider.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import eu.trentorise.smartcampus.permissionprovider.model.User;
/**
 * Persistent repository of {@link User} entities
 * @author raman
 *
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

	List<User> findByFullNameLike(String text);
	User findBySocialId(String socialId);
	
	@Query("select u from User u left join u.attributeEntities a where a.authority.name=?1 and a.key=?2 and a.value=?3")
	List<User> findByAttribute(String authority, String attribute, String value);
}
