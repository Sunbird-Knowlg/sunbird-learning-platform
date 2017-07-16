package org.ekstep.auditLogs.controller;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.searchindex.processor.AuditHistoryMessageProcessor;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.ilimi.dac.impl.IAuditHistoryEsService;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })

public class AuditHistoryHelper {
	
	@Autowired
	IAuditHistoryEsService auditHistoryEsService;
	private AuditHistoryMessageProcessor auditMessageProcessor = new AuditHistoryMessageProcessor();
	final static String graphId = "test";

	@SuppressWarnings("unused")
	private ObjectMapper mapper = new ObjectMapper();

	final static Map<String, String> outRelationDefinition = new HashMap<>();
	final static Map<String, Object> propertyDefinition = new HashMap<>();
	final static String def_node_req = "{\"nodeGraphId\":1,\"operationType\":\"CREATE\",\"requestId\":\"2f031db5-739a-494c-95dd-455d5de6d31e\",\"graphId\":\"test\",\"userId\":\"ANONYMOUS\",\"transactionData\":{\"properties\":{\"createdOn\":{\"ov\":null,\"nv\":\"2016-06-13T08:18:59.330+0530\"},\"IL_OUT_RELATIONS_KEY\":{\"ov\":null,\"nv\":\"[{\"relationName\":\"hasAntonym\",\"objectTypes\":[\"Word\"],\"title\":\"antonyms\",\"description\":null,\"required\":false,\"renderingHints\":null},{\"relationName\":\"hasHypernym\",\"objectTypes\":[\"Word\"],\"title\":\"hypernyms\",\"description\":null,\"required\":false,\"renderingHints\":null}]\"},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Word\"},\"IL_NON_INDEXABLE_METADATA_KEY\":{\"ov\":null,\"nv\":\"[{\"required\":true,\"dataType\":\"Text\",\"propertyName\":\"lemma\",\"title\":\"Word\",\"description\":null,\"category\":null,\"displayProperty\":\"Editable\",\"range\":null,\"defaultValue\":\"\",\"renderingHints\":null,\"indexed\":false,\"draft\":false}]\"},\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DEFINITION_NODE\"},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2016-06-13T08:18:59.330+0530\"},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"DEFINITION_NODE_Word\"},\"IL_REQUIRED_PROPERTIES\":{\"ov\":null,\"nv\":[\"lemma\"]}}},\"label\":\"\",\"nodeUniqueId\":\"DEFINITION_NODE_Word\",\"nodeType\":\"DEFINITION_NODE\",\"objectType\":\"Word\"}";
	final static String create_node_req = "{\"nodeGraphId\":2,\"operationType\":\"CREATE\",\"requestId\":\"3d7b1d47-d941-4092-bccc-fe4ca77cac0f\",\"graphId\":\"test\",\"userId\":\"ANONYMOUS\",\"transactionData\":{\"properties\":{\"createdOn\":{\"ov\":null,\"nv\":\"2016-06-13T08:19:13.465+0530\"},\"lemma\":{\"ov\":null,\"nv\":\"dummyLemmaTest_1\"},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Word\"},\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2016-06-13T08:19:13.465+0530\"},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"NODEID\"}}},\"label\":\"dummyLemmaTest_1\",\"nodeUniqueId\":\"NODEID\",\"nodeType\":\"DATA_NODE\",\"objectType\":\"Word\"}";
	final static String create_node_prop_req = "{\"nodeGraphId\":2,\"operationType\":\"UPDATE\",\"requestId\":\"2d6191ed-f3fb-4a9e-aa61-865157bef664\",\"graphId\":\"test\",\"userId\":\"ANONYMOUS\",\"transactionData\":{\"removedRelations\":[{\"id\":\"NODEID2\",\"dir\":\"OUT\",\"label\":\"dummyLemma2\",\"rel\":\"hasAntonym\",\"type\":\"Word\"}],\"addedTags\":[],\"addedRelations\":[{\"id\":\"NODEID2\",\"dir\":\"OUT\",\"label\":\"dummyLemma2\",\"rel\":\"hasHypernym\",\"type\":\"Word\"},{\"id\":\"NODEID2\",\"dir\":\"OUT\",\"label\":\"dummyLemma2\",\"rel\":\"hasHypernym\",\"type\":\"Word\"}],\"properties\":{},\"removedTags\":[]},\"label\":\"dummyLemmaTest_1\",\"nodeUniqueId\":\"NODEID\",\"nodeType\":\"DATA_NODE\",\"objectType\":\"Word\"}";
	final static String update_node_req = "{\"nodeGraphId\":2,\"operationType\":\"UPDATE\",\"requestId\":\"2d6191ed-f3fb-4a9e-aa61-865157bef664\",\"graphId\":\"test\",\"userId\":\"ANONYMOUS\",\"transactionData\":{\"properties\":{\"thresholdLevel\":{\"ov\":null,\"nv\":\"5\"},\"grade\":{\"ov\":null,\"nv\":\"1\"},\"lastUpdatedOn\":{\"ov\":\"2016-06-13T08:19:13.465+0530\",\"nv\":\"2016-06-13T08:19:37.760+0530\"}}},\"label\":\"dummyLemmaTest_1\",\"nodeUniqueId\":\"NODEID\",\"nodeType\":\"DATA_NODE\",\"objectType\":\"Word\"}";
	final static String update_relation_req = "{\"nodeGraphId\":2,\"operationType\":\"UPDATE\",\"requestId\":\"2d6191ed-f3fb-4a9e-aa61-865157bef664\",\"graphId\":\"test\",\"userId\":\"ANONYMOUS\",\"transactionData\":{\"removedRelations\":[{\"id\":\"NODEID2\",\"dir\":\"OUT\",\"label\":\"dummyLemma2\",\"rel\":\"hasAntonym\",\"type\":\"Word\"}],\"addedTags\":[],\"addedRelations\":[{\"id\":\"NODEID2\",\"dir\":\"OUT\",\"label\":\"dummyLemma2\",\"rel\":\"hasHypernym\",\"type\":\"Word\"},{\"id\":\"NODEID2\",\"dir\":\"OUT\",\"label\":\"dummyLemma2\",\"rel\":\"hasHypernym\",\"type\":\"Word\"}],\"properties\":{},\"removedTags\":[]},\"label\":\"dummyLemmaTest_1\",\"nodeUniqueId\":\"NODEID\",\"nodeType\":\"DATA_NODE\",\"objectType\":\"Word\"}";
	final static String update_tag_req = "{\"nodeGraphId\":2,\"operationType\":\"UPDATE\",\"requestId\":\"78709d31-afd6-4f84-9108-22baa7717106\",\"graphId\":\"test\",\"userId\":\"ANONYMOUS\",\"transactionData\":{\"addedTags\":[\"TestLanguage\"],\"properties\":{},\"removedTags\":[]},\"label\":\"dummyLemmaTest_1\",\"nodeUniqueId\":\"NODEID\",\"nodeType\":\"DATA_NODE\",\"objectType\":\"Word\"}";

	private static String nodeId1;
	private static String nodeId2;
	
	static {
		outRelationDefinition.put("OUT_Word_hasAntonym", "antonyms");
		outRelationDefinition.put("OUT_Word_hasHypernym", "hypernyms");
		Map<String, Object> objectMap = new HashMap<>();
		objectMap.put("propertyName", "lemma");
		objectMap.put("title", "Word");
		objectMap.put("dataType", "Text");
		objectMap.put("defaultValue", "");
		objectMap.put("required", true);
		objectMap.put("indexed", true);
		propertyDefinition.put("lemma", objectMap);
		objectMap.put("propertyName", "grade");
		objectMap.put("title", "Grade");
		objectMap.put("dataType", "Multi-Select");
		objectMap.put("defaultValue", "");
		objectMap.put("required", true);
		objectMap.put("indexed", true);
		propertyDefinition.put("grade", objectMap);
	}

	public void create() {
		try {
			nodeId1 = "test_word";
			String create_node_request1 = create_node_req.replaceAll("NODEID", nodeId1);
			auditMessageProcessor.processMessage(create_node_request1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createNodeProperties() {
		try {
			nodeId2 = "test_word" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
			String create_node_prp_request = create_node_prop_req.replaceAll("NODEID", nodeId2);
			auditMessageProcessor.processMessage(create_node_prp_request);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void update() {
		try {
			String update_node_request1 = update_node_req.replaceAll("NODEID", nodeId1);
			auditMessageProcessor.processMessage(update_node_request1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateNodeRelation() {
		try {
			String update_node_relation_request = update_relation_req.replaceAll("NODEID", nodeId1)
					.replaceAll("NODEID2", nodeId2);
			auditMessageProcessor.processMessage(update_node_relation_request);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateNodeTag() {
		try {
			String update_node_tag_request = update_tag_req.replaceAll("NODEID", nodeId1);
			auditMessageProcessor.processMessage(update_node_tag_request);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
