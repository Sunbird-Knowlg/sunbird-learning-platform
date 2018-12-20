package org.ekstep.taxonomy.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.graph.cache.util.RedisStoreUtil;
import org.ekstep.graph.engine.common.TestParams;
import org.ekstep.graph.model.cache.CategoryCache;
import org.ekstep.learning.contentstore.CollectionStore;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.taxonomy.mgr.impl.ContentManagerImpl;
import org.ekstep.test.common.CommonTestSetup;
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
	private static final String SCRIPT_2 = "CREATE TABLE IF NOT EXISTS content_store_test.content_data_test (content_id text, last_updated_on timestamp,body blob,oldBody blob,screenshots blob,stageIcons blob,PRIMARY KEY (content_id));";
	private static final String SCRIPT_3 = "CREATE KEYSPACE IF NOT EXISTS hierarhcy_store_test WITH replication = {'class': 'SimpleStrategy','replication_factor': '1'};";
	private static final String SCRIPT_4 = "CREATE TABLE IF NOT EXISTS hierarhcy_store_test.content_hierarhcy_test (identifier text, hierarchy text, PRIMARY KEY (identifier));";

	private static String contentId = "";
	private static String contentId2 = "";
	private static String versionKey = "";
	private static String versionKey2 = "";
	private static String frContentId = "";
	private static String passKey = Platform.config.getString("graph.passport.key.base");

	private static String DIALCODE_INDEX = "testdialcode";
	private static String DIALCODE_INDEX_TYPE = "dc";

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
			String createDocumentContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Resource\",\"code\":\"test content\",\"mimeType\":\"application/pdf\"}";
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

	@Test
	public void testContentV3Controller_12() throws Exception {
		String path = basePath + "/hierarchy/" + collectionContent3Id;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void testGetHeirarchyWithFailedResp() throws Exception {
		String path = basePath + "/hierarchy/do_11257769111443865611";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void testGetHeirarchyWithSuccessResp() throws Exception {
		CollectionStore collectionStore = new CollectionStore();
		String hierarchyData = "{\"code\":\"org.ekstep.jul18.story.test01\",\"keywords\":[\"QA_Content\"],\"channel\":\"in.ekstep\",\"downloadUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_11257769111443865611/course-testcourse_1535362933121_do_11257769111443865611_1.0.ecar\",\"description\":\"Text Book Test\",\"language\":[\"English\"],\"mimeType\":\"application/vnd.ekstep.content-collection\",\"variants\":{\"spine\":{\"ecarUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_11257769111443865611/course-testcourse_1535362933312_do_11257769111443865611_1.0_spine.ecar\",\"size\":1097.0}},\"idealScreenSize\":\"normal\",\"createdOn\":\"2018-08-27T15:08:04.840+0530\",\"children\":[{\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3acb\",\"downloadUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_11257769294298316813/testbook_collection1_1535362926857_do_11257769294298316813_1.0.ecar\",\"language\":[\"English\"],\"variants\":{\"spine\":{\"ecarUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_11257769294298316813/testbook_collection1_1535362931580_do_11257769294298316813_1.0_spine.ecar\",\"size\":589.0}},\"mimeType\":\"application/vnd.ekstep.content-collection\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2018-08-27T15:11:48.079+0530\",\"children\":[],\"usesContent\":[],\"contentDisposition\":\"inline\",\"contentEncoding\":\"gzip\",\"lastUpdatedOn\":\"2018-08-27T15:11:48.080+0530\",\"sYS_INTERNAL_LAST_UPDATED_ON\":\"2018-08-27T15:12:12.560+0530\",\"contentType\":\"CourseUnit\",\"identifier\":\"do_11257769294298316813\",\"audience\":[\"Learner\"],\"os\":[\"All\"],\"visibility\":\"Parent\",\"index\":1,\"mediaType\":\"content\",\"osId\":\"org.ekstep.launcher\",\"pkgVersion\":1.0,\"versionKey\":\"1535362908079\",\"idealScreenDensity\":\"hdpi\",\"s3Key\":\"ecar_files/do_11257769294298316813/testbook_collection1_1535362926857_do_11257769294298316813_1.0.ecar\",\"lastPublishedOn\":\"2018-08-27T15:12:06.771+0530\",\"size\":589.0,\"concepts\":[],\"compatibilityLevel\":4,\"name\":\"TestBook_Collection1\",\"status\":\"Live\"}],\"contentDisposition\":\"inline\",\"contentEncoding\":\"gzip\",\"lastUpdatedOn\":\"2018-08-27T15:08:04.840+0530\",\"sYS_INTERNAL_LAST_UPDATED_ON\":\"2018-08-27T15:12:13.668+0530\",\"contentType\":\"Course\",\"identifier\":\"do_11257769111443865611\",\"audience\":[\"Learner\"],\"os\":[\"All\"],\"visibility\":\"Default\",\"mediaType\":\"content\",\"osId\":\"org.ekstep.quiz.app\",\"pkgVersion\":1.0,\"versionKey\":\"1535362684840\",\"tags\":[\"QA_Content\"],\"idealScreenDensity\":\"hdpi\",\"s3Key\":\"ecar_files/do_11257769111443865611/course-testcourse_1535362933121_do_11257769111443865611_1.0.ecar\",\"framework\":\"NCF\",\"lastPublishedOn\":\"2018-08-27T15:12:13.076+0530\",\"size\":1098.0,\"compatibilityLevel\":4,\"name\":\"Course TestCourse\",\"status\":\"Live\"}";
		collectionStore.updateContentHierarchy("do_11257769111443865611", mapper.readValue(hierarchyData, Map.class));
		RedisStoreUtil.saveNodeProperty("domain", "do_11257769111443865611", "status", "Live");
		delay(15000);
		String path = basePath + "/hierarchy/do_11257769111443865611";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Given: Link Single Dial Code to Single Valid Content. (Data Given as
	 * List) When: Link Dial Code API Hits Then: 200 - OK.
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Test
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
		String request = "{\"request\": {\"content\": {\"identifier\":\"LP_UTEST_03\",\"name\": \"Unit Test Content\",\"framework\":\"NCFTEST\",\"medium\":\"english\",\"code\": \"unit.test\",\"contentType\": \"Resource\",\"mimeType\": \"application/pdf\",\"tags\": [\"colors\", \"games\"]}}}";
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
		String request = "{\"request\": {\"content\": {\"name\": \"Unit Test Content\",\"medium\":\"english\",\"versionKey\":\""
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
		String request = "{\"request\": {\"content\": {\"identifier\":\"LP_UTEST_07\",\"name\": \"Unit Test Content\",\"framework\":\"NCFTEST\",\"subject\":\"math\",\"code\": \"unit.test\",\"contentType\": \"Resource\",\"mimeType\": \"application/pdf\",\"tags\": [\"colors\", \"games\"]}}}";
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
		String request = "{\"request\": {\"content\": {\"name\": \"Unit Test Content\",\"subject\":\"math\",\"versionKey\":\""
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

	/*
	 * Create Content without contentType. Expected : 400 - CLIENT_ERROR
	 * 
	 */

	@Test
	public void createContentWithoutContentType() throws Exception {
		String createContentReq = "{\"request\": {\"content\": {\"name\": \"Unit Test\",\"code\": \"unit.test\",\"mimeType\": \"application/pdf\"}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(createContentReq));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		Assert.assertEquals("ERR_GRAPH_ADD_NODE_VALIDATION_FAILED", resp.getParams().getErr());
		Assert.assertEquals("CLIENT_ERROR", resp.getResponseCode().toString());
	}

	/*
	 * Create Content with wrong contentType (e.g: pdf). Expected : 400 -
	 * CLIENT_ERROR
	 * 
	 */

	@Test
	public void createContentWithInvalidContentType() throws Exception {
		String createContentReq = "{\"request\": {\"content\": {\"name\": \"Unit Test\",\"code\": \"unit.test\",\"mimeType\": \"application/pdf\",\"contentType\":\"pdf\"}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(createContentReq));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		Assert.assertEquals("ERR_GRAPH_ADD_NODE_VALIDATION_FAILED", resp.getParams().getErr());
		Assert.assertEquals("CLIENT_ERROR", resp.getResponseCode().toString());
	}

	/*
	 * Create Content without Name. Expected : 400 - CLIENT_ERROR
	 * 
	 */

	@Test
	public void createContentWithoutName() throws Exception {
		String createContentReq = "{\"request\": {\"content\": {\"code\": \"unit.test\",\"mimeType\": \"application/pdf\",\"contentType\":\"Resource\"}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(createContentReq));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		Assert.assertEquals("ERR_GRAPH_ADD_NODE_VALIDATION_FAILED", resp.getParams().getErr());
		Assert.assertEquals("CLIENT_ERROR", resp.getResponseCode().toString());
	}

	/*
	 * Create Content without Code. Expected : 400 - CLIENT_ERROR
	 * 
	 */

	@Test
	public void createContentWithoutCode() throws Exception {
		String createContentReq = "{\"request\": {\"content\": {\"name\": \"Unit Test\",\"mimeType\": \"application/pdf\",\"contentType\":\"Resource\"}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(createContentReq));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		Assert.assertEquals("ERR_GRAPH_ADD_NODE_VALIDATION_FAILED", resp.getParams().getErr());
		Assert.assertEquals("CLIENT_ERROR", resp.getResponseCode().toString());
	}

	/*
	 * Create Content without MimeType. Expected : 400 - CLIENT_ERROR
	 * 
	 */

	@Test
	public void createContentWithoutMimeType() throws Exception {
		String createContentReq = "{\"request\": {\"content\": {\"name\": \"Unit Test\",\"code\": \"unit.test\",\"contentType\":\"Resource\"}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(createContentReq));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		Assert.assertEquals("ERR_CONTENT_INVALID_CONTENT_MIMETYPE_TYPE", resp.getParams().getErr());
		Assert.assertEquals("CLIENT_ERROR", resp.getResponseCode().toString());
	}

	/*
	 * Create Content with Invalid MimeType. Expected : 400 - CLIENT_ERROR
	 * 
	 */

	@Test
	public void createContentWithInvalidMimeType() throws Exception {
		String createContentReq = "{\"request\": {\"content\": {\"name\": \"Unit Test\",\"code\": \"unit.test\",\"mimeType\": \"pdf\",\"contentType\":\"Resource\"}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(createContentReq));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		Assert.assertEquals("ERR_GRAPH_ADD_NODE_VALIDATION_FAILED", resp.getParams().getErr());
		Assert.assertEquals("CLIENT_ERROR", resp.getResponseCode().toString());
	}
	
	/*
	 * Create Content without passing ownershipType Expected : 200
	 * Content will be created with default ownershipType, i.e. 'createdBy'
	 */

	@Test
	public void createContentWithoutOwnershipType() throws Exception{
		String createContentReq = "{\"request\":{\"content\":{\"name\":\"ResourceContent\",\"code\":\"ResourceContent\",\"contentType\":\"Resource\",\"mimeType\":\"application/pdf\"}}}";
		String createPath = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(createPath).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(createContentReq));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response createResponse = getResponse(actions);
		String contentId = (String)createResponse.getResult().get("node_id");
		
		String readPath = basePath + "/read/" + contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(readPath).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response readResponse = getResponse(actions);
		List<String> ownershipType = (List<String>)((Map)readResponse.getResult().get("content")).get("ownershipType");
		String[] expected = {"createdBy"};
		Assert.assertArrayEquals(expected, ownershipType.toArray(new String[ownershipType.size()]));
	}
	
	/*
	 * Create Content with passing valid ownershipType Expected : 200
	 * Content will be created with valid ownershipType
	 */

	@Test
	public void createContentWithValidOwnershipType() throws Exception{
		String createContentReq = "{\"request\":{\"content\":{\"name\":\"ResourceContent\",\"code\":\"ResourceContent\",\"contentType\":\"Resource\",\"mimeType\":\"application/pdf\",\"ownershipType\":[\"createdFor\"]}}}";
		String createPath = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(createPath).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(createContentReq));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response createResponse = getResponse(actions);
		String contentId = (String)createResponse.getResult().get("node_id");
		
		String readPath = basePath + "/read/" + contentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(readPath).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response readResponse = getResponse(actions);
		List<String> ownershipType = (List<String>)((Map)readResponse.getResult().get("content")).get("ownershipType");
		String[] expected = {"createdFor"};
		Assert.assertArrayEquals(expected, ownershipType.toArray(new String[ownershipType.size()]));
	}
	
	/*
	 * Create Content with passing invalid ownershipType Expected : 400
	 * Content will not be created with invalid ownershipType
	 */

	@Test
	public void createContentWithInvalidOwnershipType() throws Exception{
		String createContentReq = "{\"request\":{\"content\":{\"name\":\"ResourceContent\",\"code\":\"ResourceContent\",\"contentType\":\"Resource\",\"mimeType\":\"application/pdf\",\"ownershipType\":[\"created\"]}}}";
		String createPath = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(createPath).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(createContentReq));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
		Response createResponse = getResponse(actions);
		Assert.assertEquals("ERR_GRAPH_ADD_NODE_VALIDATION_FAILED", createResponse.getParams().getErr());
		Assert.assertEquals("CLIENT_ERROR", createResponse.getResponseCode().toString());
	}

    private String createResourceContent() throws Exception {
	    String path = basePath + "/create";
	    String createDocumentContentRequestBody = "{\"request\": {\"content\": {\"name\": \"Text Book 1\",\"code\": \"test.book.1\",\"mimeType\": \"application/pdf\",\"contentType\":\"Resource\"}}}";
        actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi").header("X-Channel-Id", "channelTest")
                .header("user-id", "ilimi").contentType(MediaType.APPLICATION_JSON).content(createDocumentContentRequestBody));
        assertEquals(200, actions.andReturn().getResponse().getStatus());
        return (String) getResponse(actions).getResult().get(TestParams.node_id.name());
    }

    private void retireContent(String contentId) throws Exception {
        String path = basePath + "/retire/" + contentId;
        actions = mockMvc.perform(MockMvcRequestBuilders.delete(path));
        assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    private void validateRetiredNode(String contentId) throws Exception {
        Response response = getContent(contentId);
        assertEquals("Retired", ((Map<String, Object>) response.get("content")).get("status"));
    }

    private void uploadResourceContent(String contentId) throws Exception {
        String uploadPath = basePath + "/upload/" + contentId;
        File inputFile = getResourceFile("pdf.pdf");
        FileInputStream fileStream = new FileInputStream(inputFile);
        MockMultipartFile testFile = new MockMultipartFile("file", "pdf.pdf", "application/pdf", fileStream);
        actions = mockMvc.perform(MockMvcRequestBuilders.fileUpload(uploadPath).file(testFile).header("user-id", "ilimi"));
        assertEquals(200, actions.andReturn().getResponse().getStatus());
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

	@Test
	public void retireDraftedDocumentContent() throws Exception {
		String contentId = createResourceContent();
		retireContent(contentId);
		validateRetiredNode(contentId);
	}
	@Ignore
	@Test
	public void retirePublishedDocumentContent() throws Exception {
		String contentId = createResourceContent();
		uploadResourceContent(contentId);
		delay(5000);
		publish(contentId);
		delay(5000);
		update(contentId);
		retireContent(contentId);
		validateRetiredNode(contentId);
	}

	@Ignore
	@Test
	public void retireReviewedDocumentContent() throws Exception {
		String contentId = createResourceContent();
		uploadResourceContent(contentId);
		delay(25000);
		review(contentId);
		retireContent(contentId);
        validateRetiredNode(contentId);
	}

	private String createCollection() throws Exception {
        String path = basePath + "/create";
        String createCollectionContentRequestBody = "{\"request\": {\"content\": {\"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_Dev\",\"name\": \"TestBook1\",\"language\":[\"English\"],\"contentType\": \"Collection\",\"code\": \"testbook1\",\"tags\":[\"QA_Content\"],\"mimeType\": \"application/vnd.ekstep.content-collection\",\"children\":[]}}}";
        actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi").header("X-Channel-Id", "channelTest")
                .header("user-id", "ilimi").contentType(MediaType.APPLICATION_JSON).content(createCollectionContentRequestBody));
        assertEquals(200, actions.andReturn().getResponse().getStatus());
        return (String) getResponse(actions).getResult().get(TestParams.node_id.name());
    }

    private void heirarchyUpdate(String collectionContentId) throws Exception {
        String resourceContentId1 = createResourceContent();
        String resourceContentId2 = createResourceContent();
        String path = basePath + "/hierarchy/update";
        String updateHeirarchyReq = "{\"request\":{\"data\":{\"nodesModified\":{\"TestBookUnit-0111\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_01\",\"description\":\"Test_Collection_TextBookUnit_01\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\"}},\"TestBookUnit-0211\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_02\",\"description\":\"TTest_Collection_TextBookUnit_02\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\"}},\"TestBookUnit-0311\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_03\",\"description\":\"TTest_Collection_TextBookUnit_03\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b82dd3aca\"}},\"TestBookUnit-0411\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_04\",\"description\":\"TTest_Collection_TextBookUnit_04\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4912-9b2b-8390b82dd3aca\"}},\"TestBookUnit-0511\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_05\",\"description\":\"TTest_Collection_TextBookUnit_05\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4912-9b2b-8390b82dd3aca\"}}},\"hierarchy\":{\"" + collectionContentId + "\":{\"name\":\"TextBook1-CreatedforRetireTesting\",\"contentType\":\"Collection\",\"children\":[\"TestBookUnit-0111\",\"TestBookUnit-0211\",\"TestBookUnit-0311\",\"TestBookUnit-0411\",\"TestBookUnit-0511\",\"" + resourceContentId1 + "\"],\"root\":true},\"TestBookUnit-0111\":{\"name\":\"Test_Collection_TextBookUnit_01\",\"contentType\":\"TextBookUnit\",\"children\":[\"" + resourceContentId2 + "\"],\"root\":false},\"TestBookUnit-0211\":{\"name\":\"Test_Collection_TextBookUnit_02\",\"contentType\":\"TextBookUnit\",\"children\":[\"" + resourceContentId2 +"\"],\"root\":false},\"TestBookUnit-0311\":{\"name\":\"Test_Collection_TextBookUnit_03\",\"contentType\":\"TextBookUnit\",\"children\":[\""+ resourceContentId2 +"\"],\"root\":false},\"TestBookUnit-0411\":{\"name\":\"Test_Collection_TextBookUnit_04\",\"contentType\":\"TextBookUnit\",\"children\":[\""+ resourceContentId2 +"\"],\"root\":false},\"TestBookUnit-0511\":{\"name\":\"Test_Collection_TextBookUnit_05\",\"contentType\":\"TextBookUnit\",\"children\":[\"" + resourceContentId2 + "\"],\"root\":false},\"" + resourceContentId1 + "\":{\"name\":\"Test_Resource_Content\",\"contentType\":\"Story\",\"children\":[],\"root\":false}}}}}";
        actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).header("X-Channel-Id", "channelTest")
                .header("user-id", "ilimi").contentType(MediaType.APPLICATION_JSON).content(updateHeirarchyReq));
        assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    private void validateChildrenRetiredStatusRecursively(String contentId) throws Exception {
        if(!StringUtils.endsWithIgnoreCase(contentId, ".img")) {
			validateChildrenRetiredStatusRecursively(contentId + ".img");
        }
        Response response = getContent(contentId);
        if(200 == response.getResponseCode().code()) {
            Map<String, Object> contentMap = (Map<String, Object>) response.get("content");
			Optional.ofNullable(contentMap.get("children")).ifPresent( children -> {
				List<Map<String, Object>> childrenList = (List<Map<String, Object>>) children;
				if(!childrenList.isEmpty()) {
					childrenList.forEach(child -> {
						try {
							if ("Parent".equals(child.get("visibility"))) {
								validateChildrenRetiredStatusRecursively((String) child.get("identifier"));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					});
				}
			});
			if ("Parent".equals(contentMap.get("visibility"))) {
				assertEquals("Retired", contentMap.get("status"));
			}
        }
    }

	private void validateRetiredCollectionContent(String contentId) throws Exception {
		validateChildrenRetiredStatusRecursively(contentId);
		validateRetiredNode(contentId);
	}

	@Test
	public void retireDraftedCollectionContent() throws Exception {
		String collectionContentId = createCollection();
		heirarchyUpdate(collectionContentId);
		retireContent(collectionContentId);
		validateRetiredCollectionContent(collectionContentId);
	}

	@Test
	public void retireReviewedCollectionContent() throws Exception {
		String collectionContentId = createCollection();
		heirarchyUpdate(collectionContentId);
		review(collectionContentId);
		retireContent(collectionContentId);
		validateRetiredCollectionContent(collectionContentId);
	}

    @Test
    public void retirePublishedCollectionContentWithNoChildren() throws Exception {
        String collectionContentId = createCollection();
        publish(collectionContentId);
        delay(3000);
        update(collectionContentId);
        retireContent(collectionContentId);
		validateRetiredCollectionContent(collectionContentId);
    }

    @Test
    public void retirePublishedCollectionContentWithChildren() throws Exception {
        String collectionContentId = createCollection();
        heirarchyUpdate(collectionContentId);
        publish(collectionContentId);
        delay(3000);
        update(collectionContentId);
        retireContent(collectionContentId);
		validateRetiredCollectionContent(collectionContentId);
    }
}