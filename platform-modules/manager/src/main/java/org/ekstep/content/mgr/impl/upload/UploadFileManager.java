package org.ekstep.content.mgr.impl.upload;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.content.mgr.util.UploadUtil;
import org.ekstep.content.mimetype.mgr.IMimeTypeManager;
import org.ekstep.content.util.MimeTypeManagerFactory;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.taxonomy.mgr.impl.DummyBaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class UploadFileManager extends DummyBaseContentManager {

    @Autowired private UploadUtil uploadUtil;

    public Response upload(String contentId, File uploadedFile, String mimeType) {
        boolean updateMimeType = false;

        try {
            if (StringUtils.isBlank(contentId))
                throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
                        "Content Object Id is blank.");
            if (null == uploadedFile)
                throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_UPLOAD_OBJECT.name(),
                        "Upload file is blank.");
            TelemetryManager.log("Uploaded File: " + uploadedFile.getAbsolutePath());

            if (StringUtils.endsWithIgnoreCase(contentId, DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX))
                throw new ClientException(ContentErrorCodes.OPERATION_DENIED.name(),
                        "Invalid Content Identifier. | [Content Identifier does not Exists.]");

            Node node = getNodeForOperation(contentId, "upload");

            isNodeUnderProcessing(node, "Upload");
            if (StringUtils.isBlank(mimeType)) {
                mimeType = getMimeType(node);
            } else {
                setMimeTypeForUpload(mimeType, node);
                updateMimeType = true;
            }

            TelemetryManager.log("Mime-Type: " + mimeType + " | [Content ID: " + contentId + "]");
            TelemetryManager.log(
                    "Fetching Mime-Type Factory For Mime-Type: " + mimeType + " | [Content ID: " + contentId + "]");
            String contentType = (String) node.getMetadata().get("contentType");
            IMimeTypeManager mimeTypeManager = MimeTypeManagerFactory.getManager(contentType, mimeType);
            Response res = mimeTypeManager.upload(contentId, node, uploadedFile, false);

            if (updateMimeType && !checkError(res)) {
                node.getMetadata().put("versionKey", res.getResult().get("versionKey"));
                Response response = uploadUtil.updateMimeType(contentId, mimeType);
                if (checkError(response))
                    return response;
            }

            return checkAndReturnUploadResponse(res);
        } catch (ClientException e) {
            throw e;
        } catch (ServerException e) {
            return ERROR(e.getErrCode(), e.getMessage(), ResponseCode.SERVER_ERROR);
        } catch (Exception e) {
            String message = "Something went wrong while processing uploaded file.";
            TelemetryManager.error(message, e);
            return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), message, ResponseCode.SERVER_ERROR);
        } finally {
            if (null != uploadedFile && uploadedFile.exists())
                uploadedFile.delete();
        }
    }

}
