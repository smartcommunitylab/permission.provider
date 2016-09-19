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
package eu.trentorise.smartcampus.permissionprovider.model;

import java.util.HashSet;
import java.util.Set;

import eu.trentorise.smartcampus.permissionprovider.manager.ClientDetailsManager;

/**
 * @author raman
 *
 */
public class ClientModel {

	private String clientId;
	private String clientSecret;
	private String clientSecretMobile;
	private String name;
	private Set<String> redirectUris;
	private Set<String> grantedTypes;

	private boolean nativeAppsAccess;
	private String nativeAppSignatures;
	private boolean browserAccess;
	private boolean serverSideAccess;

	private Set<String> identityProviders;

	private Set<String> scopes;
	
	private Set<ServiceParameterModel> ownParameters;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getClientSecretMobile() {
		return clientSecretMobile;
	}

	public void setClientSecretMobile(String clientSecretMobile) {
		this.clientSecretMobile = clientSecretMobile;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<String> getRedirectUris() {
		return redirectUris;
	}

	public void setRedirectUris(Set<String> redirectUris) {
		this.redirectUris = redirectUris;
	}

	public Set<String> getGrantedTypes() {
		return grantedTypes;
	}

	public void setGrantedTypes(Set<String> grantedTypes) {
		this.grantedTypes = grantedTypes;
	}

	public boolean isNativeAppsAccess() {
		return nativeAppsAccess;
	}

	public void setNativeAppsAccess(boolean nativeAppsAccess) {
		this.nativeAppsAccess = nativeAppsAccess;
	}

	public String getNativeAppSignatures() {
		return nativeAppSignatures;
	}

	public void setNativeAppSignatures(String nativeAppSignatures) {
		this.nativeAppSignatures = nativeAppSignatures;
	}

	public boolean isBrowserAccess() {
		return browserAccess;
	}

	public void setBrowserAccess(boolean browserAccess) {
		this.browserAccess = browserAccess;
	}

	public boolean isServerSideAccess() {
		return serverSideAccess;
	}

	public void setServerSideAccess(boolean serverSideAccess) {
		this.serverSideAccess = serverSideAccess;
	}

	public Set<String> getIdentityProviders() {
		return identityProviders;
	}

	public void setIdentityProviders(Set<String> identityProviders) {
		this.identityProviders = identityProviders;
	}

	public Set<String> getScopes() {
		return scopes;
	}

	public void setScopes(Set<String> scopes) {
		this.scopes = scopes;
	}

	public Set<ServiceParameterModel> getOwnParameters() {
		return ownParameters;
	}

	public void setOwnParameters(Set<ServiceParameterModel> ownParameters) {
		this.ownParameters = ownParameters;
	}

	/**
	 * @param client
	 * @return
	 */
	public static ClientModel fromClient(ClientDetailsEntity client) {
		ClientModel model = new ClientModel();
		model.setClientId(client.getClientId());
		model.setClientSecret(client.getClientSecret());
		model.setClientSecretMobile(client.getClientSecretMobile());
		model.setGrantedTypes(client.getAuthorizedGrantTypes());

		// approval status
		model.setIdentityProviders(new HashSet<String>());
		
		
		ClientAppInfo info = ClientAppInfo.convert(client.getAdditionalInformation());
		if (info != null) {
			model.setName(info.getName());
			model.setNativeAppsAccess(info.isNativeAppsAccess());
			model.setNativeAppSignatures(info.getNativeAppSignatures());
			if (info.getIdentityProviders() != null) {
				for (String key : info.getIdentityProviders().keySet()) {
					if (ClientAppInfo.APPROVED == info.getIdentityProviders().get(key)) {
						model.getIdentityProviders().add(key);
					}
				}
			}
		}
		// access server-side corresponds to the 'authorization grant' flow.
		model.setServerSideAccess(client.getAuthorizedGrantTypes().contains(ClientDetailsManager.GT_AUTHORIZATION_CODE));
		// browser access corresponds to the 'implicit' flow.
		model.setBrowserAccess(client.getAuthorizedGrantTypes().contains(ClientDetailsManager.GT_IMPLICIT));

		model.setRedirectUris(client.getRegisteredRedirectUri());
		
		model.setScopes(client.getScope());
		
		return model;
	}
	
	
}
