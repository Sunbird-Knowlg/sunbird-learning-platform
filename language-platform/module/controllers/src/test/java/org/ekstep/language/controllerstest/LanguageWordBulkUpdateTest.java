package org.ekstep.language.controllerstest;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.language.router.LanguageRequestRouterPool;
import org.ekstep.search.router.SearchRequestRouterPool;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.ilimi.common.router.RequestRouterPool;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class LanguageWordBulkUpdateTest {

	@Autowired
	private WebApplicationContext context;
	private static ObjectMapper mapper = new ObjectMapper();
	private ResultActions actions;
	
	static {
		LanguageRequestRouterPool.init();
        SearchRequestRouterPool.init(RequestRouterPool.getActorSystem());		
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void bulkUpdateSuccess() throws JsonParseException,
			JsonMappingException, IOException {
		
        InputStream inputStream = LanguageWordBulkUpdateTest.class.getClassLoader().getResourceAsStream("testData/words_bulk_test.csv");
        MockMultipartFile file = new MockMultipartFile("file", inputStream);
        
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v2/language/dictionary/word/bulkUpdateWordsByCSV/en";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.fileUpload(path).
					file(file)
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Response response = jsonToObject(actions);
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		List errors = (List) result.get("errors");
		Assert.assertNotNull(errors);
		Assert.assertTrue(errors.isEmpty());
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void bulkUpdateFail() throws JsonParseException,
			JsonMappingException, IOException {
		
        InputStream inputStream = LanguageWordBulkUpdateTest.class.getClassLoader().getResourceAsStream("testData/words_bulk_test_fail.csv");
        MockMultipartFile file = new MockMultipartFile("file", inputStream);
        
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v2/language/dictionary/word/bulkUpdateWordsByCSV/en";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.fileUpload(path).
					file(file)
					.header("user-id", "ilimi"));
			Assert.assertEquals(400, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Response jsonToObject(ResultActions actions) {
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
}
