package org.ekstep.content.mgr.impl;

import org.ekstep.common.dto.Response;
import org.ekstep.content.mgr.impl.operation.content.CreateOperation;
import org.ekstep.content.mgr.impl.operation.content.DiscardOperation;
import org.ekstep.content.mgr.impl.operation.content.FindOperation;
import org.ekstep.content.mgr.impl.operation.content.RetireOperation;
import org.ekstep.content.mgr.impl.operation.content.UpdateOperation;

import java.util.List;
import java.util.Map;

public class ContentManger {

	private final CreateOperation createOperation = new CreateOperation();
    private final FindOperation findOperation = new FindOperation();
    private final UpdateOperation updateOperation = new UpdateOperation();
    private final RetireOperation retireOperation = new RetireOperation();
    private final DiscardOperation discardOperation = new DiscardOperation();

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

    public Response discard(String contentId) throws Exception {
        return this.discardOperation.discard(contentId);
    }

}
