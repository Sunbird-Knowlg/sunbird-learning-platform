package com.ilimi.taxonomy.mgr.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.activation.MimetypesFileTypeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ilimi.assessment.enums.QuestionnaireType;
import com.ilimi.assessment.mgr.IAssessmentManager;
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
import com.ilimi.taxonomy.dto.ContentDTO;
import com.ilimi.taxonomy.dto.ContentSearchCriteria;
import com.ilimi.taxonomy.enums.ContentAPIParams;
import com.ilimi.taxonomy.enums.ContentErrorCodes;
import com.ilimi.taxonomy.mgr.IContentManager;
import com.ilimi.taxonomy.util.AWSUploader;
import com.ilimi.taxonomy.util.ContentBundle;
import com.ilimi.taxonomy.util.CustomParser;
import com.ilimi.taxonomy.util.HttpDownloadUtility;
import com.ilimi.taxonomy.util.UnzipUtility;
import com.ilimi.taxonomy.util.ZipUtility;

@Component
public class ContentManagerImpl extends BaseManager implements IContentManager {

	@Autowired
	private ContentBundle contentBundle;

	@Autowired
	private IAssessmentManager assessmentMgr;

	private static Logger LOGGER = LogManager.getLogger(IContentManager.class
			.getName());

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
	private static final String tempFileLocation = "/temp/";

	protected static final String URL_FIELD = "URL";

	@Override
	public Response create(String taxonomyId, String objectType, Request request) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(
					ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank");
		if (StringUtils.isBlank(objectType))
			throw new ClientException(
					ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
					"ObjectType is blank");
		Node item = (Node) request.get(ContentAPIParams.content.name());
		if (null == item)
			throw new ClientException(
					ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT.name(),
					objectType + " Object is blank");
		item.setObjectType(objectType);
		Request validateReq = getRequest(taxonomyId,
				GraphEngineManagers.NODE_MANAGER, "validateNode");
		validateReq.put(GraphDACParams.node.name(), item);
		Response validateRes = getResponse(validateReq, LOGGER);
		if (checkError(validateRes)) {
			return validateRes;
		} else {
			Request createReq = getRequest(taxonomyId,
					GraphEngineManagers.NODE_MANAGER, "createDataNode");
			createReq.put(GraphDACParams.node.name(), item);
			Response createRes = getResponse(createReq, LOGGER);
			return createRes;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response findAll(String taxonomyId, String objectType,
			Integer offset, Integer limit, String[] gfields) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(
					ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank");
		if (StringUtils.isBlank(objectType))
			throw new ClientException(
					ContentErrorCodes.ERR_CONTENT_INVALID_OBJECT_TYPE.name(),
					"Object Type is blank");
		LOGGER.info("Find All Content : " + taxonomyId + ", ObjectType: "
				+ objectType);
		SearchCriteria sc = new SearchCriteria();
		sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
		sc.setObjectType(objectType);
		sc.sort(new Sort(PARAM_STATUS, Sort.SORT_ASC));
		if (null != offset && offset.intValue() >= 0)
			sc.setStartPosition(offset);
		if (null != limit && limit.intValue() > 0)
			sc.setResultSize(limit);
		Request request = getRequest(taxonomyId,
				GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
				GraphDACParams.search_criteria.name(), sc);
		request.put(GraphDACParams.get_tags.name(), true);
		Response findRes = getResponse(request, LOGGER);
		Response response = copyResponse(findRes);
		if (checkError(response))
			return response;
		else {
			List<Node> nodes = (List<Node>) findRes
					.get(GraphDACParams.node_list.name());
			if (null != nodes && !nodes.isEmpty()) {
				if (null != gfields && gfields.length > 0) {
					for (Node node : nodes) {
						setMetadataFields(node, gfields);
					}
				}
				response.put(ContentAPIParams.contents.name(), nodes);
			}
			Request countReq = getRequest(taxonomyId,
					GraphEngineManagers.SEARCH_MANAGER, "getNodesCount",
					GraphDACParams.search_criteria.name(), sc);
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
			throw new ClientException(
					ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
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
		Request request = getRequest(taxonomyId,
				GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
				GraphDACParams.node_id.name(), id);
		request.put(GraphDACParams.get_tags.name(), true);
		Response getNodeRes = getResponse(request, LOGGER);
		return getNodeRes;
	}

	@Override
	public Response update(String id, String taxonomyId, String objectType,
			Request request) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(
					ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank");
		if (StringUtils.isBlank(id))
			throw new ClientException(
					ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
					"Content Object Id is blank");
		if (StringUtils.isBlank(objectType))
			throw new ClientException(
					ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
					"ObjectType is blank");
		Node item = (Node) request.get(ContentAPIParams.content.name());
		if (null == item)
			throw new ClientException(
					ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT.name(),
					objectType + " Object is blank");
		item.setIdentifier(id);
		item.setObjectType(objectType);
		Request validateReq = getRequest(taxonomyId,
				GraphEngineManagers.NODE_MANAGER, "validateNode");
		validateReq.put(GraphDACParams.node.name(), item);
		Response validateRes = getResponse(validateReq, LOGGER);
		if (checkError(validateRes)) {
			return validateRes;
		} else {
			Request updateReq = getRequest(taxonomyId,
					GraphEngineManagers.NODE_MANAGER, "updateDataNode");
			updateReq.put(GraphDACParams.node.name(), item);
			updateReq.put(GraphDACParams.node_id.name(), item.getIdentifier());
			Response updateRes = getResponse(updateReq, LOGGER);
			return updateRes;
		}
	}

	@Override
	public Response delete(String id, String taxonomyId) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(
					ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank");
		if (StringUtils.isBlank(id))
			throw new ClientException(
					ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
					"Content Object Id is blank");
		Request request = getRequest(taxonomyId,
				GraphEngineManagers.NODE_MANAGER, "deleteDataNode",
				GraphDACParams.node_id.name(), id);
		return getResponse(request, LOGGER);
	}

	@Override
	public Response upload(String id, String taxonomyId, File uploadedFile,
			String folder) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(
					ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank.");
		if (StringUtils.isBlank(id))
			throw new ClientException(
					ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
					"Content Object Id is blank.");
		if (null == uploadedFile) {
			throw new ClientException(
					ContentErrorCodes.ERR_CONTENT_BLANK_UPLOAD_OBJECT.name(),
					"Upload file is blank.");
		}
		Request request = getRequest(taxonomyId,
				GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
				GraphDACParams.node_id.name(), id);
		request.put(GraphDACParams.get_tags.name(), true);
		Response getNodeRes = getResponse(request, LOGGER);
		Response response = copyResponse(getNodeRes);
		if (checkError(response)) {
			return response;
		}
		Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
		String[] urlArray = new String[] {};
		try {
			if (StringUtils.isBlank(folder))
				folder = folderName;
			urlArray = AWSUploader.uploadFile(bucketName, folder, uploadedFile);
		} catch (Exception e) {
			throw new ServerException(
					ContentErrorCodes.ERR_CONTENT_UPLOAD_FILE.name(),
					"Error wihile uploading the File.", e);
		}
		node.getMetadata().put("s3Key", urlArray[0]);
		node.getMetadata().put("downloadUrl", urlArray[1]);

		Number pkgVersion = (Number) node.getMetadata().get("pkgVersion");
		if (null == pkgVersion || pkgVersion.intValue() < 1) {
			pkgVersion = 1;
		} else {
			pkgVersion = pkgVersion.doubleValue() + 1;
		}
		node.getMetadata().put("pkgVersion", pkgVersion);

		Request updateReq = getRequest(taxonomyId,
				GraphEngineManagers.NODE_MANAGER, "updateDataNode");
		updateReq.put(GraphDACParams.node.name(), node);
		updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
		Response updateRes = getResponse(updateReq, LOGGER);
		updateRes.put(ContentAPIParams.content_url.name(), urlArray[1]);
		return updateRes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response listContents(String taxonomyId, String objectType,
			Request request) {
		if (StringUtils.isBlank(taxonomyId))
			taxonomyId = (String) request.get(PARAM_SUBJECT);
		LOGGER.info("List Contents : " + taxonomyId);
		Map<String, DefinitionDTO> definitions = new HashMap<String, DefinitionDTO>();
		List<Request> requests = new ArrayList<Request>();
		if (StringUtils.isNotBlank(taxonomyId)) {
			DefinitionDTO definition = getDefinition(taxonomyId, objectType);
			definitions.put(taxonomyId, definition);
			Request req = getContentsListRequest(request, taxonomyId,
					objectType, definition);
			requests.add(req);
		} else {
			DefinitionDTO definition = getDefinition(
					TaxonomyManagerImpl.taxonomyIds[0], objectType);
			// TODO: need to get definition from the specific taxonomy.
			for (String id : TaxonomyManagerImpl.taxonomyIds) {
				definitions.put(id, definition);
				Request req = getContentsListRequest(request, id, objectType,
						definition);
				requests.add(req);
			}
		}
		Response response = getResponse(requests, LOGGER,
				GraphDACParams.node_list.name(),
				ContentAPIParams.contents.name());
		Response listRes = copyResponse(response);
		if (checkError(response))
			return response;
		else {
			List<List<Node>> nodes = (List<List<Node>>) response
					.get(ContentAPIParams.contents.name());
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
										&& null != definitions.get(subject)
												.getMetadata()) {
									String[] arr = (String[]) definitions
											.get(subject).getMetadata()
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
				}
			}
			String returnKey = ContentAPIParams.content.name();
			listRes.put(returnKey, contents);
			Integer ttl = null;
			if (null != definitions.get(TaxonomyManagerImpl.taxonomyIds[0])
					&& null != definitions.get(
							TaxonomyManagerImpl.taxonomyIds[0]).getMetadata())
				ttl = (Integer) definitions
						.get(TaxonomyManagerImpl.taxonomyIds[0]).getMetadata()
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
		List<String> contentIds = (List<String>) request
				.get("content_identifiers");
		Response response = searchNodes(taxonomyId, contentIds);
		Response listRes = copyResponse(response);
		if (checkError(response)) {
			return response;
		} else {
			List<Object> list = (List<Object>) response
					.get(ContentAPIParams.contents.name());
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
			getContentBundleData(taxonomyId, nodes, ctnts, childrenIds);
			if (ctnts.size() < contentIds.size()) {
				throw new ResourceNotFoundException(
						ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
						"One or more of the input content identifier are not found");
			}
			String fileName = bundleFileName + "_" + System.currentTimeMillis()
					+ ".ecar";
			contentBundle.asyncCreateContentBundle(ctnts, childrenIds,
					fileName, version);
			String url = "https://" + bucketName
					+ ".s3-ap-southeast-1.amazonaws.com/" + ecarFolderName
					+ "/" + fileName;
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
				if (null != node.getOutRelations()
						&& !node.getOutRelations().isEmpty()) {
					List<NodeDTO> children = new ArrayList<NodeDTO>();
					for (Relation rel : node.getOutRelations()) {
						if (StringUtils.equalsIgnoreCase(
								RelationTypes.SEQUENCE_MEMBERSHIP
										.relationName(), rel.getRelationType())
								&& StringUtils.equalsIgnoreCase(
										node.getObjectType(),
										rel.getEndNodeObjectType())) {
							childrenIds.add(rel.getEndNodeId());
							children.add(new NodeDTO(rel.getEndNodeId(), rel
									.getEndNodeName(), rel
									.getEndNodeObjectType(), rel
									.getRelationType(), rel.getMetadata()));
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
					throw new ServerException(
							ContentErrorCodes.ERR_CONTENT_SEARCH_ERROR.name(),
							getErrorMessage(searchRes));
				} else {
					List<Object> list = (List<Object>) searchRes
							.get(ContentAPIParams.contents.name());
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
								if (null != node.getTags()
										&& !node.getTags().isEmpty())
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
		Filter filter = new Filter("identifier", SearchConditions.OP_IN,
				contentIds);
		filters.add(filter);
		MetadataCriterion metadata = MetadataCriterion.create(filters);
		metadata.addFilter(filter);
		criteria.setMetadata(metadata);
		List<Request> requests = new ArrayList<Request>();
		if (StringUtils.isNotBlank(taxonomyId)) {
			Request req = getRequest(taxonomyId,
					GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
					GraphDACParams.search_criteria.name(),
					criteria.getSearchCriteria());
			req.put(GraphDACParams.get_tags.name(), true);
			requests.add(req);
		} else {
			for (String tId : TaxonomyManagerImpl.taxonomyIds) {
				Request req = getRequest(tId,
						GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
						GraphDACParams.search_criteria.name(),
						criteria.getSearchCriteria());
				req.put(GraphDACParams.get_tags.name(), true);
				requests.add(req);
			}
		}
		Response response = getResponse(requests, LOGGER,
				GraphDACParams.node_list.name(),
				ContentAPIParams.contents.name());
		return response;
	}

	@SuppressWarnings("unchecked")
	public Response search(String taxonomyId, String objectType, Request request) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(
					ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank.");
		ContentSearchCriteria criteria = (ContentSearchCriteria) request
				.get(ContentAPIParams.search_criteria.name());
		if (null == criteria)
			throw new ClientException(
					ContentErrorCodes.ERR_CONTENT_INVALID_SEARCH_CRITERIA
							.name(),
					"Search Criteria Object is blank");

		Map<String, DefinitionDTO> definitions = new HashMap<String, DefinitionDTO>();
		if (StringUtils.isNotBlank(taxonomyId)) {
			DefinitionDTO definition = getDefinition(taxonomyId, objectType);
			definitions.put(taxonomyId, definition);
		} else {
			DefinitionDTO definition = getDefinition(
					TaxonomyManagerImpl.taxonomyIds[0], objectType);
			for (String id : TaxonomyManagerImpl.taxonomyIds) {
				definitions.put(id, definition);
			}
		}
		Request req = getRequest(taxonomyId,
				GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
				GraphDACParams.search_criteria.name(),
				criteria.getSearchCriteria());
		Response response = getResponse(req, LOGGER);
		Response listRes = copyResponse(response);
		if (checkError(response)) {
			return response;
		} else {
			List<Node> nodes = (List<Node>) response
					.get(GraphDACParams.node_list.name());
			List<Map<String, Object>> contents = new ArrayList<Map<String, Object>>();
			if (null != nodes && !nodes.isEmpty()) {
				Object objFields = request.get(PARAM_FIELDS);
				List<String> fields = getList(mapper, objFields, true);
				for (Node node : nodes) {
					if (null == fields || fields.isEmpty()) {
						String subject = node.getGraphId();
						if (null != definitions.get(subject)
								&& null != definitions.get(subject)
										.getMetadata()) {
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
			String returnKey = ContentAPIParams.content.name();
			listRes.put(returnKey, contents);
			return listRes;
		}
	}

	private DefinitionDTO getDefinition(String taxonomyId, String objectType) {
		Request request = getRequest(taxonomyId,
				GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
				GraphDACParams.object_type.name(), objectType);
		Response response = getResponse(request, LOGGER);
		if (!checkError(response)) {
			DefinitionDTO definition = (DefinitionDTO) response
					.get(GraphDACParams.definition_node.name());
			return definition;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private Request getContentsListRequest(Request request, String taxonomyId,
			String objectType, DefinitionDTO definition) {
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
				String[] arr = (String[]) definition.getMetadata().get(
						PARAM_STATUS);
				if (null != arr && arr.length > 0) {
					statusList = Arrays.asList(arr);
				}
			}
		}
		if (null == statusList || statusList.isEmpty())
			statusList = DEFAULT_STATUS;
		MetadataCriterion mc = MetadataCriterion.create(Arrays
				.asList(new Filter(PARAM_STATUS, SearchConditions.OP_IN,
						statusList)));

		// set metadata filter params
		for (Entry<String, Object> entry : request.getRequest().entrySet()) {
			if (!StringUtils.equalsIgnoreCase(PARAM_SUBJECT, entry.getKey())
					&& !StringUtils.equalsIgnoreCase(PARAM_FIELDS,
							entry.getKey())
					&& !StringUtils.equalsIgnoreCase(PARAM_LIMIT,
							entry.getKey())
					&& !StringUtils.equalsIgnoreCase(PARAM_UID, entry.getKey())
					&& !StringUtils.equalsIgnoreCase(PARAM_STATUS,
							entry.getKey())
					&& !StringUtils
							.equalsIgnoreCase(PARAM_TAGS, entry.getKey())) {
				Object val = entry.getValue();
				List<String> list = getList(mapper, val, false);
				if (null != list && !list.isEmpty()) {
					mc.addFilter(new Filter(entry.getKey(),
							SearchConditions.OP_IN, list));
				} else if (null != val
						&& StringUtils.isNotBlank(val.toString())) {
					if (val instanceof String) {
						mc.addFilter(new Filter(entry.getKey(),
								SearchConditions.OP_LIKE, val.toString()));
					} else {
						mc.addFilter(new Filter(entry.getKey(),
								SearchConditions.OP_EQUAL, val));
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
				String[] arr = (String[]) definition.getMetadata().get(
						PARAM_FIELDS);
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
		Request req = getRequest(taxonomyId,
				GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
				GraphDACParams.search_criteria.name(), sc);
		return req;
	}

	private void setLimit(Request request, SearchCriteria sc,
			DefinitionDTO definition) {
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

	public Response publish(String taxonomyId, String contentId) {
		Response response = new Response();
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(
					ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(
					ContentErrorCodes.ERR_CONTENT_BLANK_ID.name(),
					"Content Id is blank");
		Response responseNode = getDataNode(taxonomyId, contentId);
		if (checkError(responseNode))
			throw new ResourceNotFoundException(
					ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
					"Content not found with id: " + contentId);
		Node node = (Node) responseNode.get(GraphDACParams.node.name());
		String fileName = System.currentTimeMillis() + "_"
				+ node.getIdentifier();
		Map<String, Object> metadata = new HashMap<String, Object>();
		metadata = node.getMetadata();
		String contentBody = (String) metadata.get("body");
		String contentType = checkBodyContentType(contentBody);
		if (StringUtils.isBlank(contentType))
			throw new ClientException(
					ContentErrorCodes.ERR_CONTENT_BODY_INVALID.name(),
					"Content of Body Either Invalid or Null");
		String tempFile = null;
		String tempWithTimeStamp = null;
		try {
			tempFile = tempFileLocation;
			tempWithTimeStamp = tempFile + File.separator
					+ System.currentTimeMillis() + "_temp";
			File file = null;
			if (StringUtils.equalsIgnoreCase("ecml", contentType)) {
				file = new File(tempWithTimeStamp + File.separator
						+ "index.ecml");
			} else if (StringUtils.equalsIgnoreCase("json", contentType)) {
				file = new File(tempWithTimeStamp + File.separator
						+ "index.json");
			}
			if (null != file) {
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
					if (!file.exists()) {
						file.createNewFile();
					}
				}
				FileUtils.writeStringToFile(file, contentBody);
			}
			String appIcon = (String) metadata.get("appIcon");
			if (StringUtils.isNotBlank(appIcon)) {
				File logoFile = HttpDownloadUtility.downloadFile(appIcon,
						tempWithTimeStamp);
				try {
					if (null != logoFile && logoFile.exists()
							&& logoFile.isFile()) {
						String parentFolderName = logoFile.getParent();
						File newName = new File(parentFolderName
								+ File.separator + "logo.png");
						logoFile.renameTo(newName);
					}
				} catch (Exception ex) {
					throw new ServerException(
							ContentErrorCodes.ERR_CONTENT_EXTRACT.name(),
							ex.getMessage());
				}
			}
			response = parseContent(taxonomyId, contentId, tempWithTimeStamp,
					fileName, contentType);
		} catch (IOException e) {
			throw new ServerException(
					ContentErrorCodes.ERR_CONTENT_PUBLISH.name(),
					e.getMessage());
		}
		return response;
	}

	private String checkBodyContentType(String contentBody) {
		if (StringUtils.isNotEmpty(contentBody)) {
			if (isECMLValid(contentBody)) {
				return "ecml";
			} else if (isJSONValid(contentBody)) {
				return "json";
			}
		}
		return null;
	}

	public boolean isJSONValid(String content) {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			mapper.readTree(content);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public boolean isECMLValid(String content) {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			dBuilder.parse(IOUtils.toInputStream(content, "UTF-8"));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * this method parse ECML file and find src to download media type in assets
	 * folder and finally zip parent folder.
	 * 
	 * @param filePath
	 * @param saveDir
	 */
	public Response parseContent(String taxonomyId, String contentId,
			String filePath, String fileName, String contentType) {
		File file = new File(filePath);
		String fileLocation = filePath + File.separator + "index.ecml";
		String sourceFolder = file.getParent() + File.separator;
		Response response = new Response();
		try {
			if (contentType.equalsIgnoreCase("json")) {
				CustomParser.readJsonFileDownload(filePath);
			} else if (contentType.equalsIgnoreCase("ecml")) {
				new CustomParser(new File(fileLocation)).updateEcml(filePath);
			}
			String zipFilePathName = sourceFolder + fileName + ".zip";
			List<String> fileList = new ArrayList<String>();
			ZipUtility appZip = new ZipUtility(fileList, zipFilePathName,
					filePath);
			appZip.generateFileList(new File(filePath));
			appZip.zipIt(zipFilePathName);
			File olderName = new File(zipFilePathName);
			if (olderName.exists() && olderName.isFile()) {
				File newName = new File(sourceFolder + File.separator
						+ olderName.getName());
				olderName.renameTo(newName);
				Request request = getRequest(taxonomyId,
						GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
						GraphDACParams.node_id.name(), contentId);
				request.put(GraphDACParams.get_tags.name(), true);
				Response getNodeRes = getResponse(request, LOGGER);
				if (checkError(getNodeRes)) {
					return getNodeRes;
				}
				Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
				node.getMetadata().put("downloadUrl", newName);
				List<Node> nodes = new ArrayList<Node>();
				nodes.add(node);
				List<Map<String, Object>> ctnts = new ArrayList<Map<String, Object>>();
				List<String> childrenIds = new ArrayList<String>();
				getContentBundleData(taxonomyId, nodes, ctnts, childrenIds);
				String bundleFileName = contentId + "_"
						+ System.currentTimeMillis() + ".ecar";
				String[] urlArray = contentBundle.createContentBundle(ctnts,
						childrenIds, bundleFileName, "1.1");
				node.getMetadata().put("s3Key", urlArray[0]);
				node.getMetadata().put("downloadUrl", urlArray[1]);
				Number pkgVersion = (Number) node.getMetadata().get(
						"pkgVersion");
				if (null == pkgVersion || pkgVersion.intValue() < 1) {
					pkgVersion = 1.0;
				} else {
					pkgVersion = pkgVersion.doubleValue() + 1;
				}
				node.getMetadata().put("pkgVersion", pkgVersion);
				node.getMetadata().put("status", "Live");
				Request updateReq = getRequest(taxonomyId,
						GraphEngineManagers.NODE_MANAGER, "updateDataNode");
				updateReq.put(GraphDACParams.node.name(), node);
				updateReq.put(GraphDACParams.node_id.name(),
						node.getIdentifier());
				Response updateRes = getResponse(updateReq, LOGGER);
				updateRes.put(ContentAPIParams.content_url.name(), urlArray[1]);
				response = updateRes;
			}

		} catch (Exception e) {
			throw new ServerException(
					ContentErrorCodes.ERR_CONTENT_PUBLISH.name(),
					e.getMessage());
		} finally {
			File directory = new File(sourceFolder);
			if (!directory.exists()) {
				System.out.println("Directory does not exist.");
				System.exit(0);
			} else {
				try {
					delete(directory);
					if (!directory.exists()) {
						directory.mkdirs();
					}
				} catch (IOException e) {
					throw new ServerException(
							ContentErrorCodes.ERR_CONTENT_PUBLISH.name(),
							e.getMessage());
				}
			}
		}
		return response;
	}

	private static void delete(File file) throws IOException {
		if (file.isDirectory()) {
			// directory is empty, then delete it
			if (file.list().length == 0) {
				file.delete();
			} else {
				// list all the directory contents
				String files[] = file.list();
				for (String temp : files) {
					// construct the file structure
					File fileDelete = new File(file, temp);
					// recursive delete
					delete(fileDelete);
				}
				// check the directory again, if empty then delete it
				if (file.list().length == 0) {
					file.delete();
				}
			}

		} else {
			// if file, then delete it
			file.delete();
		}
	}

	@SuppressWarnings("unchecked")
	public Response extract(String taxonomyId, String contentId) {
		Response updateRes = null;
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(
					ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(
					ContentErrorCodes.ERR_CONTENT_BLANK_ID.name(),
					"Content Id is blank");
		Response responseNode = getDataNode(taxonomyId, contentId);
		Node node = (Node) responseNode.get(GraphDACParams.node.name());
		if (responseNode != null) {
			String zipFilUrl = (String) node.getMetadata().get("downloadUrl");
			String tempFileDwn = tempFileLocation + System.currentTimeMillis()
					+ "_temp";
			File zipFile = null;
			System.out.println("zipFilUrl is " + zipFilUrl);
			if (StringUtils.isNotBlank(zipFilUrl)) {
				zipFile = HttpDownloadUtility.downloadFile(zipFilUrl,
						tempFileDwn);
				System.out.println("zipFile is " + zipFile);
			}
			Response response = new Response();
			response = extractContent(taxonomyId, zipFile, contentId);
			if (checkError(response)) {
				return response;
			}
			Map<String, Object> metadata = new HashMap<String, Object>();
			metadata = node.getMetadata();
			metadata.put("body", response.get("ecmlBody"));
			String appIconUrl = (String) node.getMetadata().get("appIcon");
			if (StringUtils.isBlank(appIconUrl)) {
				String newUrl = uploadFile(tempFileLocation, "logo.png");
				if (StringUtils.isNotBlank(newUrl))
					metadata.put("appIcon", newUrl);
			}
			node.setMetadata(metadata);
			node.setOutRelations(new ArrayList<Relation>());
			Request validateReq = getRequest(taxonomyId,
					GraphEngineManagers.NODE_MANAGER, "validateNode");
			validateReq.put(GraphDACParams.node.name(), node);
			Response validateRes = getResponse(validateReq, LOGGER);
			if (checkError(validateRes)) {
				return validateRes;
			} else {
				Request updateReq = getRequest(taxonomyId,
						GraphEngineManagers.NODE_MANAGER, "updateDataNode");
				updateReq.put(GraphDACParams.node.name(), node);
				updateReq.put(GraphDACParams.node_id.name(),
						node.getIdentifier());
				updateRes = getResponse(updateReq, LOGGER);
			}
			List<Relation> outRelations = (List<Relation>) response
					.get("outRelations");
			System.out.println("Out relations size: " + outRelations.size());
			if (null != outRelations && !outRelations.isEmpty()) {
				for (Relation outRel : outRelations) {
					addRelation(taxonomyId, node.getIdentifier(),
							outRel.getRelationType(), outRel.getEndNodeId());
				}
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
						+ System.currentTimeMillis() + "_"
						+ olderName.getName());
				olderName.renameTo(newName);
				String[] url = AWSUploader.uploadFile(bucketName, folderName,
						newName);
				return url[1];
			}
		} catch (Exception ex) {
			throw new ServerException(
					ContentErrorCodes.ERR_CONTENT_EXTRACT.name(),
					ex.getMessage());
		}
		return null;
	}

	/**
	 * This method unzip content folder then it read ECML file and check graph
	 * with id if Id avl then modify ECML File if Id not there it upload on AWS3
	 * and then ceate graph and modify ECML file.
	 * 
	 * @param zipFilePath
	 * @param saveDir
	 */
	@SuppressWarnings({ "unchecked" })
	public Response extractContent(String taxonomyId, File zipFile,
			String contentId) {
		String zipFilePath = zipFile.getPath();
		String zipFileDir = zipFile.getParent();
		String filePath = zipFileDir + File.separator + "index.ecml";
		String uploadFilePath = zipFileDir + File.separator + "assets"
				+ File.separator;
		Map<String, List<String>> mediaIdURL = new HashMap<String, List<String>>();
		List<String> listOfCtrlType = new ArrayList<>();
		listOfCtrlType.add("items");
		listOfCtrlType.add("data");
		UnzipUtility unzipper = new UnzipUtility();
		Response response = new Response();
		try {
			unzipper.unzip(zipFilePath, zipFileDir);
			List<Relation> outRelations = new ArrayList<Relation>();
			createAssessmentItemFromContent(taxonomyId, zipFileDir, contentId,
					outRelations);
			Map<String, List<String>> mediaIdMap = CustomParser
					.readECMLFile(filePath);
			Map<String, String> mediaSrcMap = new HashMap<String, String>();
			Map<String, String> mediaAssetIdMap = new HashMap<String, String>();
			Set<String> mediaIdSet = new HashSet<String>();
			for (Entry<String, List<String>> entry : mediaIdMap.entrySet()) {
				String id = entry.getKey();
				List<String> values = entry.getValue();
				if (null != values && values.size() == 2) {
					String src = values.get(0);
					String assetId = values.get(1);
					String nodeId = null;
					if (StringUtils.isNotBlank(assetId)) {
						nodeId = assetId;
					} else {
						nodeId = contentId + "_" + id;
					}
					mediaIdSet.add(nodeId);
					mediaSrcMap.put(id, src);
					mediaAssetIdMap.put(nodeId, id);
				}
			}
			CustomParser customParser = new CustomParser(new File(filePath));
			if (null != mediaIdSet && !mediaIdSet.isEmpty()) {
				Request mediaReq = getRequest(taxonomyId,
						GraphEngineManagers.SEARCH_MANAGER, "getDataNodes",
						GraphDACParams.node_ids.name(), new ArrayList<>(
								mediaIdSet));
				Response mediaRes = getResponse(mediaReq, LOGGER);
				List<Node> nodeList = (List<Node>) mediaRes
						.get(GraphDACParams.node_list.name());
				if (null != mediaIdSet && !mediaIdSet.isEmpty()) {
					for (Node node : nodeList) {
						if (mediaIdSet.contains(node.getIdentifier())) {
							mediaIdSet.remove(node.getIdentifier());
							String downloadUrl = (String) node.getMetadata()
									.get("downloadUrl");
							if (StringUtils.isBlank(downloadUrl)) {
								long timeStempInMiliSec = System
										.currentTimeMillis();
								String mediaSrc = mediaSrcMap
										.get(mediaAssetIdMap.get(node
												.getIdentifier()));
								File olderName = new File(uploadFilePath
										+ mediaSrc);
								if (olderName.exists() && olderName.isFile()) {
									String parentFolderName = olderName
											.getParent();
									File newName = new File(parentFolderName
											+ File.separator
											+ timeStempInMiliSec
											+ olderName.getName());
									olderName.renameTo(newName);
									String[] url = AWSUploader.uploadFile(
											bucketName, folderName, newName);
									Node newItem = createNode(node, url[1],
											node.getIdentifier(), olderName);
									List<String> values = new ArrayList<String>();
									values.add(url[1]);
									values.add(node.getIdentifier());
									mediaIdURL.put(mediaAssetIdMap.get(node
											.getIdentifier()), values);
									Request validateReq = getRequest(
											taxonomyId,
											GraphEngineManagers.NODE_MANAGER,
											"validateNode");
									validateReq.put(GraphDACParams.node.name(),
											newItem);
									Response validateRes = getResponse(
											validateReq, LOGGER);
									if (checkError(validateRes)) {
										return validateRes;
									} else {
										Request updateReq = getRequest(
												taxonomyId,
												GraphEngineManagers.NODE_MANAGER,
												"updateDataNode");
										updateReq.put(
												GraphDACParams.node.name(),
												newItem);
										updateReq.put(
												GraphDACParams.node_id.name(),
												newItem.getIdentifier());
										getResponse(updateReq, LOGGER);
									}
								}
							} else {
								List<String> values = new ArrayList<String>();
								values.add(downloadUrl);
								values.add(node.getIdentifier());
								mediaIdURL.put(mediaAssetIdMap.get(node
										.getIdentifier()), values);
							}
						}
					}
					if (mediaIdSet != null && !mediaIdSet.isEmpty()) {
						for (String mediaId : mediaIdSet) {
							long timeStempInMiliSec = System
									.currentTimeMillis();
							String mediaSrc = mediaSrcMap.get(mediaAssetIdMap
									.get(mediaId));
							File olderName = new File(uploadFilePath + mediaSrc);
							if (olderName.exists() && olderName.isFile()) {
								String parentFolderName = olderName.getParent();
								File newName = new File(parentFolderName
										+ File.separator + timeStempInMiliSec
										+ olderName.getName());
								olderName.renameTo(newName);
								String[] url = AWSUploader.uploadFile(
										bucketName, folderName, newName);
								Node item = createNode(new Node(), url[1],
										mediaId, olderName);
								// Creating a graph.
								Request validateReq = getRequest(taxonomyId,
										GraphEngineManagers.NODE_MANAGER,
										"validateNode");
								validateReq.put(GraphDACParams.node.name(),
										item);
								Response validateRes = getResponse(validateReq,
										LOGGER);
								if (checkError(validateRes)) {
									return validateRes;
								} else {
									Request createReq = getRequest(taxonomyId,
											GraphEngineManagers.NODE_MANAGER,
											"createDataNode");
									createReq.put(GraphDACParams.node.name(),
											item);
									getResponse(createReq, LOGGER);
									List<String> values = new ArrayList<String>();
									values.add(url[1]);
									values.add(item.getIdentifier());
									mediaIdURL.put(
											mediaAssetIdMap.get(mediaId),
											values);
								}
							}
						}
					}
				}
				customParser.updateSrcInEcml(filePath, mediaIdURL);
			}
			customParser.updateJsonInEcml(filePath, listOfCtrlType);
			response.put("ecmlBody", CustomParser.readFile(new File(filePath)));
			response.put("outRelations", outRelations);
		} catch (Exception ex) {
			throw new ServerException(
					ContentErrorCodes.ERR_CONTENT_EXTRACT.name(),
					ex.getMessage());
		} finally {
			File directory = new File(zipFileDir);
			if (!directory.exists()) {
				System.out.println("Directory does not exist.");
				System.exit(0);
			} else {
				try {
					delete(directory);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return response;
	}

	private Node createNode(Node item, String url, String mediaId,
			File olderName) {
		item.setIdentifier(mediaId);
		item.setObjectType("Content");
		Map<String, Object> metadata = new HashMap<String, Object>();
		metadata.put("name", mediaId);
		metadata.put("code", mediaId);
		metadata.put("body", "<content></content>");
		metadata.put("status", "Live");
		metadata.put("owner", "ekstep");
		metadata.put("contentType", "Asset");
		metadata.put("downloadUrl", url);
		if (metadata.get("pkgVersion") == null) {
			metadata.put("pkgVersion", 1);
		} else {
			Double version = (Double) metadata.get("pkgVersion") + 1;
			metadata.put("pkgVersion", version);
		}
		Object mimeType = getMimeType(new File(olderName.getName()));
		metadata.put("mimeType", mimeType);
		String mediaType = getMediaType(olderName.getName());
		metadata.put("mediaType", mediaType);
		item.setMetadata(metadata);
		return item;
	}

	private String getMediaType(String fileName) {
		String mediaType = "image";
		if (StringUtils.isNotBlank(fileName)) {
			if (fileName.endsWith(".pdf")) {
				mediaType = "pdf";
			} else if (fileName.endsWith(".mp4") || fileName.endsWith(".avi")
					|| fileName.endsWith(".3gpp") || fileName.endsWith(".webm")) {
				mediaType = "video";
			} else if (fileName.endsWith(".mp3") || fileName.endsWith(".ogg")
					|| fileName.endsWith(".wav")) {
				mediaType = "audio";
			} else if (fileName.endsWith(".txt") || fileName.endsWith(".json")
					|| fileName.endsWith(".xml")) {
				mediaType = "text";
			}
		}
		return mediaType;
	}

	private Object getMimeType(File file) {
		MimetypesFileTypeMap mimeType = new MimetypesFileTypeMap();
		return mimeType.getContentType(file);
	}

	@SuppressWarnings({ "unchecked" })
	private Map<String, Object> createAssessmentItemFromContent(
			String taxonomyId, String contentExtractedPath, String contentId,
			List<Relation> outRelations) {
		if (null != contentExtractedPath) {
			List<File> fileList = getControllerFileList(contentExtractedPath);
			Map<String, Object> mapResDetail = new HashMap<String, Object>();
			if (null == fileList)
				return null;
			for (File file : fileList) {
				System.out.println(file.getName());
				if (file.exists()) {
					try {
						Map<String, Object> fileJSON = new ObjectMapper()
								.readValue(file, HashMap.class);
						if (null != fileJSON) {
							Map<String, Object> itemSet = (Map<String, Object>) fileJSON
									.get(ContentAPIParams.items.name());
							List<Object> lstAssessmentItem = new ArrayList<Object>();
							for (Entry<String, Object> entry : itemSet
									.entrySet()) {
								Object assessmentItem = (Object) entry
										.getValue();
								lstAssessmentItem.add(assessmentItem);
								List<Map<String, Object>> lstMap = (List<Map<String, Object>>) assessmentItem;
								List<String> lstAssessmentItemId = new ArrayList<String>();
								Map<String, Object> assessResMap = new HashMap<String, Object>();
								Map<String, String> mapAssessItemRes = new HashMap<String, String>();
								Map<String, Object> mapRelation = new HashMap<String, Object>();
								for (Map<String, Object> map : lstMap) {
									Request request = getAssessmentItemRequestObject(
											map, "AssessmentItem", contentId,
											ContentAPIParams.assessment_item
													.name());
									if (null != request) {
										Node itemNode = (Node) request
												.get(ContentAPIParams.assessment_item
														.name());
										Response response = null;
										if (StringUtils.isBlank(itemNode
												.getIdentifier())) {
											response = assessmentMgr
													.createAssessmentItem(
															taxonomyId, request);
										} else {
											response = assessmentMgr
													.updateAssessmentItem(
															itemNode.getIdentifier(),
															taxonomyId, request);
										}
										LOGGER.info("Create Item | Response: "
												+ response);
										Map<String, Object> resMap = response
												.getResult();
										if (null != resMap
												.get(ContentAPIParams.node_id
														.name())) {
											String identifier = (String) resMap
													.get(ContentAPIParams.node_id
															.name());
											mapRelation
													.put(identifier,
															map.get(ContentAPIParams.concepts
																	.name()));
											lstAssessmentItemId.add(identifier);
											mapAssessItemRes
													.put(identifier,
															"Assessment Item "
																	+ identifier
																	+ " Added Successfully");
										} else {
											System.out
													.println("Item validation failed: "
															+ resMap.get(ContentAPIParams.messages
																	.name()));
											String id = (String) map
													.get(ContentAPIParams.identifier
															.name());
											if (StringUtils.isNotBlank(id))
												mapAssessItemRes
														.put(id,
																(String) resMap
																		.get(ContentAPIParams.messages
																				.name()));
										}
									}
								}
								assessResMap
										.put(ContentAPIParams.assessment_item
												.name(), mapAssessItemRes);
								Response itemSetRes = createItemSet(taxonomyId,
										contentId, lstAssessmentItemId,
										fileJSON);
								if (null != itemSetRes) {
									Map<String, Object> mapItemSetRes = itemSetRes
											.getResult();
									assessResMap
											.put(ContentAPIParams.assessment_item_set
													.name(), mapItemSetRes);
									String itemSetNodeId = (String) mapItemSetRes
											.get(ContentAPIParams.set_id.name());
									System.out.println("itemSetNodeId: "
											+ itemSetNodeId);
									if (StringUtils.isNotBlank(itemSetNodeId)) {
										Relation outRel = new Relation(null,
												RelationTypes.ASSOCIATED_TO
														.relationName(),
												itemSetNodeId);
										outRelations.add(outRel);
									}
								}
								List<String> lstAssessItemRelRes = createRelation(
										taxonomyId, mapRelation, outRelations);
								assessResMap.put(
										ContentAPIParams.AssessmentItemRelation
												.name(), lstAssessItemRelRes);
								mapResDetail.put(file.getName(), assessResMap);
							}
						} else {
							mapResDetail.put(file.getName(),
									"Error : Invalid JSON");
						}
					} catch (Exception e) {
						mapResDetail.put(file.getName(),
								"Error : Unable to Parse JSON");
						e.printStackTrace();
					}
				} else {
					mapResDetail.put(file.getName(),
							"Error: File doesn't Exist");
				}
			}
			return mapResDetail;
		}
		return null;
	}

	private Response createItemSet(String taxonomyId, String contentId,
			List<String> lstAssessmentItemId, Map<String, Object> fileJSON) {
		if (null != lstAssessmentItemId && lstAssessmentItemId.size() > 0
				&& StringUtils.isNotBlank(taxonomyId)) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(ContentAPIParams.memberIds.name(), lstAssessmentItemId);
			Integer totalItems = (Integer) fileJSON.get("total_items");
			if (null == totalItems || totalItems > lstAssessmentItemId.size())
				totalItems = lstAssessmentItemId.size();
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
			Request request = getAssessmentItemRequestObject(map, "ItemSet",
					contentId, ContentAPIParams.assessment_item_set.name());
			if (null != request) {
				Response response = assessmentMgr.createItemSet(taxonomyId,
						request);
				LOGGER.info("Create Item | Response: " + response);
				return response;
			}
		}
		return null;
	}

	private String[] qlevels = new String[] { "EASY", "MEDIUM", "DIFFICULT",
			"RARE" };
	private List<String> qlevelList = Arrays.asList(qlevels);

	private Request getAssessmentItemRequestObject(Map<String, Object> map,
			String objectType, String contentId, String param) {
		if (null != objectType && null != map) {
			Map<String, Object> reqMap = new HashMap<String, Object>();
			Map<String, Object> assessMap = new HashMap<String, Object>();
			Map<String, Object> requestMap = new HashMap<String, Object>();
			reqMap.put(ContentAPIParams.objectType.name(), objectType);
			reqMap.put(ContentAPIParams.metadata.name(), map);
			String identifier = null;
			if (null != map.get("qid")) {
				String qid = (String) map.get("qid");
				if (StringUtils.isNotBlank(qid))
					identifier = qid;
			}
			if (StringUtils.isBlank(identifier)) {
				if (null != map.get(ContentAPIParams.identifier.name())) {
					String id = (String) map.get(ContentAPIParams.identifier
							.name());
					if (StringUtils.isNotBlank(id))
						identifier = id;
				}
			}
			if (StringUtils.isNotBlank(identifier)) {
				reqMap.put(ContentAPIParams.identifier.name(), identifier);
				map.put("code", identifier);
				map.put("name", identifier);
			} else {
				map.put("name", "Assessment Item");
				map.put("code", "item_" + RandomUtils.nextInt(1, 10000));
			}
			map.put("usedIn", contentId);
			String qlevel = (String) map.get("qlevel");
			if (StringUtils.isBlank(qlevel)) {
				qlevel = "MEDIUM";
			} else {
				if (!qlevelList.contains(qlevel))
					qlevel = "MEDIUM";
			}
			map.put("qlevel", qlevel);
			assessMap.put(param, reqMap);
			requestMap.put(ContentAPIParams.skipValidations.name(), true);
			requestMap.put(ContentAPIParams.request.name(), assessMap);
			return getRequestObjectForAssessmentMgr(requestMap, param);
		}
		return null;
	}

	private Request getRequestObjectForAssessmentMgr(
			Map<String, Object> requestMap, String param) {
		Request request = getRequest(requestMap);
		Map<String, Object> map = request.getRequest();
		if (null != map && !map.isEmpty()) {
			try {
				Object obj = map.get(param);
				if (null != obj) {
					Node item = (Node) mapper.convertValue(obj, Node.class);
					request.put(param, item);
					request.put(ContentAPIParams.skipValidations.name(), true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return request;
	}

	@SuppressWarnings("unchecked")
	private List<String> createRelation(String taxonomyId,
			Map<String, Object> mapRelation, List<Relation> outRelations) {
		if (null != mapRelation && !mapRelation.isEmpty()) {
			List<String> lstResponse = new ArrayList<String>();
			for (Entry<String, Object> entry : mapRelation.entrySet()) {
				List<Map<String, Object>> lstConceptMap = (List<Map<String, Object>>) entry
						.getValue();
				if (null != lstConceptMap && !lstConceptMap.isEmpty()) {
					for (Map<String, Object> conceptMap : lstConceptMap) {
						String conceptId = (String) conceptMap
								.get(ContentAPIParams.identifier.name());
						Response response = addRelation(taxonomyId,
								entry.getKey(),
								RelationTypes.ASSOCIATED_TO.relationName(),
								conceptId);
						lstResponse.add(response.getResult().toString());
						Relation outRel = new Relation(null,
								RelationTypes.ASSOCIATED_TO.relationName(),
								conceptId);
						outRelations.add(outRel);
					}
				}
			}
			return lstResponse;
		}
		return null;
	}

	public Response addRelation(String taxonomyId, String objectId1,
			String relation, String objectId2) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(
					ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
					"Invalid taxonomy Id");
		if (StringUtils.isBlank(objectId1) || StringUtils.isBlank(objectId2))
			throw new ClientException(
					ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
					"Object Id is blank");
		if (StringUtils.isBlank(relation))
			throw new ClientException(
					ContentErrorCodes.ERR_INVALID_RELATION_NAME.name(),
					"Relation name is blank");
		Request request = getRequest(taxonomyId,
				GraphEngineManagers.GRAPH_MANAGER, "createRelation");
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
					RequestParams params = (RequestParams) mapper.convertValue(
							reqParams, RequestParams.class);
					request.setParams(params);
				} catch (Exception e) {
				}
			}
			Object requestObj = requestMap.get("request");
			if (null != requestObj) {
				try {
					String strRequest = mapper.writeValueAsString(requestObj);
					Map<String, Object> map = mapper.readValue(strRequest,
							Map.class);
					if (null != map && !map.isEmpty())
						request.setRequest(map);
				} catch (Exception e) {
				}
			}
		}
		return request;
	}

	private List<File> getControllerFileList(String contentExtractedPath) {
		try {
			String indexFile = contentExtractedPath + File.separator
					+ "index.ecml";
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(indexFile);
			NodeList attrList = doc.getElementsByTagName("controller");
			List<File> lstControllerFile = new ArrayList<File>();
			for (int i = 0; i < attrList.getLength(); i++) {
				Element controller = (Element) attrList.item(i);
				if (controller.getAttribute("type").equalsIgnoreCase("Items")) {
					controller = (Element) attrList.item(i);
					if (!StringUtils.isBlank(controller.getAttribute("id"))) {
						lstControllerFile.add(new File(contentExtractedPath
								+ File.separator + "items" + File.separator
								+ controller.getAttribute("id") + ".json"));
					}
				}
			}
			return lstControllerFile;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
