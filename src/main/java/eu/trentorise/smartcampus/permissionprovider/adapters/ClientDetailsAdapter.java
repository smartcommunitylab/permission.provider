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
import java.util.UUID;

import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import eu.trentorise.smartcampus.permissionprovider.model.ClientAppBasic;
import eu.trentorise.smartcampus.permissionprovider.model.ClientAppInfo;
import eu.trentorise.smartcampus.permissionprovider.model.ClientDetailsEntity;
import eu.trentorise.smartcampus.permissionprovider.model.PermissionBasic;

/**
 * @author raman
 *
 */
@Component
@Transactional
public class ClientDetailsAdapter {

	public synchronized String generateClientId() {
		return UUID.randomUUID().toString();
	}
	public synchronized String generateClientSecret() {
		return UUID.randomUUID().toString();
	}
	
	public List<ClientAppBasic> convertClientApps(List<ClientDetailsEntity> entities){
		if (entities == null) {
			return Collections.emptyList();
		}
		List<ClientAppBasic> res = new ArrayList<ClientAppBasic>();
		for (ClientDetailsEntity e : entities) {
			res.add(convertClientApp(e));
		}
		return res;
	}

	/**
	 * @param e
	 * @return
	 */
	public ClientAppBasic convertClientApp(ClientDetails e) {
		ClientAppBasic res = new ClientAppBasic();
		res.setClientId(e.getClientId());
		res.setClientSecret(e.getClientSecret());
		
		ClientAppInfo info = ClientAppInfo.convert(e.getAdditionalInformation());
		if (info != null) {
			res.setName(info.getName());
		}
		res.setRedirectUris(new ArrayList<String>(e.getRegisteredRedirectUri()));
		// TODO
		res.setPermissions(Collections.<PermissionBasic>emptyList());
		return res;
	}
	public String defaultGrantTypes() {
		return "authorization_code,implicit,refresh_token,client_credentials";
	}
	public String defaultAuthorities() {
		return "ROLE_CLIENT";
	}
	public String defaultScope() {
		return "read,write";
	}
}
