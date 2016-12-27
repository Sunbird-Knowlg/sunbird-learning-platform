package controllers;

import com.ilimi.common.dto.Request;

import managers.PlaySearchManager;
import play.libs.F.Promise;
import play.mvc.Result;

public class MetricsController extends SearchBaseController {

	private PlaySearchManager mgr = new PlaySearchManager();

	public Promise<Result> search() {
		String apiId = "search.metrics";
		Request request = getRequest(request().body(), apiId);
		Promise<Result> searchResponse = mgr.metrics(request);
		return searchResponse;
	}

}
