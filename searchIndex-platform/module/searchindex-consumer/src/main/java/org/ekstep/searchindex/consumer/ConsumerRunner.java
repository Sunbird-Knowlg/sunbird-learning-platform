package org.ekstep.searchindex.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.ekstep.searchindex.util.PropertiesUtil;

public class ConsumerRunner {
	public static void main(String[] args) {
		int numConsumers = 1;
		String groupId = PropertiesUtil.getProperty("groupId");
		String topic = PropertiesUtil.getProperty("topic");
		String serverURI = PropertiesUtil.getProperty("bootstrap.servers");
		String[] partitionsProperty = PropertiesUtil.getProperty("partitions").split(",");
		int[] partitions =  new int[partitionsProperty.length];
		
		for(int i=0; i<partitionsProperty.length; i++){
			partitions[i] = Integer.parseInt(partitionsProperty[i]);
		}
		
		final ExecutorService executor = Executors.newFixedThreadPool(numConsumers);
		final List<ConsumerThread> consumers = new ArrayList<ConsumerThread>();
		for (int i = 0; i < numConsumers; i++) {
			ConsumerThread consumer = new ConsumerThread(i, groupId, topic, serverURI, partitions);
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
