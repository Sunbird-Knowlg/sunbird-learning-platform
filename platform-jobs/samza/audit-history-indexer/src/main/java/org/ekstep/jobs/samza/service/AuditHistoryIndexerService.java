package org.ekstep.jobs.samza.service;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.task.MessageCollector;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.dac.dto.AuditHistoryRecord;
import org.ekstep.dac.enums.AuditHistoryConstants;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.JSONUtils;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;

/**
 * The Class AuditHistoryService provides implementations of the core operations defined in the IMessageProcessor along
 * with the methods to getAuditLogs and their properties
 * 
 * @author Santhosh Vasabhaktula
 * 
 * @see ISamzaService
 */
public class AuditHistoryIndexerService implements ISamzaService {

	static JobLogger LOGGER = new JobLogger(AuditHistoryIndexerService.class);

	private ObjectMapper mapper = new ObjectMapper();

	private ElasticSearchUtil esUtil = null;

	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	/** The constructor */
	public AuditHistoryIndexerService() {
		super();
		mapper.setDateFormat(df);
	}

	public void initialize(Config config) throws Exception {
		JSONUtils.loadProperties(config);
		esUtil = new ElasticSearchUtil();
		// Create index if not found
		String settings = "{\"settings\":{\"index\":{\"index\":\""
				+ AuditHistoryConstants.AUDIT_HISTORY_INDEX
				+ "\",\"type\":\""
				+ AuditHistoryConstants.AUDIT_HISTORY_INDEX_TYPE
				+ "\",\"analysis\":{\"analyzer\":{\"ah_index_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"standard\",\"filter\":[\"lowercase\",\"mynGram\"]},\"ah_search_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"standard\",           \"filter\": [             \"standard\",             \"lowercase\"           ]         },         \"keylower\": {           \"tokenizer\": \"keyword\",           \"filter\": \"lowercase\"         }       },       \"filter\": {         \"mynGram\": {           \"type\": \"nGram\",           \"min_gram\": 1,           \"max_gram\": 20,           \"token_chars\": [             \"letter\",             \"digit\",             \"whitespace\",             \"punctuation\",\"symbol\"]}}}}}}";
		String mappings = "{ \""
				+ AuditHistoryConstants.AUDIT_HISTORY_INDEX_TYPE
				+ "\" : {    \"dynamic_templates\": [      {        \"longs\": {          \"match_mapping_type\": \"long\",          \"mapping\": {            \"type\": \"long\",            fields: {              \"raw\": {                \"type\": \"long\"              }            }          }        }      },      {        \"booleans\": {          \"match_mapping_type\": \"boolean\",          \"mapping\": {            \"type\": \"boolean\",            fields: {              \"raw\": {                \"type\": \"boolean\"              }            }          }        }      },{        \"doubles\": {          \"match_mapping_type\": \"double\",          \"mapping\": {            \"type\": \"double\",            fields: {              \"raw\": {                \"type\": \"double\"              }            }          }        }      },	  {        \"dates\": {          \"match_mapping_type\": \"date\",          \"mapping\": {            \"type\": \"date\",            fields: {              \"raw\": {                \"type\": \"date\"              }            }          }        }      },      {        \"strings\": {          \"match_mapping_type\": \"string\",          \"mapping\": {            \"type\": \"string\",            \"copy_to\": \"all_fields\",            \"analyzer\": \"ah_index_analyzer\",            \"search_analyzer\": \"ah_search_analyzer\",            fields: {              \"raw\": {                \"type\": \"string\",                \"analyzer\": \"keylower\"              }            }          }        }      }    ],    \"properties\": {      \"all_fields\": {        \"type\": \"string\",        \"analyzer\": \"ah_index_analyzer\",        \"search_analyzer\": \"ah_search_analyzer\",        fields: {          \"raw\": {            \"type\": \"string\",            \"analyzer\": \"keylower\"          }        }      }    }  }}";
		esUtil.addIndex(AuditHistoryConstants.AUDIT_HISTORY_INDEX, AuditHistoryConstants.AUDIT_HISTORY_INDEX_TYPE, settings, mappings);
		LOGGER.info(AuditHistoryConstants.AUDIT_HISTORY_INDEX + " created");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector) throws Exception {
		Object audit = message.get("audit");
		Boolean shouldAudit = BooleanUtils.toBoolean(null == audit ? "true" : audit.toString());
		if (message != null && message.get("operationType") != null && null == message.get("syncMessage")
				&& !BooleanUtils.isFalse(shouldAudit)) {
			LOGGER.debug("Audit learning event received");
			try {
				AuditHistoryRecord record = getAuditHistory(message);
				LOGGER.debug("Audit record created");
				Map<String, Object> entity_map = mapper.convertValue(record, Map.class);
				String document = mapper.writeValueAsString(entity_map);
				LOGGER.debug("Saving the record into ES");
				esUtil.addDocument(AuditHistoryConstants.AUDIT_HISTORY_INDEX, AuditHistoryConstants.AUDIT_HISTORY_INDEX_TYPE, document);
				metrics.incSuccessCounter();
			} catch (Exception ex) {
				LOGGER.error("Failed to process message", message, ex);
				metrics.incFailedCounter();
			}
		} else {
			LOGGER.debug("Learning event not qualified for audit");
			metrics.incSkippedCounter();
		}
	}

	/**
	 * This method getAuditHistory sets the required data from the transaction message that can be saved to elastic
	 * search
	 * 
	 * @param transactionDataMap The Neo4j TransactionDataMap
	 * 
	 * @return AuditHistoryRecord that can be saved to elastic search DB
	 */
	private AuditHistoryRecord getAuditHistory(Map<String, Object> transactionDataMap) throws Exception {
		AuditHistoryRecord record = new AuditHistoryRecord();
		record.setUserId((String) transactionDataMap.get("userId"));
		record.setRequestId((String) transactionDataMap.get("requestId"));
		String nodeUniqueId = (String) transactionDataMap.get("nodeUniqueId");
		if (StringUtils.endsWith(nodeUniqueId, ".img")) {
			nodeUniqueId = StringUtils.replace(nodeUniqueId, ".img", "");
			record.setObjectId(nodeUniqueId);
		}
		record.setObjectId(nodeUniqueId);
		record.setObjectType((String) transactionDataMap.get("objectType"));
		record.setGraphId((String) transactionDataMap.get("graphId"));
		record.setOperation((String) transactionDataMap.get("operationType"));
		record.setLabel((String) transactionDataMap.get("label"));
		String transactionDataStr = mapper.writeValueAsString(transactionDataMap.get("transactionData"));
		record.setLogRecord(transactionDataStr);
		String summary = setSummaryData(transactionDataMap);
		record.setSummary(summary);
		String createdOn = (String) transactionDataMap.get("createdOn");
		Date date = df.parse(createdOn);
		record.setCreatedOn(null == date ? new Date() : date);
		return record;
	}

	/**
	 * This method setSummaryData sets the required summaryData from the transaction message and that can be saved to
	 * elastic search
	 * 
	 * @param transactionDataMap The Neo4j TransactionDataMap
	 * 
	 * @return summary
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private String setSummaryData(Map<String, Object> transactionDataMap) throws Exception {

		Map<String, Object> summaryData = new HashMap<String, Object>();
		Map<String, Integer> relations = new HashMap<String, Integer>();
		Map<String, Integer> tags = new HashMap<String, Integer>();
		Map<String, Object> properties = new HashMap<String, Object>();

		List<String> fields = new ArrayList<String>();
		Map<String, Object> transactionMap;
		String summaryResult = null;

		transactionMap = (Map<String, Object>) transactionDataMap.get("transactionData");
		for (Map.Entry<String, Object> entry : transactionMap.entrySet()) {
			List<Object> list = null;
			switch (entry.getKey()) {
			case "addedRelations":
				list = (List) entry.getValue();
				if (null != list && !list.isEmpty()) {
					relations.put("addedRelations", list.size());
				} else {
					relations.put("addedRelations", 0);
				}
				summaryData.put("relations", relations);
				break;
			case "removedRelations":
				list = (List) entry.getValue();
				if (null != list && !list.isEmpty()) {
					relations.put("removedRelations", list.size());
				} else {
					relations.put("removedRelations", 0);
				}
				summaryData.put("relations", relations);
				break;
			case "addedTags":
				list = (List) entry.getValue();
				if (null != list && !list.isEmpty()) {
					tags.put("addedTags", list.size());
				} else {
					tags.put("addedTags", 0);
				}
				summaryData.put("tags", tags);
				break;
			case "removedTags":
				list = (List) entry.getValue();
				if (null != list && !list.isEmpty()) {
					tags.put("removedTags", list.size());
				} else {
					tags.put("removedTags", 0);
				}
				summaryData.put("tags", tags);
				break;
			case "properties":
				if (StringUtils.isNotBlank(entry.getValue().toString())) {
					Map<String, Object> propsMap = (Map<String, Object>) entry.getValue();
					Set<String> propertiesSet = propsMap.keySet();
					if (null != propertiesSet) {
						for (String s : propertiesSet) {
							fields.add(s);
						}
					} else {
						properties.put("count", 0);
					}
				}
				properties.put("count", fields.size());
				properties.put("fields", fields);
				summaryData.put("properties", properties);
				break;
			default:
				break;
			}
		}
		summaryResult = mapper.writeValueAsString(summaryData);
		return summaryResult;
	}
}