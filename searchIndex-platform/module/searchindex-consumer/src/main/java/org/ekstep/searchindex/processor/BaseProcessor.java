package org.ekstep.searchindex.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.util.ControllerUtil;

import com.ilimi.common.logger.LoggerEnum;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

public class BaseProcessor {

	/** The logger. */
	

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
		if (null != message.get("edata")) {
			edata = (Map) message.get("edata");
			if (null != edata.get("eks")) {
				eks = (Map) edata.get("eks");
				if (null != eks.get("state") && StringUtils.equalsIgnoreCase("Live", eks.get("state").toString())) {
					if (null != eks.get("id")) {
						node = util.getNode("domain", eks.get("id").toString());
						PlatformLogger.log("node data fetched from id" , node);
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

		PlatformLogger.log("checking if node contains language in it");
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
					if (null != rel.getEndNodeMetadata().get("status")) {
						status = (String) rel.getEndNodeMetadata().get(ContentAPIParams.status.name());
					}
					
					if (StringUtils.isBlank(domain) && null != rel.getEndNodeMetadata().get("subject")) {
						domain = (String) rel.getEndNodeMetadata().get("subject");
					}
					if (StringUtils.isNotBlank(status)
							&& StringUtils.equalsIgnoreCase(ContentAPIParams.Live.name(), status)) {
						nodeIds.add(rel.getEndNodeId());
					}
					if (null != rel.getEndNodeMetadata().get("gradeLevel")) {
						String[] grade_array =  (String[])(rel.getEndNodeMetadata().get("gradeLevel"));
						for(String garde : grade_array){
							conceptGrades.add(garde);
						}
					}
				}
			}
			if (null != nodeIds && !nodeIds.isEmpty()) {
				result_map.put("conceptIds", nodeIds);
			}
			if (null != conceptGrades && !conceptGrades.isEmpty()) {
				result_map.put("conceptGrades", conceptGrades);
			}
		
			if (StringUtils.isNotBlank(domain)) {
				result_map.put("domain", domain);
			}
		}
		PlatformLogger.log("Map of conceptGrades and nodeIds" + result_map.size(), null ,LoggerEnum.INFO.name());
		return result_map;
	}
}