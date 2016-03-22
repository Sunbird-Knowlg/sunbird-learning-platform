package org.ekstep.searchindex.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.codec.Charsets;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.processor.IMessageProcessor;

public class ConsumerUtil {

	private ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();
	private ObjectMapper mapper = new ObjectMapper();
	private ConsumerConfig consumerConfig;
	
	public ConsumerUtil() {
		this.consumerConfig = readConsumerProperties();
	}

	
	
	public ConsumerConfig getConsumerConfig() {
		return consumerConfig;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void reSyncNodes(List<Map> nodeList, Map<String, Object> definitionNode, String objectType)
			throws Exception {
		Map<String, String> indexesMap = new HashMap<String, String>();
		for (Map node : nodeList) {
			Map<String, Object> indexMap = new HashMap<String, Object>();
			indexMap.put("graph_id", (String) node.get("graphId"));
			indexMap.put("node_unique_id", (String) node.get("identifier"));
			indexMap.put("object_type", (String) node.get("objectType"));
			indexMap.put("node_type", (String) node.get("nodeType"));
			Map<String, Object> metadataMap = (Map<String, Object>) node.get("metadata");
			for (Map.Entry<String, Object> entry : metadataMap.entrySet()) {
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
			indexesMap.put((String) indexMap.get("node_unique_id"), indexDocument);
		}
		elasticSearchUtil.bulkIndexWithIndexId(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, indexesMap);
	}

	@SuppressWarnings("rawtypes")
	public void reSyncNodes(String objectType, String graphId, Map<String, Object> definitionNode) throws Exception {
		List<Map> nodeList = getAllNodes(objectType, graphId);
		reSyncNodes(nodeList, definitionNode, objectType);
	}

	@SuppressWarnings("unchecked")
	public IMessageProcessor getMessageProcessorFactory(String messageProcessorName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class<IMessageProcessor> clazz = (Class<IMessageProcessor>) Class.forName(messageProcessorName);
		IMessageProcessor messageProcessor= clazz.newInstance();
		return messageProcessor;
	}

	public ConsumerConfig readConsumerProperties() {
		try {
			String filename = "consumer-config.xml";
			InputStream is = this.getClass().getClassLoader().getResourceAsStream(filename);
			JAXBContext jaxbContext = JAXBContext.newInstance(ConsumerConfig.class);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			ConsumerConfig consumerConfig = (ConsumerConfig) jaxbUnmarshaller.unmarshal(is);
			return consumerConfig;

		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return null;

	}

	public static void main(String[] args) {
		ConsumerUtil util = new ConsumerUtil();
		util.readConsumerProperties();
	}
	
	public String makeHTTPGetRequest(String url) throws Exception{
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		request.addHeader("user-id", consumerConfig.consumerInit.ekstepPlatformApiUserId);
		request.addHeader("Content-Type", "application/json");
		HttpResponse response = client.execute(request);
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new Exception("Ekstep service unavailable: " + response.getStatusLine().getStatusCode() + " : "
					+ response.getStatusLine().getReasonPhrase());
		}
		BufferedReader rd = new BufferedReader(
			new InputStreamReader(response.getEntity().getContent(),Charsets.UTF_8));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		return result.toString();
	}
	
	
	public void makeHttpPostRequest(String url, String body) throws Exception{
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
		post.addHeader("user-id", consumerConfig.consumerInit.ekstepPlatformApiUserId);
		post.addHeader("Content-Type", "application/json");
		post.setEntity(new StringEntity(body));

		HttpResponse response = client.execute(post);
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new Exception("Ekstep service unavailable: " + response.getStatusLine().getStatusCode() + " : "
					+ response.getStatusLine().getReasonPhrase());
		}

	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Map> getAllNodes(String objectType, String graphId) throws Exception {
		String url = consumerConfig.consumerInit.ekstepPlatformURI +"/taxonomy/"+graphId+"/"+objectType;
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
}
