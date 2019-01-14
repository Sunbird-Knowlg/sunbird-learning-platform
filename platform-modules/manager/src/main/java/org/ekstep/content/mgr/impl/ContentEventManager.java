package org.ekstep.content.mgr.impl;

import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.content.mgr.impl.operation.event.AcceptFlagOperation;
import org.ekstep.content.mgr.impl.operation.event.PublishOperation;
import org.ekstep.content.mgr.impl.operation.event.ReviewOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ContentEventManager {

    @Autowired private ReviewOperation reviewOperation;
    @Autowired private PublishOperation publishOperation;
    @Autowired private AcceptFlagOperation acceptFlagOperation;

    public Response publish(String contentId, Map<String, Object> requestMap) {
        return this.publishOperation.publish(contentId, requestMap);
    }

    public Response review(String contentId, Request request) throws Exception {
        return this.reviewOperation.review(contentId, request);
    }

    public Response acceptFlag(String contentId) { return this.acceptFlagOperation.acceptFlag(contentId); }

}
