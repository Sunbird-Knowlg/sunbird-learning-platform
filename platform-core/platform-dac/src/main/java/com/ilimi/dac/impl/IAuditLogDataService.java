package com.ilimi.dac.impl;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

public interface IAuditLogDataService {

    public Response saveAuditLog(Request request);
    
    public Response saveComment(Request request);
    
    public Response getAuditHistory(Request request);
    
    public Response getCommentThread(Request request);
    
    public Response getComments(Request request);
}
