package org.ekstep.searchindex.consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.ekstep.searchindex.processor.MessageProcessor;

public class ConsumerThread implements Runnable {
	private final KafkaConsumer<String, String> consumer;
	private final String topic;
	private final int id;
	private int[] partitions;
	private MessageProcessor messagePrcessor = new MessageProcessor();

	public ConsumerThread(int id, String groupId, String topic, String serverURI, int[] partitions) {
		this.id = id;
		this.topic = topic;
		this.partitions = partitions;
		Properties props = new Properties();
		props.put("bootstrap.servers", serverURI);
		props.put("group.id", groupId);
		props.put("key.deserializer", StringDeserializer.class.getName());
		props.put("value.deserializer", StringDeserializer.class.getName());
		this.consumer = new KafkaConsumer<String, String>(props);
	}

	public void run() {
		try {
			List<TopicPartition> topicPartitions = new ArrayList<TopicPartition>();
			for(int partition: partitions){
				topicPartitions.add(new TopicPartition(this.topic, partition));
			}
			consumer.assign(topicPartitions);
			while (true) {
				ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
				for (ConsumerRecord<String, String> record : records) {
					Map<String, Object> data = new HashMap<String, Object>();
					String messageData = record.value();
					messagePrcessor.processMessage(messageData);
					System.out.println(this.id + ": " + data);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			consumer.close();
		}
	}

	public void shutdown() {
		consumer.wakeup();
	}
}