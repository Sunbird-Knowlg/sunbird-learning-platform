package org.ekstep.searchindex.consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.ekstep.searchindex.util.Consumer;
import org.ekstep.searchindex.util.ConsumerConfig;
import org.ekstep.searchindex.util.ConsumerGroup;
import org.ekstep.searchindex.util.ConsumerInit;
import org.ekstep.searchindex.util.ConsumerUtil;

import com.ilimi.common.dto.CoverageIgnore;

import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

public class ConsumerRunner {

    private static ConsumerUtil consumerUtil = new ConsumerUtil();

    private static kafka.consumer.ConsumerConfig createConsumerConfig(String a_zookeeper, String a_groupId) {
        Properties props = new Properties();
        props.put("zookeeper.connect", a_zookeeper);
        props.put("group.id", a_groupId);
        props.put("zookeeper.session.timeout.ms", "1000");
        props.put("zookeeper.sync.time.ms", "1000");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "largest");
        return new kafka.consumer.ConsumerConfig(props);
    }

    @CoverageIgnore
    public static void startConsumers() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        ConsumerConfig config = consumerUtil.getConsumerConfig();
        List<ConsumerGroup> consumerGroups = config.consumerGroups;
        ConsumerInit initConfig = config.consumerInit;
        final List<ConsumerConnector> consumerList = new ArrayList<ConsumerConnector>();
        for (ConsumerGroup consumerGroup : consumerGroups) {
            String groupId = consumerGroup.id;
            ConsumerConnector consumer = kafka.consumer.Consumer
                    .createJavaConsumerConnector(createConsumerConfig(initConfig.serverURI, groupId));
            consumerList.add(consumer);
            List<Consumer> consumers = consumerGroup.consumers;
            int a_numThreads = (null == consumers || consumers.isEmpty()) ? 1 : consumers.size();
            Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
            topicCountMap.put(initConfig.topic, new Integer(a_numThreads));
            Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
            List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(initConfig.topic);
            ExecutorService executor = Executors.newFixedThreadPool(a_numThreads);
            int threadNumber = 0;
            String messageProcessor = consumerGroup.messageProcessor;
            for (final KafkaStream<byte[], byte[]> stream : streams) {
                executor.submit(new ConsumerThread(stream, threadNumber, messageProcessor, consumer));
                threadNumber++;
            }
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {
        	@CoverageIgnore
            @Override
            public void run() {
                for (ConsumerConnector consumer : consumerList)
                    consumer.commitOffsets();
            }
        });
    }
}
