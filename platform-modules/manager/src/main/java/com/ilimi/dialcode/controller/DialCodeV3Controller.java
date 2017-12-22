package com.ilimi.dialcode.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.dialcode.mgr.IDialCodeManager;

/**
 * Controller Class for All CRUD Operation of QR Codes (DIAL Code).
 * 
 * This class is entry point for all operation related to DIAL Code.
 * 
 * @author gauraw
 *
 */
@Controller
@RequestMapping("/v3/dialcode")
public class DialCodeV3Controller extends BaseController{
	 
	@Autowired
	private IDialCodeManager dialCodeManager;

	/**
	 * Generate Dial Codes.
	 * 
	 * @param requestMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/generate", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> generateDialCode(@RequestBody Map<String, Object> requestMap,@RequestHeader(value = "X-Channel-Id") String channelId) {

		String apiId = "ekstep.dialcode.generate";
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("dialcodes");
			Response response = dialCodeManager.generateDialCode(map, channelId);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			PlatformLogger.log("Exception Occured while generating Dial Code : ", e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Fetch Dial Code Details
	 * 
	 * @param dialCodeId
	 * 
	 * @return
	 */
	@RequestMapping(value = "/read/{id:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> readDialCode(@PathVariable(value = "id") String dialCodeId) {
		String apiId = "ekstep.dialcode.info";
		try {
			Response response = dialCodeManager.readDialCode(dialCodeId);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			PlatformLogger.log("Exception Occured while reading Dial Code details : ", e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * 
	 * @param dialCodeId
	 * @param requestMap
	 * @param channelId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/update/{id:.+}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Response> updateDialCode(@PathVariable(value = "id") String dialCodeId,
			@RequestBody Map<String, Object> requestMap,@RequestHeader(value = "X-Channel-Id") String channelId) {
		String apiId = "ekstep.dialcode.update";
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("dialcode");
			Response response = dialCodeManager.updateDialCode(dialCodeId, channelId, map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			PlatformLogger.log("Exception Occured while updating Dial Code : ", e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * 
	 * @param requestMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> listDialCode(@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.dialcode.list";
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("search");
			Response response = dialCodeManager.listDialCode(map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			PlatformLogger.log("Exception Occured while Performing List Operation for Dial Codes : " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	/**
	 * 
	 * @param dialCodeId
	 * @param userId
	 * @param channelId
	 * @return
	 */
	@RequestMapping(value = "/publish/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> publishDialCode(@PathVariable(value = "id") String dialCodeId,
			@RequestHeader(value = "user-id") String userId,@RequestHeader(value = "X-Channel-Id") String channelId) {
		String apiId = "ekstep.dialcode.publish";
		try {
			Response response = dialCodeManager.publishDialCode(dialCodeId, channelId);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			PlatformLogger.log("Exception Occured while Performing Publish Operation on Dial Code : " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
}