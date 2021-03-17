package org.sunbird.test.common;

import org.sunbird.common.Platform;

import info.batey.kafka.unit.KafkaUnit;

/**
 * @author gauraw
 *
 */
public class KafkaTestSetup {

	private static KafkaUnit kafkaServer = null;

	protected static void startKafkaServer() {
		String zookeeperUrl = Platform.config.hasPath("learning.test.zookeeper.url")
				? Platform.config.getString("learning.test.zookeeper.url") : "localhost:2080";
		String kafkaUrl = Platform.config.hasPath("learning.test.kafka.url")
				? Platform.config.getString("learning.test.kafka.url") : "localhost:9098";
		try {
			kafkaServer = new KafkaUnit(zookeeperUrl, kafkaUrl);
			kafkaServer.startup();
			System.out.println("Embedded Kafka Started!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createTopic(String topicName) {
		kafkaServer.createTopic(topicName);
	}

	public void createTopicWithPartition(String topicName, int partition) {
		kafkaServer.createTopic(topicName, partition);
	}

	protected static void tearKafkaServer() {
		kafkaServer.shutdown();
		System.out.println("Embedded Kafka Shutdown Successfully!");
	}
}
