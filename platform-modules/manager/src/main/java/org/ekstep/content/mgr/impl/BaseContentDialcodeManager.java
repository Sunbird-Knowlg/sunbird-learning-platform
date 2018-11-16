package org.ekstep.content.mgr.impl;

import org.ekstep.common.Platform;
import org.ekstep.common.exception.ClientException;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BaseContentDialcodeManager extends BaseContentManager {

    protected void validateContentForReservedDialcodes(Map<String, Object> metaData) {
        List<String> validContentType = Platform.config.hasPath("learning.reserve_dialcode.content_type") ?
                Platform.config.getStringList("learning.reserve_dialcode.content_type") :
                Arrays.asList("TextBook");

        if(!validContentType.contains(metaData.get(ContentAPIParams.contentType.name())))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_CONTENTTYPE.name(),
                    "Invalid Content Type.");
    }

}
