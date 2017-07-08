package org.ekstep.searchindex.processor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.dac.dto.AuditHistoryRecord;
import com.ilimi.graph.common.DateUtils;
import com.ilimi.taxonomy.mgr.IAuditHistoryManager;
import com.ilimi.util.ApplicationContextUtils;

/**
 * The Class AuditHistoryMessageProcessor provides implementations of the core
 * operations defined in the IMessageProcessor along with the methods to
 * getAuditLogs and their properties
 * 
 * @author Karthik, Rashmi
 * 
 * @see IMessageProcessor
 */
public class AuditHistoryMessageProcessor implements IMessageProcessor {

	/** The LOGGER */
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	/** The ObjectMapper */
	private ObjectMapper mapper = new ObjectMapper();

	/** The interface IAduitHistoryManager */
	private IAuditHistoryManager manager = null;

	/** The constructor */
	public AuditHistoryMessageProcessor() {
		super();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.searchindex.processor #processMessage(java.lang.String,
	 * java.lang.String, java.io.File, java.lang.String)
	 */
	@Override
	public void processMessage(String messageData) {
		try {
			Map<String, Object> message = new HashMap<String, Object>();
			if(StringUtils.isNotBlank(messageData)){
				LOGGER.log("Reading from kafka consumer");
				message = mapper.readValue(messageData, new TypeReference<Map<String, Object>>() {
				});
			}
			if (null != message)
				processMessage(message);
		} catch (Exception e) {
			LOGGER.log("Error while processing kafka message", e.getMessage(), e);
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.searchindex.processor #processMessage(java.lang.String,
	 * java.lang.String, java.io.File, java.lang.String)
	 */
	@Override
	public void processMessage(Map<String, Object> message) throws Exception {
		if (null == manager) {
			manager = (IAuditHistoryManager) ApplicationContextUtils.getApplicationContext()
					.getBean("auditHistoryManager");
		}
		LOGGER.log("Processing audit history message: Object Type: " + message.get("objectType") + " | Identifier: "
				+ message.get("nodeUniqueId") + " | Graph: " + message.get("graphId") + " | Operation: "
				+ message.get("operationType"));
		Object audit = message.get("audit");
		Boolean shouldAudit = BooleanUtils.toBoolean(null == audit ? "true" : audit.toString());
		if (message != null && message.get("operationType") != null && null == message.get("syncMessage") && !BooleanUtils.isFalse(shouldAudit)) {
				AuditHistoryRecord record = getAuditHistory(message);
				LOGGER.log("Sending AuditHistoryRecord to audit History manager" , record);
				manager.saveAuditHistory(record);
		}
	}

	/** 
	 * This method getAuditHistory sets the required data from the transaction message 
	 * that can be saved to elastic search
	 * 
	 * @param transactionDataMap
	 *        The Neo4j TransactionDataMap
	 *        
	 * @return AuditHistoryRecord that can be saved to elastic search DB
	 */
	private AuditHistoryRecord getAuditHistory(Map<String, Object> transactionDataMap) {
		AuditHistoryRecord record = new AuditHistoryRecord();
        LOGGER.log("Setting the audit history fields from transactionData" , transactionDataMap.size());
        try {
			record.setUserId((String) transactionDataMap.get("userId"));
			record.setRequestId((String) transactionDataMap.get("requestId"));
			String nodeUniqueId = (String) transactionDataMap.get("nodeUniqueId");
			if(StringUtils.endsWith(nodeUniqueId, ".img")){
				nodeUniqueId = StringUtils.replace(nodeUniqueId, ".img", "");
				record.setObjectId(nodeUniqueId);
			}
			record.setObjectId(nodeUniqueId);
			record.setObjectType((String) transactionDataMap.get("objectType"));
			record.setGraphId((String) transactionDataMap.get("graphId"));
			record.setOperation((String) transactionDataMap.get("operationType"));
			record.setLabel((String) transactionDataMap.get("label"));
			String transactionDataStr = mapper.writeValueAsString(transactionDataMap.get("transactionData"));
//			Map<String,Object> transactionData = setLogRecordData(transactionDataMap);
//			String transactionDataStr = mapper.writeValueAsString(transactionData);
			record.setLogRecord(transactionDataStr);
			String summary = setSummaryData(transactionDataMap);
			record.setSummary(summary);
			String createdOn = (String) transactionDataMap.get("createdOn");
			Date date = DateUtils.parse(createdOn);
			record.setCreatedOn(null == date ? new Date() : date);
			LOGGER.log("mapped audit history record from transcationData" , record);
		} catch (Exception e) {
			LOGGER.log("Error while setting the transactionData to elastic search" , e.getMessage(), e);
			e.printStackTrace();
		}
		return record;
	}

//	@SuppressWarnings("unchecked")
//	private Map<String,Object> setLogRecordData(Map<String, Object> transactionDataMap) {
//		Map<String,Object> newPropertiesMap = new HashMap<String,Object>();
//		Map<String,Object> transactionMap = (Map<String, Object>) transactionDataMap.get("transactionData");
//		LOGGER.log("Fetching transactionData from transactionMap");
//		Map<String,Object> propertiesMap = (Map<String, Object>) transactionMap.get("properties");
//		for(Entry <String, Object> entry: propertiesMap.entrySet()){
//			LOGGER.log("Checking if entry is a systemProperty :" + entry.getKey());
//			if(!SystemProperties.isSystemProperty(entry.getKey())){
//				newPropertiesMap.put(entry.getKey(), entry.getValue());
//			}
//		}
//		transactionMap.replace("properties", newPropertiesMap);
//		transactionDataMap.replace("transactionData", transactionMap);
//		return transactionDataMap;
//	}

	/** 
	 * This method setSummaryData sets the required summaryData from the transaction message 
	 * and that can be saved to elastic search
	 * 
	 * @param transactionDataMap
	 *        The Neo4j TransactionDataMap
	 *        
	 * @return summary
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String setSummaryData(Map<String, Object> transactionDataMap) {

		Map<String, Object> summaryData = new HashMap<String, Object>();
		Map<String, Integer> relations = new HashMap<String, Integer>();
		Map<String, Integer> tags = new HashMap<String, Integer>();
		Map<String, Object> properties = new HashMap<String, Object>();

		List<String> fields = new ArrayList<String>();
		Map<String, Object> transactionMap;
		String summaryResult = null;
		try {
			LOGGER.log("setting the summary from transactionData" , transactionDataMap);
			transactionMap = (Map<String, Object>) transactionDataMap.get("transactionData");
			for (Map.Entry<String, Object> entry : transactionMap.entrySet()) {
				if (StringUtils.equalsIgnoreCase("addedRelations", entry.getKey())) {
					List<Object> list = (List) entry.getValue();
					if (null != list && !list.isEmpty()) {

						relations.put("addedRelations", list.size());
					} else {
						relations.put("addedRelations", 0);
					}
					summaryData.put("relations", relations);

				} else if (StringUtils.equalsIgnoreCase("removedRelations", entry.getKey())) {
					List<Object> list = (List) entry.getValue();
					if (null != list && !list.isEmpty()) {
						relations.put("removedRelations", list.size());
					} else {
						relations.put("removedRelations", 0);
					}
					summaryData.put("relations", relations);

				} else if (StringUtils.equalsIgnoreCase("addedTags", entry.getKey())) {
					List<Object> list = (List) entry.getValue();
					if (null != list && !list.isEmpty()) {
						list.add(entry.getValue());
						tags.put("addedTags", list.size());
					} else {
						tags.put("addedTags", 0);
					}
					summaryData.put("tags", tags);

				} else if (StringUtils.equalsIgnoreCase("removedTags", entry.getKey())) {
					List<Object> list = (List) entry.getValue();
					if (null != list && !list.isEmpty()) {
						list.add(entry.getValue());
						tags.put("removedTags", list.size());
					} else {
						tags.put("removedTags", 0);
					}
					summaryData.put("tags", tags);

				} else if (StringUtils.equalsIgnoreCase("properties",entry.getKey())) {
					if (StringUtils.isNotBlank(entry.getValue().toString())) {
						Map<String, Object> propsMap = (Map<String, Object>) entry.getValue();
						Set<String> propertiesSet = propsMap.keySet();
						if(null!= propertiesSet) {
							for (String s : propertiesSet) {
								fields.add(s);
							}
						}
						else{
							properties.put("count", 0);
						}
					}
					properties.put("count", fields.size());
					properties.put("fields", fields);
					summaryData.put("properties", properties);
				}
			}
		     summaryResult = mapper.writeValueAsString(summaryData);
			LOGGER.log("setting summary field from transaction data" , summaryData);
		} catch (Exception e) {
			LOGGER.log("Error while setting the summary info to mysql db" + e.getMessage(), e);
			e.printStackTrace();
		}
		return summaryResult;
	}
}