package controllers;

import com.ilimi.common.dto.Request;

import managers.PlaySearchManager;
import play.libs.F.Promise;
import play.mvc.Result;

public class SearchController extends SearchBaseController {

	private PlaySearchManager mgr = new PlaySearchManager();
	
	public Promise<Result> search() {
		String apiId = "search-api.search";
		Request request = getRequest(request().body(),apiId);
		Promise<Result> searchResponse = mgr.search(request);
		return searchResponse;
	}

	public Promise<Result> count() {
		String apiId = "search-api.count";
		Request request = getRequest(request().body(),apiId);
		Promise<Result> response = mgr.count(request);
		return response;
	}

}
