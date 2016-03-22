package org.ekstep.searchindex.producer;

import java.util.Map;

public interface IMessageProducer {

	public void init();
	
	public void pushMessage(Map<String, Object> message);
	
}
