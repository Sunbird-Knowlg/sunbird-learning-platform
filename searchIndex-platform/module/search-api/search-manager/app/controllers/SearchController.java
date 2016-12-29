package controllers;

import com.ilimi.common.dto.Request;

import managers.PlaySearchManager;
import play.libs.F.Promise;
import play.mvc.Result;

public class SearchController extends SearchBaseController {

	private PlaySearchManager mgr = new PlaySearchManager();
	
	public Promise<Result> search() {
		String apiId = "composite-search.search";
		Request request = getRequest(request().body(),apiId,request().uri());
		Promise<Result> searchResponse = mgr.search(request);
		return searchResponse;
	}

	public Promise<Result> count() {
		String apiId = "composite-search.count";
		Request request = getRequest(request().body(),apiId,request().uri());
		Promise<Result> response = mgr.count(request);
		return response;
	}

}
