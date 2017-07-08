package com.ilimi.common.util;


public class PlatformLogManager {

	private static ILogger logger = new PlatformLogger();
	
	public static ILogger getLogger(){
		return logger;
	}
}
