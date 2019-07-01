package org.ekstep.content.mgr.impl.operation.content;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.graph.cache.util.RedisStoreUtil;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;

import java.util.Set;

public class DeleteCacheOperation extends BaseContentManager {

    public Response deleteCache(String key) throws Exception {
        if (StringUtils.isBlank(key))
            throw new ClientException(ContentErrorCodes.ERR_BLANK_REDIS_KEY.name(), "Redis Key cannot be blank");
        Set keys = RedisStoreUtil.keys("*" + key + "*");
        if(keys != null)
            keys.forEach(deleteKey -> RedisStoreUtil.delete((String) deleteKey));
        Response response = OK();
        return getResult(response, key);

    }

    private Response getResult(Response response, String key) {
        response.getResult().put("redisKey", key);
        response.getResult().put("message", "Cache successfully deleted for the key: " + key);
        return response;
    }
}
