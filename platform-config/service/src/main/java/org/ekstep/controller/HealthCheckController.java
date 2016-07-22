package org.ekstep.controller;

import org.ekstep.common.util.HttpDownloadUtility;
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
	private static final String folderName = "resources";
	private static final String baseUrl = "https://ekstep-config.s3-ap-southeast-1.amazonaws.com/";

	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> search() {
		Response response = new Response();
		String ordinals = "";
		String apiId  = "";
		String resourcebundle = "";
		try {
			resourcebundle = HttpDownloadUtility.readFromUrl(baseUrl + folderName + "/en.json");
			ordinals = HttpDownloadUtility.readFromUrl(baseUrl + "ordinals.json");
			ResponseParams params = new ResponseParams();
			if ((!ordinals.isEmpty()) && (!resourcebundle.isEmpty())) {
				String name = "config-service";
				apiId = name + ".health";
				params.setErr("0");
				params.setStatus(StatusType.successful.name());
				params.setErrmsg("Operation successful");
				response.setParams(params);
				response.put("name", name);
				response.put("healthy", true);
			}
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
