package org.ekstep.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.mgr.HealthCheckManager;

@Controller
@RequestMapping("health")
public class HealthCheckController extends BaseController {
	
	@Autowired
	HealthCheckManager healthCheckManager;
	
	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> search() {
		String name = "language-service";
		String apiId = name + ".health";
		Response response;
		try {
			response = healthCheckManager.getAllServiceHealth();
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			response = new Response();
			ResponseParams resStatus = new ResponseParams();
	        resStatus.setErrmsg(e.getMessage());
	        resStatus.setStatus(StatusType.failed.name());
            response.setResponseCode(ResponseCode.SERVER_ERROR);
            response.setParams(resStatus);
            response.put("healthy", false);
            return getResponseEntity(response, apiId, null);
		}
	}
	
	@RequestMapping(value = "/register/{graphId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> register(@PathVariable(value = "graphId") String graphId) {
		String name = "language-service";
		String apiId = name + ".health.register";
		Response response;
		try {
			response = healthCheckManager.registerGraph(graphId);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			response = new Response();
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
