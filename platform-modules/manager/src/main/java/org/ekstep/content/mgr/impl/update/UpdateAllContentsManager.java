package org.ekstep.content.mgr.impl.update;

import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.mgr.ConvertToGraphNode;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.graph.service.common.DACConfigurationConstants;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.taxonomy.mgr.impl.DummyBaseContentManager;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@Component
public class UpdateAllContentsManager extends DummyBaseContentManager {

    public Response updateAllContents(String originalId, Map<String, Object> map) throws Exception {
        if (null == map)
            return ERROR("ERR_CONTENT_INVALID_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);

        DefinitionDTO definition = getDefinition(TAXONOMY_ID, CONTENT_OBJECT_TYPE);

        Map<String, Object> externalProps = new HashMap<>();
        List<String> externalPropsList = getExternalPropsList(definition);
        if (null != externalPropsList && !externalPropsList.isEmpty()) {
            for (String prop : externalPropsList) {
                if (null != map.get(prop))
                    externalProps.put(prop, map.get(prop));
                if (equalsIgnoreCase(ContentAPIParams.screenshots.name(), prop) && null != map.get(prop)) {
                    map.put(prop, null);
                } else {
                    map.remove(prop);
                }

            }
        }

        String graphPassportKey = Platform.config.getString(DACConfigurationConstants.PASSPORT_KEY_BASE_PROPERTY);
        map.put("versionKey", graphPassportKey);
        Node domainObj = ConvertToGraphNode.convertToGraphNode(map, definition, null);
        Response updateResponse = updateNode(originalId, CONTENT_OBJECT_TYPE, domainObj);
        if (checkError(updateResponse))
            return updateResponse;
        updateResponse.put(GraphDACParams.node_id.name(), originalId);

        Response getNodeResponse = getDataNode(TAXONOMY_ID, originalId + ".img");
        if(!checkError(getNodeResponse)){
            Node imgDomainObj = ConvertToGraphNode.convertToGraphNode(map, definition, null);
            updateNode(originalId + ".img", CONTENT_IMAGE_OBJECT_TYPE, imgDomainObj);
        }

        if (null != externalProps && !externalProps.isEmpty()) {
            Response externalPropsResponse = updateContentProperties(originalId, externalProps);
            if (checkError(externalPropsResponse))
                return externalPropsResponse;
        }
        return updateResponse;
    }

}
