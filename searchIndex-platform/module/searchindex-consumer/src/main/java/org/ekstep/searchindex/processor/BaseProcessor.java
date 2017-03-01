package org.ekstep.searchindex.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.learning.util.ControllerUtil;

import com.ilimi.graph.dac.model.Node;

public class BaseProcessor implements IMessageProcessor {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(LanguageEnrichmentMessageProcessor.class.getName());

	/** The Controller Utility */
	private static ControllerUtil util = new ControllerUtil();

	/** The ObjectMapper */
	private static ObjectMapper mapper = new ObjectMapper();

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
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.searchindex.processor #processMessage(java.lang.String
	 * java.lang.String, java.io.File, java.lang.String)
	 */
	@Override
	public void processMessage(Map<String, Object> message) throws Exception {
		filterMessage(message);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Node filterMessage(Map<String, Object> message) {

		Map<String, Object> edata = new HashMap<String, Object>();
		Map<String, Object> eks = new HashMap<String, Object>();
		Node node = null;

		LOGGER.info("checking if kafka message contains edata");
		if (null != message.get("edata")) {
			LOGGER.debug("checking if kafka message contains edata" + edata);
			edata = (Map) message.get("edata");

			LOGGER.info("checking if edata contains eks");
			if (null != edata.get("eks")) {
				LOGGER.debug("checking if edata contains eks" + eks);
				eks = (Map) edata.get("eks");

				LOGGER.info("checking if the content is a live content");
				if (null != edata.get("state") && StringUtils.equalsIgnoreCase("Live", edata.get("state").toString())) {

					LOGGER.info("checking if eks contains cid/nodeId");
					if (null != eks.get("cid")) {
						LOGGER.debug("checking if eks contains cid" + eks);
						node = util.getNode("domain", eks.get("cid").toString());
						LOGGER.info("node data fetched from cid" + node);
						return node;
					}
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unused")
	protected String getLanguage(Node node) {
		if (null != node.getMetadata().get("languageCode")) {
			String language = (String) node.getMetadata().get("languageCode");
			if (StringUtils.isNotBlank(language) && !StringUtils.equalsIgnoreCase(language, "en")) {
				return language;
			}
		}
		return null;
	}
}
