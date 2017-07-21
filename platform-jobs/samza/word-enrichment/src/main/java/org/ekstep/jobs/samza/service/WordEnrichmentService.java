package org.ekstep.jobs.samza.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.task.MessageCollector;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.jobs.samza.util.WordEnrichmentParams;
import org.ekstep.searchindex.util.HTTPUtil;
import org.ekstep.searchindex.util.PropertiesUtil;

public class WordEnrichmentService implements ISamzaService {

	static JobLogger LOGGER = new JobLogger(WordEnrichmentService.class);

	private ObjectMapper mapper = new ObjectMapper();
	
	@SuppressWarnings("unused")
	private Config config = null;

	@Override
	public void initialize(Config config) throws Exception {
		this.config = config;
		Map<String, Object> props = new HashMap<String, Object>();
		for (Entry<String, String> entry : config.entrySet()) {
			props.put(entry.getKey(), entry.getValue());
		}
		PropertiesUtil.loadProperties(props);
		LOGGER.info("Service config initialized");
	}

	@Override
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector) throws Exception {
		Map<String, Object> transactionData = getTransactionEvent(message);
		if (null == transactionData) {
			metrics.incSkippedCounter();
			return;
		}
		try {
			String operationType = (String)message.get(WordEnrichmentParams.operationType.name());
			String identifier = (String)message.get(WordEnrichmentParams.nodeUniqueId.name());
			String languageId = (String)message.get(WordEnrichmentParams.graphId.name());
			LOGGER.info("Word Enrichment for OpertaionType" + operationType);
			switch(operationType){
				case "CREATE": {
						   if(transactionData.containsKey(WordEnrichmentParams.properties.name())){
						    	enrichWord(transactionData, languageId, identifier);
						    	break;
						    }
						    else{
						    	metrics.incSkippedCounter();
						    }
					   }	
				case "UPDATE": {
							if(transactionData.containsKey(WordEnrichmentParams.properties.name())){
						    	enrichWord(transactionData, languageId, identifier);
						    	break;
						    }
						    else{
						    	metrics.incSkippedCounter();
							}
					  }
			}
		} catch (Exception e) {
			LOGGER.error("Failed to process message. Content enrichment failed", message, e);
			metrics.incFailedCounter();
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String,Object> getTransactionEvent(Map<String,Object> message ){
		if(!message.containsKey(WordEnrichmentParams.graphId.name()))
	        	return null;
        if(!message.containsKey(WordEnrichmentParams.objectType.name()))
			return null;
        String objectType = (String)message.get(WordEnrichmentParams.objectType.name());
        if(!StringUtils.equalsIgnoreCase(objectType, WordEnrichmentParams.word.name()))
        	return null;
        if(!message.containsKey(WordEnrichmentParams.nodeType.name()))
        	return null;
        String nodeType = (String)message.get(WordEnrichmentParams.nodeType.name());
        if(!StringUtils.equalsIgnoreCase(nodeType, WordEnrichmentParams.DATA_NODE.name()))
        	return null;
        if(!message.containsKey(WordEnrichmentParams.operationType.name()))
        	return null;
        if (!message.containsKey(WordEnrichmentParams.transactionData.name()))
			return null;
		Map<String, Object> transactionMap = (Map<String, Object>) message.get(WordEnrichmentParams.transactionData.name());
        return transactionMap;   
	}
	
	@SuppressWarnings("unchecked")
	private void enrichWord(Map<String,Object> transactionData,String languageId,String identifier) throws Exception{
		Map<String, Object> properties = (Map<String, Object>) transactionData.get(WordEnrichmentParams.properties.name());
		if (properties != null && !properties.isEmpty()) {
			if (isEnrichNeeded(properties))
			enrichWord(languageId, identifier);
		}
	}
	
	@SuppressWarnings("unchecked")
	private boolean isEnrichNeeded(Map<String, Object> properties) {
		if (!properties.isEmpty()) {
			for (Map.Entry<String, Object> propertyMap : properties.entrySet()) {
				if (propertyMap != null && propertyMap.getKey() != null) {
					String propertyName = (String) propertyMap.getKey();
					if (propertyName.equalsIgnoreCase(WordEnrichmentParams.lemma.name())) {
						String newpLemmaValue = (String) ((Map<String, Object>) propertyMap.getValue()).get("nv");
						String oldLemmaValue = (String) ((Map<String, Object>) propertyMap.getValue()).get("ov");
						if (oldLemmaValue == null)
							return true;
						if (newpLemmaValue != null && oldLemmaValue != null
								&& !StringUtils.equalsIgnoreCase(oldLemmaValue, newpLemmaValue))
							return true;
					}
				}
			}
		}
		return false;
	}
	
	private void enrichWord(String languageId, String identifier) throws Exception {
		LOGGER.info("Word Enrichment initialized for ");
		Map<String, Object> requestBodyMap = new HashMap<String, Object>();
		Map<String, Object> requestMap = new HashMap<String, Object>();
		String url = PropertiesUtil.getProperty("language-api-url") + "/v1/language/tools/enrich/" + languageId;
		requestMap.put("word_id", identifier);
		requestBodyMap.put("request", requestMap);
		String requestBody = mapper.writeValueAsString(requestBodyMap);
		LOGGER.info("Updating Word enrich | URL: " + url + " | Request body: " + requestBody);
		HTTPUtil.makePostRequest(url, requestBody);
	}
}