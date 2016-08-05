package org.ekstep.search.mgr;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.compositesearch.enums.CompositeSearchErrorCodes;
import org.ekstep.compositesearch.enums.SearchActorNames;
import org.ekstep.compositesearch.enums.SearchOperations;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ServerException;

public class CompositeSearchManager extends BaseSearchManager {

	private static Logger LOGGER = LogManager.getLogger(CompositeSearchManager.class.getName());
	
	public Response search(Request request){
		request = setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name() ,SearchOperations.SEARCH.name());
		Response getRes = getSearchResponse(request, LOGGER);
		if (checkError(getRes)) {
			throw new ServerException(CompositeSearchErrorCodes.SYSTEM_ERROR.name(), getErrorMessage(getRes));
		}
		
		return getRes;
	}
	
	public Response count(Request request){
		request = setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name() ,SearchOperations.COUNT.name());
		Response getRes = getSearchResponse(request, LOGGER);
		if (checkError(getRes)) {
			throw new ServerException(CompositeSearchErrorCodes.SYSTEM_ERROR.name(), getErrorMessage(getRes));
		}
		
		return getRes;
	}
	
	public Response metrics(Request request){
		request = setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name() ,SearchOperations.METRICS.name());
		Response getRes = getSearchResponse(request, LOGGER);
		if (checkError(getRes)) {
			throw new ServerException(CompositeSearchErrorCodes.SYSTEM_ERROR.name(), getErrorMessage(getRes));
		}
		
		return getRes;
	}
	
	public Map<String, Object> languageSearch(Request request){
		Map<String, Object> result = null;
		request = setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name() ,SearchOperations.LANGUAGE_SEARCH.name());
		Response getRes = getSearchResponse(request, LOGGER);
		if (!checkError(getRes)) {
			result = (Map<String, Object>) getRes.get("result");
		}else{
			throw new ServerException(CompositeSearchErrorCodes.SYSTEM_ERROR.name(), getErrorMessage(getRes));
		}
		
		return result;
	}
	
	public Response getSearchResponse(Map<String, Object> searchResult){
		Request request = getSearchRequest(SearchActorNames.SEARCH_MANAGER.name(), SearchOperations.GET_COMPOSITE_SEARCH_RESPONSE.name());
		request.put("searchResult", searchResult);
		Response getRes = getSearchResponse(request, LOGGER);
		if (checkError(getRes)) {
			throw new ServerException(CompositeSearchErrorCodes.SYSTEM_ERROR.name(), getErrorMessage(getRes));
		}
		
		return getRes;
	}
}
