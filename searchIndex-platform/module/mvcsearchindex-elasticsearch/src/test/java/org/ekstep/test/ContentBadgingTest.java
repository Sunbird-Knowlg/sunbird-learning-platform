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
import org.ekstep.compositesearch.enums.CompositeSearchParams;
import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.mvcsearchindex.processor.SearchProcessor;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.dispatch.OnSuccess;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

/**
 * @author pradyumna
 *
 */
public class ContentBadgingTest extends BaseSearchTest {

	private static SearchProcessor searchprocessor = new SearchProcessor();

	@BeforeClass
	public static void beforeTest() throws Exception {
		createCompositeSearchIndex();
		insertDoc();
		Thread.sleep(3000);
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

	@SuppressWarnings("rawtypes")
	@Test
	public void testSearch() throws Exception {

		SearchDTO searchDTO = new SearchDTO();

		List<Map> properties = new ArrayList<>();
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
		Future<Map<String, Object>> response = searchprocessor.processSearch(searchDTO, true);

		response.onSuccess(new OnSuccess<Map<String, Object>>() {
			public void onSuccess(Map<String, Object> lstResult) {
				assertNotNull(lstResult);
			}
		}, ExecutionContext.Implicits$.MODULE$.global());
	}

}

