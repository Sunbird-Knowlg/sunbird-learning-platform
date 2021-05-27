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
import org.sunbird.graph.dac.enums.SystemNodeTypes;
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
	public void TestEnrichFrameworkMetadata() throws Exception {
		
		ControllerUtil controllerUtil = new ControllerUtil();
		
		DefinitionDTO categoryDefinition = controllerUtil.getDefinition("domain", "Category");
		DefinitionDTO termDefinition = controllerUtil.getDefinition("domain", "Term");
		DefinitionDTO contentDefinition = controllerUtil.getDefinition("domain", "Content");
		
		Node termOrgCbse =  getTermMap("ncf_board_cbse", "CBSE", termDefinition);
		Node termOrgGradeLevel =  getTermMap("ncf_gradelevel_grade1", "Class 1", termDefinition);
		Node termOrgSubject =  getTermMap("ncf_subject_math", "Math", termDefinition);
		Node terOrgMedium =  getTermMap("ncf_medium_english", "English", termDefinition);
		Node termTarCbse =  getTermMap("tpd_board_cbse", "CBSE Board", termDefinition);
		Node termTarGradeLevel =  getTermMap("tpd_gradelevel_class1", "Class 1", termDefinition);
		Node termTarSubject =  getTermMap("tpd_subject_math", "Mathematics", termDefinition);
		Node terTarMedium =  getTermMap("tpd_medium_english", "English Medium", termDefinition);
		Node terTarTopic =  getTermMap("tpd_medium_abc", "Abc", termDefinition);
		
		List<Node> nodeList = Arrays.asList(termOrgCbse, termOrgGradeLevel, termOrgSubject, terOrgMedium, termTarCbse, termTarGradeLevel, termTarSubject, terTarMedium, terTarTopic);
		Response response = new Response();
		response.put(GraphDACParams.node_list.name(), nodeList);
		
		Node boardcategory =  getMasterCategory("board", "boardIds", "targetBoardIds", "se_boardIds", "se_boards", categoryDefinition);
		Node mediumcategory =  getMasterCategory("medium", "mediumIds", "targetMediumIds", "se_mediumIds", "se_mediums", categoryDefinition);
		Node subjectcategory =  getMasterCategory("subject", "subjectIds", "targetSubjectIds", "se_subjectIds", "se_subjects", categoryDefinition);
		Node gradeLevelcategory =  getMasterCategory("gradeLevel", "gradeLevelIds", "targetGradeLevelIds", "se_gradeLevelIds", "se_gradeLevels", categoryDefinition);
		Node topiccategory =  getMasterCategory("topic", "topicIds", "targetTopicIds", "se_topicIds", "se_topics", categoryDefinition);
		
		List<Node> categoryNodeList = Arrays.asList(boardcategory, mediumcategory, subjectcategory, gradeLevelcategory, topiccategory);
		
		ControllerUtil mockControllerUtil = PowerMockito.spy(new ControllerUtil());
		PowerMockito.when(mockControllerUtil.getNodes(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(categoryNodeList);
		PowerMockito.when(mockControllerUtil.getDataNodes(Mockito.anyString(), Mockito.anyList())).thenReturn(response);
	
		Map<String, Object> contentNodeMap = new HashMap<>();
		contentNodeMap.put("identifier", "do_11292666508456755211");
		contentNodeMap.put("objectType", "Content");
		contentNodeMap.put("framework", "ncf");
		
		enrichContentMapWithFrameworkCategoryData(contentNodeMap, "boardIds", "ncf_board_cbse");
		enrichContentMapWithFrameworkCategoryData(contentNodeMap, "gradeLevelIds", "ncf_gradelevel_grade1");
		enrichContentMapWithFrameworkCategoryData(contentNodeMap, "subjectIds", "ncf_subject_math");
		enrichContentMapWithFrameworkCategoryData(contentNodeMap, "mediumIds", "ncf_medium_english");
		enrichContentMapWithFrameworkCategoryData(contentNodeMap, "targetFWIds", "tpd");
		enrichContentMapWithFrameworkCategoryData(contentNodeMap, "targetBoardIds", "tpd_board_cbse");
		enrichContentMapWithFrameworkCategoryData(contentNodeMap, "targetGradeLevelIds", "tpd_gradelevel_class1");
		enrichContentMapWithFrameworkCategoryData(contentNodeMap, "targetSubjectIds", "tpd_subject_math");
		enrichContentMapWithFrameworkCategoryData(contentNodeMap, "targetMediumIds", "tpd_medium_english");
		enrichContentMapWithFrameworkCategoryData(contentNodeMap, "targetTopicIds", "tpd_medium_abc");
		
		Node contentNode = ConvertToGraphNode.convertToGraphNode(contentNodeMap, contentDefinition, null);
		PublishFinalizeUtil publishFinalizeUtil = new PublishFinalizeUtil(mockControllerUtil);
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
	
	private void enrichContentMapWithFrameworkCategoryData(Map<String, Object> contentNodeMap, String fieldName, String topicId) {
		String[] targetTopicIds = {topicId};
		contentNodeMap.put(fieldName, targetTopicIds);
	}
	
	private Node getTermMap(String identifier, String name, DefinitionDTO termDefinition) throws Exception {
		Map<String, Object> termMap = new HashMap<>();
		termMap.put("identifier", identifier);
		termMap.put("name", name);
		Node termNode =  ConvertToGraphNode.convertToGraphNode(termMap, termDefinition, null);
		return termNode;
	}
	private Node getMasterCategory(String code, String orgIdFieldName, String targetIdFieldName, String searchIdFieldName, String searchLabelFieldName, DefinitionDTO categoryDefinition) throws Exception {
		Map<String, Object> categoryMap = new HashMap<>();
		categoryMap.put("identifier", code);
		categoryMap.put("objectType", "Category");
		categoryMap.put("code", code);
		categoryMap.put("orgIdFieldName", orgIdFieldName);
		categoryMap.put("targetIdFieldName", targetIdFieldName);
		categoryMap.put("searchIdFieldName", searchIdFieldName);
		categoryMap.put("searchLabelFieldName", searchLabelFieldName);
		Node categoryNode =  ConvertToGraphNode.convertToGraphNode(categoryMap, categoryDefinition, null);
		categoryNode.setNodeType(SystemNodeTypes.DATA_NODE.name());
		return categoryNode;
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
