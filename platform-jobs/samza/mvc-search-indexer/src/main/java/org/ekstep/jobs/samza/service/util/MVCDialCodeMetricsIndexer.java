package org.ekstep.jobs.samza.service.util;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.common.Platform;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MVCDialCodeMetricsIndexer extends AbstractESIndexer {

    private JobLogger LOGGER = new JobLogger(MVCDialCodeMetricsIndexer.class);
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void init() {
        ElasticSearchUtil.initialiseESClient(CompositeSearchConstants.DIAL_CODE_METRICS_INDEX,
                Platform.config.getString("search.es_conn_info"));
    }

    public void createDialCodeIndex() throws IOException {
        String settings = "{\"number_of_shards\":5}";
        String mappings = "{\"dynamic\":false,\"properties\":{\"dial_code\":{\"type\":\"keyword\"},\"total_dial_scans_local\":{\"type\":\"double\"},\"total_dial_scans_global\":{\"type\":\"double\"},\"average_scans_per_day\":{\"type\":\"double\"},\"last_scan\":{\"type\":\"date\",\"format\":\"strict_date_optional_time||epoch_millis\"},\"first_scan\":{\"type\":\"date\",\"format\":\"strict_date_optional_time||epoch_millis\"}}}";
        ElasticSearchUtil.addIndex(CompositeSearchConstants.DIAL_CODE_METRICS_INDEX,
                CompositeSearchConstants.DIAL_CODE_METRICS_INDEX_TYPE, settings, mappings);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map<String, Object> getIndexDocument(Map<String, Object> message, boolean updateRequest)
            throws IOException {
        Map<String, Object> indexDocument = new HashMap<>();
        // The nodeUniqueId will be the dialcodeId to be inserted or updated
        String uniqueId = (String) message.get("nodeUniqueId");
        if (updateRequest) {
            String documentJson = ElasticSearchUtil.getDocumentAsStringById(CompositeSearchConstants.DIAL_CODE_METRICS_INDEX,
                    CompositeSearchConstants.DIAL_CODE_METRICS_INDEX_TYPE, uniqueId);
            if (documentJson != null && !documentJson.isEmpty()) {
                indexDocument = mapper.readValue(documentJson, new TypeReference<Map<String, Object>>() {
                });
            }
        }
        Map transactionData = (Map) message.get("transactionData");
        if (transactionData != null) {
            Map<String, Object> addedProperties = (Map<String, Object>) transactionData.get("properties");
            if (addedProperties != null && !addedProperties.isEmpty()) {
                for (Map.Entry<String, Object> propertyMap : addedProperties.entrySet()) {
                    if (propertyMap != null && propertyMap.getKey() != null) {
                        String propertyName = propertyMap.getKey();
                        // new value of the property
                        Object propertyNewValue = ((Map<String, Object>) propertyMap.getValue()).get("nv");
                        // New value from transaction data is null, then remove
                        // the property from document
                        if (propertyNewValue == null)
                            indexDocument.remove(propertyName);
                        else {
                            indexDocument.put(propertyName, propertyNewValue);
                        }
                    }
                }
            }
        }
        indexDocument.put("dial_code", message.get("nodeUniqueId"));
        indexDocument.put("objectType", message.get("objectType"));
        return indexDocument;
    }

    private void upsertDocument(String uniqueId, String jsonIndexDocument) throws Exception {
        ElasticSearchUtil.addDocumentWithId(CompositeSearchConstants.DIAL_CODE_METRICS_INDEX,
                CompositeSearchConstants.DIAL_CODE_METRICS_INDEX_TYPE, uniqueId, jsonIndexDocument);
        LOGGER.info("Indexed dialcode metrics successfully for dialcode " + uniqueId);
    }

    public void upsertDocument(String uniqueId, Map<String, Object> message) throws Exception {
        LOGGER.info(uniqueId + " is indexing into dialcodemetrics.");
        String operationType = (String) message.get("operationType");
        switch (operationType) {
            case CompositeSearchConstants.OPERATION_CREATE: {
                Map<String, Object> indexDocument = getIndexDocument(message, false);
                String jsonIndexDocument = mapper.writeValueAsString(indexDocument);
                upsertDocument(uniqueId, jsonIndexDocument);
                break;
            }
            case CompositeSearchConstants.OPERATION_UPDATE: {
                Map<String, Object> indexDocument = getIndexDocument(message, true);
                String jsonIndexDocument = mapper.writeValueAsString(indexDocument);
                upsertDocument(uniqueId, jsonIndexDocument);
                break;
            }
            case CompositeSearchConstants.OPERATION_DELETE: {
                ElasticSearchUtil.deleteDocument(CompositeSearchConstants.DIAL_CODE_INDEX,
                        CompositeSearchConstants.DIAL_CODE_INDEX_TYPE, uniqueId);
                break;
            }
        }
    }

}
