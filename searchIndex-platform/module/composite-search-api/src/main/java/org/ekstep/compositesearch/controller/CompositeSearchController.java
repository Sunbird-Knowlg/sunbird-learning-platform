package org.ekstep.compositesearch.controller;

import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.ekstep.compositesearch.enums.CompositeSearchParams;
import org.ekstep.search.mgr.CompositeSearchManager;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.logger.LogHelper;
import com.ilimi.common.util.LogTelemetryEventUtil;

@Controller
@RequestMapping("v2/search")
public class CompositeSearchController extends BaseCompositeSearchController {

	private static LogHelper LOGGER = LogHelper.getInstance(CompositeSearchController.class.getName());
	
	private CompositeSearchManager compositeSearchManager = new CompositeSearchManager();

	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> search(@RequestBody Map<String, Object> map,
			@RequestHeader(value = "user-id") String userId, HttpServletResponse resp) {
		String apiId = "composite-search.search";
		LOGGER.info(apiId + " | Request : " + map);
		Request request = getRequest(map);
		Map<String, Object> requestMap = (Map<String, Object>) map.get("request"); 
		String queryString = (String) requestMap.get(CompositeSearchParams.query.name());
		Object filters = requestMap.get(CompositeSearchParams.filters.name());
		Object sort = requestMap.get(CompositeSearchParams.sort_by.name());
		Response searchResponse = compositeSearchManager.search(request);
		Response  response = compositeSearchManager.getSearchResponse(searchResponse);
		String correlationId = UUID.randomUUID().toString();
		int count = (int) response.getResult().get("count");
		LogTelemetryEventUtil.logContentSearchEvent(queryString, filters, sort, correlationId, count);
		return getResponseEntity(response, apiId, null, correlationId);
	}

	@RequestMapping(value = "/count", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> count(@RequestBody Map<String, Object> map,
			@RequestHeader(value = "user-id") String userId, HttpServletResponse resp) {
		String apiId = "composite-search.count";
		LOGGER.info(apiId + " | Request : " + map);
		Request request = getRequest(map);
		Response response = compositeSearchManager.count(request);
		return getResponseEntity(response, apiId, null);
	}

}
