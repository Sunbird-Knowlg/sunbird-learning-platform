package org.ekstep.taxonomy.controller;

import java.util.List;
import java.util.Map;

import org.ekstep.common.controller.BaseController;
import org.ekstep.common.dto.Response;
import org.ekstep.taxonomy.mgr.ICompositeSearchManager;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("v2/composite-search")
public class LearningDataSyncController extends BaseController {

	
	
	@Autowired
	private ICompositeSearchManager compositeSearchManager;

	@RequestMapping(value = "/sync/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> sync(@PathVariable(value = "id") String graphId,
			@RequestParam(name = "objectType", required = false) String objectType,
			@RequestParam(name = "start", required = false) Integer start,
			@RequestParam(name = "total", required = false) Integer total,
			@RequestParam(name = "delete", required = false, defaultValue = "false") boolean delete,
			@RequestBody Map<String, Object> map) {
		String apiId = "ekstep.composite-search.sync";
		TelemetryManager.log(apiId + " | Graph : " + graphId + " | ObjectType: " + objectType);
		try {
			Response response = compositeSearchManager.sync(graphId, objectType, start, total, delete);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Error: " + apiId, e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	@RequestMapping(value = "/sync/object/{graphId:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> syncObject(@PathVariable(value = "graphId") String graphId,
			@RequestBody Map<String, Object> map) {
		String apiId = "ekstep.composite-search.sync-object";
		TelemetryManager.log(apiId + " | Graph : " + graphId + " | request body: " + map);
		try {
			String[] identifiers =null;
			if (map.get("request") != null && ((Map) map.get("request")).get("identifiers") != null) {
				List<String> identifiersList = (List<String>) ((Map) map.get("request")).get("identifiers");
				identifiers = identifiersList.toArray(new String[identifiersList.size()]);
			}

			Response response = compositeSearchManager.syncObject(graphId, identifiers);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Error: "+ apiId, e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
}
