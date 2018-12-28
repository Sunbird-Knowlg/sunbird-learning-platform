package org.ekstep.content.mgr.impl;

import org.ekstep.common.dto.Response;
import org.ekstep.content.mgr.impl.update.UpdateAllContentsManager;
import org.ekstep.content.mgr.impl.update.UpdateContentManager;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UpdateManager {

    private final UpdateContentManager updateContentManager = new UpdateContentManager();

    private final UpdateAllContentsManager updateAllContents = new UpdateAllContentsManager();

    public Response update(String contentId, Map<String, Object> map) throws Exception {
        return this.updateContentManager.update(contentId, map);
    }

    public Response updateAllContents(String originalId, Map<String, Object> map) throws Exception {
        return this.updateAllContents.updateAllContents(originalId, map);
    }

}
