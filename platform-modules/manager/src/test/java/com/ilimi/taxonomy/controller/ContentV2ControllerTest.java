package com.ilimi.taxonomy.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import com.ilimi.common.dto.Response;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.taxonomy.content.common.BaseTest;

/**
 * The Class ContentV2ControllerTest.
 * 
 * @author Mohammad Azharuddin
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class ContentV2ControllerTest extends BaseTest {

	/** The Class Logger. */
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	/** The context. */
	@Autowired
	private WebApplicationContext context;

	/** The actions. */
	private ResultActions actions;

	/** The default Content Bucket Folder */
	private static final String VALID_CONTENT_PACKAGE_FILE = "TEST_PACKAGE_I.zip";
	
	private static final String s3Content = "s3.content.folder";
    private static final String s3Artifact = "s3.artifact.folder";

	/** The Map of Created Node with id */
	private static final Map<String, String> createdNodeMap = new HashMap<String, String>();

	@BeforeClass
	public static void init() {
		// Upload the Test Package to S3
		// Create a node for upload operation
		String uploadNodeIdentifier = "CM_TEST_UPLOAD_01";
				
		uploadFileToS3(getResourceFile(VALID_CONTENT_PACKAGE_FILE), uploadNodeIdentifier);

//		createContentNode(getContentNodeMetadata(uploadNodeIdentifier));
		createdNodeMap.put(uploadNodeIdentifier, "Node for the Upload Operation.");

	}

	/**
	 * The Unit TestCase <code>testUpload_01</code> perform the test for the
	 * positive use case where a valid file is given and valid <code>content
	 * identifier</cod> is given.
	 * 
	 * <ul>Asserts For: <li>The <code>response</code> success (HTTP 200) status.
	 * </li>
	 * <li>The <code>node</code> identifier which will come inside the
	 * <code>result set</code> in response.</li>
	 * <li>The URL of the uploaded content package.</li>
	 * </ul>
	 * 
	 */
	@Test
	public void testUpload_01() {
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String validContentId = "CM_TEST_UPLOAD_01";
		String path = "/v2/content/upload/" + validContentId;
		try {
			FileInputStream fis = new FileInputStream(getResourceFile(VALID_CONTENT_PACKAGE_FILE));
			MockMultipartFile multipartFile = new MockMultipartFile("file", fis);

			Map<String, String> contentTypeParams = new HashMap<String, String>();
			contentTypeParams.put("boundary", "265001916915724");
			MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);
			actions = mockMvc.perform(
					MockMvcRequestBuilders.post(path).contentType(mediaType).content(multipartFile.getBytes()));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response response = jsonToObject(actions);
		Assert.assertEquals("successful", response.getParams().getStatus());
	}

	/**
	 * Json to object.
	 *
	 * @param actions
	 *            the actions
	 * @return the response
	 */
	protected Response jsonToObject(ResultActions actions) {
		String content = null;
		Response resp = null;
		try {
			content = actions.andReturn().getResponse().getContentAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			if (StringUtils.isNotBlank(content))
				resp = objectMapper.readValue(content, Response.class);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	private static String uploadFileToS3(File file, String identifier) {
		String url = "";
		try {
			if (null == file) {
				LOGGER.log("Error! Upload File Package Cannot be 'null'.");
			} else {
				String folder = S3PropertyReader.getProperty(s3Content);
            	folder = folder + "/" + identifier + "/" + S3PropertyReader.getProperty(s3Artifact);
				String[] result = AWSUploader.uploadFile(folder, file);
				if (null != result && result.length == 2)
					url = result[1];
			}
		} catch (Exception e) {
			LOGGER.log("Error! Upload File Package Cannot be 'null'.", e.getMessage(), e);
		}
		return url;
	}

	private static File getResourceFile(String fileName) {
		File file = new File(ContentV2ControllerTest.class.getResource("/Contents/" + fileName).getFile());
		return file;
	}

	@SuppressWarnings("unused")
	private static Map<String, Object> getContentNodeMetadata(String identifier) {
		Map<String, Object> metadata = new HashMap<String, Object>();
		metadata.put(ContentAPIParams.identifier.name(), identifier);
		metadata.put(ContentAPIParams.body.name(), "");
		metadata.put(ContentAPIParams.status.name(), "Mock");
		metadata.put(ContentAPIParams.description.name(), "शेर का साथी हाथी");
		metadata.put(ContentAPIParams.subject.name(), "literacy");
		metadata.put(ContentAPIParams.name.name(), "शेर का साथी हाथी");
		metadata.put(ContentAPIParams.owner.name(), "EkStep");
		metadata.put(ContentAPIParams.code.name(), identifier);
		metadata.put(ContentAPIParams.mimeType.name(), "application/vnd.ekstep.ecml-archive");
		metadata.put(ContentAPIParams.contentType.name(), "Story");
		metadata.put(ContentAPIParams.osId.name(), "org.ekstep.quiz.app");
		return metadata;
	}

	@AfterClass
	public static void finalyze() {

	}

}
