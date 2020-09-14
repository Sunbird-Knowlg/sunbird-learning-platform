package org.ekstep.content.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import org.ekstep.common.dto.Response;
import org.ekstep.common.mgr.ConvertToGraphNode;
import org.ekstep.content.entity.Manifest;
import org.ekstep.content.entity.Media;
import org.ekstep.content.entity.Plugin;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.engine.common.GraphEngineTestSetup;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.contentstore.ContentStore;
import org.ekstep.learning.util.CloudStore;
import org.ekstep.learning.util.ControllerUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.util.Assert;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CloudStore.class, ContentStore.class})
@PowerMockIgnore({ "javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*", "javax.crypto.*" })
public class PublishFinalizeUtilTest extends GraphEngineTestSetup{

	public static ObjectMapper mapper = new ObjectMapper();

	@BeforeClass
	public static void create() throws Exception {
		loadDefinition("definitions/content_definition.json", "definitions/objectcategorydefinition_definition.json");
	}

	@AfterClass
	public static void destroy() {}
//
//	@Test
//	public void TestReplaceArtifactUrl() throws Exception {
//		PowerMockito.mockStatic(CloudStore.class);
//		PowerMockito.doNothing().when(CloudStore.class);
//
//		String contentNodeString = "{\"identifier\":\"do_11292666508456755211\",\"objectType\":\"Content\",\"artifactBasePath\":\"program/app\",\"artifactUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/program/app/content/do_112999482416209920112/artifact/1.pdf\",\"cloudStorageKey\":\"program/app/content/do_112999482416209920112/artifact/1.pdf\",\"s3Key\":\"program/app/content/do_112999482416209920112/artifact/1.pdf\"}";
//		Map<String, Object> contentNodeMap = mapper.readValue(contentNodeString, HashMap.class);
//		DefinitionDTO contentDefinition = new ControllerUtil().getDefinition("domain", "Content");
//		Node contentNode = ConvertToGraphNode.convertToGraphNode(contentNodeMap, contentDefinition, null);
//		PublishFinalizeUtil publishFinalizeUtil = new PublishFinalizeUtil();
//		publishFinalizeUtil.replaceArtifactUrl(contentNode);
//		String artifactUrl = (String)contentNode.getMetadata().get("artifactUrl");
//		String cloudStorageKey = (String)contentNode.getMetadata().get("cloudStorageKey");
//		String s3Key = (String)contentNode.getMetadata().get("s3Key");
//		Assert.equals("https://sunbirddev.blob.core.windows.net/sunbird-content-dev/content/do_112999482416209920112/artifact/1.pdf", artifactUrl);
//		Assert.equals("content/do_112999482416209920112/artifact/1.pdf", cloudStorageKey);
//		Assert.equals("content/do_112999482416209920112/artifact/1.pdf", s3Key);
//	}
//
//	@Rule
//	@Test(expected = Exception.class)
//	public void TestReplaceArtifactUrlThrowException() throws Exception {
//		PowerMockito.mockStatic(CloudStore.class);
//		PowerMockito.doThrow(new Exception()).when(CloudStore.class);
//
//		String contentNodeString = "{\"identifier\":\"do_11292666508456755211\",\"objectType\":\"Content\",\"artifactBasePath\":\"program/app\",\"artifactUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/program/app/content/do_112999482416209920112/artifact/1.pdf\",\"cloudStorageKey\":\"program/app/content/do_112999482416209920112/artifact/1.pdf\",\"s3Key\":\"program/app/content/do_112999482416209920112/artifact/1.pdf\"}";
//		Map<String, Object> contentNodeMap = mapper.readValue(contentNodeString, HashMap.class);
//		DefinitionDTO contentDefinition = new ControllerUtil().getDefinition("domain", "Content");
//		Node contentNode = ConvertToGraphNode.convertToGraphNode(contentNodeMap, contentDefinition, null);
//		PublishFinalizeUtil publishFinalizeUtil = new PublishFinalizeUtil();
//		publishFinalizeUtil.replaceArtifactUrl(contentNode);
//		String artifactUrl = (String)contentNode.getMetadata().get("artifactUrl");
//		String cloudStorageKey = (String)contentNode.getMetadata().get("cloudStorageKey");
//		String s3Key = (String)contentNode.getMetadata().get("s3Key");
//		Assert.equals("https://sunbirddev.blob.core.windows.net/sunbird-content-dev/content/do_112999482416209920112/artifact/1.pdf", artifactUrl);
//		Assert.equals("content/do_112999482416209920112/artifact/1.pdf", cloudStorageKey);
//		Assert.equals("content/do_112999482416209920112/artifact/1.pdf", s3Key);
//	}
//
//	@Test
//	public void testValidateAssetMediaForExternalLinkPositiveScenario() {
//		Media media = new Media();
//		media.setId("testid");
//		media.setType("youtube");
//		media.setSrc("https://www.youtube.com/watch?v=Gi2nuTLse7M");
//		PublishFinalizeUtil publishFinalizeUtil = new PublishFinalizeUtil();
//		Assert.isTrue(publishFinalizeUtil.validateAssetMediaForExternalLink(media), "External Link");
//	}
//
//	@Test
//	public void testValidateAssetMediaForExternalLinkNegativeScenario() {
//		Media media = new Media();
//		media.setId("testid");
//		media.setType("plugin");
//		media.setSrc("https://sunbirddev.blob.core.windows.net/sunbird-content-dev/content/org.ekstep.simpletimer/artifact/org.ekstep.simpletimer-1.0.zip");
//		PublishFinalizeUtil publishFinalizeUtil = new PublishFinalizeUtil();
//		Assert.isTrue(!publishFinalizeUtil.validateAssetMediaForExternalLink(media), "External Link");
//	}
//
//	@Test
//	public void testHandleAssetWithExternalLink() {
//
//		ContentStore contentStore = PowerMockito.spy(new ContentStore());
//		PowerMockito.doNothing().when(contentStore).updateExternalLink(Mockito.anyString(), Mockito.anyList());
//
//		Media media = new Media();
//		media.setId("testid");
//		media.setType("youtube");
//		media.setSrc("https://www.youtube.com/watch?v=Gi2nuTLse7M");
//		Manifest manifest = new Manifest();
//		manifest.setMedias(Arrays.asList(media));
//		Plugin plugin = new Plugin();
//		plugin.setManifest(manifest);
//
//		PublishFinalizeUtil publishFinalizeUtil = new PublishFinalizeUtil(contentStore);
//		publishFinalizeUtil.handleAssetWithExternalLink(plugin, "ECML_CONTENT_ID");
//	}

	@Test
	public void testHandleAutoBatchAndTrackability() throws Exception {
		categoryDefinition_all_1();
		ContentStore contentStore = PowerMockito.spy(new ContentStore());
		PowerMockito.doNothing().when(contentStore).updateExternalLink(Mockito.anyString(), Mockito.anyList());
		PublishFinalizeUtil publishFinalizeUtil = new PublishFinalizeUtil(contentStore);
		Node node =getNode_1();
		publishFinalizeUtil.handleAutoBatchAndTrackability(node);
		Assert.equals("Yes",  ((Map<String, Object>)node.getMetadata().get("trackable")).get("enabled"));
		Assert.equals("Yes",  ((Map<String, Object>)node.getMetadata().get("trackable")).get("autoBatch"));
	}

	@Test
	public void testHandleAutoBatchAndTrackability_2() throws Exception {
		categoryDefinition_all_2();
		ContentStore contentStore = PowerMockito.spy(new ContentStore());
		PowerMockito.doNothing().when(contentStore).updateExternalLink(Mockito.anyString(), Mockito.anyList());
		PublishFinalizeUtil publishFinalizeUtil = new PublishFinalizeUtil(contentStore);
		Node node =getNode_1();
		publishFinalizeUtil.handleAutoBatchAndTrackability(node);
		Assert.equals("Yes",  ((Map<String, Object>)node.getMetadata().get("trackable")).get("enabled"));
		Assert.equals("Yes",  ((Map<String, Object>)node.getMetadata().get("trackable")).get("autoBatch"));
		Assert.equals(Arrays.asList("progress-report"),  (List<String>)node.getMetadata().get("monitorable"));
	}

	@Test
	public void testHandleAutoBatchAndTrackability_3() throws Exception {
		ContentStore contentStore = PowerMockito.spy(new ContentStore());
		PowerMockito.doNothing().when(contentStore).updateExternalLink(Mockito.anyString(), Mockito.anyList());
		PublishFinalizeUtil publishFinalizeUtil = new PublishFinalizeUtil(contentStore);
		Node node =getNode_1();
		publishFinalizeUtil.handleAutoBatchAndTrackability(node);
		Assert.isTrue( node.getMetadata().get("trackable") == null);
		Assert.isTrue(  node.getMetadata().get("monitorable") == null);
	}

	private static void categoryDefinition_all_1() throws Exception {
		ControllerUtil util = new ControllerUtil();
		String categoryDefinition = "{\"objectType\":\"ObjectCategoryDefinition\",\"channel\": \"in.ekstep\",\"name\":\"Content Playlist\",\"description\":\"Content Playlist\",\"categoryId\":\"obj-cat:digital-textbook\",\"targetObjectType\":\"Collection\",\"identifier\":\"obj-cat:digital-textbook_content_in.ekstep\",\"objectMetadata\":{\"config\":{},\"schema\":{\"required\":[\"author\",\"copyright\",\"license\",\"audience\"],\"properties\":{\"audience\":{\"type\":\"array\",\"items\":{\"type\":\"string\",\"enum\":[\"Student\",\"Teacher\"]},\"default\":[\"Student\"]},\"mimeType\":{\"type\":\"string\",\"enum\":[\"application/vnd.ekstep.content-collection\"]},\"monitorable\":{\"type\":\"array\",\"items\":{\"type\":\"string\",\"enum\":[\"progress-report\",\"score-report\"]}},\"userConsent\":{\"type\":\"string\",\"enum\":[\"Yes\",\"No\"]},\"trackable\":{\"type\":\"object\",\"properties\":{\"enabled\":{\"type\":\"string\",\"enum\":[\"Yes\",\"No\"],\"default\":\"Yes\"},\"autoBatch\":{\"type\":\"string\",\"enum\":[\"Yes\",\"No\"],\"default\":\"Yes\"}},\"additionalProperties\":false},\"credentials\":{\"type\":\"object\",\"properties\":{\"enabled\":{\"type\":\"string\",\"enum\":[\"Yes\",\"No\"],\"default\":\"No\"}},\"additionalProperties\":false}}}}}";
		Node node = new Node();
		node.setIdentifier("obj-cat:digital-textbook_content_in.ekstep");
		node.setGraphId("domain");
		node.setMetadata(mapper.readValue(categoryDefinition, new TypeReference<Map<String, Object>>() {
		}));
		node.setObjectType("ObjectCategoryDefinition");
		util.createDataNode(node);
	}

	private static void categoryDefinition_all_2() throws Exception {
		ControllerUtil util = new ControllerUtil();
		String categoryDefinition ="{\"objectType\":\"ObjectCategoryDefinition\",\"channel\":\"in.ekstep\",\"name\":\"Content Playlist\",\"description\":\"Content Playlist\",\"categoryId\":\"obj-cat:digital-textbook\",\"targetObjectType\":\"Collection\",\"identifier\":\"obj-cat:digital-textbook_content_all\",\"objectMetadata\":{\"config\":{},\"schema\":{\"required\":[\"author\",\"copyright\",\"license\",\"audience\"],\"properties\":{\"audience\":{\"type\":\"array\",\"items\":{\"type\":\"string\",\"enum\":[\"Student\",\"Teacher\"]},\"default\":[\"Student\"]},\"mimeType\":{\"type\":\"string\",\"enum\":[\"application/vnd.ekstep.content-collection\"]},\"monitorable\":{\"type\":\"array\",\"items\":{\"type\":\"string\",\"enum\":[\"progress-report\",\"score-report\"]},\"default\":[\"progress-report\"]},\"userConsent\":{\"type\":\"string\",\"enum\":[\"Yes\",\"No\"],\"default\":\"Yes\"},\"trackable\":{\"type\":\"object\",\"properties\":{\"enabled\":{\"type\":\"string\",\"enum\":[\"Yes\",\"No\"],\"default\":\"Yes\"},\"autoBatch\":{\"type\":\"string\",\"enum\":[\"Yes\",\"No\"],\"default\":\"Yes\"}},\"additionalProperties\":false},\"credentials\":{\"type\":\"object\",\"properties\":{\"enabled\":{\"type\":\"string\",\"enum\":[\"Yes\",\"No\"],\"default\":\"No\"}},\"additionalProperties\":false}}}}}";
		Node node = new Node();
		node.setIdentifier("obj-cat:digital-textbook_content_all");
		node.setGraphId("domain");
		node.setMetadata(mapper.readValue(categoryDefinition, new TypeReference<Map<String, Object>>() {
		}));
		node.setObjectType("ObjectCategoryDefinition");
		util.createDataNode(node);
	}


	private static Node getNode_1() throws Exception {
		String nodeString = "{\"primaryCategory\": \"Digital Textbook\",\"ownershipType\":[\"createdBy\"],\"code\":\"Test_Textbook\",\"prevStatus\":\"Processing\",\"channel\":\"in.ekstep\",\"downloadUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_11310636870976307215/asset-rhea_1599898759538_do_11310636870976307215_1.0_spine.ecar\",\"language\":[\"English\"],\"mimeType\":\"application/vnd.ekstep.content-collection\",\"variants\":{\"online\":{\"ecarUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_11310636870976307215/asset-rhea_1599898759694_do_11310636870976307215_1.0_online.ecar\",\"size\":2576},\"spine\":{\"ecarUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_11310636870976307215/asset-rhea_1599898759538_do_11310636870976307215_1.0_spine.ecar\",\"size\":2519}},\"idealScreenSize\":\"normal\",\"createdOn\":\"2020-09-12T08:15:24.148+0000\",\"objectType\":\"Content\",\"primaryCategory\":\"Digital Textbook\",\"contentDisposition\":\"inline\",\"lastUpdatedOn\":\"2020-09-12T08:19:19.162+0000\",\"contentEncoding\":\"gzip\",\"totalCompressedSize\":0,\"mimeTypesCount\":\"{\\\"application/vnd.ekstep.content-collection\\\":3}\",\"sYS_INTERNAL_LAST_UPDATED_ON\":\"2020-09-12T08:19:20.052+0000\",\"contentType\":\"TextBook\",\"dialcodeRequired\":\"No\",\"identifier\":\"do_11310636870976307215\",\"lastStatusChangedOn\":\"2020-09-12T08:19:20.045+0000\",\"audience\":[\"Student\"],\"toc_url\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/content/do_11310636870976307215/artifact/do_11310636870976307215_toc.json\",\"os\":[\"All\"],\"visibility\":\"Default\",\"contentTypesCount\":\"{\\\"TextBookUnit\\\":3}\",\"childNodes\":[\"do_11310637046191718416\",\"do_113106370462400512110\",\"do_11310637046235136018\"],\"mediaType\":\"content\",\"osId\":\"org.ekstep.quiz.app\",\"languageCode\":[\"en\"],\"lastPublishedBy\":\"KP_FT_PUBLISHER\",\"version\":2,\"pkgVersion\":1,\"versionKey\":\"1599898759162\",\"license\":\"CC BY 4.0\",\"prevState\":\"Draft\",\"idealScreenDensity\":\"hdpi\",\"framework\":\"NCF\",\"depth\":0,\"s3Key\":\"ecar_files/do_11310636870976307215/asset-rhea_1599898759538_do_11310636870976307215_1.0_spine.ecar\",\"size\":2519,\"lastPublishedOn\":\"2020-09-12T08:19:19.204+0000\",\"compatibilityLevel\":1,\"leafNodesCount\":0,\"name\":\"Asset RHEA\",\"resourceType\":\"Teach\",\"status\":\"Live\"}";
		Node node = new Node();
		node.setObjectType("Content");
		node.setMetadata(mapper.readValue(nodeString, new TypeReference<Map<String, Object>>() {
		}));
		return node;
	}
	
	
}
