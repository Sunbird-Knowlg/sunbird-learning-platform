package org.sunbird.content.concrete.processor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.content.common.ContentConfigurationConstants;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.entity.Plugin;
import org.sunbird.content.enums.ContentErrorCodeConstants;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.content.processor.AbstractProcessor;
import org.sunbird.telemetry.logger.TelemetryManager;

/**
 * The Class AssessmentItemCreatorProcessor is responsible of creating the
 * Assessment Items into the Graph by reading the Controller of type items.
 * 
 * It reads all the files corresponding to the controller and iterate over all
 * of them one by one and create the Items in Graph
 * 
 * After Creating all the Assessment Items It creates the Set also.
 * 
 * @author Mohammad Azharuddin
 * 
 * @see AssetCreatorProcessor
 * @see AssetsValidatorProcessor
 * @see BaseConcreteProcessor
 * @see EmbedControllerProcessor
 * @see GlobalizeAssetProcessor
 * @see LocalizeAssetProcessor
 * @see MissingAssetValidatorProcessor
 * @see MissingControllerValidatorProcessor
 * 
 */
public class AssessmentItemCreatorProcessor extends AbstractProcessor {

//	@Autowired
//	private IAssessmentManager assessmentMgr;

	/** The logger. */
	

//	private ObjectMapper mapper = new ObjectMapper();

	/** The valid list of question difficulty level. */
	private List<String> questionLevelList = Arrays.asList("EASY", "MEDIUM", "DIFFICULT", "RARE");

	/**
	 * Instantiates a new assessment item creator processor and sets the base
	 * path and current content id for further processing.
	 *
	 * @param basePath
	 *            the base path is the location for content package file handling and all manipulations. 
	 * @param contentId
	 *            the content id is the identifier of content for which the Processor is being processed currently.
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sunbird.taxonomy.content.processor.AbstractProcessor#process(org.sunbird.
	 * taxonomy.content.entity.Plugin)
	 */
	@Override
	protected Plugin process(Plugin plugin) {
		try {
			TelemetryManager.log("Calling 'createAssessmentItemSubGraph' Inner Operation.");
			createAssessmentItemSubGraph(plugin);
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodeConstants.PROCESSOR_ERROR.name(),
					ContentErrorMessageConstants.PROCESSOR_ERROR + " | [AssessmentItemCreatorProcessor]", e);
		}
		TelemetryManager.log("Returning the ECRF (Plugin) Object"+ plugin);
		return plugin;
	}

	/**
	 * Creates the assessment item sub graph by iterating over all the files from the controllers whose type is item..
	 *
	 * @param plugin
	 *            the plugin
	 * @return the map
	 */

	private Map<String, Object> createAssessmentItemSubGraph(Plugin plugin) {
//		PlatformLogger.log("ECRF Object (Plugin): ", plugin + " | [Content Id '"+ contentId +"']");
//		Map<String, Object> assessmentItemCreationFilewiseResult = new HashMap<String, Object>();
//		try {
//			List<Relation> outRelations = new ArrayList<Relation>();
//			
//			PlatformLogger.log("Fetching the Controller File List." + " | [Content Id '"+ contentId +"']");
//			List<File> controllerFileList = getControllersFileList(plugin.getControllers(),
//					ContentWorkflowPipelineParams.data.name(), basePath);
//			PlatformLogger.log("Total no. of Item Controllers: " + controllerFileList.size());
//			
//			if (null != controllerFileList) {
//				PlatformLogger.log("Iterating over all the Assessment Item JSON Files." + " | [Content Id '"+ contentId +"']");
//				for (File file : controllerFileList) {
//					PlatformLogger.log("Processing for File Name: " + file.getName() + " | [Content Id '"+ contentId +"']");
//					if (file.exists()) {
//						Map<String, Object> assessmentItemMap = new ObjectMapper().readValue(file, HashMap.class);
//						if (null != assessmentItemMap) {
//							Map<String, Object> itemSet = (Map<String, Object>) assessmentItemMap
//									.get(ContentWorkflowPipelineParams.items.name());
//							PlatformLogger.log("Iterating over the Controllers of Item Type Only." + " | [Content Id '"+ contentId +"']");
//							for (Entry<String, Object> entry : itemSet.entrySet()) {
//								Object assessmentItem = (Object) entry.getValue();
//								List<Map<String, Object>> lstMap = (List<Map<String, Object>>) assessmentItem;
//								List<String> lstAssessmentItemId = new ArrayList<String>();
//								Map<String, Object> assessResMap = new HashMap<String, Object>();
//								Map<String, String> mapAssessItemRes = new HashMap<String, String>();
//								Map<String, Object> mapRelation = new HashMap<String, Object>();
//								for (Map<String, Object> map : lstMap) {
//									PlatformLogger.log("Creating the Request Object For Item: ", map);
//									Request request = getAssessmentModelRequestObject(map,
//											ContentWorkflowPipelineParams.AssessmentItem.name(), contentId,
//											ContentWorkflowPipelineParams.assessment_item.name());
//									if (null != request) {
//										Node itemNode = (Node) request
//												.get(ContentWorkflowPipelineParams.assessment_item.name());
//										Response response = null;
//										if (StringUtils.isBlank(itemNode.getIdentifier())) {
////											response = assessmentMgr.createAssessmentItem(
////													ContentConfigurationConstants.GRAPH_ID, request);
//										} else {
////											response = assessmentMgr.updateAssessmentItem(itemNode.getIdentifier(),
////													ContentConfigurationConstants.GRAPH_ID, request);
//										}
//										PlatformLogger.log("Create Item | Response: " + response + " | [Content Id '"+ contentId +"']");
//										Map<String, Object> resMap = response.getResult();
//										if (null != resMap.get(ContentWorkflowPipelineParams.node_id.name())) {
//											String identifier = (String) resMap
//													.get(ContentWorkflowPipelineParams.node_id.name());
//											mapRelation.put(identifier,
//													map.get(ContentWorkflowPipelineParams.concepts.name()));
//											lstAssessmentItemId.add(identifier);
//											mapAssessItemRes.put(identifier,
//													"Assessment Item " + identifier + " Added Successfully");
//										} else {
//											String id = (String) map
//													.get(ContentWorkflowPipelineParams.identifier.name());
//											if (StringUtils.isNotBlank(id))
//												mapAssessItemRes.put(id, (String) resMap
//														.get(ContentWorkflowPipelineParams.messages.name()));
//										}
//									}
//								}
//								PlatformLogger.log("Adding and Entry in Response Map." + " | [Content Id '"+ contentId +"']");
//								assessResMap.put(ContentWorkflowPipelineParams.assessment_item.name(),
//										mapAssessItemRes);
//								
//								PlatformLogger.log("Creating Item Set." + " | [Content Id '"+ contentId +"']");
//								Response itemSetRes = createItemSet(lstAssessmentItemId, assessmentItemMap);
//								if (null != itemSetRes) {
//									Map<String, Object> mapItemSetRes = itemSetRes.getResult();
//									assessResMap.put(ContentWorkflowPipelineParams.assessment_item_set.name(),
//											mapItemSetRes);
//									String itemSetNodeId = (String) mapItemSetRes
//											.get(ContentWorkflowPipelineParams.set_id.name());
//									PlatformLogger.log("Item Set ID: " + itemSetNodeId + " | [Content Id '"+ contentId +"']");
//									if (StringUtils.isNotBlank(itemSetNodeId)) {
//										Relation outRel = new Relation(null, RelationTypes.ASSOCIATED_TO.relationName(),
//												itemSetNodeId);
//										outRelations.add(outRel);
//									}
//									PlatformLogger.log("Creatign 'outRelations'." + " | [Content Id '"+ contentId +"']");
//								}
//								List<String> lstAssessItemRelRes = createRelation(
//										ContentConfigurationConstants.GRAPH_ID, mapRelation, outRelations);
//								PlatformLogger.log("Adding and Entry in Assessment Item Creation Response Map." + " | [Content Id '"+ contentId +"']");
//								assessResMap.put(ContentWorkflowPipelineParams.AssessmentItemRelation.name(),
//										lstAssessItemRelRes);
//								PlatformLogger.log("Adding and Entry in File Wise Assessment Item Creation Response Map." + " | [Content Id '"+ contentId +"']");
//								assessmentItemCreationFilewiseResult.put(file.getName(), assessResMap);
//							}
//						} else {
//							LOGGER.warn("Invalid JSON File." + file.getName() + " | [Content Id '"+ contentId +"']");
//							assessmentItemCreationFilewiseResult.put(file.getName(),
//									ContentErrorMessageConstants.INVALID_JSON);
//						}
//					} else {
//						LOGGER.warn("File Doesn't Exist." + file.getName() + " | [Content Id '"+ contentId +"']");
//						assessmentItemCreationFilewiseResult.put(file.getName(),
//								ContentErrorMessageConstants.FILE_DOES_NOT_EXIST);
//					}
//				}
//			}
//		} catch (IOException e) {
//			PlatformLogger.log(ContentErrorMessageConstants.CONTROLLER_ASSESSMENT_ITEM_JSON_OBJECT_CONVERSION_CASTING_ERROR,
//					e);
//		}
//		
//		PlatformLogger.log("Returning Map of File wise details about Creation of Assessment Item." + " | [Content Id '"+ contentId +"']");
//		return assessmentItemCreationFilewiseResult;
		return null;
	}

	/**
	 * Creates the Assessment Item Set Object into the Graph.
	 *
	 * @param assessmentItemIds
	 *            is the member assessment item identifiers.
	 * @param assessmentItemMap
	 *            the assessment item map form ECRF Object.
	 * @return the response is the object of Assessment Item Set Creation.
	 */
	@SuppressWarnings("unused")
	private Response createItemSet(List<String> assessmentItemIds, Map<String, Object> assessmentItemMap) {
//		PlatformLogger.log("Member Assessment Items: ", assessmentItemIds);
//		PlatformLogger.log("Assessment Item Map: ", assessmentItemMap);
//		
//		Response response = new Response();
//		if (null != assessmentItemIds) {
//			Map<String, Object> map = new HashMap<String, Object>();
//			PlatformLogger.log("Setting the Member IDs." + " | [Content Id '"+ contentId +"']");
//			map.put(ContentWorkflowPipelineParams.memberIds.name(), assessmentItemIds);
//			Integer totalItems = (Integer) assessmentItemMap.get(ContentWorkflowPipelineParams.total_items.name());
//			if (null == totalItems || totalItems > assessmentItemIds.size())
//				totalItems = assessmentItemIds.size();
//			PlatformLogger.log("Setting the Total Items Count: " + totalItems + " | [Content Id '"+ contentId +"']");
//			map.put(ContentWorkflowPipelineParams.total_items.name(), totalItems);
//			Integer maxScore = (Integer) assessmentItemMap.get(ContentWorkflowPipelineParams.max_score.name());
//			if (null == maxScore)
//				maxScore = totalItems;
//			PlatformLogger.log("Setting the Maximum Scores: " + maxScore + " | [Content Id '"+ contentId +"']");
//			map.put(ContentWorkflowPipelineParams.max_score.name(), maxScore);
//			String title = (String) assessmentItemMap.get(ContentWorkflowPipelineParams.title.name());
//			if (StringUtils.isNotBlank(title))
//				map.put(ContentWorkflowPipelineParams.title.name(), title);
//			PlatformLogger.log("Setting the Assessment Item Set Title: " + title + " | [Content Id '"+ contentId +"']");
//			map.put(ContentWorkflowPipelineParams.type.name(), QuestionnaireType.materialised.name());
//			String identifier = (String) assessmentItemMap.get(ContentWorkflowPipelineParams.identifier.name());
//			PlatformLogger.log("Setting the Identifier: " + identifier + " | [Content Id '"+ contentId +"']");
//			if (StringUtils.isNotBlank(identifier)) {
//				map.put(ContentWorkflowPipelineParams.code.name(), identifier);
//			} else {
//				map.put(ContentWorkflowPipelineParams.code.name(),
//						ContentWorkflowPipelineParams.item_set_.name() + RandomUtils.nextInt(1, 10000));
//			}
//			PlatformLogger.log("Creating Request Object. | [Content Id '"+ contentId +"']");
//			Request request = getAssessmentModelRequestObject(map, ContentWorkflowPipelineParams.ItemSet.name(),
//					contentId, ContentWorkflowPipelineParams.assessment_item_set.name());
//			if (null != request) {
//				PlatformLogger.log("Creating Assessment Item Object in Graph. | [Content Id '"+ contentId +"']");
////				response = assessmentMgr.createItemSet(ContentConfigurationConstants.GRAPH_ID, request);
//				PlatformLogger.log("Create Item | Response: " + response);
//			}
//		}
//		return response;
		return null;
	}

	/**
	 * Gets the assessment model request object.
	 *
	 * @param map
	 *            the map is the request body items.
	 * @param objectType
	 *            the object type is Item Set or Assessment item.
	 * @param contentId
	 *            the content id is the current content ID.
	 * @param param
	 *            the param the parameters for creation.
	 * @return the assessment model request object.
	 */
	@SuppressWarnings("unused")
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
				map.put(ContentWorkflowPipelineParams.name.name(),
						ContentWorkflowPipelineParams.AssessmentItemObject.name());
				map.put(ContentWorkflowPipelineParams.code.name(),
						ContentWorkflowPipelineParams.item_.name() + RandomUtils.nextInt(1, 10000));
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

	/**
	 * Gets the assessment mgr request object.
	 *
	 * @param requestMap
	 *            the request map
	 * @param param
	 *            the param
	 * @return the assessment mgr request object
	 */
	private Request getAssessmentMgrRequestObject(Map<String, Object> requestMap, String param) {
//		Request request = getRequest(requestMap);
//		Map<String, Object> map = request.getRequest();
//		if (null != map && !map.isEmpty()) {
//			try {
//				Object obj = map.get(param);
//				if (null != obj) {
//					Node item = (Node) mapper.convertValue(obj, Node.class);
//					request.put(param, item);
//					request.put(ContentWorkflowPipelineParams.skipValidations.name(), true);
//				}
//			} catch (Exception e) {
//				PlatformLogger.log(ContentErrorMessageConstants.ASSESSMENT_MANAGER_REQUEST_OBJECT_CREATION_ERROR, e);
//			}
//		}
//		return request;
		return null;
	}

	/**
	 * Gets the request.
	 *
	 * @param requestMap
	 *            the request map
	 * @return the request
	 */
	@SuppressWarnings({ "unused" })
	private Request getRequest(Map<String, Object> requestMap) {
//		Request request = new Request();
//		if (null != requestMap) {
//			request.setId((String) requestMap.get(ContentWorkflowPipelineParams.id.name()));
//			request.setVer((String) requestMap.get(ContentWorkflowPipelineParams.ver.name()));
//			request.setTs((String) requestMap.get(ContentWorkflowPipelineParams.ts.name()));
//			Object reqParams = requestMap.get(ContentWorkflowPipelineParams.params.name());
//			if (null != reqParams) {
//				try {
//					RequestParams params = (RequestParams) mapper.convertValue(reqParams, RequestParams.class);
//					request.setParams(params);
//				} catch (Exception e) {
//				}
//			}
//			Object requestObj = requestMap.get(ContentWorkflowPipelineParams.request.name());
//			if (null != requestObj) {
//				try {
//					String strRequest = mapper.writeValueAsString(requestObj);
//					Map<String, Object> map = mapper.readValue(strRequest, Map.class);
//					if (null != map && !map.isEmpty())
//						request.setRequest(map);
//				} catch (Exception e) {
//					PlatformLogger.log(ContentErrorMessageConstants.ASSESSMENT_MANAGER_REQUEST_OBJECT_CREATION_ERROR, e);
//				}
//			}
//		}
//		return request;
		return null;
	}

	/**
	 * Gets the code by object type.
	 *
	 * @param identifier
	 *            the identifier
	 * @param objectType
	 *            the object type
	 * @return the code by object type
	 */
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
