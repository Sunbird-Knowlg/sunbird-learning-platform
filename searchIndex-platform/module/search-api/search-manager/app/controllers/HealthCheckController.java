package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;

import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.exception.ResponseCode;

import managers.BasePlaySearchManager;
import play.mvc.Result;

public class HealthCheckController extends SearchBaseController {
	
	ElasticSearchUtil es = new ElasticSearchUtil();
	BasePlaySearchManager mgr = new BasePlaySearchManager();

	public Result search() {
		String name = "search-service";
		String apiId = name + ".health";
		List<Map<String, Object>> checks = new ArrayList<Map<String, Object>>();
		Response response = new Response();
		boolean index = false;
		try {
			index = es.isIndexExists(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX);
			if (index == true) {
				checks.add(getResponseData(response, true, "", ""));
			} else {
				checks.add(getResponseData(response, false, "404", "Elastic Search index is not avaialable"));
			}
		} catch (Exception e) {
			checks.add(getResponseData(response, false, "503", e.getMessage()));
		}
		response.put("checks", checks);
		return ok(mgr.getResult(response, getAPIId(apiId) , getAPIVersion(request().uri()))).as("application/json");
	}
	 
	public Map<String, Object> getResponseData(Response response, boolean res, String error, String errorMsg) {
		ResponseParams params = new ResponseParams();
		String err = error;
		Map<String, Object> esCheck = new HashMap<String, Object>();
		esCheck.put("name", "ElasticSearch");
		if (res == true && err.isEmpty()) {
			params.setErr("0");
			params.setStatus(StatusType.successful.name());
			params.setErrmsg("Operation successful");
			response.setParams(params);
			response.put("healthy", true);
			esCheck.put("healthy", true);
		} else {
			params.setStatus(StatusType.failed.name());
			params.setErrmsg(errorMsg);
			response.setResponseCode(ResponseCode.SERVER_ERROR);
			response.setParams(params);
			response.put("healthy", false);
			esCheck.put("healthy", false);
			esCheck.put("err", err);
			esCheck.put("errmsg", errorMsg);
		}
		return esCheck;
	}
	
	
}
