package org.ekstep.search.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

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
	
	@Test
	public void testSearchArrayFilter() {
	}
	
	@Test
	public void testSearchStringValueFilter() {
	}
	
	@Test
	public void testSearchStartsWithFilter() {
	}
	
	@Test
	public void testSearchEndsWithFilter() {
	}
	
	@Test
	public void testSearchMinFilter() {
	}
	
	@Test
	public void testSearchMaxFilter() {
	}
	
	@Test
	public void testSearchLTFilter() {
	}
	
	@Test
	public void testSearchGTFilter() {
	}
	
	@Test
	public void testSearchValueFilter() {
	}
	
	@Test
	public void testSearchExistsCondition() {
	}
	
	@Test
	public void testSearchNotExistsCondition() {
	}
	
	@Test
	public void testSearchFacets() {
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
		System.out.println(result);
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
}
