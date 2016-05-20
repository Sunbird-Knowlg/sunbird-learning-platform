package com.ilimi.taxonomy.mgr.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.dac.dto.AuditHistoryRecord;
import com.ilimi.dac.enums.CommonDACParams;
import com.ilimi.dac.impl.IAuditHistoryDataService;
import com.ilimi.taxonomy.enums.AuditLogErrorCodes;
import com.ilimi.taxonomy.mgr.IAuditHistoryManager;

@Component("auditHistoryManager")
public class AuditHistoryManager implements IAuditHistoryManager {

   @Autowired
   IAuditHistoryDataService auditHistoryDataService;

    @Override
    //@Async
    public void saveAuditHistory(AuditHistoryRecord audit) {
        if (null != audit) {
            if (StringUtils.isBlank(audit.getObjectId()) || StringUtils.isBlank(audit.getLogRecord())) {
                throw new ClientException(AuditLogErrorCodes.ERR_SAVE_AUDIT_MISSING_REQ_PARAMS.name(), "Required params missing...");
            }
            Request request = new Request();
            request.put(CommonDACParams.audit_history_record.name(), audit);
            auditHistoryDataService.saveAuditHistoryLog(request);
        } else {
            throw new ClientException(AuditLogErrorCodes.ERR_INVALID_AUDIT_RECORD.name(), "audit record is null.");
        }

    }

    @Override
    public Response getAuditHistory(String graphId, String timeStamp1, String timeStamp2) {

    	Request request = new Request();
    	request.put(CommonDACParams.graph_id.name(), graphId);
    	Date startDate=null;
    	Date endDate=null;
        DateFormat df = new SimpleDateFormat("ddMMyyyy");
        try{
        	startDate=df.parse(timeStamp1);
        	if(timeStamp2!=null)
        		endDate=df.parse(timeStamp2);
        }catch(Exception ex){
        }

    	request.put(CommonDACParams.start_date.name(), startDate);
    	request.put(CommonDACParams.end_date.name(), endDate);
    	
        Response response = auditHistoryDataService.getAuditHistoryLog(request);
        return response;
    }

	@Override
	public Response getAuditHistoryByType(String graphId, String objectType, String timeStamp1, String timeStamp2) {
    	Request request = new Request();
    	request.put(CommonDACParams.graph_id.name(), graphId);
    	request.put(CommonDACParams.object_type.name(), objectType);
    	Date startDate=null;
    	Date endDate=null;
        DateFormat df = new SimpleDateFormat("ddMMyyyy");
        try{
        	startDate=df.parse(timeStamp1);
        	if(timeStamp2!=null)
        		endDate=df.parse(timeStamp2);
        }catch(Exception ex){
        }

    	request.put(CommonDACParams.start_date.name(), startDate);
    	request.put(CommonDACParams.end_date.name(), endDate);
    	
        Response response = auditHistoryDataService.getAuditHistoryLogByObjectType(request);
        return response;	
    }

	@Override
	public Response getAuditHistoryById(String graphId, String objectId, String timeStamp1, String timeStamp2) {
    	Request request = new Request();
    	request.put(CommonDACParams.graph_id.name(), graphId);
    	request.put(CommonDACParams.object_id.name(), objectId);
    	Date startDate=null;
    	Date endDate=null;
        DateFormat df = new SimpleDateFormat("ddMMyyyy");
        try{
        	startDate=df.parse(timeStamp1);
        	if(timeStamp2!=null)
        		endDate=df.parse(timeStamp2);
        }catch(Exception ex){
        }

    	request.put(CommonDACParams.start_date.name(), startDate);
    	request.put(CommonDACParams.end_date.name(), endDate);
    	
        Response response = auditHistoryDataService.getAuditHistoryLogByObjectId(request);
        return response;	
	}

}
