package org.sunbird.jobs.samza.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    
    public static void updateBatches(String index, String type, Map<String, Object> batches) throws Exception {
        List<String> documents = ElasticSearchUtil.getMultiDocumentAsStringByIdList(index, type, new ArrayList<>(batches.keySet()));
        if(CollectionUtils.isNotEmpty(documents)) {
            Map<String, Object> docMap = new HashMap<>();
            for(String doc: documents) {
                Map<String, Object> document = mapper.readValue(doc, Map.class);
                String docId = (String) document.getOrDefault("id", "");
                Map<String, Object> dataToUpdate = new HashMap<>();
                dataToUpdate.putAll((Map<String, Object>) batches.getOrDefault(docId, new HashMap<String, Object>()));
                dataToUpdate.put("contentStatus", mapper.writeValueAsString(dataToUpdate.get("contentStatus")));
                document.putAll(dataToUpdate);
                docMap.put(docId, document);
            }
            if(MapUtils.isNotEmpty(docMap)){
                ElasticSearchUtil.bulkIndexWithIndexId(index, type, docMap);
            }
        }
    }
    
}
