package managers;

import java.util.Map;

import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.dto.ResponseParams.StatusType;
import org.ekstep.compositesearch.enums.SearchActorNames;
import org.ekstep.compositesearch.enums.SearchOperations;
import org.ekstep.searchindex.util.ObjectDefinitionCache;

import play.libs.F.Promise;
import play.mvc.Result;

public class PlaySearchManager extends BasePlaySearchManager {

	

	public Promise<Result> search(Request request) {
		request = setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name(),
				SearchOperations.INDEX_SEARCH.name());
		Promise<Result> getRes = getSearchResponse(request);
		return getRes;
	}

	public Promise<Result> count(Request request) {
		request = setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name(), SearchOperations.COUNT.name());
		Promise<Result> getRes = getSearchResponse(request);
		return getRes;
	}
	
	public Promise<Result> metrics(Request request){
		request = setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name() , SearchOperations.METRICS.name());
		Promise<Result> getRes = getSearchResponse(request);
		return getRes;
	}
	
	public Promise<Result> health(Request request){
		request = setSearchContext(request, SearchActorNames.HEALTH_CHECK_MANAGER.name() , SearchOperations.HEALTH.name());
		Promise<Result> getRes = getSearchResponse(request);
		return getRes;
	}
	
	public String callResyncDefinition(Request request) {
		Map<String, Object> requestMap = request.getRequest();
		String objectType = (String) requestMap.get("objectType");
		String graphId = (String) requestMap.get("graphId");
		Response resp = new Response();
		try {
			ObjectDefinitionCache.resyncDefinition(objectType, graphId);
			ResponseParams params = new ResponseParams();
			params.setStatus(StatusType.successful.name());
			resp.setParams(params);
		} catch (Exception e) {
			e.printStackTrace();
			ResponseParams params = new ResponseParams();
			params.setStatus(StatusType.successful.name());
			resp.setParams(params);
		}
		return getResult(resp, request.getId(), request.getVer(), null , null);
	}
}
