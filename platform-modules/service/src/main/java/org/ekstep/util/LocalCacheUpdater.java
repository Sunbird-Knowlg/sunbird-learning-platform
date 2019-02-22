package org.ekstep.util;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
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
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.router.RequestRouterPool;
import org.ekstep.learning.common.enums.LearningActorNames;
import org.ekstep.learning.contentstore.ContentStoreOperations;
import org.ekstep.learning.contentstore.ContentStoreParams;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.telemetry.logger.TelemetryManager;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * This Class Will Listen to the Kafka Topic and Update the local cache
 *
 * @author Kumar Gauraw
 */
public class LocalCacheUpdater {

    private static final String BOOTSTRAP_SERVERS = Platform.config.getString("kafka.urls");
    private static final String TOPIC_ID = Platform.config.getString("kafka.topic.system.command");
    private static KafkaConsumer<Long, String> consumer = null;

    public static void init() {
        startConsumer();
    }

    private static void startConsumer() {
        try {
            consumer = new KafkaConsumer<>(getProps());
            consumer.subscribe(Arrays.asList(TOPIC_ID));
            // actor call, scheduled every 1 minute
            makeLearningRequest(getRequest(consumer));
        } catch (Exception ex) {
            TelemetryManager.error("Exception Occured While Subscribing to kafka topic : " + TOPIC_ID + ". Exception is : " + ex);
        } finally {
            consumer.close();
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

    private static Request getRequest(KafkaConsumer<Long, String> consumer) {
        Request request = new Request();
        request.setManagerName(LearningActorNames.CACHE_UPDATE_ACTOR.name());
        request.setOperation("update-local-cache");
        request.put("consumer", consumer);
        return request;
    }

    private static void makeLearningRequest(Request request) {
        ActorRef router = LearningRequestRouterPool.getRequestRouter();
        try {
            router.tell(request, ActorRef.noSender());
        } catch (Exception e) {
            TelemetryManager.error("Error! Something went wrong: " + e.getMessage(), e);
            throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", e);
        }
    }

}
