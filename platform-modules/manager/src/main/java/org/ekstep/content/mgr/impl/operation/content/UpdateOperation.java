package org.ekstep.content.mgr.impl.operation.content;

import org.ekstep.common.dto.Response;
import org.ekstep.content.mgr.impl.operation.content.update.UpdateAllContentsOperation;
import org.ekstep.content.mgr.impl.operation.content.update.UpdateContentOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UpdateOperation {

    @Autowired private UpdateContentOperation updateContentOperation;

    @Autowired private UpdateAllContentsOperation updateAllContentsOperation;

    public Response update(String contentId, Map<String, Object> map) throws Exception {
        return this.updateContentOperation.update(contentId, map);
    }

    public Response updateAllContents(String originalId, Map<String, Object> map) throws Exception {
        return this.updateAllContentsOperation.updateAllContents(originalId, map);
    }

}