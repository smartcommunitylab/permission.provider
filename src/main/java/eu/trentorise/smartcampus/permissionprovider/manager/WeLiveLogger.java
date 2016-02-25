/*******************************************************************************
 * Copyright 2015 Fondazione Bruno Kessler
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
 ******************************************************************************/
package eu.trentorise.smartcampus.permissionprovider.manager;

import it.smartcommunitylab.logging.LoggingClient;
import it.smartcommunitylab.logging.model.LogMsg;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author raman
 *
 */
@Component
public class WeLiveLogger {

	private static final String APP_ID = "aac";
	
	public static final String USER_CLIENT_AUTHORIZATION = "UserAppAuthorization";
	
	public static final String USER_CREATED = "UserCreated";
	public static final String USER_UPDATED = "UserUpdated";
	public static final String USER_EXTRA_INFO = "UserExtraInfo";
	
	public static final String CLIENT_APP_CREATED = "ClientAppCreated";
	public static final String CLIENT_APP_UPDATED = "ClientAppUpdated";
	public static final String CLIENT_APP_DELETED = "ClientAppDeleted";

	public static final String USER_DEVELOPER_ACCESS = "DeveloperAccess";
	
	@Autowired
	private LoggingClient log;
	
	public void log(String type, Map<String, Object> data) {
		try {
			LogMsg payload = new LogMsg();
			payload.setTimestamp(System.currentTimeMillis());
			payload.setAppId(APP_ID);
			payload.setType(type);
			if (data != null) {
				payload.setCustomAttributes(data);
			}
			log.log(APP_ID, payload);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
