package org.ekstep.content.mgr.impl;

import org.ekstep.common.Platform;
import org.ekstep.common.dto.ExecutionContext;
import org.ekstep.common.dto.HeaderParam;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.taxonomy.mgr.impl.DummyBaseContentManager;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class CreateManager extends DummyBaseContentManager {

    private List<String> contentTypeList = Arrays.asList("Story", "Worksheet", "Game", "Simulation", "Puzzle",
            "Diagnostic", "ContentTemplate", "ItemTemplate");

    public Response create(Map<String, Object> map, String channelId) {
       return null;
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
