package org.ekstep.telemetry.handler;

public enum Level {
 INFO, WARN, DEBUG, ERROR, TRACE, FATAL;
	
	public static Level getLevel(String level) {
		return Level.valueOf(level);
	}
}

