package org.ekstep.kafka.producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.ekstep.common.Platform;

import java.util.Properties;

public class KafkaClientProducer {

	private final static String TOPIC = Platform.config.getString("instructionevent.kafka.topic.id");//"dev.learning.job.request";
    private final static String BOOTSTRAP_SERVERS = Platform.config.getString("graphevent.kafka.url");
    		//"10.42.8.107:9092,10.42.8.74:9092,10.42.8.164:9092";
	
    private static Producer<Long, String> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                                            BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaClientProducer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<Long, String>(props);
    }

    public static void runProducer(String event) throws Exception {
        final Producer<Long, String> producer = createProducer();
        try {
        		ProducerRecord<Long, String> record = new ProducerRecord<Long, String>(TOPIC, event);
        		producer.send(record);
        } finally {
        		producer.flush();
            producer.close();
        }
    }
}
