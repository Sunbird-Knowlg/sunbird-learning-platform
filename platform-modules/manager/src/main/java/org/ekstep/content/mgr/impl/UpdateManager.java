package org.ekstep.content.mgr.impl;

import org.ekstep.common.dto.Response;
import org.ekstep.content.mgr.impl.update.UpdateAllContentsManager;
import org.ekstep.content.mgr.impl.update.UpdateContentManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UpdateManager {

    @Autowired private UpdateContentManager updateContentManager;

    @Autowired private UpdateAllContentsManager updateAllContentsManager;

    public Response update(String contentId, Map<String, Object> map) throws Exception {
        return this.updateContentManager.update(contentId, map);
    }

    public Response updateAllContents(String originalId, Map<String, Object> map) throws Exception {
        return this.updateAllContentsManager.updateAllContents(originalId, map);
    }

}