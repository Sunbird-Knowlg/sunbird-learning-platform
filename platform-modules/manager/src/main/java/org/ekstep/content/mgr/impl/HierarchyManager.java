package org.ekstep.content.mgr.impl;

import org.ekstep.common.dto.Response;
import org.ekstep.content.mgr.impl.hierarchy.GetHierarchyManager;
import org.ekstep.content.mgr.impl.hierarchy.SyncHierarchyManager;
import org.ekstep.content.mgr.impl.hierarchy.UpdateHierarchyManager;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HierarchyManager {

    private final GetHierarchyManager getHierarchyManager = new GetHierarchyManager();

    private final UpdateHierarchyManager updateHierarchyManager = new UpdateHierarchyManager();

    private final SyncHierarchyManager syncHierarchyManager = new SyncHierarchyManager();

    public Response get(String contentId, String mode) {
        return this.getHierarchyManager.getHierarchy(contentId, mode);
    }

    public Response update(Map<String, Object> data) {
        return this.updateHierarchyManager.updateHierarchy(data);
    }

    public Response sync(String identifier) { return this.syncHierarchyManager.syncHierarchy(identifier); }
}
