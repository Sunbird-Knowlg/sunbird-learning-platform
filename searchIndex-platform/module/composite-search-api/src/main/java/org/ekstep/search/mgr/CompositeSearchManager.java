package org.ekstep.search.mgr;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.compositesearch.enums.CompositeSearchErrorCodes;
import org.ekstep.compositesearch.enums.SearchActorNames;
import org.ekstep.compositesearch.enums.SearchOperations;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ResponseCode;

public class CompositeSearchManager extends BaseSearchManager {

	private static Logger LOGGER = LogManager.getLogger(CompositeSearchManager.class.getName());
	
	public Response search(Request request){
		request = setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name() ,SearchOperations.INDEX_SEARCH.name());
		Response getRes = getSearchResponse(request, LOGGER);
		if (checkError(getRes)) {
			return ERROR(CompositeSearchErrorCodes.SYSTEM_ERROR.name(), getErrorMessage(getRes), ResponseCode.SERVER_ERROR);
		}
		
		return getRes;
	}
	
	public Response count(Request request){
		request = setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name() ,SearchOperations.COUNT.name());
		Response getRes = getSearchResponse(request, LOGGER);
		if (checkError(getRes)) {
			return ERROR(CompositeSearchErrorCodes.SYSTEM_ERROR.name(), getErrorMessage(getRes), ResponseCode.SERVER_ERROR);
		}
		
		return getRes;
	}
	
	public Response metrics(Request request){
		request = setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name() ,SearchOperations.METRICS.name());
		Response getRes = getSearchResponse(request, LOGGER);
		if (checkError(getRes)) {
			return ERROR(CompositeSearchErrorCodes.SYSTEM_ERROR.name(), getErrorMessage(getRes), ResponseCode.SERVER_ERROR);
		}
		
		return getRes;
	}
	
	
	public Response getSearchResponse(Response searchResult){
		Request request = getSearchRequest(SearchActorNames.SEARCH_MANAGER.name(), SearchOperations.GROUP_SEARCH_RESULT_BY_OBJECTTYPE.name());
		request.put("searchResult", searchResult.getResult());
		Response getRes = getSearchResponse(request, LOGGER);
		if (checkError(getRes)) {
			return ERROR(CompositeSearchErrorCodes.SYSTEM_ERROR.name(), getErrorMessage(getRes), ResponseCode.SERVER_ERROR);
		}
		
		return getRes;
	}
}
