package org.ekstep.kafka.producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.ekstep.common.Platform;
import java.util.Properties;

public class KafkaClientProducer {

	private final static String BOOTSTRAP_SERVERS = Platform.config.getString("graphevent.kafka.url");//"10.42.8.107:9092,10.42.8.74:9092,10.42.8.164:9092";
	
    public static Producer<Long, String> getProducer() {
		return loadProducerProperties();
	}
    
    public static Producer<Long, String> loadProducerProperties() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                                            BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaClientProducer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<Long, String>(props);
    }
}
