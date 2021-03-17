package org.sunbird.content.mgr.impl.operation.content;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.ExecutionContext;
import org.sunbird.common.dto.HeaderParam;
import org.sunbird.common.dto.Response;
import org.sunbird.common.enums.TaxonomyErrorCodes;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.mgr.ConvertToGraphNode;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.model.node.DefinitionDTO;
import org.sunbird.learning.common.enums.ContentAPIParams;
import org.sunbird.taxonomy.mgr.impl.BaseContentManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateOperation extends BaseContentManager {

    private List<String> contentTypeList = Arrays.asList("Story", "Worksheet", "Game", "Simulation", "Puzzle",
            "Diagnostic", "ContentTemplate", "ItemTemplate");

    public Response create(Map<String, Object> map, String channelId) throws Exception {
        if (null == map)
            return ERROR("ERR_CONTENT_INVALID_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);

        validateEmptyOrNullChannelId(channelId);

        // Checking for resourceType if contentType resource
        // validateNodeForContentType(map);

        DefinitionDTO definition = getDefinition(TAXONOMY_ID, CONTENT_OBJECT_TYPE);

        restrictProps(definition, map, "status");
        String framework = (String) map.get("framework");
        if (StringUtils.isBlank(framework))
            map.put("framework", getDefaultFramework());

        String mimeType = (String) map.get("mimeType");
        if (StringUtils.isNotBlank(mimeType)) {
            if (!isPluginMimeType(mimeType))
                map.put("osId", "org.sunbird.quiz.app");
            String contentType = (String) map.get("contentType");
            if (StringUtils.isNotBlank(contentType)) {
                List<String> parentVisibilityList = Platform.config.getStringList("content.metadata.visibility.parent");
                if (parentVisibilityList.contains(contentType.toLowerCase()))
                    map.put("visibility", "Parent");
            }

            if (StringUtils.equalsIgnoreCase("application/vnd.ekstep.plugin-archive", mimeType)) {
                String code = (String) map.get("code");
                if (null == code || StringUtils.isBlank(code))
                    return ERROR("ERR_PLUGIN_CODE_REQUIRED", "Unique code is mandatory for plugins",
                            ResponseCode.CLIENT_ERROR);
                map.put("identifier", map.get("code"));
            }

            updateDefaultValuesByMimeType(map, mimeType);

            if (StringUtils.equalsIgnoreCase(COLLECTION_MIME_TYPE, mimeType) || StringUtils.equalsIgnoreCase("application/vnd.ekstep.ecml-archive", mimeType)) {
                map.put("version", LATEST_CONTENT_VERSION);
            } else {
                map.put("version", DEFAULT_CONTENT_VERSION);
            }


            Map<String, Object> externalProps = new HashMap<String, Object>();
            List<String> externalPropsList = getExternalPropsList(definition);
            if (null != externalPropsList && !externalPropsList.isEmpty()) {
                for (String prop : externalPropsList) {
                    if (null != map.get(prop))
                        externalProps.put(prop, map.get(prop));
                    map.remove(prop);
                }
            }

            try {
                Node node = ConvertToGraphNode.convertToGraphNode(map, definition, null);
                node.setObjectType(CONTENT_OBJECT_TYPE);
                node.setGraphId(TAXONOMY_ID);
                Response response = createDataNode(node);
                if (checkError(response))
                    return response;
                else {
                    String contentId = (String) response.get(GraphDACParams.node_id.name());
                    if (null != externalProps && !externalProps.isEmpty()) {
                        Response externalPropsResponse = updateContentProperties(contentId, externalProps);
                        if (checkError(externalPropsResponse))
                            return externalPropsResponse;
                    }
                    return response;
                }
            } catch (Exception e) {
                return ERROR("ERR_CONTENT_SERVER_ERROR", "Internal error", ResponseCode.SERVER_ERROR);
            }
        } else {
            return ERROR("ERR_CONTENT_INVALID_CONTENT_MIMETYPE_TYPE", "Mime Type cannot be empty",
                    ResponseCode.CLIENT_ERROR);
        }
    }

    private String getDefaultFramework() {
        String channel = (String) ExecutionContext.getCurrent().getGlobalContext().get(HeaderParam.CHANNEL_ID.name());
        // TODO: check channel for default framework.
        if (Platform.config.hasPath("platform.framework.default"))
            return Platform.config.getString("platform.framework.default");
        else
            return "NCF";
    }

    private void validateNodeForContentType(Map<String, Object> map) {
        if (contentTypeList.contains((String) map.get(ContentAPIParams.contentType.name())))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_INVALID_CONTENT.name(),
                    ((String) map.get(ContentAPIParams.contentType.name())) + " is not a valid value for contentType");
    }

}