package com.ilimi.dac.impl;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.Response;

public interface IAuditLogDataService {

    public Response saveAuditLog(Request request);
    
    public Response saveComment(Request request);
    
    public Response getAuditHistory(Request request);
    
    public Response getCommentThread(Request request);
    
    public Response getComments(Request request);
}
