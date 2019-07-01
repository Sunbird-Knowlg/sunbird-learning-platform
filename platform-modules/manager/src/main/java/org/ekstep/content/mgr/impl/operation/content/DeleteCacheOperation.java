package org.ekstep.content.mgr.impl.operation.content;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.graph.cache.util.RedisStoreUtil;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;

import java.util.Set;

public class DeleteCacheOperation extends BaseContentManager {

    public Response deleteCache(String identifier) throws Exception {
        if (StringUtils.isBlank(identifier))
            throw new ClientException(ContentErrorCodes.ERR_BLANK_REDIS_KEY.name(), "Redis Key cannot be blank");
        Set keys = RedisStoreUtil.keys("*" + identifier + "*");
        if (keys != null)
            keys.forEach(deleteKey -> RedisStoreUtil.delete((String) deleteKey));
        Response response = OK();
        return getResult(response, identifier, keys);

    }

    private Response getResult(Response response, String identifier, Set keys) {
        response.getResult().put("identifier", identifier);
        response.getResult().put("message", "Cache successfully deleted for the keys: " + keys);
        return response;
    }
}
