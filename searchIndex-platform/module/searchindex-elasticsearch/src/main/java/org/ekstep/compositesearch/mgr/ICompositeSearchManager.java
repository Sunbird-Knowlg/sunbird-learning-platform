package org.ekstep.compositesearch.mgr;

import java.util.Map;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

public interface ICompositeSearchManager {
	
	Response search(Request request);
	
	Response metrics(Request request);
	
	Response count(Request request);

	Map<String, Object> languageSearch(Request request);

	Response getCompositeSearchResponse(Map<String, Object> searchResponse);

}
