package org.ekstep.config.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.common.util.HttpDownloadUtility;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.exception.ResponseCode;

@Controller
@RequestMapping("/v3/")
public class ConfigV3Controller extends BaseController {
	private ObjectMapper mapper = new ObjectMapper();
	public static final String folderName = "resources";
	public static final String baseUrl = "https://" + AWSUploader.getBucketName() + ".s3.amazonaws.com/";

	private static Logger LOGGER = LogManager.getLogger(ConfigController.class.getName());
	
	@RequestMapping(value = "/resourcebundles/list", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> getResourceBundles() {
		String apiId = "ekstep.config.resourebundles.list";
		try {
			Response response = new Response();
			Map<String, Object> resourcebundles = new HashMap<String, Object>();
			Map<String, String> urlMap = getUrlFromS3();
			for (Entry<String, String> entry : urlMap.entrySet()) {
				String langMap = HttpDownloadUtility.readFromUrl(entry.getValue());
				String langId = entry.getKey();
				try {
					if (StringUtils.isBlank(langMap))
						continue;
					Map<String, Object> map = mapper.readValue(langMap, new TypeReference<Map<String, Object>>() {
					});
					resourcebundles.put(langId, map);
				} catch (Exception e) {
					LOGGER.error("Error in fetching all ResourceBundles from s3"+ e.getMessage(), e);
				}
			}
			response.put("resourcebundles", resourcebundles);
			ResponseParams params = new ResponseParams();
			params.setErr("0");
			params.setStatus(StatusType.successful.name());
			params.setErrmsg("Operation successful");
			response.setParams(params);
			response.put("ttl", 24.0);
			LOGGER.info("get All ResourceBundles | Response: " + response + "Id" + apiId);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.warn("getAllResources | Exception" + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@RequestMapping(value = "/resourcebundles/read/{languageId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> getResourceBundle(@PathVariable(value = "languageId") String languageId) {
		String apiId = "ekstep.config.resourebundles.read";

		try {
			LOGGER.debug("ResourceBundle | GET | languageId" + languageId);
			Response response = new Response();
			String data = HttpDownloadUtility
					.readFromUrl(baseUrl + folderName + "/" + languageId + ".json");
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
					LOGGER.info("getResourceBundle | successResponse" + response);
				} catch (Exception e) {
					LOGGER.error("getResourceBundle | Exception" + e.getMessage(), e);
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
				LOGGER.info("getResourceBundle | FailureResponse" + response);
				return getResponseEntity(response, apiId, null);
			}
		} catch (Exception e) {
			LOGGER.warn("getResourceBundle | Exception" + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@RequestMapping(value = "/ordinals/list", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> getOrdinals() {
		String apiId = "ekstep.config.ordinals.list";
		String ordinals = "";
		Response response = new Response();
		try {
			ordinals = HttpDownloadUtility.readFromUrl(baseUrl + "ordinals.json");
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
				LOGGER.error("Get Ordinals | Exception" + e.getMessage(), e);
			}
			LOGGER.info("Get Ordinals | Response" + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.warn("getOrdinalsException" + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	private Map<String, String> getUrlFromS3() {
		Map<String, String> urlList = new HashMap<String, String>();
		String apiUrl = "";
		List<String> res = AWSUploader.getObjectList(folderName);
		LOGGER.info("ResourceBundle Urls fetched from s3" + res.size());
		for (String data : res) {
			if (StringUtils.isNotBlank(FilenameUtils.getExtension(data))) {
				apiUrl = baseUrl + data;
				urlList.put(FilenameUtils.getBaseName(data), apiUrl);
			}
		}
		return urlList;
	}
}