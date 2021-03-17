package org.sunbird.search.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import akka.dispatch.Futures;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.dto.ResponseParams;
import org.sunbird.common.dto.ResponseParams.StatusType;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.compositesearch.enums.CompositeSearchErrorCodes;
import org.sunbird.compositesearch.enums.SearchOperations;
import org.sunbird.searchindex.elasticsearch.ElasticSearchUtil;
import org.sunbird.searchindex.util.CompositeSearchConstants;

import org.sunbird.telemetry.logger.TelemetryManager;
import scala.concurrent.Future;

public class HealthCheckManager extends SearchBaseActor {


	public Future<Response> onReceive(Request request) throws Throwable {
		ElasticSearchUtil.initialiseESClient(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, Platform.config.getString("search.es_conn_info"));
		String operation = request.getOperation();
		if (StringUtils.equalsIgnoreCase(SearchOperations.HEALTH.name(), operation)) {
			Response response = checkIndexExists();
			return Futures.successful(OK("", response));
		} else {
			TelemetryManager.log("Unsupported operation: " + operation);
			throw new ClientException(CompositeSearchErrorCodes.ERR_INVALID_OPERATION.name(),
					"Unsupported operation: " + operation);
		}
	}


	@Override
	public Response OK(String responseIdentifier, Object vo) {
		Response response = new Response();
		if (vo instanceof Response) {
			response = (Response) vo;
		}
		return response;
	}

	public Response checkIndexExists() {
		List<Map<String, Object>> checks = new ArrayList<Map<String, Object>>();
		Response response = new Response();
		boolean index = false;
		try {
			index = ElasticSearchUtil.isIndexExists(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX);
			if (index == true) {
				checks.add(getResponseData(response, true, "", ""));
			} else {
				checks.add(getResponseData(response, false, "404", "Elastic Search index is not avaialable"));
			}
		} catch (Exception e) {
			checks.add(getResponseData(response, false, "503", e.getMessage()));
		}
		response.put("checks", checks);
		return response;
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
