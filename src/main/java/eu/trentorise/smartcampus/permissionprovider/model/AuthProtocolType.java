package eu.trentorise.smartcampus.permissionprovider.model;

public enum AuthProtocolType {
	OAUTH, CAS;

	public static AuthProtocolType AuthProtocolType(String protocol) {
		if (protocol.equalsIgnoreCase("OAUTH")) {
			return AuthProtocolType.OAUTH;
		} else if (protocol.equalsIgnoreCase("CAS")) {
			return AuthProtocolType.CAS;
		}
		return AuthProtocolType.CAS;

	}
}
