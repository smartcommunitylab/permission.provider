package eu.trentorise.smartcampus.permissionprovider.model;

import java.io.Serializable;

public class SingleSignoutData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** protocol type. **/
	private String authProtocolType;
	/** session identified (ticket | token). **/
	private String sessionIdentifier;
	/** client redirect. **/
	private String redirectUrl;

	public SingleSignoutData(String protocolType, String sessionIdentifier, String redirectUrl) {
		super();
		this.authProtocolType = protocolType;
		this.sessionIdentifier = sessionIdentifier;
		this.redirectUrl = redirectUrl;
	}

	public String getProtocolType() {
		return authProtocolType;
	}

	public void setProtocolType(String protocolType) {
		this.authProtocolType = protocolType;
	}

	public String getSessionIdentifier() {
		return sessionIdentifier;
	}

	public void setSessionIdentifier(String sessionIdentifier) {
		this.sessionIdentifier = sessionIdentifier;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}

	@Override
	public String toString() {
		return "SingleSignoutData [protocolType=" + authProtocolType + ", sessionIdentifier=" + sessionIdentifier
				+ ", redirectUrl=" + redirectUrl + "]";
	}

}
