/**
 *
 */
/*
package org.ekstep.test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.mvcsearchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

/**
 * @author pradyumna
 *
 */
	/*
public class ElasticSearchUtilTest extends BaseSearchTest {


	private static ObjectMapper mapper = new ObjectMapper();
	private static Random random = new Random();
*/
/*
	@Test
	public void testAddDocumentWithId() throws Exception {
		Map<String, Object> content = getContentTestRecord();
		String id = (String) content.get("identifier");
		addToIndex(id, content);
		when(ElasticSearchUtil.getDocumentAsStringById(Mockito.anyString(),Mockito.anyString())).thenReturn(id);
		String doc = ElasticSearchUtil.getDocumentAsStringById(CompositeSearchConstants.MVC_SEARCH_INDEX, id);
		assertTrue(StringUtils.contains(doc, id));
	}
	*/
	/*
	@Test
	public void testUpdateDocument() throws Exception {
		Map<String, Object> content = getContentTestRecord();
		String id = (String) content.get("identifier");
		addToIndex(id, content);
		content.put("name", "Content_" + System.currentTimeMillis() + "_name");
		PowerMockito.mockStatic(ElasticSearchUtil.class);
		PowerMockito.doNothing().when(ElasticSearchUtil.class);
		ElasticSearchUtil.updateDocument(CompositeSearchConstants.MVC_SEARCH_INDEX,
				 mapper.writeValueAsString(content), id);
		when(ElasticSearchUtil.getDocumentAsStringById(Mockito.anyString(),Mockito.anyString())).thenReturn(id);
		String doc = ElasticSearchUtil.getDocumentAsStringById(CompositeSearchConstants.MVC_SEARCH_INDEX, id);
		assertTrue(StringUtils.contains(doc, id));
	}

*/

/*
	private static Map<String, Object> getContentTestRecord() {
		String objectType = "Content";
		Date d = new Date();
		Map<String, Object> map = new HashMap<String, Object>();
		long suffix = (long) (10000000 + random.nextInt(1000000));
		map.put("identifier", "do_" + suffix);
		map.put("objectType", objectType);
		map.put("name", "Content_" + System.currentTimeMillis() + "_name");
		map.put("contentType", "Content");
		map.put("status", "Draft");
		return map;
	}

}
*/