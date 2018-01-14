package org.ekstep.language.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.mgr.BaseManager;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Filter;
import org.ekstep.graph.dac.model.MetadataCriterion;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.SearchConditions;
import org.ekstep.graph.dac.model.SearchCriteria;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.telemetry.logger.TelemetryManager;

/**
 * The Class GradeComplexityCache, caches GradeLevelCompleixty nodes against its
 * complexity in sorted map as to retrieve suitable grades for any text and in
 * addition it caches GradeLevelCompleixty in 2D- matrix style( gradeLvel x
 * languageLevel) for easy retrieval during validation.
 *
 * @author karthik
 */
public class GradeComplexityCache extends BaseManager {

	

	/**
	 * The Map which stores GradeLevelComplexity nodes against the complexity in
	 * sorted order for each language - sortedMap key - languageId and value -
	 * TreeMap<AverageComplexity, List<GradeLevelComplexityNode>>
	 */
	private final Map<String, Map<Double, List<Node>>> gradeComplexitySortedCacheMap = new HashMap<>();

	/**
	 * The Map which stores GradeLevelComplexity nodes against its languageLevel
	 * and gradeLevel for each language - matrixMap key - languageId and value -
	 * HashMap<GradeLevel, HashMap<LanguageLevel, GradeLevelComplexityNode>>
	 */
	private final Map<String, Map<String, Map<String, Node>>> gradeComplexityMatrixMap = new HashMap<>();

	/** The language graph name. */
	private static String languageGraphName = "language";

	/** The singleton instance. */
	private static GradeComplexityCache instance = null;

	private GradeComplexityCache() {
	}

	/**
	 * Gets the singleton instance of GradeComplexityCache.
	 *
	 * @return single instance of GradeComplexityCache
	 */
	public static GradeComplexityCache getInstance() {
		if (instance == null)
			instance = new GradeComplexityCache();
		return instance;
	}

	/**
	 * Load grade level complexity node into cache of sortedMap and matixMap
	 *
	 * @param gradeLevelComplexityNode
	 *            the grade level complexity node
	 * @param sortedGradeComplexityMap
	 *            the sorted grade complexity map
	 * @param gradeComplexityMatrix
	 *            the grade complexity matrix
	 */
	private void loadGradeLevelComplexityIntoCache(Node gradeLevelComplexityNode,
			Map<Double, List<Node>> sortedGradeComplexityMap, Map<String, Map<String, Node>> gradeComplexityMatrix) {
		Double averageComplexity = (Double) gradeLevelComplexityNode.getMetadata().get("averageComplexity");
		List<Node> gradeComplexityList = sortedGradeComplexityMap.get(averageComplexity);
		if (gradeComplexityList == null)
			gradeComplexityList = new ArrayList<Node>();
		gradeComplexityList.add(gradeLevelComplexityNode);
		sortedGradeComplexityMap.put(averageComplexity, gradeComplexityList);
		// cache node into matrixMap(GradeLevel x LanguageLevel)
		String gradeLevel = (String) gradeLevelComplexityNode.getMetadata().get("gradeLevel");
		String languageLevel = (String) gradeLevelComplexityNode.getMetadata().get("languageLevel");
		Map<String, Node> languageLevelMap = gradeComplexityMatrix.get(gradeLevel);
		if (languageLevelMap == null)
			languageLevelMap = new HashMap<String, Node>();
		languageLevelMap.put(languageLevel, gradeLevelComplexityNode);
		gradeComplexityMatrix.put(gradeLevel, languageLevelMap);
	}

	/**
	 * Load all grade level complexity node for given language into cache of
	 * sortedMap and matrixMap
	 *
	 * @param languageId
	 *            the language id
	 */
	@SuppressWarnings("unchecked")
	public void loadGradeLevelComplexity(String languageId) {
		TelemetryManager.log("loadGradeLevelComplexity for the language" + languageId);
		SearchCriteria sc = new SearchCriteria();
		sc.setObjectType("GradeLevelComplexity");
		sc.addMetadata(MetadataCriterion.create(
				Arrays.asList(new Filter(LanguageParams.languageId.name(), SearchConditions.OP_LIKE, languageId))));
		Request req = getRequest(languageGraphName, GraphEngineManagers.SEARCH_MANAGER, "searchNodes");
		req.put(GraphDACParams.search_criteria.name(), sc);
		Response searchRes = getResponse(req);
		if (!checkError(searchRes)) {
			List<Node> nodes = (List<Node>) searchRes.get(GraphDACParams.node_list.name());
			if (null != nodes) {
				Map<Double, List<Node>> sortedGradeComplexityMap = new TreeMap<Double, List<Node>>();
				Map<String, Map<String, Node>> gradeComplexityMatrix = new HashMap<String, Map<String, Node>>();
				for (Node node : nodes) {
					loadGradeLevelComplexityIntoCache(node, sortedGradeComplexityMap, gradeComplexityMatrix);
				}
				gradeComplexitySortedCacheMap.put(languageId, sortedGradeComplexityMap);
				gradeComplexityMatrixMap.put(languageId, gradeComplexityMatrix);
			}
			TelemetryManager.log("completed loadGradeLevelComplexity with " + nodes.size() + " nodes for the language" + languageId);
		} else {
			TelemetryManager.warn("error in getting GradeLevelComplexity nodes from graph, message: " + getErrorMessage(searchRes));
		}

	}

	/**
	 * Load given grade level complexity node into cache of sortedMap and
	 * matixMap
	 *
	 * @param languageId
	 *            the language id
	 * @param node_id
	 *            the node id
	 */
	public void loadGradeLevelComplexity(String languageId, String node_id) {
		TelemetryManager.log("loadGradeLevelComplexity for the language: " + languageId + " and node_id: " + node_id);
		Request request = getRequest(languageGraphName, GraphEngineManagers.SEARCH_MANAGER, "getDataNode");
		request.put(GraphDACParams.node_id.name(), node_id);
		request.put(GraphDACParams.get_tags.name(), true);
		Response findRes = getResponse(request);
		if (!checkError(findRes)) {
			Node gradeLevelComplexity = (Node) findRes.get(GraphDACParams.node.name());

			Map<Double, List<Node>> sortedGradeComplexityMap = (TreeMap<Double, List<Node>>) gradeComplexitySortedCacheMap
					.get(languageId);
			if (sortedGradeComplexityMap == null)
				sortedGradeComplexityMap = new TreeMap<Double, List<Node>>();
			else{
				//remove if node already exist
				for(Map.Entry<Double, List<Node>> gradeEntry: sortedGradeComplexityMap.entrySet()){
					List<Node> gradeLevelComplexityNodes = gradeEntry.getValue();
					for(Iterator<Node> itr=gradeLevelComplexityNodes.iterator(); itr.hasNext(); ){
						Node gradeLevelComplexityNode = itr.next();
						if(gradeLevelComplexityNode.getIdentifier().equalsIgnoreCase(node_id)){
							itr.remove();
						}
					}	
				}
			}

			Map<String, Map<String, Node>> gradeComplexityMatrix = gradeComplexityMatrixMap.get(languageId);
			if (gradeComplexityMatrix == null)
				gradeComplexityMatrix = new HashMap<String, Map<String, Node>>();

			loadGradeLevelComplexityIntoCache(gradeLevelComplexity, sortedGradeComplexityMap, gradeComplexityMatrix);
			gradeComplexitySortedCacheMap.put(languageId, sortedGradeComplexityMap);
			gradeComplexityMatrixMap.put(languageId, gradeComplexityMatrix);
			TelemetryManager.log(
					"completed loadGradeLevelComplexity for the language: " + languageId + " and node_id: " + node_id);
		}else {
			TelemetryManager.warn(
					"error in getting GradeLevelComplexity node("+node_id+") from graph, message: " + getErrorMessage(findRes));
		}


	}

	/**
	 * Gets the suitable grades for any given complexity
	 *
	 * @param languageId
	 *            the language id
	 * @param complexity
	 *            the complexity
	 * @return the suitable grades
	 */
	public List<Node> getSuitableGrades(String languageId, Double complexity) {
		TelemetryManager.log("getSuitableGrades for the language: " + languageId + " and complexity: " + complexity);
		TreeMap<Double, List<Node>> sortedGradeComplexityMap = (TreeMap<Double, List<Node>>) gradeComplexitySortedCacheMap
				.get(languageId);
		if (sortedGradeComplexityMap == null) {
			loadGradeLevelComplexity(languageId);
			sortedGradeComplexityMap = (TreeMap<Double, List<Node>>) gradeComplexitySortedCacheMap.get(languageId);
		}

		if (sortedGradeComplexityMap != null) {
			if (sortedGradeComplexityMap.containsKey(complexity)) {
				return sortedGradeComplexityMap.get(complexity);
			} else {
				Map.Entry<Double, List<Node>> suitableGradeEntry = sortedGradeComplexityMap.higherEntry(complexity);
				if (suitableGradeEntry != null)
					return suitableGradeEntry.getValue();
			}
		}
		return null;
	}

	/**
	 * Gets all the grade level complexity nodes for any given language
	 *
	 * @param languageId
	 *            the language id
	 * @return the grade level complexity
	 */
	public List<Node> getGradeLevelComplexity(String languageId) {
		TelemetryManager.log("getGradeLevelComplexity for the language: " + languageId);
		TreeMap<Double, List<Node>> sortedGradeComplexityMap = (TreeMap<Double, List<Node>>) gradeComplexitySortedCacheMap
				.get(languageId);
		if (sortedGradeComplexityMap == null) {
			loadGradeLevelComplexity(languageId);
			sortedGradeComplexityMap = (TreeMap<Double, List<Node>>) gradeComplexitySortedCacheMap.get(languageId);
		}

		List<Node> allGrades = new ArrayList<Node>();

		if (sortedGradeComplexityMap != null) {
			for (Map.Entry<Double, List<Node>> gradeEntry : sortedGradeComplexityMap.entrySet())
				allGrades.addAll(gradeEntry.getValue());

			return allGrades;
		}

		return allGrades;
	}

	/**
	 * Gets the grade level complexity node for any given languageId, gradeLevel
	 * and languageLevel
	 *
	 * @param languageId
	 *            the language id
	 * @param gradeLevel
	 *            the grade level
	 * @param languageLevel
	 *            the language level
	 * @return the grade level complexity
	 */
	public Double getGradeLevelComplexity(String languageId, String gradeLevel, String languageLevel) {
		TelemetryManager.log("getGradeLevelComplexity for the language: " + languageId +", gradeLevel: "+ gradeLevel+", languageLevel: "+languageLevel);
		Map<String, Map<String, Node>> matirx = gradeComplexityMatrixMap.get(languageId);
		if (matirx != null) {
			Map<String, Node> gradeLevelComplexityMap = matirx.get(gradeLevel);
			if (gradeLevelComplexityMap != null) {
				Node gradeLevelComplexity = gradeLevelComplexityMap.get(languageLevel);
				if (gradeLevelComplexity != null)
					return (Double) gradeLevelComplexity.getMetadata().get("averageComplexity");
			}
		}

		return null;
	}

}
