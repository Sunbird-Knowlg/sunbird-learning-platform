package org.ekstep.kafka.consumer;

import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.ekstep.common.Platform;

public class KafkaClientConsumer {

	private final static String BOOTSTRAP_SERVERS = Platform.config.getString("graphevent.kafka.url");//"10.42.8.107:9092,10.42.8.74:9092,10.42.8.164:9092";
	
	public static Consumer<Long, String> getConsumer() {
		return loadConsumerProperties();
	}

	public static Consumer<Long, String> loadConsumerProperties() {
		Properties props = new Properties();
	    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
	    props.put(ConsumerConfig.CLIENT_ID_CONFIG, "KafkaClientConsumer");
	    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
	    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
	    return new KafkaConsumer<>(props);
	}
}
