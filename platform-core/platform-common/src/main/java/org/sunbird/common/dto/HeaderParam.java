/*
 * Copyright (c) 2013-2014 Canopus Consulting. All rights reserved. 
 * 
 * This code is intellectual property of Canopus Consulting. The intellectual and technical 
 * concepts contained herein may be covered by patents, patents in process, and are protected 
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval 
 * from Canopus Consulting is prohibited.
 */
package org.sunbird.common.dto;

/**
 * The keys of the Execution Context Values.
 * 
 * @author Mahesh
 *
 */

public enum HeaderParam {

	REQUEST_ID, REQUEST_PATH, REQUEST_ST_ED_PATH, CURRENT_INVOCATION_PATH, USER_DATA, USER_LOCALE, SYSTEM_LOCALE, USER_ID, PROXY_USER_ID,
	USER_NAME, PROXY_USER_NAME, SCOPE_ID, CONSUMER_ID, CHANNEL_ID, APP_ID, DEVICE_ID;

	public String getParamName() {
		return this.name();
	}

}
