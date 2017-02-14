#####################################
SCENARIO (SP-initiated Single Logout)
#####################################

Agents: P1, P2, Browser(B), IdP(AAC)
LOGIN
=====================
1. The two clients(P1, P2) logged in one via CAS(P1) other client(P2) via Oauth.
	- for CAS: we refer to 'service' parameter value to be used as client logout url.
	- for OAuth2: the client logout url will be specified in dev console
	
2. Idp(AAC) save stateMap in the session <relayState, SingleSignOutData>
	Note:[see line 81 CASController.java]
	[
	 - List of this objects[], type of protocol is needed. logout url list
	 - Map<Key,SingleSignOutData>, check if exist , add, else create new one and add.
	 	- key is randUUID RelayState.
	 	- value <SingleSignOutData> 
	 		* SingleSignOutData structure contains three fields
	 			|- authProtocolType [CAS|OAUTH]
	 			|- sessionIdentifier [TICKET|TOKEN]
	 			`- redirectUrl [service|singlelogoutUrl specified using dev console]
	]
	
3. Idp(AAC) redirect to service/redirect.
=====================
LOGOUT
=====================
4. P1 initiating logout request and returns it to the end-user's browsers.
	
	- P1 calls AAC /cas/logout?service=http://localhost:8084/home.  The IdP’s(AAC) SLO endpoint is appended with the LogoutRequest passing redirect url(service url of client), which is a dedicated URL that expects to receive SLO messages.
		- as param it must specify 'service' (url to redirect after all logout completes)
		
	
6. Idp(AAC)
	- idp put redirect url in session as final url to be redirect after underlying handshakes.
	 determines the other SPs(list of SingleSignOutData) that support SLO to which the end-user received SSO during the current logon session. The IdP then iteratively does the following for each participating SP:
		- takes the list of SingleSignOutData and call all of them by making redirect to the redirect value (present as field inside SingleSignOutData structure)
			- RelayState (associate each elements in logout list as RelayState)
			- SAMLRequest=<SAML signature>
		- Sps clear session and redirect a per protocol to AAC /cas/logout with RelayState parameter passed before.
		- idp is listening to response asynchronously in (/cas/logout) method.
			- it receives RelayState, understands that SP has cleaned the session.
			- it removes RelayState from map and proceed with next one.
		- when map is empty, clear its own session
		- make a redirect to redirect url stored in the session.
		