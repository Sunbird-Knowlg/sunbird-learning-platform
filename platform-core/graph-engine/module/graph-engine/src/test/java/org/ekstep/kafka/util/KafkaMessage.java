package org.ekstep.kafka.util;

import java.util.HashMap;
import java.util.Map;

public class KafkaMessage implements IMessage{

	private Map<String, Map<String, Object>> messages = new HashMap<>(); 
	
	@Override
	public Map<String, Object> getMessage(String msgId) {
		return (Map<String, Object>) messages.get(msgId);
	}

	@Override
	public void putRecentMessage(Map<String, Object> message) {
		this.messages.put((String) message.get("nodeUniqueId"), message);
	}
	
	public void flush(){
		messages = new HashMap<>();
	}
	
}
