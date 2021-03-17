package org.sunbird.itemset.publish;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.exception.ServerException;
import org.sunbird.common.mgr.ConvertToGraphNode;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.engine.common.GraphEngineTestSetup;
import org.sunbird.graph.model.node.DefinitionDTO;
import org.sunbird.itemset.handler.QuestionPaperGenerator;
import org.sunbird.itemset.handler.QuestionPaperGeneratorUtil;
import org.sunbird.learning.util.ControllerUtil;
import org.elasticsearch.action.get.MultiGetRequest.Item;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ControllerUtil.class, QuestionPaperGenerator.class, QuestionPaperGeneratorUtil.class, ItemsetPublishManagerUtil.class})
@PowerMockIgnore({"javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*" , "javax.crypto.*"})
public class ItemsetPublishManagerTest extends GraphEngineTestSetup{

ObjectMapper mapper = new ObjectMapper();
    
    
    @BeforeClass
    public static void create() throws Exception {
    		loadDefinition("definitions/content_definition.json", "definitions/item_definition.json",
    				"definitions/itemset_definition.json");
    	}

    @AfterClass
    public static void destroy() {

    }

    //Publish with Empty list of itemset - return null
    @Test
    public void testPublish1() throws Exception{
    	
    		ControllerUtil controllerUtil = PowerMockito.mock(ControllerUtil.class);
    		ItemsetPublishManager itemsetPublishManager = new ItemsetPublishManager(controllerUtil);
    		List<String> itemsetsList = null;
    		Assert.assertNull(itemsetPublishManager.publish(itemsetsList));
    		
    		itemsetsList = new ArrayList<String>();
    		Assert.assertNull(itemsetPublishManager.publish(itemsetsList));
    }
    
    //Publish with no question linked to Itemset - return null
    @Test
    public void testPublish2() throws Exception{
    	
		List<String> itemsetsList = Arrays.asList("do_11292667205373132811");
		
		ControllerUtil controllerUtil = PowerMockito.mock(ControllerUtil.class);
		ItemsetPublishManager itemsetPublishManager = new ItemsetPublishManager(controllerUtil);
		
		String itemSetNodeString = "{\"identifier\":\"do_11292667205373132811\",\"objectType\":\"ItemSet\",\"graphId\":\"domain\",\"owner\":\"Ilimi\",\"lastStatusChangedOn\":\"2020-01-02T16:31:37.174+0530\",\"code\":\"akshara.grade5.ws1.test\",\"previewUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/itemset/do_11292667205373132811/do_11292667205373132811_html_1578418828219.html\",\"channel\":\"in.ekstep\",\"SET_OBJECT_TYPE_KEY\":\"AssessmentItem\",\"description\":\"Akshara Worksheet Grade 5 Item Set\",\"language\":[\"English\"],\"title\":\"Akshara Worksheet Grade 5 Item Set\",\"type\":\"materialised\",\"total_items\":1,\"createdOn\":\"2020-01-02T16:31:37.174+0530\",\"versionKey\":\"1578418841273\",\"gradeLevel\":[\"Grade 1\"],\"max_score\":1,\"lastUpdatedOn\":\"2020-01-07T23:10:41.273+0530\",\"used_for\":\"assessment\",\"SET_TYPE\":\"MATERIALISED_SET\",\"status\":\"Live\"}";
		Map<String, Object> itemSetNodeMap = mapper.readValue(itemSetNodeString, Map.class);
		DefinitionDTO itemSetDefinition = new ControllerUtil().getDefinition("domain", "ItemSet");
		Node itemSetNode = ConvertToGraphNode.convertToGraphNode(itemSetNodeMap, itemSetDefinition, null);
		PowerMockito.when(controllerUtil.getNode(Mockito.anyString(), Mockito.anyString())).thenReturn(itemSetNode);
		
		Assert.assertNull(itemsetPublishManager.publish(itemsetsList));
		
    }
    
    //Publish with invalid objectType question linked to Itemset - throw ServerException
    @Test
    public void testPublish3() throws Exception{
    	
		List<String> itemsetsList = Arrays.asList("do_11292667205373132811");
		
		ControllerUtil controllerUtil = PowerMockito.mock(ControllerUtil.class);
		ItemsetPublishManager itemsetPublishManager = new ItemsetPublishManager(controllerUtil);
		
		String itemSetNodeString = "{\"identifier\":\"do_11292667205373132811\",\"objectType\":\"ItemSet\",\"owner\":\"Ilimi\",\"lastStatusChangedOn\":\"2020-01-02T16:31:37.174+0530\",\"code\":\"akshara.grade5.ws1.test\",\"previewUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/itemset/do_11292667205373132811/do_11292667205373132811_html_1578418828219.html\",\"channel\":\"in.ekstep\",\"SET_OBJECT_TYPE_KEY\":\"AssessmentItem\",\"description\":\"Akshara Worksheet Grade 5 Item Set\",\"language\":[\"English\"],\"title\":\"Akshara Worksheet Grade 5 Item Set\",\"type\":\"materialised\",\"total_items\":1,\"createdOn\":\"2020-01-02T16:31:37.174+0530\",\"versionKey\":\"1578418841273\",\"gradeLevel\":[\"Grade 1\"],\"max_score\":1,\"lastUpdatedOn\":\"2020-01-07T23:10:41.273+0530\",\"used_for\":\"assessment\",\"SET_TYPE\":\"MATERIALISED_SET\",\"status\":\"Live\",\"items\":[{\"identifier\":\"test.mcq_test_mcq_101\",\"index\":1}]}";
		Map<String, Object> itemSetNodeMap = mapper.readValue(itemSetNodeString, Map.class);
		DefinitionDTO itemSetDefinition = new ControllerUtil().getDefinition("domain", "ItemSet");
		Node itemSetNode = ConvertToGraphNode.convertToGraphNode(itemSetNodeMap, itemSetDefinition, null);
		PowerMockito.when(controllerUtil.getNode(Mockito.anyString(), Mockito.anyString())).thenReturn(itemSetNode);
		
		Assert.assertNull(itemsetPublishManager.publish(itemsetsList));
	}
    
    //Publish with invalid question linked to Itemset - throw ServerException
    @Rule
	@Test(expected = ServerException.class)
    public void testPublish4() throws Exception{
    	
		List<String> itemsetsList = Arrays.asList("do_11292667205373132811");
		
		ControllerUtil controllerUtil = PowerMockito.mock(ControllerUtil.class);
		ItemsetPublishManager itemsetPublishManager = new ItemsetPublishManager(controllerUtil);
		
		String itemSetNodeString = "{\"identifier\":\"do_11292667205373132811\",\"objectType\":\"ItemSet\",\"owner\":\"Ilimi\",\"lastStatusChangedOn\":\"2020-01-02T16:31:37.174+0530\",\"code\":\"akshara.grade5.ws1.test\",\"previewUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/itemset/do_11292667205373132811/do_11292667205373132811_html_1578418828219.html\",\"channel\":\"in.ekstep\",\"SET_OBJECT_TYPE_KEY\":\"AssessmentItem\",\"description\":\"Akshara Worksheet Grade 5 Item Set\",\"language\":[\"English\"],\"title\":\"Akshara Worksheet Grade 5 Item Set\",\"type\":\"materialised\",\"total_items\":1,\"createdOn\":\"2020-01-02T16:31:37.174+0530\",\"versionKey\":\"1578418841273\",\"gradeLevel\":[\"Grade 1\"],\"max_score\":1,\"lastUpdatedOn\":\"2020-01-07T23:10:41.273+0530\",\"used_for\":\"assessment\",\"SET_TYPE\":\"MATERIALISED_SET\",\"status\":\"Live\",\"items\":[{\"identifier\":\"test.mcq_test_mcq_101\",\"index\":1}]}";
		Map<String, Object> itemSetNodeMap = mapper.readValue(itemSetNodeString, Map.class);
		DefinitionDTO itemSetDefinition = new ControllerUtil().getDefinition("domain", "ItemSet");
		Node itemSetNode = ConvertToGraphNode.convertToGraphNode(itemSetNodeMap, itemSetDefinition, null);
		itemSetNode.getOutRelations().forEach(x -> x.setEndNodeObjectType("AssessmentItem"));
		PowerMockito.when(controllerUtil.getNode(Mockito.anyString(), Mockito.anyString())).thenReturn(itemSetNode);
		
		Response getDataNodesResponse = new Response();
		getDataNodesResponse.setResponseCode(ResponseCode.RESOURCE_NOT_FOUND);
		PowerMockito.when(controllerUtil.getDataNodes(Mockito.anyString(), Mockito.anyList())).thenReturn(getDataNodesResponse);
		String url = itemsetPublishManager.publish(itemsetsList);
	}
    
    //Publish with valid question linked to Itemset, but update to question metadata failed - throw ServerException
    @Rule
	@Test(expected = ServerException.class)
    public void testPublish5() throws Exception{
    	
		List<String> itemsetsList = Arrays.asList("do_11292667205373132811");
		
		ControllerUtil controllerUtil = PowerMockito.mock(ControllerUtil.class);
		ItemsetPublishManager itemsetPublishManager = new ItemsetPublishManager(controllerUtil);
		
		String itemSetNodeString = "{\"identifier\":\"do_11292667205373132811\",\"objectType\":\"ItemSet\",\"owner\":\"Ilimi\",\"lastStatusChangedOn\":\"2020-01-02T16:31:37.174+0530\",\"code\":\"akshara.grade5.ws1.test\",\"previewUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/itemset/do_11292667205373132811/do_11292667205373132811_html_1578418828219.html\",\"channel\":\"in.ekstep\",\"SET_OBJECT_TYPE_KEY\":\"AssessmentItem\",\"description\":\"Akshara Worksheet Grade 5 Item Set\",\"language\":[\"English\"],\"title\":\"Akshara Worksheet Grade 5 Item Set\",\"type\":\"materialised\",\"total_items\":1,\"createdOn\":\"2020-01-02T16:31:37.174+0530\",\"versionKey\":\"1578418841273\",\"gradeLevel\":[\"Grade 1\"],\"max_score\":1,\"lastUpdatedOn\":\"2020-01-07T23:10:41.273+0530\",\"used_for\":\"assessment\",\"SET_TYPE\":\"MATERIALISED_SET\",\"status\":\"Live\",\"items\":[{\"identifier\":\"test.mcq_test_mcq_101\",\"index\":1}]}";
		Map<String, Object> itemSetNodeMap = mapper.readValue(itemSetNodeString, Map.class);
		DefinitionDTO itemSetDefinition = new ControllerUtil().getDefinition("domain", "ItemSet");
		Node itemSetNode = ConvertToGraphNode.convertToGraphNode(itemSetNodeMap, itemSetDefinition, null);
		itemSetNode.getOutRelations().forEach(x -> x.setEndNodeObjectType("AssessmentItem"));
		PowerMockito.when(controllerUtil.getNode(Mockito.anyString(), Mockito.anyString())).thenReturn(itemSetNode);
		
		String itemNodeString = "{\"identifier\":\"test.mcq_test_mcq_101\",\"objectType\":\"AssessmentItem\",\"type\":\"mcq\",\"name\":\"Mmcq Question 2\",\"status\":\"Draft\"}";
		Map<String, Object> itemNodeMap = mapper.readValue(itemNodeString, Map.class);
		DefinitionDTO itemDefinition = new ControllerUtil().getDefinition("domain", "AssessmentItem");
		Node itemNode = ConvertToGraphNode.convertToGraphNode(itemNodeMap, itemDefinition, null);
		Response getDataNodesResponse = new Response();
		getDataNodesResponse.getResult().put("node_list", Arrays.asList(itemNode));
		PowerMockito.when(controllerUtil.getDataNodes(Mockito.anyString(), Mockito.anyList())).thenReturn(getDataNodesResponse);
		
		Response updateNodesResponse = new Response();
		updateNodesResponse.setResponseCode(ResponseCode.SERVER_ERROR);
		PowerMockito.when(controllerUtil.updateNodes(Mockito.anyList(), Mockito.anyMap())).thenReturn(updateNodesResponse);
		
		String url = itemsetPublishManager.publish(itemsetsList);
	}
    
    //Publish with valid question linked to Itemset, update to question metadata passed, retired question removed - but HTML file generation return null - Throw server exception
    @Rule
	@Test(expected = ServerException.class)
    public void testPublish6() throws Exception{
    	
		List<String> itemsetsList = Arrays.asList("do_11292667205373132811");
		
		ControllerUtil controllerUtil = PowerMockito.mock(ControllerUtil.class);
		ItemsetPublishManager itemsetPublishManager = new ItemsetPublishManager(controllerUtil);
		
		String itemSetNodeString = "{\"identifier\":\"do_11292667205373132811\",\"objectType\":\"ItemSet\",\"owner\":\"Ilimi\",\"lastStatusChangedOn\":\"2020-01-02T16:31:37.174+0530\",\"code\":\"akshara.grade5.ws1.test\",\"previewUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/itemset/do_11292667205373132811/do_11292667205373132811_html_1578418828219.html\",\"channel\":\"in.ekstep\",\"SET_OBJECT_TYPE_KEY\":\"AssessmentItem\",\"description\":\"Akshara Worksheet Grade 5 Item Set\",\"language\":[\"English\"],\"title\":\"Akshara Worksheet Grade 5 Item Set\",\"type\":\"materialised\",\"total_items\":1,\"createdOn\":\"2020-01-02T16:31:37.174+0530\",\"versionKey\":\"1578418841273\",\"gradeLevel\":[\"Grade 1\"],\"max_score\":1,\"lastUpdatedOn\":\"2020-01-07T23:10:41.273+0530\",\"used_for\":\"assessment\",\"SET_TYPE\":\"MATERIALISED_SET\",\"status\":\"Live\",\"items\":[{\"identifier\":\"test.mcq_test_mcq_101\",\"index\":1},{\"identifier\":\"test.mcq_test_mcq_102\",\"index\":2}]}";
		Map<String, Object> itemSetNodeMap = mapper.readValue(itemSetNodeString, Map.class);
		DefinitionDTO itemSetDefinition = new ControllerUtil().getDefinition("domain", "ItemSet");
		Node itemSetNode = ConvertToGraphNode.convertToGraphNode(itemSetNodeMap, itemSetDefinition, null);
		itemSetNode.getOutRelations().forEach(x -> x.setEndNodeObjectType("AssessmentItem"));
		PowerMockito.when(controllerUtil.getNode(Mockito.anyString(), Mockito.anyString())).thenReturn(itemSetNode);
		
		String itemNodeString1 = "{\"identifier\":\"test.mcq_test_mcq_101\",\"objectType\":\"AssessmentItem\",\"type\":\"mcq\",\"name\":\"Mmcq Question 2\",\"status\":\"Draft\"}";
		String itemNodeString2 = "{\"identifier\":\"test.mcq_test_mcq_102\",\"objectType\":\"AssessmentItem\",\"type\":\"mcq\",\"name\":\"Mmcq Question 2\",\"status\":\"Retired\"}";
		Map<String, Object> itemNodeMap1 = mapper.readValue(itemNodeString1, Map.class);
		Map<String, Object> itemNodeMap2 = mapper.readValue(itemNodeString2, Map.class);
		DefinitionDTO itemDefinition = new ControllerUtil().getDefinition("domain", "AssessmentItem");
		Node itemNode1 = ConvertToGraphNode.convertToGraphNode(itemNodeMap1, itemDefinition, null);
		Node itemNode2 = ConvertToGraphNode.convertToGraphNode(itemNodeMap2, itemDefinition, null);
		Response getDataNodesResponse = new Response();
		getDataNodesResponse.getResult().put("node_list", Arrays.asList(itemNode1, itemNode2));
		PowerMockito.when(controllerUtil.getDataNodes(Mockito.anyString(), Mockito.anyList())).thenReturn(getDataNodesResponse);
		
		Response updateNodesResponse = new Response();
		PowerMockito.when(controllerUtil.updateNodes(Mockito.anyList(), Mockito.anyMap())).thenReturn(updateNodesResponse);
		
		PowerMockito.mockStatic(QuestionPaperGenerator.class);
		PowerMockito.when(QuestionPaperGenerator.generateQuestionPaper(Mockito.any())).thenReturn(null);
		
		
		String url = itemsetPublishManager.publish(itemsetsList);
	}
    
    //Publish with valid question linked to Itemset, update to question metadata passed, retired question removed - but HTML file generated - upload file failed - Throw server exception
    @Rule
	@Test(expected = ServerException.class)
    public void testPublish7() throws Exception{
    	
		List<String> itemsetsList = Arrays.asList("do_11292667205373132811");
		
		ControllerUtil controllerUtil = PowerMockito.mock(ControllerUtil.class);
		ItemsetPublishManager itemsetPublishManager = new ItemsetPublishManager(controllerUtil);
		
		String itemSetNodeString = "{\"identifier\":\"do_11292667205373132811\",\"objectType\":\"ItemSet\",\"owner\":\"Ilimi\",\"lastStatusChangedOn\":\"2020-01-02T16:31:37.174+0530\",\"code\":\"akshara.grade5.ws1.test\",\"previewUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/itemset/do_11292667205373132811/do_11292667205373132811_html_1578418828219.html\",\"channel\":\"in.ekstep\",\"SET_OBJECT_TYPE_KEY\":\"AssessmentItem\",\"description\":\"Akshara Worksheet Grade 5 Item Set\",\"language\":[\"English\"],\"title\":\"Akshara Worksheet Grade 5 Item Set\",\"type\":\"materialised\",\"total_items\":1,\"createdOn\":\"2020-01-02T16:31:37.174+0530\",\"versionKey\":\"1578418841273\",\"gradeLevel\":[\"Grade 1\"],\"max_score\":1,\"lastUpdatedOn\":\"2020-01-07T23:10:41.273+0530\",\"used_for\":\"assessment\",\"SET_TYPE\":\"MATERIALISED_SET\",\"status\":\"Live\",\"items\":[{\"identifier\":\"test.mcq_test_mcq_101\",\"index\":1},{\"identifier\":\"test.mcq_test_mcq_102\",\"index\":2}]}";
		Map<String, Object> itemSetNodeMap = mapper.readValue(itemSetNodeString, Map.class);
		DefinitionDTO itemSetDefinition = new ControllerUtil().getDefinition("domain", "ItemSet");
		Node itemSetNode = ConvertToGraphNode.convertToGraphNode(itemSetNodeMap, itemSetDefinition, null);
		itemSetNode.getOutRelations().forEach(x -> x.setEndNodeObjectType("AssessmentItem"));
		PowerMockito.when(controllerUtil.getNode(Mockito.anyString(), Mockito.anyString())).thenReturn(itemSetNode);
		
		String itemNodeString1 = "{\"identifier\":\"test.mcq_test_mcq_101\",\"objectType\":\"AssessmentItem\",\"type\":\"mcq\",\"name\":\"Mmcq Question 2\",\"status\":\"Draft\"}";
		String itemNodeString2 = "{\"identifier\":\"test.mcq_test_mcq_102\",\"objectType\":\"AssessmentItem\",\"type\":\"mcq\",\"name\":\"Mmcq Question 2\",\"status\":\"Retired\"}";
		Map<String, Object> itemNodeMap1 = mapper.readValue(itemNodeString1, Map.class);
		Map<String, Object> itemNodeMap2 = mapper.readValue(itemNodeString2, Map.class);
		DefinitionDTO itemDefinition = new ControllerUtil().getDefinition("domain", "AssessmentItem");
		Node itemNode1 = ConvertToGraphNode.convertToGraphNode(itemNodeMap1, itemDefinition, null);
		Node itemNode2 = ConvertToGraphNode.convertToGraphNode(itemNodeMap2, itemDefinition, null);
		Response getDataNodesResponse = new Response();
		getDataNodesResponse.getResult().put("node_list", Arrays.asList(itemNode1, itemNode2));
		PowerMockito.when(controllerUtil.getDataNodes(Mockito.anyString(), Mockito.anyList())).thenReturn(getDataNodesResponse);
		
		Response updateNodesResponse = new Response();
		PowerMockito.when(controllerUtil.updateNodes(Mockito.anyList(), Mockito.anyMap())).thenReturn(updateNodesResponse);
		
		PowerMockito.mockStatic(QuestionPaperGenerator.class);
		PowerMockito.when(QuestionPaperGenerator.generateQuestionPaper(Mockito.any())).thenReturn(new File("/tmp/previewUrl.html"));
		
		PowerMockito.mockStatic(ItemsetPublishManagerUtil.class);
		PowerMockito.when(ItemsetPublishManagerUtil.uploadFileToCloud(Mockito.any(), Mockito.anyString())).thenReturn(null);
		
		String url = itemsetPublishManager.publish(itemsetsList);
	}
    
    //Publish with valid question linked to Itemset, update to question metadata passed, retired question removed - but HTML file generated - file uploaded - itemset updated failed - Throw server exception
    @Rule
	@Test(expected = ServerException.class)
    public void testPublish8() throws Exception{
    	
		List<String> itemsetsList = Arrays.asList("do_11292667205373132811");
		
		ControllerUtil controllerUtil = PowerMockito.mock(ControllerUtil.class);
		ItemsetPublishManager itemsetPublishManager = new ItemsetPublishManager(controllerUtil);
		
		String itemSetNodeString = "{\"identifier\":\"do_11292667205373132811\",\"objectType\":\"ItemSet\",\"owner\":\"Ilimi\",\"lastStatusChangedOn\":\"2020-01-02T16:31:37.174+0530\",\"code\":\"akshara.grade5.ws1.test\",\"previewUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/itemset/do_11292667205373132811/do_11292667205373132811_html_1578418828219.html\",\"channel\":\"in.ekstep\",\"SET_OBJECT_TYPE_KEY\":\"AssessmentItem\",\"description\":\"Akshara Worksheet Grade 5 Item Set\",\"language\":[\"English\"],\"title\":\"Akshara Worksheet Grade 5 Item Set\",\"type\":\"materialised\",\"total_items\":1,\"createdOn\":\"2020-01-02T16:31:37.174+0530\",\"versionKey\":\"1578418841273\",\"gradeLevel\":[\"Grade 1\"],\"max_score\":1,\"lastUpdatedOn\":\"2020-01-07T23:10:41.273+0530\",\"used_for\":\"assessment\",\"SET_TYPE\":\"MATERIALISED_SET\",\"status\":\"Live\",\"items\":[{\"identifier\":\"test.mcq_test_mcq_101\",\"index\":1},{\"identifier\":\"test.mcq_test_mcq_102\",\"index\":2}]}";
		Map<String, Object> itemSetNodeMap = mapper.readValue(itemSetNodeString, Map.class);
		DefinitionDTO itemSetDefinition = new ControllerUtil().getDefinition("domain", "ItemSet");
		Node itemSetNode = ConvertToGraphNode.convertToGraphNode(itemSetNodeMap, itemSetDefinition, null);
		itemSetNode.getOutRelations().forEach(x -> x.setEndNodeObjectType("AssessmentItem"));
		PowerMockito.when(controllerUtil.getNode(Mockito.anyString(), Mockito.anyString())).thenReturn(itemSetNode);
		
		String itemNodeString1 = "{\"identifier\":\"test.mcq_test_mcq_101\",\"objectType\":\"AssessmentItem\",\"type\":\"mcq\",\"name\":\"Mmcq Question 2\",\"status\":\"Draft\"}";
		String itemNodeString2 = "{\"identifier\":\"test.mcq_test_mcq_102\",\"objectType\":\"AssessmentItem\",\"type\":\"mcq\",\"name\":\"Mmcq Question 2\",\"status\":\"Retired\"}";
		Map<String, Object> itemNodeMap1 = mapper.readValue(itemNodeString1, Map.class);
		Map<String, Object> itemNodeMap2 = mapper.readValue(itemNodeString2, Map.class);
		DefinitionDTO itemDefinition = new ControllerUtil().getDefinition("domain", "AssessmentItem");
		Node itemNode1 = ConvertToGraphNode.convertToGraphNode(itemNodeMap1, itemDefinition, null);
		Node itemNode2 = ConvertToGraphNode.convertToGraphNode(itemNodeMap2, itemDefinition, null);
		Response getDataNodesResponse = new Response();
		getDataNodesResponse.getResult().put("node_list", Arrays.asList(itemNode1, itemNode2));
		PowerMockito.when(controllerUtil.getDataNodes(Mockito.anyString(), Mockito.anyList())).thenReturn(getDataNodesResponse);
		
		Response updateNodesResponse = new Response();
		PowerMockito.when(controllerUtil.updateNodes(Mockito.anyList(), Mockito.anyMap())).thenReturn(updateNodesResponse);
		
		PowerMockito.mockStatic(QuestionPaperGenerator.class);
		PowerMockito.when(QuestionPaperGenerator.generateQuestionPaper(Mockito.any())).thenReturn(new File("/tmp/previewUrl.html"));
		
		PowerMockito.mockStatic(ItemsetPublishManagerUtil.class);
		PowerMockito.when(ItemsetPublishManagerUtil.uploadFileToCloud(Mockito.any(), Mockito.anyString())).thenReturn("previewUrl.html");
		
		Response updateNodeResponse = new Response();
		updateNodeResponse.setResponseCode(ResponseCode.SERVER_ERROR);
		PowerMockito.when(controllerUtil.updateNode(Mockito.any())).thenReturn(updateNodeResponse);
		
		String url = itemsetPublishManager.publish(itemsetsList);
	}
    
    //Publish with valid question linked to Itemset, update to question metadata passed, retired question removed - but HTML file generated - file uploaded - itemset update passed - return html file url link
    @Test
    public void testPublish9() throws Exception{
    	
		List<String> itemsetsList = Arrays.asList("do_11292667205373132811");
		
		ControllerUtil controllerUtil = PowerMockito.mock(ControllerUtil.class);
		ItemsetPublishManager itemsetPublishManager = new ItemsetPublishManager(controllerUtil);
		
		String itemSetNodeString = "{\"identifier\":\"do_11292667205373132811\",\"objectType\":\"ItemSet\",\"owner\":\"Ilimi\",\"lastStatusChangedOn\":\"2020-01-02T16:31:37.174+0530\",\"code\":\"akshara.grade5.ws1.test\",\"previewUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/itemset/do_11292667205373132811/do_11292667205373132811_html_1578418828219.html\",\"channel\":\"in.ekstep\",\"SET_OBJECT_TYPE_KEY\":\"AssessmentItem\",\"description\":\"Akshara Worksheet Grade 5 Item Set\",\"language\":[\"English\"],\"title\":\"Akshara Worksheet Grade 5 Item Set\",\"type\":\"materialised\",\"total_items\":1,\"createdOn\":\"2020-01-02T16:31:37.174+0530\",\"versionKey\":\"1578418841273\",\"gradeLevel\":[\"Grade 1\"],\"max_score\":1,\"lastUpdatedOn\":\"2020-01-07T23:10:41.273+0530\",\"used_for\":\"assessment\",\"SET_TYPE\":\"MATERIALISED_SET\",\"status\":\"Live\",\"items\":[{\"identifier\":\"test.mcq_test_mcq_101\",\"index\":1},{\"identifier\":\"test.mcq_test_mcq_102\",\"index\":2}]}";
		Map<String, Object> itemSetNodeMap = mapper.readValue(itemSetNodeString, Map.class);
		DefinitionDTO itemSetDefinition = new ControllerUtil().getDefinition("domain", "ItemSet");
		Node itemSetNode = ConvertToGraphNode.convertToGraphNode(itemSetNodeMap, itemSetDefinition, null);
		itemSetNode.getOutRelations().forEach(x -> x.setEndNodeObjectType("AssessmentItem"));
		PowerMockito.when(controllerUtil.getNode(Mockito.anyString(), Mockito.anyString())).thenReturn(itemSetNode);
		
		String itemNodeString1 = "{\"identifier\":\"test.mcq_test_mcq_101\",\"objectType\":\"AssessmentItem\",\"type\":\"mcq\",\"name\":\"Mmcq Question 2\",\"status\":\"Draft\"}";
		String itemNodeString2 = "{\"identifier\":\"test.mcq_test_mcq_102\",\"objectType\":\"AssessmentItem\",\"type\":\"mcq\",\"name\":\"Mmcq Question 2\",\"status\":\"Retired\"}";
		Map<String, Object> itemNodeMap1 = mapper.readValue(itemNodeString1, Map.class);
		Map<String, Object> itemNodeMap2 = mapper.readValue(itemNodeString2, Map.class);
		DefinitionDTO itemDefinition = new ControllerUtil().getDefinition("domain", "AssessmentItem");
		Node itemNode1 = ConvertToGraphNode.convertToGraphNode(itemNodeMap1, itemDefinition, null);
		Node itemNode2 = ConvertToGraphNode.convertToGraphNode(itemNodeMap2, itemDefinition, null);
		Response getDataNodesResponse = new Response();
		getDataNodesResponse.getResult().put("node_list", Arrays.asList(itemNode1, itemNode2));
		PowerMockito.when(controllerUtil.getDataNodes(Mockito.anyString(), Mockito.anyList())).thenReturn(getDataNodesResponse);
		
		Response updateNodesResponse = new Response();
		PowerMockito.when(controllerUtil.updateNodes(Mockito.anyList(), Mockito.anyMap())).thenReturn(updateNodesResponse);
		
		PowerMockito.mockStatic(QuestionPaperGenerator.class);
		PowerMockito.when(QuestionPaperGenerator.generateQuestionPaper(Mockito.any())).thenReturn(new File("/tmp/previewUrl.html"));
		
		PowerMockito.mockStatic(ItemsetPublishManagerUtil.class);
		PowerMockito.when(ItemsetPublishManagerUtil.uploadFileToCloud(Mockito.any(), Mockito.anyString())).thenReturn("previewUrl.html");
		
		Response updateNodeResponse = new Response();
		PowerMockito.when(controllerUtil.updateNode(Mockito.any())).thenReturn(updateNodeResponse);
		
		String url = itemsetPublishManager.publish(itemsetsList);
		Assert.assertNotNull(url);
		Assert.assertEquals("previewUrl.html", url);
	}
}
