package org.ekstep.kafka.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

public class KafkaConsumer extends Thread{

    private String TOPIC = PropertiesUtil.getProperty("topic");
    private ObjectMapper mapper = new ObjectMapper();
    private volatile boolean running;
    ConsumerConnector consumerConnector;
    IMessage message;

    public KafkaConsumer(IMessage message){
        Properties props = new Properties();
        props.put("zookeeper.connect", "localhost:2181");
        props.put("group.id","test-group");
        props.put("zookeeper.session.timeout.ms", "1000");
        props.put("zookeeper.sync.time.ms", "1000");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "largest");

        ConsumerConfig consumerConfig = new ConsumerConfig(props);
        consumerConnector = Consumer.createJavaConsumerConnector(consumerConfig);
        this.message = message;        
    }
    

    @Override
    public void run() {
    	running=true;
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(TOPIC, new Integer(1));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumerConnector.createMessageStreams(topicCountMap);
        KafkaStream<byte[], byte[]> stream =  consumerMap.get(TOPIC).get(0);
        ConsumerIterator<byte[], byte[]> it = stream.iterator();
        while(it.hasNext() && running){
    		try {
    			String messageData = new String(it.next().message());
    			Map<String, Object> messageObj = mapper.readValue(messageData, new TypeReference<Map<String, Object>>() {
    			});
    			message.putRecentMessage(messageObj);
    		} catch (Exception e) {
    			e.printStackTrace();
    			message.putRecentMessage(null);
    		}
        }
        consumerConnector.shutdown();
    }

    public void stopExecuting(){
    	running = false;
    	
    }


}
