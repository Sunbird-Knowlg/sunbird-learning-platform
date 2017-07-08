package com.ilimi.common.util;

public interface ILogger {

	public void log(String message,String className, String method, Object data);
	
	public void log(String message,String className, String method);
	
	public void log(String message,String className, String method, Object data, String logLevel);
	
	public void log(String message,String className, String method, Object data, Exception e);
	
	public void log(String message,String className, String method, Object data, Exception e, String logLevel);
}
