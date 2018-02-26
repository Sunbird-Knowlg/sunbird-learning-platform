package org.ekstep.taxonomy.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.ekstep.common.dto.Response;
import org.ekstep.graph.engine.common.GraphEngineTestSetup;
import org.ekstep.graph.engine.common.TestParams;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.taxonomy.mgr.impl.ContentManagerImpl;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
public class ContentV3ControllerTest extends GraphEngineTestSetup {

	/** The context. */
	@Autowired
	private WebApplicationContext context;

	/** The actions. */
	private ResultActions actions;

	MockMvc mockMvc;

	private static final String basePath = "/v3/content";
	private static ObjectMapper mapper = new ObjectMapper();
	private static ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();

	private static String[] validDialCode = { "ABC123", "BCD123", "CDE123", "DEF123", "EFG123" };

	private static String contentId = "";
	private static String contentId2 = "";
	private static String versionKey = "";

	private static String DIALCODE_INDEX = "testdialcode";
	private static String DIALCODE_INDEX_TYPE = "dc";

	private static String collectionContent1Id = "";
	private static String collectionVersion1Key = "";
	private static String collectionContent2Id = "";
	private static String collectionVersion2Key = "";
	private static String collectionContent3Id = "";
	private static String collectionVersion3Key = "";

	private static String createDocumentContent = "{\"request\": {\"content\": {\"name\": \"Test Contnet\",\"code\": \"test_code\",\"contentType\": \"Story\",\"mimeType\": \"application/pdf\",\"tags\": [\"colors\", \"games\"]}}}";

	@BeforeClass
	public static void setup() throws Exception {
		loadDefinition("definitions/content_definition.json", "definitions/concept_definition.json",
				"definitions/dimension_definition.json", "definitions/domain_definition.json");
		createDocumentContent();
		createCollectionContent();
		createDialCodeIndex();
	}

	@Before
	public void init() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();

	}

	@AfterClass
	public static void clean() throws Exception {
		elasticSearchUtil.deleteIndex(DIALCODE_INDEX);
	}

	private static void createDialCodeIndex() throws IOException {
		CompositeSearchConstants.DIAL_CODE_INDEX = DIALCODE_INDEX;
		String settings = "{ \"analysis\": {       \"analyzer\": {         \"dc_index_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"lowercase\",             \"mynGram\"           ]         },         \"dc_search_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"standard\",             \"lowercase\"           ]         },         \"keylower\": {           \"tokenizer\": \"keyword\",           \"filter\": \"lowercase\"         }       },       \"filter\": {         \"mynGram\": {           \"type\": \"nGram\",           \"min_gram\": 1,           \"max_gram\": 20,           \"token_chars\": [             \"letter\",             \"digit\",             \"whitespace\",             \"punctuation\",             \"symbol\"           ]         }       }     }   }";
		String mappings = "{\"dynamic_templates\": [      {        \"longs\": {          \"match_mapping_type\": \"long\",          \"mapping\": {            \"type\": \"long\",            fields: {              \"raw\": {                \"type\": \"long\"              }            }          }        }      },      {        \"booleans\": {          \"match_mapping_type\": \"boolean\",          \"mapping\": {            \"type\": \"boolean\",            fields: {              \"raw\": {                \"type\": \"boolean\"              }            }          }        }      },{        \"doubles\": {          \"match_mapping_type\": \"double\",          \"mapping\": {            \"type\": \"double\",            fields: {              \"raw\": {                \"type\": \"double\"              }            }          }        }      },	  {        \"dates\": {          \"match_mapping_type\": \"date\",          \"mapping\": {            \"type\": \"date\",            fields: {              \"raw\": {                \"type\": \"date\"              }            }          }        }      },      {        \"strings\": {          \"match_mapping_type\": \"string\",          \"mapping\": {            \"type\": \"string\",            \"copy_to\": \"all_fields\",            \"analyzer\": \"dc_index_analyzer\",            \"search_analyzer\": \"dc_search_analyzer\",            fields: {              \"raw\": {                \"type\": \"string\",                \"analyzer\": \"keylower\"              }            }          }        }      }    ],    \"properties\": {      \"all_fields\": {        \"type\": \"string\",        \"analyzer\": \"dc_index_analyzer\",        \"search_analyzer\": \"dc_search_analyzer\",        fields: {          \"raw\": {            \"type\": \"string\",            \"analyzer\": \"keylower\"          }        }      }    }  }";
		elasticSearchUtil.addIndex(CompositeSearchConstants.DIAL_CODE_INDEX,
				CompositeSearchConstants.DIAL_CODE_INDEX_TYPE, settings, mappings);

		populateData();
	}

	private static void populateData() throws JsonProcessingException, IOException {
		for (int i = 0; i < validDialCode.length; i++) {
			String dialCode = validDialCode[i];
			Map<String, Object> indexDocument = new HashMap<String, Object>();
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

			elasticSearchUtil.addDocumentWithId(DIALCODE_INDEX, CompositeSearchConstants.DIAL_CODE_INDEX_TYPE, dialCode,
					mapper.writeValueAsString(indexDocument));
		}

	}

	public static void createDocumentContent() throws Exception {
		for (int i = 1; i <= 2; i++) {
			ContentManagerImpl contentManager = new ContentManagerImpl();
			String createDocumentContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"gradeLevel\":[\"Grade 2\"],\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Story\",\"code\":\"test content\",\"mimeType\":\"application/pdf\"}";
			Map<String, Object> documentContentMap = mapper.readValue(createDocumentContent,
					new TypeReference<Map<String, Object>>() {
					});
			Response documentResponse = contentManager.create(documentContentMap);
			if (i == 1) {
				contentId = (String) documentResponse.getResult().get(TestParams.node_id.name());
				versionKey = (String) documentResponse.getResult().get(TestParams.versionKey.name());
			} else if (i == 2) {
				contentId2 = (String) documentResponse.getResult().get(TestParams.node_id.name());
			}

		}

	}

	public static void createCollectionContent() throws Exception {
		ContentManagerImpl contentManager = new ContentManagerImpl();
		String createCollectionContent1 = "{\"name\": \"Collection_Content_2017_Dec_27_102\",\"code\": \"Collection_Content_2017_Dec_27_102\",\"contentType\": \"Collection\",\"mimeType\": \"application/vnd.ekstep.content-collection\"}";
		Map<String, Object> collectionContentMap1 = mapper.readValue(createCollectionContent1,
				new TypeReference<Map<String, Object>>() {
				});
		Response resp1 = contentManager.create(collectionContentMap1);
		collectionContent1Id = (String) resp1.getResult().get(TestParams.node_id.name());
		collectionVersion1Key = (String) resp1.getResult().get(TestParams.versionKey.name());

		String createCollectionContent2 = "{\"name\": \"Collection_Content_2017_Dec_27_103\",\"code\": \"Collection_Content_2017_Dec_27_102\",\"contentType\": \"Collection\",\"mimeType\": \"application/vnd.ekstep.content-collection\"}";
		Map<String, Object> collectionContentMap2 = mapper.readValue(createCollectionContent2,
				new TypeReference<Map<String, Object>>() {
				});
		Response resp2 = contentManager.create(collectionContentMap2);
		collectionContent2Id = (String) resp2.getResult().get(TestParams.node_id.name());
		collectionVersion2Key = (String) resp2.getResult().get(TestParams.versionKey.name());

		String createCollectionContent3 = "{\"name\": \"Collection_Content_2017_Dec_27_104\",\"code\": \"Collection_Content_2017_Dec_27_102\",\"contentType\": \"Collection\",\"mimeType\": \"application/vnd.ekstep.content-collection\"}";
		Map<String, Object> collectionContentMap3 = mapper.readValue(createCollectionContent3,
				new TypeReference<Map<String, Object>>() {
				});
		Response resp3 = contentManager.create(collectionContentMap3);
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
	@Ignore
	@Test
	public void testContentV3Controller_03() throws Exception {
		String path = basePath + "/update/" + contentId;
		String updateDocumentContent = "{\"request\": {\"content\": {\"name\": \"Updated Test Contnet\",\"versionKey\": \""
				+ versionKey + "\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(updateDocumentContent));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Review Content
	 * 
	 */
	@Ignore
	@Test
	public void testContentV3Controller_04() throws Exception {
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
	public void testContentV3Controller_05() throws Exception {
		String path = basePath + "/publish/" + collectionContent2Id;
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
	public void testContentV3Controller_06() throws Exception {
		String path = basePath + "/unlisted/publish/" + collectionContent3Id;
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
	public void testContentV3Controller_07() throws Exception {
		String path = basePath + "/hierarchy/" + collectionContent3Id;
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
	public void testContentV3Controller_08() throws Exception {
		String path = basePath + "/bundle";
		String bundleReqBody = "{\"request\": {\"content_identifiers\": [\"" + contentId
				+ "\"],\"file_name\": \"test_dev_bundle\"}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(bundleReqBody));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Upload with File
	 * 
	 */
	@Ignore
	@Test
	public void testContentV3Controller_09() throws Exception {
		String path = basePath + "/upload/" + contentId;
		FileInputStream fis = new FileInputStream(getResourceFile("test.pdf"));
		MockMultipartFile multipartFile = new MockMultipartFile("file", fis);
		Map<String, String> contentTypeParams = new HashMap<String, String>();
		contentTypeParams.put("boundary", "265001916915724");
		MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);
		actions = mockMvc
				.perform(MockMvcRequestBuilders.post(path).contentType(mediaType).content(multipartFile.getBytes()));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Given: Link Single Dial Code to Single Valid Content. (Data Given as
	 * List) When: Link Dial Code API Hits Then: 200 - OK.
	 * 
	 */
	@Test
	public void testDialCodeLink_01() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + contentId
				+ "\"],\"dialcode\": [\"ABC123\"]}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
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
	 * Hits Then: 400 - CLIENT_ERROR.
	 * 
	 */
	@Test
	public void testDialCodeLink_03() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": \"" + "abc123"
				+ "\",\"dialcode\": \"ABC123\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());

	}

	/*
	 * Given: Link Single Dial Code to Single Invalid Content. (Data Given as
	 * List) When: Link Dial Code API Hits Then: 400 - CLIENT_ERROR.
	 * 
	 */
	@Test
	public void testDialCodeLink_04() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + "abc123"
				+ "\"],\"dialcode\": [\"ABC123\"]}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
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
	 * Ids. When: Link Dial Code API Hits Then: 400 - CLIENT_ERROR
	 * 
	 */

	@Test
	public void testDialCodeLink_07() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + contentId + "\",\"" + contentId2
				+ "\"],\"dialcode\": [\"ABC123\",\"BCD123\"]}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
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
	 * Content with Valid Content Ids. When: Link Dial Code API Hits Then: 400 -
	 * CLIENT_ERROR
	 * 
	 */

	@Test
	public void testDialCodeLink_09() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + contentId
				+ "\"],\"dialcode\": [\"ABC123\",\"DDD123\"]}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Given:Link Single Dial Code (Invalid Dial Code) to Single Content with
	 * Valid Content Ids. When: Link Dial Code API Hits Then: 400 - CLIENT_ERROR
	 * 
	 */

	@Test
	public void testDialCodeLink_10() throws Exception {
		String path = basePath + "/dialcode/link";
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + contentId
				+ "\"],\"dialcode\": [\"DDD123\"]}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(dialCodeLinkReq));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

}