package org.sunbird.taxonomy.mgr.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.graph.engine.common.GraphEngineTestSetup;
import org.sunbird.taxonomy.content.common.TestParams;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test Cases for code coverage of Upload Content. This Class covers test cases
 * for both upload().
 * 
 * @see ContentManagerImpl
 * @author gauraw
 *
 */
public class ContentManagerImplUploadContentTest extends GraphEngineTestSetup {

	static ContentManagerImpl contentManager = new ContentManagerImpl();

	static Map<String, Object> versionKeyMap = new HashMap<String, Object>();

	static ObjectMapper mapper = new ObjectMapper();

	static ClassLoader classLoader = ContentManagerImplUploadContentTest.class.getClassLoader();
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());

	static String createDocumentContent = "{\"osId\":\"org.sunbird.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"gradeLevel\":[\"Grade 2\"],\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Story\",\"code\":\"test content\",\"mimeType\":\"application/pdf\"}";
	private static String channelId = "in.ekstep";
	
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void initTest() throws Exception {
		loadDefinition("definitions/content_definition.json");
		seedContent();

	}

	@AfterClass
	public static void finishTest() {
	}

	/*
	 * upload with file - start
	 * 
	 */

	// Empty Content Id
	@Test
	public void testUploadContentException_01() {

		exception.expect(ClientException.class);
		String contentId = "";
		String mimeType = "application/pdf";
		File file = new File("");
		Response response = contentManager.upload(contentId, file, mimeType);
	}

	// Empty Taxonomy Id
	@Test
	public void testUploadContentException_02() {

		exception.expect(ClientException.class);
		String contentId = "TEST1";
		String taxonomyId = "";
		String mimeType = "application/pdf";
		File file = new File("");
		Response response = contentManager.upload(contentId, file, mimeType);
	}

	// Invalid Content Id
	@Test
	public void testUploadContentException_03() {

		exception.expect(ClientException.class);
		String contentId = "TEST1.img"; // Invalid Content Id
		String mimeType = "application/pdf";
		File file = new File("");
		Response response = contentManager.upload(contentId, file, mimeType);
	}

	// Empty File
	@Test
	public void testUploadContentException_04() {

		exception.expect(ClientException.class);
		String contentId = "TEST1img";
		String mimeType = "application/pdf";
		File file = null;
		Response response = contentManager.upload(contentId, file, mimeType);
	}

	// Upload Document Content
	@Test
	public void testUploadContent_01() {
		try {
			String contentId = "U_Document_001";
			String mimeType = "application/pdf";
			File file1 = new File(path + "/pdf.pdf");
			String absPath = file1.getAbsolutePath();
			File file = new File(absPath);

			Response response = contentManager.upload(contentId, file, mimeType);
			String responseCode = (String) response.getResponseCode().toString();
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			assertTrue(StringUtils.isNotBlank(nodeId));
			assertTrue(StringUtils.isNotBlank(versionKey));
			assertFalse(
					StringUtils.equalsIgnoreCase(versionKey, (String) versionKeyMap.get(TestParams.versionKey.name())));
			if (StringUtils.isNotBlank(versionKey))
				versionKeyMap.put(contentId, versionKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Upload Document Content with empty mime type.
	@Test
	public void testUploadContent_02() {
		try {
			String contentId = "U_Document_002";
			String mimeType = "";
			File file1 = new File(path + "/test.pdf");
			String absPath = file1.getAbsolutePath();
			File file = new File(absPath);

			Response response = contentManager.upload(contentId, file, mimeType);
			String responseCode = (String) response.getResponseCode().toString();
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			assertTrue(StringUtils.isNotBlank(nodeId));
			assertTrue(StringUtils.isNotBlank(versionKey));
			assertFalse(
					StringUtils.equalsIgnoreCase(versionKey, (String) versionKeyMap.get(TestParams.versionKey.name())));
			if (StringUtils.isNotBlank(versionKey))
				versionKeyMap.put(contentId, versionKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * 
	 * upload with file url - start
	 * 
	 */

	// Empty Taxonomy Id
	@Test
	public void testUploadContentException_05() {
		String contentId = "TEST1";
		String taxonomyId = "";
		String fileUrl = "https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/u_document_04/artifact/pdf.pdf";
		String mimeType = "application/pdf";
		exception.expect(ClientException.class);
		Response response = contentManager.upload(contentId, fileUrl, mimeType);

	}

	// Empty Content Id
	@Test
	public void testUploadContentException_06() {
		String contentId = "";
		String fileUrl = "https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/u_document_04/artifact/pdf.pdf";
		String mimeType = "application/pdf";
		exception.expect(ClientException.class);
		Response response = contentManager.upload(contentId, fileUrl, mimeType);

	}

	// Empty file url
	@Test
	public void testUploadContentException_07() {
		String contentId = "TEST1";
		String fileUrl = "";
		String mimeType = "application/pdf";
		exception.expect(ClientException.class);
		Response response1 = contentManager.upload(contentId, fileUrl, mimeType);

	}

	// upload with fileUrl
	@Test
	public void testUploadContent_03() {
		try {
			String contentId = "U_Document_003";
			String mimeType = "application/pdf";
			String fileUrl = "https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/u_document_04/artifact/pdf.pdf";

			Response response = contentManager.upload(contentId, fileUrl, mimeType);
			String responseCode = (String) response.getResponseCode().toString();
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			assertTrue(StringUtils.isNotBlank(nodeId));
			assertTrue(StringUtils.isNotBlank(versionKey));
			assertFalse(
					StringUtils.equalsIgnoreCase(versionKey, (String) versionKeyMap.get(TestParams.versionKey.name())));
			if (StringUtils.isNotBlank(versionKey))
				versionKeyMap.put(contentId, versionKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// upload with fileUrl and empty mime type.
	@Test
	public void testUploadContent_04() {
		try {
			String contentId = "U_Document_004";
			String mimeType = "";
			String fileUrl = "https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/u_document_04/artifact/pdf.pdf";

			Response response = contentManager.upload(contentId, fileUrl, mimeType);
			String responseCode = (String) response.getResponseCode().toString();
			String nodeId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			assertTrue(StringUtils.isNotBlank(nodeId));
			assertTrue(StringUtils.isNotBlank(versionKey));
			assertFalse(
					StringUtils.equalsIgnoreCase(versionKey, (String) versionKeyMap.get(TestParams.versionKey.name())));
			if (StringUtils.isNotBlank(versionKey))
				versionKeyMap.put(contentId, versionKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void seedContent() throws Exception {
		try {
			// Create Document Content - 001
			Map<String, Object> documentContentMap1 = mapper.readValue(createDocumentContent,
					new TypeReference<Map<String, Object>>() {
					});
			documentContentMap1.put(TestParams.identifier.name(), "U_Document_001");
			Response documentResponse1 = contentManager.create(documentContentMap1, channelId);
			String documentVersionKey1 = (String) documentResponse1.getResult().get(TestParams.versionKey.name());
			if (StringUtils.isNotBlank(documentVersionKey1))
				versionKeyMap.put("U_Document_001", documentVersionKey1);

			// Create Document Content - 002
			Map<String, Object> documentContentMap2 = mapper.readValue(createDocumentContent,
					new TypeReference<Map<String, Object>>() {
					});
			documentContentMap2.put(TestParams.identifier.name(), "U_Document_002");
			Response documentResponse2 = contentManager.create(documentContentMap2, channelId);
			String documentVersionKey2 = (String) documentResponse2.getResult().get(TestParams.versionKey.name());
			if (StringUtils.isNotBlank(documentVersionKey2))
				versionKeyMap.put("U_Document_002", documentVersionKey2);

			// Create Document Content - 003
			Map<String, Object> documentContentMap3 = mapper.readValue(createDocumentContent,
					new TypeReference<Map<String, Object>>() {
					});
			documentContentMap3.put(TestParams.identifier.name(), "U_Document_003");
			Response documentResponse3 = contentManager.create(documentContentMap3, channelId);
			String documentVersionKey3 = (String) documentResponse3.getResult().get(TestParams.versionKey.name());
			if (StringUtils.isNotBlank(documentVersionKey3))
				versionKeyMap.put("U_Document_003", documentVersionKey3);

			// Create Document Content - 004
			Map<String, Object> documentContentMap4 = mapper.readValue(createDocumentContent,
					new TypeReference<Map<String, Object>>() {
					});
			documentContentMap4.put(TestParams.identifier.name(), "U_Document_004");
			Response documentResponse4 = contentManager.create(documentContentMap4, channelId);
			String documentVersionKey4 = (String) documentResponse4.getResult().get(TestParams.versionKey.name());
			if (StringUtils.isNotBlank(documentVersionKey4))
				versionKeyMap.put("U_Document_004", documentVersionKey4);

		} catch (IOException e) {
			System.out.println("Exception Occured while creating content for Upload Test : " + e.getMessage());
			e.printStackTrace();
		}

	}

}