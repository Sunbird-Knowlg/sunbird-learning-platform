package org.ekstep.content.mgr.impl;

import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.content.mgr.impl.operation.plugin.BundleOperation;
import org.ekstep.content.mgr.impl.operation.plugin.CopyOperation;
import org.ekstep.content.mgr.impl.operation.plugin.OptimizeOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ContentPluginManager {

    @Autowired private OptimizeOperation optimizeOperation;
    @Autowired private BundleOperation bundleOperation;
    @Autowired private CopyOperation copyOperation;

    public Response optimize(String contentId) { return this.optimizeOperation.optimize(contentId); }

    public Response bundle(Request request, String version) { return this.bundleOperation.bundle(request, version); }

    public Response copyContent(String contentId, Map<String, Object> requestMap, String mode) {
        return this.copyOperation.copyContent(contentId, requestMap, mode);
    }

}
