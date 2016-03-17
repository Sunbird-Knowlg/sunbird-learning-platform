package org.ekstep.searchindex.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class ConsumerUtil {
	
	private ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();
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
					boolean indexed = (boolean) propertyDefinition.get("indexed");
					if (indexed) {
						indexMap.put(propertyName, entry.getValue());
					}
				}
			}
			String indexDocument = mapper.writeValueAsString(indexMap);
			indexesMap.put((String)indexMap.get("node_unique_id"), indexDocument);
		}
		elasticSearchUtil.bulkIndexWithIndexId(Constants.COMPOSITE_SEARCH_INDEX, objectType.toLowerCase(), indexesMap);
	}
	
	public void reSyncNodes(String objectType, String graphId, Map<String, Object> definitionNode) throws Exception {
		List<Map> nodeList = getAllNodes(objectType, graphId);
		reSyncNodes(nodeList, definitionNode, objectType);
	}

	@SuppressWarnings("unused")
	private List<Map> getAllNodes(String objectType, String graphId) throws JsonParseException, JsonMappingException, IOException {
		String url = "http://localhost:8083/taxonomy-service/taxonomy/"+graphId+"/"+objectType;

		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);

		//Change
		request.addHeader("user-id", "mahesh");
		request.addHeader("Content-Type", "application/json");
		
		HttpResponse response = client.execute(request);

		System.out.println("Response Code : " 
	                + response.getStatusLine().getStatusCode());

		BufferedReader rd = new BufferedReader(
			new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		
		Map<String, Object> responseObject = mapper.readValue(result.toString(), new TypeReference<Map<String, Object>>() {});
		if(responseObject != null){
			Map<String, Object> resultObject = (Map<String, Object>) responseObject.get("result");
			if(resultObject != null){
				List<Map> nodeList = (List<Map>) resultObject.get("node_list");
				return nodeList;
			}
		}
		return null;
	}
}
