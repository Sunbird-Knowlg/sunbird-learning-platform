package org.sunbird.content.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.sunbird.common.dto.Response;
import org.sunbird.common.mgr.ConvertToGraphNode;
import org.sunbird.content.entity.Manifest;
import org.sunbird.content.entity.Media;
import org.sunbird.content.entity.Plugin;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.engine.common.GraphEngineTestSetup;
import org.sunbird.graph.model.node.DefinitionDTO;
import org.sunbird.learning.contentstore.ContentStore;
import org.sunbird.learning.util.CloudStore;
import org.sunbird.learning.util.ControllerUtil;
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
	public void testEnrichFrameworkMetadata() throws Exception {
		
		Map<String, Object> contentNodeMap = new HashMap<>();
		contentNodeMap.put("identifier", "do_11292666508456755211");
		contentNodeMap.put("objectType", "Content");
		contentNodeMap.put("framework", "ncf");
		
		String[] organisationBoardIds = {"ncf_board_cbse"};
		contentNodeMap.put("boardIds", organisationBoardIds);
		
		String[] organisationGradeLevelIds = {"ncf_gradelevel_grade1"};
		contentNodeMap.put("gradeLevelIds", organisationGradeLevelIds);
		
		String[] organisationSubjectIds = {"ncf_subject_math"};
		contentNodeMap.put("subjectIds", organisationSubjectIds);
		
		String[] organisationMediumids = {"ncf_medium_english"};
		contentNodeMap.put("mediumIds", organisationMediumids);
		
		String[] targetFrameworkIds = {"tpd"};
		contentNodeMap.put("targetFWIds", targetFrameworkIds);
		
		String[] targetBoardIds = {"tpd_board_cbse"};
		contentNodeMap.put("targetBoardIds", targetBoardIds);
		
		String[] targetGradeLevelIds = {"tpd_gradelevel_class1"};
		contentNodeMap.put("targetGradeLevelIds", targetGradeLevelIds);
		
		String[] targetSubjectIds = {"tpd_subject_math"};
		contentNodeMap.put("targetSubjectIds", targetSubjectIds);
		
		String[] targetMediumIds = {"tpd_medium_english"};
		contentNodeMap.put("targetMediumIds", targetMediumIds);
		
		String[] targetTopicIds = {"tpd_medium_abc"};
		contentNodeMap.put("targetTopicIds", targetTopicIds);
		
		
		DefinitionDTO contentDefinition = new ControllerUtil().getDefinition("domain", "Content");
		DefinitionDTO termDefinition = new ControllerUtil().getDefinition("domain", "Term");
		
		
		   
		Response response = new Response();
		Map<String, Object> termMap = new HashMap<>();
		termMap.put("identifier", "ncf_board_cbse");
		termMap.put("name", "CBSE");
		Node termOrgCbse =  ConvertToGraphNode.convertToGraphNode(termMap, termDefinition, null);
		
		termMap = new HashMap<>();
		termMap.put("identifier", "ncf_gradelevel_grade1");
		termMap.put("name", "Class 1");
		Node termOrgGradeLevel =  ConvertToGraphNode.convertToGraphNode(termMap, termDefinition, null);
		
		termMap = new HashMap<>();
		termMap.put("identifier", "ncf_subject_math");
		termMap.put("name", "Math");
		Node termOrgSubject =  ConvertToGraphNode.convertToGraphNode(termMap, termDefinition, null);
		
		termMap = new HashMap<>();
		termMap.put("identifier", "ncf_medium_english");
		termMap.put("name", "English");
		Node terOrgMedium =  ConvertToGraphNode.convertToGraphNode(termMap, termDefinition, null);
		
		termMap = new HashMap<>();
		termMap.put("identifier", "tpd_board_cbse");
		termMap.put("name", "CBSE Board");
		Node termTarCbse =  ConvertToGraphNode.convertToGraphNode(termMap, termDefinition, null);
		
		termMap = new HashMap<>();
		termMap.put("identifier", "tpd_gradelevel_class1");
		termMap.put("name", "Class 1");
		Node termTarGradeLevel =  ConvertToGraphNode.convertToGraphNode(termMap, termDefinition, null);
		
		termMap = new HashMap<>();
		termMap.put("identifier", "tpd_subject_math");
		termMap.put("name", "Mathematics");
		Node termTarSubject =  ConvertToGraphNode.convertToGraphNode(termMap, termDefinition, null);
		
		termMap = new HashMap<>();
		termMap.put("identifier", "tpd_medium_english");
		termMap.put("name", "English Medium");
		Node terTarMedium =  ConvertToGraphNode.convertToGraphNode(termMap, termDefinition, null);
		
		termMap = new HashMap<>();
		termMap.put("identifier", "tpd_medium_abc");
		termMap.put("name", "Abc");
		Node terTarTopic =  ConvertToGraphNode.convertToGraphNode(termMap, termDefinition, null);
		
		List<Node> nodeList = Arrays.asList(termOrgCbse, termOrgGradeLevel, termOrgSubject, terOrgMedium, termTarCbse, termTarGradeLevel, termTarSubject, terTarMedium, terTarTopic);
		response.put(GraphDACParams.node_list.name(), nodeList);
		
		
		ControllerUtil controllerUtil = PowerMockito.spy(new ControllerUtil());
		PowerMockito.when(controllerUtil.getDataNodes(Mockito.anyString(), Mockito.anyList())).thenReturn(response);
		
		Node contentNode = ConvertToGraphNode.convertToGraphNode(contentNodeMap, contentDefinition, null);
		PublishFinalizeUtil publishFinalizeUtil = new PublishFinalizeUtil(controllerUtil);
		Map<String, List<String>> frameworkMetadata = publishFinalizeUtil.enrichFrameworkMetadata(contentNode);
		Assert.isTrue(((List<String>)frameworkMetadata.get("se_FWIds")).containsAll(Arrays.asList("ncf", "tpd")));
		Assert.isTrue(((List<String>)frameworkMetadata.get("se_boardIds")).containsAll(Arrays.asList("ncf_board_cbse", "tpd_board_cbse")));
		Assert.isTrue(((List<String>)frameworkMetadata.get("se_subjectIds")).containsAll(Arrays.asList("ncf_subject_math", "tpd_subject_math")));
		Assert.isTrue(((List<String>)frameworkMetadata.get("se_mediumIds")).containsAll(Arrays.asList("ncf_medium_english", "tpd_medium_english")));
		Assert.isTrue(((List<String>)frameworkMetadata.get("se_gradeLevelIds")).containsAll(Arrays.asList("ncf_gradelevel_grade1", "tpd_gradelevel_class1")));
		Assert.isTrue(((List<String>)frameworkMetadata.get("se_topicIds")).containsAll(Arrays.asList("tpd_medium_abc")));
		
		Assert.isTrue(((List<String>)frameworkMetadata.get("se_boards")).containsAll(Arrays.asList("CBSE", "CBSE Board")));
		Assert.isTrue(((List<String>)frameworkMetadata.get("se_subjects")).containsAll(Arrays.asList("Math", "Mathematics")));
		Assert.isTrue(((List<String>)frameworkMetadata.get("se_mediums")).containsAll(Arrays.asList("English", "English Medium")));
		Assert.isTrue(((List<String>)frameworkMetadata.get("se_gradeLevels")).containsAll(Arrays.asList("Class 1")));
	}
	
	@Test
	public void testRevalidateFrameworkCategoryMetadata() throws Exception {
		DefinitionDTO contentDefinition = new ControllerUtil().getDefinition("domain", "Content");
		ControllerUtil controllerUtil = PowerMockito.spy(new ControllerUtil());
		Map<String, List<String>> frameworkMetadata = new HashMap<String, List<String>>();
		Map<String, Object> contentNodeMap = new HashMap<>();
		contentNodeMap.put("identifier", "do_11292666508456755211");
		contentNodeMap.put("objectType", "Content");
		contentNodeMap.put("framework", "ncf");
		contentNodeMap.put("name", "Abc");
		String[] sub = {"English"};
		contentNodeMap.put("subject", sub);
		Node node = ConvertToGraphNode.convertToGraphNode(contentNodeMap, contentDefinition, null);
		PublishFinalizeUtil publishFinalizeUtil = new PublishFinalizeUtil(controllerUtil);
		publishFinalizeUtil.revalidateFrameworkCategoryMetadata(frameworkMetadata, node);
		Assert.isTrue(MapUtils.isNotEmpty(frameworkMetadata));
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
		media.setSrc("https://sunbirddev.blob.core.windows.net/sunbird-content-dev/content/org.sunbird.simpletimer/artifact/org.sunbird.simpletimer-1.0.zip");
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
