package controllers;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.exception.ResponseCode;

import managers.PlaySearchManager;
import play.mvc.Result;

public class SearchController extends SearchBaseController {

	private static Logger LOGGER = LogManager.getLogger(SearchController.class.getName());
	private PlaySearchManager mgr = new PlaySearchManager();
	
	public Result search() {
		String apiId = "search-api.search";
		Response response;
		int count = 0;
		Request request = getRequest(request().body());
		LOGGER.info(apiId);
		try {
			Response searchResponse = mgr.search(request);
			if(!checkError(searchResponse)){
				response = mgr.getSearchResponse(searchResponse);
				if(null != response.getResult() && !response.getResult().isEmpty()) {
					count = (Integer) response.getResult().get("count");
				}
			}else {
				response = searchResponse;
			}			
		} catch (NullPointerException e) {
			response = new Response();
			response.setResponseCode(ResponseCode.SERVER_ERROR);
			ResponseParams params = new ResponseParams();
			params.setErrmsg("Not able to process data");
			params.setStatus("Failed");
			response.setParams(params);
		}
		String correlationId = UUID.randomUUID().toString();
		writeTelemetryLog(request, correlationId, count);
		return getResult(response, apiId, null, correlationId);
	}
	
	public Result count() {
		String apiId = "search-api.count";
		LOGGER.info(apiId);
		Request request = getRequest(request().body());
		Response response = mgr.count(request);
		return getResult(response, apiId, null,null);
	}

}
