package org.ekstep.content.operation.finalizer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.mgr.ConvertToGraphNode;
import org.ekstep.common.util.HttpRestUtil;
import org.ekstep.content.util.GraphUtil;
import org.ekstep.content.util.SyncMessageGenerator;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.engine.common.GraphEngineTestSetup;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.itemset.publish.ItemsetPublishManager;
import org.ekstep.kafka.KafkaClient;
import org.ekstep.learning.hierarchy.store.HierarchyStore;
import org.ekstep.learning.util.CloudStore;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
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
import org.ekstep.content.util.PublishFinalizeUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ItemsetPublishManager.class, HttpRestUtil.class, CloudStore.class, PublishFinalizeUtil.class, ElasticSearchUtil.class, KafkaClient.class, SyncMessageGenerator.class, GraphUtil.class})
@PowerMockIgnore({ "javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*", "javax.crypto.*" })
public class PublishFinalizerTest extends GraphEngineTestSetup {

	ObjectMapper mapper = new ObjectMapper();

	@BeforeClass
	public static void create() throws Exception {
		loadDefinition("definitions/content_definition.json", "definitions/item_definition.json",
				"definitions/itemset_definition.json");
	}

	@AfterClass
	public static void destroy() {

	}

	// Content with no outRelations - return null
	@Test
	public void TestGetItemsetPreviewUrl1() throws Exception {
		String contentNodeString = "{\"identifier\":\"do_11292666508456755211\",\"objectType\":\"Content\",\"code\":\"Test_PDF\",\"channel\":\"channel-1\",\"mimeType\":\"application/pdf\",\"versionKey\":\"1578381263182\",\"name\":\"Test_PDF\",\"status\":\"Live\"}";
		Map<String, Object> contentNodeMap = mapper.readValue(contentNodeString, HashMap.class);
		DefinitionDTO contentDefinition = new ControllerUtil().getDefinition("domain", "Content");
		Node contentNode = ConvertToGraphNode.convertToGraphNode(contentNodeMap, contentDefinition, null);
		PublishFinalizer publishFinalizer = new PublishFinalizer("/tmp", "do_11292666508456755211");
		String url = publishFinalizer.getItemsetPreviewUrl(contentNode);
		Assert.assertNull(url);
		// PowerMockito.when(itemsetPublishManager.publish(Mockito.anyList())).thenReturn(null);
	}

	// Content with outRelations but no ItemSet as outRelation - return null
	@Test
	public void TestGetItemsetPreviewUrl2() throws Exception {
		String contentNodeString = "{\"identifier\":\"do_11292666508456755211\",\"objectType\":\"Content\",\"code\":\"Test_PDF\",\"channel\":\"channel-1\",\"mimeType\":\"application/pdf\",\"versionKey\":\"1578381263182\",\"name\":\"Test_PDF\",\"status\":\"Live\",\"children\":[{\"identifier\":\"do_11292667205373132811\"}]}";
		Map<String, Object> contentNodeMap = mapper.readValue(contentNodeString, HashMap.class);
		DefinitionDTO contentDefinition = new ControllerUtil().getDefinition("domain", "Content");
		Node contentNode = ConvertToGraphNode.convertToGraphNode(contentNodeMap, contentDefinition, null);
		PublishFinalizer publishFinalizer = new PublishFinalizer("/tmp", "do_11292666508456755211");
		String url = publishFinalizer.getItemsetPreviewUrl(contentNode);
		Assert.assertNull(url);
	}

	// Content with outRelations and have ItemSet as outRelation, but
	// getItemsetPreviewUrl method return null - return null
	@Test
	public void TestGetItemsetPreviewUrl3() throws Exception {
		String contentNodeString = "{\"identifier\":\"do_11292666508456755211\",\"objectType\":\"Content\",\"code\":\"Test_PDF\",\"channel\":\"channel-1\",\"mimeType\":\"application/pdf\",\"versionKey\":\"1578381263182\",\"name\":\"Test_PDF\",\"status\":\"Live\",\"itemSets\":[{\"identifier\":\"do_11292667205373132811\"}]}";
		Map<String, Object> contentNodeMap = mapper.readValue(contentNodeString, HashMap.class);
		DefinitionDTO contentDefinition = new ControllerUtil().getDefinition("domain", "Content");
		Node contentNode = ConvertToGraphNode.convertToGraphNode(contentNodeMap, contentDefinition, null);
		ItemsetPublishManager itemsetPublishManager = PowerMockito.mock(ItemsetPublishManager.class);
		PublishFinalizer publishFinalizer = new PublishFinalizer("/tmp", "do_11292666508456755211");
		PowerMockito.when(itemsetPublishManager.publish(Mockito.anyList())).thenReturn(null);
		String url = publishFinalizer.getItemsetPreviewUrl(contentNode);
		Assert.assertNull(url);
	}

	// Content with outRelations and have ItemSet as outRelation - print service
	// return no pdfUrl- return null
	@Rule
	@Test(expected = ServerException.class)
	public void TestGetItemsetPreviewUrl4() throws Exception {
		String contentNodeString = "{\"identifier\":\"do_11292666508456755211\",\"objectType\":\"Content\",\"code\":\"Test_PDF\",\"channel\":\"channel-1\",\"mimeType\":\"application/pdf\",\"versionKey\":\"1578381263182\",\"name\":\"Test_PDF\",\"status\":\"Live\",\"itemSets\":[{\"identifier\":\"do_11292667205373132811\"}]}";
		Map<String, Object> contentNodeMap = mapper.readValue(contentNodeString, HashMap.class);
		DefinitionDTO contentDefinition = new ControllerUtil().getDefinition("domain", "Content");
		Node contentNode = ConvertToGraphNode.convertToGraphNode(contentNodeMap, contentDefinition, null);
		contentNode.getOutRelations().forEach(x -> x.setEndNodeObjectType("ItemSet"));
		ItemsetPublishManager itemsetPublishManager = PowerMockito.spy(new ItemsetPublishManager(null));//PowerMockito.mock(ItemsetPublishManager.class);
		PowerMockito.when(itemsetPublishManager.publish(Mockito.anyList())).thenReturn("previewUrl.html");
		PublishFinalizer publishFinalizer = new PublishFinalizer("/tmp", "do_11292666508456755211");
		publishFinalizer.setItemsetPublishManager(itemsetPublishManager);
		
		Response response = new Response();
		PowerMockito.mockStatic(HttpRestUtil.class);
		PowerMockito.when(HttpRestUtil.makePostRequest(Mockito.anyString(), Mockito.anyMap(), Mockito.anyMap()))
				.thenReturn(response);
		String url = publishFinalizer.getItemsetPreviewUrl(contentNode);
	}
	
	// Content with outRelations and have ItemSet as outRelation - print service
		// return pdfUrl- return null
	@Ignore
	@Test
	public void TestGetItemsetPreviewUrl5() throws Exception {
		String contentNodeString = "{\"identifier\":\"do_11292666508456755211\",\"objectType\":\"Content\",\"code\":\"Test_PDF\",\"channel\":\"channel-1\",\"mimeType\":\"application/pdf\",\"versionKey\":\"1578381263182\",\"name\":\"Test_PDF\",\"status\":\"Live\",\"itemSets\":[{\"identifier\":\"do_11292667205373132811\"}]}";
		Map<String, Object> contentNodeMap = mapper.readValue(contentNodeString, HashMap.class);
		DefinitionDTO contentDefinition = new ControllerUtil().getDefinition("domain", "Content");
		Node contentNode = ConvertToGraphNode.convertToGraphNode(contentNodeMap, contentDefinition, null);
		contentNode.getOutRelations().forEach(x -> x.setEndNodeObjectType("ItemSet"));
		ItemsetPublishManager itemsetPublishManager = PowerMockito.spy(new ItemsetPublishManager(null));
		PowerMockito.when(itemsetPublishManager.publish(Mockito.anyList())).thenReturn("previewUrl.html");
		PublishFinalizer publishFinalizer = new PublishFinalizer("/tmp", "do_11292666508456755211");
		publishFinalizer.setItemsetPublishManager(itemsetPublishManager);
		
		Response response = new Response();
		response.getResult().put("pdfUrl", "https://sunbirddev.blob.core.windows.net/sunbird-content-dev/content/do_11292666508456755211/artifact/do_11292666508456755211_1578420745714.html");
		PowerMockito.mockStatic(HttpRestUtil.class);
		PowerMockito.when(HttpRestUtil.makePostRequest(Mockito.anyString(), Mockito.anyMap(), Mockito.anyMap()))
				.thenReturn(response);
		/*PowerMockito.mockStatic(HttpDownloadUtility.class);
		File file = new File("/tmp/previewUrl.pdf");
		String previewurl = "https://sunbirddev.blob.core.windows.net/sunbird-content-dev/content/do_11292666508456755211/artifact/do_11292666508456755211_1578420745714.html";
		PowerMockito.when(HttpDownloadUtility.downloadFile(Mockito.contains("https://"), Mockito.anyString())).thenReturn(file);
		
		PowerMockito.mockStatic(CloudStore.class);
		PowerMockito.when(CloudStore.uploadFile(Mockito.anyString(), Mockito.any(), Mockito.anyBoolean())).thenReturn(new String[] {"s3Key", "previewUrl.pdf"});*/
		
		PublishFinalizeUtil publishFinalizeUtil = PowerMockito.spy(new PublishFinalizeUtil());
		PowerMockito.when(publishFinalizeUtil.uploadFile(Mockito.anyString(), Mockito.any(), Mockito.anyString())).thenReturn("previewUrl.pdf");
		
		
		String url = publishFinalizer.getItemsetPreviewUrl(contentNode);
		Assert.assertSame("previewUrl.pdf", url);
	}

	@Test
	public void testGetHierarchy() throws Exception {
		PublishFinalizer publishFinalizer = new PublishFinalizer("/tmp", "do_11292666508456755211");
		HierarchyStore hierarchyStore = PowerMockito.spy(new HierarchyStore());
		PowerMockito.doReturn(new HashMap<String, Object>(){{
			put("identifier", "do_11292666508456755211");
			put("children", new ArrayList<Map<String, Object>>());
		}}).when(hierarchyStore).getHierarchy(Mockito.anyString());
		publishFinalizer.setHierarchyStore(hierarchyStore);


		Method method = PublishFinalizer.class.getDeclaredMethod("getHierarchy", String.class, Boolean.TYPE);
		method.setAccessible(true);
		Map<String, Object> response = (Map<String, Object>)method.invoke(publishFinalizer, "do_11292666508456755211", true);
		Assert.assertNotNull(response);
	}
	
	@Test
	public void testIsContentShallowCopyShallow() throws Exception{
		PublishFinalizer publishFinalizer = new PublishFinalizer("/tmp", "do_11292666508456755211");
		
		String contentNodeString = "{\"identifier\":\"do_11292666508456755211\",\"origin\":\"abc\",\"originData\":{\"copyType\":\"shallow\"}, \"objectType\":\"Content\",\"code\":\"Test_PDF\",\"channel\":\"channel-1\",\"mimeType\":\"application/pdf\",\"versionKey\":\"1578381263182\",\"name\":\"Test_PDF\",\"status\":\"Live\"}";
		Map<String, Object> contentNodeMap = mapper.readValue(contentNodeString, HashMap.class);
		DefinitionDTO contentDefinition = new ControllerUtil().getDefinition("domain", "Content");
		Node contentNode = ConvertToGraphNode.convertToGraphNode(contentNodeMap, contentDefinition, null);
		Assert.assertTrue(publishFinalizer.isContentShallowCopy(contentNode));
	}
	
	@Test
	public void testIsContentShallowCopyDeep() throws Exception{
		PublishFinalizer publishFinalizer = new PublishFinalizer("/tmp", "do_11292666508456755211");
		
		String contentNodeString = "{\"identifier\":\"do_11292666508456755211\",\"origin\":\"abc\",\"originData\":{\"copyType\":\"deep\"}, \"objectType\":\"Content\",\"code\":\"Test_PDF\",\"channel\":\"channel-1\",\"mimeType\":\"application/pdf\",\"versionKey\":\"1578381263182\",\"name\":\"Test_PDF\",\"status\":\"Live\"}";
		Map<String, Object> contentNodeMap = mapper.readValue(contentNodeString, HashMap.class);
		DefinitionDTO contentDefinition = new ControllerUtil().getDefinition("domain", "Content");
		Node contentNode = ConvertToGraphNode.convertToGraphNode(contentNodeMap, contentDefinition, null);
		Assert.assertFalse(publishFinalizer.isContentShallowCopy(contentNode));
	}
	@Test
	public void testIsContentShallowCopyNoCopyType() throws Exception{
		PublishFinalizer publishFinalizer = new PublishFinalizer("/tmp", "do_11292666508456755211");
		
		String contentNodeString = "{\"identifier\":\"do_11292666508456755211\",\"origin\":\"abc\",\"originData\":{}, \"objectType\":\"Content\",\"code\":\"Test_PDF\",\"channel\":\"channel-1\",\"mimeType\":\"application/pdf\",\"versionKey\":\"1578381263182\",\"name\":\"Test_PDF\",\"status\":\"Live\"}";
		Map<String, Object> contentNodeMap = mapper.readValue(contentNodeString, HashMap.class);
		DefinitionDTO contentDefinition = new ControllerUtil().getDefinition("domain", "Content");
		Node contentNode = ConvertToGraphNode.convertToGraphNode(contentNodeMap, contentDefinition, null);
		Assert.assertFalse(publishFinalizer.isContentShallowCopy(contentNode));
	}
	@Test
	public void testIsContentShallowCopyNoOriginData() throws Exception{
		PublishFinalizer publishFinalizer = new PublishFinalizer("/tmp", "do_11292666508456755211");
		
		String contentNodeString = "{\"identifier\":\"do_11292666508456755211\",\"origin\":\"abc\", \"objectType\":\"Content\",\"code\":\"Test_PDF\",\"channel\":\"channel-1\",\"mimeType\":\"application/pdf\",\"versionKey\":\"1578381263182\",\"name\":\"Test_PDF\",\"status\":\"Live\"}";
		Map<String, Object> contentNodeMap = mapper.readValue(contentNodeString, HashMap.class);
		DefinitionDTO contentDefinition = new ControllerUtil().getDefinition("domain", "Content");
		Node contentNode = ConvertToGraphNode.convertToGraphNode(contentNodeMap, contentDefinition, null);
		Assert.assertFalse(publishFinalizer.isContentShallowCopy(contentNode));
	}
	
	/*@Test
	public void testupdateParent() throws Exception{
		PublishFinalizer publishFinalizer = new PublishFinalizer("/tmp", "do_11292666508456755211");
		
		String contentNodeString = "{\"identifier\":\"do_11292666508456755211\",\"origin\":\"do_11298183063028531217\", \"objectType\":\"Content\",\"code\":\"Test_PDF\",\"channel\":\"channel-1\",\"mimeType\":\"application/pdf\",\"versionKey\":\"1578381263182\",\"name\":\"Test_PDF\",\"status\":\"Live\"}";
		Map<String, Object> contentNodeMap = mapper.readValue(contentNodeString, HashMap.class);
		DefinitionDTO contentDefinition = new ControllerUtil().getDefinition("domain", "Content");
		Node contentNode = ConvertToGraphNode.convertToGraphNode(contentNodeMap, contentDefinition, null);
		
		String contentHierarchyString = "{\"children\":[{\"parent\":\"do_11298183063028531217\",\"objectType\":\"Content\",\"children\":[{\"parent\":\"do_11298183102799052818\",\"objectType\":\"Content\",\"contentType\":\"TextBookUnit\",\"identifier\":\"do_112981831028031488110\",\"license\":\"CC BY 4.0\",\"name\":\"U1.1\",\"status\":\"Draft\"}],\"contentType\":\"TextBookUnit\",\"identifier\":\"do_11298183102799052818\",\"license\":\"CC BY 4.0\",\"name\":\"U1\",\"status\":\"Draft\"}],\"contentType\":\"TextBook\",\"identifier\":\"do_11298183063028531217\",\"license\":\"CC BY 4.0\",\"name\":\"Testcase\",\"status\":\"Draft\"}";
		Map<String, Object> contentHierarchyMap = mapper.readValue(contentHierarchyString, HashMap.class);
		HierarchyStore hierarchyStore = PowerMockito.spy(new HierarchyStore());
		PowerMockito.doReturn(contentHierarchyMap).when(hierarchyStore).getHierarchy(Mockito.anyString());
		
		List<Map<String, Object>>children = publishFinalizer.updateParent(contentNode, contentHierarchyMap);
		children.forEach(child -> Assert.assertTrue(StringUtils.equalsIgnoreCase((String)child.get("parent"), "do_11292666508456755211")));
		
		
	}*/
	@Test
	public void testGetOriginData() throws Exception {
		String contentNodeString = "{\"ownershipType\":[\"createdBy\"],\"code\":\"org.ekstep.pt.text.book.1\",\"origin\":\"do_11297962047265996813\",\"channel\":\"in.ekstep\",\"organisation\":[\"test\"],\"description\":\"Copy textbook Testing For shallow Copy\",\"language\":[\"English\"],\"mimeType\":\"application/vnd.ekstep.content-collection\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2020-03-17T12:18:57.811+0530\",\"objectType\":\"Content\",\"contentDisposition\":\"inline\",\"lastUpdatedOn\":\"2020-03-17T12:18:57.811+0530\",\"contentEncoding\":\"gzip\",\"originData\":\"{\\\"name\\\":\\\"Copy Collecction Testing For shallow Copy\\\",\\\"license\\\":\\\"CC BY 4.0\\\",\\\"copyType\\\":\\\"shallow\\\",\\\"organisation\\\":[\\\"test\\\"]}\",\"contentType\":\"TextBook\",\"dialcodeRequired\":\"No\",\"audience\":[\"Learner\"],\"createdFor\":[\"gauraw\"],\"lastStatusChangedOn\":\"2020-03-17T12:18:57.811+0530\",\"os\":[\"All\"],\"visibility\":\"Default\",\"IL_SYS_NODE_TYPE\":\"DATA_NODE\",\"mediaType\":\"content\",\"osId\":\"org.ekstep.quiz.app\",\"languageCode\":[\"en\"],\"version\":2,\"versionKey\":\"1584427737811\",\"license\":\"CC BY 4.0\",\"idealScreenDensity\":\"hdpi\",\"framework\":\"NCF\",\"createdBy\":\"gauraw\",\"compatibilityLevel\":1,\"IL_FUNC_OBJECT_TYPE\":\"Content\",\"name\":\"Copy Collecction Testing For shallow Copy\",\"IL_UNIQUE_ID\":\"do_11297963202136473611\",\"status\":\"Draft\"}";
		Map<String, Object> contentNodeMap = mapper.readValue(contentNodeString, HashMap.class);
		DefinitionDTO contentDefinition = new ControllerUtil().getDefinition("domain", "Content");
		Node contentNode = ConvertToGraphNode.convertToGraphNode(contentNodeMap, contentDefinition, null);
		Method method = PublishFinalizer.class.getDeclaredMethod("getOriginData", Node.class);
		method.setAccessible(true);
		PublishFinalizer finalizer = new PublishFinalizer("/tmp", "do_11297963202136473611");
		Map<String, Object> result = (Map<String, Object>) method.invoke(finalizer, contentNode);
		Assert.assertTrue(MapUtils.isNotEmpty(result));
		Assert.assertEquals("shallow", result.get("copyType"));
	}

	@Test
	public void testUpdateOriginPkgVersion() throws Exception {
		String contentNodeString = "{\"ownershipType\":[\"createdBy\"],\"code\":\"org.ekstep.pt.text.book.1\",\"origin\":\"do_11297962047265996813\",\"channel\":\"in.ekstep\",\"organisation\":[\"test\"],\"description\":\"Copy textbook Testing For shallow Copy\",\"language\":[\"English\"],\"mimeType\":\"application/vnd.ekstep.content-collection\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2020-03-17T12:18:57.811+0530\",\"objectType\":\"Content\",\"contentDisposition\":\"inline\",\"lastUpdatedOn\":\"2020-03-17T12:18:57.811+0530\",\"contentEncoding\":\"gzip\",\"originData\":\"{\\\"name\\\":\\\"Copy Collecction Testing For shallow Copy\\\",\\\"license\\\":\\\"CC BY 4.0\\\",\\\"copyType\\\":\\\"shallow\\\",\\\"pkgVersion\\\":2.0,\\\"organisation\\\":[\\\"test\\\"]}\",\"contentType\":\"TextBook\",\"dialcodeRequired\":\"No\",\"audience\":[\"Learner\"],\"createdFor\":[\"gauraw\"],\"lastStatusChangedOn\":\"2020-03-17T12:18:57.811+0530\",\"os\":[\"All\"],\"visibility\":\"Default\",\"IL_SYS_NODE_TYPE\":\"DATA_NODE\",\"mediaType\":\"content\",\"osId\":\"org.ekstep.quiz.app\",\"languageCode\":[\"en\"],\"version\":2,\"versionKey\":\"1584427737811\",\"license\":\"CC BY 4.0\",\"idealScreenDensity\":\"hdpi\",\"framework\":\"NCF\",\"createdBy\":\"gauraw\",\"compatibilityLevel\":1,\"IL_FUNC_OBJECT_TYPE\":\"Content\",\"name\":\"Copy Collecction Testing For shallow Copy\",\"IL_UNIQUE_ID\":\"do_11297963202136473611\",\"status\":\"Draft\"}";
		Map<String, Object> contentNodeMap = mapper.readValue(contentNodeString, HashMap.class);
		DefinitionDTO contentDefinition = new ControllerUtil().getDefinition("domain", "Content");
		Node contentNode = ConvertToGraphNode.convertToGraphNode(contentNodeMap, contentDefinition, null);
		String originNodeStr = "{\"identifier\":\"do_11297962047265996813\",\"objectType\":\"Content\",\"code\":\"Test_PDF\",\"channel\":\"channel-1\",\"mimeType\":\"application/pdf\",\"versionKey\":\"1578381263182\",\"name\":\"Test_PDF\",\"status\":\"Live\",\"pkgVersion\":4.0}";
		Map<String, Object> originNodeMap = mapper.readValue(originNodeStr, HashMap.class);
		Node originNode = ConvertToGraphNode.convertToGraphNode(originNodeMap, contentDefinition, null);
		ControllerUtil util = PowerMockito.spy(new ControllerUtil());
		PowerMockito.doReturn(originNode).when(util).getNode(Mockito.anyString(), Mockito.anyString());
		Method method = PublishFinalizer.class.getDeclaredMethod("updateOriginPkgVersion", Node.class);
		method.setAccessible(true);
		PublishFinalizer finalizer = new PublishFinalizer("/tmp", "do_11297963202136473611");
		finalizer.setControllerUtil(util);
		method.invoke(finalizer, contentNode);
		Assert.assertEquals(4.0, ((Map<String, Object>)contentNode.getMetadata().get("originData")).get("pkgVersion"));
	}
	
	@Test
	public void testContextDrivenContentUpload() throws Exception {
		
		PublishFinalizeUtil publishFinalizeUtil = PowerMockito.mock(PublishFinalizeUtil.class);//PowerMockito.spy(new PublishFinalizeUtil());
		PowerMockito.doNothing().when(publishFinalizeUtil).replaceArtifactUrl(Mockito.anyObject());
		
		
		String contentNodeString = "{\"identifier\":\"do_11292666508456755211\",\"objectType\":\"Content\",\"artifactBasePath\":\"program/app\",\"artifactUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/program/app/content/do_112999482416209920112/artifact/1.pdf\",\"cloudStorageKey\":\"program/app/content/do_112999482416209920112/artifact/1.pdf\",\"s3Key\":\"program/app/content/do_112999482416209920112/artifact/1.pdf\"}";
		Map<String, Object> contentNodeMap = mapper.readValue(contentNodeString, HashMap.class);
		DefinitionDTO contentDefinition = new ControllerUtil().getDefinition("domain", "Content");
		Node contentNode = ConvertToGraphNode.convertToGraphNode(contentNodeMap, contentDefinition, null);
		
		PublishFinalizer publishFinalizer = new PublishFinalizer("/tmp", "do_11292666508456755211");
		
		publishFinalizer.contextDrivenContentUpload(contentNode);
		
	}

	@Test
	public void testCollectionUnitsSyncIntoESAndKafkaTopic() throws Exception {
		PublishFinalizer publishFinalizer = new PublishFinalizer("/tmp", "do_11292666508456755211");
		Method syncNodesMethod = PublishFinalizer.class.getDeclaredMethod("syncNodes", List.class, List.class);
		syncNodesMethod.setAccessible(true);
		mockDependencies();
		syncNodesMethod.invoke(publishFinalizer, getChildNode(), new ArrayList<String>() {{ add("1test1"); }});
	}

	@Test
	public void testCollectionUnitsSyncIntoESAndKafkaTopicFailed() throws Exception {
		PublishFinalizer publishFinalizer = new PublishFinalizer("/tmp", "do_11292666508456755211");
		Method syncNodesMethod = PublishFinalizer.class.getDeclaredMethod("syncNodes", List.class, List.class);
		syncNodesMethod.setAccessible(true);
		mockDependenciesThrowError();
		syncNodesMethod.invoke(publishFinalizer, getChildNode(), new ArrayList<String>() {{ add("1test1"); }});
	}

	private void mockDependencies() throws Exception {
		PowerMockito.mockStatic(ElasticSearchUtil.class);
		PowerMockito.doNothing().when(ElasticSearchUtil.class);
		ElasticSearchUtil.bulkIndexWithIndexId(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());
		ElasticSearchUtil.bulkDeleteDocumentById(Mockito.anyString(), Mockito.anyString(), Mockito.anyList());

		PowerMockito.mockStatic(KafkaClient.class);
		PowerMockito.doNothing().when(KafkaClient.class);
		KafkaClient.send(Mockito.anyString(), Mockito.anyString());

		PowerMockito.mockStatic(GraphUtil.class);
		PowerMockito.when(GraphUtil.getRelationMap("", new HashMap<>())).thenReturn(new HashMap<>());
	}

	private void mockDependenciesThrowError() throws Exception {
		PowerMockito.mockStatic(ElasticSearchUtil.class);
		PowerMockito.doThrow(new Exception()).when(ElasticSearchUtil.class);
		ElasticSearchUtil.bulkIndexWithIndexId(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());
		ElasticSearchUtil.bulkDeleteDocumentById(Mockito.anyString(), Mockito.anyString(), Mockito.anyList());

		PowerMockito.mockStatic(KafkaClient.class);
		PowerMockito.doThrow(new Exception()).when(KafkaClient.class);
		KafkaClient.send(Mockito.anyString(), Mockito.anyString());

		PowerMockito.mockStatic(GraphUtil.class);
		PowerMockito.when(GraphUtil.getRelationMap("", new HashMap<>())).thenReturn(new HashMap<>());
	}

	private List<Map<String, Object>> getChildNode() throws Exception {
		String childMap = "{\"ownershipType\":[\"createdBy\"],\"parent\":\"do_113053289443917824115\",\"code\":\"b9a50833-eff6-4ef5-a2a4-2413f2d51f6c\",\"channel\":\"in.ekstep\",\"description\":\"Test_CourseUnit_desc_1\",\"language\":[\"English\"],\"mimeType\":\"application/vnd.ekstep.content-collection\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2020-06-29T13:55:29.587+0530\",\"objectType\":\"Content\",\"children\":[{\"ownershipType\":[\"createdBy\"],\"parent\":\"do_113053289509576704120\",\"code\":\"b9a50833-eff6-4ef5-a2a4-2413f2d51f6d\",\"channel\":\"in.ekstep\",\"description\":\"Test_CourseSubUnit_desc_1\",\"language\":[\"English\"],\"mimeType\":\"application/vnd.ekstep.content-collection\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2020-06-29T13:55:29.583+0530\",\"objectType\":\"Content\",\"contentDisposition\":\"inline\",\"lastUpdatedOn\":\"2020-06-29T13:55:29.796+0530\",\"contentEncoding\":\"gzip\",\"contentType\":\"TextBookUnit\",\"dialcodeRequired\":\"No\",\"identifier\":\"do_113053289509543936118\",\"lastStatusChangedOn\":\"2020-06-29T13:55:29.583+0530\",\"audience\":[\"Learner\"],\"os\":[\"All\"],\"visibility\":\"Parent\",\"index\":1,\"mediaType\":\"content\",\"osId\":\"org.ekstep.launcher\",\"languageCode\":[\"en\"],\"versionKey\":\"1593419129583\",\"license\":\"CC BY 4.0\",\"idealScreenDensity\":\"hdpi\",\"depth\":2,\"compatibilityLevel\":1,\"name\":\"Test_Course_SubUnit_name_1\",\"status\":\"Live\",\"lastPublishedOn\":\"2020-06-29T18:55:00.479+0530\",\"pkgVersion\":6.0,\"leafNodesCount\":0,\"leafNodes\":[],\"downloadUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_113053289443917824115/marigold_1593437125852_do_113053289443917824115_6.0_spine.ecar\",\"variants\":\"{\\\"online\\\":{\\\"ecarUrl\\\":\\\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_113053289443917824115/marigold_1593437129524_do_113053289443917824115_6.0_online.ecar\\\",\\\"size\\\":2788.0},\\\"spine\\\":{\\\"ecarUrl\\\":\\\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_113053289443917824115/marigold_1593437125852_do_113053289443917824115_6.0_spine.ecar\\\",\\\"size\\\":2764.0}}\"},{\"ownershipType\":[\"createdBy\"],\"parent\":\"do_113053289509576704120\",\"code\":\"b9a50833-eff6-4ef5-a2a4-2413f2d51f6e\",\"channel\":\"in.ekstep\",\"description\":\"Test_CourseSubUnit_desc_2\",\"language\":[\"English\"],\"mimeType\":\"application/vnd.ekstep.content-collection\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2020-06-29T13:55:29.579+0530\",\"objectType\":\"Content\",\"contentDisposition\":\"inline\",\"lastUpdatedOn\":\"2020-06-29T13:55:29.796+0530\",\"contentEncoding\":\"gzip\",\"contentType\":\"TextBookUnit\",\"dialcodeRequired\":\"No\",\"identifier\":\"do_113053289509511168116\",\"lastStatusChangedOn\":\"2020-06-29T13:55:29.579+0530\",\"audience\":[\"Learner\"],\"os\":[\"All\"],\"visibility\":\"Parent\",\"index\":2,\"mediaType\":\"content\",\"osId\":\"org.ekstep.launcher\",\"languageCode\":[\"en\"],\"versionKey\":\"1593419129579\",\"license\":\"CC BY 4.0\",\"idealScreenDensity\":\"hdpi\",\"depth\":2,\"compatibilityLevel\":1,\"name\":\"Test_Course_SubUnit_name_2\",\"status\":\"Live\",\"lastPublishedOn\":\"2020-06-29T18:55:00.479+0530\",\"pkgVersion\":6.0,\"leafNodesCount\":0,\"leafNodes\":[],\"downloadUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_113053289443917824115/marigold_1593437125852_do_113053289443917824115_6.0_spine.ecar\",\"variants\":\"{\\\"online\\\":{\\\"ecarUrl\\\":\\\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_113053289443917824115/marigold_1593437129524_do_113053289443917824115_6.0_online.ecar\\\",\\\"size\\\":2788.0},\\\"spine\\\":{\\\"ecarUrl\\\":\\\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_113053289443917824115/marigold_1593437125852_do_113053289443917824115_6.0_spine.ecar\\\",\\\"size\\\":2764.0}}\"}],\"contentDisposition\":\"inline\",\"lastUpdatedOn\":\"2020-06-29T13:55:29.796+0530\",\"contentEncoding\":\"gzip\",\"contentType\":\"TextBookUnit\",\"dialcodeRequired\":\"No\",\"identifier\":\"do_113053289509576704120\",\"lastStatusChangedOn\":\"2020-06-29T13:55:29.587+0530\",\"audience\":[\"Learner\"],\"os\":[\"All\"],\"visibility\":\"Parent\",\"index\":1,\"mediaType\":\"content\",\"osId\":\"org.ekstep.launcher\",\"languageCode\":[\"en\"],\"versionKey\":\"1593419129587\",\"license\":\"CC BY 4.0\",\"idealScreenDensity\":\"hdpi\",\"depth\":1,\"compatibilityLevel\":1,\"name\":\"Test_CourseUnit_1\",\"status\":\"Live\",\"lastPublishedOn\":\"2020-06-29T18:55:00.479+0530\",\"pkgVersion\":6.0,\"leafNodesCount\":0,\"leafNodes\":[],\"downloadUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_113053289443917824115/marigold_1593437125852_do_113053289443917824115_6.0_spine.ecar\",\"variants\":\"{\\\"online\\\":{\\\"ecarUrl\\\":\\\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_113053289443917824115/marigold_1593437129524_do_113053289443917824115_6.0_online.ecar\\\",\\\"size\\\":2788.0},\\\"spine\\\":{\\\"ecarUrl\\\":\\\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_113053289443917824115/marigold_1593437125852_do_113053289443917824115_6.0_spine.ecar\\\",\\\"size\\\":2764.0}}\"}";
		Map<String, Object> children = mapper.readValue(childMap, Map.class);
		return new ArrayList<Map<String, Object>>() {{
			add(children);
		}};
	}

}
