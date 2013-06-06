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

package eu.trentorise.smartcampus.permissionprovider.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import eu.trentorise.smartcampus.permissionprovider.model.ClientDetailsEntity;
import eu.trentorise.smartcampus.permissionprovider.repository.ClientDetailsRepository;

/**
 * @author raman
 *
 */
public class AbstractController {

	@Autowired
	private ClientDetailsRepository clientDetailsRepository;

	protected void checkClientIdOwnership(String clientId) {
		ClientDetailsEntity client = clientDetailsRepository.findByClientId(clientId);
		if (client == null || !client.getDeveloperId().equals(getUserId())) {
			throw new SecurityException("Attempt modifyung non-owned client app data");
		};
	}
	
	protected UserDetails getUser(){
		return (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}
	
	protected Long getUserId() {
		return Long.parseLong(getUser().getUsername());
	}

	protected String getUserAuthority() {
		return SecurityContextHolder.getContext().getAuthentication().getDetails().toString();
	}

}
