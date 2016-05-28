package com.ilimi.taxonomy.mgr;


import com.ilimi.common.dto.Response;
import com.ilimi.dac.dto.AuditHistoryRecord;


public interface IAuditHistoryManager {

    void saveAuditHistory(AuditHistoryRecord audit);

    Response getAuditHistory(String graphId,String timestamp1,String timestamp2);
    
    Response getAuditHistoryByType(String graphId, String objectType,String timeStamp1,String timeStamp2);
    
    Response getAuditHistoryById(String graphId, String objectId,String timeStamp1,String timeStamp2);

}
