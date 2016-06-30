package org.ekstep.compositesearch.mgr;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

public interface ICompositeSearchManager {
	
	Response search(Request request);
	
	Response metrics(Request request);
	
	Response count(Request request);

}
