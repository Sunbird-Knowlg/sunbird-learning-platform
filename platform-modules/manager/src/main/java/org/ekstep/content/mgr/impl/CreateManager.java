package org.ekstep.content.mgr.impl;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.ExecutionContext;
import org.ekstep.common.dto.HeaderParam;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.mgr.ConvertToGraphNode;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.taxonomy.mgr.impl.DummyBaseContentManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateManager extends DummyBaseContentManager {

    private List<String> contentTypeList = Arrays.asList("Story", "Worksheet", "Game", "Simulation", "Puzzle",
            "Diagnostic", "ContentTemplate", "ItemTemplate");

    public Response create(Map<String, Object> map, String channelId) throws Exception {
        if (null == map)
            return ERROR("ERR_CONTENT_INVALID_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);

        if(StringUtils.isBlank(channelId))
            return ERROR(ContentErrorCodes.ERR_CHANNEL_BLANK_OBJECT.name(), "Channel can not be blank.", ResponseCode.CLIENT_ERROR);
        // Checking for resourceType if contentType resource
        // validateNodeForContentType(map);

        DefinitionDTO definition = getDefinition(TAXONOMY_ID, CONTENT_OBJECT_TYPE);

        restrictProps(definition, map, "status");
        String framework = (String) map.get("framework");
        if (StringUtils.isBlank(framework))
            map.put("framework", getDefaultFramework());

        String mimeType = (String) map.get("mimeType");
        if (StringUtils.isNotBlank(mimeType)) {
            if (!StringUtils.equalsIgnoreCase("application/vnd.android.package-archive", mimeType))
                map.put("osId", "org.ekstep.quiz.app");
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