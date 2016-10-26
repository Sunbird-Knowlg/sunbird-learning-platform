package com.ilimi.taxonomy.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.MiddlewareException;
import com.ilimi.common.logger.LogHelper;
import com.ilimi.taxonomy.mgr.IAwsUrlUpdateManager;

/**
 * The Class AwsUrlUpdateController, is the main entry point for 
 * operation related to AWS relocation and existing URL updates
 * 
 * All the Methods are backed by their corresponding managers, which have the
 * actual logic to communicate with the middleware and core level APIs.
 * 
 * @author Jyotsna
 */
@Controller
@RequestMapping("/v1/AWS")
public class AwsUrlUpdateController extends BaseController {

	/** The Class Logger. */
	private static LogHelper LOGGER = LogHelper.getInstance(AwsUrlUpdateController.class.getName());

	@Autowired
	private IAwsUrlUpdateManager awsUrlUpdateManager;


	
	/**
	 * This method contains the task related to fetching the list of nodes specific
	 * to the object type and graph Id provided in the input map and updating the
	 * AWS urls present in the properties of each of these nodes
	 *
	 * @param map
	 *            the map contains the object type and graph Id of the node
	 * @param userId
	 *            Unique 'id' of the user mainly for authentication purpose.
	 * @return The Response entity with the list of identifiers of failed nodes if there are any
	 */
	@RequestMapping(value = "/urlUpdate", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> urlUpdate(@RequestBody Map<String, Object> map,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "aws.urls";
		LOGGER.info("API to update AWS urls:"+apiId);
		boolean requestFlag = false;
		try {
			Request request = getRequest(map);
			Map<String, Object> requestMap = request.getRequest();
			if (null != requestMap && !requestMap.isEmpty()) {
				if(requestMap.get("objectType")!=null && requestMap.get("graphId")!=null)
				{
					requestFlag = true;
				}
			} 
			if(requestFlag){
				LOGGER.info("Invoking API to update AWS urls of existing nodes:"+apiId);
				Response response = awsUrlUpdateManager.updateNodesWithUrl((String)requestMap.get("objectType"), 
						(String)requestMap.get("graphId"), apiId);
				return getResponseEntity(response, apiId, null);
			}else{
				throw new MiddlewareException("INVALID_NODE_PARAMETERS", "Invalid request body");
			}	
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	
	protected String getAPIVersion() {
		return API_VERSION;
	}
}
