package org.sunbird.learning.actor;

import akka.actor.ActorRef;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Request;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.learning.common.enums.LearningActorNames;
import org.sunbird.learning.util.ControllerUtil;
import org.sunbird.telemetry.logger.TelemetryManager;
import scala.concurrent.duration.FiniteDuration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Actor Class Which Updates Local Cache based on definition update event.
 *
 * @author Kumar Gauraw
 */
public class LocalCacheUpdateActor extends BaseGraphManager {

    private static final Integer TIME = Platform.config.hasPath("refresh.local.cache.duration.sec") ? Platform.config.getInt("refresh.local.cache.duration.sec") : 60;
    private static final FiniteDuration SCHEDULE = new FiniteDuration(TIME, TimeUnit.SECONDS);
    private static final String TOPIC_ID = Platform.config.hasPath("kafka.topic.system.command") ? Platform.config.getString("kafka.topic.system.command") : "dev.system.command";
    private static ObjectMapper mapper = new ObjectMapper();
    private static ControllerUtil controllerUtil = new ControllerUtil();

    public void onReceive(Object msg) throws Exception {
        Request request = (Request) msg;
        KafkaConsumer<Long, String> consumer = (KafkaConsumer<Long, String>) request.get("consumer");
        processEventData(consumer);
        getContext().system().scheduler().scheduleOnce(SCHEDULE, getSelf(), getRequest(consumer), getContext().dispatcher(), ActorRef.noSender());
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
                controllerUtil.updateDefinitionCache(graphId, objectType);
            } else {
                TelemetryManager.log("Skipping Definition Update in Local Cache as graphId or objectType is Blank. Event Data :" + event);
            }
        } catch (Exception e) {
            TelemetryManager.error("Error Occured While Updating Local Definition Cache : " + e);
        }
    }

    private static Request getRequest(KafkaConsumer<Long, String> consumer) {
        Request request = new Request();
        request.setManagerName(LearningActorNames.CACHE_UPDATE_ACTOR.name());
        request.setOperation("update-local-cache");
        request.put("consumer", consumer);
        return request;
    }

    @Override
    protected void invokeMethod(Request request, ActorRef parent) {

    }
}
