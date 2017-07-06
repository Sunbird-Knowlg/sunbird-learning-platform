package com.ilimi.common.util;

public interface ILogger {

	public void log(String message, Object data);
	
	public void log(String message);
	
	public void log(String message, Object data, String logLevel);
	
	public void log(String message, Object data, Exception e);
	
	public void log(String message, Object data, Exception e, String logLevel);
}
