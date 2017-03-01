package org.ekstep.searchindex.processor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.learning.util.ControllerUtil;

import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;

public class BaseProcessor {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(LanguageEnrichmentMessageProcessor.class.getName());

	/** The Controller Utility */
	private static ControllerUtil util = new ControllerUtil();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Node filterMessage(Map<String, Object> message) throws Exception {
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
				if (null != eks.get("state") && StringUtils.equalsIgnoreCase("Live", eks.get("state").toString())) {

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

	/**
	 * This method holds logic to fetch languageId from node metadata
	 * 
	 * @param node
	 *            The node
	 * 
	 * @return The languageId
	 */
	protected String getLanguage(Node node) {
		Map<String, String> languageMap = new HashMap<String, String>();
		languageMap.put("english", "en");
		languageMap.put("telugu", "te");
		languageMap.put("hindi", "hi");
		languageMap.put("kannada", "ka");
		languageMap.put("tamil", "ta");
		languageMap.put("marathi", "mr");
		languageMap.put("bengali", "bn");
		languageMap.put("gujarati", "gu");
		languageMap.put("odia", "or");
		languageMap.put("assamese", "as");
		if (null != node.getMetadata().get("language")) {
			Object language = node.getMetadata().get("language");
			if (null != language) {
				String[] lang = (String[]) language;
				for (String str : lang) {
					String langId = str;
					String languageCode = languageMap.get(langId.toLowerCase());
					if (StringUtils.isNotBlank(languageCode) && !StringUtils.equalsIgnoreCase(languageCode, "en")) {
						return languageCode;
					}
				}
			}
		}
		return null;
	}
}