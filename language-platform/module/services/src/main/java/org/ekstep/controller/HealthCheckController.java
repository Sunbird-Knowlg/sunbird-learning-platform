package org.ekstep.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.graph.common.mgr.Configuration;

@Controller
@RequestMapping("health")
public class HealthCheckController extends BaseController {
	
	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> search() {
		String name = "language-service";
		String apiId = name + ".health";
		Response response = new Response();
		response.put("name", name);
		try {
			response.put("healthy", true);
			List<Map<String, Object>> checks = new ArrayList<Map<String, Object>>();
			List<String> graphIds = Configuration.graphIds;
			if (null != graphIds && graphIds.size() > 0) {
				for (String id : graphIds) {
					Map<String, Object> check = new HashMap<String, Object>();
					check.put("name", id + " graph");
					check.put("healthy", true);
					checks.add(check);
				}
			}
			response.put("checks", checks);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			ResponseParams resStatus = new ResponseParams();
	        resStatus.setErrmsg(e.getMessage());
	        resStatus.setStatus(StatusType.failed.name());
            response.setResponseCode(ResponseCode.SERVER_ERROR);
            response.setParams(resStatus);
            response.put("healthy", false);
            return getResponseEntity(response, apiId, null);
		}
	}
}
