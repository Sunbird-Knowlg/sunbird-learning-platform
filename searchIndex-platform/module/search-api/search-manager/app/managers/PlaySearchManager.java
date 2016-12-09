package managers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.compositesearch.enums.CompositeSearchErrorCodes;
import org.ekstep.compositesearch.enums.SearchActorNames;
import org.ekstep.compositesearch.enums.SearchOperations;
import org.ekstep.search.router.SearchRequestRouterPool;
import static akka.pattern.Patterns.ask;
import com.ilimi.common.*;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import play.libs.F.Function;
import akka.actor.ActorRef;
import akka.pattern.Patterns;
import play.libs.F.Promise;
import play.mvc.Result;

public class PlaySearchManager {
	
	private static Logger LOGGER = LogManager.getLogger(PlaySearchManager.class.getName());

	public Response search(Request request) {
		System.out.println("inside search manager");
		request = setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name(),
				SearchOperations.INDEX_SEARCH.name());
		System.out.println(request);
		Response getRes = getSearchResponse(request, LOGGER);
		System.out.println("after getsearchresponse" + getRes);
		// if (checkError(getRes)) {
		// return ERROR(CompositeSearchErrorCodes.SYSTEM_ERROR.name(),
		// getErrorMessage(getRes), ResponseCode.SERVER_ERROR);
		// }

		return getRes;
	}

	public Response getSearchResponse(Response searchResult) {
		Request request = getSearchRequest(SearchActorNames.SEARCH_MANAGER.name(),
				SearchOperations.GROUP_SEARCH_RESULT_BY_OBJECTTYPE.name());
		request.put("searchResult", searchResult.getResult());
		Response getRes = getSearchResponse(request, LOGGER);
		// if (checkError(getRes)) {
		// return ERROR(CompositeSearchErrorCodes.SYSTEM_ERROR.name(),
		// getErrorMessage(getRes), ResponseCode.SERVER_ERROR);
		// }

		return getRes;
	}

	protected Request setSearchContext(Request request, String manager, String operation) {
		System.out.println("inside search context" + manager);
		request.setManagerName(manager);
		request.setOperation(operation);
		return request;
	}

	protected Request getSearchRequest(String manager, String operation) {
		Request request = new Request();
		return setSearchContext(request, manager, operation);
	}

	protected Response getSearchResponse(Request request, Logger logger) {
		System.out.println("inside search getsearchresponse");
		
		ActorRef router = SearchRequestRouterPool.getRequestRouter();
		System.out.println("router" + router);
		Response response = new Response();
		try {
			Promise.wrap(ask(router, request, SearchRequestRouterPool.REQ_TIMEOUT)).map(new Function<Object, Result>() {
				public Result apply(Object response) {
					System.out.println("Response" + response);
					if (response instanceof Response) {
						System.out.println("In response as request");
						Request message = (Request) response;
						System.out.println(message.getRequest());
						return (Result) response;
					}
					return (Result) response;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
}
