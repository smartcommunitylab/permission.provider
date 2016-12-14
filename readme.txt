#######################################
SCENARIO 1 (SP-initiated Single Logout)
#######################################

Agents: P1, P2, Browser(B), IdP(AAC)

1.The two clients(P1, P2) logged in one via CAS(P1) other client(P2) via Oauth.
	- clients specify logoutUrl.
	
2. Idp(AAC) save in the session the following
	- type of protocol (CAS, OAuth etc)
	- logoutUrl
	
	
3. Idp(AAC) redirect to service/redirect.

4. P1 initiating logout request and returns it to the end-user's browsers.
	- validate the request to Idp(AAC) ?
	- P1 calls AAC /singlelogout?redirectUrl=p1.home.page  The IdP’s(AAC) SLO endpoint is appended with the LogoutRequest passing redirect url(homepage of client), which is a dedicated URL that expects to receive SLO messages.

	
6. Idp(AAC)
	- idp put redirect url in session.
	 determines the other SPs(list of logout) that support SLO to which the end-user received SSO during the current logon session. The IdP then iteratively does the following for each participating SP:
		- Generates a new, digitally signed LogoutRequest.
		- Redirect user’s browser to that Clients’s SLO endpoint providing them callback address to send response.
		- Listen for response in callback.
			- receive ok from client, remove it from session map.
			- call next.
			- when all map is empty.
			- terminate its own session
			- make a redirect to the redirectUrl provided by client.
				
7. Client(P1,P2) terminates its own logon session for the end user after receiving and validating the LogoutRequest from the IdP(AAC).
	
	
8. The IdP(AAC)
	- terminates its own logon session
    - sends a final LogoutResponse message that to the initiating SP. This matches the original LogoutRequest it sent it step #1.
    - The response includes a flag telling the originating SP whether SAML Single Logout was either fully  or partially completed.
	