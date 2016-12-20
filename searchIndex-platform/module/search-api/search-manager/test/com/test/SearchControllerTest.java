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
		Result result = route(fakeRequest(POST, "/search"));
		assertEquals(OK, result.status());
		assertEquals("text/plain", result.contentType());
		assertEquals("utf-8", result.charset());
		assertTrue(contentAsString(result).contains("result"));
		assertFalse(contentAsString(result).contains("failed"));
	}
	
	@Test
	public void testCount() {
		Result result = route(fakeRequest(POST, "/count"));
		assertEquals(OK, result.status());
		assertEquals("text/plain", result.contentType());
		assertEquals("utf-8", result.charset());
		assertTrue(contentAsString(result).contains("count"));
		assertFalse(contentAsString(result).contains("failed"));
	}
}
