package org.ekstep.searchindex.processor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.searchindex.util.HTTPUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;
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
 * @author rashmi
 *
 */
public class LanguageEnrichmentMessageProcessor implements IMessageProcessor {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(LanguageEnrichmentMessageProcessor.class.getName());

	/** The ObjectMapper */
	private static ObjectMapper mapper = new ObjectMapper();

	/** The constructor */
	public LanguageEnrichmentMessageProcessor() {
		super();
	}

	@Autowired
	private WebApplicationContext context;
	private ResultActions actions;

	/** The Controller Utility */
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
						LOGGER.info("checking if eks contains cid" + eks.get("cid"));
						Node node = util.getNode("domain", eks.get("cid").toString());
						LOGGER.info("Checking if node contains languageCode" + node.getMetadata().get("languageCode"));
						if (null != node.getMetadata().get("languageCode"))
							languageId = (String) node.getMetadata().get("languageCode");
						LOGGER.info("checking if node metadata contains text tag in it"
								+ node.getMetadata().containsKey("text"));
						if (null != node.getMetadata().get("text")) {
							Object obj = node.getMetadata().get("text");
							if (obj instanceof String[]) {
								String[] textArray = (String[]) obj;
								String text = Arrays.toString(textArray);
								LOGGER.info(
										"text tag fetched from node and sending for complexity measures actor" + text);
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void updateComplexityMeasures(Node node, Map result) {
		try {
			if (null != result.get("text_complexity")) {
				Map text_complexity = (Map) result.get("text_complexity");
				if (null != text_complexity && !text_complexity.isEmpty()) {
					if (text_complexity.containsKey("wordCount")) {
						LOGGER.info("checking if complexity map contains wordCount");
						String wordCount = (String) text_complexity.get("wordCount");
						if (StringUtils.isNotBlank(wordCount)) {
							LOGGER.info("updating node with wordCount" + wordCount);
							node.getMetadata().put("wordCount", wordCount);
						}
					}
					if (text_complexity.containsKey("syllableCount")) {
						LOGGER.info("checking if complexity map contains syllableCount");
						String syllableCount = (String) text_complexity.get("syllableCount");
						if (StringUtils.isNotBlank(syllableCount)) {
							LOGGER.info("updating node with syllableCount" + syllableCount);
							node.getMetadata().put("syllableCount", syllableCount);
						}
					}
					if (text_complexity.containsKey("totalOrthoComplexity")) {
						LOGGER.info("checking if complexity map contains totalOrthoComplexity");
						String totalOrthoComplexity = (String) text_complexity.get("totalOrthoComplexity");
						if (StringUtils.isNotBlank(totalOrthoComplexity)) {
							LOGGER.info("updating node with totalOrthoComplexity" + totalOrthoComplexity);
							node.getMetadata().put("totalOrthoComplexity", totalOrthoComplexity);
						}
					}
					if (text_complexity.containsKey("totalPhonicComplexity")) {
						LOGGER.info("checking if complexity map contains totalPhonicComplexity");
						String totalPhonicComplexity = (String) text_complexity.get("totalPhonicComplexity");
						if (StringUtils.isNotBlank(totalPhonicComplexity)) {
							LOGGER.info("updating node with totalPhonicComplexity" + totalPhonicComplexity);
							node.getMetadata().put("totalPhonicComplexity", totalPhonicComplexity);
						}
					}
					if (text_complexity.containsKey("totalWordComplexity")) {
						LOGGER.info("checking if complexity map contains totalWordComplexity");
						String totalWordComplexity = (String) text_complexity.get("totalWordComplexity");
						if (StringUtils.isNotBlank(totalWordComplexity)) {
							LOGGER.info("updating node with totalWordComplexity" + totalWordComplexity);
							node.getMetadata().put("totalWordComplexity", totalWordComplexity);
						}
					}
					if (text_complexity.containsKey("gradeLevel")) {
						LOGGER.info("checking if complexity map contains gradeLevel");
						List<String> grades = (List) text_complexity.get("gradeLevels");
						LOGGER.info("getting gradeLevel from complexity map" + grades);
						List<String> gradeLevel = (List) node.getMetadata().get("gradeLevel");
						LOGGER.info("getting gradeLevel from node" + gradeLevel);
						if (null != grades && !grades.isEmpty()) {
							for (Object grade : grades) {
								Map map = (Map) grade;
								if (!gradeLevel.contains(map.get("Grade"))) {
									node.getMetadata().put("gradeLevel", grades);
									node.getMetadata().put("Language Level", map.get("Language Level"));
									LOGGER.info("updating node with grade level and language level" + node);
								}
							}
						}
					}
					if (text_complexity.containsKey("themes")) {
						LOGGER.info("checking if complexity map contains themes");
						node.getMetadata().put("themes", text_complexity.get("themes"));
					}
				}
			}
			LOGGER.info("updating node with extracted language metadata" + node);
			Response response = util.updateNode(node);
			LOGGER.info("response of content update" + response.getResponseCode());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<String, Object> getComplexityMeasures(String languageId, String text) throws Exception {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
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
		resultMap = (Map) responseObject.get("result");
		LOGGER.info("getting resultMap from response Object" + resultMap);
		if (resultMap == null) {
			throw new Exception("Result in response is empty");
		}
		return resultMap;
	}
}
