package org.sunbird.content.mgr.impl;

import org.sunbird.common.dto.Response;
import org.sunbird.content.mgr.impl.operation.content.CreateOperation;
import org.sunbird.content.mgr.impl.operation.content.DiscardOperation;
import org.sunbird.content.mgr.impl.operation.content.FindOperation;
import org.sunbird.content.mgr.impl.operation.content.RejectOperation;
import org.sunbird.content.mgr.impl.operation.content.RetireOperation;
import org.sunbird.content.mgr.impl.operation.content.UpdateOperation;

import java.util.List;
import java.util.Map;

public class ContentManger {

	private final CreateOperation createOperation = new CreateOperation();
    private final FindOperation findOperation = new FindOperation();
    private final UpdateOperation updateOperation = new UpdateOperation();
    private final RetireOperation retireOperation = new RetireOperation();
    private final DiscardOperation discardOperation = new DiscardOperation();
    private final RejectOperation rejectOperation = new RejectOperation();


    public Response create(Map<String, Object> map, String channelId) throws Exception {
        return this.createOperation.create(map, channelId);
    }

    public Response find(String contentId, String mode, List<String> fields) {
        return this.findOperation.find(contentId, mode, fields);
    }

    public Response updateAllContents(String originalId, Map<String, Object> map) throws Exception {
        return this.updateOperation.updateAllContents(originalId, map);
    }

    public Response update(String contentId, Map<String, Object> map) throws Exception {
        return this.updateOperation.update(contentId, map);
    }

    public Response retire(String contentId) { return this.retireOperation.retire(contentId); }

    /**
     * Discard operations
     * @param contentId
     * @return
     * @throws Exception
     */
    public Response discard(String contentId) throws Exception {
        return this.discardOperation.discard(contentId);
    }
    /**
     * Reject operations
     *
     * @param contentId
     * @return
     * @throws Exception
     */
    public Response reject(String contentId, Map<String, Object> requestMap) throws Exception {
        return this.rejectOperation.rejectContent(contentId, requestMap);
    }
}
