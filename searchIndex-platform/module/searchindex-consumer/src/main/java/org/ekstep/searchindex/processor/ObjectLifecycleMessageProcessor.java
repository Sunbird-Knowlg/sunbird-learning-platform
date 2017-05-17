package org.ekstep.searchindex.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.learning.util.ControllerUtil;

import com.ilimi.common.logger.LogHelper;
import com.ilimi.graph.dac.model.Node;

public class ObjectLifecycleMessageProcessor implements IMessageProcessor {

	/** The LOGGER */
	private static LogHelper LOGGER = LogHelper.getInstance(AuditHistoryMessageProcessor.class.getName());

	/** The ObjectMapper */
	private ObjectMapper mapper = new ObjectMapper();

	/** The controllerUtil */
	private ControllerUtil util = new ControllerUtil();
	
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
				LOGGER.info("Reading from kafka consumer" + messageData);
				message = mapper.readValue(messageData, new TypeReference<Map<String, Object>>() {
				});
			}
			if (null != message)
				processMessage(message);
		} catch (Exception e) {
			LOGGER.error("Error while processing kafka message", e);
			e.printStackTrace();
		}
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void processMessage(Map<String, Object> message) throws Exception {
		String prevstate = null;
		String state = null;
		Map<String,Object> objectMap = new HashMap<String,Object>();
		if(message.containsKey("transactionData")){
			Map<String,Object> transactionMap = (Map<String, Object>) message.get("transactionData");
			LOGGER.info("Fetching transactionData from transactionMap");
			Map<String,Object> propertiesMap = (Map<String, Object>) transactionMap.get("properties");
			if(propertiesMap.containsKey("status")){
				Map<String,Object> statusMap = (Map)propertiesMap.get("status");
				prevstate = (String)statusMap.get("ov");
				state = (String) statusMap.get("nv");
				objectMap.put("prevstate", prevstate);
				objectMap.put("state", state);
				if(null != message.get("nodeUniqueId")){
				Node node = util.getNode("domain", (String)message.get("nodeUniqueId"));
				objectMap.put("identifier", node.getIdentifier());
				objectMap.put("objectType", node.getObjectType());
				if(null != node.getMetadata()){
					Map<String,Object> nodeMap = new HashMap<String, Object>();
					nodeMap = (Map)node.getMetadata();
					for(Map.Entry<String, Object> entry:nodeMap.entrySet()){
						if(entry.getKey().equals("name"))
							objectMap.put("name", entry.getValue());
						if(entry.getKey().equals("code"))
							objectMap.put("code", entry.getValue());
						
					}
					}
				}
			}
		}
	}

}
