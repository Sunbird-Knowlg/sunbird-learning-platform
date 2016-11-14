package org.ekstep.searchindex.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import com.ilimi.common.logger.LogHelper;
import com.ilimi.dac.dto.AuditHistoryRecord;
import com.ilimi.taxonomy.mgr.IAuditHistoryManager;
import com.ilimi.util.ApplicationContextUtils;

public class AuditHistoryMeassageProcessor implements IMessageProcessor {

	private static LogHelper LOGGER = LogHelper.getInstance(AuditHistoryMeassageProcessor.class.getName());
	private ObjectMapper mapper = new ObjectMapper();
	private IAuditHistoryManager manager = null;
	Map<String, Object> summaryData = new HashMap<String, Object>();

	public AuditHistoryMeassageProcessor() {
		super();
	}

	@Override
	public void processMessage(String messageData) {
		try {
			Map<String, Object> message = mapper.readValue(messageData, new TypeReference<Map<String, Object>>() {
			});
			if (null != message)
				processMessage(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void processMessage(Map<String, Object> message) throws Exception {
		if (null == manager) {
			manager = (IAuditHistoryManager) ApplicationContextUtils.getApplicationContext()
					.getBean("auditHistoryManager");
		}
		LOGGER.info("Processing audit history message: Object Type: " + message.get("objectType") + " | Identifier: "
				+ message.get("nodeUniqueId") + " | Graph: " + message.get("graphId") + " | Operation: "
				+ message.get("operationType"));
		if (message != null && message.get("operationType") != null && null == message.get("syncMessage")) {
			AuditHistoryRecord record = getAuditHistory(message);
			manager.saveAuditHistory(record);
		}
	}

	private AuditHistoryRecord getAuditHistory(Map<String, Object> transactionDataMap) {
		AuditHistoryRecord record = new AuditHistoryRecord();

		try {
			record.setUserId((String) transactionDataMap.get("userId"));
			record.setRequestId((String) transactionDataMap.get("requestId"));
			record.setObjectId((String) transactionDataMap.get("nodeUniqueId"));
			record.setObjectType((String) transactionDataMap.get("objectType"));
			record.setGraphId((String) transactionDataMap.get("graphId"));
			record.setOperation((String) transactionDataMap.get("operationType"));
			record.setLabel((String) transactionDataMap.get("label"));
			String transactionDataStr = mapper.writeValueAsString(transactionDataMap.get("transactionData"));
			record.setLogRecord(transactionDataStr);
			String summary = setSummaryData(transactionDataStr);
			record.setSummary(summary);
			record.setCreatedOn(new Date());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return record;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String setSummaryData(String transactionDataStr)
			throws JsonGenerationException, JsonMappingException, IOException {
		
		List<String> fields = new ArrayList<String>();
		TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
		};
		HashMap<String, Object> transactionMap = mapper.readValue(transactionDataStr, typeRef);
		
		for (Map.Entry<String, Object> entry : transactionMap.entrySet()) {
			if (entry.getKey().equals("addedRelations")) {
				Map<String, Integer> relations = new HashMap<String, Integer>();
				List<Object> list = (List) entry.getValue();
				if (!list.isEmpty()) {
					relations.put("addedRelations", list.size());
				} else {
					relations.put("addedRelations", 0);
				}
				summaryData.put("relations", relations);
				
			} else if (entry.getKey().equals("removedRelations")) {
				Map<String, Integer> relations = new HashMap<String, Integer>();
				List<Object> list = (List) entry.getValue();
				if (!list.isEmpty()) {
					relations.put("removedRelations", list.size());
				} else {
					relations.put("removedRelations", 0);
				}
				summaryData.put("relations", relations);
				
			} else if (entry.getKey().equals("addedTags")) {
				Map<String, Integer> tags = new HashMap<String, Integer>();
				List<Object> list = (List) entry.getValue();
				if (!list.isEmpty()) {
					list.add(entry.getValue());
					tags.put("addedTags", list.size());
				} else {
					tags.put("addedTags", 0);
				}
				summaryData.put("tags", tags);
				
			} else if (entry.getKey().equals("removedTags")) {
				Map<String, Integer> tags = new HashMap<String, Integer>();
				List<Object> list = (List) entry.getValue();
				if (!list.isEmpty()) {
					list.add(entry.getValue());
					tags.put("removedTags", list.size());
				} else {
					tags.put("removedTags", 0);
				}
				summaryData.put("tags", tags);
				
			} else if (entry.getKey().equals("properties")) {
				Map<String, Object> properties = new HashMap<String, Object>();
				if (!entry.getValue().toString().isEmpty()) {
					String props = mapper.writeValueAsString(entry.getValue());
					HashMap<String, Object> propsMap = mapper.readValue(props, typeRef);

					Set<String> propertiesSet = propsMap.keySet();
					for (String s : propertiesSet) {
						fields.add(s);
					}
				}
				properties.put("count", fields.size());
				properties.put("fields", fields);
				summaryData.put("properties", properties);
			}
		}

		String summaryResult = mapper.writeValueAsString(summaryData);
		return summaryResult;
	}

}
