package org.sunbird.content.mgr.impl;

import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.content.mgr.impl.operation.event.AcceptFlagOperation;
import org.sunbird.content.mgr.impl.operation.event.FlagOperation;
import org.sunbird.content.mgr.impl.operation.event.RejectFlagOperation;
import org.sunbird.content.mgr.impl.operation.event.PublishOperation;
import org.sunbird.content.mgr.impl.operation.event.ReviewOperation;

import java.util.Map;

public class ContentEventManager {

	private final ReviewOperation reviewOperation = new ReviewOperation();
    private final PublishOperation publishOperation = new PublishOperation();
    private final AcceptFlagOperation acceptFlagOperation = new AcceptFlagOperation();
    private final FlagOperation flagOperation = new FlagOperation();
    private final RejectFlagOperation rejectFlagOperation = new RejectFlagOperation();

    public Response publish(String contentId, Map<String, Object> requestMap) {
        return this.publishOperation.publish(contentId, requestMap);
    }

    public Response review(String contentId, Request request) throws Exception {
        return this.reviewOperation.review(contentId, request);
    }

    public Response acceptFlag(String contentId) { return this.acceptFlagOperation.acceptFlag(contentId); }

    public Response flag(String contentId, Map<String, Object> requestMap) throws Exception {
        return this.flagOperation.flag(contentId, requestMap);
    }

    public Response rejectFlag(String contentId) throws Exception {
        return this.rejectFlagOperation.rejectFlag(contentId);
    }

}
