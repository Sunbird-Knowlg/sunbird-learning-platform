package org.ekstep.graph.service.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.DACConfigurationConstants;

import com.ilimi.graph.common.mgr.Configuration;

public class PassportUtil {

	private static Logger LOGGER = LogManager.getLogger(PassportUtil.class.getName());

	public static boolean isValidPassportKey(String passportKey) {
		LOGGER.debug("Given Passport Key: ", passportKey);
		boolean isValidPassportKey = false;

		// Read the 'graph.passport.key.base' in Graph Properties
		String graphPassportKeyBase = Configuration.getProperty(DACConfigurationConstants.PASSPORT_KEY_BASE_PROPERTY);
		LOGGER.debug("Passport Key Base: " + graphPassportKeyBase);
		LOGGER.debug("G_P_K: " + passportKey);

		String expectedPassportKey = graphPassportKeyBase;
		LOGGER.debug("E_P_K: " + expectedPassportKey);

		// Encryption(Platform Passport. Key) = Raw(Analytics Passport);
		if (StringUtils.isNotBlank(passportKey) && StringUtils.equals(passportKey, expectedPassportKey))
			isValidPassportKey = true;
		
		LOGGER.debug("Is Valid Passport Key ? " + isValidPassportKey);
		return isValidPassportKey;
	}

}
