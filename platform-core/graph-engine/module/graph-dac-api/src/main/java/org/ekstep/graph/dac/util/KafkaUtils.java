package org.ekstep.graph.dac.util;

import java.util.Properties;

import org.neo4j.graphdb.event.TransactionData;

import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

public class KafkaUtils {
	
	final static String TOPIC = "LP@EkStep";
	
	public String transformTxData(TransactionData data) {
		String message = "";
		return message;
	}

	public void send(String messageBody) {
		ProducerConfig producerConfig = new ProducerConfig(getProperties());
        kafka.javaapi.producer.Producer<String,String> producer = new kafka.javaapi.producer.Producer<String, String>(producerConfig);
        KeyedMessage<String, String> message = new KeyedMessage<String, String>(TOPIC, messageBody);
        producer.send(message);
        producer.close();
	}
    
    public Properties getProperties() {
    	Properties properties = new Properties();
        properties.put("metadata.broker.list","localhost:9092");
        properties.put("serializer.class","kafka.serializer.StringEncoder");
        
        return properties;
    }

}
