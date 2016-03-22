package org.ekstep.searchindex.processor;

import java.util.Map;

public interface IMessageProcessor {

	public void processMessage(String messageData);
	
	public void processMessage(Map<String, Object> message) throws Exception;
}
