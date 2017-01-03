package com.test;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.test.Helpers.POST;
import static play.test.Helpers.route;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import managers.PlaySearchManager;
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.WithApplication;

public class PlaySearchManagerTest extends WithApplication {
	
	ObjectMapper mapper = new ObjectMapper();
	PlaySearchManager manager = new PlaySearchManager();
	
	@Test
	public void testResposneError() {
		String json = "{\"request\": {\"filters\":{\"objectType\":"
				+ " [\"Concept\", \"Word\", \"Domain\", \"Dimension\","
				+ " \"AssessmentItem\", \"Content\", \"Method\"] }}}";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/v3/metrics").method(POST).bodyJson(data);
			Result result = route(req);
			assertEquals(NOT_FOUND, result.status());
			assertEquals("application/json", result.contentType());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
