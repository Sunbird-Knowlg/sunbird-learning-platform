package com.ilimi.taxonomy.mgr.impl;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Response;
import com.ilimi.taxonomy.content.common.TestCaseParams;
import com.ilimi.taxonomy.content.common.TestSuitSetup;

public class ContentManagerImplTest extends TestSuitSetup {

	ContentManagerImpl contentManager = new ContentManagerImpl();

	ObjectMapper mapper = new ObjectMapper();

	String createContent = "{\"osId\":\"org.ekstep.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Unit Test Content\",\"gradeLevel\":[\"Grade 2\"],\"name\":\"Unit Test Content\",\"language\":[\"English\"],\"contentType\":\"Story\",\"code\":\"test content\",\"mimeType\":\"application/vnd.ekstep.ecml-archive\"}";
	String requestForReview = "{\"request\":{\"content\":{\"lastPublishedBy\":\"Ekstep\"}}}";

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void beforeSetupTestSuit() {
		System.out.println("ContentManagerImplTest -- Before");
	}

	@AfterClass
	public static void afterCleanData() {
		System.out.println("ContentManagerImplTest -- After");
	}

	@Test
	public void testCreateContent_01() {
		try {
			Map<String, Object> contentMap = mapper.readValue(createContent, new TypeReference<Map<String, Object>>() {
			});
			Response response = contentManager.createContent(contentMap);
			String nodeId = (String) response.getResult().get(TestCaseParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestCaseParams.versionKey.name());
			Assert.assertTrue(StringUtils.isNotBlank(nodeId));
			Assert.assertTrue(StringUtils.isNotBlank(versionKey));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
