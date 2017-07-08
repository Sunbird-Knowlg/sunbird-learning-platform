package com.ilimi.taxonomy.controller;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.taxonomy.mgr.IContentManager;

/**
 * The Class ContentV2Controller, is the main entry point for the High Level
 * Content Operations, mostly it holds the API Method related to Content
 * Workflow Management such as 'Upload', 'Publish' 'Optimize', 'Extract' and
 * 'Bundle'. Other that these operation the Content can have other basic CRUD
 * Operations.
 * 
 * All the Methods are backed by their corresponding managers, which have the
 * actual logic to communicate with the middleware and core level APIs.
 * 
 * @author Azhar
 */
@Controller
@RequestMapping("/v3/content")
public class ContentV3Controller extends BaseController {

	private static ILogger LOGGER = PlatformLogManager.getLogger();

	@Autowired
	private IContentManager contentManager;

	/** The graph id. */
	private String graphId = "domain";

	/**
	 * This method carries all the tasks related to 'Upload' operation of
	 * content work-flow.
	 * 
	 *
	 * @param contentId
	 *            The Content Id for which the Content Package needs to be
	 *            Uploaded.
	 * @param file
	 *            The Content Package File
	 * @param userId
	 *            Unique id of the user mainly for authentication purpose, It
	 *            can impersonation details as well.
	 * @return The Response entity with Content Id in its Result Set.
	 */
	@RequestMapping(value = "/upload/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> upload(@PathVariable(value = "id") String contentId,
			@RequestParam(value = "file", required = true) MultipartFile file) {
		String apiId = "ekstep.learning.content.upload";
		LOGGER.log("Upload Content | Content Id: " + contentId);
		LOGGER.log("Uploaded File Name: " + file.getName());
		LOGGER.log("Calling the Manager for 'Upload' Operation | [Content Id " + contentId + "]");
        try {
            String name = FilenameUtils.getBaseName(file.getOriginalFilename()) + "_" + System.currentTimeMillis() + "."
                    + FilenameUtils.getExtension(file.getOriginalFilename());
            File uploadedFile = new File(name);
            file.transferTo(uploadedFile);
            Response response = contentManager.upload(contentId, "domain", uploadedFile);
            LOGGER.log("Upload | Response: " , response.getResponseCode());
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.log("Upload | Exception: " , e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
	}

	/**
	 * This method carries all the tasks related of bundling the contents into
	 * one package, It includes all the operations valid for the Publish
	 * operation but without making the status of content as 'Live'. i.e. It
	 * bundles content of all status with a 'expiry' date.
	 *
	 * @param map
	 *            the map contains the parameter for creating the Bundle e.g.
	 *            "identifier" List.
	 * @param userId
	 *            Unique 'id' of the user mainly for authentication purpose, It
	 *            can impersonation details as well.
	 * @return The Response entity with a Bundle URL in its Result Set.
	 */
	@RequestMapping(value = "/bundle", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> bundle(@RequestBody Map<String, Object> map) {
		String apiId = "ekstep.learning.content.archive";
		LOGGER.log("Create Content Bundle");
		try {
			Request request = getBundleRequest(map, ContentErrorCodes.ERR_CONTENT_INVALID_BUNDLE_CRITERIA.name());
			request.put(ContentAPIParams.version.name(), "v2");

			LOGGER.log("Calling the Manager for 'Bundle' Operation");
			Response response = contentManager.bundle(request, graphId, "1.1");
			LOGGER.log("Archive | Response: " , response.getResponseCode());
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	/**
	 * This method carries all the tasks related to 'Publish' operation of
	 * content work-flow.
	 *
	 * @param contentId
	 *            The Content Id which needs to be published.
	 * @param userId
	 *            Unique 'id' of the user mainly for authentication purpose, It
	 *            can impersonation details as well.
	 * @return The Response entity with Content Id and ECAR URL in its Result
	 *         Set.
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/publish/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> publish(@PathVariable(value = "id") String contentId,
			@RequestBody Map<String, Object> map) {
		String apiId = "ekstep.learning.content.publish";
		Response response;
		LOGGER.log("Publish content | Content Id : " + contentId);
		try {
			LOGGER.log("Calling the Manager for 'Publish' Operation | [Content Id " + contentId + "]", contentId);
			Request request = getRequest(map);
			Map<String, Object> requestMap = (Map<String, Object>) request.getRequest().get("content");
			if(null==requestMap.get("lastPublishedBy") || StringUtils.isBlank(requestMap.get("lastPublishedBy").toString())){
				return getExceptionResponseEntity(new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_PUBLISHER.name(), "Publisher User Id is blank"), apiId, null);
			}
			
			response = contentManager.publish(graphId, contentId, requestMap);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	/**
	 * This method carries all the tasks related to 'Review' operation of
	 * content work-flow.
	 *
	 * @param contentId
	 *            The Content Id which needs to be published.
	 * @param userId
	 *            Unique 'id' of the user mainly for authentication purpose, It
	 *            can impersonation details as well.
	 * @return The Response entity with Content Id in its Result
	 *         Set.
	 */
	@RequestMapping(value = "/review/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> review(@PathVariable(value = "id") String contentId,
			@RequestBody Map<String, Object> map) {
		String apiId = "ekstep.learning.content.review";
		Response response;
		LOGGER.log("Review content | Content Id : " + contentId);
		try {
			LOGGER.log("Calling the Manager for 'Review' Operation | [Content Id " + contentId + "]",contentId);
			Request request = getRequest(map);
			response = contentManager.review(graphId, contentId, request);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	/**
	 * This method fetches the hierarchy of a given content
	 *
	 * @param contentId
	 *            The Content Id whose hierarchy needs to be fetched
	 * @return The Response entity with Content hierarchy in the result set
	 */
	@RequestMapping(value = "/hierarchy/{id:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> hierarchy(@PathVariable(value = "id") String contentId,
			@RequestParam(value = "mode", required = false) String mode) {
		String apiId = "ekstep.learning.content.hierarchy";
		Response response;
		LOGGER.log("Content Hierarchy | Content Id : " + contentId);
		try {
			LOGGER.log("Calling the Manager for fetching content 'Hierarchy' | [Content Id " + contentId + "]", contentId);
			response = contentManager.getHierarchy(graphId, contentId, mode);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	/**
	 * This method fetches the Content by Content Id
	 *
	 * @param contentId
	 *            The Content Id whose hierarchy needs to be fetched
	 * @return The Response entity with Content hierarchy in the result set
	 */
	@RequestMapping(value = "/getbyid/{id:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> getById(@PathVariable(value = "id") String contentId,
			@RequestParam(value = "mode", required = false) String mode) {
		String apiId = "content.getById";
		Response response;
		LOGGER.log("Content GetById | Content Id : " + contentId);
		try {
			LOGGER.log("Calling the Manager for fetching content 'getById' | [Content Id " + contentId + "]", contentId);
			response = contentManager.getById(graphId, contentId, mode);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	protected String getAPIVersion() {
		return API_VERSION_3;
	}
}

