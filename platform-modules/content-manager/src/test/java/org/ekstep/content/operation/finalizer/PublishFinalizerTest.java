package org.ekstep.content.operation.finalizer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.mgr.ConvertToGraphNode;
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.common.util.HttpRestUtil;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.engine.common.GraphEngineTestSetup;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.itemset.publish.ItemsetPublishManager;
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

}
