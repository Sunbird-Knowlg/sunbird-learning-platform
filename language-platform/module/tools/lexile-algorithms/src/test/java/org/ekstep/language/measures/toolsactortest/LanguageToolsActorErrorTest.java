package org.ekstep.language.measures.toolsactortest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.logger.PlatformLogger;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.measures.meta.SyllableMap;
import org.ekstep.language.router.LanguageRequestRouterPool;
import org.ekstep.language.test.util.RequestResponseTestHelper;
import org.ekstep.language.util.ElasticSearchUtil;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.web.servlet.ResultActions;


public class LanguageToolsActorErrorTest {

	private static ObjectMapper mapper = new ObjectMapper();
	static ElasticSearchUtil util;
	private static String TEST_LANGUAGE = "hi";
	

	static {
		LanguageRequestRouterPool.init();
	}

	@Test
	public void getWordComplexity() throws JsonParseException, JsonMappingException,
			IOException {
		String contentString = "{\"request\":{\"language_id\":\"hi\",\"word\":12344}}";
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
		Assert.assertEquals("failed", response.getParams().getStatus());
	}

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
        request.setOperation("computeTextComplexityError");
        request.getContext().put(LanguageParams.language_id.name(), TEST_LANGUAGE);
		PlatformLogger.log("List | Request: " + request);
		Response response = RequestResponseTestHelper.getResponse(
				request);
		PlatformLogger.log("List | Response: " + response);
		RequestResponseTestHelper.getResponseEntity(response, apiId,
				(null != request.getParams()) ? request.getParams().getMsgid()
						: null);
		
		Assert.assertEquals("failed", response.getParams().getStatus());
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
