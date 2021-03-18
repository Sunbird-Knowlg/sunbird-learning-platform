package org.sunbird.itemset.publish;

import java.io.File;

import org.sunbird.assessment.enums.AssessmentErrorCodes;
import org.sunbird.common.exception.ServerException;
import org.sunbird.common.util.S3PropertyReader;
import org.sunbird.learning.util.CloudStore;
import org.sunbird.telemetry.logger.TelemetryManager;

public class ItemsetPublishManagerUtil {
	
	private static final String ITEMSET_FOLDER = "cloud_storage.itemset.folder";

	public static String uploadFileToCloud(File file, String identifier) {
        try {
            String folder = S3PropertyReader.getProperty(ITEMSET_FOLDER) + "/" + identifier;
            String[] urlArray = CloudStore.uploadFile(folder, file, true);
            return urlArray[1];
        } catch (Exception e) {
            TelemetryManager.error("Error while uploading the file.", e);
            throw new ServerException(AssessmentErrorCodes.ERR_ASSESSMENT_UPLOAD_FILE.name(),
                    "Error while uploading the File.", e);
        }
    }
}
