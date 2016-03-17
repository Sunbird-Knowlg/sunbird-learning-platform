package org.ekstep.searchindex.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class SearchUtil {
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Map> getAllNodes(String objectType, String graphId) throws Exception {
		String url = PropertiesUtil.getProperty("ekstep_platform")+"/taxonomy/"+graphId+"/"+objectType;
		String result = makeHTTPGetRequest(url);
		Map<String, Object> responseObject = mapper.readValue(result, new TypeReference<Map<String, Object>>() {});
		if(responseObject != null){
			Map<String, Object> resultObject = (Map<String, Object>) responseObject.get("result");
			if(resultObject != null){
				List<Map> nodeList = (List<Map>) resultObject.get("node_list");
				return nodeList;
			}
		}
		return null;
	}
	
	public String makeHTTPGetRequest(String url) throws Exception{
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		request.addHeader("user-id", PropertiesUtil.getProperty("ekstep_platform_api_user_id"));
		request.addHeader("Content-Type", "application/json");
		HttpResponse response = client.execute(request);
		if (response.getStatusLine().getStatusCode() == 200) {
			throw new Exception("Ekstep service unavailable: " + response.getStatusLine().getStatusCode() + " : "
					+ response.getStatusLine().getReasonPhrase());
		}
		BufferedReader rd = new BufferedReader(
			new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		return result.toString();
	}
}
