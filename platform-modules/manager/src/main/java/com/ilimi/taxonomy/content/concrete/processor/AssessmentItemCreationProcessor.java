package com.ilimi.taxonomy.content.concrete.processor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import com.ilimi.assessment.enums.QuestionnaireType;
import com.ilimi.assessment.mgr.IAssessmentManager;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.taxonomy.content.common.ContentConfigurationConstants;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Content;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.processor.AbstractProcessor;
import com.ilimi.taxonomy.enums.ContentAPIParams;

public class AssessmentItemCreationProcessor extends AbstractProcessor {
	
	@Autowired
	private IAssessmentManager assessmentMgr;
	
	private static Logger LOGGER = LogManager.getLogger(AssessmentItemCreationProcessor.class.getName());
	
	public AssessmentItemCreationProcessor(String basePath, String contentId) {
		this.basePath = basePath;
		this.contentId = contentId;
	}

	@Override
	protected Content process(Content content) {
		try {
			
		} catch(Exception e) {
			 LOGGER.error("", e);
		}
		return content;
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private Map<String, Object> createAssessmentItemGraph(Content content) {
		Map<String, Object> assessmentItemCreationFilewiseResult = new HashMap<String, Object>();
		try {
			List<Relation> outRelations = new ArrayList<Relation>();
			List<File> controllerFileList = getControllersFileList(content.getControllers(), ContentWorkflowPipelineParams.data.name(), basePath);
			if (null != controllerFileList) {
				for (File file: controllerFileList) {
					if (file.exists()) {
						Map<String, Object> assessmentItemMap = new ObjectMapper().readValue(file, HashMap.class);
						if (null != assessmentItemMap) {
							Map<String, Object> itemSet = (Map<String, Object>) assessmentItemMap.get(ContentWorkflowPipelineParams.items.name());
							for (Entry<String, Object> entry : itemSet.entrySet()) {
								Object assessmentItem = (Object) entry.getValue();
								List<Map<String, Object>> lstMap = (List<Map<String, Object>>) assessmentItem;
								List<String> lstAssessmentItemId = new ArrayList<String>();
								Map<String, Object> assessResMap = new HashMap<String, Object>();
								Map<String, String> mapAssessItemRes = new HashMap<String, String>();
								Map<String, Object> mapRelation = new HashMap<String, Object>();
								for (Map<String, Object> map : lstMap) {
									Request request = null; //getAssessmentItemRequestObject(map, "AssessmentItem", contentId,
											// ContentAPIParams.assessment_item.name());
									if (null != request) {
										Node itemNode = (Node) request.get(ContentAPIParams.assessment_item.name());
										Response response = null;
										if (StringUtils.isBlank(itemNode.getIdentifier())) {
											response = assessmentMgr.createAssessmentItem(ContentConfigurationConstants.GRAPH_ID, request);
										} else {
											response = assessmentMgr.updateAssessmentItem(itemNode.getIdentifier(),
													ContentConfigurationConstants.GRAPH_ID, request);
										}
										LOGGER.info("Create Item | Response: " + response);
										Map<String, Object> resMap = response.getResult();
										if (null != resMap.get(ContentAPIParams.node_id.name())) {
											String identifier = (String) resMap.get(ContentAPIParams.node_id.name());
											mapRelation.put(identifier, map.get(ContentAPIParams.concepts.name()));
											lstAssessmentItemId.add(identifier);
											mapAssessItemRes.put(identifier,
													"Assessment Item " + identifier + " Added Successfully");
										} else {
											System.out.println("Item validation failed: "
													+ resMap.get(ContentAPIParams.messages.name()));
											String id = (String) map.get(ContentAPIParams.identifier.name());
											if (StringUtils.isNotBlank(id))
												mapAssessItemRes.put(id,
														(String) resMap.get(ContentAPIParams.messages.name()));
										}
									}
								}
								assessResMap.put(ContentAPIParams.assessment_item.name(), mapAssessItemRes);
								Response itemSetRes = createItemSet(lstAssessmentItemId, assessmentItemMap);
								if (null != itemSetRes) {
									Map<String, Object> mapItemSetRes = itemSetRes.getResult();
									assessResMap.put(ContentAPIParams.assessment_item_set.name(), mapItemSetRes);
									String itemSetNodeId = (String) mapItemSetRes.get(ContentAPIParams.set_id.name());
									System.out.println("itemSetNodeId: " + itemSetNodeId);
									if (StringUtils.isNotBlank(itemSetNodeId)) {
										Relation outRel = new Relation(null, RelationTypes.ASSOCIATED_TO.relationName(),
												itemSetNodeId);
										outRelations.add(outRel);
									}
								}
								List<String> lstAssessItemRelRes = createRelation(ContentConfigurationConstants.GRAPH_ID, mapRelation,
										outRelations);
								assessResMap.put(ContentAPIParams.AssessmentItemRelation.name(), lstAssessItemRelRes);
//								mapResDetail.put(file.getName(), assessResMap);
							}
						}
					}
				}
			}
		} catch (IOException e) { 
			LOGGER.error(ContentErrorMessageConstants.CONTROLLER_ASSESSMENT_ITEM_JSON_OBJECT_CONVERSION_CASTING_ERROR, e);
		}
		return assessmentItemCreationFilewiseResult;
	}
	
	@SuppressWarnings("unused")
	private Response createItemSet(List<String> assessmentItemIds, Map<String, Object> fileJSON) {
		if (null != assessmentItemIds && assessmentItemIds.size() > 0 && StringUtils.isNotBlank(ContentConfigurationConstants.GRAPH_ID)) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(ContentAPIParams.memberIds.name(), assessmentItemIds);
			Integer totalItems = (Integer) fileJSON.get("total_items");
			if (null == totalItems || totalItems > assessmentItemIds.size())
				totalItems = assessmentItemIds.size();
			map.put("total_items", totalItems);
			Integer maxScore = (Integer) fileJSON.get("max_score");
			if (null == maxScore)
				maxScore = totalItems;
			map.put("max_score", maxScore);
			String title = (String) fileJSON.get("title");
			if (StringUtils.isNotBlank(title))
				map.put("title", title);
			map.put("type", QuestionnaireType.materialised.name());
			String identifier = (String) fileJSON.get("identifier");
			if (StringUtils.isNotBlank(identifier)) {
				map.put("code", identifier);
			} else {
				map.put("code", "item_set_" + RandomUtils.nextInt(1, 10000));
			}
			Request request = null; // getAssessmentItemRequestObject(map, "ItemSet", contentId,
					// ContentAPIParams.assessment_item_set.name());
			if (null != request) {
				Response response = assessmentMgr.createItemSet(ContentConfigurationConstants.GRAPH_ID, request);
				LOGGER.info("Create Item | Response: " + response);
				return response;
			}
		}
		return null;
	}

}
