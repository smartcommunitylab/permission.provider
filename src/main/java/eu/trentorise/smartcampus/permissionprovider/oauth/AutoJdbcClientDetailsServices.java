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

package eu.trentorise.smartcampus.permissionprovider.oauth;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.provider.JdbcClientDetailsService;

/**
 * @author raman
 *
 */
public class AutoJdbcClientDetailsServices extends JdbcClientDetailsService {

	private JdbcTemplate jdbcTemplate;
	
	private static final String DEFAULT_CREATE_TABLE_STATEMENT = "CREATE TABLE IF NOT EXISTS oauth_client_details (" +
			"client_id VARCHAR(256), " +
			"resource_ids TEXT, client_secret VARCHAR(256), scope TEXT, authorized_grant_types TEXT, " +
			"web_server_redirect_uri VARCHAR(256), " +
			"authorities TEXT, " +
			"access_token_validity INTEGER, refresh_token_validity INTEGER, additional_information TEXT, " +
			"id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY);";
	
	private String createStatement = DEFAULT_CREATE_TABLE_STATEMENT;
	
	/**
	 * @param dataSource
	 */
	public AutoJdbcClientDetailsServices(DataSource dataSource) {
		super(dataSource);
		initSchema(dataSource);
	}

	/**
	 * @param dataSource
	 * @param createStatement
	 */
	public AutoJdbcClientDetailsServices(DataSource dataSource, String createStatement) {
		super(dataSource);
		this.createStatement = createStatement;
		initSchema(dataSource); 
	}



	protected void initSchema(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.execute(createStatement);
	}

}
