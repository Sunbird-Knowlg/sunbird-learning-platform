package org.ekstep.learning.util.cloud.actor;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.learning.util.cloud.CloudStore;
import org.ekstep.learning.util.cloud.enums.CloudStoreParams;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.io.File;

import static org.ekstep.learning.util.ActorUtils.OK;
import static org.ekstep.telemetry.logger.TelemetryManager.log;

/**
 * Actor for executing Cloud Store {@link CloudStore} Operations Asynchronously
 *
 * @see CloudStore#uploadFile(String, File, boolean)
 * @see CloudStore#uploadDirectory(String, File, boolean)
 */
public class CloudStoreAsync extends UntypedActor {

    @Override
    public void onReceive(Object message) {
        if (message instanceof Request) {
            Request request = (Request) message;
            invokeMethod(request, sender());
        } else {
            unhandled(message);
        }
    }

    private void invokeMethod(Request request, ActorRef parent) {
        try {
            String operation = request.getOperation();
            if  (operation.startsWith(CloudStoreParams.upload.name())) { upload(request, parent); }
            else {
                TelemetryManager.error("CloudStoreAsyncActor#invokeMethod | Requested Operation: " + operation + " not supported");
            }
        } catch (Exception e) {
            TelemetryManager.error("CloudStoreAsyncActor#invokeMethod | Error occurred with message: " + e.getMessage(), e);
        }
    }

    /**
     * Invoking {@link CloudStore} upload operations.
     *
     * @see CloudStore
     * @see CloudStore#uploadFile(String, File, boolean)
     * @see CloudStore#uploadDirectory(String, File, boolean)
     * @param request
     * @param parent
     */
    private void upload(Request request, ActorRef parent) {
        String operation = request.getOperation();
        String folderName = null;
        String basePath = null;
        boolean slugFile;
        boolean deleteFile = false;
        try {
            folderName = (String) request.get(CloudStoreParams.folderName.name());
            File f = (File) request.get(CloudStoreParams.file.name());
            slugFile = (boolean) request.get(CloudStoreParams.slugFile.name());
            basePath = (String) request.get(CloudStoreParams.basePath.name());
            deleteFile = (boolean) request.get(CloudStoreParams.deleteFile.name());
            if (null == folderName || null == f || !f.exists() || StringUtils.isBlank(basePath)) {
                TelemetryManager.error("CloudStoreAsync#upload | Invalid Request Params Found for upload request.");
                throw new ClientException("ERR_INVALID_REQUEST_PARAM(S)",
                        "Invalid Request Params Found for upload request.");
            }
            switch (operation) {
                case "uploadFile" :
                    OK(CloudStoreParams.uploadedUrl.name(), CloudStore.uploadFile(folderName, f, slugFile), parent, getSelf());
                    break;
                case "uploadDirectory" :
                    OK(CloudStoreParams.uploadedUrl.name(), CloudStore.uploadDirectory(folderName, f, slugFile), parent, getSelf());
                    break;
                default:
                    TelemetryManager.error("CloudStoreAsync#upload | Upload Operation " + operation + "not Supported");
                    throw new ServerException("ERR_OPERATION_NOT_SUPPORTED", "Upload Operation " + operation + " not Supported");
            }
        } catch (Exception e) {
            cleanUpCloudFolder(folderName);
            TelemetryManager.error("Error! Something went wrong while uploading the Content Package on Cloud Storage Space.");
            throw new ServerException(ContentErrorCodes.UPLOAD_ERROR.name(),
                    "Error! Something went wrong while uploading the Content Package on Storage Space.", e);
        } finally {
            try {
                log("Deleting Locally Extracted File.");
                File dir = new File(basePath);
                if (deleteFile && dir.exists()) {
                    dir.delete();
                }
            } catch (SecurityException e) {
                TelemetryManager.error(
                        "Error! While deleting the local extraction directory: " + basePath, e);
            } catch (Exception e) {
                TelemetryManager.error(
                        "Error! Something Went wrong while deleting the local extraction directory: " + basePath, e);
            }
        }
    }

    private void cleanUpCloudFolder(String cloudFolderPath) {
        try {
            log("Cleaning Cloud Folder Path: " + cloudFolderPath);
            if (StringUtils.isNoneBlank(cloudFolderPath))
                CloudStore.deleteFile(cloudFolderPath, true);
        } catch (Exception ex) {
            TelemetryManager.error("Error! While Cleanup of Half Extracted Folder from Cloud: " + ex.getMessage(), ex);
        }
    }

}
