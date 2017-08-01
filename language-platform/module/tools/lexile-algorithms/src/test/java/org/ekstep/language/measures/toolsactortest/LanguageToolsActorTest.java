package org.ekstep.language.measures.toolsactortest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.web.servlet.ResultActions;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.logger.PlatformLogger;

public class LanguageToolsActorTest {

	private static ObjectMapper mapper = new ObjectMapper();
	static ElasticSearchUtil util;
	private static String TEST_LANGUAGE = "hi";
	

	static {
		LanguageRequestRouterPool.init();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void computeComplexity() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{  \"request\": {    \"language_id\": \""+TEST_LANGUAGE+"\",    \"words\": [\"समोसा\",\"आम\", \"माला\",\"शेर\",\"पेड़\",\"धागा\",\"बाल\",\"दिया\",\"जल\",\"दूध\"],    \"texts\": [\"एक चर्मरोग जिसमें बहुत खुजली होती है\", \"वे अपने भुलक्कड़पन की कथा बखान करते\"]  }}";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
        String apiId = "language.complexity";
        Request request = RequestResponseTestHelper.getRequest(map);
        request.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
        request.setOperation(LanguageOperations.computeComplexity.name());
        request.getContext().put(LanguageParams.language_id.name(), TEST_LANGUAGE);
		PlatformLogger.log("List | Request: " + request);
		Response response = RequestResponseTestHelper.getResponse(
				request);
		PlatformLogger.log("List | Response: " + response);
		RequestResponseTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		Map<String, Object> complexityMeasures = (Map<String, Object>) result
				.get("complexity_measures");
		ComplexityMeasures cm1 = (ComplexityMeasures) complexityMeasures.get("धागा");
		ComplexityMeasures cm2 = (ComplexityMeasures) complexityMeasures.get("समोसा");
		ComplexityMeasures cm3 = (ComplexityMeasures) complexityMeasures.get("एक चर्मरोग जिसमें बहुत खुजली होती है");
		Assert.assertEquals(0.6, cm1.getOrthographic_complexity(), 0.0);
		Assert.assertEquals(11.01, cm1.getPhonologic_complexity(), 0.0);
		
		Assert.assertEquals(1.0, cm2.getOrthographic_complexity(), 0.0);
        Assert.assertEquals(14.19, cm2.getPhonologic_complexity(), 0.0);
        
//        Assert.assertEquals(0.99, cm3.getOrthographic_complexity(), 0.0);
//        Assert.assertEquals(16.32, cm3.getPhonologic_complexity(), 0.0);
        
        Assert.assertEquals(0.41, cm3.getOrthographic_complexity(), 0.0);
        Assert.assertEquals(6.35, cm3.getPhonologic_complexity(), 0.0);

	}
	
	@Test
	public void getWordComplexity() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{\"request\":{\"language_id\":\"hi\",\"word\":\"समोसा\"}}";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
        String apiId = "language.wordcomplexity";
        Request request = RequestResponseTestHelper.getRequest(map);
        request.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
        request.setOperation(LanguageOperations.computeWordComplexity.name());
        request.getContext().put(LanguageParams.language_id.name(), TEST_LANGUAGE);
		PlatformLogger.log("List | Request: " + request);
		Response response = RequestResponseTestHelper.getResponse(
				request);
		PlatformLogger.log("List | Response: " + response);
		RequestResponseTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		ComplexityMeasures wordComplexity = (ComplexityMeasures) result
				.get("word_complexity");
		Assert.assertEquals(wordComplexity.getOrthographic_complexity(), new Double(1.0));
		Assert.assertEquals(wordComplexity.getPhonologic_complexity(), new Double(14.19));
	}

	@SuppressWarnings("unchecked")
	@Ignore
	@Test
	public void getTextComplexity() throws JsonParseException, JsonMappingException,
			IOException {
	    SyllableMap.loadSyllables("hi");
	    String contentString = "{\"request\":{\"language_id\":\"hi\",\"text\":\"एकचर्मरोगजिसमेंबहुतखुजलीहोतीहै\"}}";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
        String apiId = "language.textcomplexity";
        Request request = RequestResponseTestHelper.getRequest(map);
        request.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
        request.setOperation(LanguageOperations.computeTextComplexity.name());
        request.getContext().put(LanguageParams.language_id.name(), TEST_LANGUAGE);
		PlatformLogger.log("List | Request: " + request);
		Response response = RequestResponseTestHelper.getResponse(
				request);
		PlatformLogger.log("List | Response: " + response);
		RequestResponseTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
//		ComplexityMeasures textComplexity = (ComplexityMeasures) result
//				.get("text_complexity");
//		Assert.assertEquals(textComplexity.getOrthographic_complexity(), new Double(6.9));
//		Assert.assertEquals(textComplexity.getPhonologic_complexity(), new Double(114.23));
		Map<String, Object>	text_complexity = (Map<String, Object>) result
				.get("text_complexity");
		Assert.assertEquals((Double)text_complexity.get("totalOrthoComplexity"), new Double(7.36));
		Assert.assertEquals((Double)text_complexity.get("totalPhonicComplexity"), new Double(114.23));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void getWordFeatures() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{\"request\":{\"language_id\":\"hi\",\"word\":\"धागा\"}}";
		String word="धागा";
		String wordUnicode="\\0927\\093e \\0917\\093e";
		Double phonicComplexity = 11.01;
		Double orthoComplexity = 0.6;
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
        String apiId = "wordFeatures.get";
        Request request = RequestResponseTestHelper.getRequest(map);
        request.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
        request.setOperation(LanguageOperations.getWordFeatures.name());
        request.getContext().put(LanguageParams.language_id.name(), TEST_LANGUAGE);
		PlatformLogger.log("List | Request: " + request);
		Response response = RequestResponseTestHelper.getResponse(
				request);
		PlatformLogger.log("List | Response: " + response);
		RequestResponseTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		Map<String, WordComplexity> wordFeatures = (Map<String, WordComplexity>) result
				.get("word_features");
		WordComplexity wordComplexity= wordFeatures.get(word);
		Assert.assertEquals(wordComplexity.getWord(), word);
		Assert.assertEquals(wordComplexity.getUnicode(), wordUnicode);
		Assert.assertEquals(wordComplexity.getOrthoComplexity(), orthoComplexity);
		Assert.assertEquals(wordComplexity.getPhonicComplexity(), phonicComplexity);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void getWordsFeatures() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{\"request\":{\"language_id\":\"hi\",\"words\":[\"धागा\"]}}";
		String word="धागा";
		String wordUnicode="\\0927\\093e \\0917\\093e";
		Double phonicComplexity = 11.01;
		Double orthoComplexity = 0.6;
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
        String apiId = "wordFeatures.get";
        Request request = RequestResponseTestHelper.getRequest(map);
        request.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
        request.setOperation(LanguageOperations.getWordFeatures.name());
        request.getContext().put(LanguageParams.language_id.name(), TEST_LANGUAGE);
		PlatformLogger.log("List | Request: " + request);
		Response response = RequestResponseTestHelper.getResponse(
				request);
		PlatformLogger.log("List | Response: " + response);
		RequestResponseTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		Map<String, WordComplexity> wordFeatures = (Map<String, WordComplexity>) result
				.get("word_features");
		WordComplexity wordComplexity= wordFeatures.get(word);
		Assert.assertEquals(wordComplexity.getWord(), word);
		Assert.assertEquals(wordComplexity.getUnicode(), wordUnicode);
		Assert.assertEquals(wordComplexity.getOrthoComplexity(), orthoComplexity);
		Assert.assertEquals(wordComplexity.getPhonicComplexity(), phonicComplexity);
	}
	
	@SuppressWarnings({ "unused" })
	@Test
	public void loadLanguageVectors() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{\"request\":{\"language_id\":\"hi\"}}";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
        String apiId = "loadLanguageVectors.put";
        Request request = RequestResponseTestHelper.getRequest(map);
        request.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
        request.setOperation(LanguageOperations.loadLanguageVectors.name());
        request.getContext().put(LanguageParams.language_id.name(), TEST_LANGUAGE);
		PlatformLogger.log("List | Request: " + request);
		Response response = RequestResponseTestHelper.getResponse(
				request);
		PlatformLogger.log("List | Response: " + response);
		RequestResponseTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		System.out.println("Test");
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
