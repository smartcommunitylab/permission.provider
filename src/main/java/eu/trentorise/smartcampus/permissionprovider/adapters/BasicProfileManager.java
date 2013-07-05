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

package eu.trentorise.smartcampus.permissionprovider.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.trentorise.smartcampus.basicprofile.model.BasicProfile;
import eu.trentorise.smartcampus.permissionprovider.repository.UserRepository;

/**
 * @author raman
 *
 */
@Component
public class BasicProfileManager {

	@Autowired
	private UserRepository userRepository;
	/**
	 * @param userId
	 * @return
	 */
	public BasicProfile getBasicProfileById(String userId) {
		try {
			return BasicProfileConverter.toBasicProfile(userRepository.findOne(Long.parseLong(userId)));
		} catch (Exception e) {
			throw new IllegalStateException("User with id "+userId +" does not exist");
		}
	}
	/**
	 * returns all users eu.trentorise.smartcampus.profileservice.model in the
	 * system
	 * 
	 * @return the list of all minimal profiles of users
	 * @throws CommunityManagerException
	 */
	public List<BasicProfile> getUsers() {
		try {
			return BasicProfileConverter.toBasicProfile(userRepository.findAll());
		} catch (Exception e) {
			throw new IllegalStateException("Problem reading users: "+e.getMessage());
		}
	}
	/**
	 * returns all minimal eu.trentorise.smartcampus.profileservice.model of
	 * users who match part of name
	 * 
	 * @param name
	 *            the string to match with name of user
	 * @return the list of minimal
	 *         eu.trentorise.smartcampus.profileservice.model of users which
	 *         name contains parameter or an empty list
	 * @throws CommunityManagerException
	 */
	public List<BasicProfile> getUsers(String fullNameFilter) {
		try {
			return BasicProfileConverter.toBasicProfile(userRepository.findUsers(fullNameFilter));
		} catch (Exception e) {
			throw new IllegalStateException("Problem reading users: "+e.getMessage());
		}
	}
	/**
	 * @param userIds
	 * @return
	 */
	public List<BasicProfile> getUsers(List<String> userIds) {
		if (userIds != null) {
			List<BasicProfile> list = new ArrayList<BasicProfile>();
			for (String userId : userIds) {
				try {
					list.add(getBasicProfileById(userId));
				} catch (Exception e) {
					throw new IllegalStateException("Problem reading users: "+e.getMessage());
				}
			}
		}
		return Collections.emptyList();
	}

}
