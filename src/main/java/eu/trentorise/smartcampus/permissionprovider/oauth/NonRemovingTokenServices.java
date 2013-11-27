/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
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

package eu.trentorise.smartcampus.permissionprovider.oauth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author raman
 *
 */
@Transactional
public class NonRemovingTokenServices extends DefaultTokenServices {

	private ExtTokenStore localtokenStore;

	private Log logger = LogFactory.getLog(getClass());
	private static final Logger traceUserLogger = Logger.getLogger("traceUserToken");

	/** threshold for access token */
	protected int tokenThreshold = 60*60;
	
	/**
	 * Do not remove access token if expired
	 */
	@Override
	public OAuth2Authentication loadAuthentication(String accessTokenValue) throws AuthenticationException {
		OAuth2AccessToken accessToken = localtokenStore.readAccessToken(accessTokenValue);
		if (accessToken == null) {
			throw new InvalidTokenException("Invalid access token: " + accessTokenValue);
		}
		else if (accessToken.isExpired()) {
			logger.error("Accessing expired token: "+accessTokenValue);
			throw new InvalidTokenException("Access token expired: " + accessTokenValue);
		}

		OAuth2Authentication result = localtokenStore.readAuthentication(accessToken);
		return result;
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, isolation=Isolation.SERIALIZABLE)
	public OAuth2AccessToken refreshAccessToken(String refreshTokenValue, AuthorizationRequest request) throws AuthenticationException {
		return refreshWithRepeat(refreshTokenValue, request, false);
	}

	private OAuth2AccessToken refreshWithRepeat(String refreshTokenValue, AuthorizationRequest request, boolean repeat) {
		OAuth2AccessToken accessToken = localtokenStore.readAccessTokenForRefreshToken(refreshTokenValue);
		if (accessToken == null) {
			throw new InvalidGrantException("Invalid refresh token: " + refreshTokenValue);
		}

		int validity = getAccessTokenValiditySeconds(request);
		long created = accessToken.getExpiration().getTime() - validity*1000L;
		if (System.currentTimeMillis()-created < tokenThreshold*1000L ) {
			return accessToken;
		}

		try {
			OAuth2AccessToken res = super.refreshAccessToken(refreshTokenValue, request);
			OAuth2Authentication auth = localtokenStore.readAuthentication(res);
			traceUserLogger.info(String.format("'type':'refresh','user':'%s','token':'%s'", auth.getName(), res.getValue()));
			return res;
		} catch (RuntimeException e) {
			// do retry: it may be the case of race condition so retry the operation but only once
			if (!repeat) return refreshWithRepeat(refreshTokenValue, request, true);
			throw e;
		}
	}

	@Transactional(isolation=Isolation.SERIALIZABLE)
	public OAuth2AccessToken createAccessToken(OAuth2Authentication authentication) throws AuthenticationException {
		OAuth2AccessToken res = super.createAccessToken(authentication);
		traceUserLogger.info(String.format("'type':'new','user':'%s','token':'%s'", authentication.getName(), res.getValue()));
		return res;
	}

	@Override
	public void setTokenStore(TokenStore tokenStore) {
		super.setTokenStore(tokenStore);
		assert tokenStore instanceof ExtTokenStore;
		this.localtokenStore = (ExtTokenStore)tokenStore;
	}

	/**
	 * @param tokenThreshold the tokenThreshold to set
	 */
	public void setTokenThreshold(int tokenThreshold) {
		this.tokenThreshold = tokenThreshold;
	}
	
}
