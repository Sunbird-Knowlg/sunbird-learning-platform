package managers;

import java.util.Map;

import javax.ws.rs.core.Response.StatusType;

import play.libs.F.Promise;
import play.mvc.Result;

public class PlaySearchManager extends BasePlaySearchManager {

	private static ILogger LOGGER = PlatformLogManager.getLogger();

	public Promise<Result> search(Request request) {
		request = setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name(),
				SearchOperations.INDEX_SEARCH.name());
		Promise<Result> getRes = getSearchResponse(request, LOGGER);
		return getRes;
	}

	public Promise<Result> count(Request request) {
		request = setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name(), SearchOperations.COUNT.name());
		Promise<Result> getRes = getSearchResponse(request, LOGGER);
		return getRes;
	}
	
	public Promise<Result> metrics(Request request){
		request = setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name() , SearchOperations.METRICS.name());
		Promise<Result> getRes = getSearchResponse(request, LOGGER);
		return getRes;
	}
	
	public Promise<Result> health(Request request){
		request = setSearchContext(request, SearchActorNames.HEALTH_CHECK_MANAGER.name() , SearchOperations.HEALTH.name());
		Promise<Result> getRes = getSearchResponse(request, LOGGER);
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
