package controllers;

import org.ekstep.common.dto.Request;

import managers.PlaySearchManager;
import play.libs.F.Promise;
import play.mvc.Result;

/**
 * This is the main API that accepts the search request and returns the response. 
 * The API contract is identical to the graph data structures. 
 * So the returned data structures for each node type 
 * conform to the graph specification for that node type.
 */
public class SearchController extends SearchBaseController {

	private PlaySearchManager mgr = new PlaySearchManager();
	
	/**
	 * Method to search for all indexed data in the platform
	 * based on the request
	 * @return Promise<Result> containing searchResponse object
	 */
	public Promise<Result> search() {
		String apiId = "composite-search.search";
		Request request = getRequest(request().body(),apiId,request().uri());
		setHeaderContext(request(),request);
		Promise<Result> searchResponse = mgr.search(request);
		return searchResponse;
	}
	
	/**
	 * Method to get the count of indexed data in the platform 
	 * based on the request
	 * @return Promise<Result> containing count of data being searched
	 */
	public Promise<Result> count() {
		String apiId = "composite-search.count";
		Request request = getRequest(request().body(),apiId,request().uri());
		Promise<Result> response = mgr.count(request);
		return response;
	}

}
