package com.ilimi.taxonomy.mgr.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.optimizr.Optimizr;
import org.ekstep.common.util.AWSUploader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.NodeDTO;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.RequestParams;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.common.util.LogTelemetryEventUtil;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.exception.GraphDACErrorCodes;
import com.ilimi.graph.dac.model.Filter;
import com.ilimi.graph.dac.model.MetadataCriterion;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.Sort;
import com.ilimi.graph.dac.model.TagCriterion;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.DefinitionDTO;
import com.ilimi.taxonomy.content.ContentMimeTypeFactory;
import com.ilimi.taxonomy.dto.ContentDTO;
import com.ilimi.taxonomy.dto.ContentSearchCriteria;
import com.ilimi.taxonomy.enums.ContentAPIParams;
import com.ilimi.taxonomy.enums.ContentErrorCodes;
import com.ilimi.taxonomy.mgr.IContentManager;
import com.ilimi.taxonomy.mgr.IMimeTypeManager;
import com.ilimi.taxonomy.util.ContentBundle;

@Component
public class ContentManagerImpl extends BaseManager implements IContentManager {

	@Autowired
	private ContentBundle contentBundle;

	@Autowired
	private ContentMimeTypeFactory contentFactory;

	private static Logger LOGGER = LogManager.getLogger(IContentManager.class.getName());
	
	private static final List<String> DEFAULT_FIELDS = new ArrayList<String>();
	private static final List<String> DEFAULT_STATUS = new ArrayList<String>();

	private ObjectMapper mapper = new ObjectMapper();

	static {
		DEFAULT_FIELDS.add("identifier");
		DEFAULT_FIELDS.add("name");
		DEFAULT_FIELDS.add("description");
		DEFAULT_FIELDS.add(SystemProperties.IL_UNIQUE_ID.name());

		DEFAULT_STATUS.add("Live");
	}

	private static final String bucketName = "ekstep-public";
	private static final String folderName = "content";
	private static final String ecarFolderName = "ecar_files";
	private static final String tempFileLocation = "/data/contentBundle/";

	protected static final String URL_FIELD = "URL";

	@Override
	public Response create(String taxonomyId, String objectType, Request request) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank");
		if (StringUtils.isBlank(objectType))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
					"ObjectType is blank");
		Node item = (Node) request.get(ContentAPIParams.content.name());
		if (null == item)
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT.name(), objectType
					+ " Object is blank");
		item.setObjectType(objectType);
		Request validateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER,
				"validateNode");
		validateReq.put(GraphDACParams.node.name(), item);
		Response validateRes = getResponse(validateReq, LOGGER);
		if (checkError(validateRes)) {
			return validateRes;
		} else {
			Request createReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER,
					"createDataNode");
			createReq.put(GraphDACParams.node.name(), item);
			Response createRes = getResponse(createReq, LOGGER);
			return createRes;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response findAll(String taxonomyId, String objectType, Integer offset, Integer limit,
			String[] gfields) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank");
		if (StringUtils.isBlank(objectType))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_INVALID_OBJECT_TYPE.name(),
					"Object Type is blank");
		LOGGER.info("Find All Content : " + taxonomyId + ", ObjectType: " + objectType);
		SearchCriteria sc = new SearchCriteria();
		sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
		sc.setObjectType(objectType);
		sc.sort(new Sort(PARAM_STATUS, Sort.SORT_ASC));
		if (null != offset && offset.intValue() >= 0)
			sc.setStartPosition(offset);
		if (null != limit && limit.intValue() > 0)
			sc.setResultSize(limit);
		Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
				GraphDACParams.search_criteria.name(), sc);
		request.put(GraphDACParams.get_tags.name(), true);
		Response findRes = getResponse(request, LOGGER);
		Response response = copyResponse(findRes);
		if (checkError(response))
			return response;
		else {
			List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
			if (null != nodes && !nodes.isEmpty()) {
				if (null != gfields && gfields.length > 0) {
					for (Node node : nodes) {
						setMetadataFields(node, gfields);
					}
				}
				response.put(ContentAPIParams.contents.name(), nodes);
			}
			Request countReq = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER,
					"getNodesCount", GraphDACParams.search_criteria.name(), sc);
			Response countRes = getResponse(countReq, LOGGER);
			if (checkError(countRes)) {
				return countRes;
			} else {
				Long count = (Long) countRes.get(GraphDACParams.count.name());
				response.put(GraphDACParams.count.name(), count);
			}
		}
		return response;
	}

	@Override
	public Response find(String id, String taxonomyId, String[] fields) {
		if (StringUtils.isBlank(id))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
					"Content Object Id is blank");
		if (StringUtils.isNotBlank(taxonomyId)) {
			return getDataNode(taxonomyId, id);
		} else {
			for (String tid : TaxonomyManagerImpl.taxonomyIds) {
				Response response = getDataNode(tid, id);
				if (!checkError(response)) {
					return response;
				}
			}
		}
		Response response = new Response();
		ResponseParams params = new ResponseParams();
		params.setErr(GraphDACErrorCodes.ERR_GRAPH_NODE_NOT_FOUND.name());
		params.setStatus(StatusType.failed.name());
		params.setErrmsg("Content not found");
		response.setParams(params);
		response.setResponseCode(ResponseCode.RESOURCE_NOT_FOUND);
		return response;
	}

	private Response getDataNode(String taxonomyId, String id) {
		Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
				GraphDACParams.node_id.name(), id);
		request.put(GraphDACParams.get_tags.name(), true);
		Response getNodeRes = getResponse(request, LOGGER);
		return getNodeRes;
	}

	@Override
	public Response update(String id, String taxonomyId, String objectType, Request request) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank");
		if (StringUtils.isBlank(id))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
					"Content Object Id is blank");
		if (StringUtils.isBlank(objectType))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
					"ObjectType is blank");
		Node item = (Node) request.get(ContentAPIParams.content.name());
		if (null == item)
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT.name(), objectType
					+ " Object is blank");
		item.setIdentifier(id);
		item.setObjectType(objectType);
		Request validateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER,
				"validateNode");
		validateReq.put(GraphDACParams.node.name(), item);
		Response validateRes = getResponse(validateReq, LOGGER);
		if (checkError(validateRes)) {
			return validateRes;
		} else {
			Request updateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER,
					"updateDataNode");
			updateReq.put(GraphDACParams.node.name(), item);
			updateReq.put(GraphDACParams.node_id.name(), item.getIdentifier());
			Response updateRes = getResponse(updateReq, LOGGER);
			return updateRes;
		}
	}

	@Override
	public Response delete(String id, String taxonomyId) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank");
		if (StringUtils.isBlank(id))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
					"Content Object Id is blank");
		Request request = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER,
				"deleteDataNode", GraphDACParams.node_id.name(), id);
		return getResponse(request, LOGGER);
	}

	@Override
	public Response upload(String id, String taxonomyId, File uploadedFile, String folder) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank.");
		if (StringUtils.isBlank(id))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
					"Content Object Id is blank.");
		if (null == uploadedFile) {
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_UPLOAD_OBJECT.name(),
					"Upload file is blank.");
		}
		Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
				GraphDACParams.node_id.name(), id);
		request.put(GraphDACParams.get_tags.name(), true);
		Response getNodeRes = getResponse(request, LOGGER);
		Response response = copyResponse(getNodeRes);
		if (checkError(response)) {
			return response;
		}
		Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
		String mimeType = (String) node.getMetadata().get("mimeType");
		if (StringUtils.isBlank(mimeType)) {
			mimeType = "assets";
		}
		IMimeTypeManager mimeTypeManager = contentFactory.getImplForService(mimeType);
		Response res = mimeTypeManager.upload(node,uploadedFile,folder);
		if (null != uploadedFile && uploadedFile.exists()) {
			try {
				uploadedFile.delete();
			} catch (Exception e) {
			}
		}
		return res;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response listContents(String taxonomyId, String objectType, Request request) {
		if (StringUtils.isBlank(taxonomyId))
			taxonomyId = (String) request.get(PARAM_SUBJECT);
		LOGGER.info("List Contents : " + taxonomyId);
		Map<String, DefinitionDTO> definitions = new HashMap<String, DefinitionDTO>();
		List<Request> requests = new ArrayList<Request>();
		if (StringUtils.isNotBlank(taxonomyId)) {
			DefinitionDTO definition = getDefinition(taxonomyId, objectType);
			definitions.put(taxonomyId, definition);
			Request req = getContentsListRequest(request, taxonomyId, objectType, definition);
			requests.add(req);
		} else {
			DefinitionDTO definition = getDefinition(TaxonomyManagerImpl.taxonomyIds[0], objectType);
			// TODO: need to get definition from the specific taxonomy.
			for (String id : TaxonomyManagerImpl.taxonomyIds) {
				definitions.put(id, definition);
				Request req = getContentsListRequest(request, id, objectType, definition);
				requests.add(req);
			}
		}
		Response response = getResponse(requests, LOGGER, GraphDACParams.node_list.name(),
				ContentAPIParams.contents.name());
		Response listRes = copyResponse(response);
		if (checkError(response))
			return response;
		else {
			List<List<Node>> nodes = (List<List<Node>>) response.get(ContentAPIParams.contents
					.name());
			List<Map<String, Object>> contents = new ArrayList<Map<String, Object>>();
			if (null != nodes && !nodes.isEmpty()) {
				Object objFields = request.get(PARAM_FIELDS);
				List<String> fields = getList(mapper, objFields, true);
				for (List<Node> list : nodes) {
					if (null != list && !list.isEmpty()) {
						for (Node node : list) {
							if (null == fields || fields.isEmpty()) {
								String subject = node.getGraphId();
								if (null != definitions.get(subject)
										&& null != definitions.get(subject).getMetadata()) {
									String[] arr = (String[]) definitions.get(subject)
											.getMetadata().get(PARAM_FIELDS);
									if (null != arr && arr.length > 0) {
										fields = Arrays.asList(arr);
									}
								}
							}
							ContentDTO content = new ContentDTO(node, fields);
							contents.add(content.returnMap());
						}
					}
				}
			}
			String returnKey = ContentAPIParams.content.name();
			listRes.put(returnKey, contents);
			Integer ttl = null;
			if (null != definitions.get(TaxonomyManagerImpl.taxonomyIds[0])
					&& null != definitions.get(TaxonomyManagerImpl.taxonomyIds[0]).getMetadata())
				ttl = (Integer) definitions.get(TaxonomyManagerImpl.taxonomyIds[0]).getMetadata()
						.get(PARAM_TTL);
			if (null == ttl || ttl.intValue() <= 0)
				ttl = DEFAULT_TTL;
			listRes.put(PARAM_TTL, ttl);
			return listRes;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response bundle(Request request, String taxonomyId, String version) {
		String bundleFileName = (String) request.get("file_name");
		bundleFileName = bundleFileName.replaceAll("\\s+", "_");
		List<String> contentIds = (List<String>) request.get("content_identifiers");
		Response response = searchNodes(taxonomyId, contentIds);
		Response listRes = copyResponse(response);
		if (checkError(response)) {
			return response;
		} else {
			List<Object> list = (List<Object>) response.get(ContentAPIParams.contents.name());
			List<Map<String, Object>> ctnts = new ArrayList<Map<String, Object>>();
			List<String> childrenIds = new ArrayList<String>();
			List<Node> nodes = new ArrayList<Node>();
			if (null != list && !list.isEmpty()) {
				for (Object obj : list) {
					List<Node> nodelist = (List<Node>) obj;
					if (null != nodelist && !nodelist.isEmpty())
						nodes.addAll(nodelist);
				}
			}
			// Tune Each node for bundling as per mimetype
			int i = 0;
			for (Node node : nodes) {
				String mimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
				if (StringUtils.isBlank(mimeType)) {
					mimeType = "assets";
				}
				nodes.set(i, contentFactory.getImplForService(mimeType).tuneInputForBundling(node));
				i++;
			}
			
			getContentBundleData(taxonomyId, nodes, ctnts, childrenIds);
			if (ctnts.size() < contentIds.size()) {
				throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
						"One or more of the input content identifier are not found");
			}
			String fileName = bundleFileName + "_" + System.currentTimeMillis() + ".ecar";
			contentBundle.asyncCreateContentBundle(ctnts, childrenIds, fileName, version);
			String url = "https://" + bucketName + ".s3-ap-southeast-1.amazonaws.com/"
					+ ecarFolderName + "/" + fileName;
			String returnKey = ContentAPIParams.bundle.name();
			listRes.put(returnKey, url);
			return listRes;
		}
	}

	@SuppressWarnings("unchecked")
	private void getContentBundleData(String taxonomyId, List<Node> nodes,
			List<Map<String, Object>> ctnts, List<String> childrenIds) {
		Map<String, Node> nodeMap = new HashMap<String, Node>();
		if (null != nodes && !nodes.isEmpty()) {
			for (Node node : nodes) {
				nodeMap.put(node.getIdentifier(), node);
				Map<String, Object> metadata = new HashMap<String, Object>();
				if (null == node.getMetadata())
					node.setMetadata(new HashMap<String, Object>());
				metadata.putAll(node.getMetadata());
				metadata.put("identifier", node.getIdentifier());
				metadata.put("objectType", node.getObjectType());
				metadata.put("subject", node.getGraphId());
				metadata.remove("body");
				metadata.remove("editorState");
				if (null != node.getTags() && !node.getTags().isEmpty())
					metadata.put("tags", node.getTags());
				if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
					List<NodeDTO> children = new ArrayList<NodeDTO>();
					for (Relation rel : node.getOutRelations()) {
						if (StringUtils.equalsIgnoreCase(
								RelationTypes.SEQUENCE_MEMBERSHIP.relationName(),
								rel.getRelationType())
								&& StringUtils.equalsIgnoreCase(node.getObjectType(),
										rel.getEndNodeObjectType())) {
							childrenIds.add(rel.getEndNodeId());
							children.add(new NodeDTO(rel.getEndNodeId(), rel.getEndNodeName(), rel
									.getEndNodeObjectType(), rel.getRelationType(), rel
									.getMetadata()));
						}
					}
					if (!children.isEmpty()) {
						metadata.put("children", children);
					}
				}
				ctnts.add(metadata);
			}
			List<String> searchIds = new ArrayList<String>();
			for (String nodeId : childrenIds) {
				if (!nodeMap.containsKey(nodeId)) {
					searchIds.add(nodeId);
				}
			}
			if (!searchIds.isEmpty()) {
				Response searchRes = searchNodes(taxonomyId, searchIds);
				if (checkError(searchRes)) {
					throw new ServerException(ContentErrorCodes.ERR_CONTENT_SEARCH_ERROR.name(),
							getErrorMessage(searchRes));
				} else {
					List<Object> list = (List<Object>) searchRes.get(ContentAPIParams.contents
							.name());
					if (null != list && !list.isEmpty()) {
						for (Object obj : list) {
							List<Node> nodeList = (List<Node>) obj;
							for (Node node : nodeList) {
								nodeMap.put(node.getIdentifier(), node);
								Map<String, Object> metadata = new HashMap<String, Object>();
								if (null == node.getMetadata())
									node.setMetadata(new HashMap<String, Object>());
								metadata.putAll(node.getMetadata());
								metadata.put("identifier", node.getIdentifier());
								metadata.put("objectType", node.getObjectType());
								metadata.put("subject", node.getGraphId());
								metadata.remove("body");
								if (null != node.getTags() && !node.getTags().isEmpty())
									metadata.put("tags", node.getTags());
								ctnts.add(metadata);
							}
						}
					}
				}
			}
		}
	}

	private Response searchNodes(String taxonomyId, List<String> contentIds) {
		ContentSearchCriteria criteria = new ContentSearchCriteria();
		List<Filter> filters = new ArrayList<Filter>();
		Filter filter = new Filter("identifier", SearchConditions.OP_IN, contentIds);
		filters.add(filter);
		MetadataCriterion metadata = MetadataCriterion.create(filters);
		metadata.addFilter(filter);
		criteria.setMetadata(metadata);
		List<Request> requests = new ArrayList<Request>();
		if (StringUtils.isNotBlank(taxonomyId)) {
			Request req = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
					GraphDACParams.search_criteria.name(), criteria.getSearchCriteria());
			req.put(GraphDACParams.get_tags.name(), true);
			requests.add(req);
		} else {
			for (String tId : TaxonomyManagerImpl.taxonomyIds) {
				Request req = getRequest(tId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
						GraphDACParams.search_criteria.name(), criteria.getSearchCriteria());
				req.put(GraphDACParams.get_tags.name(), true);
				requests.add(req);
			}
		}
		Response response = getResponse(requests, LOGGER, GraphDACParams.node_list.name(),
				ContentAPIParams.contents.name());
		return response;
	}

	@SuppressWarnings("unchecked")
	public Response search(String taxonomyId, String objectType, Request request) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank.");
		ContentSearchCriteria criteria = (ContentSearchCriteria) request
				.get(ContentAPIParams.search_criteria.name());
		if (null == criteria)
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_INVALID_SEARCH_CRITERIA.name(),
					"Search Criteria Object is blank");

		Map<String, DefinitionDTO> definitions = new HashMap<String, DefinitionDTO>();
		if (StringUtils.isNotBlank(taxonomyId)) {
			DefinitionDTO definition = getDefinition(taxonomyId, objectType);
			definitions.put(taxonomyId, definition);
		} else {
			DefinitionDTO definition = getDefinition(TaxonomyManagerImpl.taxonomyIds[0], objectType);
			for (String id : TaxonomyManagerImpl.taxonomyIds) {
				definitions.put(id, definition);
			}
		}
		Request req = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
				GraphDACParams.search_criteria.name(), criteria.getSearchCriteria());
		Response response = getResponse(req, LOGGER);
		Response listRes = copyResponse(response);
		if (checkError(response)) {
			return response;
		} else {
			List<Node> nodes = (List<Node>) response.get(GraphDACParams.node_list.name());
			List<Map<String, Object>> contents = new ArrayList<Map<String, Object>>();
			if (null != nodes && !nodes.isEmpty()) {
				Object objFields = request.get(PARAM_FIELDS);
				List<String> fields = getList(mapper, objFields, true);
				for (Node node : nodes) {
					if (null == fields || fields.isEmpty()) {
						String subject = node.getGraphId();
						if (null != definitions.get(subject)
								&& null != definitions.get(subject).getMetadata()) {
							String[] arr = (String[]) definitions.get(subject).getMetadata()
									.get(PARAM_FIELDS);
							if (null != arr && arr.length > 0) {
								fields = Arrays.asList(arr);
							}
						}
					}
					ContentDTO content = new ContentDTO(node, fields);
					contents.add(content.returnMap());
				}
			}
			String returnKey = ContentAPIParams.content.name();
			listRes.put(returnKey, contents);
			return listRes;
		}
	}

	private DefinitionDTO getDefinition(String taxonomyId, String objectType) {
		Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER,
				"getNodeDefinition", GraphDACParams.object_type.name(), objectType);
		Response response = getResponse(request, LOGGER);
		if (!checkError(response)) {
			DefinitionDTO definition = (DefinitionDTO) response.get(GraphDACParams.definition_node
					.name());
			return definition;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private Request getContentsListRequest(Request request, String taxonomyId, String objectType,
			DefinitionDTO definition) {
		SearchCriteria sc = new SearchCriteria();
		sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
		sc.setObjectType(objectType);
		sc.sort(new Sort(SystemProperties.IL_UNIQUE_ID.name(), Sort.SORT_ASC));
		setLimit(request, sc, definition);

		ObjectMapper mapper = new ObjectMapper();
		// status filter
		List<String> statusList = new ArrayList<String>();
		Object statusParam = request.get(PARAM_STATUS);
		if (null != statusParam)
			statusList = getList(mapper, statusParam, true);
		if (null == statusList || statusList.isEmpty()) {
			if (null != definition && null != definition.getMetadata()) {
				String[] arr = (String[]) definition.getMetadata().get(PARAM_STATUS);
				if (null != arr && arr.length > 0) {
					statusList = Arrays.asList(arr);
				}
			}
		}
		if (null == statusList || statusList.isEmpty())
			statusList = DEFAULT_STATUS;
		MetadataCriterion mc = MetadataCriterion.create(Arrays.asList(new Filter(PARAM_STATUS,
				SearchConditions.OP_IN, statusList)));

		// set metadata filter params
		for (Entry<String, Object> entry : request.getRequest().entrySet()) {
			if (!StringUtils.equalsIgnoreCase(PARAM_SUBJECT, entry.getKey())
					&& !StringUtils.equalsIgnoreCase(PARAM_FIELDS, entry.getKey())
					&& !StringUtils.equalsIgnoreCase(PARAM_LIMIT, entry.getKey())
					&& !StringUtils.equalsIgnoreCase(PARAM_UID, entry.getKey())
					&& !StringUtils.equalsIgnoreCase(PARAM_STATUS, entry.getKey())
					&& !StringUtils.equalsIgnoreCase(PARAM_TAGS, entry.getKey())) {
				Object val = entry.getValue();
				List<String> list = getList(mapper, val, false);
				if (null != list && !list.isEmpty()) {
					mc.addFilter(new Filter(entry.getKey(), SearchConditions.OP_IN, list));
				} else if (null != val && StringUtils.isNotBlank(val.toString())) {
					if (val instanceof String) {
						mc.addFilter(new Filter(entry.getKey(), SearchConditions.OP_LIKE, val
								.toString()));
					} else {
						mc.addFilter(new Filter(entry.getKey(), SearchConditions.OP_EQUAL, val));
					}
				}
			} else if (StringUtils.equalsIgnoreCase(PARAM_TAGS, entry.getKey())) {
				List<String> tags = getList(mapper, entry.getValue(), true);
				if (null != tags && !tags.isEmpty()) {
					TagCriterion tc = new TagCriterion(tags);
					sc.setTag(tc);
				}
			}
		}
		sc.addMetadata(mc);
		Object objFields = request.get(PARAM_FIELDS);
		List<String> fields = getList(mapper, objFields, true);
		if (null == fields || fields.isEmpty()) {
			if (null != definition && null != definition.getMetadata()) {
				String[] arr = (String[]) definition.getMetadata().get(PARAM_FIELDS);
				if (null != arr && arr.length > 0) {
					List<String> arrFields = Arrays.asList(arr);
					fields = new ArrayList<String>();
					fields.addAll(arrFields);
				}
			}
		}
		if (null == fields || fields.isEmpty())
			fields = DEFAULT_FIELDS;
		else {
			fields.add(SystemProperties.IL_UNIQUE_ID.name());
		}
		sc.setFields(fields);
		Request req = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
				GraphDACParams.search_criteria.name(), sc);
		return req;
	}

	private void setLimit(Request request, SearchCriteria sc, DefinitionDTO definition) {
		Integer defaultLimit = null;
		if (null != definition && null != definition.getMetadata())
			defaultLimit = (Integer) definition.getMetadata().get(PARAM_LIMIT);
		if (null == defaultLimit || defaultLimit.intValue() <= 0)
			defaultLimit = DEFAULT_LIMIT;
		Integer limit = null;
		try {
			Object obj = request.get(PARAM_LIMIT);
			if (obj instanceof String)
				limit = Integer.parseInt((String) obj);
			else
				limit = (Integer) request.get(PARAM_LIMIT);
			if (null == limit || limit.intValue() <= 0)
				limit = defaultLimit;
		} catch (Exception e) {
		}
		sc.setResultSize(limit);
	}

	@SuppressWarnings("rawtypes")
	private List getList(ObjectMapper mapper, Object object, boolean returnList) {
		if (null != object) {
			try {
				String strObject = mapper.writeValueAsString(object);
				List list = mapper.readValue(strObject.toString(), List.class);
				return list;
			} catch (Exception e) {
				if (returnList) {
					List<String> list = new ArrayList<String>();
					list.add(object.toString());
					return list;
				}
			}
		}
		return null;
	}
	
	public static void main(String[] args) {
        String r = "https://ekstep-public.s3-ap-southeast-1.amazonaws.com/ecar_files/org.ekstep.story.hi.elephant_1458713044510.ecar";
        System.out.println(FilenameUtils.getExtension(r));
        String s = r.substring(0, r.lastIndexOf('/'));
        System.out.println(s);
        System.out.println(s.substring(s.lastIndexOf('/') + 1));
    }
	
	public Response optimize(String taxonomyId, String contentId) {
	    Response response = new Response();
	    if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
                    "Taxonomy Id is blank");
        if (StringUtils.isBlank(contentId))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_ID.name(),
                    "Content Id is blank");
        Response responseNode = getDataNode(taxonomyId, contentId);
        if (checkError(responseNode))
            throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
                    "Content not found with id: " + contentId);
        Node node = (Node) responseNode.get(GraphDACParams.node.name());
        String status = (String) node.getMetadata().get(ContentAPIParams.status.name());
        if (!StringUtils.equalsIgnoreCase("Live", status))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(),
                    "UnPublished content cannot be optimized");
        String downloadUrl = (String) node.getMetadata().get(ContentAPIParams.downloadUrl.name());
        if (StringUtils.isBlank(downloadUrl))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(),
                    "ECAR file not available for content");
        if (!StringUtils.equalsIgnoreCase("ecar", FilenameUtils.getExtension(downloadUrl)))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(),
                    "Content package is not an ECAR file");
        String optStatus = (String) node.getMetadata().get(ContentAPIParams.optStatus.name());
        if (StringUtils.equalsIgnoreCase("Processing", optStatus))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(),
                    "Content optimization is in progress. Please try after the current optimization is complete");
        node.getMetadata().put(ContentAPIParams.optStatus.name(), "Processing");
        updateNode(node);
        Optimizr optimizr = new Optimizr();
        try {
            File minEcar = optimizr.optimizeECAR(downloadUrl);
            String folder = getFolderName(downloadUrl);
            String[] arr = AWSUploader.uploadFile(bucketName, folder, minEcar);
            response.put("url", arr[1]);
            node.getMetadata().put(ContentAPIParams.optStatus.name(), "Complete");
            updateNode(node);
            FileUtils.deleteDirectory(minEcar.getParentFile());
        } catch (Exception e) {
            node.getMetadata().put(ContentAPIParams.optStatus.name(), "Error");
            updateNode(node);
            response = ERROR(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(), e.getMessage(), ResponseCode.SERVER_ERROR);
        }
	    return response;
	}
	
	private String getFolderName(String url) {
	    try {
	        String s = url.substring(0, url.lastIndexOf('/'));
	        return s.substring(s.lastIndexOf('/') + 1);
	    } catch (Exception e) {
	    }
	    return "";
	}
	
	protected Response updateNode(Node node) {
        Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "updateDataNode");
        updateReq.put(GraphDACParams.node.name(), node);
        updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
        Response updateRes = getResponse(updateReq, LOGGER);
        return updateRes;
    }

	public Response publish(String taxonomyId, String contentId) {
		Response response = new Response();
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_ID.name(),
					"Content Id is blank");
		Response responseNode = getDataNode(taxonomyId, contentId);
		if (checkError(responseNode))
			throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
					"Content not found with id: " + contentId);
		Node node = (Node) responseNode.get(GraphDACParams.node.name());
		String mimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
		if (StringUtils.isBlank(mimeType)) {
			mimeType = "assets";
		}
		IMimeTypeManager mimeTypeManager = contentFactory.getImplForService(mimeType);
		try {
			response = mimeTypeManager.publish(node);
			LogTelemetryEventUtil.logContentLifecycleEvent(contentId, node.getMetadata());
		} catch (ClientException e) {
			throw e;
		} catch (ServerException e) {
			throw e;
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_PUBLISH.name(), e.getMessage());
		}
		return response;
	}

	public Response extract(String taxonomyId, String contentId) {
		Response updateRes = null;
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_ID.name(),
					"Content Id is blank");
		Response responseNode = getDataNode(taxonomyId, contentId);
		if (checkError(responseNode))
			throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
					"Content not found with id: " + contentId);
		Node node = (Node) responseNode.get(GraphDACParams.node.name());
		if (node != null) {
			String mimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
			if (StringUtils.isBlank(mimeType)) {
				mimeType = "assets";
			}
			IMimeTypeManager mimeTypeManager = contentFactory.getImplForService(mimeType);
			Response response =  mimeTypeManager.extract(node);
			if (checkError(response)) {
				return response;
			}
			Map<String, Object> metadata = new HashMap<String, Object>();
			metadata = node.getMetadata();
			metadata.put(ContentAPIParams.body.name(), response.get("ecmlBody"));
			metadata.put(ContentAPIParams.editorState.name(), null);
			String appIconUrl = (String) node.getMetadata().get("appIcon");
			if (StringUtils.isBlank(appIconUrl)) {
				String newUrl = uploadFile(tempFileLocation, "logo.png");
				if (StringUtils.isNotBlank(newUrl))
					metadata.put("appIcon", newUrl);
			}
			node.setMetadata(metadata);
			node.setOutRelations(new ArrayList<Relation>());
			Request validateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER,
					"validateNode");
			validateReq.put(GraphDACParams.node.name(), node);
			Response validateRes = getResponse(validateReq, LOGGER);
			if (checkError(validateRes)) {
				return validateRes;
			} else {
				node.setInRelations(null);
				node.setOutRelations(null);
				Request updateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER,
						"updateDataNode");
				updateReq.put(GraphDACParams.node.name(), node);
				updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
				updateRes = getResponse(updateReq, LOGGER);
			}
		}
		return updateRes;
	}

	private String uploadFile(String folder, String filename) {
		File olderName = new File(folder + filename);
		try {
			if (null != olderName && olderName.exists() && olderName.isFile()) {
				String parentFolderName = olderName.getParent();
				File newName = new File(parentFolderName + File.separator
						+ System.currentTimeMillis() + "_" + olderName.getName());
				olderName.renameTo(newName);
				String[] url = AWSUploader.uploadFile(bucketName, folderName, newName);
				return url[1];
			}
		} catch (Exception ex) {
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_EXTRACT.name(), ex.getMessage());
		}
		return null;
	}

	public Response addRelation(String taxonomyId, String objectId1, String relation,
			String objectId2) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
					"Invalid taxonomy Id");
		if (StringUtils.isBlank(objectId1) || StringUtils.isBlank(objectId2))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
					"Object Id is blank");
		if (StringUtils.isBlank(relation))
			throw new ClientException(ContentErrorCodes.ERR_INVALID_RELATION_NAME.name(),
					"Relation name is blank");
		Request request = getRequest(taxonomyId, GraphEngineManagers.GRAPH_MANAGER,
				"createRelation");
		request.put(GraphDACParams.start_node_id.name(), objectId1);
		request.put(GraphDACParams.relation_type.name(), relation);
		request.put(GraphDACParams.end_node_id.name(), objectId2);
		Response response = getResponse(request, LOGGER);
		return response;
	}

	@SuppressWarnings("unchecked")
	protected Request getRequest(Map<String, Object> requestMap) {
		Request request = new Request();
		if (null != requestMap && !requestMap.isEmpty()) {
			String id = (String) requestMap.get("id");
			String ver = (String) requestMap.get("ver");
			String ts = (String) requestMap.get("ts");
			request.setId(id);
			request.setVer(ver);
			request.setTs(ts);
			Object reqParams = requestMap.get("params");
			if (null != reqParams) {
				try {
					RequestParams params = (RequestParams) mapper.convertValue(reqParams,
							RequestParams.class);
					request.setParams(params);
				} catch (Exception e) {
				}
			}
			Object requestObj = requestMap.get("request");
			if (null != requestObj) {
				try {
					String strRequest = mapper.writeValueAsString(requestObj);
					Map<String, Object> map = mapper.readValue(strRequest, Map.class);
					if (null != map && !map.isEmpty())
						request.setRequest(map);
				} catch (Exception e) {
				}
			}
		}
		return request;
	}	
}
