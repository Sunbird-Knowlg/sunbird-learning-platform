package org.ekstep.kafka.util;

import java.util.Map;

public interface IMessage {

	public Map<String, Object> getMessage(String msgId);
	
	public void putRecentMessage(Map<String, Object> message);
	
	public void flush();
}
