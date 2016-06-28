package org.ekstep.compositesearch.mgr;

import java.util.Map;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

public interface ICompositeSearchManager {
	
	Response sync(String graphId, String objectType, Request request);
	
	Response search(Request request);
	
	Response metrics(Request request);
	
	Response count(Request request);

	Map<String, Object> searchForTraversal(Request request);

}
