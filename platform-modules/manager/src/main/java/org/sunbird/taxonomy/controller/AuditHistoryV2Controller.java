package org.sunbird.taxonomy.controller;

import org.sunbird.common.controller.BaseController;
import org.sunbird.common.dto.Response;
import org.sunbird.taxonomy.mgr.IAuditHistoryManager;
import org.sunbird.telemetry.logger.TelemetryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * The Class AuditHistoryController, is the main entry point for the High Level
 * Audit Operations, mostly it holds the API Method related to Audit Logs
 * Workflow Management
 * 
 * All the Methods are backed by their corresponding manager classes, which have the
 * actual logic to communicate with the middleware and core level APIs.
 * 
 * @author Karthik, Rashmi
 */

@Controller
@RequestMapping("/v2/audit")
public class AuditHistoryV2Controller extends BaseController {
	
	/** The Logger */
	
	
	private String versionId = getAPIVersion();
	
	@Autowired
	private IAuditHistoryManager auditHistoryManager;
 
	
	/**
	 * This method carries all the tasks related to 'getAllLogs' operation of
	 * AuditHistory work-flow.
	 * 
	 * @param graphId
	 *            The graphId for which the Audit History needs to be fetched
	 *            
	 * @param userId
	 *            Unique id of the user mainly for authentication purpose, It
	 *            can impersonation details as well.
	 *            
	 * @return The Response entity with details of All AuditLogs in its ResultSet
	 */
	@RequestMapping(value = "/{graphId:.+}/all", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> getAll(@PathVariable(value = "graphId") String graphId,
			@RequestParam(value = "start", required = false) String startTime,
			@RequestParam(value = "end", required = false) String endTime,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.audit_history.list";

		TelemetryManager.log("get all AuditHistory | " + " GraphId: " + graphId + " | TimeStamp1: " + startTime + " | Timestamp2: "
				+ endTime);
		try {
			Response response = auditHistoryManager.getAuditHistory(graphId, startTime, endTime, versionId);
			TelemetryManager.log("Find Item | Response: " + response.getResponseCode());
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Find Item | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * This method carries all the tasks related to 'get AuditLogs By objectId' operation of
	 * AuditHistory work-flow.
	 * 
	 *
	 * @param graphId
	 *            The graphId for which the Audit History needs to be fetched
	 *            
	 * @param objectId
	 *            The objectId  for whose AuditLogs to be fetched      
	 *                 
	 * @param userId
	 *            Unique id of the user mainly for authentication purpose, It
	 *            can impersonation details as well.
	 *            
	 * @return The Response entity with details of All AuditLog for a given objectId
	 *  in its ResultSet
	 */
	@RequestMapping(value = "/history/{graphId:.+}/{objectId:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> getById(@PathVariable(value = "graphId") String graphId,
			@PathVariable(value = "objectId") String objectId,
			@RequestParam(value = "start", required = false) String startTime,
			@RequestParam(value = "end", required = false) String endTime,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.audit_history.info";

		TelemetryManager.log("get AuditHistory By ObjectId | " +  "GraphId: " + graphId + " | TimeStamp1: " + startTime
				+ " | Timestamp2: " + endTime + " | ObjectId: " + objectId);
		try {
			Response response = auditHistoryManager.getAuditHistoryById(graphId, objectId, startTime, endTime, versionId);
			TelemetryManager.log("Find Item | Response: " + response.getResponseCode());
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Find Item | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * This method carries all the tasks related to 'get Audit Logs By ObjectType' operation of
	 * AuditHistory work-flow.
	 * 
	 * @param graphId
	 *            The graphId for which the Audit History needs to be fetched
	 *            
	 * @param objectype
	 *           The objectType for a given graphId from which Audit Logs to be fetched
	 *           
	 * @param userId
	 *            Unique id of the user mainly for authentication purpose, It
	 *            can impersonation details as well.
	 *            
	 * @return The Response entity with details of All AuditLogs for a given ObjectType
	 *  in its ResultSet
	 */
	@RequestMapping(value = "/{graphId:.+}/{objectType:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> getByType(@PathVariable(value = "graphId") String graphId,
			@PathVariable(value = "objectType") String objectType,
			@RequestParam(value = "start", required = false) String startTime,
			@RequestParam(value = "end", required = false) String endTime,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.audit_history.info";

		TelemetryManager.log("get AuditHistory By ObjectType | " +  " GraphId: " + graphId + " | TimeStamp1: " + startTime
				+ " | Timestamp2: " + endTime + " | ObjectType: " + objectType);
		try {
			Response response = auditHistoryManager.getAuditHistoryByType(graphId, objectType, startTime, endTime, versionId);
			TelemetryManager.log("Find Item | Response: " , response.getResult());
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Find Item | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * This method carries all the tasks related to 'get LogRecord By objectId' operation of
	 * AuditHistory work-flow.
	 * 
	 * @param graphId
	 *            The graphId for which the Audit History needs to be fetched
	 *            
	 * @param objectId
	 *  	    The auditId for which the audit Log record needs to be fetched
	 *  
	 * @param userId
	 *            Unique id of the user mainly for authentication purpose, It
	 *            can impersonation details as well.
	 *            
	 * @return The Response entity with details of All Audit LogRecord for a given objectId
	 *  in its ResultSet
	 */
	@RequestMapping(value = "/details/{objectId:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> getLogRecord(@PathVariable(value = "objectId") String objectId,
			@RequestParam(value = "timestamp", required = false) String timeStamp,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.audit_history.info";

		TelemetryManager.log("get AuditHistory By objectId | TimeStamp1: " + timeStamp
				+ " | objectId: " + objectId);
		try {
				Response response = auditHistoryManager.getAuditLogRecordById(objectId, timeStamp);
				TelemetryManager.log("Find Item | Response: " + response.getResponseCode());
				return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Find Item | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Response> deleteAuditHistory(
			@RequestParam(value = "start", required = false) String start,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.audit_history.delete";

		TelemetryManager.log("delete AuditHistory By date| date: " + start
				+ " | objectId: " + start);
		try {
				Response response = auditHistoryManager.deleteAuditHistory(start);
				TelemetryManager.log("delete Item | Response: " + response.getResponseCode());
				return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Find Item | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	 protected String getAPIVersion() {
	        return API_VERSION_2;
	 }
}
