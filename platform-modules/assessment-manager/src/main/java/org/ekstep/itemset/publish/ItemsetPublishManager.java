package org.ekstep.itemset.publish;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.assessment.enums.AssessmentErrorCodes;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.itemset.handler.QuestionPaperGenerator;
import org.ekstep.learning.util.CloudStore;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemsetPublishManager {
    private static final ControllerUtil controllerUtil = new ControllerUtil();
    private static final String TAXONOMY_ID = "domain";
    private static final String ITEMSET_FOLDER = "cloud_storage.itemset.folder";
    private static final List<String> assessmentItemAcceptedStatus = Arrays.asList("Draft", "Review", "Live");

    public static String publish(List<String> itemSetIdetifiers) throws Exception {
        if (CollectionUtils.isNotEmpty(itemSetIdetifiers)) {
            Node itemSet = controllerUtil.getNode(TAXONOMY_ID, itemSetIdetifiers.get(0));
            List<Relation> outRelations = itemSet.getOutRelations();
            List<String> assessmentItemIds = outRelations.stream().filter(r -> StringUtils.equalsIgnoreCase(r.getEndNodeObjectType(), "AssessmentItem")).map(x -> x.getEndNodeId()).collect(Collectors.toList());
            Response response = null;
            if(CollectionUtils.isNotEmpty(assessmentItemIds)) {
            		response = controllerUtil.getDataNodes(TAXONOMY_ID, assessmentItemIds);
            		if(response.getResponseCode() != ResponseCode.OK) {
            			TelemetryManager.error("Fetching Itemset linked question failed for :: " + itemSet.getIdentifier());
            			throw new ServerException(AssessmentErrorCodes.ERR_ASSESSMENT_BULK_READ.name(), "AssessmentItem linked to ItemSet: " + itemSet + " couldn't be fetched.");
            		}
            		List<Node> assessmentItems = (List<Node>)response.getResult().get("node_list");
            		List<Node> assessmentItemAcceptedPublish = assessmentItems.stream().filter(q -> assessmentItemAcceptedStatus.contains(q.getMetadata().get("status"))).collect(Collectors.toList());
            		List<String> assessmentItemNotAcceptedPublish = assessmentItems.stream().filter(q -> !assessmentItemAcceptedStatus.contains(q.getMetadata().get("status"))).map(x -> x.getIdentifier()).collect(Collectors.toList());
            		if(CollectionUtils.isNotEmpty(assessmentItemAcceptedPublish)) {
                		Map<String, Object> metadata = new HashMap<>();metadata.put("status", "Live");
                		response = controllerUtil.updateNodes(assessmentItemAcceptedPublish, metadata);
            			if(response.getResponseCode() != ResponseCode.OK) {
            				TelemetryManager.error("AssessmentItem update failed for ItemSet: " + itemSet.getIdentifier());
            				throw new ServerException(AssessmentErrorCodes.ERR_ASSESSMENT_BULK_UPDATE.name(), "AssessmentItem linked to ItemSet: " + itemSet + " couldn't be updated.");
            			}
            		}
            		if(CollectionUtils.isNotEmpty(assessmentItemNotAcceptedPublish)) {
            			List<Relation> outRelationForPublish = itemSet.getOutRelations().stream().filter(x -> !assessmentItemNotAcceptedPublish.contains(x.getEndNodeId())).collect(Collectors.toList());
            			itemSet.setOutRelations(outRelationForPublish);
            		}
            }
            
            File previewFile = QuestionPaperGenerator.generateQuestionPaper(itemSet);
            if (null != previewFile) {
                String previewUrl = uploadFileToCloud(previewFile, itemSet.getIdentifier());
                itemSet.getMetadata().put("previewUrl", previewUrl);
                itemSet.getMetadata().put("status", "Live");
                
                response = controllerUtil.updateNode(itemSet);
                if (response.getResponseCode() != ResponseCode.OK) {
                		previewFile.delete();
                		TelemetryManager.error("Itemset publish operation failed for :: " + itemSet.getIdentifier());
                		TelemetryManager.error("response.getParams() :: " + response.getParams());
                		TelemetryManager.error("response.getResponseCode() :: " + response.getResponseCode());
                		TelemetryManager.error("response.getResult() :: " + response.getResult());
                    throw new ServerException(AssessmentErrorCodes.ERR_ASSESSMENT_UPDATE.name(), "AssessmentItem with identifier: " + itemSet + "couldn't be updated");
                }
                return previewUrl;
            }
        }
        TelemetryManager.error("Itemset List is empty :: " + itemSetIdetifiers.size());
        return null;
    }

    private static String uploadFileToCloud(File file, String identifier) {
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
