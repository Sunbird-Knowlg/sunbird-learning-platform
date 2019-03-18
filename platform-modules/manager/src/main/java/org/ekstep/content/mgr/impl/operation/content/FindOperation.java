package org.ekstep.content.mgr.impl.operation.content;

import org.apache.commons.collections.CollectionUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.mgr.ConvertGraphNode;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.taxonomy.common.LanguageCodeMap;
import org.ekstep.taxonomy.enums.TaxonomyAPIParams;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        response.put(TaxonomyAPIParams.content.name(), contentCleanUp(contentMap));
        response.setParams(getSucessStatus());
        return response;
    }

}
