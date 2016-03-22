package org.ekstep.searchindex.util;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.processor.CompositeSearchMessageProcessor;
import org.ekstep.searchindex.processor.IMessageProcessor;
import org.ekstep.searchindex.processor.WordCountMessageProcessor;

public class ConsumerUtil {

	private ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();
	private SearchUtil searchUtil = new SearchUtil();
	private ObjectMapper mapper = new ObjectMapper();

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
		List<Map> nodeList = searchUtil.getAllNodes(objectType, graphId);
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
}
