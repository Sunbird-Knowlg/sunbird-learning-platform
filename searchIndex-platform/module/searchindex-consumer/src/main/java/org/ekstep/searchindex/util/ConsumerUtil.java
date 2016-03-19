package org.ekstep.searchindex.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;

public class ConsumerUtil {
	
	private ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();
	private SearchUtil searchUtil = new SearchUtil();
	private ObjectMapper mapper = new ObjectMapper();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void reSyncNodes(List<Map> nodeList, Map<String, Object> definitionNode, String objectType) throws Exception {
		Map<String, String> indexesMap = new HashMap<String, String>();
		for(Map node: nodeList){
			Map<String, Object> indexMap = new HashMap<String, Object>();
			indexMap.put("graph_id", (String) node.get("graphId"));
			indexMap.put("node_unique_id", (String) node.get("identifier"));
			indexMap.put("object_type", (String) node.get("objectType"));
			indexMap.put("node_type", (String) node.get("nodeType"));
			Map<String, Object> metadataMap = (Map<String, Object>) node.get("metadata");
			for(Map.Entry<String, Object> entry: metadataMap.entrySet()){
				String propertyName = entry.getKey();
				Map<String, Object> propertyDefinition = (Map<String, Object>) definitionNode.get(propertyName);
				if (propertyDefinition != null) {
					boolean indexed =  (boolean) propertyDefinition.get("indexed");
					if (indexed) {
						indexMap.put(propertyName, entry.getValue());
					}
				}
			}
			String indexDocument = mapper.writeValueAsString(indexMap);
			indexesMap.put((String)indexMap.get("node_unique_id"), indexDocument);
		}
		elasticSearchUtil.bulkIndexWithIndexId(Constants.COMPOSITE_SEARCH_INDEX, Constants.COMPOSITE_SEARCH_INDEX_TYPE, indexesMap);
	}
	
	@SuppressWarnings("rawtypes")
	public void reSyncNodes(String objectType, String graphId, Map<String, Object> definitionNode) throws Exception {
		List<Map> nodeList = searchUtil.getAllNodes(objectType, graphId);
		reSyncNodes(nodeList, definitionNode, objectType);
	}
}
