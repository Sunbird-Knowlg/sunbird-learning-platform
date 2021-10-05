package org.sunbird.taxonomy.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.graph.cache.util.RedisStoreUtil;
import org.sunbird.graph.engine.common.TestParams;
import org.sunbird.graph.model.cache.CategoryCache;
import org.sunbird.learning.hierarchy.store.HierarchyStore;
import org.sunbird.learning.router.LearningRequestRouterPool;
import org.sunbird.searchindex.elasticsearch.ElasticSearchUtil;
import org.sunbird.searchindex.util.CompositeSearchConstants;
import org.sunbird.taxonomy.mgr.impl.ContentManagerImpl;
import org.sunbird.test.common.CommonTestSetup;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Test Cases for ContentV3Controller
 * 
 * @author gauraw
 *
 */

@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class ContentV3ControllerTest extends CommonTestSetup {

	/** The context. */
	@Autowired
	private WebApplicationContext context;

	/** The actions. */
	private ResultActions actions;

	private MockMvc mockMvc;

	private static final String basePath = "/content/v3";
	private static ObjectMapper mapper = new ObjectMapper();

	private static String[] validDialCode = { "ABC123", "BCD123", "CDE123", "DEF123", "EFG123" };
	private static String createDocumentContent = "{\"request\": {\"content\": {\"name\": \"Unit Test Content\",\"code\": \"test_code\",\"contentType\": \"Resource\",\"mimeType\": \"application/pdf\",\"tags\": [\"colors\", \"games\"]}}}";
	private static final String SCRIPT_1 = "CREATE KEYSPACE IF NOT EXISTS content_store_test WITH replication = {'class': 'SimpleStrategy','replication_factor': '1'};";
	private static final String SCRIPT_2 = "CREATE TABLE IF NOT EXISTS content_store_test.content_data (content_id text, last_updated_on timestamp,body blob,oldBody blob,screenshots blob,stageIcons blob,PRIMARY KEY (content_id));";
	private static final String SCRIPT_3 = "CREATE KEYSPACE IF NOT EXISTS hierarchy_store_test WITH replication = {'class': 'SimpleStrategy','replication_factor': '1'};";
	private static final String SCRIPT_4 = "CREATE TABLE IF NOT EXISTS hierarchy_store_test.content_hierarchy_test (identifier text, hierarchy text, PRIMARY KEY (identifier));";

	private static String contentId = "";
	private static String contentId2 = "";
	private static String versionKey = "";
	private static String versionKey2 = "";
	private static String frContentId = "";
	private static String passKey = Platform.config.getString("graph.passport.key.base");

	private static String DIALCODE_INDEX = "testdialcode";

	private static String collectionContent1Id = "";
	private static String collectionVersion1Key = "";
	private static String collectionContent2Id = "";
	private static String collectionVersion2Key = "";
	private static String collectionContent3Id = "";
	private static String collectionVersion3Key = "";
	
	private static String channelId = "in.ekstep";

	@BeforeClass
	public static void setup() throws Exception {
		loadDefinition("definitions/content_definition.json","definitions/content_image_definition.json", "definitions/concept_definition.json",
				"definitions/dimension_definition.json", "definitions/domain_definition.json");
		executeScript(SCRIPT_1, SCRIPT_2, SCRIPT_3, SCRIPT_4);
		LearningRequestRouterPool.init();
		createFramework();
		createDocumentContent();
		createCollectionContent();
		createDialCodeIndex();
		uploadContent();
	}

	@Before
	public void init() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
		if (StringUtils.isBlank(frContentId))
			createContentWithFramework();
	}

	@AfterClass
	public static void clean() throws Exception {
		ElasticSearchUtil.deleteIndex(DIALCODE_INDEX);
	}

    public void delay(long millis){
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	/*
	 * Framework Name: NCFTEST
	 * 
	 * Framework Category: medium, subject, board, gradeLevel, topic
	 * 
	 * Framework Term: english, math, cbse, class 1, addition
	 *
	 */
	public static void createFramework() throws Exception {
		String fwHierarchy = "{\"categories\":[{\"identifier\":\"ncftest_medium\",\"code\":\"medium\",\"terms\":[{\"identifier\":\"ncftest_medium_english\",\"code\":\"english\",\"name\":\"english\",\"description\":\"English Medium\",\"index\":1,\"category\":\"medium\",\"status\":\"Live\"}],\"name\":\"medium\",\"description\":\"Medium for NCFTEST\",\"index\":1,\"status\":\"Live\"},{\"identifier\":\"ncftest_subject\",\"code\":\"subject\",\"terms\":[{\"identifier\":\"ncftest_subject_math\",\"code\":\"math\",\"name\":\"math\",\"description\":\"Mathematics\",\"index\":1,\"category\":\"subject\",\"status\":\"Live\"}],\"name\":\"subject\",\"description\":\"Subject for NCFTEST\",\"index\":2,\"status\":\"Live\"},{\"identifier\":\"ncftest_board\",\"code\":\"board\",\"terms\":[{\"identifier\":\"ncftest_board_cbse\",\"code\":\"cbse\",\"name\":\"cbse\",\"description\":\"CBSE Board\",\"index\":1,\"category\":\"board\",\"status\":\"Live\"}],\"name\":\"board\",\"description\":\"Board for NCFTEST\",\"index\":3,\"status\":\"Live\"},{\"identifier\":\"ncftest_topic\",\"code\":\"topic\",\"terms\":[{\"identifier\":\"ncftest_topic_addition\",\"code\":\"addition\",\"name\":\"addition\",\"description\":\"Addition\",\"index\":1,\"category\":\"topic\",\"status\":\"Live\"}],\"name\":\"topic\",\"description\":\"Topics for NCFTEST\",\"index\":4,\"status\":\"Live\"},{\"identifier\":\"ncftest_gradelevel\",\"code\":\"gradeLevel\",\"terms\":[{\"identifier\":\"ncftest_gradelevel_class-1\",\"code\":\"class 1\",\"name\":\"class 1\",\"description\":\"Class 1\",\"index\":1,\"category\":\"gradeLevel\",\"status\":\"Live\"}],\"name\":\"gradeLevel\",\"description\":\"Grade Level for NCFTEST\",\"index\":5,\"status\":\"Live\"}]}";
		Map<String, Object> frameworkHierarchy = mapper.readValue(fwHierarchy,
				new TypeReference<Map<String, Object>>() {
				});
		CategoryCache.setFramework("NCFTEST", frameworkHierarchy);
	}

	private void createContentWithFramework() throws Exception {
		String request = "{\"request\": {\"content\": {\"identifier\":\"LP_UTEST_00\",\"name\": \"Unit Test Content\",\"framework\":\"NCFTEST\",\"code\": \"unit.test\",\"contentType\": \"Resource\",\"mimeType\": \"application/pdf\",\"tags\": [\"colors\", \"games\"]}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		if (200 == actions.andReturn().getResponse().getStatus())
			frContentId = "LP_UTEST_00";
	}

	private static void createDialCodeIndex() throws IOException {
		CompositeSearchConstants.DIAL_CODE_INDEX = DIALCODE_INDEX;
		ElasticSearchUtil.initialiseESClient(DIALCODE_INDEX, Platform.config.getString("dialcode.es_conn_info"));
		String settings = "{ \"analysis\": {       \"analyzer\": {         \"dc_index_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"lowercase\",             \"mynGram\"           ]         },         \"dc_search_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"standard\",             \"lowercase\"           ]         },         \"keylower\": {           \"tokenizer\": \"keyword\",           \"filter\": \"lowercase\"         }       },       \"filter\": {         \"mynGram\": {           \"type\": \"nGram\",           \"min_gram\": 1,           \"max_gram\": 20,           \"token_chars\": [             \"letter\",             \"digit\",             \"whitespace\",             \"punctuation\",             \"symbol\"           ]         }       }     }   }";
		String mappings = "{\"dynamic_templates\":[{\"longs\":{\"match_mapping_type\":\"long\",\"mapping\":{\"type\":\"long\",\"fields\":{\"raw\":{\"type\":\"long\"}}}}},{\"booleans\":{\"match_mapping_type\":\"boolean\",\"mapping\":{\"type\":\"boolean\",\"fields\":{\"raw\":{\"type\":\"boolean\"}}}}},{\"doubles\":{\"match_mapping_type\":\"double\",\"mapping\":{\"type\":\"double\",\"fields\":{\"raw\":{\"type\":\"double\"}}}}},{\"dates\":{\"match_mapping_type\":\"date\",\"mapping\":{\"type\":\"date\",\"fields\":{\"raw\":{\"type\":\"date\"}}}}},{\"strings\":{\"match_mapping_type\":\"string\",\"mapping\":{\"type\":\"text\",\"copy_to\":\"all_fields\",\"analyzer\":\"dc_index_analyzer\",\"search_analyzer\":\"dc_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\"}}}}}],\"properties\":{\"all_fields\":{\"type\":\"text\",\"analyzer\":\"dc_index_analyzer\",\"search_analyzer\":\"dc_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\"}}}}}";
		ElasticSearchUtil.addIndex(CompositeSearchConstants.DIAL_CODE_INDEX,
				CompositeSearchConstants.DIAL_CODE_INDEX_TYPE, settings, mappings);

		populateData();
	}

	private static void populateData() throws JsonProcessingException, IOException {
		for (int i = 0; i < validDialCode.length; i++) {
			String dialCode = validDialCode[i];
			Map<String, Object> indexDocument = new HashMap<String, Object>();
			indexDocument.put("dialcode_index", i);
			indexDocument.put("identifier", dialCode);
			indexDocument.put("channel", "channelTest");
			indexDocument.put("batchcode", "test_math_std1");
			indexDocument.put("publisher", "mock_pub01");
			indexDocument.put("status", "Draft");
			indexDocument.put("generated_on", "2018-01-30T16:50:40.562");
			indexDocument.put("index", "true");
			indexDocument.put("operationType", "CREATE");
			indexDocument.put("nodeType", "EXTERNAL");
			indexDocument.put("userId", "ANONYMOUS");
			indexDocument.put("createdOn", "2018-01-30T16:50:40.593+0530");
			indexDocument.put("userId", "ANONYMOUS");
			indexDocument.put("objectType", "DialCode");

			ElasticSearchUtil.addDocumentWithId(DIALCODE_INDEX, CompositeSearchConstants.DIAL_CODE_INDEX_TYPE, dialCode,
					mapper.writeValueAsString(indexDocument));
		}
	}

	public static void createDocumentContent() throws Exception {
		for (int i = 1; i <= 2; i++) {
			ContentManagerImpl contentManager = new ContentManagerImpl();
			String createDocumentContent = "{\"osId\":\"org.sunbird.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Resource\",\"code\":\"test content\",\"mimeType\":\"application/pdf\"}";
			Map<String, Object> documentContentMap = mapper.readValue(createDocumentContent,
					new TypeReference<Map<String, Object>>() {
					});
			Response documentResponse = contentManager.create(documentContentMap, channelId);
			if (i == 1) {
				contentId = (String) documentResponse.getResult().get(TestParams.node_id.name());
				versionKey = (String) documentResponse.getResult().get(TestParams.versionKey.name());
			} else if (i == 2) {
				contentId2 = (String) documentResponse.getResult().get(TestParams.node_id.name());
				versionKey2 = (String) documentResponse.getResult().get(TestParams.versionKey.name());
			}
		}
	}

	private static void uploadContent() {
		String mimeType = "application/pdf";
		String fileUrl = "https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/u_document_04/artifact/pdf.pdf";
		ContentManagerImpl contentManager = new ContentManagerImpl();
		Response response = contentManager.upload(contentId, fileUrl, mimeType);
		String responseCode = (String) response.getResponseCode().toString();
		if ("OK".equalsIgnoreCase(responseCode))
			versionKey = (String) response.getResult().get(TestParams.versionKey.name());
	}

	public static void createCollectionContent() throws Exception {
		ContentManagerImpl contentManager = new ContentManagerImpl();
		String createCollectionContent1 = "{\"name\": \"Collection_Content_2017_Dec_27_102\",\"code\": \"Collection_Content_2017_Dec_27_102\",\"contentType\": \"Collection\",\"mimeType\": \"application/vnd.ekstep.content-collection\"}";
		Map<String, Object> collectionContentMap1 = mapper.readValue(createCollectionContent1,
				new TypeReference<Map<String, Object>>() {
				});
		Response resp1 = contentManager.create(collectionContentMap1, channelId);
		collectionContent1Id = (String) resp1.getResult().get(TestParams.node_id.name());
		collectionVersion1Key = (String) resp1.getResult().get(TestParams.versionKey.name());

		String createCollectionContent2 = "{\"name\": \"Collection_Content_2017_Dec_27_103\",\"code\": \"Collection_Content_2017_Dec_27_102\",\"contentType\": \"Collection\",\"mimeType\": \"application/vnd.ekstep.content-collection\"}";
		Map<String, Object> collectionContentMap2 = mapper.readValue(createCollectionContent2,
				new TypeReference<Map<String, Object>>() {
				});
		Response resp2 = contentManager.create(collectionContentMap2, channelId);
		collectionContent2Id = (String) resp2.getResult().get(TestParams.node_id.name());
		collectionVersion2Key = (String) resp2.getResult().get(TestParams.versionKey.name());

		String createCollectionContent3 = "{\"name\": \"Collection_Content_2017_Dec_27_104\",\"code\": \"Collection_Content_2017_Dec_27_102\",\"contentType\": \"Collection\",\"mimeType\": \"application/vnd.ekstep.content-collection\"}";
		Map<String, Object> collectionContentMap3 = mapper.readValue(createCollectionContent3,
				new TypeReference<Map<String, Object>>() {
				});
		Response resp3 = contentManager.create(collectionContentMap3, channelId);
		collectionContent3Id = (String) resp3.getResult().get(TestParams.node_id.name());
		collectionVersion3Key = (String) resp3.getResult().get(TestParams.versionKey.name());
	}

	private static File getResourceFile(String fileName) {
		File file = new File(ContentV3ControllerTest.class.getResource("/UploadFiles/" + fileName).getFile());
		return file;
	}

	/*
	 * Create Document Content
	 * 
	 */

	@Test
	public void testContentV3Controller_01() throws Exception {
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(createDocumentContent));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Read Document Content
	 * 
	 */
	@Test
	public void testContentV3Controller_02() throws Exception {
		String path = basePath + "/read/" + contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Update Content
	 * 
	 */
	@Test
	public void testContentV3Controller_03() throws Exception {
		String path = basePath + "/update/" + contentId2;
		String updateDocumentContent = "{\"request\": {\"content\": {\"name\": \"Updated Test Contnet\",\"versionKey\": \""
				+ versionKey2 + "\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(updateDocumentContent));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Upload with File. Expect: 200
	 * 
	 */
	@Test
	public void testContentV3Controller_04() throws Exception {
		String path = basePath + "/upload/" + contentId;
		File inputFile = getResourceFile("test1.pdf");
		FileInputStream fileStream = new FileInputStream(inputFile);
		MockMultipartFile testFile = new MockMultipartFile("file", "test1.pdf", "application/pdf", fileStream);
		actions = mockMvc.perform(MockMvcRequestBuilders.fileUpload(path).file(testFile).header("user-id", "ilimi"));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Review Content
	 * 
	 */
	@Ignore
	@Test
	public void testContentV3Controller_05() throws Exception {
		String path = basePath + "/review/" + contentId;
		String publishReqBody = "{\"request\": {\"content\" : {}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(publishReqBody));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Publish Content
	 * 
	 */
	@Ignore
	@Test
	public void testContentV3Controller_06() throws Exception {
		String path = basePath + "/publish/" + contentId;
		String publishReqBody = "{\"request\": {\"content\" : {\"lastPublishedBy\" : \"Ekstep\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(publishReqBody));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Unlisted Publish Content
	 * 
	 */
	@Ignore
	@Test
	public void testContentV3Controller_07() throws Exception {
		String path = basePath + "/upload/" + contentId2;
		File inputFile = getResourceFile("test2.pdf");
		FileInputStream fileStream = new FileInputStream(inputFile);
		MockMultipartFile testFile = new MockMultipartFile("file", "test2.pdf", "application/pdf", fileStream);
		actions = mockMvc.perform(MockMvcRequestBuilders.fileUpload(path).file(testFile).header("user-id", "ilimi"));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());

		path = basePath + "/unlisted/publish/" + contentId2;
		String publishReqBody = "{\"request\": {\"content\" : {\"lastPublishedBy\" : \"Ekstep\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(publishReqBody));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Get Hierarchy of Content
	 * 
	 */

	@Test
	public void testContentV3Controller_08() throws Exception {
		String path = basePath + "/hierarchy/" + collectionContent3Id + "?mode=edit";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path));
		System.out.println("testContentV3Controller_08: "+  actions.andReturn().getResponse().getContentAsString());
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		
	}

	/*
	 * Bundle
	 * 
	 * Content with Body is Required.
	 * 
	 */
	@Ignore
	@Test
	public void testContentV3Controller_09() throws Exception {
		String path = basePath + "/bundle";
		String bundleReqBody = "{\"request\": {\"content_identifiers\": [\"" + contentId
				+ "\"],\"file_name\": \"test_dev_bundle\"}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(bundleReqBody));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void testContentV3Controller_10() throws Exception {
		String path = basePath + "/upload/" + contentId;
		FileInputStream fis = new FileInputStream(getResourceFile("test2.pdf"));
		MockMultipartFile multipartFile = new MockMultipartFile("file", fis);
		Map<String, String> contentTypeParams = new HashMap<String, String>();
		contentTypeParams.put("boundary", "265001916915724");
		MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);
		actions = mockMvc
				.perform(MockMvcRequestBuilders.post(path).contentType(mediaType).content(multipartFile.getBytes()));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void testContentV3Controller_11() throws Exception {
		String path = basePath + "/upload/" + contentId;
		FileInputStream fis = new FileInputStream(getResourceFile("test2.pdf"));
		MockMultipartFile multipartFile = new MockMultipartFile("file", fis);
		actions = mockMvc.perform(fileUpload(path).file(multipartFile));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

//	@Test
	public void testContentV3Controller_12() throws Exception {
		String path = basePath + "/hierarchy/" + collectionContent3Id;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path));
		System.out.println("testContentV3Controller_12: " + actions.andReturn().getResponse().getContentAsString());
//		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void testGetHeirarchyWithFailedResp() throws Exception {
		String path = basePath + "/hierarchy/do_abc";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path));
		System.out.println("testGetHeirarchyWithFailedResp: " + actions.andReturn().getResponse().getContentAsString());
//		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void testGetHeirarchyWithSuccessResp() throws Exception {
		HierarchyStore hierarchyStore = new HierarchyStore();
		String hierarchyData = "{\"code\":\"org.sunbird.jul18.story.test01\",\"keywords\":[\"QA_Content\"],\"channel\":\"in.ekstep\",\"downloadUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_11257769111443865611/course-testcourse_1535362933121_do_11257769111443865611_1.0.ecar\",\"description\":\"Text Book Test\",\"language\":[\"English\"],\"mimeType\":\"application/vnd.ekstep.content-collection\",\"variants\":{\"spine\":{\"ecarUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_11257769111443865611/course-testcourse_1535362933312_do_11257769111443865611_1.0_spine.ecar\",\"size\":1097.0}},\"idealScreenSize\":\"normal\",\"createdOn\":\"2018-08-27T15:08:04.840+0530\",\"children\":[{\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3acb\",\"downloadUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_11257769294298316813/testbook_collection1_1535362926857_do_11257769294298316813_1.0.ecar\",\"language\":[\"English\"],\"variants\":{\"spine\":{\"ecarUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_11257769294298316813/testbook_collection1_1535362931580_do_11257769294298316813_1.0_spine.ecar\",\"size\":589.0}},\"mimeType\":\"application/vnd.ekstep.content-collection\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2018-08-27T15:11:48.079+0530\",\"children\":[],\"usesContent\":[],\"contentDisposition\":\"inline\",\"contentEncoding\":\"gzip\",\"lastUpdatedOn\":\"2018-08-27T15:11:48.080+0530\",\"SYS_INTERNAL_LAST_UPDATED_ON\":\"2018-08-27T15:12:12.560+0530\",\"contentType\":\"CourseUnit\",\"identifier\":\"do_11257769294298316813\",\"audience\":[\"Learner\"],\"os\":[\"All\"],\"visibility\":\"Parent\",\"index\":1,\"mediaType\":\"content\",\"osId\":\"org.sunbird.launcher\",\"pkgVersion\":1.0,\"versionKey\":\"1535362908079\",\"idealScreenDensity\":\"hdpi\",\"s3Key\":\"ecar_files/do_11257769294298316813/testbook_collection1_1535362926857_do_11257769294298316813_1.0.ecar\",\"lastPublishedOn\":\"2018-08-27T15:12:06.771+0530\",\"size\":589.0,\"concepts\":[],\"compatibilityLevel\":4,\"name\":\"TestBook_Collection1\",\"status\":\"Live\"}],\"contentDisposition\":\"inline\",\"contentEncoding\":\"gzip\",\"lastUpdatedOn\":\"2018-08-27T15:08:04.840+0530\",\"SYS_INTERNAL_LAST_UPDATED_ON\":\"2018-08-27T15:12:13.668+0530\",\"contentType\":\"Course\",\"identifier\":\"do_11257769111443865611\",\"audience\":[\"Learner\"],\"os\":[\"All\"],\"visibility\":\"Default\",\"mediaType\":\"content\",\"osId\":\"org.sunbird.quiz.app\",\"pkgVersion\":1.0,\"versionKey\":\"1535362684840\",\"tags\":[\"QA_Content\"],\"idealScreenDensity\":\"hdpi\",\"s3Key\":\"ecar_files/do_11257769111443865611/course-testcourse_1535362933121_do_11257769111443865611_1.0.ecar\",\"framework\":\"NCF\",\"lastPublishedOn\":\"2018-08-27T15:12:13.076+0530\",\"size\":1098.0,\"compatibilityLevel\":4,\"name\":\"Course TestCourse\",\"status\":\"Live\"}";
		hierarchyStore.saveOrUpdateHierarchy("do_11257769111443865611", mapper.readValue(hierarchyData, Map.class));
		RedisStoreUtil.saveNodeProperty("domain", "do_11257769111443865611", "status", "Live");
		delay(15000);
		String path = basePath + "/hierarchy/do_11257769111443865611";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void testGetHeirarchyWithBookMarkIdSuccessResp() throws Exception {
		HierarchyStore hierarchyStore = new HierarchyStore();
		String hierarchyData = "{\"code\":\"org.sunbird.jul18.story.test01\",\"keywords\":[\"QA_Content\"],\"channel\":\"in.ekstep\",\"downloadUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_11257769111443865611/course-testcourse_1535362933121_do_11257769111443865611_1.0.ecar\",\"description\":\"Text Book Test\",\"language\":[\"English\"],\"mimeType\":\"application/vnd.ekstep.content-collection\",\"variants\":{\"spine\":{\"ecarUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_11257769111443865611/course-testcourse_1535362933312_do_11257769111443865611_1.0_spine.ecar\",\"size\":1097.0}},\"idealScreenSize\":\"normal\",\"createdOn\":\"2018-08-27T15:08:04.840+0530\",\"children\":[{\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3acb\",\"downloadUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_11257769294298316813/testbook_collection1_1535362926857_do_11257769294298316813_1.0.ecar\",\"language\":[\"English\"],\"variants\":{\"spine\":{\"ecarUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_11257769294298316813/testbook_collection1_1535362931580_do_11257769294298316813_1.0_spine.ecar\",\"size\":589.0}},\"mimeType\":\"application/vnd.ekstep.content-collection\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2018-08-27T15:11:48.079+0530\",\"children\":[],\"usesContent\":[],\"contentDisposition\":\"inline\",\"contentEncoding\":\"gzip\",\"lastUpdatedOn\":\"2018-08-27T15:11:48.080+0530\",\"sYS_INTERNAL_LAST_UPDATED_ON\":\"2018-08-27T15:12:12.560+0530\",\"contentType\":\"CourseUnit\",\"identifier\":\"do_11257769294298316813\",\"audience\":[\"Learner\"],\"os\":[\"All\"],\"visibility\":\"Parent\",\"index\":1,\"mediaType\":\"content\",\"osId\":\"org.sunbird.launcher\",\"pkgVersion\":1.0,\"versionKey\":\"1535362908079\",\"idealScreenDensity\":\"hdpi\",\"s3Key\":\"ecar_files/do_11257769294298316813/testbook_collection1_1535362926857_do_11257769294298316813_1.0.ecar\",\"lastPublishedOn\":\"2018-08-27T15:12:06.771+0530\",\"size\":589.0,\"concepts\":[],\"compatibilityLevel\":4,\"name\":\"TestBook_Collection1\",\"status\":\"Live\"}],\"contentDisposition\":\"inline\",\"contentEncoding\":\"gzip\",\"lastUpdatedOn\":\"2018-08-27T15:08:04.840+0530\",\"sYS_INTERNAL_LAST_UPDATED_ON\":\"2018-08-27T15:12:13.668+0530\",\"contentType\":\"Course\",\"identifier\":\"do_11257769111443865611\",\"audience\":[\"Learner\"],\"os\":[\"All\"],\"visibility\":\"Default\",\"mediaType\":\"content\",\"osId\":\"org.sunbird.quiz.app\",\"pkgVersion\":1.0,\"versionKey\":\"1535362684840\",\"tags\":[\"QA_Content\"],\"idealScreenDensity\":\"hdpi\",\"s3Key\":\"ecar_files/do_11257769111443865611/course-testcourse_1535362933121_do_11257769111443865611_1.0.ecar\",\"framework\":\"NCF\",\"lastPublishedOn\":\"2018-08-27T15:12:13.076+0530\",\"size\":1098.0,\"compatibilityLevel\":4,\"name\":\"Course TestCourse\",\"status\":\"Live\"}";
		hierarchyStore.saveOrUpdateHierarchy("do_11257769111443865611", mapper.readValue(hierarchyData, Map.class));
		RedisStoreUtil.saveNodeProperty("domain", "do_11257769111443865611", "status", "Live");
		delay(15000);
		String path = basePath + "/hierarchy/do_11257769111443865611/do_11257769294298316813";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void testGetHeirarchyWithInvalidBookMarkId404Resp() throws Exception {
		HierarchyStore hierarchyStore = new HierarchyStore();
		String hierarchyData = "{\"code\":\"org.sunbird.jul18.story.test01\",\"keywords\":[\"QA_Content\"],\"channel\":\"in.ekstep\",\"downloadUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_11257769111443865611/course-testcourse_1535362933121_do_11257769111443865611_1.0.ecar\",\"description\":\"Text Book Test\",\"language\":[\"English\"],\"mimeType\":\"application/vnd.ekstep.content-collection\",\"variants\":{\"spine\":{\"ecarUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_11257769111443865611/course-testcourse_1535362933312_do_11257769111443865611_1.0_spine.ecar\",\"size\":1097.0}},\"idealScreenSize\":\"normal\",\"createdOn\":\"2018-08-27T15:08:04.840+0530\",\"children\":[{\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3acb\",\"downloadUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_11257769294298316813/testbook_collection1_1535362926857_do_11257769294298316813_1.0.ecar\",\"language\":[\"English\"],\"variants\":{\"spine\":{\"ecarUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_11257769294298316813/testbook_collection1_1535362931580_do_11257769294298316813_1.0_spine.ecar\",\"size\":589.0}},\"mimeType\":\"application/vnd.ekstep.content-collection\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2018-08-27T15:11:48.079+0530\",\"children\":[],\"usesContent\":[],\"contentDisposition\":\"inline\",\"contentEncoding\":\"gzip\",\"lastUpdatedOn\":\"2018-08-27T15:11:48.080+0530\",\"sYS_INTERNAL_LAST_UPDATED_ON\":\"2018-08-27T15:12:12.560+0530\",\"contentType\":\"CourseUnit\",\"identifier\":\"do_11257769294298316813\",\"audience\":[\"Learner\"],\"os\":[\"All\"],\"visibility\":\"Parent\",\"index\":1,\"mediaType\":\"content\",\"osId\":\"org.sunbird.launcher\",\"pkgVersion\":1.0,\"versionKey\":\"1535362908079\",\"idealScreenDensity\":\"hdpi\",\"s3Key\":\"ecar_files/do_11257769294298316813/testbook_collection1_1535362926857_do_11257769294298316813_1.0.ecar\",\"lastPublishedOn\":\"2018-08-27T15:12:06.771+0530\",\"size\":589.0,\"concepts\":[],\"compatibilityLevel\":4,\"name\":\"TestBook_Collection1\",\"status\":\"Live\"}],\"contentDisposition\":\"inline\",\"contentEncoding\":\"gzip\",\"lastUpdatedOn\":\"2018-08-27T15:08:04.840+0530\",\"sYS_INTERNAL_LAST_UPDATED_ON\":\"2018-08-27T15:12:13.668+0530\",\"contentType\":\"Course\",\"identifier\":\"do_11257769111443865611\",\"audience\":[\"Learner\"],\"os\":[\"All\"],\"visibility\":\"Default\",\"mediaType\":\"content\",\"osId\":\"org.sunbird.quiz.app\",\"pkgVersion\":1.0,\"versionKey\":\"1535362684840\",\"tags\":[\"QA_Content\"],\"idealScreenDensity\":\"hdpi\",\"s3Key\":\"ecar_files/do_11257769111443865611/course-testcourse_1535362933121_do_11257769111443865611_1.0.ecar\",\"framework\":\"NCF\",\"lastPublishedOn\":\"2018-08-27T15:12:13.076+0530\",\"size\":1098.0,\"compatibilityLevel\":4,\"name\":\"Course TestCourse\",\"status\":\"Live\"}";
		hierarchyStore.saveOrUpdateHierarchy("do_11257769111443865611", mapper.readValue(hierarchyData, Map.class));
		RedisStoreUtil.saveNodeProperty("domain", "do_11257769111443865611", "status", "Live");
		delay(15000);
		String path = basePath + "/hierarchy/do_11257769111443865611/do_1125776929429831681";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Given: Link Single Dial Code to Single Valid Content. (Data Given as
	 * List) When: Link Dial Code API Hits Then: 200 - OK.
	 * 
	 */
	@SuppressWarnings("unchecked")
//	@Test
	public void testDialCodeLink_01() throws Exception {
		// Create Document Content
		String path = basePath + "/create";
		String contentCreateReq = "{\"request\": {\"content\": {\"name\": \"Test Resource Content\",\"code\": \"test.res.content\",\"mimeType\": \"application/pdf\",\"contentType\":\"Resource\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(contentCreateReq));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response response = getResponse(actions);
		String nodeId = (String) response.getResult().get("node_id");

		// Upload Content
		path = basePath + "/upload/" + nodeId;
		File inputFile = getResourceFile("test2.pdf");
		FileInputStream fileStream = new FileInputStream(inputFile);
		MockMultipartFile testFile = new MockMultipartFile("file", "test2.pdf", "application/pdf", fileStream);
		actions = mockMvc.perform(MockMvcRequestBuilders.fileUpload(path).file(testFile).header("user-id", "ilimi"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());

		// Publish Content
		path = basePath + "/publish/" + nodeId;
		String publishReqBody = "{\"request\": {\"content\" : {\"lastPublishedBy\" : \"Ekstep\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(publishReqBody));
		assertEquals(200, actions.andReturn().getResponse().getStatus());

		boolean published = false;
        while(!published) {
            delay(5000);
            path = basePath + "/read/" + nodeId;
            actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("X-Channel-Id", "channelTest"));
            assertEquals(200, actions.andReturn().getResponse().getStatus());
            response = getResponse(actions);
            String status = (String)((Map<String, Object>) response.getResult()
                    .get("content")).get("status");
            if(StringUtils.equalsIgnoreCase(status, "Live"))
                published = true;
        }

		// Link DialCode
		path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + nodeId
				+ "\"],\"dialcode\": [\"ABC123\"]}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		assertEquals(200, actions.andReturn().getResponse().getStatus());

		// Read Content
		path = basePath + "/read/" + nodeId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("X-Channel-Id", "channelTest"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		response = getResponse(actions);
		List<String> dialcodesLive = (List<String>) (List<String>) ((Map<String, Object>) response.getResult()
				.get("content")).get("dialcodes");
		assertNull(dialcodesLive);

		// Read Image Content
		path = basePath + "/read/" + nodeId + "?mode=edit";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("X-Channel-Id", "channelTest"));
		response = getResponse(actions);
		List<String> dialcodes = (List<String>) (List<String>) ((Map<String, Object>) response.getResult()
				.get("content")).get("dialcodes");
		assertEquals("ABC123", (String) dialcodes.get(0));
	}

	/*
	 * Given: Link Single Dial Code to Single Valid Content. (Data given as
	 * String) When: Link Dial Code API Hits Then: 200 - OK.
	 * 
	 */
	@Test
	public void testDialCodeLink_02() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": \"" + contentId
				+ "\",\"dialcode\": \"ABC123\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());

	}

	/*
	 * Given: Link Single Dial Code (Valid Dial Code) to Single Invalid Content
	 * (Data Given as String). (Data given as String) When: Link Dial Code API
	 * Hits Then: 404 - RESOURCE_NOT_FOUND
	 * 
	 */
	@Test
	public void testDialCodeLink_03() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": \"" + "abc123"
				+ "\",\"dialcode\": \"ABC123\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());

	}

	/*
	 * Given: Link Single Dial Code to Single Invalid Content. (Data Given as
	 * List) When: Link Dial Code API Hits Then: 404 - RESOURCE_NOT_FOUND
	 * 
	 */
	@Test
	public void testDialCodeLink_04() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + "abc123"
				+ "\"],\"dialcode\": [\"ABC123\"]}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Given: Link Single Dial Code to Single Content with invalid uri. (Data
	 * Given as List) When: Link Dial Code API Hits Then: 404 - Bad Request
	 * 
	 */
	@Test
	public void testDialCodeLink_05() throws Exception {
		String path = basePath + "/dialcodes/link";
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": \"" + contentId
				+ "\",\"dialcode\": \"ABC123\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Given: Link Single Dial Code to Multiple Contents with Valid Content Ids.
	 * When: Link Dial Code API Hits Then: 200 - OK
	 * 
	 */
	@Test
	public void testDialCodeLink_06() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + contentId + "\",\"" + contentId2
				+ "\"],\"dialcode\": \"ABC123\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Given: Link Multiple Dial Code to Multiple Contents with Valid Content
	 * Ids. When: Link Dial Code API Hits Then: 200 - OK
	 * 
	 */

	@Test
	public void testDialCodeLink_07() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + contentId + "\",\"" + contentId2
				+ "\"],\"dialcode\": [\"ABC123\",\"BCD123\"]}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Given:Link Multiple Dial Code (Valid Dial Code) to Single Content with
	 * Valid Content Ids. When: Link Dial Code API Hits Then: 200 - OK
	 * 
	 */

	@Test
	public void testDialCodeLink_08() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + contentId
				+ "\"],\"dialcode\": [\"ABC123\",\"BCD123\"]}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Given:Link Multiple Dial Code (With Some Invalid Dial Code) to Single
	 * Content with Valid Content Ids. When: Link Dial Code API Hits Then: 404 -
	 * RESOURCE_NOT_FOUND
	 * 
	 */
	@Ignore
	@Test
	public void testDialCodeLink_09() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + contentId
				+ "\"],\"dialcode\": [\"ABC123\",\"DDD123\"]}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Given:Link Single Dial Code (Invalid Dial Code) to Single Content with
	 * Valid Content Ids. When: Link Dial Code API Hits Then: 404 -
	 * RESOURCE_NOT_FOUND
	 * 
	 */

	//@Test
	public void testDialCodeLink_10() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\": {\"content\": [{\"identifier\": [\"" + contentId
				+ "\"],\"dialcode\": [\"DDD123\"]}]}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void testDialCodeLink_11() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\":{\"content\":[{\"dialcode\":\"ABC123\",\"identifier\":\"" + contentId
				+ "\"},{\"dialcode\":\"BCD123\",\"identifier\":\"" + contentId + "\"}]}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void testDialCodeLink_12() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\":{\"content\":[{\"dialcode\":\"ABC123\",\"identifier\":\"" + contentId
				+ "\"},{\"dialcode\":\"BCD123\",\"identifier\":\"" + "do_abc123" + "\"}]}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		Assert.assertEquals(207, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void testDialCodeLink_13() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\":{\"content\":[{\"dialcode\":\"ABC123\",\"identifier\":\"" + "do_pqr123"
				+ "\"},{\"dialcode\":\"BCD123\",\"identifier\":\"" + "do_abc123" + "\"}]}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void testDialCodeLink_14() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\":{\"content\":[{\"dialcode\":\"DDD123\",\"identifier\":\"" + "do_pqr123"
				+ "\"},{\"dialcode\":\"BCD123\",\"identifier\":\"" + "do_abc123" + "\"}]}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void testDialCodeLink_15() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\":{\"content\":[{\"dialcode\":[\"DDD123\",\"ABC123\"],\"identifier\":[\"do_abc\",\"do_def\"]},{\"dialcode\":\"BCD123\",\"identifier\":\""
				+ "do_abc123" + "\"}]}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	// Link Dial Code - Blank Identifier - Expect: 400
	@Test
	public void testDialCodeLink_16() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\":{\"content\":{\"dialcode\":\"ABC123\",\"identifier\":\"\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	// Link Dial Code - Blank Identifier ([]) - Expect: 400
	@Test
	public void testDialCodeLink_17() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\":{\"content\":{\"dialcode\":\"ABC123\",\"identifier\":[]}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	// Link Dial Code - Blank Identifier ([""]) - Expect: 400
	@Test
	public void testDialCodeLink_18() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\":{\"content\":{\"dialcode\":\"ABC123\",\"identifier\":[\"\"]}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	// Link Dial Code - Invalid Request Body - Expect: 400
	@Test
	public void testDialCodeLink_19() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\":{\"content\":{}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	// UnLink Dial Code. Provided Blank DialCode (""). Expected : 200 - OK.
	@SuppressWarnings("unchecked")
	@Test
	public void testDialCodeDeLink_01() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + contentId
				+ "\"],\"dialcode\": [\"ABC123\"]}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		assertEquals(200, actions.andReturn().getResponse().getStatus());

		path = basePath + "/read/" + contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("X-Channel-Id", "channelTest"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response response = getResponse(actions);
		List<String> dialcodes = (List<String>) (List<String>) ((Map<String, Object>) response.getResult()
				.get("content")).get("dialcodes");
		assertEquals("ABC123", (String) dialcodes.get(0));

		path = basePath + "/dialcode/link";
		String dialCodeDeLinkReq = "{\"request\":{\"content\":{\"dialcode\":\"\",\"identifier\":\"" + contentId
				+ "\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeDeLinkReq));
		assertEquals(200, actions.andReturn().getResponse().getStatus());

		path = basePath + "/read/" + contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("X-Channel-Id", "channelTest"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		List<String> dialcodeList = (List<String>) (List<String>) ((Map<String, Object>) resp.getResult()
				.get("content")).get("dialcodes");
		assertEquals(null, dialcodeList);
	}

	// UnLink Dial Code. Provided Blank DialCode ([]) Expected : 200 - OK.
	@SuppressWarnings("unchecked")
	@Test
	public void testDialCodeDeLink_02() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + contentId
				+ "\"],\"dialcode\": [\"ABC123\"]}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		assertEquals(200, actions.andReturn().getResponse().getStatus());

		path = basePath + "/read/" + contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("X-Channel-Id", "channelTest"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response response = getResponse(actions);
		List<String> dialcodes = (List<String>) (List<String>) ((Map<String, Object>) response.getResult()
				.get("content")).get("dialcodes");
		assertEquals("ABC123", (String) dialcodes.get(0));

		path = basePath + "/dialcode/link";
		String dialCodeDeLinkReq = "{\"request\":{\"content\":{\"dialcode\":[],\"identifier\":\"" + contentId + "\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeDeLinkReq));
		assertEquals(200, actions.andReturn().getResponse().getStatus());

		path = basePath + "/read/" + contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("X-Channel-Id", "channelTest"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		List<String> dialcodeList = (List<String>) (List<String>) ((Map<String, Object>) resp.getResult()
				.get("content")).get("dialcodes");
		assertEquals(null, dialcodeList);
	}

	// UnLink Dial Code. Provided Blank DialCode ([""]) Expected : 200 - OK.
	@SuppressWarnings("unchecked")
	@Test
	public void testDialCodeDeLink_03() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + contentId
				+ "\"],\"dialcode\": [\"ABC123\"]}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		assertEquals(200, actions.andReturn().getResponse().getStatus());

		path = basePath + "/read/" + contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("X-Channel-Id", "channelTest"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response response = getResponse(actions);
		List<String> dialcodes = (List<String>) (List<String>) ((Map<String, Object>) response.getResult()
				.get("content")).get("dialcodes");
		assertEquals("ABC123", (String) dialcodes.get(0));

		path = basePath + "/dialcode/link";
		String dialCodeDeLinkReq = "{\"request\":{\"content\":{\"dialcode\":[\"\"],\"identifier\":\"" + contentId
				+ "\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeDeLinkReq));
		assertEquals(200, actions.andReturn().getResponse().getStatus());

		path = basePath + "/read/" + contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("X-Channel-Id", "channelTest"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		List<String> dialcodeList = (List<String>) (List<String>) ((Map<String, Object>) resp.getResult()
				.get("content")).get("dialcodes");
		assertEquals(null, dialcodeList);
	}

	/*
	 * Create a Content without Framework Expected: Content should be created
	 * with NCF Framework
	 * 
	 */
	@Test
	public void testFrameworkLinking_01() throws Exception {
		String request = "{\"request\": {\"content\": {\"identifier\":\"LP_UTEST_01\",\"name\": \"Unit Test Content\",\"code\": \"unit.test\",\"contentType\": \"Resource\",\"mimeType\": \"application/pdf\",\"tags\": [\"colors\", \"games\"]}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		path = basePath + "/read/LP_UTEST_01";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		String frId = (String) ((Map<String, Object>) resp.getResult().get("content")).get("framework");
		assertEquals("NCF", frId);
	}

	/*
	 * Create a Content with Framework (NCFTEST). Expected: Content should be
	 * created with given Framework
	 * 
	 */
	@Test
	public void testFrameworkLinking_02() throws Exception {
		String request = "{\"request\": {\"content\": {\"identifier\":\"LP_UTEST_02\",\"name\": \"Unit Test Content\",\"framework\":\"NCFTEST\",\"code\": \"unit.test\",\"contentType\": \"Resource\",\"mimeType\": \"application/pdf\",\"tags\": [\"colors\", \"games\"]}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		path = basePath + "/read/LP_UTEST_02";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		String frId = (String) ((Map<String, Object>) resp.getResult().get("content")).get("framework");
		assertEquals("NCFTEST", frId);
	}

	/*
	 * Create Content with valid data for medium (medium=english). Expected: 200
	 * - OK. Record Should be created with given data.
	 * 
	 */
	@Test
	public void testFrameworkLinking_03() throws Exception {
		String request = "{\"request\": {\"content\": {\"identifier\":\"LP_UTEST_03\",\"name\": \"Unit Test Content\",\"framework\":\"NCFTEST\",\"medium\":[\"english\"],\"code\": \"unit.test\",\"contentType\": \"Resource\",\"mimeType\": \"application/pdf\",\"tags\": [\"colors\", \"games\"]}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		path = basePath + "/read/LP_UTEST_03";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		String medium = (String) ((Map<String, Object>) resp.getResult().get("content")).get("medium");
		assertEquals("english", medium);
	}

	/*
	 * Create Content with Invalid data for medium (medium=hindi). Expected: 400
	 * - OK. Record Should not be created with given data.
	 * 
	 */
	@Test
	public void testFrameworkLinking_04() throws Exception {
		String request = "{\"request\": {\"content\": {\"identifier\":\"LP_UTEST_04\",\"name\": \"Unit Test Content\",\"framework\":\"NCFTEST\",\"medium\":\"hindi\",\"code\": \"unit.test\",\"contentType\": \"Resource\",\"mimeType\": \"application/pdf\",\"tags\": [\"colors\", \"games\"]}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Update Content with valid data for medium (medium=english). Expected: 200
	 * - OK. Record Should be created with given data.
	 * 
	 */
	@Test
	public void testFrameworkLinking_05() throws Exception {
		String request = "{\"request\": {\"content\": {\"name\": \"Unit Test Content\",\"medium\":[\"english\"],\"versionKey\":\""
				+ passKey + "\"}}}";
		String path = basePath + "/update/" + frContentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		path = basePath + "/read/" + frContentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		String medium = (String) ((Map<String, Object>) resp.getResult().get("content")).get("medium");
		assertEquals("english", medium);
	}

	/*
	 * Update Content with Invalid data for medium (medium=hindi). Expected: 400
	 * - OK. Record Should not be updated with given data.
	 * 
	 */
	@Test
	public void testFrameworkLinking_06() throws Exception {
		String request = "{\"request\": {\"content\": {\"name\": \"Unit Test Content\",\"versionKey\":\"" + passKey
				+ "\"\"medium\":\"hindi\"}}}";
		String path = basePath + "/update/" + frContentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Create Content with valid data for board (board=cbse). Expected: 200 -
	 * OK. Record Should be created with given data.
	 * 
	 */
	@Test
	public void testFrameworkLinking_07() throws Exception {
		String request = "{\"request\": {\"content\": {\"identifier\":\"LP_UTEST_05\",\"name\": \"Unit Test Content\",\"framework\":\"NCFTEST\",\"board\":\"cbse\",\"code\": \"unit.test\",\"contentType\": \"Resource\",\"mimeType\": \"application/pdf\",\"tags\": [\"colors\", \"games\"]}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		path = basePath + "/read/LP_UTEST_05";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		String board = (String) ((Map<String, Object>) resp.getResult().get("content")).get("board");
		assertEquals("cbse", board);
	}

	/*
	 * Create Content with Invalid data for board (board=ncert). Expected: 400 -
	 * OK. Record Should not be created with given data.
	 * 
	 */
	@Test
	public void testFrameworkLinking_08() throws Exception {
		String request = "{\"request\": {\"content\": {\"identifier\":\"LP_UTEST_06\",\"name\": \"Unit Test Content\",\"framework\":\"NCFTEST\",\"board\":\"ncert\",\"code\": \"unit.test\",\"contentType\": \"Resource\",\"mimeType\": \"application/pdf\",\"tags\": [\"colors\", \"games\"]}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Update Content with valid data for board (board=cbse). Expected: 200 -
	 * OK. Record Should be created with given data.
	 * 
	 */
	@Test
	public void testFrameworkLinking_09() throws Exception {
		String request = "{\"request\": {\"content\": {\"name\": \"Unit Test Content\",\"board\":\"cbse\",\"versionKey\":\""
				+ passKey + "\"}}}";
		String path = basePath + "/update/" + frContentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		path = basePath + "/read/" + frContentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		String board = (String) ((Map<String, Object>) resp.getResult().get("content")).get("board");
		assertEquals("cbse", board);
	}

	/*
	 * Update Content with Invalid data for board (board=ncert). Expected: 400 -
	 * OK. Record Should not be updated with given data.
	 * 
	 */
	@Test
	public void testFrameworkLinking_10() throws Exception {
		String request = "{\"request\": {\"content\": {\"name\": \"Unit Test Content\",\"versionKey\":\"" + passKey
				+ "\"\"board\":\"ncert\"}}}";
		String path = basePath + "/update/" + frContentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Create Content with valid data for subject (subject=math). Expected: 200
	 * - OK. Record Should be created with given data.
	 * 
	 */
	@Test
	public void testFrameworkLinking_11() throws Exception {
		String request = "{\"request\": {\"content\": {\"identifier\":\"LP_UTEST_07\",\"name\": \"Unit Test Content\",\"framework\":\"NCFTEST\",\"subject\":[\"math\"],\"code\": \"unit.test\",\"contentType\": \"Resource\",\"mimeType\": \"application/pdf\",\"tags\": [\"colors\", \"games\"]}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		path = basePath + "/read/LP_UTEST_07";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		String subject = (String) ((Map<String, Object>) resp.getResult().get("content")).get("subject");
		assertEquals("math", subject);
	}

	/*
	 * Create Content with Invalid data for subject (subject=science). Expected:
	 * 400 - OK. Record Should not be created with given data.
	 * 
	 */
	@Test
	public void testFrameworkLinking_12() throws Exception {
		String request = "{\"request\": {\"content\": {\"identifier\":\"LP_UTEST_08\",\"name\": \"Unit Test Content\",\"framework\":\"NCFTEST\",\"subject\":\"science\",\"code\": \"unit.test\",\"contentType\": \"Resource\",\"mimeType\": \"application/pdf\",\"tags\": [\"colors\", \"games\"]}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Update Content with valid data for subject (subject=math). Expected: 200
	 * - OK. Record Should be created with given data.
	 * 
	 */
	@Test
	public void testFrameworkLinking_13() throws Exception {
		String request = "{\"request\": {\"content\": {\"name\": \"Unit Test Content\",\"subject\":[\"math\"],\"versionKey\":\""
				+ passKey + "\"}}}";
		String path = basePath + "/update/" + frContentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		path = basePath + "/read/" + frContentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		String subject = (String) ((Map<String, Object>) resp.getResult().get("content")).get("subject");
		assertEquals("math", subject);
	}

	/*
	 * Update Content with Invalid data for subject (subject=science). Expected:
	 * 400 - OK. Record Should not be updated with given data.
	 * 
	 */
	@Test
	public void testFrameworkLinking_14() throws Exception {
		String request = "{\"request\": {\"content\": {\"name\": \"Unit Test Content\",\"versionKey\":\"" + passKey
				+ "\"\"subject\":\"science\"}}}";
		String path = basePath + "/update/" + frContentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Create Content with valid data for gradeLevel (gradeLevel=class 1).
	 * Expected: 200 - OK. Record Should be created with given data.
	 * 
	 */
	@Test
	public void testFrameworkLinking_15() throws Exception {
		String request = "{\"request\": {\"content\": {\"identifier\":\"LP_UTEST_09\",\"name\": \"Unit Test Content\",\"framework\":\"NCFTEST\",\"gradeLevel\":[\"class 1\"],\"code\": \"unit.test\",\"contentType\": \"Resource\",\"mimeType\": \"application/pdf\",\"tags\": [\"colors\", \"games\"]}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		path = basePath + "/read/LP_UTEST_09";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		List<String> gradeLevel = (List<String>) ((Map<String, Object>) resp.getResult().get("content"))
				.get("gradeLevel");
		assertEquals("class 1", (String) gradeLevel.get(0));
	}

	/*
	 * Create Content with Invalid data for gradeLevel (gradeLevel=grade 1).
	 * Expected: 400 - OK. Record Should not be created with given data.
	 * 
	 */
	@Test
	public void testFrameworkLinking_16() throws Exception {
		String request = "{\"request\": {\"content\": {\"identifier\":\"LP_UTEST_10\",\"name\": \"Unit Test Content\",\"framework\":\"NCFTEST\",\"gradeLevel\":[\"grade 1\"],\"code\": \"unit.test\",\"contentType\": \"Resource\",\"mimeType\": \"application/pdf\",\"tags\": [\"colors\", \"games\"]}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Update Content with valid data for gradeLevel (gradeLevel=class 1).
	 * Expected: 200 - OK. Record Should be created with given data.
	 * 
	 */
	@Test
	public void testFrameworkLinking_17() throws Exception {
		String request = "{\"request\": {\"content\": {\"name\": \"Unit Test Content\",\"gradeLevel\":[\"class 1\"],\"versionKey\":\""
				+ passKey + "\"}}}";
		String path = basePath + "/update/" + frContentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		path = basePath + "/read/" + frContentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		List<String> gradeLevel = (List<String>) ((Map<String, Object>) resp.getResult().get("content"))
				.get("gradeLevel");
		assertEquals("class 1", (String) gradeLevel.get(0));
	}

	/*
	 * Update Content with Invalid data for gradeLevel (gradeLevel=grade 1).
	 * Expected: 400 - OK. Record Should not be updated with given data.
	 * 
	 */
	@Test
	public void testFrameworkLinking_18() throws Exception {
		String request = "{\"request\": {\"content\": {\"name\": \"Unit Test Content\",\"versionKey\":\"" + passKey
				+ "\"\"gradeLevel\":[\"grade 1\"]}}}";
		String path = basePath + "/update/" + frContentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Create Content with valid data for topic (topic=addition). Expected: 200
	 * - OK. Record Should be created with given data.
	 * 
	 */
	@Test
	public void testFrameworkLinking_19() throws Exception {
		String request = "{\"request\": {\"content\": {\"identifier\":\"LP_UTEST_11\",\"name\": \"Unit Test Content\",\"framework\":\"NCFTEST\",\"topic\":[\"addition\"],\"code\": \"unit.test\",\"contentType\": \"Resource\",\"mimeType\": \"application/pdf\",\"tags\": [\"colors\", \"games\"]}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		path = basePath + "/read/LP_UTEST_11";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		List<String> topic = (List<String>) ((Map<String, Object>) resp.getResult().get("content")).get("topic");
		assertEquals("addition", (String) topic.get(0));
	}

	/*
	 * Create Content with Invalid data for topic (topic=multiplication).
	 * Expected: 400 - OK. Record Should not be created with given data.
	 * 
	 */
	@Test
	public void testFrameworkLinking_20() throws Exception {
		String request = "{\"request\": {\"content\": {\"identifier\":\"LP_UTEST_12\",\"name\": \"Unit Test Content\",\"framework\":\"NCFTEST\",\"topic\":[\"multiplication\"],\"code\": \"unit.test\",\"contentType\": \"Resource\",\"mimeType\": \"application/pdf\",\"tags\": [\"colors\", \"games\"]}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Update Content with valid data for topic (topic=addition). Expected: 200
	 * - OK. Record Should be created with given data.
	 * 
	 */
	@Test
	public void testFrameworkLinking_21() throws Exception {
		String request = "{\"request\": {\"content\": {\"name\": \"Unit Test Content\",\"topic\":[\"addition\"],\"versionKey\":\""
				+ passKey + "\"}}}";
		String path = basePath + "/update/" + frContentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		path = basePath + "/read/" + frContentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		List<String> topic = (List<String>) ((Map<String, Object>) resp.getResult().get("content")).get("topic");
		assertEquals("addition", (String) topic.get(0));
	}

	/*
	 * Update Content with Invalid data for topic (topic=multiplication).
	 * Expected: 400 - OK. Record Should not be updated with given data.
	 * 
	 */
	@Test
	public void testFrameworkLinking_22() throws Exception {
		String request = "{\"request\": {\"content\": {\"name\": \"Unit Test Content\",\"versionKey\":\"" + passKey
				+ "\"\"topic\":[\"multiplication\"]}}}";
		String path = basePath + "/update/" + frContentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}


//	@Test
	public void retireDocumentContentWithDraftStatus() throws Exception {
		String createDocumentContentRequestBody = "{\"request\": {\"content\": {\"name\": \"Text Book 1\",\"code\": \"test.book.1\",\"mimeType\": \"application/pdf\",\"contentType\":\"Resource\"}}}";
		String contentId = createContent(createDocumentContentRequestBody);
		retireContent(contentId);
		validateRetiredNode(contentId);
	}

//	@Test
	public void retirePublishedDocumentContent() throws Exception {
		String createDocumentContentRequestBody = "{\"request\": {\"content\": {\"name\": \"Text Book 1\",\"code\": \"test.book.1\",\"mimeType\": \"application/pdf\",\"contentType\":\"Resource\"}}}";
		String contentId = createContent(createDocumentContentRequestBody);
		uploadContent(contentId, "test2.pdf","application/pdf");
		delay(5000);
		publish(contentId);
		waitTillPublish(contentId);
		update(contentId);
		retireContent(contentId);
		validateRetiredNode(contentId);
	}


//	@Test
	public void retireReviewedDocumentContent() throws Exception {
		String createDocumentContentRequestBody = "{\"request\": {\"content\": {\"name\": \"Text Book 1\",\"code\": \"test.book.1\",\"mimeType\": \"application/pdf\",\"contentType\":\"Resource\"}}}";
		String contentId = createContent(createDocumentContentRequestBody);
		uploadContent(contentId, "test2.pdf","application/pdf");
		delay(25000);
		review(contentId);
		retireContent(contentId);
		validateRetiredNode(contentId);
	}

//	@Test
	public void retireCollectionContentWithDraftStatus() throws Exception {
		String createCollectionReq = "{\"request\":{\"content\":{\"name\":\"Test-G-Dev-01\",\"code\":\"test.book.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBook\"}}}";
		String contentId = createContent(createCollectionReq);
		String updateHierarchyReq = "{\"request\":{\"data\":{\"nodesModified\":{\"TestBookUnit-01\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_01\",\"description\":\"Test_Collection_TextBookUnit_01\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\"}},\"TestBookUnit-01-01\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_01_01\",\"description\":\"TTest_Collection_TextBookUnit_01_01\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\"}},\"TestBookUnit-02\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_02\",\"description\":\"TTest_Collection_TextBookUnit_02\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\"}},\"TestBookUnit-02-01\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_02_01\",\"description\":\"TTest_Collection_TextBookUnit_02_01\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\"}}},\"hierarchy\":{\"tbId\":{\"name\":\"Test TextBook\",\"contentType\":\"Collection\",\"children\":[\"TestBookUnit-01\",\"TestBookUnit-02\"],\"root\":true},\"TestBookUnit-01\":{\"name\":\"Test_Collection_TextBookUnit_01\",\"contentType\":\"TextBookUnit\",\"children\":[\"TestBookUnit-01-01\"],\"root\":false},\"TestBookUnit-02\":{\"name\":\"Test_Collection_TextBookUnit_02\",\"contentType\":\"TextBookUnit\",\"children\":[\"TestBookUnit-02-01\"],\"root\":false}},\"lastUpdatedBy\":\"ecff2373-6c52-4956-b103-a9741eae16f0\"}}}".replace("tbId",contentId);
		updateHierarchy(updateHierarchyReq);
		retireContent(contentId);
		validateRetiredCollectionContent(contentId);
	}

//	@Test
	public void retireReviewedCollectionContent() throws Exception {
		String createCollectionReq = "{\"request\":{\"content\":{\"name\":\"Test-G-Dev-01\",\"code\":\"test.book.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBook\"}}}";
		String contentId = createContent(createCollectionReq);
		String updateHierarchyReq = "{\"request\":{\"data\":{\"nodesModified\":{\"TestBookUnit-01\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_01\",\"description\":\"Test_Collection_TextBookUnit_01\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\"}},\"TestBookUnit-01-01\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_01_01\",\"description\":\"TTest_Collection_TextBookUnit_01_01\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\"}},\"TestBookUnit-02\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_02\",\"description\":\"TTest_Collection_TextBookUnit_02\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\"}},\"TestBookUnit-02-01\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_02_01\",\"description\":\"TTest_Collection_TextBookUnit_02_01\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\"}}},\"hierarchy\":{\"tbId\":{\"name\":\"Test TextBook\",\"contentType\":\"Collection\",\"children\":[\"TestBookUnit-01\",\"TestBookUnit-02\"],\"root\":true},\"TestBookUnit-01\":{\"name\":\"Test_Collection_TextBookUnit_01\",\"contentType\":\"TextBookUnit\",\"children\":[\"TestBookUnit-01-01\"],\"root\":false},\"TestBookUnit-02\":{\"name\":\"Test_Collection_TextBookUnit_02\",\"contentType\":\"TextBookUnit\",\"children\":[\"TestBookUnit-02-01\"],\"root\":false}},\"lastUpdatedBy\":\"ecff2373-6c52-4956-b103-a9741eae16f0\"}}}".replace("tbId",contentId);
		updateHierarchy(updateHierarchyReq);
		review(contentId);
		retireContent(contentId);
		validateRetiredCollectionContent(contentId);
	}

//	@Test
	public void retirePublishedCollectionContentWithNoChildren() throws Exception {
		String createCollectionReq = "{\"request\":{\"content\":{\"name\":\"Test-G-Dev-01\",\"code\":\"test.book.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBook\"}}}";
		String contentId = createContent(createCollectionReq);
		publish(contentId);
		delay(3000);
		retireContent(contentId);
		validateRetiredCollectionContent(contentId);
	}

	@Ignore
	@Test
	public void retirePublishedCollectionContentWithChildren() throws Exception {
		String createCollectionReq = "{\"request\":{\"content\":{\"name\":\"Test-G-Dev-01\",\"code\":\"test.book.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBook\"}}}";
		String contentId = createContent(createCollectionReq);
		String updateHierarchyReq = "{\"request\":{\"data\":{\"nodesModified\":{\"TestBookUnit-01\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_01\",\"description\":\"Test_Collection_TextBookUnit_01\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\"}},\"TestBookUnit-01-01\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_01_01\",\"description\":\"TTest_Collection_TextBookUnit_01_01\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\"}},\"TestBookUnit-02\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_02\",\"description\":\"TTest_Collection_TextBookUnit_02\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\"}},\"TestBookUnit-02-01\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_02_01\",\"description\":\"TTest_Collection_TextBookUnit_02_01\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\"}}},\"hierarchy\":{\"tbId\":{\"name\":\"Test TextBook\",\"contentType\":\"Collection\",\"children\":[\"TestBookUnit-01\",\"TestBookUnit-02\"],\"root\":true},\"TestBookUnit-01\":{\"name\":\"Test_Collection_TextBookUnit_01\",\"contentType\":\"TextBookUnit\",\"children\":[\"TestBookUnit-01-01\"],\"root\":false},\"TestBookUnit-02\":{\"name\":\"Test_Collection_TextBookUnit_02\",\"contentType\":\"TextBookUnit\",\"children\":[\"TestBookUnit-02-01\"],\"root\":false}},\"lastUpdatedBy\":\"ecff2373-6c52-4956-b103-a9741eae16f0\"}}}".replace("tbId",contentId);
		updateHierarchy(updateHierarchyReq);
		publish(contentId);
		waitTillPublish(contentId);
		retireContent(contentId);
		validateRetiredCollectionContent(contentId);
	}

	//Test case for Online Ecar generation in Textbook
//	@Test
	public void publishTextbookExpectOnlineEcar() throws Exception {
		String textbookId = createAndPublishATextBook();
		Response response = getContent(textbookId);
		Map<String,Object> variants = (Map<String,Object>) ((Map<String, Object>)response.getResult().get("content")).get("variants");
		String onlineEcarUrl = (String) ((Map<String,Object>) variants.get("online")).get("ecarUrl");
		assertNotNull(onlineEcarUrl);
	}

	//Test case to validate Whether Textbook Id is mandatory field in update hierarchy or not
	@Test
	public void updateHierarchyWithoutRootNodeExpectClientError() throws Exception {
		String createTextbookReq = "{\"request\": {\"content\": {\"name\": \"Test Textbook\",\"code\": \"test.book.1\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBook\"}}}";
		String textbookId = createContent(createTextbookReq);

		String updateHierarchyRequestBody = "{\"request\": {\"data\": {\"nodesModified\": {\"TestBookUnit-01\": {\"isNew\": true,\"root\": false,\"metadata\": {\"mimeType\": \"application/vnd.ekstep.content-collection\",\"keywords\": [],\"name\": \"Test_Collection_TextBookUnit_01\",\"description\": \"Test_Collection_TextBookUnit_01\",\"contentType\": \"TextBookUnit\"}},\"TestBookUnit-02\": {\"isNew\": true,\"root\": false,\"metadata\": {\"mimeType\": \"application/vnd.ekstep.content-collection\",\"keywords\": [],\"name\": \"Test_Collection_TextBookUnit_02\",\"description\": \"TTest_Collection_TextBookUnit_02\",\"contentType\": \"TextBookUnit\",\"appIcon\":\"appIconUrl\"}}},\"hierarchy\": {\"textbookIdentifier\": {\"name\": \"Test Textbook\",\"contentType\": \"TextBook\",\"children\": [\"TestBookUnit-01\",\"TestBookUnit-02\"],\"root\": false},\"TestBookUnit-01\": {\"name\": \"Test_Collection_TextBookUnit_01\",\"contentType\": \"TextBookUnit\",\"root\": false,\"children\":[]},\"TestBookUnit-02\": {\"name\": \"Test_Collection_TextBookUnit_02\",\"contentType\": \"TextBookUnit\",\"root\": false,\"children\":[]}}}}}";
		updateHierarchyRequestBody = updateHierarchyRequestBody.replace("textbookIdentifier", textbookId);

		String path = basePath + "/hierarchy/update";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi").header("X-Channel-Id", "channelTest")
				.header("user-id", "ilimi").contentType(MediaType.APPLICATION_JSON).content(updateHierarchyRequestBody));
		Response response = getResponse(actions);

		assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
		assertEquals("failed", response.getParams().getStatus());
		assertEquals("ERR_INVALID_ROOT_ID", response.getParams().getErr());
		assertEquals("Please Provide Valid Root Node Identifier", response.getParams().getErrmsg());
	}

	//Test case to validate Whether Textbook Id is mandatory field in update hierarchy or not
	@Test
	public void updateHierarchyWithRootNodeInNodesModifiedExpect200() throws Exception {
		String createTextbookReq = "{\"request\": {\"content\": {\"name\": \"Test Textbook\",\"code\": \"test.book.1\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBook\"}}}";
		String textbookId = createContent(createTextbookReq);

		String updateHierarchyRequestBody = "{\"request\":{\"data\":{\"nodesModified\":{\"TestBookUnit-01\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_01\",\"description\":\"Test_Collection_TextBookUnit_01\",\"contentType\":\"TextBookUnit\"}},\"TestBookUnit-02\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_02\",\"description\":\"TTest_Collection_TextBookUnit_02\",\"contentType\":\"TextBookUnit\",\"appIcon\":\"appIconUrl\"}},\"textbookIdentifier\":{\"metadata\":{\"name\":\"Test Textbook\",\"contentType\":\"TextBook\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"isNew\":false,\"root\":true}},\"hierarchy\":{\"textbookIdentifier\":{\"name\":\"Test Textbook\",\"contentType\":\"TextBook\",\"children\":[\"TestBookUnit-01\",\"TestBookUnit-02\"]},\"TestBookUnit-01\":{\"name\":\"Test_Collection_TextBookUnit_01\",\"contentType\":\"TextBookUnit\",\"root\":false,\"children\":[]},\"TestBookUnit-02\":{\"name\":\"Test_Collection_TextBookUnit_02\",\"contentType\":\"TextBookUnit\",\"root\":false,\"children\":[]}}}}}";
		updateHierarchyRequestBody = updateHierarchyRequestBody.replace("textbookIdentifier", textbookId);

		String path = basePath + "/hierarchy/update";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi").header("X-Channel-Id", "channelTest")
				.header("user-id", "ilimi").contentType(MediaType.APPLICATION_JSON).content(updateHierarchyRequestBody));
		Response response = getResponse(actions);

		assertEquals("OK", response.getResponseCode().toString());
		assertEquals("successful", response.getParams().getStatus());
		Assert.assertNotNull(response.getResult());
	}

	//Test case to validate the file name while generating pre-signed url for content
	@Test
	public void testGeneratePreSignedUrlWithInvalidFileNameExpect400() throws Exception {
		String createVideoContentReq = "{\"request\": {\"content\": {\"name\": \"Test Video Resource Content\",\"code\": \"test.res.1\",\"mimeType\": \"video/mp4\",\"contentType\":\"Resource\"}}}";
		String contentId = createContent(createVideoContentReq);

		String preSignedReq = "{\"request\": {\"content\": {\"fileName\": \".mp4\"}}}";
		String path = basePath + "/upload/url/"+contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi").header("X-Channel-Id", "channelTest")
				.header("user-id", "ilimi").contentType(MediaType.APPLICATION_JSON).content(preSignedReq));
		Response response = getResponse(actions);

		assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
		assertEquals("failed", response.getParams().getStatus());
		assertEquals("Please Provide Valid File Name.", response.getParams().getErrmsg());
	}

	//Test case to validate the file name while generating pre-signed url for content
	@Test
	public void testGeneratePreSignedUrlWithInvalidFileNameHavingLongStringExpect400() throws Exception {
		String createVideoContentReq = "{\"request\": {\"content\": {\"name\": \"Test Video Resource Content\",\"code\": \"test.res.1\",\"mimeType\": \"video/mp4\",\"contentType\":\"Resource\"}}}";
		String contentId = createContent(createVideoContentReq);

		String preSignedReq = "{\"request\": {\"content\": {\"fileName\": \"tIdPRlKzpJlti4NXsNJItjqPJB4iHJOx9mEOQphThOoPc2x6BbBF9lRPKcWk7ORteqwytBwVoOLLrFYi3fMaoUsUOBEiaz4c89I6Y3OfGtFAKXAO7eFXVXNrRLlwFnDp11wHvSmtqbNTKlycU3CELbfAXvbojuXVdDBi4W0EnSF0cMzpVeiL0ISPCPTVMiFpLabIbKlyvOiEB1taJdeGTcgGqaJdGGp2WVpnZOV56qqtPOvSg5kwB5naZ2qoQ0I4X.mp4\"}}}";
		String path = basePath + "/upload/url/"+contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi").header("X-Channel-Id", "channelTest")
				.header("user-id", "ilimi").contentType(MediaType.APPLICATION_JSON).content(preSignedReq));
		Response response = getResponse(actions);

		assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
		assertEquals("failed", response.getParams().getStatus());
		assertEquals("Please Provide Valid File Name.", response.getParams().getErrmsg());
	}

	//Test case to validate the file name while generating pre-signed url for content
	@Test
	public void testGeneratePreSignedUrlWithInvalidFileNameHavingLongHindiStringExpect400() throws Exception {
		String createVideoContentReq = "{\"request\": {\"content\": {\"name\": \"Test Video Resource Content\",\"code\": \"test.res.1\",\"mimeType\": \"video/mp4\",\"contentType\":\"Resource\"}}}";
		String contentId = createContent(createVideoContentReq);

		String preSignedReq = "{\"request\": {\"content\": {\"fileName\": \"तकनीकी सामूहिक विवरण होने मानव बिन्दुओमे विकेन्द्रियकरण विभाग कुशलता सुविधा सोफ़तवेर पुर्व केवल बाधा बातसमय पुर्णता विश्वव्यापि विवरण सुनत सभीकुछ दस्तावेज मुख्य ब्रौशर तकरीबन कार्यकर्ता पढाए सीमित पढाए शीघ्र स्थिति है।अभी सभिसमज काम वर्तमान समस्याए मुख्य भाषा है।अभी वर्तमान प्रति आधुनिक गोपनीयता सार्वजनिक संसाध उनको उपेक्ष मुश्किले पसंद बनाना विकसित समस्याए नाकर देखने यायेका भोगोलिक विश्लेषण सुनत लेकिन प्राण विज्ञानि गुजरना पहोचने जिसकी कैसे.mp4\"}}}";
		String path = basePath + "/upload/url/"+contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi").header("X-Channel-Id", "channelTest")
				.header("user-id", "ilimi").contentType(MediaType.APPLICATION_JSON).content(preSignedReq));
		Response response = getResponse(actions);

		assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
		assertEquals("failed", response.getParams().getStatus());
		assertEquals("Please Provide Valid File Name.", response.getParams().getErrmsg());
	}

	//Test case to validate the file name while generating pre-signed url for content
	@Test
	public void testGeneratePreSignedUrlWithFileNameHavingSpaceOnlyExpect400() throws Exception {
		String createVideoContentReq = "{\"request\": {\"content\": {\"name\": \"Test Video Resource Content\",\"code\": \"test.res.1\",\"mimeType\": \"video/mp4\",\"contentType\":\"Resource\"}}}";
		String contentId = createContent(createVideoContentReq);

		String preSignedReq = "{\"request\": {\"content\": {\"fileName\": \" .mp4\"}}}";
		String path = basePath + "/upload/url/"+contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi").header("X-Channel-Id", "channelTest")
				.header("user-id", "ilimi").contentType(MediaType.APPLICATION_JSON).content(preSignedReq));
		Response response = getResponse(actions);

		assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
		assertEquals("failed", response.getParams().getStatus());
		assertEquals("Please Provide Valid File Name.", response.getParams().getErrmsg());
	}

	//Test case to validate the file name while generating pre-signed url for content
	@Test
	public void testGeneratePreSignedUrlWithValidFileNameExpect200() throws Exception {
		String createVideoContentReq = "{\"request\": {\"content\": {\"name\": \"Test Video Resource Content\",\"code\": \"test.res.1\",\"mimeType\": \"video/mp4\",\"contentType\":\"Resource\"}}}";
		String contentId = createContent(createVideoContentReq);

		String preSignedReq = "{\"request\": {\"content\": {\"fileName\": \"test.mp4\"}}}";
		String path = basePath + "/upload/url/"+contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi").header("X-Channel-Id", "channelTest")
				.header("user-id", "ilimi").contentType(MediaType.APPLICATION_JSON).content(preSignedReq));
		Response response = getResponse(actions);

		assertEquals("OK", response.getResponseCode().toString());
		assertEquals("successful", response.getParams().getStatus());
		Assert.assertNotNull(response.getResult());
	}

	//Test case to validate the file name while generating pre-signed url for content
	@Test
	public void testGeneratePreSignedUrlWithValidFileNameWithSpaceExpect200() throws Exception {
		String createVideoContentReq = "{\"request\": {\"content\": {\"name\": \"Test Video Resource Content\",\"code\": \"test.res.1\",\"mimeType\": \"video/mp4\",\"contentType\":\"Resource\"}}}";
		String contentId = createContent(createVideoContentReq);

		String preSignedReq = "{\"request\": {\"content\": {\"fileName\": \"test 123.mp4\"}}}";
		String path = basePath + "/upload/url/"+contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi").header("X-Channel-Id", "channelTest")
				.header("user-id", "ilimi").contentType(MediaType.APPLICATION_JSON).content(preSignedReq));
		Response response = getResponse(actions);

		assertEquals("OK", response.getResponseCode().toString());
		assertEquals("successful", response.getParams().getStatus());
		assertEquals(contentId, response.getResult().get("content_id"));
		Assert.assertNotNull(response.getResult().get("pre_signed_url"));
	}

	/**
	 * Test case to validate whether collection hierarchy is updated as part of system update
	 * for Live and Unlisted Collections
	 */
	@Test
	public void testSystemUpdateExpectHierarchyToUpdateForCollectionTypes() throws Exception {
		String textBookId = createAndPublishATextBook();
		String updatedTextBookName = "Updated Text Book Name For System Update Test";
		String systemUpdateReqForTB = "{\"request\": {\"content\": {\"name\": \"" + updatedTextBookName + "\"}}}";
		systemUpdate(textBookId, systemUpdateReqForTB);

		HierarchyStore hierarchyStore = new HierarchyStore();
		Map<String, Object> hierarchy = hierarchyStore.getHierarchy(textBookId);
		assertEquals(updatedTextBookName, hierarchy.get("name"));
	}

	/**
	 * Test case to validate - Status of a Content, which has a image node, cannot be updated using System Update Call.
	 * Only Image nodes status will be updated in this scenario
	 */
	@Test
	public void testSystemUpdateCannotUpdateStatusOfContentHavingImageNode() throws Exception {
		String contentId = createAndPublishAContent();
		update(contentId);

		String systemUpdateReqForContent = "{\"request\": {\"content\": {\"status\": \"flagged\"}}}";
		systemUpdate(contentId, systemUpdateReqForContent);

		Response contentResponse = getContent(contentId);
		Response contentImageResponse = getContent(contentId + ".img");

		assertTrue("live".equalsIgnoreCase((String) ((Map<String, Object>) contentResponse.getResult().get("content")).get("status")));
		assertTrue("flagged".equalsIgnoreCase((String) ((Map<String, Object>) contentImageResponse.getResult().get("content")).get("status")));
	}

    /**
     * Test case to validate whether both Content and ContentImage nodes are getting updated,
     * without changing the version key, when a published content having image node is updated.
     */
    @Test
    public void testSystemUpdateExpectContentAndImageNodesToUpdateOnContentUpdate() throws Exception {
        String contentId = createAndPublishAContent();
        update(contentId);

        String liveVersionKey = (String) ((Map<String, Object>) getContent(contentId).getResult().get("content")).get("versionKey");
        String draftVersionKey = (String) ((Map<String, Object>) getContent(contentId + ".img").getResult().get("content")).get("versionKey");

        String contentUpdateName = "Updated Name for Resource Content";
        String systemUpdateReqForContent = "{\"request\": {\"content\": {\"name\": \"" + contentUpdateName + "\"}}}";
        systemUpdate(contentId, systemUpdateReqForContent);

        Response contentResponse = getContent(contentId);
        Response contentImageResponse = getContent(contentId + ".img");

        assertEquals(contentUpdateName, (String) ((Map<String, Object>) contentResponse.getResult().get("content")).get("name"));
        assertEquals(contentUpdateName, (String) ((Map<String, Object>) contentImageResponse.getResult().get("content")).get("name"));
        assertEquals(liveVersionKey, (String) ((Map<String, Object>) contentResponse.getResult().get("content")).get("versionKey"));
        assertEquals(draftVersionKey, (String) ((Map<String, Object>) contentImageResponse.getResult().get("content")).get("versionKey"));
    }

    /**
     * Test case to validate whether only ContentImage node is getting updated without affecting Content node,
     * without changing the version key, When a image node of a content is updated
     */
    @Test
    public void testSystemUpdateExpectOnlyImageNodeToUpdateOnContentImageUpdate() throws Exception {
        String originalName = "Test Resource Content";
        String contentId = createAndPublishAContent(originalName);
        update(contentId);

        String liveVersionKey = (String) ((Map<String, Object>) getContent(contentId).getResult().get("content")).get("versionKey");
        String draftVersionKey = (String) ((Map<String, Object>) getContent(contentId + ".img").getResult().get("content")).get("versionKey");

        String contentImageUpdateName = "Updated Name for Resource Content - Image Node Only";
        String systemUpdateReqForContentImage = "{\"request\": {\"content\": {\"name\": \"" + contentImageUpdateName + "\"}}}";
        systemUpdate(contentId + ".img", systemUpdateReqForContentImage);

        Response contentResponse = getContent(contentId);
        Response contentImageResponse = getContent(contentId + ".img");

        assertEquals(originalName, (String) ((Map<String, Object>) contentResponse.getResult().get("content")).get("name"));
        assertEquals(contentImageUpdateName, (String) ((Map<String, Object>) contentImageResponse.getResult().get("content")).get("name"));
        assertEquals(liveVersionKey, (String) ((Map<String, Object>) contentResponse.getResult().get("content")).get("versionKey"));
        assertEquals(draftVersionKey, (String) ((Map<String, Object>) contentImageResponse.getResult().get("content")).get("versionKey"));
    }

	//Test case to validate whether lastStatusChangedOn property gets updated or not during System Update.
	@Test
	public void testSystemUpdateExpectNoChangeInLastStatusChangedOn() throws Exception {
		String createResourceContentReq = "{\"request\": {\"content\": {\"name\": \"Test Resource Content\",\"code\": \"test.res.1\",\"mimeType\": \"application/pdf\",\"contentType\":\"Resource\"}}}";
		String contentId = createContent(createResourceContentReq);
		Response getResponse = getContent(contentId);
		String lastStatusChangedOn = (String) ((Map<String, Object>)getResponse.getResult().get("content")).get("lastStatusChangedOn");

		String systemUpdateReq = "{\"request\": {\"content\": {\"name\": \"Updated Name for Resource Content\"}}}";
		systemUpdate(contentId, systemUpdateReq);
		Response getRespPostSysUpdate = getContent(contentId);
		String lastStatusChangedOnPostSysUpdate = (String) ((Map<String, Object>)getRespPostSysUpdate.getResult().get("content")).get("lastStatusChangedOn");

		assertEquals(lastStatusChangedOn,lastStatusChangedOnPostSysUpdate);
	}

	//Test case to validate whether lastStatusChangedOn property gets updated or not during Normal Update.
	@Test
	public void testNormalUpdateExpectNoChangeInLastStatusChangedOn() throws Exception {
		String createResourceContentReq = "{\"request\": {\"content\": {\"name\": \"Test Resource Content\",\"code\": \"test.res.1\",\"mimeType\": \"application/pdf\",\"contentType\":\"Resource\"}}}";
		String contentId = createContent(createResourceContentReq);
		Response getResponse = getContent(contentId);
		String lastStatusChangedOn = (String) ((Map<String, Object>)getResponse.getResult().get("content")).get("lastStatusChangedOn");
		update(contentId);

		Response getRespPostUpdate = getContent(contentId);
		String lastStatusChangedOnPostUpdate = (String) ((Map<String, Object>)getRespPostUpdate.getResult().get("content")).get("lastStatusChangedOn");
		assertEquals(lastStatusChangedOn,lastStatusChangedOnPostUpdate);
	}

	//Test case to validate whether lastStatusChangedOn property gets updated or not during status change.
	@Test
	public void testReviewContentExpectChangeInLastStatusChangedOn() throws Exception {
		String createResourceContentReq = "{\"request\": {\"content\": {\"name\": \"Test Resource Content\",\"code\": \"test.res.1\",\"mimeType\": \"application/pdf\",\"contentType\":\"Resource\"}}}";
		String contentId = createContent(createResourceContentReq);
		Response getResponse = getContent(contentId);
		String lastStatusChangedOn = (String) ((Map<String, Object>)getResponse.getResult().get("content")).get("lastStatusChangedOn");
		uploadContent(contentId, "test3.pdf","application/pdf");
		delay(25000);
		review(contentId);

		Response getResponsePostReview = getContent(contentId);
		String lastStatusChangedOnPostReview = (String) ((Map<String, Object>)getResponsePostReview.getResult().get("content")).get("lastStatusChangedOn");
		assertFalse(StringUtils.equalsIgnoreCase(lastStatusChangedOn,lastStatusChangedOnPostReview));

	}

	/**
	 * Test case to validate flag content
	 */
	@Test
	public void testFlagContentExpectFlagSuccess() throws Exception {
		String contentId = createAndPublishAContent();

		Response flagResponse = flag(contentId);
		assertTrue(ResponseCode.OK.code() == flagResponse.getResponseCode().code());

		Response readResponse = getContent(contentId);
		assertTrue("flagged".equalsIgnoreCase((String) ((Map<String, Object>) readResponse.getResult().get("content")).get("status")));
	}

	/**
	 * Test case to validate failure scenarios of flag content
	 */
	@Test
	public void testFlagContentFailureScenarios() throws Exception {
		String createResourceContentReq = "{\"request\": {\"content\": {\"name\": \"Test Resource Content\",\"code\": \"test.res.1\",\"mimeType\": \"application/pdf\",\"contentType\":\"Resource\"}}}";
		String contentId = createContent(createResourceContentReq);

		Response flagResponseContentNotFound = flag(contentId + "xxx", "versionKey");
		Response flagResponseContentNotPublished = flag(contentId);

		assertTrue(ResponseCode.RESOURCE_NOT_FOUND.code() == flagResponseContentNotFound.getResponseCode().code());
		assertTrue(ResponseCode.CLIENT_ERROR.code() == flagResponseContentNotPublished.getResponseCode().code());
		assertTrue("ERR_CONTENT_NOT_FLAGGABLE".equalsIgnoreCase(flagResponseContentNotPublished.getParams().getErr()));
	}

	/**
	 * Test case to validate reject flag content
	 */
	@Test
	public void testRejectFlagContentExpectRejectFlagSuccess() throws Exception {
		String contentId = createAndPublishAContent();

		Response flagResponse = flag(contentId);
		assertTrue(ResponseCode.OK.code() == flagResponse.getResponseCode().code());

		Response readResponse = getContent(contentId);
		assertTrue("flagged".equalsIgnoreCase((String) ((Map<String, Object>) readResponse.getResult().get("content")).get("status")));

		Response rejectFlagResponse = rejectFlag(contentId);
		assertTrue(ResponseCode.OK.code() == rejectFlagResponse.getResponseCode().code());

		readResponse = getContent(contentId);
		assertTrue("live".equalsIgnoreCase((String) ((Map<String, Object>) readResponse.getResult().get("content")).get("status")));
	}

	/**
	 * Test case to validate reject flag content failure scenarios
	 */
	@Test
	public void testRejectFlagContentFailureScenarios() throws Exception {
		String contentId = createAndPublishAContent();

		Response rejectFlagResponseContentNotFound = rejectFlag(contentId + "xxx");
		Response rejectFlagResponseContentNotFlagged = rejectFlag(contentId);

		assertTrue(ResponseCode.RESOURCE_NOT_FOUND.code() == rejectFlagResponseContentNotFound.getResponseCode().code());
		assertTrue(ResponseCode.CLIENT_ERROR.code() == rejectFlagResponseContentNotFlagged.getResponseCode().code());
		assertTrue("ERR_CONTENT_NOT_FLAGGED".equalsIgnoreCase(rejectFlagResponseContentNotFlagged.getParams().getErr()));
	}
	@Test
	public void rejectReviewedDocumentContent() throws Exception {
		String createDocumentContentRequestBody = "{\"request\": {\"content\": {\"name\": \"Text Book 1\",\"code\": \"test.book.1\",\"mimeType\": \"application/pdf\",\"contentType\":\"Resource\"}}}";
		String contentId = createContent(createDocumentContentRequestBody);
		uploadContent(contentId, "test2.pdf","application/pdf");
		review(contentId);
		Response response = rejectValidContent(contentId,
				"{\n" +
						"\"request\": {\n" +
						"\t\"content\": {\n" +
						"\t\t\"rejectReasons\":[\"VALID REJECT REASON\"],\n" +
						"\t\t\"rejectComment\": \"Valid Reject Comment\"\n" +
						"   }\n" +
						" }\n" +
						"}");
		validateRejectedNode(contentId, true);
	}

	@Test
	public void rejectReviewedDocumentContentInvalidReasonFormat() throws Exception {
		String createDocumentContentRequestBody = "{\"request\": {\"content\": {\"name\": \"Text Book 1\",\"code\": \"test.book.1\",\"mimeType\": \"application/pdf\",\"contentType\":\"Resource\"}}}";
		String contentId = createContent(createDocumentContentRequestBody);
		uploadContent(contentId, "test2.pdf","application/pdf");
		review(contentId);
		Response response = rejectInvalidContent(contentId,
				"{\n" +
						"\"request\": {\n" +
						"\t\"content\": {\n" +
						"\t\t\"rejectReasons\":\"VALID REJECT REASON\",\n" +
						"\t\t\"rejectComment\": \"Valid Reject Comment\"\n" +
						"   }\n" +
						" }\n" +
						"}");
	}

	@Test
	public void rejectDraftDocumentContent() throws Exception {
		String createDocumentContentRequestBody = "{\"request\": {\"content\": {\"name\": \"Text Book 1\",\"code\": \"test.book.1\",\"mimeType\": \"application/pdf\",\"contentType\":\"Resource\"}}}";
		String contentId = createContent(createDocumentContentRequestBody);
		uploadContent(contentId, "test2.pdf","application/pdf");
		Response response = rejectInvalidContent(contentId,
				"{\n" +
						"\"request\": {\n" +
						"\t\"content\": {\n" +
						"   }\n" +
						" }\n" +
						"}");
		System.out.println(response.getParams().getErrmsg());
	}

	@Test
	public void rejectValidTextbookExpect200() throws Exception {
		//create an asset
		String createAssetReq = "{\"request\": {\"content\": {\"name\": \"Test Asset for Unit Testing\",\"code\": \"test.asset.1\",\"mimeType\": \"image/jpg\",\"mediaType\":\"image\",\"contentType\":\"Asset\"}}}";
		String assetId = createContent(createAssetReq);
		String assetUrl = uploadContent(assetId,"education.jpeg","image/jpeg");

		String createTextbookReq = "{\"request\": {\"content\": {\"name\": \"Test Textbook\",\"code\": \"test.book.1\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBook\",\"appIcon\":\""+assetUrl+"\"}}}";
		String textbookId = createContent(createTextbookReq);

		String updateHierarchyRequestBody = "{\"request\": {\"data\": {\"nodesModified\": {\"TestBookUnit-01\": {\"isNew\": true,\"root\": false,\"metadata\": {\"mimeType\": \"application/vnd.ekstep.content-collection\",\"keywords\": [],\"name\": \"Test_Collection_TextBookUnit_01\",\"description\": \"Test_Collection_TextBookUnit_01\",\"contentType\": \"TextBookUnit\",\"appIcon\":\"appIconUrl\"}},\"TestBookUnit-02\": {\"isNew\": true,\"root\": false,\"metadata\": {\"mimeType\": \"application/vnd.ekstep.content-collection\",\"keywords\": [],\"name\": \"Test_Collection_TextBookUnit_02\",\"description\": \"TTest_Collection_TextBookUnit_02\",\"contentType\": \"TextBookUnit\",\"appIcon\":\"appIconUrl\"}}},\"hierarchy\": {\"textbookIdentifier\": {\"name\": \"Test Textbook\",\"contentType\": \"TextBook\",\"children\": [\"TestBookUnit-01\",\"TestBookUnit-02\"],\"root\": true},\"TestBookUnit-01\": {\"name\": \"Test_Collection_TextBookUnit_01\",\"contentType\": \"TextBookUnit\",\"root\": false,\"children\":[]},\"TestBookUnit-02\": {\"name\": \"Test_Collection_TextBookUnit_02\",\"contentType\": \"TextBookUnit\",\"root\": false,\"children\":[]}}}}\n" +
				"}";
		updateHierarchyRequestBody = updateHierarchyRequestBody.replace("appIconUrl",assetUrl);
		updateHierarchyRequestBody = updateHierarchyRequestBody.replace("textbookIdentifier",textbookId);
		updateHierarchy(updateHierarchyRequestBody);
		review(textbookId);
		rejectValidContent(textbookId,
				"{\n" +
						"\"request\": {\n" +
						"\t\"content\": {\n" +
						"   }\n" +
						" }\n" +
						"}");
		validateRejectedNode(contentId, false);
	}
	@Test
	public void rejectInvalidTextbookExpect200() throws Exception {
		//create an asset
		String createAssetReq = "{\"request\": {\"content\": {\"name\": \"Test Asset for Unit Testing\",\"code\": \"test.asset.1\",\"mimeType\": \"image/jpg\",\"mediaType\":\"image\",\"contentType\":\"Asset\"}}}";
		String assetId = createContent(createAssetReq);
		String assetUrl = uploadContent(assetId,"education.jpeg","image/jpeg");

		String createTextbookReq = "{\"request\": {\"content\": {\"name\": \"Test Textbook\",\"code\": \"test.book.1\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBook\",\"appIcon\":\""+assetUrl+"\"}}}";
		String textbookId = createContent(createTextbookReq);

		String updateHierarchyRequestBody = "{\"request\": {\"data\": {\"nodesModified\": {\"TestBookUnit-01\": {\"isNew\": true,\"root\": false,\"metadata\": {\"mimeType\": \"application/vnd.ekstep.content-collection\",\"keywords\": [],\"name\": \"Test_Collection_TextBookUnit_01\",\"description\": \"Test_Collection_TextBookUnit_01\",\"contentType\": \"TextBookUnit\",\"appIcon\":\"appIconUrl\"}},\"TestBookUnit-02\": {\"isNew\": true,\"root\": false,\"metadata\": {\"mimeType\": \"application/vnd.ekstep.content-collection\",\"keywords\": [],\"name\": \"Test_Collection_TextBookUnit_02\",\"description\": \"TTest_Collection_TextBookUnit_02\",\"contentType\": \"TextBookUnit\",\"appIcon\":\"appIconUrl\"}}},\"hierarchy\": {\"textbookIdentifier\": {\"name\": \"Test Textbook\",\"contentType\": \"TextBook\",\"children\": [\"TestBookUnit-01\",\"TestBookUnit-02\"],\"root\": true},\"TestBookUnit-01\": {\"name\": \"Test_Collection_TextBookUnit_01\",\"contentType\": \"TextBookUnit\",\"root\": false,\"children\":[]},\"TestBookUnit-02\": {\"name\": \"Test_Collection_TextBookUnit_02\",\"contentType\": \"TextBookUnit\",\"root\": false,\"children\":[]}}}}\n" +
				"}";
		updateHierarchyRequestBody = updateHierarchyRequestBody.replace("appIconUrl",assetUrl);
		updateHierarchyRequestBody = updateHierarchyRequestBody.replace("textbookIdentifier",textbookId);
		updateHierarchy(updateHierarchyRequestBody);
		rejectInvalidContent(textbookId,
				"{\n" +
						"\"request\": {\n" +
						"\t\"content\": {\n" +
						"   }\n" +
						" }\n" +
						"}");
	}


	private String createContent(String requestBody) throws Exception {
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi").header("X-Channel-Id", "channelTest")
				.header("user-id", "ilimi").contentType(MediaType.APPLICATION_JSON).content(requestBody));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		return (String) getResponse(actions).getResult().get(TestParams.node_id.name());
	}

	private void systemUpdate(String contentId, String requestBody) throws Exception {
		String path = "/system/v3/content/update/"+contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi").header("X-Channel-Id", "channelTest")
				.header("user-id", "ilimi").contentType(MediaType.APPLICATION_JSON).content(requestBody));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	private void retireContent(String contentId) throws Exception {
		String path = basePath + "/retire/" + contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.delete(path));
		System.out.println("retireContentResponse:"+actions.andReturn().getResponse().getContentAsString());
		assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	private void validateRetiredNode(String contentId) throws Exception {
		Response response = getContent(contentId);
		assertEquals("Retired", ((Map<String, Object>) response.get("content")).get("status"));
	}

	private String uploadContent(String contentId, String fileName, String contentType) throws Exception {
		FileInputStream fileStream = null;
		try {
			String uploadPath = basePath + "/upload/" + contentId;
			File inputFile = getResourceFile(fileName);
			fileStream = new FileInputStream(inputFile);
			MockMultipartFile testFile = new MockMultipartFile("file", fileName, contentType, fileStream);
			actions = mockMvc.perform(MockMvcRequestBuilders.fileUpload(uploadPath).file(testFile).header("user-id", "ilimi"));
			assertEquals(200, actions.andReturn().getResponse().getStatus());
			return (String) getResponse(actions).getResult().get(TestParams.content_url.name());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != fileStream)
				fileStream.close();
		}
		return null;
	}

	private void publish(String contentId) throws Exception {
		String path = basePath + "/publish/" + contentId;
		String publishReqBody = "{\"request\": {\"content\" : {\"lastPublishedBy\" : \"Ekstep\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(publishReqBody));
		System.out.print("Response::::" + actions.andReturn().getResponse().getContentAsString());
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	private Response getContent(String contentId) throws Exception {
		String path = basePath + "/read/" + contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelKA"));
		return getResponse(actions);
	}

	private void update(String contentId) throws Exception {
		String versionKey = (String) ((Map<String, Object>)getContent(contentId).getResult().get("content")).get("versionKey");
		String updateReq = "{\"request\": {\"content\": {\"versionKey\": \"" + versionKey + "\",\"screenshots\": null,\"name\": \"Image Node\"}}}";
		String path = basePath + "/update/" + contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("user-id", "ilimi").content(updateReq));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	private void review(String contentId) throws Exception {
		String path = basePath + "/review/" + contentId;
		String publishReqBody = "{\"request\": {\"content\" : {}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(publishReqBody));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	private void updateHierarchy(String requestBody) throws Exception {
		String path = basePath + "/hierarchy/update";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi").header("X-Channel-Id", "channelTest")
				.header("user-id", "ilimi").contentType(MediaType.APPLICATION_JSON).content(requestBody));
		System.out.println("Update Hierarchy : "+actions.andReturn().getResponse().getContentAsString());
		assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	private Response flag(String contentId) throws Exception {
		String versionKey = (String) ((Map<String, Object>)getContent(contentId).getResult().get("content")).get("versionKey");
		return flag(contentId, versionKey);
	}

	private Response flag(String contentId, String versionKey) throws Exception {
		String path = basePath + "/flag/" + contentId;
		String flagReqBody = "{ \"request\": { \"flagReasons\": [ \"Copyright Violation\" ], \"flaggedBy\": \"user\", \"versionKey\": \"" + versionKey + "\" } }";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("user-id", "ilimi").content(flagReqBody));
		return getResponse(actions);
	}

	private Response rejectFlag(String contentId) throws Exception {
		String path = basePath + "/flag/reject/" + contentId;
		String rejectFlagReqBody = "{ \"request\": { } }";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("user-id", "ilimi").content(rejectFlagReqBody));
		return getResponse(actions);
	}

	private Response acceptFlag(String contentId) throws Exception {
		String versionKey = (String) ((Map<String, Object>)getContent(contentId).getResult().get("content")).get("versionKey");
		return acceptFlag(contentId, versionKey);
	}

	private Response acceptFlag(String contentId, String versionKey) throws Exception {
		String path = basePath + "/flag/accept/" + contentId;
		String acceptFlagReqBody = "{ \"request\": { \"versionKey\":\"" + versionKey + "\" } }";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("user-id", "ilimi").content(acceptFlagReqBody));
		return getResponse(actions);
	}

	private void validateRetiredCollectionContent(String contentId) throws Exception {
		validateRetiredNode(contentId);
		//TODO: Validate Cassandra Record if retired from a live version.
	}

	private String createAndPublishATextBook() throws Exception {
		//create an asset
		String createAssetReq = "{\"request\": {\"content\": {\"name\": \"Test Asset for Unit Testing\",\"code\": \"test.asset.1\",\"mimeType\": \"image/jpg\",\"mediaType\":\"image\",\"contentType\":\"Asset\"}}}";
		String assetId = createContent(createAssetReq);
		String assetUrl = uploadContent(assetId,"education.jpeg","image/jpeg");

		String createTextbookReq = "{\"request\": {\"content\": {\"name\": \"Test Textbook\",\"code\": \"test.book.1\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBook\",\"appIcon\":\""+assetUrl+"\"}}}";
		String textbookId = createContent(createTextbookReq);

		String updateHierarchyRequestBody = "{\"request\": {\"data\": {\"nodesModified\": {\"TestBookUnit-01\": {\"isNew\": true,\"root\": false,\"metadata\": {\"mimeType\": \"application/vnd.ekstep.content-collection\",\"keywords\": [],\"name\": \"Test_Collection_TextBookUnit_01\",\"description\": \"Test_Collection_TextBookUnit_01\",\"contentType\": \"TextBookUnit\",\"appIcon\":\"appIconUrl\"}},\"TestBookUnit-02\": {\"isNew\": true,\"root\": false,\"metadata\": {\"mimeType\": \"application/vnd.ekstep.content-collection\",\"keywords\": [],\"name\": \"Test_Collection_TextBookUnit_02\",\"description\": \"TTest_Collection_TextBookUnit_02\",\"contentType\": \"TextBookUnit\",\"appIcon\":\"appIconUrl\"}}},\"hierarchy\": {\"textbookIdentifier\": {\"name\": \"Test Textbook\",\"contentType\": \"TextBook\",\"children\": [\"TestBookUnit-01\",\"TestBookUnit-02\"],\"root\": true},\"TestBookUnit-01\": {\"name\": \"Test_Collection_TextBookUnit_01\",\"contentType\": \"TextBookUnit\",\"root\": false,\"children\":[]},\"TestBookUnit-02\": {\"name\": \"Test_Collection_TextBookUnit_02\",\"contentType\": \"TextBookUnit\",\"root\": false,\"children\":[]}}}}\n" +
				"}";
		updateHierarchyRequestBody = updateHierarchyRequestBody.replace("appIconUrl",assetUrl);
		updateHierarchyRequestBody = updateHierarchyRequestBody.replace("textbookIdentifier",textbookId);
		updateHierarchy(updateHierarchyRequestBody);
		publish(textbookId);
		waitTillPublish(textbookId);

		return textbookId;
	}

	private String createAndPublishAContent() throws Exception {
		return createAndPublishAContent("Test Resource Content");
	}

	private String createAndPublishAContent(String contentName) throws Exception {
		String createResourceContentReq = "{\"request\": {\"content\": {\"name\": \"" + contentName + "\",\"code\": \"test.res.1\",\"mimeType\": \"application/pdf\",\"contentType\":\"Resource\"}}}";
		String contentId = createContent(createResourceContentReq);
		uploadContent(contentId, "test2.pdf", "application/pdf");
		publish(contentId);
		waitTillPublish(contentId);
		return contentId;
	}

	private void waitTillPublish(String contentId) throws Exception {
		String status = "";
		Integer counter = 0;
		while(!"Live".equalsIgnoreCase(status) && counter<10){
			Response resp = getContent(contentId);
			if("OK".equalsIgnoreCase(resp.getResponseCode().toString())){
				status = (String)((Map<String,Object>)resp.getResult().get("content")).get("status");
				++counter;
				delay(20000);
			}
		}
	}

	private Response rejectValidContent(String contentId, String requestBody) throws Exception {
		String path = basePath + "/reject/" + contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi").header("X-Channel-Id", "channelTest")
				.header("user-id", "ilimi").contentType(MediaType.APPLICATION_JSON).content(requestBody));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		return  getResponse(actions);
	}

	private Response rejectInvalidContent(String contentId, String requestBody) throws Exception {
		String path = basePath + "/reject/" + contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi").header("X-Channel-Id", "channelTest")
				.header("user-id", "ilimi").contentType(MediaType.APPLICATION_JSON).content(requestBody));
		assertEquals(400, actions.andReturn().getResponse().getStatus());
		return  getResponse(actions);
	}
	private void validateRejectedNode(String contentId, Boolean rejectReasons) throws Exception {
		Response response = getContent(contentId);
		assertEquals("Draft", ((Map<String, Object>) response.get("content")).get("status"));
		if(rejectReasons) {
			assertNotNull(((Map<String, Object>) response.get("content")).get("rejectReasons"));
			assertNotNull(((Map<String, Object>) response.get("content")).get("rejectComment"));
		}
	}
}