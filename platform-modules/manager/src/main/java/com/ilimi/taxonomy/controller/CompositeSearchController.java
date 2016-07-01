package com.ilimi.taxonomy.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.taxonomy.mgr.ICompositeSearchSyncManager;

@Controller
@RequestMapping("v2/composite-search")
public class CompositeSearchController extends BaseController {

	private static Logger LOGGER = LogManager.getLogger(CompositeSearchController.class.getName());
	
	@Autowired
	private ICompositeSearchSyncManager compositeSearchManager;

	@RequestMapping(value = "/sync/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@PathVariable(value = "id") String graphId,
			@RequestParam(name = "objectType", required = false) String objectType,
			@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId,
			HttpServletResponse resp) {
		String apiId = "composite-search.sync";
		LOGGER.info(apiId + " | Graph : " + graphId + " | ObjectType: " + objectType);
		try {
			Request request = getRequest(map);
			Response response = compositeSearchManager.sync(graphId, objectType, request);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("Error: " + apiId, e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
}
