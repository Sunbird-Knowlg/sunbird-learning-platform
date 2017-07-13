package org.ekstep.searchindex.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.searchindex.util.HTTPUtil;
import org.ekstep.searchindex.util.PropertiesUtil;

import com.ilimi.common.dto.Response;
import com.ilimi.common.logger.LoggerEnum;
import com.ilimi.common.logger.PlatformLogger;
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
			
			Map<String, Object> message = new HashMap<String, Object>();
			if (StringUtils.isNotBlank(messageData)) {
				message = mapper.readValue(messageData, new TypeReference<Map<String, Object>>() {
				});
				if (null != message) {
					String eid = (String) message.get("eid");
					if (StringUtils.isNotBlank(eid) && StringUtils.equals("BE_OBJECT_LIFECYCLE", eid))
						processMessage(message);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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

		Node node = filterMessage(message);
		if (null != node) {
			String languageId = null;
			String language = getLanguage(node);

			if (StringUtils.isNotBlank(language)) {
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

		PlatformLogger.log("checking if node contains text tag: " + node.getMetadata().containsKey("text"));
		if (null != node.getMetadata().get("text")) {
			Object object = node.getMetadata().get("text");

			if (object instanceof String[]) {
				String[] textArray = (String[]) object;
				String text = Arrays.toString(textArray);
				text = text.replaceAll("\\[|\\]", "");
				processData(text, languageId, node);

			} else if (object instanceof String) {
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

		PlatformLogger.log("calling get complexity measures to get text_complexity" + text + languageId);
		if (StringUtils.isNotBlank(text) && StringUtils.isNotBlank(languageId)) {

			Map<String, Object> result = new HashMap<String, Object>();
			try {
				result = getComplexityMeasures(languageId, text);
				PlatformLogger.log("complexity measures result" + result.size(), null , LoggerEnum.INFO.name());

				if (null != result && !result.isEmpty()) {
					PlatformLogger.log("mapping complexity measures with node metadata and updating the node");
					updateComplexityMeasures(node, result);
				}

			} catch (Exception e) {
				PlatformLogger.log("Exception occured while getting complexity measures", e.getMessage(), e);
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

		String api_url = PropertiesUtil.getProperty("language-api-url") + "/v1/language/tools/complexityMeasures/text";

		Map<String, Object> request_map = new HashMap<String, Object>();
		Map<String, Object> requestObj = new HashMap<String, Object>();
		request_map.put("language_id", languageId);
		request_map.put("text", text);
		requestObj.put("request", request_map);

		String request = mapper.writeValueAsString(requestObj);
		String result = HTTPUtil.makePostRequest(api_url, request);

		Map<String, Object> responseObject = mapper.readValue(result, new TypeReference<Map<String, Object>>() {
		});

		if (responseObject == null) {
			throw new Exception("Unable to get complexity measures");
		}

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap = (Map) responseObject.get("result");

		PlatformLogger.log("getting resultMap from response Object");
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
			if (null != result.get("text_complexity")) {
				Map text_complexity = (Map) result.get("text_complexity");

				if (null != text_complexity && !text_complexity.isEmpty()) {
					if (text_complexity.containsKey("wordCount")) {
						if (null == text_complexity.get("wordCount")) {
							node.getMetadata().put("wordCount", null);
						} else {
							Object wordCount = text_complexity.get("wordCount");
							node.getMetadata().put("wordCount", wordCount);
						}
					}

					if (text_complexity.containsKey("syllableCount")) {
						if (null == text_complexity.get("syllableCount")) {
							node.getMetadata().put("syllableCount", null);
						} else {
							Object syllableCount = text_complexity.get("syllableCount");
							node.getMetadata().put("syllableCount", syllableCount);
						}
					}

					if (text_complexity.containsKey("totalOrthoComplexity")) {

						if (null == text_complexity.get("totalOrthoComplexity")) {
							node.getMetadata().put("totalOrthoComplexity", null);
						} else {
							Object totalOrthoComplexity = text_complexity.get("totalOrthoComplexity");
							node.getMetadata().put("totalOrthoComplexity", totalOrthoComplexity);
						}
					}

					if (text_complexity.containsKey("totalPhonicComplexity")) {

						if (null == text_complexity.get("totalPhonicComplexity")) {
							node.getMetadata().put("totalPhonicComplexity", null);
						} else {
							Object totalPhonicComplexity = text_complexity.get("totalPhonicComplexity");
							node.getMetadata().put("totalPhonicComplexity", totalPhonicComplexity);
						}
					}
			
					if (text_complexity.containsKey("totalWordComplexity")) {
						if (null == text_complexity.get("totalWordComplexity")) {
							node.getMetadata().put("totalWordComplexity", null);
						} else {
							Object totalWordComplexity = text_complexity.get("totalWordComplexity");
							
							node.getMetadata().put("totalWordComplexity", totalWordComplexity);
						}
					}

					if (text_complexity.containsKey("gradeLevels")) {

						List<Map<String, Object>> list = (List) text_complexity.get("gradeLevels");
						node = updateGradeMap(node, list);
					}

					if (text_complexity.containsKey("themes")) {
						if (null == text_complexity.get("themes")) {
							node.getMetadata().put("themes", null);
						} else {
							Map<String, Integer> themes = (Map<String, Integer>) text_complexity.get("themes");
							List<String> themeList = ListUtils.EMPTY_LIST;
							if (themes != null && themes.size() > 0)
								themeList = new ArrayList<>(themes.keySet());
							node.getMetadata().put("themes", themeList);
						}
					}
					if (text_complexity.containsKey("partsOfSpeech")) {
						if (null == text_complexity.get("partsOfSpeech")) {
							node.getMetadata().put("partsOfSpeech", null);
						} else {
							Object partsOfSpeech = text_complexity.get("partsOfSpeech");
							node.getMetadata().put("partsOfSpeech", partsOfSpeech);
						}
					}

					if (text_complexity.containsKey("thresholdVocabulary")) {
						if (null == text_complexity.get("thresholdVocabulary")) {
							node.getMetadata().put("thresholdVocabulary", null);
						} else {
							Object thresholdVocabulary = text_complexity.get("thresholdVocabulary");
							node.getMetadata().put("thresholdVocabulary", thresholdVocabulary);
						}
					}
					if (text_complexity.containsKey("nonThresholdVocabulary")) {

						if (null == text_complexity.get("nonThresholdVocabulary")) {
							node.getMetadata().put("nonThresholdVocabulary", null);
						} else {
							Object nonThresholdVocabulary = text_complexity.get("nonThresholdVocabulary");
							node.getMetadata().put("nonThresholdVocabulary", nonThresholdVocabulary);
						}
					}

					if (text_complexity.containsKey("top5")) {

						PlatformLogger.log("checking if complexity_map contains top5");
						if (null == text_complexity.get("top5")) {
							node.getMetadata().put("top5", null);
						} else {
							PlatformLogger.log("extracting top5 from complexity map");
							Object top5 = text_complexity.get("top5");
							node.getMetadata().put("top5", top5);
						}
					}
					if (text_complexity.containsKey("totalWordCount")) {

						if (null == text_complexity.get("totalWordCount")) {
							node.getMetadata().put("totalWordCount", null);
						} else {
							Object totalWordCount = text_complexity.get("totalWordCount");
							PlatformLogger.log("setting node metadata with integer value" + totalWordCount);
							node.getMetadata().put("totalWordCount", totalWordCount);
						}
					}

					node.setOutRelations(null);
					node.setInRelations(null);
					Response response = util.updateNode(node);
				}
			}
		} catch (Exception e) {
			PlatformLogger.log("exception occured while setting language metadata" , e.getMessage(), e);
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
		if (null == node.getMetadata().get("gradeLevel")) {
			List<String> gradeLevel = new ArrayList<String>();
			if (null != gradeList) {
				for (Map<String, Object> map : gradeList) {

					if (map.containsKey("grade")) {
						String grade = (String) map.get("grade");
						if (!gradeLevel.contains(grade) && StringUtils.isNotBlank(grade)) {
							gradeLevel.add(grade);
							node.getMetadata().put("gradeLevel", gradeLevel);
						}
					}
				}

			}
		} else {
			String[] grade_array = (String[]) node.getMetadata().get("gradeLevel");
			List<String> gradeLevel = new ArrayList<String>();
			if (null != grade_array) {
				for (String str : grade_array) {
					gradeLevel.add(str);
				}
				if (!gradeList.isEmpty()) {
					for (Map<String, Object> map : gradeList) {

						if (map.containsKey("grade")) {
							String grade = (String) map.get("grade");

							if (!gradeLevel.contains(grade) && StringUtils.isNotBlank(grade)) {
								gradeLevel.add(grade);

								PlatformLogger.log("Updating node with gradeLevel" , gradeLevel.size());
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