package org.ekstep.assessment.mgr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.assessment.dto.ItemSearchCriteria;
import org.ekstep.assessment.dto.ItemSetDTO;
import org.ekstep.assessment.dto.ItemSetSearchCriteria;
import org.ekstep.assessment.enums.AssessmentAPIParams;
import org.ekstep.assessment.enums.AssessmentErrorCodes;
import org.ekstep.assessment.store.AssessmentStore;
import org.ekstep.assessment.util.AssessmentValidator;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.NodeDTO;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.mgr.BaseManager;
import org.ekstep.graph.common.JSONUtils;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.enums.RelationTypes;
import org.ekstep.graph.dac.enums.SystemNodeTypes;
import org.ekstep.graph.dac.model.Filter;
import org.ekstep.graph.dac.model.MetadataCriterion;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.graph.dac.model.SearchConditions;
import org.ekstep.graph.dac.model.SearchCriteria;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.graph.exception.GraphEngineErrorCodes;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.graph.model.node.MetadataDefinition;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.taxonomy.mgr.impl.TaxonomyManagerImpl;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@Component
public class AssessmentManagerImpl extends BaseManager implements IAssessmentManager {

	private static final String ITEM_SET_OBJECT_TYPE = "ItemSet";
	private static final String ITEM_SET_MEMBERS_TYPE = "AssessmentItem";
	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private AssessmentValidator validator;

	@Autowired
	private AssessmentStore assessmentStore;

	long[] sum = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	@SuppressWarnings("unchecked")
	@Override
	public Response createAssessmentItem(String taxonomyId, Request request) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank");
		Node item = null;
		try {
			item = (Node) request.get(AssessmentAPIParams.assessment_item.name());
		} catch (Exception e) {
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_INVALID_REQUEST_FORMAT.name(),
					"Invalid request format");
		}
		if (null == item)
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_ITEM.name(),
					"AssessmentItem Object is blank");
		Boolean skipValidation = (Boolean) request.get(ContentAPIParams.skipValidations.name());
		if (null == skipValidation)
			skipValidation = false;

		String framework = (String) item.getMetadata().get(ContentAPIParams.framework.name());
		if (StringUtils.isBlank(framework))
			item.getMetadata().put("framework", getDefaultFramework());
		
		if(item.getMetadata().containsKey("level"))
			item.getMetadata().remove("level");

		Object version = item.getMetadata().get(ContentAPIParams.version.name());
		if (null == version)
			item.getMetadata().put(ContentAPIParams.version.name(), 1);

		Response validateRes = new Response();
		List<String> assessmentErrors = new ArrayList<String>();
		Map<String, Object> externalProps = handleExternalProperties(item.getMetadata());
		if (!skipValidation) {
			Request validateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "validateNode");
			validateReq.put(GraphDACParams.node.name(), item);
			validateRes = getResponse(validateReq);
			assessmentErrors = validator.validateAssessmentItem(item);

			if (checkError(validateRes)) {
				List<String> messages = (List<String>) validateRes.get(GraphDACParams.messages.name());
				messages.addAll(assessmentErrors);
				return validateRes;
			}

			if (assessmentErrors.size() > 0) {
				return ERROR(GraphEngineErrorCodes.ERR_GRAPH_NODE_VALIDATION_FAILED.name(),
						"AssessmentItem validation failed", ResponseCode.CLIENT_ERROR, GraphDACParams.messages.name(),
						assessmentErrors);
			}
		}
		replaceMediaItemsWithVariants(taxonomyId, item);
		Request createReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "createDataNode");
		createReq.put(GraphDACParams.node.name(), item);
		createReq.put(GraphDACParams.skip_validations.name(), skipValidation);
		Response createRes = getResponse(createReq);
		if (checkError(createRes)) {
			return createRes;
		} else {
			String contentId = (String) createRes.get(GraphDACParams.node_id.name());
			if (MapUtils.isNotEmpty(externalProps)) {
				assessmentStore.updateAssessmentProperties(contentId, externalProps);
			}
			return createRes;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response updateAssessmentItem(String id, String taxonomyId, Request request) {
		Node item = null;
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank");
		if (StringUtils.isBlank(id))
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_ITEM_ID.name(),
					"AssessmentItem Id is blank");
		try {
			item = (Node) request.get(AssessmentAPIParams.assessment_item.name());
		} catch (Exception e) {
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_INVALID_REQUEST_FORMAT.name(),
					"Invalid request format");
		}
		if (null == item)
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_ITEM.name(),
					"AssessmentItem Object is blank");

		Response getNodeResponse = getDataNode(taxonomyId, id);
		if (checkError(getNodeResponse)) {
			TelemetryManager.log("AssessmentItem not found: " + id);
			return getNodeResponse;
		}

		if (StringUtils.isBlank((String) item.getMetadata().get("framework"))) {
			Node graphNode = (Node) getNodeResponse.get(GraphDACParams.node.name());
			if (StringUtils.isNotBlank((String) graphNode.getMetadata().get("framework")))
				item.getMetadata().put("framework", (String) graphNode.getMetadata().get("framework"));
			else
				item.getMetadata().put("framework", getDefaultFramework());
		}
		Map<String, Object> externalProps = handleExternalProperties(item.getMetadata());
		
		if(item.getMetadata().containsKey("level"))
			item.getMetadata().remove("level");
		
		Boolean skipValidation = (Boolean) request.get(ContentAPIParams.skipValidations.name());
		if (null == skipValidation)
			skipValidation = false;
		Response validateRes = new Response();
		List<String> assessmentErrors = new ArrayList<String>();
		if (!skipValidation) {
			Request validateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "validateNode");
			validateReq.put(GraphDACParams.node.name(), item);
			validateRes = getResponse(validateReq);
			assessmentErrors = validator.validateAssessmentItem(item);
		}
		if (checkError(validateRes) && !skipValidation) {
			if (assessmentErrors.size() > 0) {
				List<String> messages = (List<String>) validateRes.get(GraphDACParams.messages.name());
				messages.addAll(assessmentErrors);
			}
			return validateRes;
		} else {
			if (assessmentErrors.size() > 0 && !skipValidation) {
				return ERROR(GraphEngineErrorCodes.ERR_GRAPH_NODE_VALIDATION_FAILED.name(), "Node validation failed",
						ResponseCode.CLIENT_ERROR, GraphDACParams.messages.name(), assessmentErrors);
			} else {
				if (null == item.getIdentifier())
					item.setIdentifier(id);
				replaceMediaItemsWithVariants(taxonomyId, item);
				Request updateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
				updateReq.put(GraphDACParams.node.name(), item);
				updateReq.put(GraphDACParams.node_id.name(), item.getIdentifier());
				updateReq.put(GraphDACParams.skip_validations.name(), skipValidation);
				Response updateRes = getResponse(updateReq);
				if (checkError(updateRes)) {
					return updateRes;
				} else {
					String contentId = (String) updateRes.get(GraphDACParams.node_id.name());
					if (MapUtils.isNotEmpty(externalProps)) {
						assessmentStore.updateAssessmentProperties(contentId, externalProps);
					}
				}
				return updateRes;
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response searchAssessmentItems(String taxonomyId, Request request) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank");
		ItemSearchCriteria criteria = (ItemSearchCriteria) request
				.get(AssessmentAPIParams.assessment_search_criteria.name());

		if (null == criteria)
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_CRITERIA.name(),
					"AssessmentItem Search Criteria Object is blank");
		/*
		 * List<String> assessmentErrors =
		 * validator.validateAssessmentItemSet(request); if
		 * (assessmentErrors.size() > 0) throw new
		 * ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_CRITERIA.
		 * name(), "property can not be empty string");
		 */
		Request req = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
				GraphDACParams.search_criteria.name(), criteria.getSearchCriteria());
		Response response = getResponse(req);
		Response listRes = copyResponse(response);
		if (checkError(response)) {
			return response;
		} else {
			List<Node> nodes = (List<Node>) response.get(GraphDACParams.node_list.name());
			List<Map<String, Object>> searchItems = new ArrayList<Map<String, Object>>();
			if (null != nodes && nodes.size() > 0) {
				DefinitionDTO definition = getDefinition(taxonomyId, ITEM_SET_MEMBERS_TYPE);
				List<String> jsonProps = getJSONProperties(definition);
				for (Node node : nodes) {
					Map<String, Object> dto = getAssessmentItem(node, jsonProps, null);
					searchItems.add(dto);
				}
			}
			listRes.put(AssessmentAPIParams.assessment_items.name(), searchItems);
			return listRes;
		}
	}

	@Override
	public Response deleteAssessmentItem(String id, String taxonomyId) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank");
		if (StringUtils.isBlank(id))
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_ITEM_ID.name(),
					"AssessmentItem Id is blank");
		Request request = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "deleteDataNode",
				GraphDACParams.node_id.name(), id);
		return getResponse(request);
	}

	@Override
	public Response getAssessmentItem(String id, String taxonomyId, String[] ifields, String[] fields) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank");
		if (StringUtils.isBlank(id))
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_ITEM_ID.name(),
					"AssessmentItem Id is blank");
		Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
				GraphDACParams.node_id.name(), id);
		request.put(GraphDACParams.get_tags.name(), true);
		Response getNodeRes = getResponse(request);
		Response response = copyResponse(getNodeRes);
		if (checkError(response)) {
			return response;
		}
		Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
		String questionId = node.getIdentifier();
		List<String> externalProps = getItemExternalPropsList();
		Set<String> externalPropsFromRequest = null;
		Set<String> allFields = getAllFields(ifields, fields);
		if (CollectionUtils.isNotEmpty(allFields))
			externalPropsFromRequest = allFields.stream()
					.filter(prop -> externalProps.contains(prop))
					.collect(Collectors.toSet());
		Map<String, Object> externalPropMap;
		if(CollectionUtils.isNotEmpty(externalPropsFromRequest))
			externalPropMap= assessmentStore.getAssessmentProperties(questionId, Arrays.asList(externalPropsFromRequest.toArray()));
		else
			externalPropMap= assessmentStore.getAssessmentProperties(questionId, externalProps);
		if (null != node) {
			DefinitionDTO definition = getDefinition(taxonomyId, ITEM_SET_MEMBERS_TYPE);
			List<String> jsonProps = getJSONProperties(definition);
			Map<String, Object> dto = getAssessmentItem(node, jsonProps, new ArrayList<>(allFields));
			if(MapUtils.isNotEmpty(externalPropMap))
				dto.putAll(externalPropMap);
			response.put(AssessmentAPIParams.assessment_item.name(), dto);
		}
		return response;
	}

	private Set<String> getAllFields(String[] fields, String[] ifields) {
		Set<String> allFields = new HashSet<>();
		if(null != ifields)
			allFields.addAll(Arrays.asList(ifields));
		if(null != fields)
			allFields.addAll(Arrays.asList(fields));
		return allFields;
	}

	private List<String> getJSONProperties(DefinitionDTO definition) {
		List<String> props = new ArrayList<String>();
		if (null != definition && null != definition.getProperties()) {
			for (MetadataDefinition mDef : definition.getProperties()) {
				if (StringUtils.equalsIgnoreCase("json", mDef.getDataType())) {
					props.add(mDef.getPropertyName());
				}
			}
		}
		return props;
	}

	private Map<String, Object> getAssessmentItem(Node node, List<String> jsonProps, List<String> allFields) {
		Map<String, Object> metadata = new HashMap<String, Object>();
		metadata.put("subject", node.getGraphId());
		Map<String, Object> nodeMetadata = node.getMetadata();
		if (null != nodeMetadata && !nodeMetadata.isEmpty()) {
			if (CollectionUtils.isNotEmpty(allFields)) {
				for (Entry<String, Object> entry : nodeMetadata.entrySet()) {
					if (null != entry.getValue()) {
						if (allFields.contains(entry.getKey())) {
							if (jsonProps.contains(entry.getKey())) {
								Object val = JSONUtils.convertJSONString((String) entry.getValue());
								if (null != val)
									metadata.put(entry.getKey(), val);
							} else {
								metadata.put(entry.getKey(), entry.getValue());
							}
						}
					}
				}
			} else {
				for (Entry<String, Object> entry : nodeMetadata.entrySet()) {
					if (null != entry.getValue()) {
						if (jsonProps.contains(entry.getKey())) {
							Object val = JSONUtils.convertJSONString((String) entry.getValue());
							if (null != val)
								metadata.put(entry.getKey(), val);
						} else {
							metadata.put(entry.getKey(), entry.getValue());
						}
					}
				}
			}
		}
		if (null != node.getTags() && !node.getTags().isEmpty()) {
			metadata.put("tags", node.getTags());
		}
		if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
			List<NodeDTO> concepts = new ArrayList<NodeDTO>();
			for (Relation rel : node.getOutRelations()) {
				if (StringUtils.equals(RelationTypes.ASSOCIATED_TO.relationName(), rel.getRelationType())
						&& StringUtils.equalsIgnoreCase(SystemNodeTypes.DATA_NODE.name(), rel.getEndNodeType())) {
					if (StringUtils.equalsIgnoreCase("Concept", rel.getEndNodeObjectType())) {
						concepts.add(new NodeDTO(rel.getEndNodeId(), rel.getEndNodeName(), rel.getEndNodeObjectType(),
								rel.getRelationType()));
					}
				}
			}
			if (null != concepts && !concepts.isEmpty())
				metadata.put("concepts", concepts);
		}
		metadata.put("identifier", node.getIdentifier());
		return metadata;
	}

	@SuppressWarnings("unchecked")
	private List<String> getSetMembers(String taxonomyId, String setId) {
		Request setReq = getRequest(taxonomyId, GraphEngineManagers.COLLECTION_MANAGER, "getCollectionMembers");
		setReq.put(GraphDACParams.collection_type.name(), SystemNodeTypes.SET.name());
		setReq.put(GraphDACParams.collection_id.name(), setId);
		Response setRes = getResponse(setReq);
		List<String> members = (List<String>) setRes.get(GraphDACParams.members.name());
		return members;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response createItemSet(String taxonomyId, Request request) {
		Node node = null;
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank");
		try {
			node = (Node) request.get(AssessmentAPIParams.assessment_item_set.name());
		} catch (Exception e) {
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_INVALID_REQUEST_FORMAT.name(),
					"Invalid request format");
		}
		if (null == node)
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_ITEM.name(),
					"AssessmentItemSet Object is blank");
		Boolean skipValidation = (Boolean) request.get(ContentAPIParams.skipValidations.name());
		if (null == skipValidation)
			skipValidation = false;
		Response validateRes = new Response();
		List<String> assessmentErrors = new ArrayList<String>();
		if (!skipValidation) {
			Request validateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "validateNode");
			validateReq.put(GraphDACParams.node.name(), node);
			validateRes = getResponse(validateReq);
			assessmentErrors = validator.validateAssessmentItemSet(node);
		}
		if (checkError(validateRes) && !skipValidation) {
			if (assessmentErrors.size() > 0) {
				List<String> messages = (List<String>) validateRes.get(GraphDACParams.messages.name());
				messages.addAll(assessmentErrors);
			}
			return validateRes;
		} else {
			if (assessmentErrors.size() > 0 && !skipValidation) {
				return ERROR(GraphEngineErrorCodes.ERR_GRAPH_NODE_VALIDATION_FAILED.name(),
						"AssessmentItemSet validation failed", ResponseCode.CLIENT_ERROR,
						GraphDACParams.messages.name(), assessmentErrors);
			} else {
				Request setReq = getRequest(taxonomyId, GraphEngineManagers.COLLECTION_MANAGER, "createSet");
				setReq.put(GraphDACParams.criteria.name(), getItemSetCriteria(node));
				setReq.put(GraphDACParams.members.name(), getMemberIds(node));
				setReq.put(GraphDACParams.node.name(), node);
				setReq.put(GraphDACParams.object_type.name(), ITEM_SET_OBJECT_TYPE);
				setReq.put(GraphDACParams.member_type.name(), ITEM_SET_MEMBERS_TYPE);
				return getResponse(setReq);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Response updateItemSet(String id, String taxonomyId, Request request) {
		Node node = null;
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank");
		if (StringUtils.isBlank(id))
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_ITEM_SET_ID.name(),
					"AssessmentItemSet Id is blank");
		try {
			node = (Node) request.get(AssessmentAPIParams.assessment_item_set.name());
		} catch (Exception e) {
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_INVALID_REQUEST_FORMAT.name(),
					"Invalid request format");
		}
		if (null == node)
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_ITEM.name(),
					"AssessmentItemSet Object is blank");
		Request validateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "validateNode");
		validateReq.put(GraphDACParams.node.name(), node);
		Response validateRes = getResponse(validateReq);
		List<String> assessmentErrors = validator.validateAssessmentItemSet(node);
		if (checkError(validateRes)) {
			if (assessmentErrors.size() > 0) {
				List<String> messages = (List<String>) validateRes.get(GraphDACParams.messages.name());
				messages.addAll(assessmentErrors);
			}
			return validateRes;
		} else {
			if (assessmentErrors.size() > 0) {
				return ERROR(GraphEngineErrorCodes.ERR_GRAPH_NODE_VALIDATION_FAILED.name(),
						"AssessmentItemSet validation failed", ResponseCode.CLIENT_ERROR,
						GraphDACParams.messages.name(), assessmentErrors);
			} else {
				node.setIdentifier(id);
				Request setReq = getRequest(taxonomyId, GraphEngineManagers.COLLECTION_MANAGER, "updateSet");
				setReq.put(GraphDACParams.criteria.name(), getItemSetCriteria(node));
				setReq.put(GraphDACParams.members.name(), getMemberIds(node));
				setReq.put(GraphDACParams.node.name(), node);
				setReq.put(GraphDACParams.object_type.name(), ITEM_SET_OBJECT_TYPE);
				setReq.put(GraphDACParams.member_type.name(), ITEM_SET_MEMBERS_TYPE);
				return getResponse(setReq);
			}
		}
	}

	private String MEMBER_IDS_KEY = "memberIds";
	private String CRITERIA_KEY = "criteria";

	private SearchCriteria getItemSetCriteria(Node node) {
		if (null != node) {
			try {
				String strCriteria = (String) node.getMetadata().get(CRITERIA_KEY);
				if (StringUtils.isNotBlank(strCriteria)) {
					ItemSearchCriteria itemSearchCriteria = mapper.readValue(strCriteria, ItemSearchCriteria.class);
					return itemSearchCriteria.getSearchCriteria();
				}
			} catch (Exception e) {
				throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_INVALID_SEARCH_CRITERIA.name(),
						"Criteria given to create ItemSet is invalid.");
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private List<String> getMemberIds(Node node) {
		if (null != node && null != node.getMetadata()) {
			Object obj = node.getMetadata().get(MEMBER_IDS_KEY);
			if (null != obj) {
				node.getMetadata().remove(MEMBER_IDS_KEY);
				List<String> memberIds = mapper.convertValue(obj, List.class);
				return memberIds;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response getItemSet(String id, String taxonomyId, String[] isfields, boolean expandItems) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank");
		if (StringUtils.isBlank(id))
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_ITEM_SET_ID.name(),
					"ItemSet Id is blank");
		Request request = getRequest(taxonomyId, GraphEngineManagers.COLLECTION_MANAGER, "getSet",
				GraphDACParams.collection_id.name(), id);
		Response getNodeRes = getResponse(request);
		Response response = copyResponse(getNodeRes);
		if (checkError(response)) {
			return response;
		}
		Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
		if (null != node) {
			DefinitionDTO definition = getDefinition(taxonomyId, ITEM_SET_OBJECT_TYPE);
			List<String> jsonProps = getJSONProperties(definition);
			List<String> items = getSetMembers(taxonomyId, id);
			ItemSetDTO dto = new ItemSetDTO(node, items, isfields, jsonProps);
			Map<String, Object> itemSetMap = dto.returnMap();
			if (expandItems) {
				itemSetMap.remove("items");
				if (null != items && !items.isEmpty()) {
					Response searchRes = searchItems(taxonomyId, items);
					if (checkError(searchRes)) {
						return response;
					} else {
						DefinitionDTO itemDefinition = getDefinition(taxonomyId, ITEM_SET_MEMBERS_TYPE);
						List<String> itemJsonProps = getJSONProperties(itemDefinition);
						List<Object> list = (List<Object>) searchRes.get(AssessmentAPIParams.assessment_items.name());
						List<Map<String, Object>> itemMaps = new ArrayList<Map<String, Object>>();
						if (null != list && !list.isEmpty()) {
							for (Object obj : list) {
								List<Node> nodeList = (List<Node>) obj;
								for (Node itemNode : nodeList) {
									Map<String, Object> itemDto = getAssessmentItem(itemNode, itemJsonProps, null);
									itemMaps.add(itemDto);
								}
							}
						}
						Integer total = null;
						if (itemSetMap.get("total_items") instanceof Long)
							total = Integer.valueOf(((Long) itemSetMap.get("total_items")).intValue());
						else
							total = (Integer) itemSetMap.get("total_items");
						if (null == total) {
							total = itemMaps.size();
							itemSetMap.put("total_items", total);
						}
						Map<String, Object> itemSetCountMap = new HashMap<String, Object>();
						itemSetCountMap.put("id", node.getIdentifier());
						itemSetCountMap.put("count", total);
						List<Map<String, Object>> itemSetCountMaps = new ArrayList<Map<String, Object>>();
						itemSetCountMaps.add(itemSetCountMap);
						itemSetMap.put("item_sets", itemSetCountMaps);
						Map<String, Object> itemMap = new HashMap<String, Object>();
						itemMap.put(node.getIdentifier(), itemMaps);
						itemSetMap.put("items", itemMap);
					}
				}
			}
			response.put(AssessmentAPIParams.assessment_item_set.name(), itemSetMap);
		}
		return response;
	}

	private Response searchItems(String taxonomyId, List<String> itemIds) {
		SearchCriteria criteria = new SearchCriteria();
		List<Filter> filters = new ArrayList<Filter>();
		Filter filter = new Filter("identifier", SearchConditions.OP_IN, itemIds);
		filters.add(filter);
		MetadataCriterion metadata = MetadataCriterion.create(filters);
		criteria.addMetadata(metadata);
		List<Request> requests = new ArrayList<Request>();
		if (StringUtils.isNotBlank(taxonomyId)) {
			Request req = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
					GraphDACParams.search_criteria.name(), criteria);
			requests.add(req);
		} else {
			for (String tId : TaxonomyManagerImpl.taxonomyIds) {
				Request req = getRequest(tId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
						GraphDACParams.search_criteria.name(), criteria);
				req.put(GraphDACParams.get_tags.name(), true);
				requests.add(req);
			}
		}
		Response response = getResponse(requests, GraphDACParams.node_list.name(),
				AssessmentAPIParams.assessment_items.name());
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response searchItemSets(String taxonomyId, Request request) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank");
		ItemSetSearchCriteria criteria = (ItemSetSearchCriteria) request
				.get(AssessmentAPIParams.assessment_search_criteria.name());

		if (null == criteria)
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_CRITERIA.name(),
					"ItemSet Search Criteria Object is blank");
		Request req = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
				GraphDACParams.search_criteria.name(), criteria.getSearchCriteria());
		Response response = getResponse(req);
		Response listRes = copyResponse(response);
		if (checkError(response)) {
			return response;
		} else {
			List<Node> nodes = (List<Node>) response.get(GraphDACParams.node_list.name());
			List<Map<String, Object>> searchItems = new ArrayList<Map<String, Object>>();
			if (null != nodes && nodes.size() > 0) {
				DefinitionDTO definition = getDefinition(taxonomyId, ITEM_SET_OBJECT_TYPE);
				List<String> jsonProps = getJSONProperties(definition);
				for (Node node : nodes) {
					List<String> items = getSetMembers(taxonomyId, node.getIdentifier());
					ItemSetDTO dto = new ItemSetDTO(node, items, null, jsonProps);
					searchItems.add(dto.returnMap());
				}
			}
			listRes.put(AssessmentAPIParams.assessment_item_sets.name(), searchItems);
			return listRes;
		}
	}

	@Override
	public Response deleteItemSet(String id, String taxonomyId) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank");
		if (StringUtils.isBlank(id))
			throw new ClientException(AssessmentErrorCodes.ERR_ASSESSMENT_BLANK_ITEM_ID.name(),
					"AssessmentItem Id is blank");
		Request request = getRequest(taxonomyId, GraphEngineManagers.COLLECTION_MANAGER, "dropCollection",
				GraphDACParams.collection_id.name(), id);
		request.put(GraphDACParams.collection_type.name(), SystemNodeTypes.SET.name());
		return getResponse(request);
	}

	private void replaceMediaItemsWithVariants(String graphId, Node item) {
		String media = (String) item.getMetadata().get(AssessmentAPIParams.media.name());
		boolean replaced = false;
		try {
			if (StringUtils.isNotBlank(media)) {
				TypeReference<List<Map<String, Object>>> typeRef = new TypeReference<List<Map<String, Object>>>() {
				};
				List<Map<String, String>> mediaMap = mapper.readValue(media, typeRef);
				if (mediaMap != null && mediaMap.size() > 0) {
					DefinitionDTO definition = getDefinition(graphId, ITEM_SET_MEMBERS_TYPE);
					String resolution = "low";
					if (null != definition && null != definition.getMetadata() && !definition.getMetadata().isEmpty()) {
						Object defaultRes = definition.getMetadata().get("defaultRes");
						if (null != defaultRes && StringUtils.isNotBlank(defaultRes.toString()))
							resolution = defaultRes.toString();
					}
					for (Map<String, String> mediaItem : mediaMap) {
						String asset_id = (String) mediaItem.get(ContentAPIParams.asset_id.name());
						if (StringUtils.isBlank(asset_id))
							asset_id = (String) mediaItem.get(ContentAPIParams.assetId.name());
						if (StringUtils.isNotBlank(asset_id)) {
							Node asset = getNode(graphId, asset_id);
							if (null != asset && null != asset.getMetadata().get(ContentAPIParams.variants.name())) {
								String variantsJSON = (String) asset.getMetadata()
										.get(ContentAPIParams.variants.name());
								Map<String, String> variants = mapper.readValue(variantsJSON,
										new TypeReference<Map<String, String>>() {
										});
								if (variants != null && variants.size() > 0) {
									String lowVariantURL = variants.get(resolution);
									if (StringUtils.isNotEmpty(lowVariantURL)) {
										replaced = true;
										mediaItem.put(ContentAPIParams.src.name(), lowVariantURL);
									}
								}
							}
						}
					}
				}
				if (replaced) {
					String updatedMedia = mapper.writeValueAsString(mediaMap);
					item.getMetadata().put(AssessmentAPIParams.media.name(), updatedMedia);
				}
			}
		} catch (Exception e) {
			TelemetryManager.error(
					"error in replaceMediaItemsWithLowVariants while checking media for replacing with low variants, message= "
							+ e.getMessage(),
					e);
			e.printStackTrace();
		}
	}

	/**
	 * Gets the node.
	 *
	 * @param taxonomyId
	 *            the taxonomy id
	 * @param contentId
	 *            the content id
	 * @return the node
	 */
	private Node getNode(String taxonomyId, String contentId) {
		Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
				GraphDACParams.node_id.name(), contentId);
		request.put(GraphDACParams.get_tags.name(), true);
		Response getNodeRes = getResponse(request);
		Response response = copyResponse(getNodeRes);
		if (checkError(response)) {
			return null;
		}
		Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
		return node;
	}

	private String getDefaultFramework() {
		if (Platform.config.hasPath("platform.framework.default"))
			return Platform.config.getString("platform.framework.default");
		else
			return "NCF";
	}

	/**
	 * Get external properties from Item Definition
	 * (external properties won't be stored in neo4j but some external db)
	 * @return
	 */
	protected List<String> getItemExternalPropsList() {
		DefinitionDTO definition = getDefinition(TAXONOMY_ID, "AssessmentItem");
		List<MetadataDefinition> props = definition.getProperties();
		List<String> externalProperties = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(props))
			externalProperties = props.stream().filter(prop -> equalsIgnoreCase("external", prop.getDataType()))
					.map(prop -> prop.getPropertyName().trim()).collect(Collectors.toList());
		return externalProperties;
	}

	protected DefinitionDTO getDefinition(String graphId, String objectType) {
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
				GraphDACParams.object_type.name(), objectType);
		Response response = getResponse(request);
		if (!checkError(response)) {
			DefinitionDTO definition = (DefinitionDTO) response.get(GraphDACParams.definition_node.name());
			return definition;
		}
		return null;
	}

	protected Map<String,Object> handleExternalProperties(Map<String, Object> metadata) {
		List<String> externalPropsList = getItemExternalPropsList();
		Map<String, Object> externalProps = externalPropsList.stream().filter(prop -> null != metadata.get(prop))
				.collect(Collectors.toMap(prop -> prop , prop -> metadata.get(prop)));
		metadata.keySet().removeAll(externalPropsList);
		return externalProps;
	}

}