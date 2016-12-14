#####################################
SCENARIO (SP-initiated Single Logout)
#####################################

Agents: P1, P2, Browser(B), IdP(AAC)

1. The two clients(P1, P2) logged in one via CAS(P1) other client(P2) via Oauth.
	- clients specify logoutUrl.
	
2. Idp(AAC) save stateMap in the session <stateKey, logoutUrl>
	Note:[see line 81 CASController.java]
	[
	 - List of this objects[], type of protocol is needed. logout url list
	 - Map<Key,LogoutUrl>, check if exist , add, else create new one and add.
	 	- key is randUUID stateKey.
	 	- value <logout url>
	]
	
3. Idp(AAC) redirect to service/redirect.

4. P1 initiating logout request and returns it to the end-user's browsers.
	
	- P1 calls AAC /cas/singleLogout?redirectUrl=http://localhost:8084/home.  The IdP’s(AAC) SLO endpoint is appended with the LogoutRequest passing redirect url(homepage of client), which is a dedicated URL that expects to receive SLO messages.
		- as param it must specify redirectUrl (url to redirect after all logout completes)
		
	
6. Idp(AAC)
	- idp put redirect url in session.
	 determines the other SPs(list of logout) that support SLO to which the end-user received SSO during the current logon session. The IdP then iteratively does the following for each participating SP:
		- takes redirectURL and put it in session.
		- takes the list of logouts and call all of them by making redirect specify
			- as param (/cas/singleLogout/callback)
			- stateKey (associate each elements in logout list as stateKey)
		- Sps clear session and redirect to callback url passed to it as param.
		- idp is listening to response asynchronously in (/cas/singleLogout/callback) method.
			- it receives state id.
		- idp when receive stateId, understand that SP has cleaned the session.
			- it remove stateId from map and proceed with next one.
		- when map is empty, clear its own session
		- make a redirect to redirectUrl provided by client.
		