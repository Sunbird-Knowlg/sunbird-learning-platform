package org.ekstep.content.mgr.impl.operation.upload;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.content.mimetype.mgr.IMimeTypeManager;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.io.File;

public class UploadFileOperation extends BaseUploadOperation {

    public Response upload(String contentId, File uploadedFile, String mimeType) {
        boolean updateMimeType = false;

        try {
            validateEmptyOrNullContentId(contentId);

            if (null == uploadedFile)
                throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_UPLOAD_OBJECT.name(),
                        "Upload file is blank.");
            TelemetryManager.log("Uploaded File: " + uploadedFile.getAbsolutePath());

            isImageContentId(contentId);

            Node node = getNodeForOperation(contentId, "upload");

            isNodeUnderProcessing(node, "Upload");
            if (StringUtils.isBlank(mimeType)) {
                mimeType = getMimeType(node);
            } else {
                setMimeTypeForUpload(mimeType, node);
                updateMimeType = true;
            }

            TelemetryManager.log("Mime-Type: " + mimeType + " | [Content ID: " + contentId + "]");
            IMimeTypeManager mimeTypeManager = getMimeTypeManger(contentId, mimeType, node);

            Response res = mimeTypeManager.upload(contentId, node, uploadedFile, false);

            Response response = validateResponseAndUpdateMimeType(updateMimeType, node, res, contentId, mimeType);
            if (null != response && checkError(response)) { return response; }

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
