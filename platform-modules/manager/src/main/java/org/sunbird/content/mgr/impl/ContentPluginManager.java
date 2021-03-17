package org.sunbird.content.mgr.impl;

import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.content.mgr.impl.operation.plugin.BundleOperation;
import org.sunbird.content.mgr.impl.operation.plugin.CopyOperation;
import org.sunbird.content.mgr.impl.operation.plugin.OptimizeOperation;
import org.sunbird.content.mgr.impl.operation.plugin.PreSignedUrlOperation;

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
    
    public Response preSignedUrl(String contentId, String fileName, String type) {
    	return this.preSignedUrlOperation.preSignedUrl(contentId, fileName, type);
    }
    

}
