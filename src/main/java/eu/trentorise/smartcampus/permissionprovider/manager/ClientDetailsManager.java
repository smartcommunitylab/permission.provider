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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import eu.trentorise.smartcampus.permissionprovider.Config.RESOURCE_VISIBILITY;
import eu.trentorise.smartcampus.permissionprovider.common.ResourceException;
import eu.trentorise.smartcampus.permissionprovider.common.Utils;
import eu.trentorise.smartcampus.permissionprovider.jaxbmodel.AuthorityMapping;
import eu.trentorise.smartcampus.permissionprovider.model.ClientAppBasic;
import eu.trentorise.smartcampus.permissionprovider.model.ClientAppInfo;
import eu.trentorise.smartcampus.permissionprovider.model.ClientDetailsEntity;
import eu.trentorise.smartcampus.permissionprovider.model.ClientModel;
import eu.trentorise.smartcampus.permissionprovider.model.Resource;
import eu.trentorise.smartcampus.permissionprovider.model.ResourceParameter;
import eu.trentorise.smartcampus.permissionprovider.model.ServiceDescriptor;
import eu.trentorise.smartcampus.permissionprovider.model.ServiceParameterModel;
import eu.trentorise.smartcampus.permissionprovider.repository.ClientDetailsRepository;
import eu.trentorise.smartcampus.permissionprovider.repository.ResourceParameterRepository;
import eu.trentorise.smartcampus.permissionprovider.repository.ResourceRepository;
import eu.trentorise.smartcampus.permissionprovider.repository.ServiceRepository;

/**
 * Support for the management of client app registration details
 * @author raman
 *
 */
@Component
@Transactional
public class ClientDetailsManager {

	/** GRANT TYPE: CLIENT CRIDENTIALS FLOW */
	public static final String GT_CLIENT_CREDENTIALS = "client_credentials";
	/** GRANT TYPE: IMPLICIT FLOW */
	public static final String GT_IMPLICIT = "implicit";
	/** GRANT TYPE: AUTHORIZATION GRANT FLOW */
	public static final String GT_AUTHORIZATION_CODE = "authorization_code";
	/** GRANT TYPE: REFRESH TOKEN */
	public static final String GT_REFRESH_TOKEN = "refresh_token";
	private Log log = LogFactory.getLog(getClass());

	@Autowired
	private ClientDetailsRepository clientDetailsRepository;
	@Autowired
	private ResourceRepository resourceRepository;
	@Autowired
	private ResourceManager resourceManager;
	@Autowired
	private ServiceRepository serviceRepository;
	@Autowired
	private AttributesAdapter attributesAdapter;
	@Autowired
	private ResourceParameterRepository resourceParameterRepository;
	/**
	 * Generate new value to be used as clientId (String)
	 * @return
	 */
	public synchronized String generateClientId() {
		return UUID.randomUUID().toString();
	}
	/**
	 * Generate new value to be used as client secret (String)
	 * @return
	 */
	public synchronized String generateClientSecret() {
		return UUID.randomUUID().toString();
	}
	/**
	 * Convert DB objects to the simplified client representation
	 * @param entities
	 * @return
	 */
	private List<ClientAppBasic> convertToClientApps(List<ClientDetailsEntity> entities){
		if (entities == null) {
			return Collections.emptyList();
		}
		List<ClientAppBasic> res = new ArrayList<ClientAppBasic>();
		for (ClientDetailsEntity e : entities) {
			res.add(convertToClientApp(e));
		}
		return res;
	}

	/**
	 * Convert DB object to the simplified client representation
	 * @param e
	 * @return
	 */
	private ClientAppBasic convertToClientApp(ClientDetailsEntity e) {
		ClientAppBasic res = new ClientAppBasic();
		res.setClientId(e.getClientId());
		res.setClientSecret(e.getClientSecret());
		res.setClientSecretMobile(e.getClientSecretMobile());
		res.setGrantedTypes(e.getAuthorizedGrantTypes());
		res.setSloUrl(e.getSloUrl());
		
		// approval status
		res.setIdentityProviderApproval(new HashMap<String, Boolean>());
		// request status
		res.setIdentityProviders(new HashMap<String, Boolean>());
		for (String key : attributesAdapter.getAuthorityUrls().keySet()) {
			res.getIdentityProviders().put(key, false);
		}
		
		
		ClientAppInfo info = ClientAppInfo.convert(e.getAdditionalInformation());
		if (info != null) {
			res.setName(info.getName());
			res.setNativeAppsAccess(info.isNativeAppsAccess());
			res.setNativeAppSignatures(info.getNativeAppSignatures());
			if (info.getIdentityProviders() != null) {
				for (String key : info.getIdentityProviders().keySet()) {
					switch (info.getIdentityProviders().get(key)) {
					case ClientAppInfo.APPROVED:
						res.getIdentityProviderApproval().put(key, true);
						res.getIdentityProviders().put(key, true);
						break;
					case ClientAppInfo.REJECTED:
						res.getIdentityProviderApproval().put(key, false);
						res.getIdentityProviders().put(key, true);
						break;
					case ClientAppInfo.REQUESTED:
						res.getIdentityProviders().put(key, true);
						break;
					default:
						break;
					}
				}
			}
		}
		// access server-side corresponds to the 'authorization grant' flow.
		res.setServerSideAccess(e.getAuthorizedGrantTypes().contains(GT_AUTHORIZATION_CODE));
		// browser access corresponds to the 'implicit' flow.
		res.setBrowserAccess(e.getAuthorizedGrantTypes().contains(GT_IMPLICIT));

		res.setRedirectUris(StringUtils.collectionToCommaDelimitedString(e.getRegisteredRedirectUri()));
		return res;
	}
	/**
	 * Client types to be associated with client app by default
	 * @return
	 */
	public String defaultGrantTypes() {
		return GT_CLIENT_CREDENTIALS;
	}
	/**
	 * Client authorities to be associated with client app by default
	 * @return
	 */
	public String defaultAuthorities() {
		return "ROLE_CLIENT";
	}
	/**
	 * Fill in the DB object with the properties of {@link ClientAppBasic} instance. In case of problem, return null.
	 * @param client
	 * @param data
	 * @return
	 * @throws Exception 
	 */
	public ClientDetailsEntity convertFromClientApp(ClientDetailsEntity client, ClientAppBasic data) {
		try {
			ClientAppInfo info = null;
			if (client.getAdditionalInformation() == null) {
				info = new ClientAppInfo();
			} else {
				info = ClientAppInfo.convert(client.getAdditionalInformation());
			}
			info.setName(data.getName());
			info.setNativeAppsAccess(data.isNativeAppsAccess());
			info.setNativeAppSignatures(Utils.normalizeValues(data.getNativeAppSignatures()));
			Set<String> types = new HashSet<String>(client.getAuthorizedGrantTypes());
			if (data.isBrowserAccess()) {
				types.add(GT_IMPLICIT);
			} else {
				types.remove(GT_IMPLICIT);
			} 
			if (data.isServerSideAccess() || data.isNativeAppsAccess()) {
				types.add(GT_AUTHORIZATION_CODE);
				types.add(GT_REFRESH_TOKEN);
			} else {
				types.remove(GT_AUTHORIZATION_CODE);
				types.remove(GT_REFRESH_TOKEN);
			}
			client.setAuthorizedGrantTypes(StringUtils.collectionToCommaDelimitedString(types));
			if (info.getIdentityProviders() == null) {
				info.setIdentityProviders(new HashMap<String, Integer>());
			}
			
			for (String key : attributesAdapter.getAuthorityUrls().keySet()) {
				if (data.getIdentityProviders().containsKey(key) && data.getIdentityProviders().get(key)) {
					Integer value = info.getIdentityProviders().get(key);
					AuthorityMapping a = attributesAdapter.getAuthority(key);
					if (value == null || value == ClientAppInfo.UNKNOWN) {
						info.getIdentityProviders().put(key, a.isPublic() ? ClientAppInfo.APPROVED : ClientAppInfo.REQUESTED);
					}
				} else {
					info.getIdentityProviders().remove(key);
				}
			}
			
			client.setAdditionalInformation(info.toJson());
			client.setRedirectUri(Utils.normalizeValues(data.getRedirectUris()));
			client.setSloUrl(data.getSloUrl());
		} catch (Exception e) {
			log .error("failed to convert an object: "+e.getMessage(), e);
			return null;
		}
		return client;
	}
	/**
	 * Validate correctness of the data specified for the app
	 * @param client
	 * @param data
	 */
	public String validate(ClientDetailsEntity client, ClientAppBasic data) {
		if (client == null) return "app not found";
		// name should not be empty
		if (data.getName() == null || data.getName().trim().isEmpty()) {
			return "name cannot be empty";
		}
		// for server-side or native access redirect URLs are required
		if ((data.isServerSideAccess() || data.isNativeAppsAccess()) && (data.getRedirectUris() == null || data.getRedirectUris().trim().isEmpty())) {
			return "redirect URL is required for Server-side or native access";
		}
//		if (data.isNativeAppsAccess() && (data.getNativeAppSignatures() == null || data.getNativeAppSignatures().isEmpty())) {
//			return "app signature is required for native access";
//		}
		return null;
	}
	/**
	 * Reset clientSecretMobile
	 * @param clientId
	 * @return updated {@link ClientDetailsEntity} instance
	 */
	public ClientAppBasic resetClientSecretMobile(String clientId) {
		return convertToClientApp(resetClientData(clientId, true));
	}
	/**
	 * Reset client secret
	 * @param clientId
	 * @return updated {@link ClientDetailsEntity} instance
	 */
	public ClientAppBasic resetClientSecret(String clientId) {
		return convertToClientApp(resetClientData(clientId, false));
	}
	
	public ClientDetailsEntity resetClientData(String clientId, boolean resetClientSecretMobile) {
		ClientDetailsEntity client = clientDetailsRepository.findByClientId(clientId);
		if (client == null) {
			throw new IllegalArgumentException("client app not found");
		}
		if (resetClientSecretMobile) {
			client.setClientSecretMobile(generateClientSecret());
		} else {
			client.setClientSecret(generateClientSecret());
		}
		clientDetailsRepository.save(client);
		return client;
	}
	/**
	 * Return the set of identity providers allowed for the client
	 * @param clientId
	 * @return set of identity providers IDs (authorities)
	 */
	public Set<String> getIdentityProviders(String clientId) {
		ClientDetailsEntity entity = clientDetailsRepository.findByClientId(clientId);
		if (entity == null) throw new IllegalArgumentException("client not found");
		ClientAppInfo info = ClientAppInfo.convert(entity.getAdditionalInformation());
		Set<String> res = new HashSet<String>();
		if (info.getIdentityProviders() != null) {
			for (String s : info.getIdentityProviders().keySet()) {
				if (ClientAppInfo.APPROVED == info.getIdentityProviders().get(s)) {
					res.add(s);
				}
			}
		}
		return res;
	}
	/**
	 * @param userId
	 * @return {@link List} of {@link ClientAppBasic} objects representing client apps
	 */
	public List<ClientAppBasic> getByDeveloperId(Long userId) {
		return convertToClientApps(clientDetailsRepository.findByDeveloperId(userId));
	}
	/**
	 * Create new Client from {@link ClientAppBasic} descriptor
	 * @param appData
	 * @param userId
	 * @return {@link ClientAppBasic} descriptor of the created Client
	 * @throws Exception 
	 */
	public ClientAppBasic create(ClientAppBasic appData, Long userId) {
		ClientDetailsEntity entity = new ClientDetailsEntity();
		ClientAppInfo info = new ClientAppInfo();
		if (!StringUtils.hasText(appData.getName())) {
			throw new IllegalArgumentException("An app name cannot be empty");
		}
		info.setName(appData.getName());
		for (ClientDetailsEntity cde : clientDetailsRepository.findAll()) {
			if (ClientAppInfo.convert(cde.getAdditionalInformation()).getName().equals(appData.getName())) {
				throw new IllegalArgumentException("An app with the same name already exists");
			}
		}
		try {
			entity.setAdditionalInformation(info.toJson());
		} catch (Exception e) {
			throw new ResourceException(e.getMessage());
		}
		entity.setClientId(generateClientId());
		entity.setAuthorities(defaultAuthorities());
		entity.setAuthorizedGrantTypes(defaultGrantTypes());
		entity.setDeveloperId(userId);
		entity.setClientSecret(generateClientSecret());
		entity.setClientSecretMobile(generateClientSecret());

		entity = clientDetailsRepository.save(entity);
		return convertToClientApp(entity);
		
	}
	/**
	 * delete the specified client
	 * @param clientId
	 * @return {@link ClientAppBasic} descriptor of the deleted client
	 */
	public ClientAppBasic delete(String clientId) {
		ClientDetailsEntity client = clientDetailsRepository.findByClientId(clientId);
		if (client == null) {
			throw new EntityNotFoundException("client app not found");
		}
		clientDetailsRepository.delete(client);
		return convertToClientApp(client); 
	}
	/**
	 * Update client info
	 * @param clientId
	 * @param data
	 * @return 
	 */
	public ClientAppBasic update(String clientId, ClientAppBasic data) {
		ClientDetailsEntity client = clientDetailsRepository.findByClientId(clientId);
		String error = null;
		if  ((error = validate(client,data)) != null) {
			throw new IllegalArgumentException(error);
		}
		client = convertFromClientApp(client,data);
		if (client != null) {
			clientDetailsRepository.save(client);
			return convertToClientApp(client);
		} else {
			log.error("Problem converting the client");
			throw new IllegalArgumentException("internal error");
		}
		
	}
	
	public ClientModel createNewFromModel(ClientModel model, Long userId)  {
		ClientAppBasic data = new ClientAppBasic();
		data.setName(model.getName());
		data = create(data, userId);
		model2basic(model, data);

		return fullUpdate(model, userId, data);
	}
	public ClientModel updateFromModel(ClientModel model, Long userId) {
		ClientAppBasic data = get(model.getClientId());
		model2basic(model, data);
		return fullUpdate(model, userId, data);
	}
	
	private ClientModel fullUpdate(ClientModel model, Long userId, ClientAppBasic data) throws ResourceException {
		update(data.getClientId(), data);
		ClientDetailsEntity client = clientDetailsRepository.findByClientId(data.getClientId());
		ClientAppInfo info = ClientAppInfo.convert(client.getAdditionalInformation());
		if (model.getOwnParameters() != null) {
			for (ServiceParameterModel spm : model.getOwnParameters()) {
				ResourceParameter param = new ResourceParameter();
				ServiceDescriptor sd = serviceRepository.findByServiceName(spm.getService());
				param.setService(sd);
				param.setClientId(client.getClientId());
				param.setParameter(spm.getName());
				param.setVisibility(spm.getVisibility());
				param.setValue(spm.getValue());
				ResourceParameter old = resourceParameterRepository.findByServiceAndParameterAndValue(sd, param.getParameter(), param.getValue());
				if (old == null) {
					resourceManager.storeResourceParameter(param, sd.getServiceId());
				}
			}
		}
		
		if (model.getScopes() != null) {
			Set<String> scopes = new HashSet<String>();
			Set<String> resourceIds = new HashSet<String>();
			
			for (String s : model.getScopes()) {
				Resource r = resourceRepository.findByResourceUri(s);
				if (r != null) {
					// if not the smae client id resouce (e.g., public or defined by another client id)
					if (!client.getClientId().equals(r.getClientId())) {
						// should be the same client
						if (r.getVisibility().equals(RESOURCE_VISIBILITY.CLIENT_APP)) {
							throw new ResourceException("Unauthorized resource use: same client only access allowed");
						}
						// should be the same developer
						else if (r.getVisibility().equals(RESOURCE_VISIBILITY.DEVELOPER)) {
							ClientDetailsEntity ownerClient = clientDetailsRepository.findByClientId(r.getClientId());
							Long owner = ownerClient.getDeveloperId();
							if (!owner.equals(userId)) {
								throw new ResourceException("Unauthorized resource use: same user only access allowed");
							}
							scopes.add(s);
							resourceIds.add(r.getResourceId().toString());
							// public: check possibility to request and approval
						} else {
							if (!r.isAccessibleByOthers()) {
								throw new ResourceException("Unauthorized resource use: not accessible by other");
							}
							if (r.isApprovalRequired()) {
								if (!client.getResourceIds().contains(r.getResourceId().toString()) && ! info.getResourceApprovals().containsKey(r)) {
									info.getResourceApprovals().put(r.getResourceId().toString(), true);
								}
							} else {
								resourceIds.add(r.getResourceId().toString());
								scopes.add(s);
							}
						}
					} else {
						resourceIds.add(r.getResourceId().toString());
						scopes.add(s);
					}
				}
			}
			client.setScope(StringUtils.collectionToCommaDelimitedString(scopes));
			client.setResourceIds(StringUtils.collectionToCommaDelimitedString(resourceIds));
			model.setScopes(scopes);
		}
		model.setClientId(client.getClientId());
		model.setClientSecret(client.getClientSecret());
		model.setClientSecretMobile(client.getClientSecretMobile());
		
		return model;
	}
	private void model2basic(ClientModel model, ClientAppBasic data) {
		data.setName(model.getName());
		data.setBrowserAccess(model.isBrowserAccess());
		data.setNativeAppsAccess(model.isNativeAppsAccess());
		data.setServerSideAccess(model.isServerSideAccess());
		data.setGrantedTypes(model.getGrantedTypes());
		data.setRedirectUris(StringUtils.collectionToCommaDelimitedString(model.getRedirectUris()));
		data.setNativeAppSignatures(model.getNativeAppSignatures());
		data.setIdentityProviders(new HashMap<String, Boolean>());
		for (String provider : model.getIdentityProviders()) {
			data.getIdentityProviders().put(provider, true);
		}
	}
	
	public ClientAppBasic get(String clientId) {
		return convertToClientApp(clientDetailsRepository.findByClientId(clientId));
	}
}
