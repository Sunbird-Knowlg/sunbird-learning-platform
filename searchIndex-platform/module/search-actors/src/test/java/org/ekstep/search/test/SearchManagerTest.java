package org.ekstep.search.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ekstep.compositesearch.enums.SearchActorNames;
import org.junit.Assert;
import org.junit.Test;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ResponseCode;

/**
 * @author rayulu
 *
 */
public class SearchManagerTest extends BaseSearchActorsTest {

	@SuppressWarnings("unchecked")
	@Test
	public void testSearchByQuery() {
		Request request = getSearchRequest();
		request.put("query", "हिन्दी");
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		request.put("filters", filters);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() > 0);
		boolean found = false;
		for (Object obj : list) {
			Map<String, Object> content = (Map<String, Object>) obj;
			String desc = (String) content.get("description");
			if (null != desc && desc.contains("हिन्दी"))
				found = true;
		}
		Assert.assertTrue(found);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchByQueryFields() {
		Request request = getSearchRequest();
		request.put("query", "हिन्दी");
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		request.put("filters", filters);
		List<String> fields = new ArrayList<String>();
		fields.add("description");
		request.put("fields", fields);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() > 0);
		boolean found = false;
		for (Object obj : list) {
			Map<String, Object> content = (Map<String, Object>) obj;
			String desc = (String) content.get("description");
			if (null != desc && desc.contains("हिन्दी"))
				found = true;
		}
		Assert.assertTrue(found);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchFilters() {
		Request request = getSearchRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		request.put("filters", filters);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() == 32);
		for (Object obj : list) {
			Map<String, Object> content = (Map<String, Object>) obj;
			String objectType = (String) content.get("objectType");
			Assert.assertEquals("Content", objectType);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchArrayFilter() {
		Request request = getSearchRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		List<String> names = new ArrayList<String>();
		names.add("31 check name match");
		names.add("check ends with value32");
		filters.put("name", names);
		request.put("filters", filters);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() == 2);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchStringValueFilter() {
		Request request = getSearchRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		Map<String, Object> name = new HashMap<String, Object>();
		name.put("value", "31 check name match");
		filters.put("name", name);
		request.put("filters", filters);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() == 1);
		Map<String, Object> content = (Map<String, Object>) list.get(0);
		String identifier = (String) content.get("identifier");
		Assert.assertEquals("do_10000031", identifier);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchStartsWithFilter() {
		Request request = getSearchRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		Map<String, Object> name = new HashMap<String, Object>();
		name.put("startsWith", "31 check");
		filters.put("name", name);
		request.put("filters", filters);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() == 1);
		Map<String, Object> content = (Map<String, Object>) list.get(0);
		String identifier = (String) content.get("identifier");
		Assert.assertEquals("do_10000031", identifier);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchEndsWithFilter() {
		Request request = getSearchRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		Map<String, Object> name = new HashMap<String, Object>();
		name.put("endsWith", "value32");
		filters.put("name", name);
		request.put("filters", filters);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() == 1);
		Map<String, Object> content = (Map<String, Object>) list.get(0);
		String identifier = (String) content.get("identifier");
		Assert.assertEquals("do_10000032", identifier);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchMinFilter() {
		Request request = getSearchRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		Map<String, Object> name = new HashMap<String, Object>();
		name.put("min", 1000432);
		filters.put("size", name);
		request.put("filters", filters);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() >= 1);
		for (Object obj : list) {
			Map<String, Object> content = (Map<String, Object>) obj;
			Double identifier = (Double) content.get("size");
			if (null != identifier)
				Assert.assertTrue(identifier >= 1000432);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchMaxFilter() {
		Request request = getSearchRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		Map<String, Object> name = new HashMap<String, Object>();
		name.put("max", 564738);
		filters.put("size", name);
		request.put("filters", filters);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() >= 1);
		for (Object obj : list) {
			Map<String, Object> content = (Map<String, Object>) obj;
			Double identifier = (Double) content.get("size");
			if (null != identifier)
				Assert.assertTrue(identifier <= 564738);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchLTFilter() {
		Request request = getSearchRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		Map<String, Object> name = new HashMap<String, Object>();
		name.put("<", 1000432);
		filters.put("size", name);
		request.put("filters", filters);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() >= 1);
		for (Object obj : list) {
			Map<String, Object> content = (Map<String, Object>) obj;
			Double identifier = (Double) content.get("size");
			if (null != identifier)
				Assert.assertTrue(identifier < 1000432);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchLEGEFilter() {
		Request request = getSearchRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		Map<String, Object> name = new HashMap<String, Object>();
		name.put("<=", 1000432);
		name.put(">=", 1000432);
		filters.put("size", name);
		request.put("filters", filters);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() >= 1);
		for (Object obj : list) {
			Map<String, Object> content = (Map<String, Object>) obj;
			Double identifier = (Double) content.get("size");
			if (null != identifier)
				Assert.assertTrue(identifier == 1000432);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchGTFilter() {
		Request request = getSearchRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		Map<String, Object> name = new HashMap<String, Object>();
		name.put(">", 564738);
		filters.put("size", name);
		request.put("filters", filters);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() >= 1);
		for (Object obj : list) {
			Map<String, Object> content = (Map<String, Object>) obj;
			Double identifier = (Double) content.get("size");
			if (null != identifier)
				Assert.assertTrue(identifier > 564738);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchNumericValueFilter() {
		Request request = getSearchRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		Map<String, Object> name = new HashMap<String, Object>();
		name.put("value", 564738);
		filters.put("size", name);
		request.put("filters", filters);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() >= 1);
		for (Object obj : list) {
			Map<String, Object> content = (Map<String, Object>) obj;
			Double identifier = (Double) content.get("size");
			Assert.assertTrue(identifier == 564738);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchExistsCondition() {
		Request request = getSearchRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		request.put("filters", filters);
		List<String> exists = new ArrayList<String>();
		exists.add("size");
		request.put("exists", exists);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() >= 1);
		for (Object obj : list) {
			Map<String, Object> content = (Map<String, Object>) obj;
			Double size = (Double) content.get("size");
			Assert.assertNotNull(size);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchNotExistsCondition() {
		Request request = getSearchRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		request.put("filters", filters);
		List<String> exists = new ArrayList<String>();
		exists.add("size");
		request.put("not_exists", exists);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() >= 1);
		for (Object obj : list) {
			Map<String, Object> content = (Map<String, Object>) obj;
			Double size = (Double) content.get("size");
			Assert.assertNull(size);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchFacets() {
		Request request = getSearchRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		request.put("filters", filters);
		List<String> exists = new ArrayList<String>();
		exists.add("size");
		request.put("facets", exists);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("facets");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() == 1);
		Map<String, Object> facet = (Map<String, Object>) list.get(0);
		Assert.assertEquals("size", facet.get("name").toString());
		List<Object> values = (List<Object>) facet.get("values");
		Assert.assertEquals(2, values.size());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchLimit() {
		Request request = getSearchRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		request.put("filters", filters);
		request.put("limit", 10);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() == 10);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchEmptyResult() {
		Request request = getSearchRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("InvalidObjectType");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		request.put("filters", filters);
		request.put("limit", 10);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertTrue(null == list || list.size() == 0);
		Integer count = (Integer) result.get("count");
		Assert.assertTrue(count == 0);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchSortAsc() {
		Request request = getSearchRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		request.put("filters", filters);
		request.put("limit", 5);
		Map<String, Object> sort = new HashMap<String, Object>();
		sort.put("identifier", "asc");
		request.put("sort_by", sort);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() == 5);
		Map<String, Object> content1 = (Map<String, Object>) list.get(0);
		String id1 = (String) content1.get("identifier");
		Assert.assertEquals("do_10000001", id1);
		
		Map<String, Object> content5 = (Map<String, Object>) list.get(4);
		String id5 = (String) content5.get("identifier");
		Assert.assertEquals("do_10000005", id5);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchSortDesc() {
		Request request = getSearchRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		request.put("filters", filters);
		request.put("limit", 2);
		Map<String, Object> sort = new HashMap<String, Object>();
		sort.put("identifier", "desc");
		request.put("sort_by", sort);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() == 2);
		Map<String, Object> content1 = (Map<String, Object>) list.get(0);
		String id1 = (String) content1.get("identifier");
		Assert.assertEquals("do_10000032", id1);
		
		Map<String, Object> content2 = (Map<String, Object>) list.get(1);
		String id2 = (String) content2.get("identifier");
		Assert.assertEquals("do_10000031", id2);
	}
	
	@Test
	public void testSearchCount() {
		Request request = getCountRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		request.put("filters", filters);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		Double count = (Double) result.get("count");
		Assert.assertNotNull(count);
	}
	
	@Test
	public void testSearchMetrics() {
		Request request = getMetricsRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		request.put("filters", filters);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		Integer count = (Integer) result.get("count");
		Assert.assertTrue(count == 32);
	}
	
	@Test
	public void testGroupSearchResults() {
		Request request = getSearchRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		request.put("filters", filters);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		
		Request req = getGroupSearchResultsRequest();
		req.put("searchResult", result);
		Response resp = getSearchResponse(req);
		result = resp.getResult();
		Assert.assertNotNull(result.get("content"));
	}
	
	@Test
	public void testUnSupportedException() {
		Request request = new Request();
		setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name(), "Invalid Operation");
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		request.put("filters", filters);
		Response response = getSearchResponse(request);
		Assert.assertEquals("failed", response.getParams().getStatus());
		Assert.assertEquals(ResponseCode.CLIENT_ERROR.code(), response.getResponseCode().code());
	}
}
