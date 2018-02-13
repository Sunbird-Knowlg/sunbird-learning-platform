package org.ekstep.kafka.util;

import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.ekstep.kafka.consumer.KafkaClientConsumer;
import org.ekstep.kafka.producer.KafkaClientProducer;

public class KafkaClientUtil {

	public static void runProducer(String event, String topic) throws Exception {
		if(validateKafkaConnection(topic)) {
			final Producer<Long, String> producer = KafkaClientProducer.getProducer();
			ProducerRecord<Long, String> record = new ProducerRecord<Long, String>(topic, event);
			producer.send(record);
		}
	}
	public static boolean validateKafkaConnection(String topic) throws Exception{
		Consumer<Long, String> consumer = KafkaClientConsumer.getConsumer();
	    Map<String, List<PartitionInfo>> topics = consumer.listTopics();
	    return topics.keySet().contains(topic);
	}
}
