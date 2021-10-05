package org.sunbird.itemset.publish;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.assessment.enums.AssessmentErrorCodes;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.itemset.handler.QuestionPaperGenerator;
import org.sunbird.learning.util.ControllerUtil;
import org.sunbird.telemetry.logger.TelemetryManager;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemsetPublishManager {
	private ControllerUtil controllerUtil;
	public ItemsetPublishManager(ControllerUtil controllerUtil) {
		this.controllerUtil = controllerUtil;
	} 
	private static final String TAXONOMY_ID = "domain";
    private static final List<String> assessmentItemAcceptedStatus = Arrays.asList("Draft", "Review", "Live");
    ObjectMapper mapper = new ObjectMapper();
    public String publish(List<String> itemSetIdetifiers) throws Exception {
        if (CollectionUtils.isNotEmpty(itemSetIdetifiers)) {
            Node itemSet = controllerUtil.getNode(TAXONOMY_ID, itemSetIdetifiers.get(0));
            List<Relation> outRelations = itemSet.getOutRelations();
            if(CollectionUtils.isEmpty(outRelations))
            		return null;
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
            		
            		File previewFile = QuestionPaperGenerator.generateQuestionPaper(itemSet);
            		if(null == previewFile) {
            			TelemetryManager.error("Itemset questionPeper generated null file :: " + itemSetIdetifiers.get(0));
            			throw new ServerException(AssessmentErrorCodes.ERR_QUESTIONPAPER_FILE_GENERATE.name(), "Question paper for identifier: " + itemSet + "couldn't be generated.");
            		}
            			
                if (null != previewFile) {
                    String previewUrl = ItemsetPublishManagerUtil.uploadFileToCloud(previewFile, itemSet.getIdentifier());
                    if(null == previewUrl) {
                    		TelemetryManager.error("QuestionPeper - upload file - failed for Itemset :: " + itemSetIdetifiers.get(0));
                    		throw new ServerException(AssessmentErrorCodes.ERR_QUESTIONPAPER_UPLOAD_FAILED.name(), "QuestionPaper upload failed for identifier: " + itemSet + ".");
                    }
                    itemSet.getMetadata().put("previewUrl", previewUrl);
                    itemSet.getMetadata().put("status", "Live");
                    
                    response = controllerUtil.updateNode(itemSet);
                    if(previewFile.exists())
                    		previewFile.delete();
                    if (response.getResponseCode() != ResponseCode.OK) {
                    		TelemetryManager.error("Itemset publish operation failed for :: " + itemSet.getIdentifier() + " ::::: " + response.getParams() + " ::::: " + response.getResponseCode() + " ::::::: " + response.getResult());
                    		throw new ServerException(AssessmentErrorCodes.ERR_ASSESSMENT_UPDATE.name(), "AssessmentItem with identifier: " + itemSet + "couldn't be updated");
                    }
                    return previewUrl;
                }
            }
        }
        TelemetryManager.error("Itemset List is empty :: " + itemSetIdetifiers);
        return null;
    }
}
