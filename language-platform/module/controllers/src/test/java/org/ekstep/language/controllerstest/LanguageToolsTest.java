package org.ekstep.language.controllerstest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.language.router.LanguageRequestRouterPool;
import org.ekstep.language.util.ElasticSearchUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.ilimi.common.dto.Response;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class LanguageToolsTest {

	@Autowired
	private WebApplicationContext context;
	private static ObjectMapper mapper = new ObjectMapper();
	private ResultActions actions;
	static ElasticSearchUtil util;
	private static String TEST_LANGUAGE = "hi";

	static {
		LanguageRequestRouterPool.init();
	}

	@BeforeClass
	public static void init() throws Exception {

	}

	@AfterClass
	public static void close() throws IOException, InterruptedException {
		
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getWordComplexity() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{  \"request\": {    \"language_id\": \""+TEST_LANGUAGE+"\",    \"words\": [\"समोसा\",\"आम\", \"माला\",\"शेर\",\"पेड़\",\"धागा\",\"बाल\",\"दिया\",\"जल\",\"दूध\"],    \"texts\": [\"एक चर्मरोग जिसमें बहुत खुजली होती है\", \"वे अपने भुलक्कड़पन की कथा बखान करते\"]  }}";
		//String expectedResult = "{\"धागा\":{\"orthographic_complexity\":0.0,\"phonologic_complexity\":11.01},\"समोसा\":{\"orthographic_complexity\":0.4,\"phonologic_complexity\":14.19},\"बाल\":{\"orthographic_complexity\":0.0,\"phonologic_complexity\":8.25},\"दिया\":{\"orthographic_complexity\":0.0,\"phonologic_complexity\":10.1},\"जल\":{\"orthographic_complexity\":0.0,\"phonologic_complexity\":8.9},\"दूध\":{\"orthographic_complexity\":0.0,\"phonologic_complexity\":12.45},\"आम\":{\"orthographic_complexity\":0.0,\"phonologic_complexity\":8.1},\"वे अपने भुलक्कड़पन की कथा बखान करते\":{\"orthographic_complexity\":0.57,\"phonologic_complexity\":16.63},\"माला\":{\"orthographic_complexity\":0.0,\"phonologic_complexity\":9.45},\"शेर\":{\"orthographic_complexity\":0.4,\"phonologic_complexity\":8.9},\"पेड़\":{\"orthographic_complexity\":0.4,\"phonologic_complexity\":5.05},\"एक चर्मरोग जिसमें बहुत खुजली होती है\":{\"orthographic_complexity\":0.67,\"phonologic_complexity\":16.32}}";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/tools/lexileMeasures";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON)
					.content(contentString.getBytes())
					.header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
		Map<String, Object> result = resp.getResult();
		Map<String, Object> complexityMeasures = (Map<String, Object>) result
				.get("complexity_measures");
		String resultString = mapper.writeValueAsString(complexityMeasures);
		System.out.println(resultString);
        Map<String, Object> cm1 = (Map<String, Object>) complexityMeasures.get("धागा");
        Map<String, Object> cm2 = (Map<String, Object>) complexityMeasures.get("समोसा");
        Map<String, Object> cm3 = (Map<String, Object>) complexityMeasures.get("एक चर्मरोग जिसमें बहुत खुजली होती है");
        Assert.assertEquals(0.0, (double) cm1.get("orthographic_complexity"), 0.0);
        Assert.assertEquals(11.01, (double) cm1.get("phonologic_complexity"), 0.0);
        
        Assert.assertEquals(0.4, (double) cm2.get("orthographic_complexity"), 0.0);
        Assert.assertEquals(14.19, (double) cm2.get("phonologic_complexity"), 0.0);
        
        Assert.assertEquals(0.67, (double) cm3.get("orthographic_complexity"), 0.0);
        Assert.assertEquals(16.32, (double) cm3.get("phonologic_complexity"), 0.0);
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
