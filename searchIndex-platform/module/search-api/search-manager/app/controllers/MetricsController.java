package controllers;

import org.ekstep.common.dto.Request;

import managers.PlaySearchManager;
import play.libs.F.Promise;
import play.mvc.Result;

public class MetricsController extends SearchBaseController {

	private PlaySearchManager mgr = new PlaySearchManager();

	public Promise<Result> search() {
		String apiId = "composite-search.metrics";
		Request request = getRequest(request().body(), apiId, request().uri());
		Promise<Result> searchResponse = mgr.metrics(request);
		return searchResponse;
	}

}
