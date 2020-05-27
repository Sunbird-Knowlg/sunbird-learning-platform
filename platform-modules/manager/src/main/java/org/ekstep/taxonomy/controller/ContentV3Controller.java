package org.ekstep.taxonomy.controller;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Slug;
import org.ekstep.common.controller.BaseController;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
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

import javax.ws.rs.PathParam;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The Class ContentV3Controller, is the main entry point for the High Level
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
@RequestMapping("/content/v3")
public class ContentV3Controller extends BaseController {

	@Autowired
	private IContentManager contentManager;

	private static final String CHANNEL_ID = "X-Channel-Id";

	private String UNDERSCORE = "_";

	private String DOT = ".";

	private List<String> preSignedObjTypes = Arrays.asList("assets", "artifact", "hierarchy");

	@SuppressWarnings("unchecked")
//	@RequestMapping(value = "/create", method = RequestMethod.POST)
//	@ResponseBody
	public ResponseEntity<Response> create(@RequestBody Map<String, Object> requestMap,
			@RequestHeader(value = CHANNEL_ID, required = true) String channelId) {
		String apiId = "ekstep.learning.content.create";
		TelemetryManager.log("Executing Content Create API (Java Version) (API Version V3).", requestMap);
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
//	@RequestMapping(value = "/update/{id:.+}", method = RequestMethod.PATCH)
//	@ResponseBody
	public ResponseEntity<Response> update(@PathVariable(value = "id") String contentId,
			@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.learning.content.update";
		TelemetryManager.log(
				"Executing Content Update API (Java Version) (API Version V3) For Content Id: " + contentId + ".",
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
	 * @return The Response entity with Content Id in its Result Set.
	 */
//	@RequestMapping(value = "/upload/{id:.+}", method = RequestMethod.POST)
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
					String name = FilenameUtils.getBaseName(file.getOriginalFilename()) + UNDERSCORE
							+ System.currentTimeMillis() + DOT + FilenameUtils.getExtension(file.getOriginalFilename());
					File uploadedFile = new File(name);
					file.transferTo(uploadedFile);
					uploadedFile = new File(name);
					Response response = contentManager.upload(contentId, uploadedFile, mimeType);
					TelemetryManager.log("Upload | Response: " + response.getResponseCode());
					return getResponseEntity(response, apiId, null);
				}
			} catch (Exception e) {
				TelemetryManager.error("Exception: " + e.getMessage(), e);
				return getExceptionResponseEntity(e, apiId, null);
			}
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
			TelemetryManager.log("Archive | Response: " + response.getResponseCode());
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * This method carries all the tasks related to 'Publish' operation of
	 * content work-flow.
	 *
	 * @param contentId
	 *            The Content Id which needs to be published.
	 * @return The Response entity with Content Id and ECAR URL in its Result
	 *         Set.
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "/publish/{id:.+}", "/public/publish/{id:.+}" }, method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> publish(@PathVariable(value = "id") String contentId,
			@RequestBody Map<String, Object> map) {
		String apiId = "ekstep.learning.content.publish";
		Response response;
		TelemetryManager.log("Publish content | Content Id : " + contentId);
		try {
			TelemetryManager
					.log("Calling the Manager for 'Publish' Operation | [Content Id " + contentId + "]" + contentId);
			Request request = getRequest(map);
			Map<String, Object> requestMap = (Map<String, Object>) request.getRequest().get("content");
			requestMap.put("publish_type", ContentWorkflowPipelineParams.Public.name().toLowerCase());

			if (null == requestMap.get("lastPublishedBy")
					|| StringUtils.isBlank(requestMap.get("lastPublishedBy").toString())) {
				return getExceptionResponseEntity(
						new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_PUBLISHER.name(),
								"Publisher User Id is blank"),
						apiId, null);
			}

			response = contentManager.publish(contentId, requestMap);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * This method carries all the tasks related to 'Unlisted Publish' operation
	 * of content work-flow.
	 *
	 * @param contentId
	 *            The Content Id which needs to be published.
	 * @return The Response entity with Content Id and ECAR URL in its Result
	 *         Set.
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/unlisted/publish/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> publishUnlisted(@PathVariable(value = "id") String contentId,
			@RequestBody Map<String, Object> map) {
		String apiId = "ekstep.learning.content.unlisted.publish";
		Response response;
		TelemetryManager.log(" as Unlisted content | Content Id : " + contentId);
		try {
			TelemetryManager.log("Calling the Manager for 'Unlisted Publish' Operation | [Content Id " + contentId + "]"
					+ contentId);
			Request request = getRequest(map);
			Map<String, Object> requestMap = (Map<String, Object>) request.getRequest().get("content");
			requestMap.put("publish_type", ContentWorkflowPipelineParams.Unlisted.name().toLowerCase());

			if (null == requestMap.get("lastPublishedBy")
					|| StringUtils.isBlank(requestMap.get("lastPublishedBy").toString())) {
				return getExceptionResponseEntity(
						new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_PUBLISHER.name(),
								"Unlisted Publisher User Id is blank"),
						apiId, null);
			}

			response = contentManager.publish(contentId, requestMap);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * This method carries all the tasks related to 'Review' operation of
	 * content work-flow.
	 *
	 * @param contentId
	 *            The Content Id which needs to be published.
	 * @return The Response entity with Content Id in its Result Set.
	 */
	@RequestMapping(value = "/review/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> review(@PathVariable(value = "id") String contentId,
			@RequestBody Map<String, Object> map) {
		String apiId = "ekstep.learning.content.review";
		Response response;
		TelemetryManager.log("Review content | Content Id : " + contentId);
		try {
			TelemetryManager
					.log("Calling the Manager for 'Review' Operation | [Content Id " + contentId + "]" + contentId);
			Request request = getRequest(map);
			response = contentManager.review(contentId, request);
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
//	@RequestMapping(value = "/hierarchy/{id:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> hierarchy(@PathVariable(value = "id") String contentId,
											  @RequestParam(value = "mode", required = false) String mode,
											  @RequestParam(value = "fields", required = false) String[] fields) {
		String apiId = "ekstep.learning.content.hierarchy";
		Response response;
		TelemetryManager.log("Content Hierarchy | Content Id : " + contentId);
		try {
			TelemetryManager.log("Calling the Manager for fetching content 'Hierarchy' | [Content Id " + contentId + "]"
					+ contentId);
			List<String> reqFields = convertStringArrayToList(fields);
			// This is to support portal backward compatibility. Remove after 1.14.0 final sprint.
			if (reqFields.size() == 1 && StringUtils.equalsIgnoreCase( reqFields.get(0), "versionKey"))
				reqFields = null;
			response = contentManager.getContentHierarchy(contentId, null, mode, reqFields);

			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * This method fetches the hierarchy of a given bookMark in a Collection
	 *
	 * @param contentId
	 *            The Content Id whose hierarchy needs to be fetched
	 *
	 * @param bookmarkId The BookMarkId for the which the hierarchy is to be fetched.
	 * @return The Response entity with Content hierarchy in the result set
	 */
//	@RequestMapping(value = "/hierarchy/{id:.+}/{bookmarkId:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> hierarchy(@PathVariable(value = "id") String contentId,
											  @PathVariable(value = "bookmarkId") String bookmarkId,
											  @RequestParam(value = "mode", required = false) String mode,
											  @RequestParam(value = "fields", required = false) String[] fields) {
		String apiId = "ekstep.learning.content.hierarchy";
		Response response;
		TelemetryManager.log("Content Hierarchy | Content Id : " + contentId);
		try {
			TelemetryManager.log("Calling the Manager for fetching content 'Hierarchy' | [Content Id " + contentId + "]"
					+ contentId);
			List<String> reqFields = convertStringArrayToList(fields);
			response = contentManager.getContentHierarchy(contentId, bookmarkId, mode, reqFields);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception: " + e.getMessage(), e);
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
//	@RequestMapping(value = "/read/{id:.+}", method = RequestMethod.GET)
//	@ResponseBody
	public ResponseEntity<Response> find(@PathVariable(value = "id") String contentId,
			@RequestParam(value = "fields", required = false) String[] fields,
			@RequestParam(value = "mode", required = false) String mode) {
		String apiId = "ekstep.content.find";
		Response response;
		TelemetryManager.log("Content Find | Content Id : " + contentId);
		try {
			TelemetryManager.log(
					"Calling the Manager for fetching content 'getById' | [Content Id " + contentId + "]" + contentId);
			response = contentManager.find(contentId, mode, convertStringArrayToList(fields));
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@SuppressWarnings("unchecked")
//	@RequestMapping(value = "/upload/url/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> preSignedURL(@PathVariable(value = "id") String contentId,
												 @RequestBody Map<String, Object> map, @PathParam(value = "type") String type) {
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
				if (StringUtils.isBlank(FilenameUtils.getBaseName(fileName)) || StringUtils.length(Slug.makeSlug(fileName, true)) > 256) {
					return getExceptionResponseEntity(new ClientException(
							ContentErrorCodes.ERR_CONTENT_INVALID_FILE_NAME.name(), "Please Provide Valid File Name."), apiId, null);
				}
			} else {
				return getExceptionResponseEntity(new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT.name(),
						"content object is blank"), apiId, null);
			}
			if (StringUtils.isBlank(type)) type = "assets";

			if (!preSignedObjTypes.contains(type.toLowerCase())) {
				return getExceptionResponseEntity(new ClientException(ContentErrorCodes.ERR_INVALID_PRESIGNED_URL_TYPE.name(),
						"Invalid pre-signed url type. It should be one of " + StringUtils.join(preSignedObjTypes, ",")), apiId, null);
			}

			response = contentManager.preSignedURL(contentId, fileName, type.toLowerCase());
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@SuppressWarnings("unchecked")
//	@RequestMapping(value = "/hierarchy/update", method = RequestMethod.PATCH)
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

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/hierarchy/sync/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> syncHierarchy(@PathVariable(value = "id") String identifier) {
		String apiId = "content.hierarchy.sync";
		try {
			Response response = contentManager.syncHierarchy(identifier);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Controller Method to Link QR Code (DIAL Code) with Content
	 * 
	 * @author gauraw
	 * @param requestMap
	 * @return
	 */
	@RequestMapping(value = "/dialcode/link", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> linkDialCode(@RequestBody Map<String, Object> requestMap,
			@RequestHeader(value = CHANNEL_ID, required = true) String channelId) {
		String apiId = "ekstep.content.dialcode.link";
		Request request = getRequest(requestMap);
		try {
			Object reqObj = request.get("content");
			Response response = contentManager.linkDialCode(channelId, reqObj, null, null);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception occured while Linking Dial Code with Content: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	/**
	 * Controller Method to Link QR Code (DIAL Code) with Content
	 * 
	 * @author amitp
	 * @param contentId
	 *            The Content Id for whom DIAL Codes have to be reserved           
	 * @return
	 */
	@RequestMapping(value = "/dialcode/reserve/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> reserveDialCode(
			@PathVariable(value = "id") String contentId,
			@RequestBody Map<String, Object> requestMap,
			@RequestHeader(value = CHANNEL_ID, required = true) String channelId) {
		String apiId = "ekstep.learning.content.dialcode.reserve";
		TelemetryManager.log("Reserve DIAL Codes | Content Id : " + contentId);
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get(ContentAPIParams.dialcodes.name());
			Response response = contentManager.reserveDialCode(contentId, channelId, map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception occured while Reserving DIAL Codes with Content: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Controller method to Releases all not Linked QR Codes (DIAL Codes) from Textbook.
	 *
	 * @param contentId
	 * 				The Content Id of the Textbook from which DIAL Codes have to be released
	 * @return The Response Entity with list of Released QR Codes
	 */
	@RequestMapping(value="/dialcode/release/{id}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Response> releaseDialcodes(@PathVariable(value="id") String contentId,
													 @RequestHeader(value = CHANNEL_ID) String channelId) {
		String apiId = "ekstep.learning.content.dialcode.release";
		TelemetryManager.log("Release DIAL Codes | Content Id : " + contentId);
		Response response;
		try {
			response = contentManager.releaseDialcodes(contentId, channelId);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception occured while Releasing DIAL Codes with Content: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * This method copy the Content by Content Id
	 *
	 * @param contentId
	 *            The Content Id whose hierarchy needs to be fetched
	 * @return The Response entity with given ContentId and new ContentId in the
	 *         result set
	 */
	@SuppressWarnings("unchecked")
//	@RequestMapping(value = "/copy/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> copy(@PathVariable(value = "id") String contentId,
			@RequestParam(value = "mode", required = false) String mode, @RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.content.copy";
		Response response;
		TelemetryManager.log("Content Copy | Content Id : " + contentId);
		try {
			Request request = getRequest(requestMap);
			TelemetryManager.log(
					"Calling the Manager for copying content 'getById' | [Content Id " + contentId + "]" + contentId);
			Map<String, Object> map = (Map<String, Object>) request.get("content");
			response = contentManager.copyContent(contentId, map, mode);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception occured while copying Content: " + contentId + " :: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	protected String getAPIVersion() {
		return API_VERSION_3;
	}

	@RequestMapping(value="/retire/{id:.+}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Response> retire(@PathVariable(value = "id") String contentId) {
		String apiId = "ekstep.content.retire";
		TelemetryManager.log("Retiring content | Content Id : " + contentId);
		Response response;
		try {
			response = contentManager.retire(contentId);
			return getResponseEntity(response, apiId, null);
		} catch(Exception e) {
			TelemetryManager.error("Exception occured while Retiring Content: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
//	@RequestMapping(value="/flag/accept/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> acceptFlag(@PathVariable(value = "id") String contentId){
		String apiId = "ekstep.content.accept.flag";
		TelemetryManager.log("Accept flagged content | Content Id : " + contentId);
		Response response;
		try {
			response = contentManager.acceptFlag(contentId);
			return getResponseEntity(response, apiId, null);
		} catch(Exception e) {
			TelemetryManager.error("Exception occured while Accepting Flagged Content: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * API to Reject a Flag in content workflow
	 *
	 * @param contentId
	 *            The Flagged Content Id whose flag needs to be rejected.
	 * @return The Response entity with Content Id and Version Key in its Result
	 *         Set.
	 */
	@RequestMapping(value="/flag/reject/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> rejectFlag(@PathVariable(value = "id") String contentId){
		String apiId = "ekstep.learning.content.rejectFlag";
		TelemetryManager.log("Reject flagged content | Content Id : " + contentId);
		Response response;
		try {
			response = contentManager.rejectFlag(contentId);
			return getResponseEntity(response, apiId, null);
		} catch(Exception e) {
			TelemetryManager.error("Exception occured while Rejecting Flagged Content: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * API to Flag a content in content workflow
	 *
	 * @param contentId
	 *            The Content Id which needs to be flagged.
	 * @return The Response entity with Content Id and Version Key in its Result
	 *         Set.
	 */
//	@RequestMapping(value="/flag/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> flag(@PathVariable(value = "id") String contentId,
			@RequestBody Map<String, Object> map){
		String apiId = "ekstep.learning.content.flag";
		TelemetryManager.log("Flag content | Content Id : " + contentId);
		Response response;
		try {
			Request request = getRequest(map);
			Map<String, Object> requestMap = request.getRequest();
			response = contentManager.flag(contentId, requestMap);
			return getResponseEntity(response, apiId, null);
		} catch(Exception e) {
			TelemetryManager.error("Exception occured while Flagging Content: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Controller method to discard all the changes made by the user, which is in draft state
	 * @param contentId
	 * @return
	 */
//	@RequestMapping(value = "/discard/{id:.+}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Response> discard(@PathVariable(value = "id") String contentId) {
		String apiId = "ekstep.learning.content.discard";
		TelemetryManager.log("Discarding Changes | Content Id : " + contentId);
		Response response;
		try {
			response = contentManager.discardContent(contentId);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception occured while Discarding Content : " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);

		}
	}

	/**
	 * Controller method to reject content
	 * @param contentId
	 * @return
	 */
	@RequestMapping(value = "/reject/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> rejectContent(@PathVariable(value = "id") String contentId,
												  @RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.learning.content.reject";
		TelemetryManager.log("Content Review Reject operation for identifier : " + contentId);
		Request request = getRequest(requestMap);
		Response response;
		try {
			Map<String, Object> map = (Map<String,Object>) request.get("content");
			response = contentManager.rejectContent(contentId, map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception occured while rejecting content : " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);

		}
	}
}