package org.ekstep.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.graph.cache.util.RedisStoreUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FrameworkCache {

    private static final int cacheTtl = Platform.config.hasPath("framework.cache.ttl") ? Platform.config.getInt("framework.cache.ttl") : 86400;
    protected static boolean cacheEnabled = Platform.config.hasPath("framework.cache.read") ? Platform.config.getBoolean("framework.cache.read") : false;
    private static final String CACHE_PREFIX = "fw_";
    protected static ObjectMapper mapper = new ObjectMapper();


    protected static String getFwCacheKey(String identifier, List<String> categoryNames) {
        Collections.sort(categoryNames);
        return CACHE_PREFIX + identifier.toLowerCase() + "_" + categoryNames.stream().map(cat -> cat.toLowerCase()).collect(Collectors.joining("_"));
    }


    public static Map<String, Object> get(String id, List<String> returnCategories) throws IOException {
        if(cacheEnabled) {
            if(CollectionUtils.isNotEmpty(returnCategories)) {
                Collections.sort(returnCategories);
                String cachedCategories = RedisStoreUtil.get(getFwCacheKey(id, returnCategories));
                if(StringUtils.isNotBlank(cachedCategories)) {
                    return mapper.readValue(cachedCategories, new TypeReference<Map<String, Object>>(){});
                }
            } else {
                String frameworkMetadata = RedisStoreUtil.get(id);
                if(StringUtils.isNotBlank(frameworkMetadata)) {
                    return mapper.readValue(frameworkMetadata, new TypeReference<Map<String, Object>>(){});
                }
            }
        }
        return null;
    }


    public static void save(Map<String, Object> framework, List<String> categoryNames) throws JsonProcessingException {
        if(cacheEnabled && MapUtils.isNotEmpty(framework) && StringUtils.isNotBlank((String) framework.get("identifier")) && CollectionUtils.isNotEmpty(categoryNames)) {
            Collections.sort(categoryNames);
            String key = getFwCacheKey((String) framework.get("identifier"), categoryNames);
            RedisStoreUtil.save(key, mapper.writeValueAsString(framework), cacheTtl);
        }
    }

    public static void delete(String id) {
        if(StringUtils.isNotBlank(id))
            RedisStoreUtil.deleteByPattern(CACHE_PREFIX + id + "_*");
    }

}
