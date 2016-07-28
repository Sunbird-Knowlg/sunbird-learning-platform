package org.ekstep.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.config.controller.ConfigController;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.exception.ResponseCode;

@Controller
@RequestMapping("health")
public class HealthCheckController extends ConfigController {
	private static final String folderName = ConfigController.folderName;
	private static final String baseUrl = ConfigController.baseUrl;

	@RequestMapping(value = "/config", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> search() {
		Response response = new Response();
		String ordinals = "";
		String apiId  = "";
		String resourcebundle = "";
		List<Map<String, Object>> checks = new ArrayList<Map<String, Object>>();
		try {
			resourcebundle = HttpDownloadUtility.readFromUrl(baseUrl + folderName + "/en.json");
			ordinals = HttpDownloadUtility.readFromUrl(baseUrl + "ordinals.json");
			String name = "config-service";
			apiId = name + ".health";
			response.put("name", name);
			
			if (!ordinals.isEmpty()){
				checks.add(getResponseData(true,"",""));
			}else{
				checks.add(getResponseData(false, "404", "ordinals is not available"));
			}
			if(!resourcebundle.isEmpty()){
				checks.add(getResponseData(true,"",""));
			}
			else{
				checks.add(getResponseData(false, "404", "resourcebundle is not available"));
			}
			return getResponseEntity(response, apiId, null);
			
		} catch (Exception e) {
			checks.add(getResponseData(false, "503", e.getMessage()));		
		}
		response.put("checks", checks);
		return getResponseEntity(response, apiId, null);	
	}
	
	public Map<String, Object> getResponseData(boolean b, String err, String errorMsg){
		ResponseParams params = new ResponseParams();
		Response response = new Response();
		Map<String, Object> csCheck = new HashMap<String, Object>();
		if(b == true && err.isEmpty()){
			params.setErr("0");
			params.setStatus(StatusType.successful.name());
			params.setErrmsg("Operation successful");
			response.setParams(params);
			response.put("healthy", true);
			csCheck.put("healthy", true);
		}else{
			params.setStatus(StatusType.failed.name());
			params.setErrmsg(errorMsg);
			response.setResponseCode(ResponseCode.SERVER_ERROR);
			response.setParams(params);
			response.put("healthy", false);
			csCheck.put("healthy", false);
			csCheck.put("err", err);	
		}
		return csCheck;
	}
}
