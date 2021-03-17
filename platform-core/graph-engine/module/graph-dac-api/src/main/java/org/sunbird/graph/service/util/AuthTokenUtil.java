package org.sunbird.graph.service.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.graph.service.common.DACConfigurationConstants;
import org.sunbird.graph.service.common.DACErrorCodeConstants;
import org.sunbird.graph.service.common.DACErrorMessageConstants;
import org.sunbird.telemetry.logger.TelemetryManager;
import org.neo4j.driver.v1.AuthToken;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.exceptions.ClientException;

public class AuthTokenUtil {

	

	public static AuthToken getAuthToken() {
		AuthToken authToken;

		// Fetching Authentication Type for Neo4J
		String authType = DACConfigurationConstants.NEO4J_SERVER_AUTH_TYPE;
		TelemetryManager.log("Neo4J Authentication Type: " + authType);

		if (!isValidConfiguration(authType))
			throw new ClientException(DACErrorCodeConstants.INVALID_CONFIG.name(),
					DACErrorMessageConstants.INVALID_CONFIGURATION + " | [Database Authentication Failed.]");

		switch (authType) {
		case "basic":
		case "BASIC":
			authToken = AuthTokens.basic(DACConfigurationConstants.NEO4J_SERVER_USERNAME,
					DACConfigurationConstants.NEO4J_SERVER_PASSWORD);
			break;

		case "basic_with_realm":
		case "BASIC_WITH_REALM":
			authToken = AuthTokens.basic(DACConfigurationConstants.NEO4J_SERVER_USERNAME,
					DACConfigurationConstants.NEO4J_SERVER_PASSWORD, DACConfigurationConstants.NEO4J_SERVER_AUTH_REALM);
			break;

		case "custom":
		case "CUSTOM":
			authToken = AuthTokens.custom(DACConfigurationConstants.NEO4J_SERVER_AUTH_PRINCIPAL,
					DACConfigurationConstants.NEO4J_SERVER_PASSWORD, DACConfigurationConstants.NEO4J_SERVER_AUTH_REALM,
					DACConfigurationConstants.NEO4J_SERVER_AUTH_SCHEME);
			break;

		case "custom_with_parameter":
		case "CUSTOM_WITH_PARAMETER":
			Map<String, Object> parameterMap = getParameterMap();
			authToken = AuthTokens.custom(DACConfigurationConstants.NEO4J_SERVER_AUTH_PRINCIPAL,
					DACConfigurationConstants.NEO4J_SERVER_PASSWORD, DACConfigurationConstants.NEO4J_SERVER_AUTH_REALM,
					DACConfigurationConstants.NEO4J_SERVER_AUTH_SCHEME, parameterMap);
			break;

		default:
			authToken = AuthTokens.none();
			break;
		}

		return authToken;
	}

	private static boolean isValidConfiguration(String authType) {
		boolean isValid = false;
		switch (authType) {
		case "basic":
		case "BASIC":
			if (isValidLoginCredentials())
				isValid = true;
			break;

		case "basic_with_realm":
		case "BASIC_WITH_REALM":
			if (isValidLoginCredentials() && isValidRealm())
				isValid = true;
			break;

		case "custom":
		case "CUSTOM":
			if (isValidLoginCredentials() && isValidRealm() && isValidPrincipal() && isValidScheme())
				isValid = true;
			break;

		case "custom_with_parameter":
		case "CUSTOM_WITH_PARAMETER":
			if (isValidLoginCredentials() && isValidRealm() && isValidPrincipal() && isValidScheme())
				isValid = true;
			break;

		default:
			// Since Default Authentication Type is None
			isValid = true;
			break;
		}
		return isValid;
	}

	private static boolean isValidLoginCredentials() {
		boolean isValid = false;

		if (StringUtils.isBlank(DACConfigurationConstants.NEO4J_SERVER_USERNAME))
			throw new ClientException(DACErrorCodeConstants.INVALID_CONFIG.name(),
					DACErrorMessageConstants.INVALID_USERNAME + " | [Database Authentication Failed.]");

		if (StringUtils.isBlank(DACConfigurationConstants.NEO4J_SERVER_PASSWORD))
			throw new ClientException(DACErrorCodeConstants.INVALID_CONFIG.name(),
					DACErrorMessageConstants.INVALID_PASSWORD + " | [Database Authentication Failed.]");

		// TODO: Write Extra Logic to validate the UserName and Password. Right
		// now its simply checking for 'null'. If the Entities are not null then
		// is Valid.

		isValid = true;

		return isValid;
	}

	private static boolean isValidPrincipal() {
		boolean isValid = false;

		if (StringUtils.isBlank(DACConfigurationConstants.NEO4J_SERVER_AUTH_PRINCIPAL))
			throw new ClientException(DACErrorCodeConstants.INVALID_CONFIG.name(),
					DACErrorMessageConstants.INVALID_PRINCIPAL + " | [Database Authentication Failed.]");

		// TODO: Write Extra Logic to validate the Principal. Right
		// now its simply checking for 'null'. If the Entities are not null then
		// is Valid.

		isValid = true;

		return isValid;
	}

	private static boolean isValidRealm() {
		boolean isValid = false;

		if (StringUtils.isBlank(DACConfigurationConstants.NEO4J_SERVER_AUTH_REALM))
			throw new ClientException(DACErrorCodeConstants.INVALID_CONFIG.name(),
					DACErrorMessageConstants.INVALID_REALM + " | [Database Authentication Failed.]");

		// TODO: Write Extra Logic to validate the Realm. Right
		// now its simply checking for 'null'. If the Entities are not null then
		// is Valid.

		isValid = true;

		return isValid;
	}
	
	private static boolean isValidScheme() {
		boolean isValid = false;

		if (StringUtils.isBlank(DACConfigurationConstants.NEO4J_SERVER_AUTH_SCHEME))
			throw new ClientException(DACErrorCodeConstants.INVALID_CONFIG.name(),
					DACErrorMessageConstants.INVALID_SCHEME + " | [Database Authentication Failed.]");

		// TODO: Write Extra Logic to validate the Realm. Right
		// now its simply checking for 'null'. If the Entities are not null then
		// is Valid.

		isValid = true;

		return isValid;
	}
	
	private static Map<String, Object> getParameterMap () {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		// TODO: Put the key Values in Map
		return parameterMap;
	}

}
