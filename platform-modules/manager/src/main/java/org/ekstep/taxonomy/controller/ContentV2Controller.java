package org.ekstep.taxonomy.controller;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.controller.BaseController;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.taxonomy.mgr.IContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

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
@RequestMapping("/v2/content")
public class ContentV2Controller extends BaseController {

	/** The Class Logger. */

	@Autowired
	private IContentManager contentManager;

	/**
	 * This method fetches the Content by Content Id
	 *
	 * @param contentId
	 *            The Content Id whose hierarchy needs to be fetched
	 * @return The Response entity with Content hierarchy in the result set
	 */
	@RequestMapping(value = "/{id:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> find(@PathVariable(value = "id") String contentId,
			@RequestParam(value = "fields", required = false) String[] fields,
			@RequestParam(value = "mode", required = false) String mode) {
		String apiId = "ekstep.learning.content.info";
		TelemetryManager
				.info("Executing Content Get API (Java Version) (API Version V2) For Content Id: " + contentId + ".");
		Response response;
		TelemetryManager.log("Content GetById | Content Id : " + contentId);
		try {
			TelemetryManager.log("Calling the Manager for fetching content 'getById' | [Content Id " + contentId + "]");
			response = contentManager.find(contentId, mode, convertStringArrayToList(fields));
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@RequestBody Map<String, Object> requestMap,
			@RequestHeader(value = "X-Channel-Id", required = true) String channelId) {
		String apiId = "ekstep.learning.content.create";
		TelemetryManager.info("Executing Content Create API (Java Version) (API Version V2).", requestMap);
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("content");
			Response response = contentManager.create(map, channelId);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{id:.+}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Response> update(@PathVariable(value = "id") String contentId,
			@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.learning.content.update";
		TelemetryManager.info(
				"Executing Content Update API (Java Version) (API Version V2) For Content Id: " + contentId + ".",
				requestMap);
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("content");
			Response response = contentManager.update(contentId, map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

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
			@RequestParam(value = "file", required = false) MultipartFile file,
			@RequestParam(value = "fileUrl", required = false) String fileUrl,
			@RequestParam(value = "mimeType", required = false) String mimeType) {
		String apiId = "ekstep.learning.content.upload";
		TelemetryManager.log("Upload Content | Content Id: " + contentId);
		if (StringUtils.isBlank(fileUrl) && null == file) {
			return getExceptionResponseEntity(
					new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_UPLOAD_RESOURCE.name(),
							"File or fileUrl should be available."),
					apiId, null);
		} else {
			try {
				if (StringUtils.isNotBlank(fileUrl)) {
					Response response = contentManager.upload(contentId, fileUrl, mimeType);
					TelemetryManager.log("Upload | Response: " + response.getResponseCode());
					return getResponseEntity(response, apiId, null);
				} else {
					String name = FilenameUtils.getBaseName(file.getOriginalFilename()) + "_"
							+ System.currentTimeMillis() + "." + FilenameUtils.getExtension(file.getOriginalFilename());
					File uploadedFile = new File(name);
					file.transferTo(uploadedFile);
					Response response = contentManager.upload(contentId, uploadedFile, mimeType);
					TelemetryManager.log("Upload | Response: ", response.getResult());
					return getResponseEntity(response, apiId, null);
				}
			} catch (Exception e) {
				TelemetryManager.error("Upload | Exception: " + e.getMessage(), e);
				return getExceptionResponseEntity(e, apiId, null);
			}
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
	@RequestMapping(value = "/publish/{id:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> publish(@PathVariable(value = "id") String contentId) {
		String apiId = "ekstep.learning.content.publish";
		TelemetryManager.log("Publish content | Content Id : " + contentId);
		try {
			TelemetryManager.log("Calling the Manager for 'Publish' Operation | [Content Id " + contentId + "]");
			Response response = contentManager.publish(contentId, null);

			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * This method carries all the tasks related to 'Optimize' operation of
	 * content work-flow. This includes compressing images, audio and videos.
	 *
	 * @param contentId
	 *            Content Id which needs to be optimized.
	 * @param userId
	 *            Unique 'id' of the user mainly for authentication purpose, It
	 *            can impersonation details as well.
	 * @return The Response entity with Content Id in its Result Set.
	 */
	@RequestMapping(value = "/optimize/{id:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> optimize(@PathVariable(value = "id") String contentId) {
		String apiId = "ekstep.learning.content.optimize";
		TelemetryManager.log("Optimize content | Content Id : " + contentId);
		try {
			TelemetryManager.log("Calling the Manager for 'Optimize' Operation | [Content Id " + contentId + "]");
			Response response = contentManager.optimize(contentId);

			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception: " + e.getMessage(), e);
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
		TelemetryManager.log("Create Content Bundle");
		try {
			Request request = getBundleRequest(map, ContentErrorCodes.ERR_CONTENT_INVALID_BUNDLE_CRITERIA.name());
			request.put(ContentAPIParams.version.name(), "v2");

			TelemetryManager.log("Calling the Manager for 'Bundle' Operation");
			Response response = contentManager.bundle(request, "1.1");
			TelemetryManager.log("Archive | Response: " + response);

			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception: " + e.getMessage(), e);
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
		TelemetryManager.log("Content Hierarchy | Content Id : " + contentId);
		try {
			TelemetryManager
					.log("Calling the Manager for fetching content 'Hierarchy' | [Content Id " + contentId + "]");
			response = contentManager.getHierarchy(contentId, mode, null);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/upload/url/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> preSignedURL(@PathVariable(value = "id") String contentId,
			@RequestBody Map<String, Object> map) {
		String apiId = "ekstep.learning.content.upload.url";
		TelemetryManager.log("Upload URL content | Content Id : " + contentId);
		Response response;
		try {
			Request request = getRequest(map);
			Map<String, Object> requestMap = (Map<String, Object>) request.getRequest().get("content");
			String fileName = null;
			if (null != requestMap) {
				fileName = (String) requestMap.get("fileName");
				if (StringUtils.isBlank(fileName)) {
					return getExceptionResponseEntity(new ClientException(
							ContentErrorCodes.ERR_CONTENT_BLANK_FILE_NAME.name(), "File name is blank"), apiId, null);
				}
			} else {
				return getExceptionResponseEntity(new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT.name(),
						"content object is blank"), apiId, null);
			}
			response = contentManager.preSignedURL(contentId, fileName, "assets");
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/hierarchy/update", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Response> updateHierarchy(@RequestBody Map<String, Object> requestMap) {
		String apiId = "content.hierarchy.update";
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("data");
			Response response = contentManager.updateHierarchy(map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	protected String getAPIVersion() {
		return API_VERSION_2;
	}
}