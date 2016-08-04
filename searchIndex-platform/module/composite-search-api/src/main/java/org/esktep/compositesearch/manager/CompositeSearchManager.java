package org.esktep.compositesearch.manager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.compositesearch.enums.CompositeSearchErrorCodes;
import org.ekstep.compositesearch.enums.SearchActorNames;
import org.ekstep.compositesearch.enums.SearchOperations;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.BaseManager;

public class CompositeSearchManager extends BaseManager {

	private static Logger LOGGER = LogManager.getLogger(CompositeSearchManager.class.getName());
	
	public Response search(Request request){
		request = setContext(request, null, SearchActorNames.SEARCH_MANAGER.name() ,SearchOperations.SEARCH.name());
		Response getRes = getResponse(request, LOGGER);
		if (checkError(getRes)) {
			throw new ServerException(CompositeSearchErrorCodes.SYSTEM_ERROR.name(), getErrorMessage(getRes));
		}
		
		return getRes;
	}
	
	public Response count(Request request){
		request = setContext(request, null, SearchActorNames.SEARCH_MANAGER.name() ,SearchOperations.COUNT.name());
		Response getRes = getResponse(request, LOGGER);
		if (checkError(getRes)) {
			throw new ServerException(CompositeSearchErrorCodes.SYSTEM_ERROR.name(), getErrorMessage(getRes));
		}
		
		return getRes;
	}
	
	public Response metrics(Request request){
		request = setContext(request, null, SearchActorNames.SEARCH_MANAGER.name() ,SearchOperations.METRICS.name());
		Response getRes = getResponse(request, LOGGER);
		if (checkError(getRes)) {
			throw new ServerException(CompositeSearchErrorCodes.SYSTEM_ERROR.name(), getErrorMessage(getRes));
		}
		
		return getRes;
	}
}
