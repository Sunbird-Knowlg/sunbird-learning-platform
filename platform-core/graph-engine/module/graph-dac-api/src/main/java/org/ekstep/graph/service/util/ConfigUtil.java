package org.ekstep.graph.service.util;

import java.io.File;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.DACConfigurationConstants;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Config.ConfigBuilder;
import org.neo4j.driver.v1.Config.EncryptionLevel;
import org.neo4j.driver.v1.Config.TrustStrategy;

public class ConfigUtil {

	private static Logger LOGGER = LogManager.getLogger(ConfigUtil.class.getName());

	public static Config getConfig() {
		ConfigBuilder config = Config.build();
		
		LOGGER.info("Fetching the Configuration for Neo4J Bolt.");

		if (BooleanUtils.isTrue(DACConfigurationConstants.IS_NEO4J_SERVER_CONNECTION_ENCRYPTION_ALLOWED))
			config.withEncryptionLevel(EncryptionLevel.NONE);

		if (BooleanUtils.isTrue(DACConfigurationConstants.IS_SETTING_NEO4J_SERVER_MAX_IDLE_SESSION_ENABLED))
			config.withMaxIdleSessions(DACConfigurationConstants.NEO4J_SERVER_MAX_IDLE_SESSION);

		if (BooleanUtils.isTrue(DACConfigurationConstants.IS_SETTING_NEO4J_SERVER_MAX_IDLE_SESSION_ENABLED))
			config.withTrustStrategy(getTrustStrategy());
		
		LOGGER.info("Returning Database Config: " + config.toConfig());
		return config.toConfig();
	}

	private static TrustStrategy getTrustStrategy() {
		TrustStrategy trustStrategy = TrustStrategy.trustAllCertificates();

		String strategy = DACConfigurationConstants.NEO4J_SERVER_CONNECTION_TRUST_STRATEGY;
		LOGGER.info("Trust Strategy: " + strategy);

		switch (strategy) {
		case "all":
		case "ALL":
			// Trust All Certificate
			trustStrategy = TrustStrategy.trustAllCertificates();
			break;

		case "custom":
		case "CUSTOM":
			// Trust Custom Certificate Signed By
			trustStrategy = TrustStrategy.trustCustomCertificateSignedBy(
					new File(DACConfigurationConstants.NEO4J_SERVER_CONNECTION_TRUST_STRATEGY_CERTIFICATE_FILE));
			break;

		case "system":
		case "SYSTEM":
			// Trust System Certificate
			trustStrategy = TrustStrategy.trustAllCertificates();
			break;

		default:
			LOGGER.info("Invalid trust Strategy");
			break;
		}

		return trustStrategy;
	}

}
