package org.ekstep.content.mgr.impl.operation.upload;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.content.mimetype.mgr.IMimeTypeManager;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.telemetry.logger.TelemetryManager;

public class UploadUrlOperation extends BaseUploadOperation {

    public Response upload(String contentId, String fileUrl, String mimeType) {
        boolean updateMimeType = false;
        try {
            validateEmptyOrNullContentId(contentId);

            validateEmptyOrNullFileUrl(fileUrl);

            isImageContentId(contentId);

            Node node = getNodeForOperation(contentId, "upload");

            isNodeUnderProcessing(node, "Upload");
            if (StringUtils.isBlank(mimeType)) {
                mimeType = getMimeType(node);
            } else {
                setMimeTypeForUpload(mimeType, node);
                updateMimeType = true;
            }

            validateUrlLicense(mimeType, fileUrl, node);

            TelemetryManager.log("Mime-Type: " + mimeType + " | [Content ID: " + contentId + "]");
            IMimeTypeManager mimeTypeManager = getMimeTypeManger(contentId, mimeType, node);

            Response res = mimeTypeManager.upload(contentId, node, fileUrl);

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
        }
    }

    private void validateUrlLicense(String mimeType, String fileUrl, Node node) {
        switch (mimeType) {
            case "video/x-youtube": checkYoutubeLicense(fileUrl, node);
                                    break;
            default:                break;
        }
    }

}
