package com.ilimi.taxonomy.mgr.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.logger.LogHelper;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.dac.dto.AuditHistoryRecord;
import com.ilimi.dac.enums.CommonDACParams;
import com.ilimi.dac.impl.IAuditHistoryEsService;
import com.ilimi.taxonomy.enums.AuditLogErrorCodes;
import com.ilimi.taxonomy.mgr.IAuditHistoryManager;

/**
 * The Class AuditHistoryManager provides implementations of the various
 * operations defined in the IAuditHistoryManager
 * 
 * @author Karthik, Rashmi
 * 
 * @see IAuditHistoryManager
 */
@Component("auditHistoryManager")
public class AuditHistoryManager implements IAuditHistoryManager {

	@Autowired
	IAuditHistoryEsService auditHistoryEsService;

	/** The Logger */
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.taxonomy.mgr.IAuditHistoryManager
	 * #saveAuditHistory(java.lang.String, java.lang.String, java.io.File,
	 * java.lang.String)
	 */
	@Override
	// @Async
	public void saveAuditHistory(AuditHistoryRecord audit) {
		LOGGER.log("setting request object from audit history record" + audit);
		if (null != audit) {
			LOGGER.log("Checking if audit record is empty or not" + audit);
			if (StringUtils.isBlank(audit.getObjectId())) {
				LOGGER.log("Throws Client Exception when audit record is null");
				throw new ClientException(AuditLogErrorCodes.ERR_SAVE_AUDIT_MISSING_REQ_PARAMS.name(),
						"Required params missing...");
			}	
			LOGGER.log("checking if requestId is null or not" + audit.getRequestId());
			Request request = new Request();
			request.setRequest_id(audit.getRequestId());
			request.put(CommonDACParams.audit_history_record.name(), audit);
			LOGGER.log("Sending request to save Logs to DB" , request);
			auditHistoryEsService.saveAuditHistoryLog(request);
		} else {
			LOGGER.log("Throws new exception on processing audit history record");
			throw new ClientException(AuditLogErrorCodes.ERR_INVALID_AUDIT_RECORD.name(), "audit record is null.");
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.taxonomy.mgr.IAuditHistoryManager
	 * #getAuditHistory(java.lang.String, java.lang.String, java.io.File,
	 * java.lang.String)
	 */
	@Override
	public Response getAuditHistory(String graphId, String startTime, String endTime, String versionId) {
		Request request = new Request();
		try {
			LOGGER.log("Checking if graphId is empty or not" + graphId);
			if (StringUtils.isNotBlank(graphId)) {
				request.put(CommonDACParams.graph_id.name(), graphId);
			}
			request.put(CommonDACParams.start_date.name(), startTime);
			request.put(CommonDACParams.end_date.name(), endTime);
		} catch (Exception e) {
			LOGGER.log("Exception during creating request" , e.getMessage(), e);
			e.printStackTrace();
		}
		LOGGER.log("Sending request to auditHistoryEsService" , request);
		Response response = null;
		try {
			response = auditHistoryEsService.getAuditHistoryLog(request, versionId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.log("Response received from the auditHistoryEsService as a result" + response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.taxonomy.mgr.IAuditHistoryManager
	 * #getAuditHistoryByType(java.lang.String, java.lang.String, java.io.File,
	 * java.lang.String)
	 */
	@Override
	public Response getAuditHistoryByType(String graphId, String objectType, String startTime, String endTime,
			String versionId) {
		Request request = new Request();
		try {
			LOGGER.log("Checking if received parameters are empty or not" + graphId + objectType);
			if (StringUtils.isNotBlank(graphId) && StringUtils.isNotBlank(objectType)) {
				request.put(CommonDACParams.graph_id.name(), graphId);
				request.put(CommonDACParams.object_type.name(), objectType);
			}
			request.put(CommonDACParams.start_date.name(), startTime);
			request.put(CommonDACParams.end_date.name(), endTime);
		} catch (Exception e) {
			LOGGER.log("Exception during creating request" , e.getMessage(), e);
			e.printStackTrace();
		}
		LOGGER.log("Sending request to auditHistoryEsService" , request);
		Response response = null;
		try {
			response = auditHistoryEsService.getAuditHistoryLogByObjectType(request, versionId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.log("Response received from the auditHistoryEsService as a result" , response);
		return response;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.taxonomy.mgr.IAuditHistoryManager
	 * #getAuditHistoryById(java.lang.String, java.lang.String, java.io.File,
	 * java.lang.String)
	 */
	@Override
	public Response getAuditHistoryById(String graphId, String objectId, String startTime, String endTime,
			String versionId) {
		Request request = new Request();
		LOGGER.log("Checking if received parameters are empty or not" + graphId + objectId);
		if (StringUtils.isNotBlank(graphId) && StringUtils.isNotBlank(objectId)) {
			request.put(CommonDACParams.graph_id.name(), graphId);
			request.put(CommonDACParams.object_id.name(), objectId);
		}
		request.put(CommonDACParams.start_date.name(), startTime);
		request.put(CommonDACParams.end_date.name(), endTime);

		LOGGER.log("Sending request to auditHistoryEsService" , request);
		Response response = null;
		try {
			response = auditHistoryEsService.getAuditHistoryLogByObjectId(request, versionId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.log("Response received from the auditHistoryEsService as a result" , response);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.taxonomy.mgr.IAuditHistoryManager
	 * #getAuditLogRecordById(java.lang.String, java.lang.String, java.io.File,
	 * java.lang.String)
	 */
	@Override
	public Response getAuditLogRecordById(String objectId, String timeStamp) {
		Request request = new Request();
		LOGGER.log("Checking if received parameters are empty or not" + objectId);
		if (StringUtils.isNotBlank(objectId)) {
			request.put(CommonDACParams.object_id.name(), objectId);
		}
		request.put(CommonDACParams.time_stamp.name(), timeStamp);

		LOGGER.log("Sending request to auditHistoryEsService" , request);
		Response response = null;
		try {
			response = auditHistoryEsService.getAuditLogRecordById(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.log("Response received from the auditHistoryEsService as a result" , response);
		return response;
	}

	@Override
	public Response deleteAuditHistory(String timeStamp) {
		Request request = new Request();
		LOGGER.log("Checking if timestamp exists or not" + timeStamp);
		if (StringUtils.isNotBlank(timeStamp)) {
			request.put(CommonDACParams.time_stamp.name(), timeStamp);
		}
		request.put(CommonDACParams.time_stamp.name(), timeStamp);

		LOGGER.log("Sending request to auditHistoryESService" , request);
		Response response = null;
		try {
			response = auditHistoryEsService.deleteEsData(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.log("Response received from the auditHistoryESService as a result" + response);
		return response;
	}
}