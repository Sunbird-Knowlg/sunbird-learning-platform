package org.ekstep.content.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

	ObjectMapper mapper = new ObjectMapper();

	@BeforeClass
	public static void create() throws Exception {
		loadDefinition("definitions/content_definition.json");
	}

	@AfterClass
	public static void destroy() {}
	
	@Test
	public void TestReplaceArtifactUrl() throws Exception {
		PowerMockito.mockStatic(CloudStore.class);
		PowerMockito.doNothing().when(CloudStore.class);
		
		String contentNodeString = "{\"identifier\":\"do_11292666508456755211\",\"objectType\":\"Content\",\"artifactBasePath\":\"program/app\",\"artifactUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/program/app/content/do_112999482416209920112/artifact/1.pdf\",\"cloudStorageKey\":\"program/app/content/do_112999482416209920112/artifact/1.pdf\",\"s3Key\":\"program/app/content/do_112999482416209920112/artifact/1.pdf\"}";
		Map<String, Object> contentNodeMap = mapper.readValue(contentNodeString, HashMap.class);
		DefinitionDTO contentDefinition = new ControllerUtil().getDefinition("domain", "Content");
		Node contentNode = ConvertToGraphNode.convertToGraphNode(contentNodeMap, contentDefinition, null);
		PublishFinalizeUtil publishFinalizeUtil = new PublishFinalizeUtil();
		publishFinalizeUtil.replaceArtifactUrl(contentNode);
		String artifactUrl = (String)contentNode.getMetadata().get("artifactUrl");
		String cloudStorageKey = (String)contentNode.getMetadata().get("cloudStorageKey");
		String s3Key = (String)contentNode.getMetadata().get("s3Key");
		Assert.equals("https://sunbirddev.blob.core.windows.net/sunbird-content-dev/content/do_112999482416209920112/artifact/1.pdf", artifactUrl);
		Assert.equals("content/do_112999482416209920112/artifact/1.pdf", cloudStorageKey);
		Assert.equals("content/do_112999482416209920112/artifact/1.pdf", s3Key);
	}
	
	@Rule
	@Test(expected = Exception.class)
	public void TestReplaceArtifactUrlThrowException() throws Exception {
		PowerMockito.mockStatic(CloudStore.class);
		PowerMockito.doThrow(new Exception()).when(CloudStore.class);
		
		String contentNodeString = "{\"identifier\":\"do_11292666508456755211\",\"objectType\":\"Content\",\"artifactBasePath\":\"program/app\",\"artifactUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/program/app/content/do_112999482416209920112/artifact/1.pdf\",\"cloudStorageKey\":\"program/app/content/do_112999482416209920112/artifact/1.pdf\",\"s3Key\":\"program/app/content/do_112999482416209920112/artifact/1.pdf\"}";
		Map<String, Object> contentNodeMap = mapper.readValue(contentNodeString, HashMap.class);
		DefinitionDTO contentDefinition = new ControllerUtil().getDefinition("domain", "Content");
		Node contentNode = ConvertToGraphNode.convertToGraphNode(contentNodeMap, contentDefinition, null);
		PublishFinalizeUtil publishFinalizeUtil = new PublishFinalizeUtil();
		publishFinalizeUtil.replaceArtifactUrl(contentNode);
		String artifactUrl = (String)contentNode.getMetadata().get("artifactUrl");
		String cloudStorageKey = (String)contentNode.getMetadata().get("cloudStorageKey");
		String s3Key = (String)contentNode.getMetadata().get("s3Key");
		Assert.equals("https://sunbirddev.blob.core.windows.net/sunbird-content-dev/content/do_112999482416209920112/artifact/1.pdf", artifactUrl);
		Assert.equals("content/do_112999482416209920112/artifact/1.pdf", cloudStorageKey);
		Assert.equals("content/do_112999482416209920112/artifact/1.pdf", s3Key);
	}
	
	@Test
	public void testValidateAssetMediaForExternalLinkPositiveScenario() {
		Media media = new Media();
		media.setId("testid");
		media.setType("youtube");
		media.setSrc("https://www.youtube.com/watch?v=Gi2nuTLse7M");
		PublishFinalizeUtil publishFinalizeUtil = new PublishFinalizeUtil();
		Assert.isTrue(publishFinalizeUtil.validateAssetMediaForExternalLink(media), "External Link");
	}
	
	@Test
	public void testValidateAssetMediaForExternalLinkNegativeScenario() {
		Media media = new Media();
		media.setId("testid");
		media.setType("plugin");
		media.setSrc("https://sunbirddev.blob.core.windows.net/sunbird-content-dev/content/org.ekstep.simpletimer/artifact/org.ekstep.simpletimer-1.0.zip");
		PublishFinalizeUtil publishFinalizeUtil = new PublishFinalizeUtil();
		Assert.isTrue(!publishFinalizeUtil.validateAssetMediaForExternalLink(media), "External Link");
	}
	
	@Test
	public void testHandleAssetWithExternalLink() {
	
		ContentStore contentStore = PowerMockito.spy(new ContentStore());
		PowerMockito.doNothing().when(contentStore).updateExternalLink(Mockito.anyString(), Mockito.anyList());
		
		Media media = new Media();
		media.setId("testid");
		media.setType("youtube");
		media.setSrc("https://www.youtube.com/watch?v=Gi2nuTLse7M");
		Manifest manifest = new Manifest();
		manifest.setMedias(Arrays.asList(media));
		Plugin plugin = new Plugin();
		plugin.setManifest(manifest);
		
		PublishFinalizeUtil publishFinalizeUtil = new PublishFinalizeUtil(contentStore);
		publishFinalizeUtil.handleAssetWithExternalLink(plugin, "ECML_CONTENT_ID");
	}
	
	
}
