package org.ekstep.searchindex.processor;

import java.util.ArrayList;
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

	/**
	 * This method holds logic to filter the kafka message and return the nodeId
	 * 
	 * @param message
	 *            The kafka message
	 * 
	 * @return The content node
	 * 
	 * @throws Exception
	 */
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

		LOGGER.info("checking if node contains language in it");
		if (null != node.getMetadata().get("language")) {
			Object language = node.getMetadata().get("language");

			LOGGER.info("checking if language is empty" + language);
			if (null != language) {

				String[] lang = (String[]) language;
				for (String str : lang) {
					String langId = str;
					String languageCode = languageMap.get(langId.toLowerCase());

					LOGGER.info("checking if language is not empty and not english" + languageCode);
					if (StringUtils.isNotBlank(languageCode) && !StringUtils.equalsIgnoreCase(languageCode, "en")) {
						return languageCode;
					}
				}
			}
		}
		return null;
	}

	/**
	 * This method holds logic to process outRelations and fetch concept nodes
	 * grades from outRelations
	 * 
	 * @param outRelations
	 *            outRelationsMap
	 * @return result map
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Map<String, Object> getOutRelationsMap(List<Relation> outRelations) {

		Set<String> nodeIds = new HashSet<String>();
		List<String> conceptGrades = new ArrayList<String>();
		Map<String, Object> result_map = new HashMap<String, Object>();

		LOGGER.info("outRelations fetched from each item" + outRelations);
		if (null != outRelations && !outRelations.isEmpty()) {

			LOGGER.info("Iterating through outrelations");
			for (Relation rel : outRelations) {

				LOGGER.info("checking if endNodeType is objectType");
				if (null != rel.getEndNodeObjectType()
						&& StringUtils.equalsIgnoreCase("Concept", rel.getEndNodeObjectType())) {

					String status = null;
					LOGGER.info("checking relation contains status");
					if (null != rel.getEndNodeMetadata().get("status")) {
						LOGGER.info("getting status from node");
						status = (String) rel.getEndNodeMetadata().get(ContentWorkflowPipelineParams.status.name());
					}

					LOGGER.info("checking if status is LIVE and fetching nodeIds from it" + status);
					if (StringUtils.isNotBlank(status)
							&& StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.Live.name(), status)) {
						LOGGER.info("nodeIds fetched form LIVE nodes" + nodeIds);
						nodeIds.add(rel.getEndNodeId());
					}

					LOGGER.info("checking if concept contains gradeLevel");
					if (null != rel.getEndNodeMetadata().get("gradeLevel")) {
						List<String> list = (List) (rel.getEndNodeMetadata().get("gradeLevel"));

						LOGGER.info("checking if grade level list is empty");
						if (null != list && !list.isEmpty()) {
							LOGGER.info("adding all concept grades" + list);
							conceptGrades.addAll(list);
						}
					}
				}
			}

			LOGGER.info("Adding nodeIds to map" + nodeIds);
			if (null != nodeIds && !nodeIds.isEmpty()) {
				result_map.put("conceptIds", nodeIds);
			}

			LOGGER.info("Adding conceptGrades to map" + conceptGrades);
			if (null != conceptGrades && !conceptGrades.isEmpty()) {
				result_map.put("conceptGrades", conceptGrades);
			}
		}
		LOGGER.info("Map of conceptGrades and nodeIds" + result_map);
		return result_map;
	}
}