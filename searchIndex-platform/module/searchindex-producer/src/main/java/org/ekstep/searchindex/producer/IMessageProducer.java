package org.ekstep.searchindex.producer;

import java.util.List;
import java.util.Map;

public interface IMessageProducer {

	void sendMessage(List<Map<String, Object>> messages);
}
