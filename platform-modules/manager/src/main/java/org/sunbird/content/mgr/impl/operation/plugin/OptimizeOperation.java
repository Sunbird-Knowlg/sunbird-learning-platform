package org.sunbird.content.mgr.impl.operation.plugin;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.optimizr.Optimizr;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.learning.common.enums.ContentAPIParams;
import org.sunbird.learning.common.enums.ContentErrorCodes;
import org.sunbird.learning.util.CloudStore;
import org.sunbird.taxonomy.mgr.impl.BaseContentManager;
import org.sunbird.telemetry.logger.TelemetryManager;

import java.io.File;

public class OptimizeOperation extends BaseContentManager {

    public Response optimize(String contentId) {

        validateEmptyOrNullContentId(contentId);

        Response response = new Response();
        Node node = getNodeForOperation(contentId, "optimize");

        isNodeUnderProcessing(node, "Optimize");

        String status = getNodeStatus(node);
        TelemetryManager.log("Content Status: " + status);
        if (!isLiveStatus(status) || !StringUtils.equalsIgnoreCase(ContentAPIParams.Unlisted.name(), status))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(),
                    "UnPublished content cannot be optimized");

        String downloadUrl = getDownloadUrlFrom(node);
        TelemetryManager.log("Download Url: " + downloadUrl);
        if (StringUtils.isBlank(downloadUrl))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(),
                    "ECAR file not available for content");

        if (!StringUtils.equalsIgnoreCase(ContentAPIParams.ecar.name(), FilenameUtils.getExtension(downloadUrl)))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(),
                    "Content package is not an ECAR file");

        String optStatus = (String) node.getMetadata().get(ContentAPIParams.optStatus.name());
        TelemetryManager.log("Optimization Process Status: " + optStatus);
        if (StringUtils.equalsIgnoreCase(ContentAPIParams.Processing.name(), optStatus))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(),
                    "Content optimization is in progress. Please try after the current optimization is complete");

        node.getMetadata().put(ContentAPIParams.optStatus.name(), ContentAPIParams.Processing.name());
        updateDataNode(node);
        Optimizr optimizr = new Optimizr();
        try {
            TelemetryManager.log("Invoking the Optimizer for Content Id: " + contentId);
            File minEcar = optimizr.optimizeECAR(downloadUrl);
            TelemetryManager.log("Optimized File: " + minEcar.getName() + " | [Content Id: " + contentId + "]");

            String folder = getFolderName(downloadUrl);
            TelemetryManager.log("Folder Name: " + folder + " | [Content Id: " + contentId + "]");

            //String[] arr = AWSUploader.uploadFile(folder, minEcar);
            String[] arr = CloudStore.uploadFile(folder, minEcar, true);
            response.put("url", arr[1]);
            TelemetryManager.log("URL: " + arr[1] + " | [Content Id: " + contentId + "]");

            TelemetryManager.log("Updating the Optimization Status. | [Content Id: " + contentId + "]");
            node.getMetadata().put(ContentAPIParams.optStatus.name(), "Complete");
            updateDataNode(node);
            TelemetryManager.log("Node Updated. | [Content Id: " + contentId + "]");

            TelemetryManager.log("Directory Cleanup. | [Content Id: " + contentId + "]");
            FileUtils.deleteDirectory(minEcar.getParentFile());
        } catch (Exception e) {
            node.getMetadata().put(ContentAPIParams.optStatus.name(), "Error");
            updateDataNode(node);
            response = ERROR(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(), e.getMessage(), ResponseCode.SERVER_ERROR);
        }
        return response;
    }

}
