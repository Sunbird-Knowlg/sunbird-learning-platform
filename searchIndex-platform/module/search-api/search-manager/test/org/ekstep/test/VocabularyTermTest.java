/**
 * 
 */
package org.ekstep.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.INTERNAL_SERVER_ERROR;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.PARTIAL_CONTENT;
import static play.test.Helpers.POST;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.route;

import java.io.IOException;

import org.ekstep.common.Platform;
import org.ekstep.search.router.SearchRequestRouterPool;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import common.Constants;
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.WithApplication;

/**
 * @author pradyumna
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VocabularyTermTest extends WithApplication {

	private static ObjectMapper mapper = new ObjectMapper();
	private static final String VOCABULARY_TERM_INDEX = "testvocabularyterm";
	private static final String VOCABULARY_TERM_INDEX_TYPE = "vt";

	@BeforeClass
	public static void beforeTest() throws Exception {
		SearchRequestRouterPool.init();
		createTestIndex();
		Thread.sleep(3000);
	}

	@AfterClass
	public static void afterTest() throws Exception {
		System.out.println("deleting index: " + VOCABULARY_TERM_INDEX);
		ElasticSearchUtil.deleteIndex(VOCABULARY_TERM_INDEX);
	}

	private static void createTestIndex() throws Exception {
		Constants.VOCABULARY_TERM_INDEX = VOCABULARY_TERM_INDEX;
		ElasticSearchUtil.initialiseESClient(VOCABULARY_TERM_INDEX, Platform.config.getString("search.es_conn_info"));
		System.out.println("creating index: " + VOCABULARY_TERM_INDEX);
		String settings = "{\"analysis\":{\"analyzer\":{\"vt_index_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"standard\",\"filter\":[\"lowercase\",\"mynGram\"]},\"vt_search_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"standard\",\"filter\":[\"standard\",\"lowercase\"]},\"keylower\":{\"tokenizer\":\"keyword\",\"filter\":\"lowercase\"}},\"filter\":{\"mynGram\":{\"type\":\"edge_ngram\",\"min_gram\":1,\"max_gram\":20,\"token_chars\":[\"letter\",\"digit\",\"whitespace\",\"punctuation\",\"symbol\"]}}}}";
		String mappings = "{\"dynamic_templates\":[{\"longs\":{\"match_mapping_type\":\"long\",\"mapping\":{\"type\":\"long\",\"fields\":{\"raw\":{\"type\":\"long\"}}}}},{\"booleans\":{\"match_mapping_type\":\"boolean\",\"mapping\":{\"type\":\"boolean\",\"fields\":{\"raw\":{\"type\":\"boolean\"}}}}},{\"doubles\":{\"match_mapping_type\":\"double\",\"mapping\":{\"type\":\"double\",\"fields\":{\"raw\":{\"type\":\"double\"}}}}},{\"dates\":{\"match_mapping_type\":\"date\",\"mapping\":{\"type\":\"date\",\"fields\":{\"raw\":{\"type\":\"date\"}}}}},{\"strings\":{\"match_mapping_type\":\"string\",\"mapping\":{\"type\":\"text\",\"copy_to\":\"all_fields\",\"analyzer\":\"vt_index_analyzer\",\"search_analyzer\":\"vt_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\"}}}}}],\"properties\":{\"all_fields\":{\"type\":\"text\",\"analyzer\":\"vt_index_analyzer\",\"search_analyzer\":\"vt_search_analyzer\"}}}";
		ElasticSearchUtil.addIndex(VOCABULARY_TERM_INDEX, VOCABULARY_TERM_INDEX_TYPE, settings, mappings);
	}

	@Test
	public void testCreate() {
		String json = "{\"request\":{\"terms\":[{\"lemma\":\"add\",\"categories\":[\"keywords\"],\"language\":\"en\"},{\"lemma\":\"addition\",\"categories\":[\"keywords\"],\"language\":\"en\"},{\"lemma\":\"adding\",\"categories\":[\"keywords\"],\"language\":\"en\"},{\"lemma\":\"ಕೂಡಿಸು\",\"categories\":[\"keywords\"]},{\"lemma\":\"sub\",\"categories\":[\"keywords\"],\"language\":\"en\"},{\"lemma\":\"subtract\",\"categories\":[\"keywords\"],\"language\":\"en\"},{\"lemma\":\"ಕಳೆ\",\"categories\":[\"keywords\"]}]}}";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/vocabulary/v3/term/create").method(POST).bodyJson(data);
			Result result = route(req);
			assertEquals(OK, result.status());
			assertEquals("application/json", result.contentType());
			assertTrue(contentAsString(result).contains("identifiers"));
			assertFalse(!contentAsString(result).contains("identifiers"));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testCreateWithPartialResponse() {
		String json = "{\"request\":{\"terms\":[{\"lemma\":\"add\",\"categories\":[\"keywords\"],\"language\":\"cun\"},{\"lemma\":\"addition\",\"categories\":[\"keywords\"],\"language\":\"en\"},{\"lemma\":\"adding\",\"categories\":[\"keywords\"],\"language\":\"en\"},{\"lemma\":\"ಕೂಡಿಸು\",\"categories\":[\"keywords\"],\"language\":\"ka\"},{\"lemma\":\"sub\",\"categories\":[\"keywords\"],\"language\":\"en\"},{\"lemma\":\"subtract\",\"categories\":[\"keywords\"],\"language\":\"en\"},{\"lemma\":\"ಕಳೆ\",\"categories\":[\"keywords\"],\"language\":\"ka\"}]}}";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/vocabulary/v3/term/create").method(POST).bodyJson(data);
			Result result = route(req);
			assertEquals(PARTIAL_CONTENT, result.status());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testCreateWithInvalidRequest() {
		String json = "{\"request\":{\"terms\":[]}}";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/vocabulary/v3/term/create").method(POST).bodyJson(data);
			Result result = route(req);
			assertEquals(BAD_REQUEST, result.status());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testCreateWithInvalidRequest1() {
		String json = "{\"request\":{\"terms\":[{\"lemma\":\"add\",\"categories\":[\"keywords\"],\"language\":\"cun\"}]}}";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/vocabulary/v3/term/create").method(POST).bodyJson(data);
			Result result = route(req);
			assertEquals(BAD_REQUEST, result.status());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testzSuggest() {
		String json = "{\"request\":{\"text\" : \"add\", \"limit\":3}}";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/vocabulary/v3/term/suggest").method(POST)
					.bodyJson(data);
			Result result = route(req);
			assertEquals(OK, result.status());
			assertTrue(contentAsString(result).contains("\"count\":3"));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testzSuggest1() {
		String json = "{\"request\":{\"text\" : \"add\", \"categories\":[\"keywords\"], \"language\":\"en\", \"limit\":3}}";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/vocabulary/v3/term/suggest").method(POST)
					.bodyJson(data);
			Result result = route(req);
			assertEquals(OK, result.status());
			assertTrue(contentAsString(result).contains("\"count\":3"));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSuggest2() {
		String json = "{\"request\":{\"text\" : \"add\", \"categories\":[\"asd\"],\"language\":\"en\"}}";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/vocabulary/v3/term/suggest").method(POST)
					.bodyJson(data);
			Result result = route(req);
			assertEquals(OK, result.status());
			assertTrue(contentAsString(result).contains("\"count\":0"));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSuggestEndsWith() {
		String json = "{\"request\":{\"text\" : {\"endsWith\":\"dd\"}}}";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/vocabulary/v3/term/suggest").method(POST).bodyJson(data);
			Result result = route(req);
			assertEquals(OK, result.status());
			assertTrue(contentAsString(result).contains("count"));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSuggestStartsWith() {
		String json = "{\"request\":{\"text\" : {\"startsWith\":\"add\"}}}";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/vocabulary/v3/term/suggest").method(POST).bodyJson(data);
			Result result = route(req);
			assertEquals(OK, result.status());
			assertTrue(contentAsString(result).contains("count"));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSuggestNotEquals() {
		String json = "{\"request\":{\"text\" : {\"notEquals\":\"add\"}}}";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/vocabulary/v3/term/suggest").method(POST).bodyJson(data);
			Result result = route(req);
			assertEquals(OK, result.status());
			assertTrue(contentAsString(result).contains("count"));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSuggestInvalidOperation() {
		String json = "{\"request\":{\"text\" : {\"not\":\"add\"}}}";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/vocabulary/v3/term/suggest").method(POST).bodyJson(data);
			Result result = route(req);
			assertEquals(INTERNAL_SERVER_ERROR, result.status());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testzSuggest3() {
		String json = "{\"request\":{\"text\" : \"ಕಳೆ\", \"categories\":[\"keywords\"],\"language\":\"ka\"}}";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/vocabulary/v3/term/suggest").method(POST).bodyJson(data);
			Result result = route(req);
			assertEquals(OK, result.status());
			assertTrue(contentAsString(result).contains("\"count\":1"));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSuggestWithoutLimit() {
		String json = "{\"request\":{\"text\" : \"add\"}}";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/vocabulary/v3/term/suggest").method(POST).bodyJson(data);
			Result result = route(req);
			assertEquals(OK, result.status());
			assertTrue(contentAsString(result).contains("count"));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSuggestWithInvalidRequest() {
		String json = "{\"request\":{\"text\" : \"\"}}";
		try {
			JsonNode data = mapper.readTree(json);
			RequestBuilder req = new RequestBuilder().uri("/vocabulary/v3/term/suggest").method(POST)
					.bodyJson(data);
			Result result = route(req);
			assertEquals(BAD_REQUEST, result.status());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
