package org.ekstep.content.operation.finalizer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.mgr.ConvertToGraphNode;
import org.ekstep.common.util.HttpRestUtil;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.engine.common.GraphEngineTestSetup;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.itemset.publish.ItemsetPublishManager;
import org.ekstep.learning.hierarchy.store.HierarchyStore;
import org.ekstep.learning.util.CloudStore;
import org.ekstep.learning.util.ControllerUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.parboiled.common.StringUtils;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.ekstep.content.operation.finalizer.PublishFinalizer;
import org.ekstep.content.util.PublishFinalizeUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ItemsetPublishManager.class, HttpRestUtil.class, CloudStore.class})
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
	
	@Test
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
		
		
	}
}
