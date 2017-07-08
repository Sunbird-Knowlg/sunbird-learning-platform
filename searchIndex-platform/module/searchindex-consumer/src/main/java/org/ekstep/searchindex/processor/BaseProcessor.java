package org.ekstep.searchindex.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.util.ControllerUtil;

import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

public class BaseProcessor {

	/** The logger. */
	private static ILogger LOGGER = PlatformLogManager.getLogger();

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

		LOGGER.log("checking if kafka message contains edata");
		if (null != message.get("edata")) {
			edata = (Map) message.get("edata");
			if (null != edata.get("eks")) {
				eks = (Map) edata.get("eks");
				LOGGER.log("checking if the content is a live content");
				if (null != eks.get("state") && StringUtils.equalsIgnoreCase("Live", eks.get("state").toString())) {
					if (null != eks.get("cid")) {
						LOGGER.log("checking if eks contains cid" , eks);
						node = util.getNode("domain", eks.get("cid").toString());
						LOGGER.log("node data fetched from cid" , node);
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

		LOGGER.log("checking if node contains language in it");
		if (null != node.getMetadata().get("language")) {
			Object language = node.getMetadata().get("language");
			if (null != language) {

				String[] lang = (String[]) language;
				for (String str : lang) {
					String langId = str;
					String languageCode = languageMap.get(langId.toLowerCase());

					LOGGER.log("checking if language is not empty and not english" + languageCode);
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
	protected Map<String, Object> getOutRelationsMap(List<Relation> outRelations) {

		List<String> nodeIds = new ArrayList<String>();
		List<String> conceptGrades = new ArrayList<String>();
		String domain = null;
		Map<String, Object> result_map = new HashMap<String, Object>();

		if (null != outRelations && !outRelations.isEmpty()) {
			for (Relation rel : outRelations) {
				if (null != rel.getEndNodeObjectType()
						&& StringUtils.equalsIgnoreCase("Concept", rel.getEndNodeObjectType())) {

					String status = null;
					LOGGER.log("checking for endNode metadata contains status" , rel.getEndNodeMetadata().containsKey("status"));
					if (null != rel.getEndNodeMetadata().get("status")) {
						status = (String) rel.getEndNodeMetadata().get(ContentAPIParams.status.name());
					}
					
					if (StringUtils.isBlank(domain) && null != rel.getEndNodeMetadata().get("subject")) {
						domain = (String) rel.getEndNodeMetadata().get("subject");
					}

					LOGGER.log("checking if status is LIVE and fetching nodeIds from it" , status);
					if (StringUtils.isNotBlank(status)
							&& StringUtils.equalsIgnoreCase(ContentAPIParams.Live.name(), status)) {
						LOGGER.log("nodeIds fetched form LIVE nodes" , nodeIds);
						nodeIds.add(rel.getEndNodeId());
					}

					LOGGER.log("checking if concept contains gradeLevel" , rel.getEndNodeMetadata().containsKey("gradeLevel"));
					if (null != rel.getEndNodeMetadata().get("gradeLevel")) {
						String[] grade_array =  (String[])(rel.getEndNodeMetadata().get("gradeLevel"));
						for(String garde : grade_array){
							conceptGrades.add(garde);
						}
					}
				}
			}

			LOGGER.log("Adding nodeIds to map" + nodeIds);
			if (null != nodeIds && !nodeIds.isEmpty()) {
				result_map.put("conceptIds", nodeIds);
			}

			LOGGER.log("Adding conceptGrades to map" + conceptGrades);
			if (null != conceptGrades && !conceptGrades.isEmpty()) {
				result_map.put("conceptGrades", conceptGrades);
			}
			
			LOGGER.log("Adding domain to map: " + domain);
			if (StringUtils.isNotBlank(domain)) {
				result_map.put("domain", domain);
			}
		}
		LOGGER.log("Map of conceptGrades and nodeIds" , result_map.size());
		return result_map;
	}
}