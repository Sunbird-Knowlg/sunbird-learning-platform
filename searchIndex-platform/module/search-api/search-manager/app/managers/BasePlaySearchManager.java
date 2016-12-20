package managers;

import static akka.pattern.Patterns.ask;

import org.apache.logging.log4j.Logger;
import org.ekstep.search.router.SearchRequestRouterPool;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.mgr.BaseManager;

import akka.actor.ActorRef;
import play.libs.F.Function;
import play.libs.F.Promise;

public class BasePlaySearchManager extends BaseManager {
	
	static {
		SearchRequestRouterPool.init();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();	
		}
	}
	
	protected Response getSearchResponse(Request request, Logger logger) {
		ActorRef router = SearchRequestRouterPool.getRequestRouter();
		Response response = new Response();
		try {
			Promise<Response> res = Promise.wrap(ask(router, request, SearchRequestRouterPool.REQ_TIMEOUT)).map(
                    new Function<Object, Response>() {
                        public Response apply(Object response) {
                             if( response instanceof Response ) {
                           	  Response message = ( Response )response;
                              return message;
                             }
                             ResponseParams params = new ResponseParams();
                             params.setErrmsg("Invalid Response object");
                             Response error = new Response();
                             error.setParams(params);
                             return error;
                        }
                    }
                );
		 response =	res.get(SearchRequestRouterPool.REQ_TIMEOUT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
	
	protected Request setSearchContext(Request request, String manager, String operation) {
		request.setManagerName(manager);
		request.setOperation(operation);
		return request;
	}
	
	 protected Request getSearchRequest(String manager, String operation) {
	        Request request = new Request();
	        return setSearchContext(request, manager, operation);
	    }
	
}
