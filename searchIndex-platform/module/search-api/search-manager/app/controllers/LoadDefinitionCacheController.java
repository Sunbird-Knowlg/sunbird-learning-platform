package controllers;

import org.ekstep.common.dto.Request;

import managers.PlaySearchManager;
import play.mvc.Result;

public class LoadDefinitionCacheController extends SearchBaseController {
	
	private PlaySearchManager mgr = new PlaySearchManager();

	public Result loadDefinitionCache() {
		String apiId = "composite-search.loadDefinitionCache";
		Request request = getRequest(request().body(), apiId, request().uri());
		String response = mgr.callResyncDefinition(request);
		return ok(response).as("application/json");
	}

}
