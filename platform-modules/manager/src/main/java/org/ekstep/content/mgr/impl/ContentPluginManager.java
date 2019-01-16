package org.ekstep.content.mgr.impl;

import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.content.mgr.impl.operation.plugin.BundleOperation;
import org.ekstep.content.mgr.impl.operation.plugin.OptimizeOperation;

public class ContentPluginManager {

    private final OptimizeOperation optimizeOperation = new OptimizeOperation();
    private final BundleOperation bundleOperation = new BundleOperation();

    public Response optimize(String contentId) { return this.optimizeOperation.optimize(contentId); }

    public Response bundle(Request request, String version) { return this.bundleOperation.bundle(request, version); }

}
