package org.ekstep.searchindex.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.ekstep.searchindex.util.Consumer;
import org.ekstep.searchindex.util.ConsumerConfig;
import org.ekstep.searchindex.util.ConsumerGroup;
import org.ekstep.searchindex.util.ConsumerInit;
import org.ekstep.searchindex.util.ConsumerUtil;

public class ConsumerRunner {

	private static ConsumerUtil consumerUtil = new ConsumerUtil();

	public static void main(String[] args)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		startConsumers();
	}

	public static void startConsumers() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		ConsumerConfig config = consumerUtil.readConsumerProperties();
		List<ConsumerGroup> consumerGroups = config.consumerGroups;
		for (ConsumerGroup consumerGroup : consumerGroups) {
			String groupId = consumerGroup.id;
			List<Consumer> consumers = consumerGroup.consumers;
			for (Consumer consumer : consumers) {
				String[] partitionsProperty = consumer.partitions.split(",");
				String messageProcessor = consumer.messageProcessor;
				createConsumer(partitionsProperty, messageProcessor, config.consumerInit, groupId);
			}
		}
	}

	private static void createConsumer(String[] partitionsProperty, String messageProcessor, ConsumerInit config, String groupId)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		int numConsumers = 1;
		String topic = config.topic;
		String serverURI = config.serverURI;
		int[] partitions = new int[partitionsProperty.length];

		for (int i = 0; i < partitionsProperty.length; i++) {
			partitions[i] = Integer.parseInt(partitionsProperty[i]);
		}

		final ExecutorService executor = Executors.newFixedThreadPool(numConsumers);
		final List<ConsumerThread> consumers = new ArrayList<ConsumerThread>();
		for (int i = 0; i < numConsumers; i++) {
			ConsumerThread consumer = new ConsumerThread(i, groupId, topic, serverURI, partitions, messageProcessor);
			consumers.add(consumer);
			executor.submit(consumer);
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				for (ConsumerThread consumer : consumers) {
					consumer.shutdown();
				}
				executor.shutdown();
				try {
					executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
