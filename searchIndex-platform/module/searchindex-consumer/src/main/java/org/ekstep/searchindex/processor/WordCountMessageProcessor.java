package org.ekstep.searchindex.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.WordUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.searchindex.util.ConsumerUtil;

import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

public class WordCountMessageProcessor implements IMessageProcessor {

	ConsumerUtil consumerUtil = new ConsumerUtil();
	private ObjectMapper mapper = new ObjectMapper();
	private Timer timer;
	private boolean messageProcessed = false;
	private Map<String, Map<String, Integer>> wordsCountMap = new ConcurrentHashMap<String, Map<String, Integer>>();
	private int BATCH_TIME_IN_SECONDS = 60;
	
	public WordCountMessageProcessor() {
		super();
	}

	public void processMessage(String messageData) {
		try {
			Map<String, Object> message = mapper.readValue(messageData, new TypeReference<Map<String, Object>>() {
			});
			processMessage(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createTimer(int seconds) {
		if(timer == null){
			timer = new Timer();
			timer.schedule(new PushTask(), seconds * 1000);
		}
	}

	class PushTask extends TimerTask {
		public void run() {
			System.out.println("Time's up!");
			try {
				updateWordsCount();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void updateWordsCount() throws Exception {
		messageProcessed = true;
		for(Map.Entry<String, Map<String, Integer>> entry: wordsCountMap.entrySet()){
			String languageId = entry.getKey();
			 Map<String, Integer> wordsCountObj = entry.getValue();
			 Integer wordsCount = wordsCountObj.get("wordsCount");
			 Integer liveWordsCount = wordsCountObj.get("liveWordsCount");
			 String url = consumerUtil.getConsumerConfig().consumerInit.ekstepPlatformURI +"/v1/language/dictionary/updateWordCount/"+languageId;
			
			 Map<String, Object> requestBodyMap = new HashMap<String, Object>();
			 Map<String, Object> requestMap = new HashMap<String, Object>();
			 requestMap.put("wordCount", wordsCount);
			 requestMap.put("liveWordCount", liveWordsCount);
			 requestBodyMap.put("request", requestMap);
			 
			 String requestBody = mapper.writeValueAsString(requestBodyMap);
			 
			 consumerUtil.makeHttpPostRequest(url, requestBody);
			 
			wordsCountObj.put("wordsCount", new Integer(0));
			wordsCountObj.put("liveWordsCount", new Integer(0));
			wordsCountMap.put(languageId, wordsCountObj);
		}
		
		if(messageProcessed){
			timer.cancel();
			timer = null;
		}
		else{
			timer.schedule(new PushTask(), BATCH_TIME_IN_SECONDS * 1000);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void processMessage(Map<String, Object> message) throws Exception {
		if (message != null && message.get("operationType") != null) {
			String nodeType = (String) message.get("nodeType");
			String objectType = (String) message.get("objectType");
			objectType = WordUtils.capitalize(objectType.toLowerCase());
			String languageId = (String) message.get("graphId");
			if (objectType.equalsIgnoreCase(CompositeSearchConstants.OBJECT_TYPE_WORD)) {
				Map<String, Integer> wordsCountObj = wordsCountMap.get(languageId);
				if(wordsCountObj == null){
					wordsCountObj = new HashMap<String, Integer>();
					wordsCountObj.put("wordsCount", new Integer(0));
					wordsCountObj.put("liveWordsCount", new Integer(0));
					wordsCountMap.put(languageId, wordsCountObj);
				}
				
				Integer wordsCount = wordsCountObj.get("wordsCount");
				Integer liveWordsCount = wordsCountObj.get("liveWordsCount");
				
				switch (nodeType) {
				case CompositeSearchConstants.NODE_TYPE_DATA: {
					String operationType = (String) message.get("operationType");
					switch (operationType) {
					case CompositeSearchConstants.OPERATION_CREATE: {
						wordsCount = wordsCount + 1;
						Map transactionData = (Map) message.get("transactionData");
						if (transactionData != null) {
							Map<String, Object> addedProperties = (Map<String, Object>) transactionData
									.get("addedProperties");
							if (addedProperties != null && !addedProperties.isEmpty()) {
								for (Map.Entry<String, Object> propertyMap : addedProperties.entrySet()) {
									if (propertyMap != null && propertyMap.getKey() != null) {
										String propertyName = (String) propertyMap.getKey();
										String propertyValue = (String) propertyMap.getValue();
										if (propertyName.equalsIgnoreCase("status")) {
											if (propertyValue.equalsIgnoreCase("Live")) {
												liveWordsCount = liveWordsCount + 1;
											} 
										}
									}
								}
							}
						}
						
						break;
					}
					case CompositeSearchConstants.OPERATION_UPDATE: {
						Map transactionData = (Map) message.get("transactionData");
						if (transactionData != null) {
							Map<String, Object> addedProperties = (Map<String, Object>) transactionData
									.get("addedProperties");
							if (addedProperties != null && !addedProperties.isEmpty()) {
								for (Map.Entry<String, Object> propertyMap : addedProperties.entrySet()) {
									if (propertyMap != null && propertyMap.getKey() != null) {
										String propertyName = (String) propertyMap.getKey();
										String propertyValue = (String) propertyMap.getValue();
										if (propertyName.equalsIgnoreCase("status")) {
											if (propertyValue.equalsIgnoreCase("Live")) {
												liveWordsCount = liveWordsCount + 1;
											} else {
												liveWordsCount = liveWordsCount - 1;
											}
										}
									}
								}
							}
						}
						break;
					}
					case CompositeSearchConstants.OPERATION_DELETE: {
						wordsCount = wordsCount - 1;
						Map transactionData = (Map) message.get("transactionData");
						if (transactionData != null) {
							Map<String, Object> addedProperties = (Map<String, Object>) transactionData
									.get("addedProperties");
							if (addedProperties != null && !addedProperties.isEmpty()) {
								for (Map.Entry<String, Object> propertyMap : addedProperties.entrySet()) {
									if (propertyMap != null && propertyMap.getKey() != null) {
										String propertyName = (String) propertyMap.getKey();
										String propertyValue = (String) propertyMap.getValue();
										if (propertyName.equalsIgnoreCase("status")) {
											if (propertyValue.equalsIgnoreCase("Live")) {
												liveWordsCount = liveWordsCount - 1;
											}
										}
									}
								}
							}
						}
						break;
					}
					}
					break;
				}
				}
				
				wordsCountObj.put("wordsCount", wordsCount);
				wordsCountObj.put("liveWordsCount", liveWordsCount);
				wordsCountMap.put(languageId, wordsCountObj);
				createTimer(BATCH_TIME_IN_SECONDS);
				messageProcessed = false;
				System.out.println("Message processed");
			}
		}
	}

	public static void main(String arg[]) throws Exception {
		WordCountMessageProcessor processor = new WordCountMessageProcessor();
		JSONBuilder builder = new JSONStringer();

		/*
		 * builder.object().key("operationType").value(Constants.
		 * OPERATION_CREATE).key("graphId").value("hi")
		 * .key("nodeGraphId").value("2").key("nodeUniqueId").value("hi_2").key(
		 * "objectType")
		 * .value(Constants.OBJECT_TYPE_WORD).key("nodeType").value(Constants.
		 * NODE_TYPE_DATA) .key("transactionData").object()
		 * .key("addedProperties").array().object()
		 * .key("propertyName").value("lemma") .key("value").value("Hi 2")
		 * .endObject() .endArray() .endObject() .endObject();
		 */

		/*
		 * builder.object().key("operationType").value(Constants.
		 * OPERATION_CREATE).key("graphId").value("hi")
		 * .key("nodeGraphId").value("1").key("nodeUniqueId").value("hi_s_1").
		 * key( "objectType")
		 * .value(Constants.OBJECT_TYPE_SYNSET).key("nodeType").value(Constants.
		 * NODE_TYPE_DATA) .key("transactionData").object()
		 * .key("addedProperties").array().object()
		 * .key("propertyName").value("gloss") .key("value").value(
		 * "Hi how are you") .endObject() .endArray() .endObject() .endObject();
		 */

		/*
		 * builder.object().key("operationType").value(Constants.
		 * OPERATION_DELETE).key("graphId").value("hi")
		 * .key("nodeGraphId").value("1").key("nodeUniqueId").value("hi_2").key(
		 * "objectType")
		 * .value(Constants.OBJECT_TYPE_WORD).key("nodeType").value(Constants.
		 * NODE_TYPE_DATA).endObject();
		 */

		builder.object().key("operationType").value(CompositeSearchConstants.OPERATION_CREATE).key("graphId")
				.value("hi").key("nodeGraphId").value("1").key("nodeUniqueId").value("hi_2").key("objectType")
				.value(CompositeSearchConstants.OBJECT_TYPE_WORD).key("nodeType")
				.value(CompositeSearchConstants.NODE_TYPE_DATA).endObject();

		/*
		 * builder.object().key("operationType").value(Constants.
		 * OPERATION_UPDATE).key("graphId").value("hi")
		 * .key("nodeGraphId").value("1").key("nodeUniqueId").value("hi_1").key(
		 * "objectType")
		 * .value(Constants.OBJECT_TYPE_WORD).key("nodeType").value(Constants.
		 * NODE_TYPE_DATA)
		 * .key("transactionData").object().key("addedProperties").array().
		 * object().key("propertyName")
		 * .value("notappli").key("value").array().value("class 1"
		 * ).value("rwo").endArray().endObject()
		 * .endArray().key("removedProperties").array().value("sourceTypes").
		 * endArray().key("addedTags").array() .value("grade one"
		 * ).endArray().key("removedTags").array().value("grade three"
		 * ).endArray().endObject() .endObject();
		 */
		Map<String, Object> message = processor.mapper.readValue(builder.toString(),
				new TypeReference<Map<String, Object>>() {
				});
		processor.processMessage(message);
	}
}
