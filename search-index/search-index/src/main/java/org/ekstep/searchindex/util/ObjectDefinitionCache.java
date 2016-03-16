package org.ekstep.searchindex.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class ObjectDefinitionCache {
	
	@SuppressWarnings("rawtypes")
	private static Map<String, Map> definitionMap =  new HashMap<String, Map>();
	private static ObjectMapper mapper = new ObjectMapper();

	@SuppressWarnings({ "unchecked"})
	public static Map<String, Object> getDefinitionNode(String objectType, String graphId) throws Exception {
		Map<String, Object> definition = definitionMap.get(objectType);
		if(definition == null){
			definition = getDefinitionFromGraph(objectType, graphId);
			definitionMap.put(objectType, definition);
		}
		return definition;
	}

	private static Map<String, Object> getDefinitionFromGraph(String objectType, String graphId) throws Exception {
		String url = "http://localhost:8083/taxonomy-service/taxonomy/"+graphId+"/definition/"+objectType;

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
		System.out.println("Result:" 
                + result);
		Map<String, Object> definitionObject = mapper.readValue(result.toString(), new TypeReference<Map<String, Object>>() {});
		Map<String, Object> definition = retrieveProperties(definitionObject);
		return definition;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Map<String, Object> retrieveProperties(Map<String, Object> definitionObject) throws Exception {
		if(definitionObject == null){
			throw new Exception("Unable to find Defintion object.");
		}
		Map<String, Object> definition = new HashMap<String, Object>();
		Map result = (Map) definitionObject.get("result");
		if(result == null){
			throw new Exception("Result in response is empty");
		}
		Map definitionNode = (Map) result.get("definition_node");
		if(definitionNode == null){
			throw new Exception("Definition node in result is empty");
		}
		List<Map> propertiesList = (List<Map>) definitionNode.get("properties");
		if(propertiesList == null || propertiesList.isEmpty()){
			throw new Exception("Properties List in Definition node is empty");
		}
		for(Map propertyMap: propertiesList){
			definition.put((String) propertyMap.get("propertyName"), propertyMap);
		}
		return definition;
	}

	private static void resyncDefinition(String objectType, String graphId) throws Exception{
			Map<String, Object> definition = getDefinitionFromGraph(objectType, graphId);
			definitionMap.put(objectType, definition);
	}
	
	public static void main(String[] args) throws Exception {
		getDefinitionNode("Word", "en");
		getDefinitionNode("Word", "en");
	}
	

}
