package com.ilimi.taxonomy.content.concrete.processor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.ilimi.common.dto.RequestParams;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.taxonomy.content.common.ContentConfigurationConstants;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.processor.AbstractProcessor;

public class AssessmentItemCreatorProcessor extends AbstractProcessor {
	
	@Autowired
	private IAssessmentManager assessmentMgr;
	
	private static Logger LOGGER = LogManager.getLogger(AssessmentItemCreatorProcessor.class.getName());
	
	private ObjectMapper mapper = new ObjectMapper();
	
	private List<String> questionLevelList = Arrays.asList("EASY", "MEDIUM", "DIFFICULT", "RARE");
	
	public AssessmentItemCreatorProcessor(String basePath, String contentId) {
		if (!isValidBasePath(basePath))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Path does not Exist.]");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Invalid Content Id.]");
		this.basePath = basePath;
		this.contentId = contentId;
	}

	@Override
	protected Plugin process(Plugin plugin) {
		try {
			createAssessmentItemSubGraph(plugin);
		} catch(Exception e) {
			throw new ServerException(ContentErrorCodeConstants.PROCESSOR_ERROR.name(), 
					ContentErrorMessageConstants.PROCESSOR_ERROR + " | [AssessmentItemCreatorProcessor]", e);
		}
		return plugin;
	}
	
	@SuppressWarnings({ "unchecked" })
	private Map<String, Object> createAssessmentItemSubGraph(Plugin plugin) {
		Map<String, Object> assessmentItemCreationFilewiseResult = new HashMap<String, Object>();
		try {
			List<Relation> outRelations = new ArrayList<Relation>();
			List<File> controllerFileList = getControllersFileList(plugin.getControllers(), ContentWorkflowPipelineParams.data.name(), basePath);
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
									Request request = getAssessmentModelRequestObject(map, ContentWorkflowPipelineParams.AssessmentItem.name(), contentId,
											ContentWorkflowPipelineParams.assessment_item.name());
									if (null != request) {
										Node itemNode = (Node) request.get(ContentWorkflowPipelineParams.assessment_item.name());
										Response response = null;
										if (StringUtils.isBlank(itemNode.getIdentifier())) {
											response = assessmentMgr.createAssessmentItem(ContentConfigurationConstants.GRAPH_ID, request);
										} else {
											response = assessmentMgr.updateAssessmentItem(itemNode.getIdentifier(),
													ContentConfigurationConstants.GRAPH_ID, request);
										}
										LOGGER.info("Create Item | Response: " + response);
										Map<String, Object> resMap = response.getResult();
										if (null != resMap.get(ContentWorkflowPipelineParams.node_id.name())) {
											String identifier = (String) resMap.get(ContentWorkflowPipelineParams.node_id.name());
											mapRelation.put(identifier, map.get(ContentWorkflowPipelineParams.concepts.name()));
											lstAssessmentItemId.add(identifier);
											mapAssessItemRes.put(identifier,
													"Assessment Item " + identifier + " Added Successfully");
										} else {
											String id = (String) map.get(ContentWorkflowPipelineParams.identifier.name());
											if (StringUtils.isNotBlank(id))
												mapAssessItemRes.put(id,
														(String) resMap.get(ContentWorkflowPipelineParams.messages.name()));
										}
									}
								}
								assessResMap.put(ContentWorkflowPipelineParams.assessment_item.name(), mapAssessItemRes);
								Response itemSetRes = createItemSet(lstAssessmentItemId, assessmentItemMap);
								if (null != itemSetRes) {
									Map<String, Object> mapItemSetRes = itemSetRes.getResult();
									assessResMap.put(ContentWorkflowPipelineParams.assessment_item_set.name(), mapItemSetRes);
									String itemSetNodeId = (String) mapItemSetRes.get(ContentWorkflowPipelineParams.set_id.name());
									if (StringUtils.isNotBlank(itemSetNodeId)) {
										Relation outRel = new Relation(null, RelationTypes.ASSOCIATED_TO.relationName(),
												itemSetNodeId);
										outRelations.add(outRel);
									}
								}
								List<String> lstAssessItemRelRes = createRelation(ContentConfigurationConstants.GRAPH_ID, mapRelation,
										outRelations);
								assessResMap.put(ContentWorkflowPipelineParams.AssessmentItemRelation.name(), lstAssessItemRelRes);
								assessmentItemCreationFilewiseResult.put(file.getName(), assessResMap);
							}
						} else {
							assessmentItemCreationFilewiseResult.put(file.getName(), ContentErrorMessageConstants.INVALID_JSON);
						}
					} else {
						assessmentItemCreationFilewiseResult.put(file.getName(), ContentErrorMessageConstants.FILE_DOES_NOT_EXIST);
					}
				}
			}
		} catch (IOException e) { 
			LOGGER.error(ContentErrorMessageConstants.CONTROLLER_ASSESSMENT_ITEM_JSON_OBJECT_CONVERSION_CASTING_ERROR, e);
		}
		return assessmentItemCreationFilewiseResult;
	}
	
	private Response createItemSet(List<String> assessmentItemIds, Map<String, Object> assessmentItemMap) {
		Response response = new Response();
		if (null != assessmentItemIds) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(ContentWorkflowPipelineParams.memberIds.name(), assessmentItemIds);
			Integer totalItems = (Integer) assessmentItemMap.get(ContentWorkflowPipelineParams.total_items.name());
			if (null == totalItems || totalItems > assessmentItemIds.size())
				totalItems = assessmentItemIds.size();
			map.put(ContentWorkflowPipelineParams.total_items.name(), totalItems);
			Integer maxScore = (Integer) assessmentItemMap.get(ContentWorkflowPipelineParams.max_score.name());
			if (null == maxScore)
				maxScore = totalItems;
			map.put(ContentWorkflowPipelineParams.max_score.name(), maxScore);
			String title = (String) assessmentItemMap.get(ContentWorkflowPipelineParams.title.name());
			if (StringUtils.isNotBlank(title))
				map.put(ContentWorkflowPipelineParams.title.name(), title);
			map.put(ContentWorkflowPipelineParams.type.name(), QuestionnaireType.materialised.name());
			String identifier = (String) assessmentItemMap.get(ContentWorkflowPipelineParams.identifier.name());
			if (StringUtils.isNotBlank(identifier)) {
				map.put(ContentWorkflowPipelineParams.code.name(), identifier);
			} else {
				map.put(ContentWorkflowPipelineParams.code.name(), ContentWorkflowPipelineParams.item_set_.name() + RandomUtils.nextInt(1, 10000));
			}
			Request request = getAssessmentModelRequestObject(map, ContentWorkflowPipelineParams.ItemSet.name(), contentId,
					ContentWorkflowPipelineParams.assessment_item_set.name());
			if (null != request) {
				response = assessmentMgr.createItemSet(ContentConfigurationConstants.GRAPH_ID, request);
				LOGGER.info("Create Item | Response: " + response);
			}
		}
		return response;
	}
	
	private Request getAssessmentModelRequestObject(Map<String, Object> map, String objectType, String contentId,
			String param) {
		Request request = new Request();
		if (!StringUtils.isBlank(objectType) && null != map) {
			Map<String, Object> reqMap = new HashMap<String, Object>();
			Map<String, Object> assessMap = new HashMap<String, Object>();
			Map<String, Object> requestMap = new HashMap<String, Object>();
			reqMap.put(ContentWorkflowPipelineParams.objectType.name(), objectType);
			reqMap.put(ContentWorkflowPipelineParams.metadata.name(), map);
			String identifier = null;
			if (null != map.get(ContentWorkflowPipelineParams.qid.name())) {
				String qid = (String) map.get(ContentWorkflowPipelineParams.qid.name());
				if (StringUtils.isNotBlank(qid))
					identifier = qid;
			}
			if (StringUtils.isBlank(identifier)) {
				if (null != map.get(ContentWorkflowPipelineParams.identifier.name())) {
					String id = (String) map.get(ContentWorkflowPipelineParams.identifier.name());
					if (StringUtils.isNotBlank(id))
						identifier = id;
				}
			}
			if (StringUtils.isNotBlank(identifier)) {
				reqMap.put(ContentWorkflowPipelineParams.identifier.name(), identifier);
				map.put(ContentWorkflowPipelineParams.code.name(), getCodeByObjectType(identifier, objectType));
				map.put(ContentWorkflowPipelineParams.name.name(), getCodeByObjectType(identifier, objectType));
			} else {
				map.put(ContentWorkflowPipelineParams.name.name(), ContentWorkflowPipelineParams.AssessmentItemObject.name());
				map.put(ContentWorkflowPipelineParams.code.name(), ContentWorkflowPipelineParams.item_.name() + RandomUtils.nextInt(1, 10000));
			}
			map.put(ContentWorkflowPipelineParams.usedIn.name(), contentId);
			String qlevel = (String) map.get(ContentWorkflowPipelineParams.qlevel.name());
			if (StringUtils.isBlank(qlevel)) {
				qlevel = ContentWorkflowPipelineParams.MEDIUM.name();
			} else {
				if (!questionLevelList.contains(qlevel))
					qlevel = ContentWorkflowPipelineParams.MEDIUM.name();
			}
			map.put(ContentWorkflowPipelineParams.qlevel.name(), qlevel);
			assessMap.put(param, reqMap);
			requestMap.put(ContentWorkflowPipelineParams.skipValidations.name(), true);
			requestMap.put(ContentWorkflowPipelineParams.request.name(), assessMap);
			request = getAssessmentMgrRequestObject(requestMap, param);
		}
		return request;
	}
	
	private Request getAssessmentMgrRequestObject(Map<String, Object> requestMap, String param) {
		Request request = getRequest(requestMap);
		Map<String, Object> map = request.getRequest();
		if (null != map && !map.isEmpty()) {
			try {
				Object obj = map.get(param);
				if (null != obj) {
					Node item = (Node) mapper.convertValue(obj, Node.class);
					request.put(param, item);
					request.put(ContentWorkflowPipelineParams.skipValidations.name(), true);
				}
			} catch (Exception e) {
				LOGGER.error(ContentErrorMessageConstants.ASSESSMENT_MANAGER_REQUEST_OBJECT_CREATION_ERROR, e);
			}
		}
		return request;
	}
	
	@SuppressWarnings("unchecked")
	private Request getRequest(Map<String, Object> requestMap) {
		Request request = new Request();
		if (null != requestMap) {
			request.setId((String) requestMap.get(ContentWorkflowPipelineParams.id.name()));
			request.setVer((String) requestMap.get(ContentWorkflowPipelineParams.ver.name()));
			request.setTs((String) requestMap.get(ContentWorkflowPipelineParams.ts.name()));
			Object reqParams = requestMap.get(ContentWorkflowPipelineParams.params.name());
			if (null != reqParams) {
				try {
					RequestParams params = (RequestParams) mapper.convertValue(reqParams, RequestParams.class);
					request.setParams(params);
				} catch (Exception e) {
				}
			}
			Object requestObj = requestMap.get(ContentWorkflowPipelineParams.request.name());
			if (null != requestObj) {
				try {
					String strRequest = mapper.writeValueAsString(requestObj);
					Map<String, Object> map = mapper.readValue(strRequest, Map.class);
					if (null != map && !map.isEmpty())
						request.setRequest(map);
				} catch (Exception e) {
					LOGGER.error(ContentErrorMessageConstants.ASSESSMENT_MANAGER_REQUEST_OBJECT_CREATION_ERROR, e);
				}
			}
		}
		return request;
	}
	
	private String getCodeByObjectType(String identifier, String objectType) {
		String code = "";
		if (!StringUtils.isBlank(identifier) && !StringUtils.isBlank(objectType)) {
			if (StringUtils.equalsIgnoreCase(objectType, ContentWorkflowPipelineParams.ItemSet.name())) {
				code = ContentConfigurationConstants.DEFAULT_ASSESSMENT_ITEM_SET_CODE_PREFIX + identifier;
			} else if (StringUtils.equalsIgnoreCase(objectType, ContentWorkflowPipelineParams.AssessmentItem.name())) {
				code = ContentConfigurationConstants.DEFAULT_ASSESSMENT_ITEM_CODE_PREFIX + identifier;
			}
		}
		return code;
	}

}
