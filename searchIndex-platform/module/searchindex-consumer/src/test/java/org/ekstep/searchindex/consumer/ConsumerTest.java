package org.ekstep.searchindex.consumer;

import java.util.Collection;
import java.util.Properties;

import org.apache.commons.collections.CollectionUtils;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.searchindex.util.PropertiesUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.router.RequestRouterPool;
import com.ilimi.dac.enums.CommonDACParams;
import com.ilimi.dac.impl.IAuditHistoryDataService;

import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class ConsumerTest {

	//@Autowired 
	//private WebApplicationContext context;
	@Autowired
	IAuditHistoryDataService auditHistoryDataService;
	private ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();
	private static ProducerConfig producerConfig;
	private static String TOPIC = PropertiesUtil.getProperty("topic");
	final static String graphId = "test";
	
	static {
        RequestRouterPool.getActorSystem();
        //LanguageRequestRouterPool.init();
        try {
			ConsumerRunner.startConsumers();
		} catch (Exception e) {
			//throw new ServletException(e);
		} 
	}
	
	final static String def_node_req = "{\"nodeGraphId\":1,\"operationType\":\"CREATE\",\"requestId\":\"2f031db5-739a-494c-95dd-455d5de6d31e\",\"graphId\":\"test\",\"userId\":\"ANONYMOUS\",\"transactionData\":{\"properties\":{\"createdOn\":{\"ov\":null,\"nv\":\"2016-06-13T08:18:59.330+0530\"},\"IL_OUT_RELATIONS_KEY\":{\"ov\":null,\"nv\":\"[{\"relationName\":\"hasAntonym\",\"objectTypes\":[\"Word\"],\"title\":\"antonyms\",\"description\":null,\"required\":false,\"renderingHints\":null},{\"relationName\":\"hasHypernym\",\"objectTypes\":[\"Word\"],\"title\":\"hypernyms\",\"description\":null,\"required\":false,\"renderingHints\":null}]\"},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Word\"},\"IL_NON_INDEXABLE_METADATA_KEY\":{\"ov\":null,\"nv\":\"[{\"required\":true,\"dataType\":\"Text\",\"propertyName\":\"lemma\",\"title\":\"Word\",\"description\":null,\"category\":null,\"displayProperty\":\"Editable\",\"range\":null,\"defaultValue\":\"\",\"renderingHints\":null,\"indexed\":false,\"draft\":false}]\"},\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DEFINITION_NODE\"},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2016-06-13T08:18:59.330+0530\"},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"DEFINITION_NODE_Word\"},\"IL_REQUIRED_PROPERTIES\":{\"ov\":null,\"nv\":[\"lemma\"]}}},\"label\":\"\",\"nodeUniqueId\":\"DEFINITION_NODE_Word\",\"nodeType\":\"DEFINITION_NODE\",\"objectType\":\"Word\"}";
	final static String create_node_req = "{\"nodeGraphId\":2,\"operationType\":\"CREATE\",\"requestId\":\"3d7b1d47-d941-4092-bccc-fe4ca77cac0f\",\"graphId\":\"test\",\"userId\":\"ANONYMOUS\",\"transactionData\":{\"properties\":{\"createdOn\":{\"ov\":null,\"nv\":\"2016-06-13T08:19:13.465+0530\"},\"lemma\":{\"ov\":null,\"nv\":\"dummyLemmaTest_1\"},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Word\"},\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2016-06-13T08:19:13.465+0530\"},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":$NODE_ID}}},\"label\":\"dummyLemmaTest_1\",\"nodeUniqueId\":$NODE_ID,\"nodeType\":\"DATA_NODE\",\"objectType\":\"Word\"}";
	final static String update_node_req = "{\"nodeGraphId\":2,\"operationType\":\"UPDATE\",\"requestId\":\"2d6191ed-f3fb-4a9e-aa61-865157bef664\",\"graphId\":\"test\",\"userId\":\"ANONYMOUS\",\"transactionData\":{\"properties\":{\"thresholdLevel\":{\"ov\":null,\"nv\":\"5\"},\"grade\":{\"ov\":null,\"nv\":\"1\"},\"lastUpdatedOn\":{\"ov\":\"2016-06-13T08:19:13.465+0530\",\"nv\":\"2016-06-13T08:19:37.760+0530\"}}},\"label\":\"dummyLemmaTest_1\",\"nodeUniqueId\":\"test_word1\",\"nodeType\":\"DATA_NODE\",\"objectType\":\"Word\"}";
	
	@BeforeClass
	public static void setup(){
		producerConfig = new ProducerConfig(getProperties());
		send(def_node_req);
		//Thread.sleep(5000);
	}
	
	@Test
	public void create(){
		try {
			String nodeId = "test_word" +System.currentTimeMillis()+"_"+Thread.currentThread().getId();
			String create_node_request1=create_node_req.replace("$NODE_ID", "test_word1");
			send(create_node_req);
			Thread.sleep(10000);
			 String documentJson = elasticSearchUtil.getDocumentAsStringById(
						CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
						CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, nodeId);

		        //if (documentJson == null || !documentJson.isEmpty())
		        Assert.assertNotNull("Node is not inserted into elasticSearch index", documentJson);
		        Assert.assertFalse("Node is not inserted into elasticSearch index", documentJson.isEmpty());
		    	Request request = new Request();
		    	request.put(CommonDACParams.graph_id.name(), graphId);
		    	request.put(CommonDACParams.object_id.name(), nodeId);
		        Response response = auditHistoryDataService.getAuditHistoryLogByObjectId(request);
		        Assert.assertNull( "Node is not inserted into AuditLogs", response.get(CommonDACParams.audit_history_record.name()));
		        Assert.assertFalse("Node is not inserted into AuditLogs", CollectionUtils.isEmpty((Collection) response.get(CommonDACParams.audit_history_record.name())));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void send(String messageBody) {
        kafka.javaapi.producer.Producer<String,String> producer = new kafka.javaapi.producer.Producer<String, String>(producerConfig);
        KeyedMessage<String, String> message = new KeyedMessage<String, String>(TOPIC, messageBody);
        producer.send(message);
        producer.close();
	}
	
    private static Properties getProperties()  { 
    	Properties properties = new Properties();
        properties.put("metadata.broker.list","localhost:9092");
        properties.put("serializer.class","kafka.serializer.StringEncoder");
        return properties;
    }
    
}
