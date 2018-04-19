/**
 * 
 */
package org.ekstep.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.compositesearch.enums.CompositeSearchParams;
import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.processor.SearchProcessor;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author pradyumna
 *
 */
public class SearchProcessorTest {
	private static ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();
	private static ObjectMapper mapper = new ObjectMapper();
	private static SearchProcessor searchprocessor = new SearchProcessor();

	@BeforeClass
	public static void beforeTest() throws Exception {
		createCompositeSearchIndex();
		Thread.sleep(3000);
	}

	@AfterClass
	public static void afterTest() throws Exception {
		System.out.println("deleting index: " + CompositeSearchConstants.COMPOSITE_SEARCH_INDEX);
		elasticSearchUtil.deleteIndex(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX);
	}

	private static void createCompositeSearchIndex() throws Exception {
		CompositeSearchConstants.COMPOSITE_SEARCH_INDEX = "testindex";
		System.out.println("creating index: " + CompositeSearchConstants.COMPOSITE_SEARCH_INDEX);
		String settings = "{\"analysis\": {       \"analyzer\": {         \"cs_index_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"lowercase\",             \"mynGram\"           ]         },         \"cs_search_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"standard\",             \"lowercase\"           ]         },         \"keylower\": {           \"tokenizer\": \"keyword\",           \"filter\": \"lowercase\"         }       },       \"filter\": {         \"mynGram\": {           \"type\": \"nGram\",           \"min_gram\": 1,           \"max_gram\": 20,           \"token_chars\": [             \"letter\",             \"digit\",             \"whitespace\",             \"punctuation\",             \"symbol\"           ]         }       }     }   }";
		String mappings = "{\"dynamic_templates\":[{\"longs\":{\"match_mapping_type\":\"long\",\"mapping\":{\"type\":\"long\",\"fields\":{\"raw\":{\"type\":\"long\"}}}}},{\"booleans\":{\"match_mapping_type\":\"boolean\",\"mapping\":{\"type\":\"boolean\",\"fields\":{\"raw\":{\"type\":\"boolean\"}}}}},{\"doubles\":{\"match_mapping_type\":\"double\",\"mapping\":{\"type\":\"double\",\"fields\":{\"raw\":{\"type\":\"double\"}}}}},{\"dates\":{\"match_mapping_type\":\"date\",\"mapping\":{\"type\":\"date\",\"fields\":{\"raw\":{\"type\":\"date\"}}}}},{\"strings\":{\"match_mapping_type\":\"string\",\"mapping\":{\"type\":\"text\",\"copy_to\":\"all_fields\",\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"text\",\"fielddata\":\"true\",\"analyzer\":\"keylower\"}}}}}],\"properties\":{\"all_fields\":{\"type\":\"text\",\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"text\",\"fielddata\":\"true\",\"analyzer\":\"keylower\"}}}}}";
		elasticSearchUtil.addIndex(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, settings, mappings);
		elasticSearchUtil.setResultLimit(10000);
		elasticSearchUtil.setOffset(0);
		insertTestRecords();
	}

	private static void insertTestRecords() throws Exception {
		for (int i = 1; i <= 30; i++) {
			Map<String, Object> content = getContentTestRecord(null, i);
			String id = (String) content.get("identifier");
			addToIndex(id, content);
		}
		Map<String, Object> content = getContentTestRecord("do_10000031", 31);
		content.put("name", "31 check name match");
		content.put("description", "हिन्दी description");
		addToIndex("do_10000031", content);

		content = getContentTestRecord("do_10000032", 32);
		content.put("name", "check ends with value32");
		addToIndex("do_10000032", content);
	}

	private static void addToIndex(String uniqueId, Map<String, Object> doc) throws Exception {
		String jsonIndexDocument = mapper.writeValueAsString(doc);
		elasticSearchUtil.addDocumentWithId(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, uniqueId, jsonIndexDocument);
	}

	private static Map<String, Object> getContentTestRecord(String id, int index) {
		String objectType = "Content";
		Date d = new Date();
		Map<String, Object> map = getTestRecord(id, index, "do", objectType);
		map.put("name", "Content_" + System.currentTimeMillis() + "_name");
		map.put("code", "code_" + System.currentTimeMillis());
		map.put("contentType", getContentType());
		map.put("createdOn", new Date().toString());
		map.put("lastUpdatedOn", new Date().toString());
		if (index % 5 == 0) {
			map.put("lastPublishedOn", d.toString());
			map.put("status", "Live");
			map.put("size", 1000432);
		} else {
			map.put("status", "Draft");
			if (index % 3 == 0)
				map.put("size", 564738);
		}
		Set<String> tagList = getTags();
		if (null != tagList && !tagList.isEmpty() && index % 7 != 0)
			map.put("tags", tagList);
		map.put("downloads", index);
		return map;
	}

	private static Map<String, Object> getTestRecord(String id, int index, String prefix, String objectType) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (StringUtils.isNotBlank(id))
			map.put("identifier", id);
		else {
			long suffix = 10000000 + index;
			map.put("identifier", prefix + "_" + suffix);
		}
		map.put("objectType", objectType);
		return map;
	}

	private static String[] contentTypes = new String[] { "Story", "Worksheet", "Game", "Collection", "Asset" };

	private static String getContentType() {
		return contentTypes[RandomUtils.nextInt(5)];
	}

	private static String[] tags = new String[] { "hindi story", "NCERT", "Pratham", "एकस्टेप", "हिन्दी",
			"हाथी और भालू", "worksheet", "test" };

	private static Set<String> getTags() {
		Set<String> list = new HashSet<String>();
		int count = RandomUtils.nextInt(9);
		for (int i = 0; i < count; i++) {
			list.add(tags[RandomUtils.nextInt(8)]);
		}
		return list;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testSearchByQuery() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_LIKE);
		property.put(CompositeSearchParams.propertyName.name(), "*");
		property.put(CompositeSearchParams.values.name(), Arrays.asList("हिन्दी"));
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setLimit(100);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() >= 1);
		boolean found = false;
		for (Object obj : results) {
			Map<String, Object> content = (Map<String, Object>) obj;
			String desc = (String) content.get("description");
			if (null != desc && desc.contains("हिन्दी"))
				found = true;
		}
		Assert.assertTrue(found);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testSearchByQueryForNotEquals() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_LIKE);
		property.put(CompositeSearchParams.propertyName.name(), "*");
		property.put(CompositeSearchParams.values.name(), Arrays.asList("हिन्दी"));
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("31 check name match"));
		property.put(CompositeSearchParams.propertyName.name(), "name");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_NOT_EQUAL);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setLimit(100);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() > 0);
		boolean found = false;
		for (Object obj : results) {
			Map<String, Object> content = (Map<String, Object>) obj;
			String desc = (String) content.get("name");
			if (null != desc && !StringUtils.equalsIgnoreCase("31 check name match", desc))
				found = true;
		}
		Assert.assertTrue(found);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testSearchByQueryForNotIn() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_LIKE);
		property.put(CompositeSearchParams.propertyName.name(), "*");
		property.put(CompositeSearchParams.values.name(), Arrays.asList("हिन्दी"));
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("31 check name match"));
		property.put(CompositeSearchParams.propertyName.name(), "name");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_NOT_IN);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setLimit(100);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() > 0);
		boolean found = true;
		for (Object obj : results) {
			Map<String, Object> content = (Map<String, Object>) obj;
			String desc = (String) content.get("name");
			if (null != desc && StringUtils.equalsIgnoreCase("31 check name match", desc))
				found = false;
		}
		Assert.assertTrue(found);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testSearchByQueryFields() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_LIKE);
		property.put(CompositeSearchParams.propertyName.name(), "*");
		property.put(CompositeSearchParams.values.name(), Arrays.asList("हिन्दी"));
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setLimit(100);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		List<String> fields = new ArrayList<String>();
		fields.add("description");
		searchObj.setFields(fields);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() > 0);
		boolean found = false;
		for (Object obj : results) {
			Map<String, Object> content = (Map<String, Object>) obj;
			String desc = (String) content.get("description");
			if (null != desc && desc.contains("हिन्दी"))
				found = true;
		}
		Assert.assertTrue(found);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testSearchArrayFilter() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		List<String> names = new ArrayList<String>();
		names.add("31 check name match");
		names.add("check ends with value32");
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), names);
		property.put(CompositeSearchParams.propertyName.name(), "name");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setLimit(100);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() == 2);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testSearchStartsWithFilter() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), "31 check");
		property.put(CompositeSearchParams.propertyName.name(), "name");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_STARTS_WITH);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setLimit(100);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() == 1);
		Map<String, Object> content = (Map<String, Object>) results.get(0);
		String identifier = (String) content.get("identifier");
		Assert.assertEquals("do_10000031", identifier);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testSearchEndsWithFilter() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), "value32");
		property.put(CompositeSearchParams.propertyName.name(), "name");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_ENDS_WITH);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setLimit(100);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() == 1);
		Map<String, Object> content = (Map<String, Object>) results.get(0);
		String identifier = (String) content.get("identifier");
		Assert.assertEquals("do_10000032", identifier);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testSearchLTFilter() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), 1000432);
		property.put(CompositeSearchParams.propertyName.name(), "size");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_LESS_THAN);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setLimit(100);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() >= 1);
		for (Object obj : results) {
			Map<String, Object> content = (Map<String, Object>) obj;
			Integer identifier = (Integer) content.get("size");
			if (null != identifier)
				Assert.assertTrue(identifier < 1000432);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testSearchLEGEFilter() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), 1000432);
		property.put(CompositeSearchParams.propertyName.name(), "size");
		property.put(CompositeSearchParams.operation.name(),
				CompositeSearchConstants.SEARCH_OPERATION_LESS_THAN_EQUALS);
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), 1000432);
		property.put(CompositeSearchParams.propertyName.name(), "size");
		property.put(CompositeSearchParams.operation.name(),
				CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN_EQUALS);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setLimit(100);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() >= 1);
		for (Object obj : results) {
			Map<String, Object> content = (Map<String, Object>) obj;
			Integer identifier = (Integer) content.get("size");
			if (null != identifier)
				Assert.assertTrue(identifier == 1000432);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testSearchGTFilter() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), 564738);
		property.put(CompositeSearchParams.propertyName.name(), "size");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setLimit(100);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() >= 1);
		for (Object obj : results) {
			Map<String, Object> content = (Map<String, Object>) obj;
			Integer identifier = (Integer) content.get("size");
			if (null != identifier)
				Assert.assertTrue(identifier > 564738);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testSearchContainsFilter() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), "check");
		property.put(CompositeSearchParams.propertyName.name(), "name");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_CONTAINS);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setLimit(100);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() >= 1);
		for (Object obj : results) {
			Map<String, Object> content = (Map<String, Object>) obj;
			String identifier = (String) content.get("name");
			Assert.assertTrue(identifier.contains("check"));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testSearchExistsCondition() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), "objectType");
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EXISTS);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setLimit(100);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() >= 1);
		for (Object obj : results) {
			Map<String, Object> content = (Map<String, Object>) obj;
			String objectType = (String) content.get("objectType");
			Assert.assertNotNull(objectType);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testSoftConstraints() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		searchObj.setProperties(properties);
		List<String> fields = new ArrayList<String>();
		fields.add("name");
		fields.add("medium");
		fields.add("subject");
		fields.add("contentType");
		searchObj.setFields(fields);
		Map<String, Object> softConstraints = new HashMap<String, Object>();
		softConstraints.put("name",
				Arrays.asList(100, Arrays.asList("31 check name match", "check ends with value32")));
		searchObj.setSoftConstraints(softConstraints);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		searchObj.setLimit(100);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertEquals("31 check name match", results.get(0).get("name"));
		Assert.assertEquals("check ends with value32", results.get(1).get("name"));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testSearchFacets() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		searchObj.setProperties(properties);
		List<String> exists = new ArrayList<String>();
		exists.add("size");
		exists.add("contentType");
		searchObj.setFacets(exists);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		searchObj.setLimit(100);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Object> list = (List<Object>) response.get("facets");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() > 1);
		Map<String, Object> facet = (Map<String, Object>) list.get(0);
		Assert.assertEquals("size", facet.get("name").toString());
		List<Object> values = (List<Object>) facet.get("values");
		Assert.assertEquals(2, values.size());

	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testSearchCount() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		searchObj.setLimit(100);
		Map<String, Object> response = searchprocessor.processCount(searchObj);
		Integer count = (Integer) response.get("count");
		Assert.assertNotNull(count);

	}

	/**
	 * 
	 */

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testFuzzySearchByQuery() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_LIKE);
		property.put(CompositeSearchParams.propertyName.name(), "*");
		property.put(CompositeSearchParams.values.name(), Arrays.asList("हिन्दी"));
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setLimit(100);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		searchObj.setFuzzySearch(true);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() >= 1);
		boolean found = false;
		for (Object obj : results) {
			Map<String, Object> content = (Map<String, Object>) obj;
			String desc = (String) content.get("description");
			if (null != desc && desc.contains("हिन्दी"))
				found = true;
		}
		Assert.assertTrue(found);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testFuzzySearchByQueryForNotEquals() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_LIKE);
		property.put(CompositeSearchParams.propertyName.name(), "*");
		property.put(CompositeSearchParams.values.name(), Arrays.asList("हिन्दी"));
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("31 check name match"));
		property.put(CompositeSearchParams.propertyName.name(), "name");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_NOT_EQUAL);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setLimit(100);
		searchObj.setFuzzySearch(true);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() > 0);
		boolean found = false;
		for (Object obj : results) {
			Map<String, Object> content = (Map<String, Object>) obj;
			String desc = (String) content.get("name");
			if (null != desc && !StringUtils.equalsIgnoreCase("31 check name match", desc))
				found = true;
		}
		Assert.assertTrue(found);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testFuzzySearchByQueryForNotIn() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_LIKE);
		property.put(CompositeSearchParams.propertyName.name(), "*");
		property.put(CompositeSearchParams.values.name(), Arrays.asList("हिन्दी"));
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("31 check name match"));
		property.put(CompositeSearchParams.propertyName.name(), "name");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_NOT_IN);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setLimit(100);
		searchObj.setFuzzySearch(true);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() > 0);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testFuzzySearchByQueryFields() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_LIKE);
		property.put(CompositeSearchParams.propertyName.name(), "*");
		property.put(CompositeSearchParams.values.name(), Arrays.asList("हिन्दी"));
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setLimit(100);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		List<String> fields = new ArrayList<String>();
		fields.add("description");
		searchObj.setFields(fields);
		searchObj.setFuzzySearch(true);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() > 0);
		boolean found = false;
		for (Object obj : results) {
			Map<String, Object> content = (Map<String, Object>) obj;
			String desc = (String) content.get("description");
			if (null != desc && desc.contains("हिन्दी"))
				found = true;
		}
		Assert.assertTrue(found);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testFuzzySearchArrayFilter() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		List<String> names = new ArrayList<String>();
		names.add("31 check name match");
		names.add("check ends with value32");
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), names);
		property.put(CompositeSearchParams.propertyName.name(), "name");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setLimit(100);
		searchObj.setFuzzySearch(true);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() >= 2);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testFuzzySearchStartsWithFilter() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), "31 check");
		property.put(CompositeSearchParams.propertyName.name(), "name");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_STARTS_WITH);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setLimit(100);
		searchObj.setFuzzySearch(true);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() >= 1);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testFuzzySearchEndsWithFilter() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), "value32");
		property.put(CompositeSearchParams.propertyName.name(), "name");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_ENDS_WITH);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setLimit(100);
		searchObj.setFuzzySearch(true);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() >= 1);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testFuzzySearchLTFilter() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), 1000432);
		property.put(CompositeSearchParams.propertyName.name(), "size");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_LESS_THAN);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setLimit(100);
		searchObj.setFuzzySearch(true);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() >= 1);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testFuzzySearchLEGEFilter() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), 1000432);
		property.put(CompositeSearchParams.propertyName.name(), "size");
		property.put(CompositeSearchParams.operation.name(),
				CompositeSearchConstants.SEARCH_OPERATION_LESS_THAN_EQUALS);
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), 1000432);
		property.put(CompositeSearchParams.propertyName.name(), "size");
		property.put(CompositeSearchParams.operation.name(),
				CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN_EQUALS);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setLimit(100);
		searchObj.setFuzzySearch(true);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() >= 1);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testFuzzySearchGTFilter() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), 564738);
		property.put(CompositeSearchParams.propertyName.name(), "size");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setLimit(100);
		searchObj.setFuzzySearch(true);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() >= 1);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testFuzzySearchContainsFilter() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), "check");
		property.put(CompositeSearchParams.propertyName.name(), "name");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_CONTAINS);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setLimit(100);
		searchObj.setFuzzySearch(true);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() >= 1);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testFuzzySearchExistsCondition() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), "objectType");
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EXISTS);
		properties.add(property);
		searchObj.setProperties(properties);
		searchObj.setLimit(100);
		searchObj.setFuzzySearch(true);
		searchObj.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);
		Assert.assertTrue(results.size() >= 1);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testFuzzySoftConstraints() throws Exception {
		SearchDTO searchObj = new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);
		searchObj.setProperties(properties);
		List<String> fields = new ArrayList<String>();
		fields.add("name");
		fields.add("medium");
		fields.add("subject");
		fields.add("contentType");
		searchObj.setFields(fields);
		Map<String, Object> softConstraints = new HashMap<String, Object>();
		softConstraints.put("name", Arrays.asList(100, "31 check name match"));
		searchObj.setSoftConstraints(softConstraints);
		searchObj.setLimit(100);
		searchObj.setFuzzySearch(true);
		Map<String, Object> response = searchprocessor.processSearch(searchObj, true);
		List<Map> results = (List<Map>) response.get("results");
		Assert.assertNotNull(results);

	}
}
