package org.ekstep.searchindex.consumer;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.javaapi.message.ByteBufferMessageSet;
import kafka.message.MessageAndOffset;

/**
 * Created by user on 8/4/14.
 */
public class SearchIndexConsumer extends Thread {
	final static String clientId = "SimpleConsumerDemoClient";
	final static String TOPIC = "pythontest";
	ConsumerConnector consumerConnector;
	KafkaConsumer<String, String> consumer;

	public static void main(String[] argv) throws UnsupportedEncodingException {
		SearchIndexConsumer helloKafkaConsumer = new SearchIndexConsumer();
		helloKafkaConsumer.start();

	}

	public SearchIndexConsumer() {
		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9092");
		props.put("group.id", "consumer-tutorial");
		props.put("key.deserializer", StringDeserializer.class.getName());
		props.put("value.deserializer", StringDeserializer.class.getName());
		consumer = new KafkaConsumer<String, String>(props);
	}

	@Override
	public void run() {
		consumer.subscribe(Arrays.asList("foo", "bar"));
		try {
			while (true) {
				ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
				for (ConsumerRecord<String, String> record : records)
					System.out.println(record.offset() + ": " + record.value());
			}
		}catch (WakeupException e) {
			  // ignore for shutdown
		}finally {
			consumer.close();
		}

	}

	private static void printMessages(ByteBufferMessageSet messageSet) throws UnsupportedEncodingException {
		for (MessageAndOffset messageAndOffset : messageSet) {
			ByteBuffer payload = messageAndOffset.message().payload();
			byte[] bytes = new byte[payload.limit()];
			payload.get(bytes);
			System.out.println(new String(bytes, "UTF-8"));
		}
	}
}
