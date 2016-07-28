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

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.exception.ResponseCode;

@Controller
@RequestMapping("health")
public class HealthCheckController extends BaseController {

	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> search() {
		Response response = new Response();
		String ordinals = "";
		String apiId = "";
		String resourcebundle = "";
		List<Map<String, Object>> checks = new ArrayList<Map<String, Object>>();
		try {
			resourcebundle = HttpDownloadUtility
					.readFromUrl(ConfigController.baseUrl + ConfigController.folderName + "/en.json");
			ordinals = HttpDownloadUtility.readFromUrl(ConfigController.baseUrl + "ordinals.json");
			String name = "config-service";
			apiId = name + ".health";
			response.put("name", name);

			if (!ordinals.isEmpty()) {
				checks.add(getResponseData(response, "Ordinals", true, "", ""));
			} else {
				checks.add(getResponseData(response, "Ordinals", false, "404", "ordinals is not available"));
			}
			if (!resourcebundle.isEmpty()) {
				checks.add(getResponseData(response, "Resourcebundle", true, "", ""));
			} else {
				checks.add(
						getResponseData(response, "Resourcebundle", false, "404", "resourcebundle is not available"));
			}

		} catch (Exception e) {
			checks.add(getResponseData(response, "Resourcebundle", false, "503", e.getMessage()));
		}
		response.put("checks", checks);
		return getResponseEntity(response, apiId, null);
	}

	public Map<String, Object> getResponseData(Response response, String name, boolean res, String err, String errorMsg) {
		ResponseParams params = new ResponseParams();
		Map<String, Object> csCheck = new HashMap<String, Object>();
		csCheck.put("name", name);
		if (res == true && err.isEmpty()) {
			params.setErr("0");
			params.setStatus(StatusType.successful.name());
			params.setErrmsg("Operation successful");
			response.setParams(params);
			response.put("healthy", true);
			csCheck.put("healthy", true);
		} else {
			params.setStatus(StatusType.failed.name());
			params.setErrmsg(errorMsg);
			response.setResponseCode(ResponseCode.SERVER_ERROR);
			response.setParams(params);
			response.put("healthy", false);
			csCheck.put("healthy", false);
			csCheck.put("err", err);
			csCheck.put("errmsg", errorMsg);
		}
		return csCheck;
	}
}
