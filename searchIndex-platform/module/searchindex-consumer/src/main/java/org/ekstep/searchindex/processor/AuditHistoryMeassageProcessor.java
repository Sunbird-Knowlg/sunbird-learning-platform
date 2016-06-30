package org.ekstep.searchindex.processor;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.ilimi.dac.dto.AuditHistoryRecord;
import com.ilimi.taxonomy.mgr.IAuditHistoryManager;
import com.ilimi.util.ApplicationContextUtils;

public class AuditHistoryMeassageProcessor implements IMessageProcessor {

	private ObjectMapper mapper = new ObjectMapper();
	private IAuditHistoryManager manager= null;
	
	public AuditHistoryMeassageProcessor() {
		super();
	}
	
	@Override
	public void processMessage(String messageData) {
		try {
			Map<String, Object> message = mapper.readValue(messageData, new TypeReference<Map<String, Object>>() {
			});
			processMessage(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void processMessage(Map<String, Object> message) throws Exception {
		if (null == manager) {
			manager = (IAuditHistoryManager) ApplicationContextUtils.getApplicationContext().getBean("auditHistoryManager");
		}
		if (message != null && message.get("operationType") != null && null == message.get("syncMessage")) {
			AuditHistoryRecord record=getAuditHistory(message);
			manager.saveAuditHistory(record); 
		}

	}
	
	
	private AuditHistoryRecord getAuditHistory(Map<String, Object> transactionDataMap){
		AuditHistoryRecord record=new AuditHistoryRecord();
		
		try {
		record.setUserId((String)transactionDataMap.get("userId"));
		record.setRequestId((String)transactionDataMap.get("requestId"));
		
		record.setObjectId((String)transactionDataMap.get("nodeUniqueId"));
		record.setObjectType((String)transactionDataMap.get("objectType"));
		record.setGraphId((String)transactionDataMap.get("graphId"));
		record.setOperation((String)transactionDataMap.get("operationType"));
		record.setLabel((String)transactionDataMap.get("label"));

		String transactionDataStr = mapper.writeValueAsString(transactionDataMap.get("transactionData"));

		record.setLogRecord(transactionDataStr);
		record.setCreatedOn(new Date());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return record;
	}

}
