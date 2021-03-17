package org.sunbird.taxonomy.mgr.impl;

import static org.junit.Assert.assertEquals;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Response;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.taxonomy.content.common.TestParams;
import org.sunbird.taxonomy.mgr.IContentManager;
import org.sunbird.test.common.CommonTestSetup;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class ContentManagerImplTest extends CommonTestSetup {

	@Autowired
	private IContentManager contentManager;

	
	@Rule
	public ExpectedException exception = ExpectedException.none();

	private static String script_1 = "CREATE KEYSPACE IF NOT EXISTS content_store_test WITH replication = {'class': 'SimpleStrategy','replication_factor': '1'};";
	private static String script_2 = "CREATE TABLE IF NOT EXISTS content_store_test.content_data (content_id text, last_updated_on timestamp,body blob,oldBody blob,stageIcons blob,PRIMARY KEY (content_id));";

	private static ObjectMapper mapper = new ObjectMapper();

	private static String contentId = "UT_Document_001";
	private static String versionKey = "";
	private static String channelId = "in.ekstep";

	@BeforeClass
	public static void init() throws Exception {
		loadDefinition("definitions/content_definition.json", "definitions/concept_definition.json",
				"definitions/dimension_definition.json", "definitions/domain_definition.json");
		executeScript(script_1, script_2);
		startKafkaServer();
	}

	@AfterClass
	public static void clean() {
		tearKafkaServer();
	}

	@Before
	public void setup() throws Exception {
		if (StringUtils.isBlank(versionKey)) {
			createDocumentContent();
			uploadDocumentContent();
		}

	}

	private void createDocumentContent() throws Exception {
		String createDocumentContent = "{\"osId\":\"org.sunbird.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"gradeLevel\":[\"Grade 2\"],\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Story\",\"code\":\"test content\",\"mimeType\":\"application/pdf\"}";
		Map<String, Object> documentContentMap = mapper.readValue(createDocumentContent,
				new TypeReference<Map<String, Object>>() {
				});
		documentContentMap.put(TestParams.identifier.name(), contentId);
		Response documentResponse = contentManager.create(documentContentMap, channelId);
		String versionKeyTemp = (String) documentResponse.getResult().get(TestParams.versionKey.name());
		if (StringUtils.isNotBlank(versionKeyTemp))
			versionKey = versionKeyTemp;
	}

	private void uploadDocumentContent() {
		String mimeType = "application/pdf";
		String fileUrl = "https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/u_document_04/artifact/pdf.pdf";
		Response response = contentManager.upload(contentId, fileUrl, mimeType);
		String responseCode = (String) response.getResponseCode().toString();
		if ("OK".equalsIgnoreCase(responseCode))
			versionKey = (String) response.getResult().get(TestParams.versionKey.name());
	}

	// TODO: Modify Assert Statement when topic check will be enabled in
	// KafkaClient.
	@Ignore
	@Test
	public void contentManagerTest_01() throws Exception {
		String pubReq = "{\"request\": {\"content\": {\"publisher\": \"EkStep\",\"lastPublishedBy\": \"Ekstep\"}}}";
		Map<String, Object> pubReqMap = mapper.readValue(pubReq, new TypeReference<Map<String, Object>>() {
		});
		pubReqMap.put("publish_type", ContentWorkflowPipelineParams.Public.name().toLowerCase());
		Response response = contentManager.publish(contentId, pubReqMap);
		assertEquals("OK", response.getResponseCode().toString());
		assertEquals("Publish Operation for Content Id 'UT_Document_001' Started Successfully!",
				(String) response.getResult().get("publishStatus"));
	}
}
