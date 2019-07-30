package org.sunbird.jobs.samza.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ESUtil {
    private static ObjectMapper mapper = new ObjectMapper();


    public static void updateCoureBatch(String index, String type, Map<String, Object> dataToUpdate, Map<String, Object> dataToSelect) throws Exception {
        String key = dataToSelect.entrySet().stream().map(entry -> (String) entry.getValue()).collect(Collectors.joining("_"));
        String documentJson = ElasticSearchUtil.getDocumentAsStringById(index, type, key);
        Map<String, Object> coursebatch = new HashMap<>();
        if(StringUtils.isNotBlank(documentJson))
            coursebatch = mapper.readValue(documentJson, Map.class);
        coursebatch.putAll(dataToUpdate);
        ElasticSearchUtil.updateDocument(index, type, mapper.writeValueAsString(coursebatch), key);
    }
}
