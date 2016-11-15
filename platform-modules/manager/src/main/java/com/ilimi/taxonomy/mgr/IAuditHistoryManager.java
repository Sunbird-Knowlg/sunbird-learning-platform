package com.ilimi.taxonomy.mgr;


import com.ilimi.common.dto.Response;
import com.ilimi.dac.dto.AuditHistoryRecord;

/**
 * The Interface IAuditHistoryManager defines the Audit Logs operations that
 * needs to be implemented by implementing classes.
 * 
 */
public interface IAuditHistoryManager {
	
	/**
	 * creates a request object from the AuditHistoryRecord and passes to the
	 * AuditHistoryDataService for further processing
	 *
	 * @param AuditHistoryRecord
	 *            The record
	 */
    void saveAuditHistory(AuditHistoryRecord audit);

    /**
	 * This method carries the entire operation of fetching all AuditHistory Logs
	 * from mysql DB which holds all the modification details done on a particular object,
	 * It creates request object from the params and calls AuditHistoryDataService for further processing
	 *
	 * @param graphId
	 *            The graph id
	 * @param timestamp1
	 *            The start Time
	 * @param timestamp2
	 *            The end Time
	 * @return the response which contains all AuditHistoryLogs 
	 */
    Response getAuditHistory(String graphId,String timestamp1,String timestamp2);
    
    /**
	 * This method carries the entire operation of fetching AuditHistory Logs for a given ObjectType
	 * from mysql DB which holds all the modification details done on a particular object
	 * It creates request object from the params and calls AuditHistoryDataService for further processing
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
    Response getAuditHistoryByType(String graphId, String objectType,String timeStamp1,String timeStamp2);
    
    /**
	 * This method carries the entire operation of fetching AuditHistory Logs for a given ObjectId
	 * from mysql DB which holds all the modification details done on a particular object,
	 * It creates request object from the params and calls AuditHistoryDataService for further processing
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
    Response getAuditHistoryById(String graphId, String objectId,String timeStamp1,String timeStamp2);

    /**
	 * This method carries the entire operation of fetching AuditHistory Logs for a given auditId
	 * from mysql DB which holds all the modification details done on a particular object,
	 * It creates request object from the params and calls AuditHistoryDataService for further processing
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
	Response getAuditLogRecordByAuditId(String audit_id, String startTime, String endTime);

}
