package org.ekstep.searchindex.processor;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.learning.util.ControllerUtil;

import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.model.Node;

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
							String text = (String) node.getMetadata().get("text");
							LOGGER.info("text tag fetched from node and sending for complexity measures actor" + text);
							Response response = util.getComplexityMeasures(node.getGraphId(), languageId, text);
							LOGGER.info("response received from complexity measure actor" + response.getResponseCode());
							Map result = (Map) response.getResult();
							LOGGER.info("complexity measures result" + result);
							if (null != result && !result.isEmpty()) {
								LOGGER.info("mapping complexity measures with node metadata and updating the node");
								mapComplexityMeasures(node, result);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	private void mapComplexityMeasures(Node node, Map result) {
		try {
			if (null != result.get("text_complexity")) {
				Map text_complexity = (Map) result.get("text_complexity");
				if (null != text_complexity && !text_complexity.isEmpty()) {
					if (text_complexity.containsKey("wordCount")) {
						String wordCount = (String) text_complexity.get("wordCount");
						if (StringUtils.isNotBlank(wordCount)){
							LOGGER.info("updating node with wordCount" + wordCount);
							node.getMetadata().put("wordCount", wordCount);
					}
					}
					if (text_complexity.containsKey("syllableCount")) {
						String syllableCount = (String) text_complexity.get("syllableCount");
						if (StringUtils.isNotBlank(syllableCount)){
							LOGGER.info("updating node with syllableCount" + syllableCount);
							node.getMetadata().put("syllableCount", syllableCount);
						}
					}
					if (text_complexity.containsKey("totalOrthoComplexity")) {
						String totalOrthoComplexity = (String) text_complexity.get("totalOrthoComplexity");
						if (StringUtils.isNotBlank(totalOrthoComplexity)){
							LOGGER.info("updating node with totalOrthoComplexity" + totalOrthoComplexity);
							node.getMetadata().put("totalOrthoComplexity", totalOrthoComplexity);
						}
					}
					if (text_complexity.containsKey("totalPhonicComplexity")) {
						String totalPhonicComplexity = (String) text_complexity.get("totalPhonicComplexity");
						if (StringUtils.isNotBlank(totalPhonicComplexity)){
							LOGGER.info("updating node with totalPhonicComplexity" + totalPhonicComplexity);
							node.getMetadata().put("totalPhonicComplexity", totalPhonicComplexity);
						}
					}
					if (text_complexity.containsKey("totalWordComplexity")) {
						String totalWordComplexity = (String) text_complexity.get("totalWordComplexity");
						if (StringUtils.isNotBlank(totalWordComplexity)){
							LOGGER.info("updating node with totalWordComplexity" + totalWordComplexity);
							node.getMetadata().put("totalWordComplexity", totalWordComplexity);
						}
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
}
