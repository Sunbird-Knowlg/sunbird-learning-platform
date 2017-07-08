/*
 * Copyright (c) 2013-2014 Canopus Consutling. All rights reserved. 
 * 
 * This code is intellectual property of Canopus Consutling. The intellectual and technical 
 * concepts contained herein may be covered by patents, patents in process, and are protected 
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval 
 * from Canopus Consutling is prohibited.
 */
package com.ilimi.util;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogManager;

/**
 * @author ravitejagarlapati
 *
 */

public class ApplicationContextUtils implements ApplicationContextAware {

	private static ILogger logger = PlatformLogManager.getLogger();
	private static ApplicationContext ctx;
	private static ApplicationContextUtils applicationContextUtils; 
	private static Map<String, Object> globalObjects = new HashMap<String, Object>();
		
	private Properties appProperties;
	
	@SuppressWarnings("unused")
	private static Properties loadProperties(final String propertiesFile)
			 {
		// Properties object we are going to fill up.
		Properties properties = new Properties();
		try {
			// If file exists as an absolute path, load as input stream.
			final Path path = Paths.get(propertiesFile);
			if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
				properties.load(new FileInputStream(propertiesFile));
			} else {
				// Otherwise, use resource as stream.
				properties.load(ApplicationContextUtils.class.getClassLoader()
						.getResourceAsStream(propertiesFile));
			}
		} catch (Exception e) {

		}
		return properties;
	}

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		ctx = applicationContext;
		applicationContextUtils = this;
	}

	public static ApplicationContext getApplicationContext() {
	    return ctx;
	}

	public static Properties getAppProperties() {
		if (applicationContextUtils.appProperties == null) {
			logger.log("loading app.properties at ApplicationContextUtils...");
			ApplicationContext appCtx = ApplicationContextUtils.getApplicationContext();
			Properties props = (Properties) appCtx.getBean("appProperties");
			applicationContextUtils.appProperties = props;
		}
		return applicationContextUtils.appProperties;
	}

	public void setAppProperties(Properties appProperties) {
		this.appProperties = appProperties;
	}

	public static Map<String, Object> getGlobalObjects() {
		return globalObjects;
	}
}
