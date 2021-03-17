package org.sunbird.dac.impl;

import java.io.IOException;

import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;

/**
 * The Interface IAuditHistoryDataService defines the Audit Logs operations that
 * needs to be implemented by implementing classes.
 * 
 */
public interface IAuditHistoryEsService {

	/**
	 * Saves the Audit Logs processed from the kafka consumers to mysql DB based on the requestBody
	 * provided from the AuditHistoryManager
	 *
	 * @param Request
	 *            The request
	 * @throws IOException 
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
     * @throws Exception 
	 */
    public Response getAuditHistoryLog(Request request, String versionId);

    /**
	 * This method carries the entire operation of fetching AuditHistory Logs for a given ObjectType
	 * from mysql DB based on the requestBody sent from the AuditHistoryManager
     * @param versionId 
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
     * @throws Exception 
	 */
    public Response getAuditHistoryLogByObjectType(Request request, String versionId);
    
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
     * @throws Exception 
   	 */
    public Response getAuditHistoryLogByObjectId(Request request,String versionId);

    /**
	 * This method carries the entire operation of fetching AuditHistory Logs for a given objectId
	 * from mysql DB based on the requestBody sent from the AuditHistoryManager
	 *
	 * @param graphId
	 *            The graph id
	 * @param auditId
	 *            The objectId
	 * @param timestamp1
	 *            The time_stamp
	 * @return the response which contains AuditHistoryLogs for an given objectId
     * @throws Exception 
	 */
	public Response getAuditLogRecordById(Request request);

	/**
	 * This method carries the entire operation of deleting AuditHistory Logs for a given timestamp
	 * from elasticSearch based on the requestBody sent from the AuditHistoryManager
	 *
	 * @param timestamp
	 *            The timestamp
     * @throws Exception 
	 */
	public Response deleteEsData(Request request);

}