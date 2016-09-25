package org.ekstep.language.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.enums.LanguageParams;

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.engine.router.GraphEngineManagers;

/**
 * The Class GradeComplexityCache, caches GradeLevelCompleixty nodes against its
 * complexity in sorted map as to retrieve suitable grades and in addition
 * caches GradeLevelCompleixty in 2D- matrix( gradeLvel x languageLevel) for
 * validation
 * 
 *
 * @author karthik
 */
public class GradeComplexityCache extends BaseManager {

	private static Logger LOGGER = LogManager.getLogger(GradeComplexityCache.class.getName());

	private final Map<String, Map<Double, List<Node>>> gradeComplexityOrderedCacheMap = new HashMap<>();
	private final Map<String, Map<String, Map<String, Node>>> gradeComplexityMatrixMap = new HashMap<>();

	private List<String> orderedGradeLevel = null;
	private List<String> orderedLanguageLevel = null;
	private static String languageGraphName = "language";
	private static GradeComplexityCache instance = null;

	private GradeComplexityCache() {
	}

	public static GradeComplexityCache getInstance() {
		if (instance == null)
			instance = new GradeComplexityCache();
		return instance;
	}

	@SuppressWarnings("unchecked")
	public void loadGradeLevelComplexityFromGraph(String languageId) {
		Property property = new Property(LanguageParams.languageId.name(), languageId);
		Request request = getRequest(languageGraphName, GraphEngineManagers.SEARCH_MANAGER, "getNodesByProperty");
		request.put(GraphDACParams.metadata.name(), property);
		request.put(GraphDACParams.get_tags.name(), true);
		Response findRes = getResponse(request, LOGGER);
		if (!checkError(findRes)) {
			List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
			if (null != nodes && !nodes.isEmpty()) {
				Map<Double, List<Node>> sortedGradeComplexityMap = new TreeMap<Double, List<Node>>();
				Map<String, Map<String, Node>> gradeComplexityMatrix = new HashMap<String, Map<String, Node>>();
				for (Node node : nodes) {
					Double averageComplexity = (Double) node.getMetadata().get("averageComplexity");
					List<Node> gradeComplexityList = sortedGradeComplexityMap.get(averageComplexity);
					if (gradeComplexityList == null)
						gradeComplexityList = new ArrayList<Node>();
					gradeComplexityList.add(node);
					sortedGradeComplexityMap.put(averageComplexity, gradeComplexityList);
					// store node into matrixMap(GradeLevel x LanguageLevel)
					String gradeLevel = (String) node.getMetadata().get("gradeLevel");
					String languageLevel = (String) node.getMetadata().get("languageLevel");
					Map<String, Node> languageLevelMap = gradeComplexityMatrix.get(gradeLevel);
					if (languageLevelMap == null)
						languageLevelMap = new HashMap<String, Node>();
					languageLevelMap.put(languageLevel, node);
					gradeComplexityMatrix.put(gradeLevel, languageLevelMap);
				}
				gradeComplexityOrderedCacheMap.put(languageId, sortedGradeComplexityMap);
				gradeComplexityMatrixMap.put(languageId, gradeComplexityMatrix);
			}
		}
	}

	@SuppressWarnings("unused")
	public void loadGradeLevelComplexity(String languageId, Node gradeLevelComplexity) {

		Double averageComplexity = (Double) gradeLevelComplexity.getMetadata().get("averageComplexity");
		Map<Double, List<Node>> sortedGradeComplexityMap = (TreeMap<Double, List<Node>>) gradeComplexityOrderedCacheMap
				.get(languageId);
		if (sortedGradeComplexityMap == null)
			sortedGradeComplexityMap = new TreeMap<Double, List<Node>>();

		List<Node> gradeComplexityList = sortedGradeComplexityMap.get(averageComplexity);
		if (gradeComplexityList == null)
			gradeComplexityList = new ArrayList<Node>();

		gradeComplexityList.add(gradeLevelComplexity);
		sortedGradeComplexityMap.put(averageComplexity, gradeComplexityList);
		gradeComplexityOrderedCacheMap.put(languageId, sortedGradeComplexityMap);

		// store node into matrix map
		Map<String, Map<String, Node>> gradeComplexityMatrix = gradeComplexityMatrixMap.get(languageId);
		if (gradeComplexityMatrix == null)
			gradeComplexityMatrix = new HashMap<String, Map<String, Node>>();
		String gradeLevel = (String) gradeLevelComplexity.getMetadata().get("gradeLevel");
		String languageLevel = (String) gradeLevelComplexity.getMetadata().get("languageLevel");
		Map<String, Node> languageLevelMap = gradeComplexityMatrix.get(gradeLevel);
		if (languageLevelMap == null)
			languageLevelMap = new HashMap<String, Node>();
		languageLevelMap.put(languageLevel, gradeLevelComplexity);
		gradeComplexityMatrix.put(gradeLevel, languageLevelMap);
		gradeComplexityMatrixMap.put(languageId, gradeComplexityMatrix);

	}

	public List<Node> getSuitableGrades(String languageId, Double complexity) {
		TreeMap<Double, List<Node>> sortedGradeComplexityMap = (TreeMap<Double, List<Node>>) gradeComplexityOrderedCacheMap
				.get(languageId);
		if (sortedGradeComplexityMap == null) {
			loadGradeLevelComplexityFromGraph(languageId);
			sortedGradeComplexityMap = (TreeMap<Double, List<Node>>) gradeComplexityOrderedCacheMap
					.get(languageId);
		}
		
		if (sortedGradeComplexityMap != null) {
			if (sortedGradeComplexityMap.containsKey(complexity)) {
				return sortedGradeComplexityMap.get(complexity);
			} else {
				Map.Entry<Double, List<Node>> suitableGradeEntry = sortedGradeComplexityMap.higherEntry(complexity);
				if(suitableGradeEntry != null)
					return suitableGradeEntry.getValue();
			}
		}
		return null;
	}

	public List<Node> getGradeLevelComplexity(String languageId) {

		TreeMap<Double, List<Node>> sortedGradeComplexityMap = (TreeMap<Double, List<Node>>) gradeComplexityOrderedCacheMap
				.get(languageId);
		if (sortedGradeComplexityMap == null) {
			loadGradeLevelComplexityFromGraph(languageId);
			sortedGradeComplexityMap = (TreeMap<Double, List<Node>>) gradeComplexityOrderedCacheMap
					.get(languageId);
		}
		
		List<Node> allGrades = new ArrayList<Node>();
		
		if (sortedGradeComplexityMap != null) {
			for (Map.Entry<Double, List<Node>> gradeEntry : sortedGradeComplexityMap.entrySet())
				allGrades.addAll(gradeEntry.getValue());

			return allGrades;
		}

		return allGrades;
	}

	public Double getGradeLevelComplexity(String languageId, String gradeLevel, String languageLevel) {
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
