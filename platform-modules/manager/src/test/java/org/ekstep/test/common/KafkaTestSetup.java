/**
 * 
 */
package org.ekstep.test.common;

import java.util.List;

import org.ekstep.common.Platform;

import info.batey.kafka.unit.KafkaUnit;
import io.netty.handler.timeout.TimeoutException;

/**
 * @author gauraw
 *
 */
public class KafkaTestSetup {

	private static KafkaUnit kafkaServer = null;

	protected static void startKafkaServer() {
		String zookeeperUrl = Platform.config.hasPath("learning.test.zookeeper.url")
				? Platform.config.getString("learning.test.zookeeper.url") : "localhost:3080";
		String kafkaUrl = Platform.config.hasPath("learning.test.kafka.url")
				? Platform.config.getString("learning.test.kafka.url") : "localhost:9098";
		try {
			kafkaServer = new KafkaUnit("localhost:2180", "localhost:9095");
			kafkaServer.startup();
			System.out.println("Embedded Kafka Stated!");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void createTopic(String topicName) {
		kafkaServer.createTopic(topicName);
	}

	public List<String> getAllTopics() throws TimeoutException {
		return kafkaServer.listTopics();
	}

	public List<String> readMessageFromTopic(String topicName) throws Exception {
		return kafkaServer.pollMessages(topicName);
	}

	public static void tearKafkaServer() {
		kafkaServer.deleteAllTopics();
		kafkaServer.shutdown();
		System.out.println("Embedded Kafka Shutdown Successfully!");
	}
}
