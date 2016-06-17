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

public class KafkaMessageProducer {
	private static String TOPIC;
	private static ProducerConfig producerConfig;
	private static ObjectMapper mapper = new ObjectMapper();

	private static final Logger transactionMsgLogger = LogManager.getLogger("TransactionMessageLogger");

	static {
		init();
	}

	private static void init() {
		TOPIC = PropertiesUtil.getProperty("topic");
		Properties properties = new Properties();
		properties.put("metadata.broker.list", PropertiesUtil.getProperty("metadata.broker.list"));
		properties.put("serializer.class", PropertiesUtil.getProperty("serializer.class"));
		properties.put("partitioner.class", "org.ekstep.searchindex.producer.MessagePartitioner");
		producerConfig = new ProducerConfig(properties);
	}

	public static void sendMessage(List<Map<String, Object>> messages) {
		if (null != messages && !messages.isEmpty()) {
			for (Map<String, Object> message : messages) {
				Producer<String, String> producer = null;
				String jsonMessage = "";
				try {
					producer = new kafka.javaapi.producer.Producer<String, String>(producerConfig);
					String objectType = (String) message.get("objectType");
					if (StringUtils.isBlank(objectType)) {
						objectType = (String) message.get("nodeType");
						if (StringUtils.isBlank(objectType))
							objectType = (String) message.get("nodeUniqueId");
					}
					jsonMessage = mapper.writeValueAsString(message);
					KeyedMessage<String, String> keyedMessage = new KeyedMessage<String, String>(TOPIC, objectType,
							jsonMessage);
					producer.send(keyedMessage);
				} catch (Exception e) {
					e.printStackTrace();
					if (StringUtils.isNotBlank(jsonMessage))
						transactionMsgLogger.error(jsonMessage);
				} finally {
					if (null != producer)
						producer.close();
				}
			}
		}
	}

}
