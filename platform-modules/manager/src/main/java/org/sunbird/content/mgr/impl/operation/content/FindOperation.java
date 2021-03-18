package org.sunbird.content.mgr.impl.operation.content;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.common.mgr.ConvertGraphNode;
import org.sunbird.graph.cache.util.RedisStoreUtil;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.model.node.DefinitionDTO;
import org.sunbird.kafka.KafkaClient;
import org.sunbird.learning.common.enums.ContentAPIParams;
import org.sunbird.taxonomy.common.LanguageCodeMap;
import org.sunbird.taxonomy.enums.TaxonomyAPIParams;
import org.sunbird.taxonomy.mgr.impl.BaseContentManager;
import org.sunbird.telemetry.logger.TelemetryManager;
import org.sunbird.telemetry.util.LogTelemetryEventUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class FindOperation extends BaseContentManager {

    private static List<String> CONTENT_CACHE_FINAL_STATUS = Arrays.asList("Live", "Unlisted");
    private static final Boolean CONTENT_CACHE_ENABLED = Platform.config.hasPath("content.cache.read") ? Platform.config.getBoolean("content.cache.read") : false;

    @SuppressWarnings("unchecked")
    public Response find(String contentId, String mode, List<String> fields) {

        Response response = new Response();
        Map<String, Object> contentMap = null;

        DefinitionDTO definition = getDefinition(TAXONOMY_ID, CONTENT_OBJECT_TYPE);
        List<String> externalPropsList = getExternalPropsList(definition);

        fields = CollectionUtils.isEmpty(fields) ? new ArrayList<>() : new ArrayList<>(fields);

        if (!StringUtils.equalsIgnoreCase("edit", mode)) {
            String content = "";
            if(CONTENT_CACHE_ENABLED)
                content = RedisStoreUtil.get(contentId);

            if (StringUtils.isNotBlank(content)) {
                try{
                    contentMap = objectMapper.readValue(content, new TypeReference<Map<String,Object>>() {
                    });
                }catch(Exception e){
                    TelemetryManager.error("Error Occurred While Parsing Hierarchy for Content Id : " + contentId + " | Error is: ", e);
                    throw new ServerException("ERR_CONTENT_PARSE","Something Went Wrong While Processing the Content. ",e);
                }

            } else {
                TelemetryManager.log("Fetching the Data For Content Id: " + contentId);
                Node node = getContentNode(TAXONOMY_ID, contentId, null);
                contentMap = ConvertGraphNode.convertGraphNode(node, TAXONOMY_ID, definition, null);
                if(CONTENT_CACHE_ENABLED && CONTENT_CACHE_FINAL_STATUS.contains(contentMap.get(ContentAPIParams.status.name()).toString()))
                    RedisStoreUtil.saveData(contentId, contentMap, CONTENT_CACHE_TTL);
            }
        } else {
            TelemetryManager.log("Fetching the Data For Content Id: " + contentId);
            Node node = getContentNode(TAXONOMY_ID, contentId, mode);
            contentMap = ConvertGraphNode.convertGraphNode(node, TAXONOMY_ID, definition, null);
            contentId = node.getIdentifier();
        }

        String channel = (String) contentMap.get("channel");
        if (StringUtils.isBlank(channel))
            channel = Platform.config.hasPath("channel.default") ? Platform.config.getString("channel.default") : "in.ekstep";
        Number version = (Number) contentMap.get("version");
        String mimeType = (String) contentMap.get("mimeType");
        if (null != mimeType
                && StringUtils.equalsIgnoreCase(mimeType, "application/vnd.ekstep.ecml-archive")
                && (version == null || version.intValue() < 2)) {
            generateMigrationInstructionEvent(contentId, channel);
        }

        // Filter contentMap based on Fields
        if(CollectionUtils.isNotEmpty(fields)){
            fields.add("identifier");
            List<String> fieldsIncluded = new ArrayList<>(fields);
            contentMap.keySet().removeIf(metadata -> !fieldsIncluded.contains(metadata));
        }

        List<String> externalPropsToFetch = (List<String>) CollectionUtils.intersection(fields, externalPropsList);

        if (null != externalPropsToFetch && !externalPropsToFetch.isEmpty()) {
            Response getContentPropsRes = getContentProperties(contentId, externalPropsToFetch);
            if (!checkError(getContentPropsRes)) {
                Map<String, Object> resProps = (Map<String, Object>) getContentPropsRes
                        .get(TaxonomyAPIParams.values.name());
                if (null != resProps)
                    contentMap.putAll(resProps);
            }
        }

        // Get all the languages for a given Content
        List<String> languages = prepareList(contentMap.get(TaxonomyAPIParams.language.name()));

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

    private static List<String> prepareList(Object obj) {
        List<String> list = new ArrayList<String>();
        try {
            if (obj instanceof String) {
                list.add((String) obj);
            } else if (obj instanceof String[]) {
                list = Arrays.asList((String[]) obj);
            } else if (obj instanceof List){
                list.addAll((List<String>) obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != list) {
            list = list.stream().filter(x -> org.apache.commons.lang3.StringUtils.isNotBlank(x) && !org.apache.commons.lang3.StringUtils.equals(" ", x)).collect(toList());
        }
        return list;
    }

    private void generateMigrationInstructionEvent(String identifier, String channel) {
        try {
            pushInstructionEvent(identifier, channel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pushInstructionEvent(String contentId, String channel) throws Exception {
        Map<String, Object> actor = new HashMap<String,Object>();
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
        pdata.put("id", "org.sunbird.platform");
        pdata.put("ver", "1.0");
        context.put("pdata", pdata);
        if (Platform.config.hasPath("cloud_storage.env")) {
            String env = Platform.config.getString("cloud_storage.env");
            context.put("env", env);
        }
        contentId = contentId.replace(".img", "");
        object.put("id", contentId);
        object.put("type", "content");
        object.put("channel", channel);

        edata.put("action", "ecml-migration");
        edata.put("contentType", "Ecml");
    }

    /**
     *
     * @param contentMap
     * @param mode
     */
    private void updateContentTaggedProperty(Map<String,Object> contentMap, String mode) {
        Boolean contentTaggingFlag = Platform.config.hasPath("content.tagging.backward_enable")?
                Platform.config.getBoolean("content.tagging.backward_enable"): false;
        if(!StringUtils.equals(mode,"edit") && contentTaggingFlag) {
            List <String> contentTaggedKeys = Platform.config.hasPath("content.tagging.property") ?
                    Arrays.asList(Platform.config.getString("content.tagging.property").split(",")):
                    new ArrayList<>(Arrays.asList("subject","medium"));
            contentTaggedKeys.forEach(contentTagKey -> {
                if(contentMap.containsKey(contentTagKey)) {
                    List<String> prop = prepareList(contentMap.get(contentTagKey));
                    if (CollectionUtils.isNotEmpty(prop))
                        contentMap.put(contentTagKey, prop.get(0));
                }
            });
        }
    }

}

