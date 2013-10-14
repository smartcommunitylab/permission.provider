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

package eu.trentorise.smartcampus.permissionprovider.manager;

import it.unitn.disi.sweb.webapi.client.WebApiException;
import it.unitn.disi.sweb.webapi.client.smartcampus.SCWebApiClient;
import it.unitn.disi.sweb.webapi.model.entity.Entity;
import it.unitn.disi.sweb.webapi.model.entity.EntityBase;
import it.unitn.disi.sweb.webapi.model.entity.EntityType;
import it.unitn.disi.sweb.webapi.model.smartcampus.social.User;

import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.trentorise.smartcampus.permissionprovider.model.SocialEngineException;

/**
 * This class manages all the operations between ac-service-provider and the
 * social engine component
 * 
 * @author mirko perillo
 * 
 */
@Component
public class SocialEngineManager implements SocialEngine {

	@Value("${smartcampus.vas.web.socialengine.host}")
	private String socialEngineHost;
	@Value("${smartcampus.vas.web.socialengine.port}")
	private int socialEnginePort;

	private SCWebApiClient client = null;
	
	private static Boolean lock = true;
	
	private SCWebApiClient getClient() throws SocialEngineException {
		if (client == null) {
			client = SCWebApiClient.getInstance(Locale.ENGLISH, socialEngineHost, socialEnginePort);
			if (!client.ping())
				throw new SocialEngineException("Social engine not available");
		}
		return client;
	}
	
	/**
	 * creates a new social user in the social engine component.
	 * 
	 * @return the id of social user created
	 * @throws SocialEngineException
	 *             if social engine threw an internal exception
	 */
	public long createUser() throws SocialEngineException {
		long socialId = -1;
		EntityBase eb = null;
		Long entityBaseId = null;
		Long entityId = null;
		try {
			eb = new EntityBase();
			String id = generateId();
			eb.setLabel("SC_USER_" + id);
			entityBaseId = getClient().create(eb);
			// Re-read to get the ID of the default KB
			eb = getClient().readEntityBase(entityBaseId);

			EntityType person = getClient()
					.readEntityType("person", eb.getKbLabel());
			Entity entity = new Entity();
			entity.setEntityBase(eb);
			entity.setEtype(person);

			entityId = getClient().create(entity);

			User socialUser = new User();
			socialUser.setName("SC_" + id);
			socialUser.setEntityBaseId(entityBaseId);
			socialUser.setPersonEntityId(entityId);
			socialId = getClient().create(socialUser);

		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (eb != null) {
					getClient().deleteEntityBase(eb.getId());
				}

				if (entityId != null) {
					getClient().deleteEntity(entityId);
				}
			} catch (WebApiException e1) {
			}

			throw new SocialEngineException();
		}

		return socialId;
	}
	
	private String generateId() {
		synchronized (lock) {
			return UUID.randomUUID().toString();
		}
	}

	public static void main(String[] args) throws SocialEngineException {
		SocialEngineManager manager = new SocialEngineManager();
		manager.socialEngineHost = "sweb.smartcampuslab.it";
		manager.socialEnginePort = 8080;
		
		System.err.println(manager.createUser());
		System.err.println(manager.createUser());
		System.err.println(manager.createUser());
	}
}
