package com.ilimi.taxonomy.controller;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.learning.common.enums.ContentErrorCodes;
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
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.logger.LogHelper;
import com.ilimi.taxonomy.mgr.IContentManager;

@Controller
@RequestMapping("/v3/content")
public class ContentV3Controller extends BaseController {

	private static LogHelper LOGGER = LogHelper.getInstance(ContentV3Controller.class.getName());

	@Autowired
	private IContentManager contentManager;

	/** The graph id. */
	private String graphId = "domain";

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
			@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
		String apiId = "content.publish";
		Response response;
		LOGGER.info("Publish content | Content Id : " + contentId);
		try {
			LOGGER.info("Calling the Manager for 'Publish' Operation | [Content Id " + contentId + "]");
			Request request = getRequest(map);
			Map<String, Object> requestMap = (Map<String, Object>) request.getRequest().get("content");
			if(null==requestMap.get("publisher") && StringUtils.isBlank(requestMap.get("publisher").toString())){
				return getExceptionResponseEntity(new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_PUBLISHER_ID.name(), "Publisher Id is blank"), apiId, null);
			}
			
			response = contentManager.publish(graphId, contentId, requestMap.get("publisher").toString());
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	protected String getAPIVersion() {
		return API_VERSION_3;
	}

}