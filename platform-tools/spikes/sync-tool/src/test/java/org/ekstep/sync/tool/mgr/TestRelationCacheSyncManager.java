package org.ekstep.sync.tool.mgr;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestRelationCacheSyncManager {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testEventGeneration() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>() {{
           put("contentType", "Course");
           put("pkgVersion", 1.0);
           put("id", "do_1234");
        }};
        RelationCacheSyncManager mgr = new RelationCacheSyncManager();
        String event = mgr.generateKafkaEvent(map);
        Assert.assertTrue(StringUtils.isNotBlank(event));

        Map eventObj = mapper.readValue(event, Map.class);
        Map<String, Object> edata = (Map<String, Object>) eventObj.get("edata");
        Assert.assertTrue(MapUtils.isNotEmpty(edata));
        String action = (String) edata.getOrDefault("action", "");
        Assert.assertTrue(StringUtils.equals("post-publish-process", action));

    }
}
