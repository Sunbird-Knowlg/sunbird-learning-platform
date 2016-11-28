package com.ilimi.taxonomy.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Response;
import com.ilimi.taxonomy.mgr.IAssetCleanUpManager;

/**
 * AwsAssetCleanupContoller is used to clean the unused assets in S3
 * 
 */
@Controller
@RequestMapping("/v2/cleanUp")
public class AwsAssetCleanupContoller extends BaseController{
	

	@Autowired
	IAssetCleanUpManager cleanUpManager;
	
	
	/**
	 * This method gets the URLs of the assets stored in S3
	 * 
	 * @param List of folders to be considered  
	 * 
	 * @return ResponseEntity as success if it is able to fetch the URls and
	 * create a file of AWS URls
	 */
	@RequestMapping(value = "/getAWSUrls",  method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> getAWSUrls(@RequestBody List<String> folders) {
		Response response = null;
		try {
			response = cleanUpManager.getObjectsOnS3(folders);
		} catch (Exception e) {
			return getResponseEntity(getErrorResponse(e), null, null);
		}
		return getResponseEntity(response, null, null);
	}
	
	/**
	 * This method gets the node from the graph based on graphId and objectType
	 * and creates a document in ElasticSearch
	 * 
	 * @param ResponseBody - Map of GraphId and objectType
	 * 
	 * @return ResponseEntity as success if it is able to fetch the nodes from graph and 
	 * create a document in ElasticSearch
	 */
	@RequestMapping(value = "/syncESwithGraph", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> syncESwithGraph(@RequestBody Map<String, Object> request) {
		Response response = null;
		try {
			response = cleanUpManager.synchGraphNodestoES(request);
		} catch (Exception e) {
			return getResponseEntity(getErrorResponse(e), null, null);
		}
		return getResponseEntity(response, null, null);
	}
	
	/**
	 * This method will search for the URls of S3 using ElasticSearch if the URL is 
	 * used in the graph then it is added to used URLs list else added to unused URLs list
	 * 
	 * @param ResponseBody - Map of GraphId and objectType
	 * 
	 * @return ResponseEntity as success if it
	 */
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> searchURLsInES(@RequestBody Map<String, Object> request) {
		Response response = null;
		try {
			response = cleanUpManager.searchElasticSearch(request);
		} catch (Exception e) {
			return getResponseEntity(getErrorResponse(e), null, null);
		}
		return getResponseEntity(response, null, null);
	}
	
	/**
	 * Method to delete unused files on S3
	 * 
	 * @return ResponseEntity as success if it deletes the unused files on S3
	 */
	//TODO: Not tested as this might delete from S3
	@RequestMapping(value = "/deleteFromS3", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> deleteFromS3() {
		Response response = null;
		try {
			response = cleanUpManager.deleteUnusedFilesOnS3();
		} catch (Exception e) {
			return getResponseEntity(getErrorResponse(e), null, null);
		}
		return getResponseEntity(response, null, null);
	}
}
