package org.ekstep.graph.service.util;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.graph.service.common.DACConfigurationConstants;

import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.graph.common.mgr.Configuration;

public class PassportUtil {


	public static boolean isValidPassportKey(String passportKey) {
		PlatformLogger.log("Given Passport Key: " + passportKey);
		boolean isValidPassportKey = false;

		// Read the 'graph.passport.key.base' in Graph Properties
		String graphPassportKeyBase = Configuration.getProperty(DACConfigurationConstants.PASSPORT_KEY_BASE_PROPERTY);
		PlatformLogger.log("Passport Key Base: " + graphPassportKeyBase);
		PlatformLogger.log("G_P_K: " + passportKey);

		String expectedPassportKey = graphPassportKeyBase;
		PlatformLogger.log("E_P_K: " + expectedPassportKey);

		// Encryption(Platform Passport. Key) = Raw(Analytics Passport);
		if (StringUtils.isNotBlank(passportKey) && StringUtils.equals(passportKey, expectedPassportKey))
			isValidPassportKey = true;
		
		PlatformLogger.log("Is Valid Passport Key ? " + isValidPassportKey);
		return isValidPassportKey;
	}

}
