/**
 *
 */
package org.ekstep.test;

import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.mvcsearchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Map;



@RunWith(PowerMockRunner.class)
@PrepareForTest({ElasticSearchUtil.class})
@PowerMockIgnore({"javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*" , "javax.crypto.*"})
public class BaseSearchTest {

	protected static ObjectMapper mapper = new ObjectMapper();

	@Before
	public void setup()  {
		MockitoAnnotations.initMocks(this);
	}


	protected static void addToIndex(String uniqueId, Map<String, Object> doc) throws Exception {
		String jsonIndexDocument = mapper.writeValueAsString(doc);
		PowerMockito.mockStatic(ElasticSearchUtil.class);
		PowerMockito.doNothing().when(ElasticSearchUtil.class);
		ElasticSearchUtil.addDocumentWithId(CompositeSearchConstants.MVC_SEARCH_INDEX,
				uniqueId, jsonIndexDocument);
	}

}
