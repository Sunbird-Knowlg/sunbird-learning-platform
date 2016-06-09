package org.ekstep.searchindex.producer;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.searchindex.util.PropertiesUtil;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

public class KafkaMessageProducer implements IMessageProducer {
	private String TOPIC;
	private ProducerConfig producerConfig;
	private Producer<String, String> producer;
	private ObjectMapper mapper = new ObjectMapper();
	List<Map<String, Object>> messages = null;

	private static final Logger transactionMsgLogger = LogManager.getLogger("TransactionMessageLogger");

	public KafkaMessageProducer(List<Map<String, Object>> messages) {
		init(messages);
	}

	private void init(List<Map<String, Object>> messages) {
		TOPIC = PropertiesUtil.getProperty("topic");
		Properties properties = new Properties();
		properties.put("metadata.broker.list", PropertiesUtil.getProperty("metadata.broker.list"));
		properties.put("serializer.class", PropertiesUtil.getProperty("serializer.class"));
		properties.put("partitioner.class", "org.ekstep.searchindex.producer.MessagePartitioner");
		producerConfig = new ProducerConfig(properties);
		this.messages = messages;
	}

	@Override
	public void run() {
		if (null != messages && !messages.isEmpty()) {
			for (Map<String, Object> message : messages) {
				pushMessage(message);
			}
		}
	}

	private void pushMessage(Map<String, Object> message) {
		String jsonMessage = "";
		try {
			if (message != null && message.get("objectType") != null) {
				String objectType = (String) message.get("objectType");
				if (StringUtils.isBlank(objectType)) {
					objectType = (String) message.get("nodeType");
					if (StringUtils.isBlank(objectType))
						objectType = (String) message.get("nodeUniqueId");
				}
				producer = new kafka.javaapi.producer.Producer<String, String>(producerConfig);
				jsonMessage = mapper.writeValueAsString(message);
				KeyedMessage<String, String> keyedMessage = new KeyedMessage<String, String>(TOPIC, objectType,
						jsonMessage);
				producer.send(keyedMessage);
			}
		} catch (Exception e) {
			if (StringUtils.isNotBlank(jsonMessage))
				transactionMsgLogger.error(jsonMessage);
		} finally {
			if (null != producer)
				producer.close();
		}
	}
}
