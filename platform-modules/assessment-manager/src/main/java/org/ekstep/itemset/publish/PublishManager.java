package org.ekstep.itemset.publish;

import org.apache.commons.collections.CollectionUtils;
import org.ekstep.assessment.enums.AssessmentErrorCodes;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.itemset.handler.QuestionPaperGenerator;
import org.ekstep.learning.util.CloudStore;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.io.File;
import java.util.List;

public class PublishManager {
    private static final ControllerUtil controllerUtil = new ControllerUtil();
    private static final String TAXONOMY_ID = "domain";
    private static final String CONTENT_FOLDER = "cloud_storage.content.folder";
    private static final String QUESTION_PAPER_FOLDER = "cloud_storage.question_paper.folder";

    public static String publish(List<Node> itemSets) throws Exception {
        if (CollectionUtils.isNotEmpty(itemSets)) {
            Node itemSet = controllerUtil.getNode(TAXONOMY_ID, itemSets.get(0).getIdentifier());
            File previewFile = QuestionPaperGenerator.generateQuestionPaper(itemSet);
            if (null != previewFile) {
                String previewUrl = uploadFileToCloud(previewFile, QUESTION_PAPER_FOLDER);
                itemSet.getMetadata().put("previewUrl", previewUrl);
                Response response = controllerUtil.updateNode(itemSet);
                if (response.getResponseCode() != ResponseCode.OK)
                    throw new ServerException(AssessmentErrorCodes.ERR_ASSESSMENT_UPDATE.name(), "AssessmentItem with identifier: " + itemSet + "couldn't be updated");
                return previewUrl;
            }
        }
        return null;
    }

    private static String uploadFileToCloud(File file, String objectFolderName) {
        try {
            String folder = S3PropertyReader.getProperty(CONTENT_FOLDER);
            folder = folder + "/" + S3PropertyReader.getProperty(objectFolderName);
            String[] urlArray = CloudStore.uploadFile(folder, file, true);
            return urlArray[1];
        } catch (Exception e) {
            TelemetryManager.error("Error while uploading the file.", e);
            throw new ServerException(AssessmentErrorCodes.ERR_ASSESSMENT_UPLOAD_FILE.name(),
                    "Error while uploading the File.", e);
        }
    }
}
