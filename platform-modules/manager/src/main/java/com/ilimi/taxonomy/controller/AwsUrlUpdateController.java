package com.ilimi.taxonomy.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Response;
import com.ilimi.common.logger.LogHelper;
import com.ilimi.common.mgr.IAwsUrlUpdateManager;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.common.util.PlatformLogger;

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
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	@Autowired
	private IAwsUrlUpdateManager awsUrlUpdateManager;



	/**
	 * This method contains the task related to fetching the list of nodes specific
	 * to the object type and graph Id provided in the input map and updating the
	 * AWS urls present in the properties of each of these nodes
	 *
	 * @param graphId
	 *            graph id of the object
	 * @param objectType
	 * 			  object type of the object
	 * @param userId
	 *            Unique 'id' of the user mainly for authentication purpose.
	 * @return The Response entity with the list of identifiers of failed nodes if there are any
	 */
	@RequestMapping(value = "/urlUpdateWithObjectType/{graphId}/{objectType}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> urlUpdateWithObjectType(@PathVariable(value = "graphId") String graphId, 
			@PathVariable(value = "objectType") String objectType,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.learning.aws.urls.update";
		LOGGER.log("API to update AWS urls");
		LOGGER.log(apiId + " | Graph : " + graphId + " | ObjectType: " + objectType);
		try {
			Response response = awsUrlUpdateManager.updateNodesWithUrl(objectType, graphId, apiId);
			return getResponseEntity(response, apiId, null);
		}catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * This method contains the task related to fetching the list of nodes specific
	 * to the graph Id and identifiers provided in request and updating the
	 * AWS urls present in the properties of each of these nodes
	 *
	 * @param graphId
	 *            graph id of the object
	 * @param objectType
	 * 			  object type of the object
	 * @param identifiers
	 * 		      list of identifiers for which url needs to be updated
	 * @param userId
	 *            Unique 'id' of the user mainly for authentication purpose.
	 * @return The Response entity with the list of identifiers of failed nodes if there are any
	 */
	@RequestMapping(value = "/urlUpdateWithIdentifiers/{graphId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> urlUpdateWithIdentifiers(@PathVariable(value = "graphId") String graphId, 
			@RequestParam(value = "identifiers", required = true) String[] identifiers,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.learning.aws.urls.update";
		LOGGER.log(apiId + " | Graph : " + graphId + " | Identifier: " + identifiers);
		try {
			Response response = awsUrlUpdateManager.updateNodesWithIdentifiers(
					graphId, identifiers, apiId);
			return getResponseEntity(response, apiId, null);

		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}


	protected String getAPIVersion() {
		return API_VERSION;
	}
}
