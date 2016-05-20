package com.ilimi.dac.impl;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

public interface IAuditHistoryDataService {

    public Response saveAuditHistoryLog(Request request);
    
    public Response getAuditHistoryLog(Request request);

    public Response getAuditHistoryLogByObjectType(Request request);
    
    public Response getAuditHistoryLogByObjectId(Request request);

}
