package managers;

import java.util.Map;

import akka.actor.ActorRef;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.dto.ResponseParams.StatusType;
import org.ekstep.compositesearch.enums.SearchActorNames;
import org.ekstep.compositesearch.enums.SearchOperations;
import org.ekstep.searchindex.util.ObjectDefinitionCache;

import scala.concurrent.Future;

public class PlaySearchManager extends BasePlaySearchManager {

	public Future<Response> search(Request request, ActorRef actor) {
		request = setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name(),
				SearchOperations.INDEX_SEARCH.name());
		Future<Response> getRes = getSearchResponse(request, actor);
		return getRes;
	}

	public Future<Response> count(Request request, ActorRef actor) {
		request = setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name(), SearchOperations.COUNT.name());
		Future<Response> getRes = getSearchResponse(request, actor);
		return getRes;
	}
	
	public Future<Response> metrics(Request request, ActorRef actor) {
		request = setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name() , SearchOperations.METRICS.name());
		Future<Response> getRes = getSearchResponse(request, actor);
		return getRes;
	}
	
	public Future<Response> health(Request request, ActorRef actor) {
		request = setSearchContext(request, SearchActorNames.HEALTH_CHECK_MANAGER.name() , SearchOperations.HEALTH.name());
		Future<Response> getRes = getSearchResponse(request, actor);
		return getRes;
	}
	
	public Response callResyncDefinition(Request request, ActorRef actor) {
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
