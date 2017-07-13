package org.ekstep.searchindex.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.searchindex.util.HTTPUtil;
import org.ekstep.searchindex.util.PropertiesUtil;

import com.ilimi.common.logger.PlatformLogger;

public class WordEnrichMessageProcessor implements IMessageProcessor {

	private ObjectMapper mapper = new ObjectMapper();

	public WordEnrichMessageProcessor() {
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
			PlatformLogger.log("Exception", e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processMessage(Map<String, Object> message) throws Exception {
		if (message != null && message.get("operationType") != null) {
			String nodeType = (String) message.get("nodeType");
			String objectType = (String) message.get("objectType");
			String languageId = (String) message.get("graphId");
			String uniqueId = (String) message.get("nodeUniqueId");
			if (StringUtils.equalsIgnoreCase(CompositeSearchConstants.OBJECT_TYPE_WORD, objectType)) {
				PlatformLogger.log("Processing message for Word object type");
				switch (nodeType) {
				case CompositeSearchConstants.NODE_TYPE_DATA: {
					String operationType = (String) message.get("operationType");
					switch (operationType) {
					case CompositeSearchConstants.OPERATION_CREATE: {
						Map transactionData = (Map) message.get("transactionData");
						if (transactionData != null) {
							Map<String, Object> addedProperties = (Map<String, Object>) transactionData
									.get("properties");
							if (addedProperties != null && !addedProperties.isEmpty()) {
								if (isEnrichNeeded(addedProperties))
									enrichWord(languageId, uniqueId);
							}
						}
						break;
					}
					case CompositeSearchConstants.OPERATION_UPDATE: {
						Map transactionData = (Map) message.get("transactionData");
						if (transactionData != null) {
							Map<String, Object> addedProperties = (Map<String, Object>) transactionData
									.get("properties");
							if (addedProperties != null && !addedProperties.isEmpty()) {
								if (isEnrichNeeded(addedProperties))
									enrichWord(languageId, uniqueId);
							}
						}
						break;
					}
					}
					// System.out.println("Word count message processor: " +
					// wordsCount + " | " + liveWordsCount);
					break;
				}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private boolean isEnrichNeeded(Map<String, Object> addedProperties) {

		if (addedProperties != null && !addedProperties.isEmpty()) {
			for (Map.Entry<String, Object> propertyMap : addedProperties.entrySet()) {
				if (propertyMap != null && propertyMap.getKey() != null) {
					String propertyName = (String) propertyMap.getKey();
					if (propertyName.equalsIgnoreCase("lemma")) {
						String newpLemmaValue = (String) ((Map<String, Object>) propertyMap.getValue()).get("nv");// new
																													// value
						String oldLemmaValue = (String) ((Map<String, Object>) propertyMap.getValue()).get("ov");// old
																													// value

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

	private void enrichWord(String languageId, String wordId) {
		PlatformLogger.log("calling enrich api Language Id:" + languageId + " word :" + wordId);

		try {

		String url = PropertiesUtil.getProperty("language-api-url") + "/v1/language/tools/enrich/" + languageId;

		Map<String, Object> requestBodyMap = new HashMap<String, Object>();
		Map<String, Object> requestMap = new HashMap<String, Object>();
		requestMap.put("word_id", wordId);
		requestBodyMap.put("request", requestMap);

		String requestBody = mapper.writeValueAsString(requestBodyMap);
		PlatformLogger.log("Updating Word enrich | URL: " + url + " | Request body: " + requestBody);
		HTTPUtil.makePostRequest(url, requestBody);
		PlatformLogger.log("Word enriched for the lemma change successfully - wordId :"+ wordId);

		} catch (Exception e) {
			PlatformLogger.log("error when calling enrich api Language Id:" + languageId + " word :" + wordId + ",error",null, e);
		}

	}
}