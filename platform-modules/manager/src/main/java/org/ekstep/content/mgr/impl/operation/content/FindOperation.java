package org.ekstep.content.mgr.impl.operation.content;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.ExecutionContext;
import org.ekstep.common.dto.HeaderParam;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.mgr.ConvertGraphNode;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.kafka.KafkaClient;
import org.ekstep.taxonomy.common.LanguageCodeMap;
import org.ekstep.taxonomy.enums.TaxonomyAPIParams;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.ekstep.telemetry.util.LogTelemetryEventUtil;

import java.util.*;

public class FindOperation extends BaseContentManager {

    @SuppressWarnings("unchecked")
    public Response find(String contentId, String mode, List<String> fields) {
        Response response = new Response();

        Node node = getContentNode(TAXONOMY_ID, contentId, mode);

        TelemetryManager.log("Fetching the Data For Content Id: " + node.getIdentifier());
        DefinitionDTO definition = getDefinition(TAXONOMY_ID, node.getObjectType());
        List<String> externalPropsList = getExternalPropsList(definition);
        if (null == fields)
            fields = new ArrayList<String>();
        else
            fields = new ArrayList<String>(fields);

        // TODO: this is only for backward compatibility. remove after this
        // release.
        if (fields.contains("tags")) {
            fields.remove("tags");
            fields.add("keywords");
        }

        List<String> externalPropsToFetch = (List<String>) CollectionUtils.intersection(fields, externalPropsList);
        Map<String, Object> contentMap = ConvertGraphNode.convertGraphNode(node, TAXONOMY_ID, definition, fields);
        String channel = (String) contentMap.get("channel");
        if (StringUtils.isBlank(channel)) {
            channel = Platform.config.hasPath("channel.default") ?
                    Platform.config.getString("channel.default") : "in.ekstep";
        }
        Number version = (Number) contentMap.get("version");
        if (version == null || version.intValue() < 2) {
            generateMigrationInstructionEvent(contentId, channel);
        }

        if (null != externalPropsToFetch && !externalPropsToFetch.isEmpty()) {
            Response getContentPropsRes = getContentProperties(node.getIdentifier(), externalPropsToFetch);
            if (!checkError(getContentPropsRes)) {
                Map<String, Object> resProps = (Map<String, Object>) getContentPropsRes
                        .get(TaxonomyAPIParams.values.name());
                if (null != resProps)
                    contentMap.putAll(resProps);
            }
        }

        // Get all the languages for a given Content
        List<String> languages = convertStringArrayToList(
                (String[]) node.getMetadata().get(TaxonomyAPIParams.language.name()));

        // Eval the language code for all Content Languages
        List<String> languageCodes = new ArrayList<String>();
        for (String language : languages)
            languageCodes.add(LanguageCodeMap.getLanguageCode(language.toLowerCase()));
        if (null != languageCodes && languageCodes.size() == 1)
            contentMap.put(TaxonomyAPIParams.languageCode.name(), languageCodes.get(0));
        else
            contentMap.put(TaxonomyAPIParams.languageCode.name(), languageCodes);
        updateContentTaggedProperty(contentMap, mode);
        response.put(TaxonomyAPIParams.content.name(), contentCleanUp(contentMap));
        response.setParams(getSucessStatus());
        return response;
    }

    private void updateContentTaggedProperty(Map<String, Object> contentMap, String mode) {
        Boolean contentTaggingFlag = Platform.config.hasPath("content.tagging.backward_enable") ?
                Platform.config.getBoolean("content.tagging.backward_enable") : false;
        if (!StringUtils.equals(mode, "edit") && contentTaggingFlag) {
            List<String> contentTaggedKeys = Platform.config.hasPath("content.tagging.property") ?
                    Platform.config.getStringList("content.tagging.property") :
                    new ArrayList<>(Arrays.asList("subject", "medium"));
            contentTaggedKeys.forEach(contentTagKey -> {
                if (contentMap.containsKey(contentTagKey)) {
                    List<String> prop = Arrays.asList((String[]) contentMap.get(contentTagKey));
                    contentMap.put(contentTagKey, prop.get(0));
                }
            });
        }
    }

    private void generateMigrationInstructionEvent(String identifier, String channel) {
        try {
            pushInstructionEvent(identifier, channel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pushInstructionEvent(String contentId, String channel) throws Exception {
        Map<String, Object> actor = new HashMap<String, Object>();
        Map<String, Object> context = new HashMap<String, Object>();
        Map<String, Object> object = new HashMap<String, Object>();
        Map<String, Object> edata = new HashMap<String, Object>();

        generateInstructionEventMetadata(actor, context, object, edata, contentId, channel);
        String beJobRequestEvent = LogTelemetryEventUtil.logInstructionEvent(actor, context, object, edata);
        String topic = Platform.config.getString("kafka.topics.instruction");
        if (org.apache.commons.lang3.StringUtils.isBlank(beJobRequestEvent)) {
            throw new ClientException("BE_JOB_REQUEST_EXCEPTION", "Event is not generated properly.");
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(topic)) {
            KafkaClient.send(beJobRequestEvent, topic);
        } else {
            throw new ClientException("BE_JOB_REQUEST_EXCEPTION", "Invalid topic id.");
        }
    }

    private void generateInstructionEventMetadata(Map<String, Object> actor, Map<String, Object> context,
                                                  Map<String, Object> object, Map<String, Object> edata, String contentId,
                                                  String channel) {
        actor.put("id", "Collection Migration Samza Job");
        actor.put("type", "System");

        Map<String, Object> pdata = new HashMap<>();
        pdata.put("id", "org.ekstep.platform");
        pdata.put("ver", "1.0");
        context.put("pdata", pdata);
        if (Platform.config.hasPath("cloud_storage.env")) {
            String env = Platform.config.getString("cloud_storage.env");
            context.put("env", env);
        }

        object.put("id", contentId);
        object.put("type", "content");
        object.put("channel", channel);

        edata.put("action", "ecml-migration");
        edata.put("contentType", "Asset");
    }
}
