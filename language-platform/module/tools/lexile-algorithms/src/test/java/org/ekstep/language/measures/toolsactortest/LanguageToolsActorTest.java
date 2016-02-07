package org.ekstep.language.measures.toolsactortest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.measures.entity.ComplexityMeasures;
import org.ekstep.language.measures.entity.WordComplexity;
import org.ekstep.language.measures.meta.SyllableMap;
import org.ekstep.language.router.LanguageRequestRouterPool;
import org.ekstep.language.test.util.RequestResponseTestHelper;
import org.ekstep.language.util.ElasticSearchUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.web.servlet.ResultActions;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

public class LanguageToolsActorTest {

	private static ObjectMapper mapper = new ObjectMapper();
	static ElasticSearchUtil util;
	private static String TEST_LANGUAGE = "hi";
	private static Logger LOGGER = LogManager.getLogger(LanguageToolsActorTest.class
			.getName());

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
	public void computeComplexity() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{  \"request\": {    \"language_id\": \""+TEST_LANGUAGE+"\",    \"words\": [\"समोसा\",\"आम\", \"माला\",\"शेर\",\"पेड़\",\"धागा\",\"बाल\",\"दिया\",\"जल\",\"दूध\"],    \"texts\": [\"एक चर्मरोग जिसमें बहुत खुजली होती है\", \"वे अपने भुलक्कड़पन की कथा बखान करते\"]  }}";
		//String expectedResult = "{\"धागा\":{\"orthographic_complexity\":0.0,\"phonologic_complexity\":11.01},\"समोसा\":{\"orthographic_complexity\":0.4,\"phonologic_complexity\":14.19},\"बाल\":{\"orthographic_complexity\":0.0,\"phonologic_complexity\":8.25},\"दिया\":{\"orthographic_complexity\":0.0,\"phonologic_complexity\":10.1},\"जल\":{\"orthographic_complexity\":0.0,\"phonologic_complexity\":8.9},\"दूध\":{\"orthographic_complexity\":0.0,\"phonologic_complexity\":12.45},\"आम\":{\"orthographic_complexity\":0.0,\"phonologic_complexity\":8.1},\"वे अपने भुलक्कड़पन की कथा बखान करते\":{\"orthographic_complexity\":0.57,\"phonologic_complexity\":16.63},\"माला\":{\"orthographic_complexity\":0.0,\"phonologic_complexity\":9.45},\"शेर\":{\"orthographic_complexity\":0.4,\"phonologic_complexity\":8.9},\"पेड़\":{\"orthographic_complexity\":0.4,\"phonologic_complexity\":5.05},\"एक चर्मरोग जिसमें बहुत खुजली होती है\":{\"orthographic_complexity\":0.67,\"phonologic_complexity\":16.32}}";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
        String apiId = "language.complexity";
        Request request = RequestResponseTestHelper.getRequest(map);
        request.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
        request.setOperation(LanguageOperations.computeComplexity.name());
        request.getContext().put(LanguageParams.language_id.name(), TEST_LANGUAGE);
		LOGGER.info("List | Request: " + request);
		Response response = RequestResponseTestHelper.getResponse(
				request, LOGGER);
		LOGGER.info("List | Response: " + response);
		RequestResponseTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		Map<String, Object> complexityMeasures = (Map<String, Object>) result
				.get("complexity_measures");
		String resultString = mapper.writeValueAsString(complexityMeasures);
		System.out.println(resultString);
		ComplexityMeasures cm1 = (ComplexityMeasures) complexityMeasures.get("धागा");
		ComplexityMeasures cm2 = (ComplexityMeasures) complexityMeasures.get("समोसा");
		ComplexityMeasures cm3 = (ComplexityMeasures) complexityMeasures.get("एक चर्मरोग जिसमें बहुत खुजली होती है");
		Assert.assertEquals(0.0, cm1.getOrthographic_complexity(), 0.0);
		Assert.assertEquals(11.01, cm1.getPhonologic_complexity(), 0.0);
		
		Assert.assertEquals(0.4, cm2.getOrthographic_complexity(), 0.0);
        Assert.assertEquals(14.19, cm2.getPhonologic_complexity(), 0.0);
        
        Assert.assertEquals(0.67, cm3.getOrthographic_complexity(), 0.0);
        Assert.assertEquals(16.32, cm3.getPhonologic_complexity(), 0.0);
	}
	
	@Test
	public void getWordComplexity() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{\"request\":{\"language_id\":\"hi\",\"word\":\"समोसा\"}}";
		String expectedResult = "{\"orthographic_complexity\":0.4,\"phonologic_complexity\":14.19}";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
        String apiId = "language.wordcomplexity";
        Request request = RequestResponseTestHelper.getRequest(map);
        // TODO: return error response if language value is blank
        request.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
        request.setOperation(LanguageOperations.computeWordComplexity.name());
        request.getContext().put(LanguageParams.language_id.name(), TEST_LANGUAGE);
		LOGGER.info("List | Request: " + request);
		Response response = RequestResponseTestHelper.getResponse(
				request, LOGGER);
		LOGGER.info("List | Response: " + response);
		RequestResponseTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		ComplexityMeasures wordComplexity = (ComplexityMeasures) result
				.get("word_complexity");
		String resultString = mapper.writeValueAsString(wordComplexity);
		Assert.assertEquals(expectedResult, resultString);
	}

	@Test
	public void getTextComplexity() throws JsonParseException, JsonMappingException,
			IOException {
	    SyllableMap.loadSyllables("hi");
	    String contentString = "{\"request\":{\"language_id\":\"hi\",\"text\":\"एकचर्मरोगजिसमेंबहुतखुजलीहोतीहै\"}}";
	    String expectedResult = "{\"orthographic_complexity\":4.7,\"phonologic_complexity\":114.23}";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
        String apiId = "language.textcomplexity";
        Request request = RequestResponseTestHelper.getRequest(map);
        request.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
        request.setOperation(LanguageOperations.computeTextComplexity.name());
        request.getContext().put(LanguageParams.language_id.name(), TEST_LANGUAGE);
		LOGGER.info("List | Request: " + request);
		Response response = RequestResponseTestHelper.getResponse(
				request, LOGGER);
		LOGGER.info("List | Response: " + response);
		RequestResponseTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		ComplexityMeasures textComplexity = (ComplexityMeasures) result
				.get("text_complexity");
		String resultString = mapper.writeValueAsString(textComplexity);
		Assert.assertEquals(expectedResult, resultString);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void getWordFeatures() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{\"request\":{\"language_id\":\"hi\",\"words\":[\"धागा\"]}}";
		String expectedResult = "{\"धागा\":{\"word\":\"धागा\",\"rts\":null,\"count\":2,\"notation\":\"CVCV\",\"unicode\":\"\\\\0927\\\\093e \\\\0917\\\\093e\",\"orthoVec\":[0,0,0,0,0,0,0],\"phonicVec\":[2,0,0,3,1,0,2,3,0,0,1,0,0,2],\"orthoComplexity\":0.0,\"phonicComplexity\":11.01,\"measures\":{\"orthographic_complexity\":0.0,\"phonologic_complexity\":11.01}}}";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
        String apiId = "wordFeatures.get";
        Request request = RequestResponseTestHelper.getRequest(map);
        request.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
        request.setOperation(LanguageOperations.getWordFeatures.name());
        request.getContext().put(LanguageParams.language_id.name(), TEST_LANGUAGE);
		LOGGER.info("List | Request: " + request);
		Response response = RequestResponseTestHelper.getResponse(
				request, LOGGER);
		LOGGER.info("List | Response: " + response);
		RequestResponseTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		Map<String, WordComplexity> wordFeatures = (Map<String, WordComplexity>) result
				.get("word_features");
		String resultString = mapper.writeValueAsString(wordFeatures);
		System.out.println(resultString);
		Assert.assertEquals(expectedResult, resultString);
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
