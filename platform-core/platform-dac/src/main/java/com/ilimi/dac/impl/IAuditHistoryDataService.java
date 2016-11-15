package com.ilimi.dac.impl;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

/**
 * The Interface IAuditHistoryDataService defines the Audit Logs operations that
 * needs to be implemented by implementing classes.
 * 
 */
public interface IAuditHistoryDataService {

	/**
	 * Saves the Audit Logs processed from the kafka consumers to mysql DB based on the requestBody
	 * provided from the AuditHistoryManager
	 *
	 * @param Request
	 *            The request
	 */
    public Response saveAuditHistoryLog(Request request);
    
    /**
	 * This method carries the entire operation of fetching all AuditHistory Logs
	 * from mysql DB based on the requestBody sent from the AuditHistoryManager
	 *
	 * @param graphId
	 *            The graph id
	 * @param timestamp1
	 *            The start Time
	 * @param timestamp2
	 *            The end Time
	 * @return the response which contains all AuditHistoryLogs 
	 */
    public Response getAuditHistoryLog(Request request);

    /**
	 * This method carries the entire operation of fetching AuditHistory Logs for a given ObjectType
	 * from mysql DB based on the requestBody sent from the AuditHistoryManager
	 *
	 * @param graphId
	 *            The graph id
	 * @param objectType
	 *            The objectType
	 * @param timestamp1
	 *            The start Time
	 * @param timestamp2
	 *            The end Time
	 * @return the response which contains AuditHistoryLogs for an given ObjectType
	 */
    public Response getAuditHistoryLogByObjectType(Request request);
    
    /**
   	 * This method carries the entire operation of fetching AuditHistory Logs for a given ObjectId
   	 * from mysql DB based on the requestBody sent from the AuditHistoryManager
   	 *
   	 * @param graphId
   	 *            The graph id
   	 * @param objectId
   	 *            The objectId
   	 * @param timestamp1
   	 *            The start Time
   	 * @param timestamp2
   	 *            The end Time
   	 * @return the response which contains AuditHistoryLogs for an given ObjectId
   	 */
    public Response getAuditHistoryLogByObjectId(Request request);

    /**
	 * This method carries the entire operation of fetching AuditHistory Logs for a given auditId
	 * from mysql DB based on the requestBody sent from the AuditHistoryManager
	 *
	 * @param graphId
	 *            The graph id
	 * @param auditId
	 *            The auditId
	 * @param timestamp1
	 *            The start Time
	 * @param timestamp2
	 *            The end Time
	 * @return the response which contains AuditHistoryLogs for an given auditId
	 */
	public Response getAuditLogRecordByAuditId(Request request);

}