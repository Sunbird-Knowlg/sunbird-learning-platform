package org.ekstep.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.ekstep.common.Platform;
import org.ekstep.graph.model.cache.DefinitionCache;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * This Class Will Listen to the Kafka Topic and Update the local cache
 *
 * @author Kumar Gauraw
 */
public class LocalCacheUpdater extends Thread {

    private static final String BOOTSTRAP_SERVERS = Platform.config.getString("kafka.urls");
    private static final String TOPIC_ID = Platform.config.getString("kafka.topic.system.command");
    private static ObjectMapper mapper = new ObjectMapper();
    private static KafkaConsumer<Long, String> consumer = null;

    public static void init() {
        LocalCacheUpdater cacheUpdater = new LocalCacheUpdater();
        cacheUpdater.start();
    }

    public void run() {
        startConsumer();
    }

    private void startConsumer() {
        try {
            consumer = new KafkaConsumer<>(getProps());
            consumer.subscribe(Arrays.asList(TOPIC_ID));
            while (true)
                processEventData(consumer);
        } catch (Exception ex) {
            TelemetryManager.error("Exception Occured While Subscribing to kafka topic : " + TOPIC_ID + ". Exception is : " + ex);
        } finally {
            consumer.close();
        }
    }

    private void processEventData(KafkaConsumer<Long, String> consumer) {
        try {
            ConsumerRecords<Long, String> records = consumer.poll(60000);
            for (ConsumerRecord<Long, String> record : records) {
                Map<String, Object> event = getEventData(record);
                if (null != event && !event.isEmpty())
                    updateDefinitionCache(event);
                else
                    TelemetryManager.log("Skipping Update Local Cache Event as event is Blank.");
            }
            consumer.commitAsync();
        } catch (Exception e) {
            TelemetryManager.error("Exception Occured While Reading event from kafka topic : " + TOPIC_ID + ". Exception is : " + e);
        }
    }

    private Map<String, Object> getEventData(ConsumerRecord<Long, String> record) {
        try {
            Map<String, Object> event = mapper.readValue(record.value(), new TypeReference<Map<String, Object>>() {
            });
            return event;
        } catch (Exception e) {
            TelemetryManager.error("Exception Occured While Parsing event data from kafka topic : " + TOPIC_ID + ". Exception is : " + e);
        }
        return null;
    }

    private void updateDefinitionCache(Map<String, Object> event) {
        try {
            Map<String, String> edata = (Map<String, String>) event.getOrDefault("edata", new HashMap<>());
            String graphId = edata.get("graphId");
            String objectType = edata.get("objectType");
            if (StringUtils.isNotBlank(graphId) && StringUtils.isNotBlank(objectType)) {
                DefinitionCache.updateDefinitionCache(graphId, objectType);
            } else {
                TelemetryManager.log("Skipping Definition Update in Local Cache as graphId or objectType is Blank. Event Data :" + event);
            }
        } catch (Exception e) {
            TelemetryManager.error("Error Occured While Updating Local Definition Cache : " + e);
        }
    }

    private static Properties getProps() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "LocalCacheUpdater");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, getGroupId());
        return props;
    }

    private static String getGroupId() {
        String groupId = "learning-";
        try {
            groupId += InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            groupId += UUID.randomUUID().toString();
        }
        return groupId;
    }


}
