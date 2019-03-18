package org.ekstep.content.mgr.impl;

import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.content.mgr.impl.operation.event.AcceptFlagOperation;
import org.ekstep.content.mgr.impl.operation.event.PublishOperation;
import org.ekstep.content.mgr.impl.operation.event.ReviewOperation;

import java.util.Map;

public class ContentEventManager {

	private final ReviewOperation reviewOperation = new ReviewOperation();
    private final PublishOperation publishOperation = new PublishOperation();
    private final AcceptFlagOperation acceptFlagOperation = new AcceptFlagOperation();

    public Response publish(String contentId, Map<String, Object> requestMap) {
        return this.publishOperation.publish(contentId, requestMap);
    }

    public Response review(String contentId, Request request) throws Exception {
        return this.reviewOperation.review(contentId, request);
    }

    public Response acceptFlag(String contentId) { return this.acceptFlagOperation.acceptFlag(contentId); }

}
