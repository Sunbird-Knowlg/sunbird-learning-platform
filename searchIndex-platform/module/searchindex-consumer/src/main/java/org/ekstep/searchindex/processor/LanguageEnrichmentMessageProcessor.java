package org.ekstep.searchindex.processor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.searchindex.util.HTTPUtil;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.kafka.util.PropertiesUtil;

/**
 * The Class LanguageEnrichmentMessageProcessor is a kafka consumer which
 * provides implementations of the core language feature extraction operations
 * defined in the IMessageProcessor along with the methods to implement content
 * enrichment from language model with additional metadata
 * 
 * @author Rashmi
 *
 */
public class LanguageEnrichmentMessageProcessor implements IMessageProcessor {

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
				LOGGER.info("checking if kafka message is null" + message);
				processMessage(message);
			}
		} catch (Exception e) {
			LOGGER.error("Error while processing kafka message", e);
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.searchindex.processor #processMessage(java.lang.String
	 * java.lang.String, java.io.File, java.lang.String)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void processMessage(Map<String, Object> message) throws Exception {
		Map<String, Object> edata = new HashMap<String, Object>();
		Map<String, Object> eks = new HashMap<String, Object>();
		String languageId = null;
		try {
			LOGGER.info("processing kafka message" + message);
			if (null != message.get("edata")) {
				LOGGER.info("checking if kafka message contains edata or not" + message.get("edata"));
				edata = (Map) message.get("edata");
				if (null != edata.get("eks")) {
					LOGGER.info("checking if edata has eks present in it" + eks);
					eks = (Map) edata.get("eks");
					if (null != eks.get("cid")) {
						LOGGER.debug("checking if eks contains cid" + eks.get("cid"));
						Node node = util.getNode("domain", eks.get("cid").toString());
						LOGGER.info("Checking if node contains languageCode" + node.getMetadata().get("languageCode"));
						if (null != node.getMetadata().get("languageCode"))
							languageId = (String) node.getMetadata().get("languageCode");
						if (StringUtils.isNotBlank(languageId) && !StringUtils.equalsIgnoreCase(languageId, "en")) {
							LOGGER.info("checking if node metadata contains text tag in it" + node.getMetadata().containsKey("text"));
							if (null != node.getMetadata().get("text")) {
								Object object = node.getMetadata().get("text");
								if (object instanceof String[]) {
									String[] textArray = (String[]) object;
									String text = Arrays.toString(textArray);
									LOGGER.info("text tag fetched from node and sending for complexity measures actor" + text);
									Map result = getComplexityMeasures(languageId, text);
									LOGGER.info("complexity measures result" + result);
									if (null != result && !result.isEmpty()) {
										LOGGER.info("mapping complexity measures with node metadata and updating the node");
										updateComplexityMeasures(node, result);
									}
								} else if(object instanceof String) {
									String text = object.toString();
									LOGGER.info("text tag fetched from node and sending for complexity measures actor" + text);
									Map result = getComplexityMeasures(languageId, text);
									LOGGER.info("complexity measures result" + result);
									if (null != result && !result.isEmpty()) {
										LOGGER.info("mapping complexity measures with node metadata and updating the node");
										updateComplexityMeasures(node, result);
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception occured while processing text_complexity map", e);
			e.printStackTrace();
		}
	}

	/**
	 * This method holds logic to make a rest API call to language actor to get
	 * complexity measures for a given text
	 * 
	 * @param languageId
	 *            The languageId
	 * 
	 * @param text
	 *            The text to analysed
	 * 
	 * @return resultMap
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<String, Object> getComplexityMeasures(String languageId, String text) throws Exception {

		LOGGER.info("getting language_api_url from properties to make rest call");
		String api_url = PropertiesUtil.getProperty("language-api-url") + "/v1/language/tools/complexityMeasures/text";
		LOGGER.info("api url to make rest api call to complexity measures api" + api_url);

		Map<String, Object> request_map = new HashMap<String, Object>();
		request_map.put("languageId", languageId);
		request_map.put("text", text);
		Request request = new Request();
		request.setRequest(request_map);
		LOGGER.info("creating request map to make rest post call" + request);

		LOGGER.info("making a post call to get complexity measures for a given text");
		String result = HTTPUtil.makePostCall(api_url, request.toString());

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
	 * @param result
	 *            The result map
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void updateComplexityMeasures(Node node, Map result) {

		try {
			LOGGER.info("extracting required fields from result map" + result);
			if (null != result.get("text_complexity")) {
				LOGGER.info("Getting text_complexity map from result object" + result.containsKey("text_complexity"));
				Map text_complexity = (Map) result.get("text_complexity");
				if (null != text_complexity && !text_complexity.isEmpty()) {
					if (text_complexity.containsKey("wordCount") && null != text_complexity.get("wordCount")) {
						Integer wordCount = (Integer) text_complexity.get("wordCount");
						LOGGER.info("updating node with wordCount" + wordCount);
						node.getMetadata().put("wordCount", wordCount);
					}
					if (text_complexity.containsKey("syllableCount") && null != text_complexity.get("syllableCount")) {
						Integer syllableCount = (Integer) text_complexity.get("syllableCount");
						LOGGER.info("updating node with syllableCount" + syllableCount);
						node.getMetadata().put("syllableCount", syllableCount);
					}
					if (text_complexity.containsKey("totalOrthoComplexity")
							&& null != text_complexity.get("totalOrthoComplexity")) {
						Object object = text_complexity.get("totalOrthoComplexity");
						if (object instanceof Integer) {
							Integer totalOrthoComplexity = (Integer) text_complexity.get("totalOrthoComplexity");
							node.getMetadata().put("totalOrthoComplexity", totalOrthoComplexity);
						} else {
							double totalOrthoComplexity = (double) text_complexity.get("totalOrthoComplexity");
							node.getMetadata().put("totalOrthoComplexity", totalOrthoComplexity);
						}
						LOGGER.info("updating node with totalOrthoComplexity"
								+ text_complexity.get("totalOrthoComplexity"));
					}
					if (text_complexity.containsKey("totalPhonicComplexity")
							&& null != text_complexity.get("totalPhonicComplexity")) {
						Object object = text_complexity.get("totalPhonicComplexity");
						if (object instanceof Integer) {
							Integer totalPhonicComplexity = (Integer) text_complexity.get("totalPhonicComplexity");
							node.getMetadata().put("totalOrthoComplexity", totalPhonicComplexity);
						} else {
							double totalPhonicComplexity = (double) text_complexity.get("totalPhonicComplexity");
							node.getMetadata().put("totalPhonicComplexity", totalPhonicComplexity);
						}
						LOGGER.info("updating node with totalPhonicComplexity"
								+ text_complexity.get("totalPhonicComplexity"));
					}
					if (text_complexity.containsKey("totalWordComplexity")
							&& null != text_complexity.get("totalWordComplexity")) {
						Object object = text_complexity.get("totalWordComplexity");
						if (object instanceof Integer) {
							Integer totalWordComplexity = (Integer) text_complexity.get("totalWordComplexity");
							node.getMetadata().put("totalOrthoComplexity", totalWordComplexity);
						} else {
							double totalWordComplexity = (double) text_complexity.get("totalWordComplexity");
							node.getMetadata().put("totalWordComplexity", totalWordComplexity);
						}
						LOGGER.info(
								"updating node with totalWordComplexity" + text_complexity.get("totalWordComplexity"));
					}
					if (text_complexity.containsKey("gradeLevels") && null != text_complexity.get("gradeLevels")) {

						List<String> grades = (List) text_complexity.get("gradeLevels");
						LOGGER.info("getting gradeLevel from complexity map" + grades);

						List<String> gradeLevel = (List) node.getMetadata().get("gradeLevel");
						LOGGER.info("getting gradeLevel from node" + gradeLevel);

						if (null != grades && !grades.isEmpty()) {
							for (Object obj : grades) {
								Map gradeMap = (Map) obj;
								if (null != gradeMap && !gradeMap.isEmpty()) {
									String grade = gradeMap.get("Grade").toString();
									String language = gradeMap.get("Language Level").toString();
									if (!gradeLevel.contains(grade) && StringUtils.isNotBlank(grade)) {
										gradeLevel.add(grade);
										node.getMetadata().put("gradeLevel", gradeLevel);
									}
									if (StringUtils.isNotBlank(language)) {
										node.getMetadata().put("Language Level", language);
										LOGGER.info("updating node with grade level and language level" + node);
									}
								}
							}
						}
					}
				}
				if (text_complexity.containsKey("themes") && null != text_complexity.get("themes")) {
					LOGGER.info("checking if complexity map contains themes");
					Object themes = text_complexity.get("themes");
					node.getMetadata().put("themes", themes);
				}
			}
			LOGGER.info("updating node with extracted language metadata" + node);
			Response response = util.updateNode(node);
			LOGGER.info("response of content update" + response.getResponseCode());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
