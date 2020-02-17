package org.ekstep.content.mgr.impl;

import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.content.mgr.impl.operation.plugin.BundleOperation;
import org.ekstep.content.mgr.impl.operation.plugin.CopyOperation;
import org.ekstep.content.mgr.impl.operation.plugin.OptimizeOperation;
import org.ekstep.content.mgr.impl.operation.plugin.PreSignedUrlOperation;

import java.util.Map;

public class ContentPluginManager {

	private final OptimizeOperation optimizeOperation = new OptimizeOperation();
    private final BundleOperation bundleOperation = new BundleOperation();
    private final CopyOperation copyOperation = new CopyOperation();
    private final PreSignedUrlOperation preSignedUrlOperation = new PreSignedUrlOperation();

    public Response optimize(String contentId) { return this.optimizeOperation.optimize(contentId); }

    public Response bundle(Request request, String version) { return this.bundleOperation.bundle(request, version); }

    public Response copyContent(String contentId, Map<String, Object> requestMap, String mode) {
        return this.copyOperation.copyContent(contentId, requestMap, mode);
    }
    
    public Response preSignedUrl(String contentId, String fileName, String type, Boolean idValReq) {
    	return this.preSignedUrlOperation.preSignedUrl(contentId, fileName, type, idValReq);
    }
    

}
