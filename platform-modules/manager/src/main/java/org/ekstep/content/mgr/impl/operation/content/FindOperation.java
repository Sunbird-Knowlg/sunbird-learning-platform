package org.ekstep.content.mgr.impl.operation.content;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.mgr.ConvertGraphNode;
import org.ekstep.graph.cache.util.RedisStoreUtil;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.taxonomy.common.LanguageCodeMap;
import org.ekstep.taxonomy.enums.TaxonomyAPIParams;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class FindOperation extends BaseContentManager {


    @SuppressWarnings("unchecked")
    public Response find(String contentId, String mode, List<String> fields) {

        Response response = new Response();
        Map<String, Object> contentMap = null;

        DefinitionDTO definition = getDefinition(TAXONOMY_ID, CONTENT_OBJECT_TYPE);
        List<String> externalPropsList = getExternalPropsList(definition);

        fields = CollectionUtils.isEmpty(fields) ? new ArrayList<>() : new ArrayList<>(fields);

        //TODO: this is only for backward compatibility. remove after this release.
        if (fields.contains("tags")) {
            fields.remove("tags");
            fields.add("keywords");
        }

        if (!StringUtils.equalsIgnoreCase("edit", mode)) {
            String content = RedisStoreUtil.get(contentId);
            if (StringUtils.isNotBlank(content)) {
                try{
                    contentMap = objectMapper.readValue(content, new TypeReference<Map<String,Object>>() {
                    });
                }catch(Exception e){
                    throw new ServerException("ERR_CONTENT_PARSE","Something Went Wrong While Processing the Content. ",e);
                }

            } else {
                TelemetryManager.log("Fetching the Data For Content Id: " + contentId);
                Node node = getContentNode(TAXONOMY_ID, contentId, null);
                contentMap = ConvertGraphNode.convertGraphNode(node, TAXONOMY_ID, definition, null);
                RedisStoreUtil.saveData(contentId, contentMap, CONTENT_CACHE_TTL);
            }
        } else {
            TelemetryManager.log("Fetching the Data For Content Id: " + contentId);
            Node node = getContentNode(TAXONOMY_ID, contentId, mode);
            contentMap = ConvertGraphNode.convertGraphNode(node, TAXONOMY_ID, definition, fields);
            contentId = node.getIdentifier();
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

    private void updateContentTaggedProperty(Map<String,Object> contentMap, String mode) {
        Boolean contentTaggingFlag = Platform.config.hasPath("content.tagging.backward_enable")?
                Platform.config.getBoolean("content.tagging.backward_enable"): false;
        if(!StringUtils.equals(mode,"edit") && contentTaggingFlag) {
            List <String> contentTaggedKeys = Platform.config.hasPath("content.tagging.property") ?
                    Platform.config.getStringList("content.tagging.property"):
                    new ArrayList<>(Arrays.asList("subject","medium"));
            contentTaggedKeys.forEach(contentTagKey -> {
                if(contentMap.containsKey(contentTagKey)) {
                    List<String> prop = prepareList(contentMap.get(contentTagKey));
                    contentMap.put(contentTagKey, prop.get(0));
                }
            });
        }
    }

    private List<String> prepareList(Object obj) {
        List<String> list = new ArrayList<String>();
        try {
            list = Arrays.asList((String[]) obj);
        } catch (Exception e) {
            if (obj instanceof List)
                list.addAll((List<String>) obj);
        }
        if (null != list) {
            list = list.stream().filter(x -> org.apache.commons.lang3.StringUtils.isNotBlank(x) && !org.apache.commons.lang3.StringUtils.equals(" ", x)).collect(toList());
        }
        return list;
    }

}
