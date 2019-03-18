package org.ekstep.content.mgr.impl.operation.content;

import org.ekstep.common.dto.Response;
import org.ekstep.content.mgr.impl.operation.content.update.UpdateAllContentsOperation;
import org.ekstep.content.mgr.impl.operation.content.update.UpdateContentOperation;

import java.util.Map;

public class UpdateOperation {

    private final UpdateContentOperation updateContentOperation = new UpdateContentOperation();
    private final UpdateAllContentsOperation updateAllContentsOperation = new UpdateAllContentsOperation();

    public Response update(String contentId, Map<String, Object> map) throws Exception {
        return this.updateContentOperation.update(contentId, map);
    }

    public Response updateAllContents(String originalId, Map<String, Object> map) throws Exception {
        return this.updateAllContentsOperation.updateAllContents(originalId, map);
    }

}