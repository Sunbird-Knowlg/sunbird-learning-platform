package org.ekstep.config.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.common.util.HttpDownloadUtility;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.logger.LoggerEnum;
import com.ilimi.common.logger.PlatformLogger;

@Controller
@RequestMapping("v2/config")
public class ConfigController extends BaseController {
	private ObjectMapper mapper = new ObjectMapper();
	public static final String folderName = "resources";
	public static final String baseUrl = "https://" + AWSUploader.getBucketName("config") + ".s3.amazonaws.com/";

	@RequestMapping(value = "/resourcebundles", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> getResourceBundles() {
		String apiId = "ekstep.config.resourebundles.list";
		try {
			Response response = new Response();
			Map<String, Object> resourcebundles = new HashMap<String, Object>();
			Map<String, String> urlMap = getUrlFromS3();
			PlatformLogger.log("urls of resourcebundle files from s3", urlMap);
			for (Entry<String, String> entry : urlMap.entrySet()) {
				String langMap = HttpDownloadUtility.readFromUrl(entry.getValue());
				String langId = entry.getKey();
				try {
					if (StringUtils.isBlank(langMap))
						continue;
					Map<String, Object> map = mapper.readValue(langMap, new TypeReference<Map<String, Object>>() {
					});
					PlatformLogger.log("Resourcebundles fetched : ", map.keySet());
					resourcebundles.put(langId, map);
				} catch (Exception e) {
					PlatformLogger.log("Error in fetching all ResourceBundles from s3", e.getMessage(), e, LoggerEnum.WARN.name());
				}
			}
			response.put("resourcebundles", resourcebundles);
			ResponseParams params = new ResponseParams();
			params.setErr("0");
			params.setStatus(StatusType.successful.name());
			params.setErrmsg("Operation successful");
			response.setParams(params);
			response.put("ttl", 24.0);
			PlatformLogger.log("get All ResourceBundles | Response: " , response + "Id" + apiId);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			PlatformLogger.log("getAllResources | Exception" + e.getMessage(), null, e, LoggerEnum.WARN.name());
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@RequestMapping(value = "/resourcebundles/{languageId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> getResourceBundle(@PathVariable(value = "languageId") String languageId) {
		String apiId = "ekstep.config.resourebundles.info";

		try {
			PlatformLogger.log("ResourceBundle | GET | languageId" , languageId);
			Response response = new Response();
			String data = HttpDownloadUtility.readFromUrl(baseUrl + folderName + "/" + languageId + ".json");
			PlatformLogger.log("Resource bundle file read from url:", StringUtils.isNotBlank(data));
			if (StringUtils.isNotBlank(data)) {
				ResponseParams params = new ResponseParams();
				params.setErr("0");
				params.setStatus(StatusType.successful.name());
				params.setErrmsg("Operation successful");
				response.setParams(params);
				response.put("ttl", 24.0);
				try {
					Map<String, Object> map = mapper.readValue(data, new TypeReference<Map<String, Object>>() {
					});
					response.put(languageId, map);
					PlatformLogger.log("getResourceBundle | successResponse" , response);
				} catch (Exception e) {
					PlatformLogger.log("getResourceBundle | Exception" , e.getMessage(), e, LoggerEnum.WARN.name());
				}
				return getResponseEntity(response, apiId, null);
			} else {
				ResponseParams params = new ResponseParams();
				params.setErr("1");
				params.setStatus(StatusType.failed.name());
				params.setErrmsg("Operation failed");
				response.setParams(params);
				response.getResponseCode();
				response.setResponseCode(ResponseCode.RESOURCE_NOT_FOUND);
				PlatformLogger.log("getResourceBundle | FailureResponse" , response, LoggerEnum.WARN.name());
				return getResponseEntity(response, apiId, null);
			}
		} catch (Exception e){
			PlatformLogger.log("getResourceBundle | Exception" , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@RequestMapping(value = "/ordinals", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> getOrdinals() {
		String apiId = "ekstep.config.ordinals.list";
		String ordinals = "";
		Response response = new Response();
		try {
			ordinals = HttpDownloadUtility.readFromUrl(baseUrl + "ordinals.json");
			PlatformLogger.log("Ordinals data read from s3 url" , StringUtils.isNotBlank(ordinals));
			ResponseParams params = new ResponseParams();
			params.setErr("0");
			params.setStatus(StatusType.successful.name());
			params.setErrmsg("Operation successful");
			response.setParams(params);
			response.put("ttl", 24.0);
			try {
				Map<String, Object> map = mapper.readValue(ordinals, new TypeReference<Map<String, Object>>() {
				});
				response.put("ordinals", map);
			} catch (Exception e) {
				PlatformLogger.log("Get Ordinals | Exception" , e.getMessage(), e, LoggerEnum.WARN.name());
			}
			PlatformLogger.log("Get Ordinals | Response" , response.getResponseCode());
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
				PlatformLogger.log("getOrdinalsException" , e.getMessage(), e);
				return getExceptionResponseEntity(e, apiId, null);
		}
	}

	private Map<String, String> getUrlFromS3() throws JsonProcessingException {
		Map<String, String> urlList = new HashMap<String, String>();
		String apiUrl = "";
		List<String> res = AWSUploader.getObjectList(folderName, "config");
		PlatformLogger.log("ResourceBundle Urls fetched from s3" , res.size());
		for (String data : res) {
			if (StringUtils.isNotBlank(FilenameUtils.getExtension(data))) {
				apiUrl = baseUrl + data;
				urlList.put(FilenameUtils.getBaseName(data), apiUrl);
			}
		}
		return urlList;
	}
}