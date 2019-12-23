package org.ekstep.search.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.compositesearch.enums.SearchActorNames;
//import org.ekstep.search.router.SearchRequestRouterPool;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

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
		// request.put("limit", 1);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() > 1);
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
	public void testSearchByQueryForNotEquals() {
		Request request = getSearchRequest();
		request.put("query", "हिन्दी");
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("!=", "31 check name match");
		filters.put("name", map);
		request.put("filters", filters);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() > 0);
		boolean found = false;
		for (Object obj : list) {
			Map<String, Object> content = (Map<String, Object>) obj;
			String desc = (String) content.get("name");
			if (null != desc && !StringUtils.equalsIgnoreCase("31 check name match", desc))
				found = true;
		}
		Assert.assertTrue(found);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchByQueryForNotEqualsText() {
		Request request = getSearchRequest();
		request.put("query", "हिन्दी");
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("notEquals", "31 check name match");
		filters.put("name", map);
		request.put("filters", filters);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() > 0);
		boolean found = false;
		for (Object obj : list) {
			Map<String, Object> content = (Map<String, Object>) obj;
			String desc = (String) content.get("name");
			if (null != desc && !StringUtils.equalsIgnoreCase("31 check name match", desc))
				found = true;
		}
		Assert.assertTrue(found);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSearchByQueryForNotIn() {
		Request request = getSearchRequest();
		request.put("query", "हिन्दी");
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("notIn", Arrays.asList("31 check name match"));
		filters.put("name", map);
		request.put("filters", filters);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() > 0);
		boolean found = true;
		for (Object obj : list) {
			Map<String, Object> content = (Map<String, Object>) obj;
			String desc = (String) content.get("name");
			if (null != desc && StringUtils.equalsIgnoreCase("31 check name match", desc))
				found = false;
		}
		Assert.assertTrue(found);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSearchByQueryForNotEqualsTextUpperCase() {
		Request request = getSearchRequest();
		request.put("query", "हिन्दी");
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("NE", "31 check name match");
		filters.put("name", map);
		request.put("filters", filters);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() > 0);
		boolean found = false;
		for (Object obj : list) {
			Map<String, Object> content = (Map<String, Object>) obj;
			String desc = (String) content.get("name");
			if (null != desc && !StringUtils.equalsIgnoreCase("31 check name match", desc))
				found = true;
		}
		Assert.assertTrue(found);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchByQueryForNotEqualsTextLowerCase() {
		Request request = getSearchRequest();
		request.put("query", "हिन्दी");
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("ne", "31 check name match");
		filters.put("name", map);
		request.put("filters", filters);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() > 0);
		boolean found = false;
		for (Object obj : list) {
			Map<String, Object> content = (Map<String, Object>) obj;
			String desc = (String) content.get("name");
			if (null != desc && !StringUtils.equalsIgnoreCase("31 check name match", desc))
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
		Assert.assertTrue(list.size() == 34);
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
			Integer identifier = (Integer) content.get("size");
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
			Integer identifier = (Integer) content.get("size");
			if (null != identifier)
				Assert.assertTrue(identifier <= 564738);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchMinMaxFilter() {
		Request request = getSearchRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		Map<String, Object> name = new HashMap<String, Object>();
		name.put("min", 564737);
		name.put("max", 564739);
		filters.put("size", name);
		request.put("filters", filters);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		for (Object obj : list) {
			Map<String, Object> content = (Map<String, Object>) obj;
			Integer size = (Integer) content.get("size");
			if (null != size)
				Assert.assertTrue(size == 564738);
		}
	}

	@SuppressWarnings("unchecked")
	@Ignore
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
	@Ignore
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
	@Ignore
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
	public void testSearchValueFilter() {
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
		Assert.assertTrue(list.size() >= 1);
		for (Object obj : list) {
			Map<String, Object> content = (Map<String, Object>) obj;
			String identifier = (String) content.get("name");
			Assert.assertTrue(identifier.contains("31 check name match"));
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchContainsFilter() {
		Request request = getSearchRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		filters.put("status", new ArrayList<String>());
		Map<String, Object> name = new HashMap<String, Object>();
		name.put("contains", "check");
		filters.put("name", name);
		request.put("filters", filters);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() >= 1);
		for (Object obj : list) {
			Map<String, Object> content = (Map<String, Object>) obj;
			String identifier = (String) content.get("name");
			Assert.assertTrue(identifier.contains("check"));
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
		exists.add("objectType");
		request.put("exists", exists);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() >= 1);
		for (Object obj : list) {
			Map<String, Object> content = (Map<String, Object>) obj;
			String objectType = (String) content.get("objectType");
			Assert.assertNotNull(objectType);
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
		exists.add("contentType");
		request.put("facets", exists);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("facets");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() > 1);
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
		Assert.assertEquals("do_10000034", id1);
		
		Map<String, Object> content2 = (Map<String, Object>) list.get(1);
		String id2 = (String) content2.get("identifier");
		Assert.assertEquals("do_10000033", id2);
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
		Integer count = (Integer) result.get("count");
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
		Assert.assertTrue(count == 34);
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
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchByFields() {
		Request request = getSearchRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		List<String> status = new ArrayList<String>();
		status.add("Draft");
		filters.put("status",status);
		List<String> contentType = new ArrayList<String>();
		contentType.add("Story");
		contentType.add("Worksheet");
		filters.put("contentType",contentType);
		request.put("filters", filters);
		List<String> fields = new ArrayList<String>();
		fields.add("name");
		fields.add("code");
		request.put("fields", fields);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() > 0);
		boolean valid = false;
		for (Object obj : list) {
			Map<String, Object> contents = (Map<String, Object>) obj;
			Set<String> keys = contents.keySet();
			if (keys.size() == fields.size()) {
				if(keys.containsAll(fields)){
					valid = true;
					
				}
			}else {
				valid = false;
			}
			
		}
		Assert.assertTrue(valid);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchByOffset() {
		Request request = getSearchRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		filters.put("objectType", objectTypes);
		List<String> status = new ArrayList<String>();
		status.add("Draft");
		filters.put("status",status);
		List<String> contentType = new ArrayList<String>();
		contentType.add("Story");
		contentType.add("Worksheet");
		contentType.add("Game");
		contentType.add("Collection");
		filters.put("contentType",contentType);
		request.put("filters", filters);
		request.put("offset", 0);
		request.put("limit", 1);
		
		Request request1 = getSearchRequest();
		Map<String, Object> filters1 = new HashMap<String, Object>();
		List<String> objectTypes1 = new ArrayList<String>();
		objectTypes1.add("Content");
		filters1.put("objectType", objectTypes1);
		List<String> status1 = new ArrayList<String>();
		status1.add("Draft");
		filters1.put("status",status1);
		List<String> contentType1 = new ArrayList<String>();
		contentType1.add("Story");
		contentType.add("Game");
		contentType.add("Collection");

		contentType1.add("Worksheet");
		filters1.put("contentType",contentType1);
		request1.put("filters", filters1);
		request1.put("offset", 4);
		request1.put("limit", 1);
		Response response = getSearchResponse(request);
		Response response1 = getSearchResponse(request1);
		Map<String, Object> result = response.getResult();
		Map<String, Object> result1 = response1.getResult();
		List<Object> list = (List<Object>) result.get("results");
		List<Object> list1 = (List<Object>) result1.get("results");
		Assert.assertNotNull(list);
		Assert.assertNotNull(list1);
		Assert.assertTrue(list.size() > 0);
		Assert.assertTrue(list1.size() > 0);
		boolean validResult = false;
		if(list!=list1){
			validResult = true;
		}
		Assert.assertTrue(validResult);
	}

	@Test
	public void testSoftConstraints() {
		Request request = getSearchRequest();
		Map<String, Object> filters = new HashMap<String, Object>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("notEquals", "31 check name match");
		filters.put("name", map);
		request.put("filters", filters);
		request.put("mode", "soft");
		List<String> fields = new ArrayList<String>();
		fields.add("name");
		fields.add("medium");
		fields.add("subject");
		fields.add("contentType");
		request.put("fields", fields);
		Map<String, Object> softConstraints = new HashMap<String, Object>();
		softConstraints.put("name", 100);
		softConstraints.put("subject", 20);
		request.put("softConstraints", softConstraints);
		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		Assert.assertNotNull(list);
	}


	@Test
	public void testSearchImplicitFilter() {
		Request request = getSearchRequest();
		List<String> objectTypes = new ArrayList<String>();
		objectTypes.add("Content");
		request.put("filters", new HashMap<String, Object>() {{
			put("status", new ArrayList<String>());
			put("objectType", objectTypes);
			put("board", "test-board1");
		}});

		Response response = getSearchResponse(request);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("results");
		System.out.println("list of result : " + list);
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() == 2);
		boolean found = false;
		boolean foundOriginal = false;
		for (Object obj : list) {
			Map<String, Object> content = (Map<String, Object>) obj;
			List<String> relatedBoards = (List<String>) content.get("relatedBoards");
			if (CollectionUtils.isNotEmpty(relatedBoards) && StringUtils.equalsIgnoreCase("do_10000034", (String) content.get("identifier"))
					&& relatedBoards.contains("test-board1"))
				found = true;
			if (StringUtils.equalsIgnoreCase("do_10000033", (String) content.get("identifier")) && StringUtils.equalsIgnoreCase("test-board1", (String) content.get("board")))
			foundOriginal = true;
		}
		Assert.assertTrue(found);
		Assert.assertTrue(foundOriginal);
	}

}