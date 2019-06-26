package org.ekstep.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.route;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import play.mvc.Http.RequestBuilder;
import play.mvc.Result;

public class SearchControllerTest extends BaseSearchControllerTest {

	ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testSearchV2() {
		String json = "{\"request\": {\"filters\":{\"objectType\":"
				+ " [\"Concept\", \"Word\", \"Domain\", \"Dimension\","
				+ " \"AssessmentItem\", \"Content\", \"Method\"] }}}";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/v2/search").method(POST).bodyJson(data);
			Result result = route(req);
			assertEquals(OK, result.status());
			assertEquals("application/json", result.contentType());
			assertTrue(contentAsString(result).contains("result"));
			assertFalse(contentAsString(result).contains("failed"));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	@Ignore
	public void testSearchParams() {
		String json = "{\"id\": \"ekstep.composite-search.search\",\"ver\": \"3.0\","
				+ "\"params\": {\"msgid\": null}}";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/v3/search").method(POST).bodyJson(data);
			Result result = route(req);
			assertEquals(OK, result.status());
			assertEquals("application/json", result.contentType());
			assertTrue(contentAsString(result).contains("result"));
			assertFalse(contentAsString(result).contains("failed"));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	@Test
	@Ignore
	public void testSearchParamsException() {
		String json = "{\"id\": \"ekstep.composite-search.search\",\"ver\": \"3.0\","
				+ "\"params\": {\"msgid\": {\"test\": \" TEST_DEV\"}}}";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/v3/search").method(POST).bodyJson(data);
			Result result = route(req);
			assertEquals(OK, result.status());
			assertEquals("application/json", result.contentType());
			assertTrue(contentAsString(result).contains("result"));
			assertFalse(contentAsString(result).contains("failed"));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	@Ignore
	public void testSearchRequestException() {
		String json = "{\"request\": [\"object\",\"content\"]}";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/v3/search").method(POST).bodyJson(data);
			Result result = route(req);
			assertEquals(OK, result.status());
			assertEquals("application/json", result.contentType());
			assertTrue(contentAsString(result).contains("result"));
			assertFalse(contentAsString(result).contains("failed"));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSearchRequestInvalidLimitOrOffset() {
		String json = "{ \"request\": { \"mode\":\"soft\", \"filters\":{ \"identifier\": \"do_21271205218287616012235\",\"status\": [] }, \"offset\":\"haskjdha\", \"limit\":\"100\", \"fields\": [\"identifier\", \"status\", \"objectType\"] } }";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/v3/search").method(POST).bodyJson(data);
			Result result = route(req);
			assertEquals(BAD_REQUEST, result.status());
			assertEquals("application/json", result.contentType());
			assertTrue(contentAsString(result).contains("result"));
			assertTrue(contentAsString(result).contains("failed"));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSearchRequestValidLimitOffset() {
		String json = "{ \"request\": { \"mode\":\"soft\", \"filters\":{ \"identifier\": \"do_21271205218287616012235\",\"status\": [] }, \"offset\":\"20\", \"limit\":100, \"fields\": [\"identifier\", \"status\", \"objectType\"] } }";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/v3/search").method(POST).bodyJson(data);
			Result result = route(req);
			assertEquals(OK, result.status());
			assertEquals("application/json", result.contentType());
			assertTrue(contentAsString(result).contains("result"));
			assertTrue(contentAsString(result).contains("success"));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	@Test
	public void testCountV2() {
		String json = "{\"request\": {\"filters\":{\"objectType\":"
				+ " [\"Concept\", \"Word\", \"Domain\", \"Dimension\","
				+ " \"AssessmentItem\", \"Content\", \"Method\"] }}}";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/v2/search/count").method(POST)
					.bodyJson(data);
			Result result = route(req);
			assertEquals(OK, result.status());
			assertEquals("application/json", result.contentType());
			assertTrue(contentAsString(result).contains("count"));
			assertFalse(contentAsString(result).contains("failed"));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testMetricsV2() {
		String json = "{\"request\": {\"filters\":{\"objectType\":"
				+ " [\"Concept\", \"Word\", \"Domain\", \"Dimension\","
				+ " \"AssessmentItem\", \"Content\", \"Method\"] }}}";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/v2/metrics").method(POST).bodyJson(data);
			Result result = route(req);
			assertEquals(OK, result.status());
			assertEquals("application/json", result.contentType());
			assertTrue(contentAsString(result).contains("count"));
			assertFalse(contentAsString(result).contains("failed"));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testHealthCheckV2() {
		RequestBuilder req = new RequestBuilder().uri("/health").method(GET);
		Result result = route(req);
		assertEquals(OK, result.status());
		assertEquals("application/json", result.contentType());
		assertTrue(contentAsString(result).contains("healthy"));
		assertFalse(contentAsString(result).contains("failed"));
	}

	@Test
	public void testSearch() {
		String json = "{\"request\": {\"filters\":{\"objectType\":"
				+ " [\"Concept\", \"Word\", \"Domain\", \"Dimension\","
				+ " \"AssessmentItem\", \"Content\", \"Method\"] }}}";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/v3/search").method(POST).bodyJson(data);
			Result result = route(req);
			assertEquals(OK, result.status());
			assertEquals("application/json", result.contentType());
			assertTrue(contentAsString(result).contains("result"));
			assertFalse(contentAsString(result).contains("failed"));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testCount() {
		String json = "{\"request\": {\"filters\":{\"objectType\":"
				+ " [\"Concept\", \"Word\", \"Domain\", \"Dimension\","
				+ " \"AssessmentItem\", \"Content\", \"Method\"] }}}";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/v3/count").method(POST).bodyJson(data);
			Result result = route(req);
			assertEquals(OK, result.status());
			assertEquals("application/json", result.contentType());
			assertTrue(contentAsString(result).contains("count"));
			assertFalse(contentAsString(result).contains("failed"));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testMetrics() {
		String json = "{\"request\": {\"filters\":{\"objectType\":"
				+ " [\"Concept\", \"Word\", \"Domain\", \"Dimension\","
				+ " \"AssessmentItem\", \"Content\", \"Method\"] }}}";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/v3/metrics").method(POST).bodyJson(data);
			Result result = route(req);
			assertEquals(OK, result.status());
			assertEquals("application/json", result.contentType());
			assertTrue(contentAsString(result).contains("count"));
			assertFalse(contentAsString(result).contains("failed"));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testHealthCheck() {
		RequestBuilder req = new RequestBuilder().uri("/health").method(GET);
		Result result = route(req);
		assertEquals(OK, result.status());
		assertEquals("application/json", result.contentType());
		assertTrue(contentAsString(result).contains("healthy"));
		assertFalse(contentAsString(result).contains("failed"));
	}
}
