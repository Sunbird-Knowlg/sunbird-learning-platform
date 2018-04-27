/**
 * 
 */
package org.ekstep.test;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.compositesearch.enums.CompositeSearchParams;
import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.processor.SearchProcessor;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author pradyumna
 *
 */
public class ContentBadgingTest {

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
		CompositeSearchConstants.COMPOSITE_SEARCH_INDEX = "testbadge";
		System.out.println("creating index: " + CompositeSearchConstants.COMPOSITE_SEARCH_INDEX);
		String settings = "{\"analysis\":{\"analyzer\":{\"cs_index_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"standard\",\"filter\":[\"lowercase\",\"mynGram\"]},\"cs_search_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"standard\",\"filter\":[\"standard\",\"lowercase\"]},\"keylower\":{\"tokenizer\":\"keyword\",\"filter\":\"lowercase\"}},\"filter\":{\"mynGram\":{\"type\":\"nGram\",\"min_gram\":1,\"max_gram\":20,\"token_chars\":[\"letter\",\"digit\",\"whitespace\",\"punctuation\",\"symbol\"]}}}}";
		String mappings = "{\"dynamic_templates\":[{\"nested\":{\"match_mapping_type\":\"object\",\"mapping\":{\"type\":\"nested\",\"fields\":{\"type\":\"nested\"}}}},{\"longs\":{\"match_mapping_type\":\"long\",\"mapping\":{\"type\":\"long\",\"fields\":{\"raw\":{\"type\":\"long\"}}}}},{\"booleans\":{\"match_mapping_type\":\"boolean\",\"mapping\":{\"type\":\"boolean\",\"fields\":{\"raw\":{\"type\":\"boolean\"}}}}},{\"doubles\":{\"match_mapping_type\":\"double\",\"mapping\":{\"type\":\"double\",\"fields\":{\"raw\":{\"type\":\"double\"}}}}},{\"dates\":{\"match_mapping_type\":\"date\",\"mapping\":{\"type\":\"date\",\"fields\":{\"raw\":{\"type\":\"date\"}}}}},{\"strings\":{\"match_mapping_type\":\"string\",\"mapping\":{\"type\":\"text\",\"copy_to\":\"all_fields\",\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}}}}}],\"properties\":{\"all_fields\":{\"type\":\"text\",\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\"}}}}}";
		elasticSearchUtil.addIndex(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, settings, mappings);
		elasticSearchUtil.setResultLimit(10000);
		elasticSearchUtil.setOffset(0);
		insertDoc();
	}

	private static void addToIndex(String uniqueId, Map<String, Object> doc) throws Exception {
		String jsonIndexDocument = mapper.writeValueAsString(doc);
		elasticSearchUtil.addDocumentWithId(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, uniqueId, jsonIndexDocument);
	}

	/**
	 * @throws Exception
	 * 
	 */
	private static void insertDoc() throws Exception {
		String objectType = "Content";
		Date d = new Date();
		Map<String, Object> map = getTestRecord("do_10000031", 31, "do", objectType);
		map.put("name", "Content_" + System.currentTimeMillis() + "_name");
		map.put("code", "code_" + System.currentTimeMillis());
		map.put("contentType", "Story");
		map.put("createdOn", new Date().toString());
		map.put("lastUpdatedOn", new Date().toString());
		map.put("lastPublishedOn", d.toString());
		map.put("status", "Live");
		map.put("size", 1000432);
		Map<String, Object> badgemap = new HashMap<String, Object>();
		badgemap.put("id", "badge1");
		badgemap.put("name", "Badge 1");
		Map<String, Object> issuerMap = new HashMap<String, Object>();
		issuerMap.put("id", "abc");
		issuerMap.put("name", "ABC");
		badgemap.put("issuer", issuerMap);
		map.put("badgesList", Arrays.asList(badgemap));
		map.put("downloads", 31);

		addToIndex("do_10000031", map);

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

	@Test
	public void testSearch() throws Exception {

		SearchDTO searchDTO = new SearchDTO();

		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("Content"));
		property.put(CompositeSearchParams.propertyName.name(), "objectType");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);

		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("do_10000031"));
		property.put(CompositeSearchParams.propertyName.name(), "identifier");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);

		property = new HashMap<String, Object>();
		property.put(CompositeSearchParams.values.name(), Arrays.asList("abc"));
		property.put(CompositeSearchParams.propertyName.name(), "badgesList.issuer.id");
		property.put(CompositeSearchParams.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		properties.add(property);

		searchDTO.setProperties(properties);
		searchDTO.setLimit(100);
		searchDTO.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, Object> response = searchprocessor.processSearch(searchDTO, true);

		assertNotNull(response);

	}

}
