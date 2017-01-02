package com.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.POST;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;

import org.junit.Test;

import play.mvc.Result;
import play.test.WithApplication;

public class SearchControllerTest extends WithApplication {

	@Test
	public void testSearch() {
		Result result = route(fakeRequest(POST, "/v3/search"));
		assertEquals(OK, result.status());
		assertEquals("application/json", result.contentType());
		assertTrue(contentAsString(result).contains("result"));
		assertFalse(contentAsString(result).contains("failed"));
	}

	@Test
	public void testCount() {
		Result result = route(fakeRequest(POST, "/v3/count"));
		assertEquals(OK, result.status());
		assertEquals("application/json", result.contentType());
		assertTrue(contentAsString(result).contains("count"));
		assertFalse(contentAsString(result).contains("failed"));
	}
	
	@Test
	public void testMetrics() {
		Result result = route(fakeRequest(POST, "/v3/metrics"));
		assertEquals(OK, result.status());
		assertEquals("application/json", result.contentType());
		assertTrue(contentAsString(result).contains("count"));
		assertFalse(contentAsString(result).contains("failed"));
	}
}
