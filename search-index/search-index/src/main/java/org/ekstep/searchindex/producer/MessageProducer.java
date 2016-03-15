package org.ekstep.searchindex.producer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

public class MessageProducer {
	final static String TOPIC = "consumer-tutorial";
	
	public void init(){
		
	}

	public static void main(String[] argv) {
		Properties properties = new Properties();
		properties.put("metadata.broker.list", "localhost:9092");
		properties.put("serializer.class", "kafka.serializer.StringEncoder");
		ProducerConfig producerConfig = new ProducerConfig(properties);
		kafka.javaapi.producer.Producer<String, String> producer = new kafka.javaapi.producer.Producer<String, String>(
				producerConfig);
		SimpleDateFormat sdf = new SimpleDateFormat();
		KeyedMessage<String, String> message = new KeyedMessage<String, String>(TOPIC, "1",
				"Test message from java program " + sdf.format(new Date()));
		producer.send(message);
		producer.close();
	}
}
