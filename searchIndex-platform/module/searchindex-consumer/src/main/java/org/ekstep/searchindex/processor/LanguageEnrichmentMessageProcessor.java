package org.ekstep.searchindex.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.searchindex.util.HTTPUtil;
import org.ekstep.searchindex.util.PropertiesUtil;

import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.model.Node;

/**
 * The Class LanguageEnrichmentMessageProcessor is a kafka consumer which
 * provides implementations of the core language feature extraction operations
 * defined in the IMessageProcessor along with the methods to implement content
 * enrichment from language model with additional metadata
 * 
 * @author Rashmi
 *
 */
public class LanguageEnrichmentMessageProcessor extends BaseProcessor implements IMessageProcessor {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(LanguageEnrichmentMessageProcessor.class.getName());

	/** The ObjectMapper */
	private static ObjectMapper mapper = new ObjectMapper();

	/** The Controller Utility */
	private static ControllerUtil util = new ControllerUtil();

	/** The constructor */
	public LanguageEnrichmentMessageProcessor() {
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
			LOGGER.info("Reading from kafka consumer" + messageData);
			Map<String, Object> message = new HashMap<String, Object>();
			if (StringUtils.isNotBlank(messageData)) {
				LOGGER.debug("checking if kafka message is blank or not" + messageData);
				message = mapper.readValue(messageData, new TypeReference<Map<String, Object>>() {
				});
			}
			if (null != message) {
				String eid = (String) message.get("eid");
				if (StringUtils.isNotBlank(eid) && StringUtils.equals("BE_CONTENT_LIFECYCLE", eid))
					processMessage(message);
			}
		} catch (Exception e) {
			LOGGER.error("Error while processing kafka message", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.searchindex.processor #processMessage(java.lang.String
	 * java.lang.String, java.io.File, java.lang.String)
	 */
	@SuppressWarnings("unused")
	@Override
	public void processMessage(Map<String, Object> message) throws Exception {

		LOGGER.info("filtering out the kafka message" + message);
		Node node = filterMessage(message);

		LOGGER.info("checking if node is null" + node);
		if (null != node) {
			String languageId = null;
			LOGGER.info("getting languageId");
			String language = getLanguage(node);

			LOGGER.info("checking if language is null or not");
			if (StringUtils.isNotBlank(language)) {
				LOGGER.info("calling initCheck method");
				initCheck(node, language);
			}
		}
	}

	/**
	 * This method holds logic to fetch text tag from node and get complexities
	 * from it
	 * 
	 * @param node
	 *            The content node
	 * 
	 * @param languageId
	 *            The languageId
	 * 
	 * @throws Exception
	 */
	private void initCheck(Node node, String languageId) throws Exception {

		LOGGER.info("checking if node contains text tag" + node.getMetadata().containsKey("text"));
		if (null != node.getMetadata().get("text")) {
			Object object = node.getMetadata().get("text");

			LOGGER.info("instance check");
			if (object instanceof String[]) {
				LOGGER.info("fetched object is an string array");
				String[] textArray = (String[]) object;
				String text = Arrays.toString(textArray);
				text = text.replaceAll("\\[|\\]", "");
				processData(text, languageId, node);

			} else if (object instanceof String) {
				LOGGER.info("fetched object is a string");
				String text = object.toString();
				processData(text, languageId, node);
			}
		}
	}

	/**
	 * This method holds logic to fethc complexity measures and update them on
	 * node
	 * 
	 * @param text
	 *            The text tag
	 * 
	 * @param languageId
	 *            The languageId
	 * 
	 * @param node
	 *            The content node
	 */
	private void processData(String text, String languageId, Node node) {

		LOGGER.info("calling get complexity measures to get text_complexity" + text + languageId);
		if (StringUtils.isNotBlank(text) && StringUtils.isNotBlank(languageId)) {

			Map<String, Object> result = new HashMap<String, Object>();
			try {
				result = getComplexityMeasures(languageId, text);
				LOGGER.info("complexity measures result" + result);

				if (null != result && !result.isEmpty()) {
					LOGGER.info("mapping complexity measures with node metadata and updating the node");
					updateComplexityMeasures(node, result);
				}

			} catch (Exception e) {
				LOGGER.error("Exception occured while getting complexity measures", e);
			}
		}
	}

	/**
	 * This method holds logic to make a rest API call to language actor to get
	 * complexity measures for a given text
	 * 
	 * @param languageId
	 *            The languageId
	 * 
	 * @param Text
	 *            The text to be analysed
	 * 
	 * @return result The complexity measures result
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<String, Object> getComplexityMeasures(String languageId, String text) throws Exception {

		LOGGER.info("getting language_api_url from properties to make rest call");
		String api_url = PropertiesUtil.getProperty("language-api-url") + "/v1/language/tools/complexityMeasures/text";
		LOGGER.info("api url to make rest api call to complexity measures api" + api_url);

		Map<String, Object> request_map = new HashMap<String, Object>();
		Map<String, Object> requestObj = new HashMap<String, Object>();
		request_map.put("language_id", languageId);
		request_map.put("text", text);
		requestObj.put("request", request_map);

		LOGGER.info("creating request map to make rest post call" + requestObj);
		String request = mapper.writeValueAsString(requestObj);

		LOGGER.info("making a post call to get complexity measures for a given text");
		String result = HTTPUtil.makePostRequest(api_url, request);

		LOGGER.info("response from complexity measures api" + result);
		Map<String, Object> responseObject = mapper.readValue(result, new TypeReference<Map<String, Object>>() {
		});
		LOGGER.info("converting result string to response map" + responseObject);

		if (responseObject == null) {
			throw new Exception("Unable to get complexity measures");
		}

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap = (Map) responseObject.get("result");

		LOGGER.info("getting resultMap from response Object" + resultMap);
		if (resultMap == null) {
			throw new Exception("Result in response is empty");
		}
		return resultMap;
	}

	/**
	 * This method holds logic to extract required fields from complexity
	 * measures response and update it as node metadata
	 * 
	 * @param node
	 *            The node
	 * 
	 * @param result
	 *            The result map
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void updateComplexityMeasures(Node node, Map result) {

		try {
			LOGGER.info("extracting required fields from result map" + result);
			if (null != result.get("text_complexity")) {

				LOGGER.info("Getting text_complexity map from result object" + result.containsKey("text_complexity"));
				Map text_complexity = (Map) result.get("text_complexity");

				if (null != text_complexity && !text_complexity.isEmpty()) {

					LOGGER.info("checking if text complexity map contains wordCount");
					if (text_complexity.containsKey("wordCount")) {

						LOGGER.info("Checking if wordCount is null");
						if (null == text_complexity.get("wordCount")) {
							node.getMetadata().put("wordCount", null);
						} else {
							Object wordCount = text_complexity.get("wordCount");
							LOGGER.info("setting node metadata with integer value" + wordCount);
							node.getMetadata().put("wordCount", wordCount);
						}
					}

					LOGGER.info("checking if text complexity map contains syllableCount");
					if (text_complexity.containsKey("syllableCount")) {

						LOGGER.info("checking if syllable count is null");
						if (null == text_complexity.get("syllableCount")) {
							node.getMetadata().put("syllableCount", null);
						} else {
							Object syllableCount = text_complexity.get("syllableCount");
							LOGGER.info("updating node with syllableCount" + syllableCount);
							node.getMetadata().put("syllableCount", syllableCount);
						}
					}

					LOGGER.info("checking if text complexity map contains totalOrthoComplexity");
					if (text_complexity.containsKey("totalOrthoComplexity")) {

						LOGGER.info("checking if totalOrthoComplexity is null");
						if (null == text_complexity.get("totalOrthoComplexity")) {
							node.getMetadata().put("totalOrthoComplexity", null);
						} else {
							Object totalOrthoComplexity = text_complexity.get("totalOrthoComplexity");
							LOGGER.info("updating node with totalOrthoComplexity"
									+ text_complexity.get("totalOrthoComplexity"));
							node.getMetadata().put("totalOrthoComplexity", totalOrthoComplexity);
						}
					}

					LOGGER.info("checking if text complexity map contains totalPhonicComplexity");
					if (text_complexity.containsKey("totalPhonicComplexity")) {

						LOGGER.info("checking if totalPhonicComplexity is null");
						if (null == text_complexity.get("totalPhonicComplexity")) {
							node.getMetadata().put("totalPhonicComplexity", null);
						} else {
							Object totalPhonicComplexity = text_complexity.get("totalPhonicComplexity");
							LOGGER.info("updating node with totalPhonicComplexity"
									+ text_complexity.get("totalPhonicComplexity"));
							node.getMetadata().put("totalPhonicComplexity", totalPhonicComplexity);
						}
					}

					LOGGER.info("checking if text complexity map contains totalWordComplexity");
					if (text_complexity.containsKey("totalWordComplexity")) {

						LOGGER.info("checking if totalWordComplexity is null");
						if (null == text_complexity.get("totalWordComplexity")) {
							node.getMetadata().put("totalWordComplexity", null);
						} else {
							Object totalWordComplexity = text_complexity.get("totalWordComplexity");
							LOGGER.info("updating node with totalWordComplexity"
									+ text_complexity.get("totalWordComplexity"));
							node.getMetadata().put("totalWordComplexity", totalWordComplexity);
						}
					}

					LOGGER.info("checking if text complexity map contains gradeLevel");
					if (text_complexity.containsKey("gradeLevels")) {

						List<Map<String, Object>> list = (List) text_complexity.get("gradeLevels");
						LOGGER.info("calling upadeGraph method");
						node = updateGradeMap(node, list);
					}

					LOGGER.info("checking if text complexity map contains Themes");
					if (text_complexity.containsKey("themes")) {

						LOGGER.info("checking if complexity_map contains themes");
						if (null == text_complexity.get("themes")) {
							node.getMetadata().put("themes", null);
						} else {
							LOGGER.info("extracting themes from complexity map");
							Map<String, Integer> themes = (Map<String, Integer>) text_complexity.get("themes");
							List<String> themeList = ListUtils.EMPTY_LIST;
							if (themes != null && themes.size() > 0)
								themeList = new ArrayList<>(themes.keySet());
							node.getMetadata().put("themes", themeList);
						}
					}

					LOGGER.info("checking if text complexity map contains partsOfSpeech");
					if (text_complexity.containsKey("partsOfSpeech")) {

						LOGGER.info("checking if complexity_map contains partsOfSpeech");
						if (null == text_complexity.get("partsOfSpeech")) {
							node.getMetadata().put("partsOfSpeech", null);
						} else {
							LOGGER.info("extracting partsOfSpeech from complexity map");
							Object partsOfSpeech = text_complexity.get("partsOfSpeech");
							node.getMetadata().put("partsOfSpeech", partsOfSpeech);
						}
					}

					LOGGER.info("checking if text complexity map contains thresholdVocabulary");
					if (text_complexity.containsKey("thresholdVocabulary")) {

						LOGGER.info("checking if complexity_map contains thresholdVocabulary");
						if (null == text_complexity.get("thresholdVocabulary")) {
							node.getMetadata().put("thresholdVocabulary", null);
						} else {
							LOGGER.info("extracting top5 from complexity thresholdVocabulary");
							Object thresholdVocabulary = text_complexity.get("thresholdVocabulary");
							node.getMetadata().put("thresholdVocabulary", thresholdVocabulary);
						}
					}

					LOGGER.info("checking if text complexity map contains nonThresholdVocabulary");
					if (text_complexity.containsKey("nonThresholdVocabulary")) {

						LOGGER.info("checking if complexity_map contains nonThresholdVocabulary");
						if (null == text_complexity.get("nonThresholdVocabulary")) {
							node.getMetadata().put("nonThresholdVocabulary", null);
						} else {
							LOGGER.info("extracting top5 from complexity nonThresholdVocabulary");
							Object nonThresholdVocabulary = text_complexity.get("nonThresholdVocabulary");
							node.getMetadata().put("nonThresholdVocabulary", nonThresholdVocabulary);
						}
					}

					LOGGER.info("checking if text complexity map contains top5");
					if (text_complexity.containsKey("top5")) {

						LOGGER.info("checking if complexity_map contains top5");
						if (null == text_complexity.get("top5")) {
							node.getMetadata().put("top5", null);
						} else {
							LOGGER.info("extracting top5 from complexity map");
							Object top5 = text_complexity.get("top5");
							node.getMetadata().put("top5", top5);
						}
					}

					LOGGER.info("checking if text complexity map contains totalWordCount");
					if (text_complexity.containsKey("totalWordCount")) {

						LOGGER.info("Checking if totalWordCount is null");
						if (null == text_complexity.get("totalWordCount")) {
							node.getMetadata().put("totalWordCount", null);
						} else {
							Object totalWordCount = text_complexity.get("totalWordCount");
							LOGGER.info("setting node metadata with integer value" + totalWordCount);
							node.getMetadata().put("totalWordCount", totalWordCount);
						}
					}

					LOGGER.info("updating node with extracted language metadata" + node.getMetadata().toString());
					node.setOutRelations(null);
					node.setInRelations(null);
					Response response = util.updateNode(node);
					LOGGER.info("response of content update" + response.getResponseCode());
				}
			}
		} catch (Exception e) {
			LOGGER.error("exception occured while setting language metadata" + e);
		}
	}

	/**
	 * This method holds logic to get grades from complexity measures API and
	 * map them into node metadata
	 * 
	 * @param node
	 *            The content node
	 * 
	 * @param text_complexity
	 *            The complexity map
	 * 
	 * @return The updated content node
	 */
	private Node updateGradeMap(Node node, List<Map<String, Object>> gradeList) {

		LOGGER.info("Checking if gradeLevel from node is empty");
		if (null == node.getMetadata().get("gradeLevel")) {
			List<String> gradeLevel = new ArrayList<String>();

			LOGGER.info("Checking if grades from complexity map is empty");
			if (null != gradeList) {

				LOGGER.info("Iterating through the grades from complexity map");
				for (Map<String, Object> map : gradeList) {

					LOGGER.info("checking if map contains grade");
					if (map.containsKey("grade")) {
						String grade = (String) map.get("grade");

						LOGGER.info("Checking if grade is not there in gradeLevel from node and adding it" + grade);
						if (!gradeLevel.contains(grade) && StringUtils.isNotBlank(grade)) {
							gradeLevel.add(grade);
							LOGGER.info("Checking if grade is not there in gradeLevel from node and adding it"
									+ gradeLevel);
							node.getMetadata().put("gradeLevel", gradeLevel);
						}
					}
				}

			}
		} else {
			String[] grade_array = (String[]) node.getMetadata().get("gradeLevel");

			List<String> gradeLevel = new ArrayList<String>();
			LOGGER.info("Checking if gradeLevel from node is empty");
			if (null != grade_array) {

				LOGGER.info("adding grades from node to list" + grade_array);
				for (String str : grade_array) {
					gradeLevel.add(str);
				}

				LOGGER.info("Checking if grades from complexity map is empty");
				if (!gradeList.isEmpty()) {

					LOGGER.info("Iterating through the grades from complexity map");
					for (Map<String, Object> map : gradeList) {

						LOGGER.info("checking if map contains grade");
						if (map.containsKey("grade")) {
							String grade = (String) map.get("grade");

							LOGGER.info("Checking if grade is not there in gradeLevel from node and adding it" + grade);
							if (!gradeLevel.contains(grade) && StringUtils.isNotBlank(grade)) {
								gradeLevel.add(grade);

								LOGGER.info("Updating node with gradeLevel" + gradeLevel);
								node.getMetadata().put("gradeLevel", gradeLevel);
							}
						}
					}
				}
			}
		}
		return node;
	}
}