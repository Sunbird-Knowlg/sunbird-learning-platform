package org.sunbird.common.util;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.exception.ClientException;
import org.sunbird.kafka.KafkaClient;
import org.sunbird.telemetry.util.LogTelemetryEventUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * This Utility Class Push Definition Update Instruction Event to Refresh Local Cache.
 *
 * @author Kumar Gauraw
 */
public class DefinitionUtil {

    private static String actorId = "learning-service";
    private static String actorType = "System";
    private static String pdataId = "org.sunbird.platform";
    private static String pdataVersion = "1.0";
    private static String action = "definition_update";

    public static void pushUpdateInstructionEvent(String graphId, String objectType) throws Exception {
        Map<String, Object> actor = new HashMap<String, Object>();
        Map<String, Object> context = new HashMap<String, Object>();
        Map<String, Object> object = new HashMap<String, Object>();
        Map<String, Object> edata = new HashMap<String, Object>();

        generateInstructionEventMetadata(graphId, getDefinitionId(objectType), objectType, actor, context, object, edata);
        String definitionUpdateEvent = LogTelemetryEventUtil.logInstructionEvent(actor, context, object, edata);

        String topic = Platform.config.hasPath("kafka.topic.system.command") ? Platform.config.getString("kafka.topic.system.command") : "dev.system.command";
        if (StringUtils.isBlank(definitionUpdateEvent)) {
            throw new ClientException("BE_REFRESH_CACHE_EXCEPTION", "Event is not generated properly.");
        }
        if (StringUtils.isNotBlank(topic)) {
            KafkaClient.send(definitionUpdateEvent, topic);
        } else {
            throw new ClientException("BE_REFRESH_CACHE_EXCEPTION", "Invalid kafka topic id.");
        }
    }

    private static void generateInstructionEventMetadata(String graphId, String definitionId, String objectType, Map<String, Object> actor, Map<String, Object> context, Map<String, Object> object, Map<String, Object> edata) {
        actor.put("id", actorId);
        actor.put("type", actorType);
        // TODO: Need to decide on channel
        context.put("channel", "in.ekstep");

        Map<String, Object> pdata = new HashMap<>();
        pdata.put("id", pdataId);
        pdata.put("ver", pdataVersion);
        context.put("pdata", pdata);
        if (Platform.config.hasPath("cloud_storage.env")) {
            String env = Platform.config.getString("cloud_storage.env");
            context.put("env", env);
        }

        object.put("id", definitionId);
        object.put("ver", 1.0);
        edata.put("action", action);
        edata.put("graphId", graphId);
        edata.put("objectType", objectType);
    }

    private static String getDefinitionId(String objectType) {
        return "DEFINITION_NODE_" + objectType;
    }
}
