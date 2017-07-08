package org.ekstep.searchindex.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.searchindex.util.ConsumerUtil;
import org.ekstep.searchindex.util.HTTPUtil;
import org.ekstep.searchindex.util.PropertiesUtil;

import com.ilimi.common.logger.LogHelper;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.common.util.PlatformLogger;

import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

public class WordCountMessageProcessor implements IMessageProcessor {

	private static ILogger LOGGER = PlatformLogManager.getLogger();
	ConsumerUtil consumerUtil = new ConsumerUtil();
	private ObjectMapper mapper = new ObjectMapper();
	private Timer timer;
	private AtomicBoolean timerTaskDone = new AtomicBoolean(true);
	@SuppressWarnings("unused")
	private boolean messageProcessed = false;
	private Map<String, Map<String, Integer>> wordsCountMap = new ConcurrentHashMap<String, Map<String, Integer>>();
	@SuppressWarnings("unused")
	private int BATCH_TIME_IN_SECONDS = 60;
	
	public WordCountMessageProcessor() {
		super();
	}

	public void processMessage(String messageData) {
		try {
			LOGGER.log("Processing message: ");
			Map<String, Object> message = mapper.readValue(messageData, new TypeReference<Map<String, Object>>() {
			});
			processMessage(message);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.log("Exception", e.getMessage(), e);
		}
	}

	public void createTimer(int seconds) {
		if(timer == null){
			timer = new Timer();
			timer.schedule(new PushTask(), seconds * 1000);
			timerTaskDone.set(false);
		} else if (!timerTaskDone.get()) {
		    timer.schedule(new PushTask(), seconds * 1000);
		}
	}

	class PushTask extends TimerTask {
		public void run() {
			//System.out.println("Time's up!");
			try {
				updateWordsCount();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void updateWordsCount() throws Exception {
		messageProcessed = true;
		LOGGER.log("Updating wordcount");
		for(Map.Entry<String, Map<String, Integer>> entry: wordsCountMap.entrySet()){
			String languageId = entry.getKey();
			 Map<String, Integer> wordsCountObj = entry.getValue();
			 Integer wordsCount = wordsCountObj.get("wordsCount");
			 Integer liveWordsCount = wordsCountObj.get("liveWordsCount");
			 String url = PropertiesUtil.getProperty("platform-api-url") +"/v1/language/dictionary/updateWordCount/"+languageId;
			
			 Map<String, Object> requestBodyMap = new HashMap<String, Object>();
			 Map<String, Object> requestMap = new HashMap<String, Object>();
			 requestMap.put("wordCount", wordsCount);
			 requestMap.put("liveWordCount", liveWordsCount);
			 requestBodyMap.put("request", requestMap);
			 
			 String requestBody = mapper.writeValueAsString(requestBodyMap);
			 LOGGER.log("Updating Word Count | URL: " + url , " | Request body: " + requestBody);
			 
			 HTTPUtil.makePostRequest(url, requestBody);
			 
			 LOGGER.log("Word Count updated");
			 
			wordsCountObj.put("wordsCount", new Integer(0));
			wordsCountObj.put("liveWordsCount", new Integer(0));
			wordsCountMap.put(languageId, wordsCountObj);
		}
//		if(messageProcessed){
//			timer.cancel();
//			timer = null;
//			timerTaskDone.set(true);
//		}
//		else{
//			timer.schedule(new PushTask(), BATCH_TIME_IN_SECONDS * 1000);
//			timerTaskDone.set(false);
//		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void processMessage(Map<String, Object> message) throws Exception {
		if (message != null && message.get("operationType") != null) {
			String nodeType = (String) message.get("nodeType");
			String objectType = (String) message.get("objectType");
			//objectType = WordUtils.capitalize(objectType.toLowerCase());
			String languageId = (String) message.get("graphId");
			if (StringUtils.equalsIgnoreCase(CompositeSearchConstants.OBJECT_TYPE_WORD, objectType)) {
				LOGGER.log("Processing message for Word object type");
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
						LOGGER.log("Word create operation: " , wordsCount);
						Map transactionData = (Map) message.get("transactionData");
						if (transactionData != null) {
							Map<String, Object> addedProperties = (Map<String, Object>) transactionData
									.get("properties");
							if (addedProperties != null && !addedProperties.isEmpty()) {
								for (Map.Entry<String, Object> propertyMap : addedProperties.entrySet()) {
									if (propertyMap != null && propertyMap.getKey() != null) {
										String propertyName = (String) propertyMap.getKey();
										if (propertyName.equalsIgnoreCase("status")) {
											String propertyValue = (String) ((Map<String, Object>) propertyMap.getValue()).get("nv");//new value
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
									.get("properties");
							if (addedProperties != null && !addedProperties.isEmpty()) {
								for (Map.Entry<String, Object> propertyMap : addedProperties.entrySet()) {
									if (propertyMap != null && propertyMap.getKey() != null) {
										String propertyName = (String) propertyMap.getKey();
										if (propertyName.equalsIgnoreCase("status")) {
											String propertyValue = (String) ((Map<String, Object>) propertyMap.getValue()).get("nv");//new value											
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
						LOGGER.log("Word update operation: " , liveWordsCount);
						break;
					}
					case CompositeSearchConstants.OPERATION_DELETE: {
						wordsCount = wordsCount - 1;
						LOGGER.log("Word delete operation: " + wordsCount);
						Map transactionData = (Map) message.get("transactionData");
						if (transactionData != null) {
							Map<String, Object> addedProperties = (Map<String, Object>) transactionData
									.get("properties");
							if (addedProperties != null && !addedProperties.isEmpty()) {
								for (Map.Entry<String, Object> propertyMap : addedProperties.entrySet()) {
									if (propertyMap != null && propertyMap.getKey() != null) {
										String propertyName = (String) propertyMap.getKey();
										if (propertyName.equalsIgnoreCase("status")) {
											String propertyValue = (String) ((Map<String, Object>) propertyMap.getValue()).get("ov");//old value
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
					wordsCountObj.put("wordsCount", wordsCount);
					wordsCountObj.put("liveWordsCount", liveWordsCount);
					wordsCountMap.put(languageId, wordsCountObj);
					//createTimer(BATCH_TIME_IN_SECONDS);
					messageProcessed = false;
					LOGGER.log("Word count message processor: " + wordsCount + " | " + liveWordsCount);
					updateWordsCount();
					//System.out.println("Word count message processor: " + wordsCount + " | " + liveWordsCount);
					break;
				}
				}
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
