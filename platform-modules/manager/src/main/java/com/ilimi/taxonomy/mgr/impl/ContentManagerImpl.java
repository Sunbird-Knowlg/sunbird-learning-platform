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
import org.ekstep.common.slugs.Slug;
import org.ekstep.common.util.AWSUploader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.exception.GraphDACErrorCodes;
import com.ilimi.graph.dac.model.Filter;
import com.ilimi.graph.dac.model.MetadataCriterion;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.Sort;
import com.ilimi.graph.dac.model.TagCriterion;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.DefinitionDTO;
import com.ilimi.taxonomy.content.ContentMimeTypeFactory;
import com.ilimi.taxonomy.content.common.ContentConfigurationConstants;
import com.ilimi.taxonomy.content.pipeline.initializer.InitializePipeline;
import com.ilimi.taxonomy.dto.ContentDTO;
import com.ilimi.taxonomy.dto.ContentSearchCriteria;
import com.ilimi.taxonomy.enums.ContentAPIParams;
import com.ilimi.taxonomy.enums.ContentErrorCodes;
import com.ilimi.taxonomy.mgr.IContentManager;
import com.ilimi.taxonomy.mgr.IMimeTypeManager;
import com.ilimi.taxonomy.util.Content2VecUtil;

/**
 * The Class <code>ContentManagerImpl</code> is the implementation of
 * <code>IContentManager</code> for all the operation including CRUD operation
 * and High Level Operations. This implementation intern calls for
 * <code>IMimeTypeManager</code> implementation based on the
 * <code>MimeType</code>. For <code>Bundle</code> implementation it is directly
 * backed by Content Work-Flow Pipeline and other High Level implementation is
 * backed by the implementation of <code>IMimeTypeManager</code>.
 * 
 * @author Azhar
 * 
 * @see IContentManager
 */
@Component
public class ContentManagerImpl extends BaseManager implements IContentManager {

	@Autowired
	private ContentMimeTypeFactory contentFactory;

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(ContentManagerImpl.class.getName());

	/**
	 * The Default fields which should come as output of any search operation.
	 */
	private static final List<String> DEFAULT_FIELDS = new ArrayList<String>();

	/** The Default value of the 'status' property. */
	private static final List<String> DEFAULT_STATUS = new ArrayList<String>();

	/** The mapper. */
	private ObjectMapper mapper = new ObjectMapper();

	static {
		DEFAULT_FIELDS.add("identifier");
		DEFAULT_FIELDS.add("name");
		DEFAULT_FIELDS.add("description");
		DEFAULT_FIELDS.add(SystemProperties.IL_UNIQUE_ID.name());

		DEFAULT_STATUS.add("Live");
	}

	/** The default public AWS Bucket Name. */
	private static final String bucketName = "ekstep-public";

	/** The Disk Location where the operations on file will take place. */
	private static final String tempFileLocation = "/data/contentBundle/";

	/** Default name of URL field */
	protected static final String URL_FIELD = "URL";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.taxonomy.mgr.IContentManager#create(java.lang.String,
	 * java.lang.String, com.ilimi.common.dto.Request)
	 */
	@Override
	public Response create(String taxonomyId, String objectType, Request request) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		if (StringUtils.isBlank(objectType))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "ObjectType is blank");
		Node item = (Node) request.get(ContentAPIParams.content.name());
		if (null == item)
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT.name(),
					objectType + " Object is blank");
		item.setObjectType(objectType);
		Request validateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "validateNode");
		validateReq.put(GraphDACParams.node.name(), item);
		Response validateRes = getResponse(validateReq, LOGGER);
		if (checkError(validateRes)) {
			return validateRes;
		} else {
			Request createReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "createDataNode");
			createReq.put(GraphDACParams.node.name(), item);
			Response createRes = getResponse(createReq, LOGGER);
			return createRes;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.taxonomy.mgr.IContentManager#findAll(java.lang.String,
	 * java.lang.String, java.lang.Integer, java.lang.Integer,
	 * java.lang.String[])
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Response findAll(String taxonomyId, String objectType, Integer offset, Integer limit, String[] gfields) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		if (StringUtils.isBlank(objectType))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_INVALID_OBJECT_TYPE.name(), "Object Type is blank");
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
			Request countReq = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getNodesCount",
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.taxonomy.mgr.IContentManager#find(java.lang.String,
	 * java.lang.String, java.lang.String[])
	 */
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

	/**
	 * Gets the data node.
	 *
	 * @param taxonomyId
	 *            the taxonomy id
	 * @param id
	 *            the id
	 * @return the data node
	 */
	private Response getDataNode(String taxonomyId, String id) {
		Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
				GraphDACParams.node_id.name(), id);
		request.put(GraphDACParams.get_tags.name(), true);
		Response getNodeRes = getResponse(request, LOGGER);
		return getNodeRes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.taxonomy.mgr.IContentManager#update(java.lang.String,
	 * java.lang.String, java.lang.String, com.ilimi.common.dto.Request)
	 */
	@Override
	public Response update(String id, String taxonomyId, String objectType, Request request) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		if (StringUtils.isBlank(id))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
					"Content Object Id is blank");
		if (StringUtils.isBlank(objectType))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "ObjectType is blank");
		Node item = (Node) request.get(ContentAPIParams.content.name());
		if (null == item)
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT.name(),
					objectType + " Object is blank");
		item.setIdentifier(id);
		item.setObjectType(objectType);
		Request validateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "validateNode");
		validateReq.put(GraphDACParams.node.name(), item);
		Response validateRes = getResponse(validateReq, LOGGER);
		if (checkError(validateRes)) {
			return validateRes;
		} else {
			Request updateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
			updateReq.put(GraphDACParams.node.name(), item);
			updateReq.put(GraphDACParams.node_id.name(), item.getIdentifier());
			Response updateRes = getResponse(updateReq, LOGGER);
			return updateRes;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.taxonomy.mgr.IContentManager#delete(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Response delete(String id, String taxonomyId) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		if (StringUtils.isBlank(id))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
					"Content Object Id is blank");
		Request request = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "deleteDataNode",
				GraphDACParams.node_id.name(), id);
		return getResponse(request, LOGGER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.taxonomy.mgr.IContentManager#upload(java.lang.String,
	 * java.lang.String, java.io.File, java.lang.String)
	 */
	@SuppressWarnings("unused")
	@Override
	public Response upload(String contentId, String taxonomyId, File uploadedFile, String folder) {
		LOGGER.debug("Content ID: " + contentId);
		LOGGER.debug("Graph ID: " + taxonomyId);
		LOGGER.debug("Uploaded File: " + uploadedFile.getAbsolutePath());
		LOGGER.debug("Upload Location: " + folder);

		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank.");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
					"Content Object Id is blank.");
		if (null == uploadedFile) {
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_UPLOAD_OBJECT.name(),
					"Upload file is blank.");
		}

		LOGGER.info("Fetching the Content Node. | [Content ID: " + contentId + "]");
		Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
				GraphDACParams.node_id.name(), contentId);
		request.put(GraphDACParams.get_tags.name(), true);
		Response getNodeRes = getResponse(request, LOGGER);
		Response response = copyResponse(getNodeRes);
		if (checkError(response)) {
			return response;
		}
		Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
		LOGGER.debug("Node: ", node);

		String mimeType = (String) node.getMetadata().get("mimeType");
		if (StringUtils.isBlank(mimeType)) {
			mimeType = "assets";
		}
		LOGGER.info("Mime-Type: " + mimeType + " | [Content ID: " + contentId + "]");

		LOGGER.info("Fetching Mime-Type Factory For Mime-Type: " + mimeType + " | [Content ID: " + contentId + "]");
		IMimeTypeManager mimeTypeManager = contentFactory.getImplForService(mimeType);
		Response res = mimeTypeManager.upload(node, uploadedFile, folder);
		if (null != uploadedFile && uploadedFile.exists()) {
			try {
				LOGGER.info("Cleanup - Deleting Uploaded File. | [Content ID: " + contentId + "]");
				uploadedFile.delete();
			} catch (Exception e) {
				LOGGER.error("Something Went Wrong While Deleting the Uploaded File. | [Content ID: " + contentId + "]",
						e);
			}
		}

		LOGGER.info("Returning Response.");
		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.taxonomy.mgr.IContentManager#listContents(java.lang.String,
	 * java.lang.String, com.ilimi.common.dto.Request)
	 */
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
			List<List<Node>> nodes = (List<List<Node>>) response.get(ContentAPIParams.contents.name());
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
									String[] arr = (String[]) definitions.get(subject).getMetadata().get(PARAM_FIELDS);
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
				ttl = (Integer) definitions.get(TaxonomyManagerImpl.taxonomyIds[0]).getMetadata().get(PARAM_TTL);
			if (null == ttl || ttl.intValue() <= 0)
				ttl = DEFAULT_TTL;
			listRes.put(PARAM_TTL, ttl);
			return listRes;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.taxonomy.mgr.IContentManager#bundle(com.ilimi.common.dto.
	 * Request, java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Response bundle(Request request, String taxonomyId, String version) {
		LOGGER.debug("Request Object: ", request);
		LOGGER.debug("Graph ID: " + taxonomyId);
		LOGGER.debug("Version: " + version);

		String bundleFileName = (String) request.get("file_name");
		List<String> contentIds = (List<String>) request.get("content_identifiers");
		LOGGER.info("Bundle File Name: " + bundleFileName);
		LOGGER.info("Total No. of Contents: " + contentIds.size());
		if (contentIds.size() > 1 && StringUtils.isBlank(bundleFileName))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_INVALID_BUNDLE_CRITERIA.name(),
					"ECAR file name should not be blank");

		LOGGER.info("Fetching all the Nodes.");
		Response response = searchNodes(taxonomyId, contentIds);
		Response listRes = copyResponse(response);
		if (checkError(response)) {
			LOGGER.info("Erroneous Response.");
			return response;
		} else {
			List<Object> list = (List<Object>) response.get(ContentAPIParams.contents.name());
			List<Node> nodes = new ArrayList<Node>();
			if (null != list && !list.isEmpty()) {
				LOGGER.info("Iterating Over the List.");
				for (Object obj : list) {
					List<Node> nodelist = (List<Node>) obj;
					if (null != nodelist && !nodelist.isEmpty())
						nodes.addAll(nodelist);
				}
				if (nodes.size() == 1 && StringUtils.isBlank(bundleFileName))
					bundleFileName = (String) nodes.get(0).getMetadata().get(ContentAPIParams.name.name()) + "_"
							+ System.currentTimeMillis() + "_" + (String) nodes.get(0).getIdentifier();
			}
			bundleFileName = Slug.makeSlug(bundleFileName, true);
			String fileName = bundleFileName + ".ecar";
			LOGGER.info("Bundle File Name: " + bundleFileName);

			LOGGER.info("Preparing the Parameter Map for 'Bundle' Pipeline.");
			InitializePipeline pipeline = new InitializePipeline(tempFileLocation, "node");
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(ContentAPIParams.nodes.name(), nodes);
			parameterMap.put(ContentAPIParams.bundleFileName.name(), fileName);
			parameterMap.put(ContentAPIParams.contentIdList.name(), contentIds);
			parameterMap.put(ContentAPIParams.manifestVersion.name(),
					ContentConfigurationConstants.DEFAULT_CONTENT_MANIFEST_VERSION);

			LOGGER.info("Calling Content Workflow 'Bundle' Pipeline.");
			listRes.getResult().putAll(pipeline.init(ContentAPIParams.bundle.name(), parameterMap).getResult());

			LOGGER.info("Returning Response.");
			return listRes;
		}
	}

	/**
	 * Search nodes.
	 *
	 * @param taxonomyId
	 *            the taxonomy id
	 * @param contentIds
	 *            the content ids
	 * @return the response
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.taxonomy.mgr.IContentManager#search(java.lang.String,
	 * java.lang.String, com.ilimi.common.dto.Request)
	 */
	@SuppressWarnings("unchecked")
	public Response search(String taxonomyId, String objectType, Request request) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank.");
		ContentSearchCriteria criteria = (ContentSearchCriteria) request.get(ContentAPIParams.search_criteria.name());
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
						if (null != definitions.get(subject) && null != definitions.get(subject).getMetadata()) {
							String[] arr = (String[]) definitions.get(subject).getMetadata().get(PARAM_FIELDS);
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

	/**
	 * Gets the definition.
	 *
	 * @param taxonomyId
	 *            the taxonomy id
	 * @param objectType
	 *            the object type
	 * @return the definition
	 */
	private DefinitionDTO getDefinition(String taxonomyId, String objectType) {
		Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
				GraphDACParams.object_type.name(), objectType);
		Response response = getResponse(request, LOGGER);
		if (!checkError(response)) {
			DefinitionDTO definition = (DefinitionDTO) response.get(GraphDACParams.definition_node.name());
			return definition;
		}
		return null;
	}

	/**
	 * Gets the contents list request.
	 *
	 * @param request
	 *            the request
	 * @param taxonomyId
	 *            the taxonomy id
	 * @param objectType
	 *            the object type
	 * @param definition
	 *            the definition
	 * @return the contents list request
	 */
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
		MetadataCriterion mc = MetadataCriterion
				.create(Arrays.asList(new Filter(PARAM_STATUS, SearchConditions.OP_IN, statusList)));

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
						mc.addFilter(new Filter(entry.getKey(), SearchConditions.OP_LIKE, val.toString()));
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

	/**
	 * Sets the limit.
	 *
	 * @param request
	 *            the request
	 * @param sc
	 *            the sc
	 * @param definition
	 *            the definition
	 */
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

	/**
	 * Gets the list.
	 *
	 * @param mapper
	 *            the mapper
	 * @param object
	 *            the object
	 * @param returnList
	 *            the return list
	 * @return the list
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.taxonomy.mgr.IContentManager#optimize(java.lang.String,
	 * java.lang.String)
	 */
	public Response optimize(String taxonomyId, String contentId) {
		LOGGER.debug("Graph ID: " + taxonomyId);
		LOGGER.debug("Content ID: " + contentId);

		Response response = new Response();
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_ID.name(), "Content Id is blank");
		Response responseNode = getDataNode(taxonomyId, contentId);
		if (checkError(responseNode))
			throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
					"Content not found with id: " + contentId);

		Node node = (Node) responseNode.get(GraphDACParams.node.name());
		LOGGER.debug("Got Node: ", node);

		String status = (String) node.getMetadata().get(ContentAPIParams.status.name());
		LOGGER.info("Content Status: " + status);
		if (!StringUtils.equalsIgnoreCase(ContentAPIParams.Live.name(), status))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(),
					"UnPublished content cannot be optimized");

		String downloadUrl = (String) node.getMetadata().get(ContentAPIParams.downloadUrl.name());
		LOGGER.info("Download Url: " + downloadUrl);
		if (StringUtils.isBlank(downloadUrl))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(),
					"ECAR file not available for content");

		if (!StringUtils.equalsIgnoreCase(ContentAPIParams.ecar.name(), FilenameUtils.getExtension(downloadUrl)))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(),
					"Content package is not an ECAR file");

		String optStatus = (String) node.getMetadata().get(ContentAPIParams.optStatus.name());
		LOGGER.info("Optimization Process Status: " + optStatus);
		if (StringUtils.equalsIgnoreCase(ContentAPIParams.Processing.name(), optStatus))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(),
					"Content optimization is in progress. Please try after the current optimization is complete");

		node.getMetadata().put(ContentAPIParams.optStatus.name(), ContentAPIParams.Processing.name());
		updateNode(node);
		Optimizr optimizr = new Optimizr();
		try {
			LOGGER.info("Invoking the Optimizer For Content Id: " + contentId);
			File minEcar = optimizr.optimizeECAR(downloadUrl);
			LOGGER.info("Optimized File: " + minEcar.getName() + " | [Content Id: " + contentId + "]");

			String folder = getFolderName(downloadUrl);
			LOGGER.info("Folder Name: " + folder + " | [Content Id: " + contentId + "]");

			String[] arr = AWSUploader.uploadFile(bucketName, folder, minEcar);
			response.put("url", arr[1]);
			LOGGER.info("URL: " + arr[1] + " | [Content Id: " + contentId + "]");

			LOGGER.info("Updating the Optimization Status. | [Content Id: " + contentId + "]");
			node.getMetadata().put(ContentAPIParams.optStatus.name(), "Complete");
			updateNode(node);
			LOGGER.info("Node Updated. | [Content Id: " + contentId + "]");

			LOGGER.info("Directory Cleanup. | [Content Id: " + contentId + "]");
			FileUtils.deleteDirectory(minEcar.getParentFile());
		} catch (Exception e) {
			node.getMetadata().put(ContentAPIParams.optStatus.name(), "Error");
			updateNode(node);
			response = ERROR(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(), e.getMessage(), ResponseCode.SERVER_ERROR);
		}
		return response;
	}

	/**
	 * Gets the folder name.
	 *
	 * @param url
	 *            the url
	 * @return the folder name
	 */
	private String getFolderName(String url) {
		try {
			String s = url.substring(0, url.lastIndexOf('/'));
			return s.substring(s.lastIndexOf('/') + 1);
		} catch (Exception e) {
		}
		return "";
	}

	/**
	 * Update node.
	 *
	 * @param node
	 *            the node
	 * @return the response
	 */
	protected Response updateNode(Node node) {
		LOGGER.debug("[updateNode] | Node: ", node);

		LOGGER.info("Getting Update Node Request For Node ID: " + node.getIdentifier());
		Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "updateDataNode");
		updateReq.put(GraphDACParams.node.name(), node);
		updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());

		LOGGER.info("Updating the Node ID: " + node.getIdentifier());
		Response updateRes = getResponse(updateReq, LOGGER);

		LOGGER.info("Returning Node Update Response.");
		return updateRes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.taxonomy.mgr.IContentManager#publish(java.lang.String,
	 * java.lang.String)
	 */
	public Response publish(String taxonomyId, String contentId) {
		LOGGER.debug("Graph ID: " + taxonomyId);
		LOGGER.debug("Content ID: " + contentId);

		Response response = new Response();
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_ID.name(), "Content Id is blank");
		Response responseNode = getDataNode(taxonomyId, contentId);
		if (checkError(responseNode))
			throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
					"Content not found with id: " + contentId);

		Node node = (Node) responseNode.get(GraphDACParams.node.name());
		LOGGER.debug("Got Node: ", node);

		String mimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
		if (StringUtils.isBlank(mimeType)) {
			mimeType = "assets";
		}
		LOGGER.info("Mime-Type" + mimeType + " | [Content ID: " + contentId + "]");

		String prevState = (String) node.getMetadata().get(ContentAPIParams.status.name());

		LOGGER.info("Getting Mime-Type Manager Factory. | [Content ID: " + contentId + "]");
		IMimeTypeManager mimeTypeManager = contentFactory.getImplForService(mimeType);

		try {
			response = mimeTypeManager.publish(node);
			String contentType = (String) node.getMetadata().get("contentType");
			if (!checkError(response) && !StringUtils.equalsIgnoreCase("Asset", contentType)) {
				node.getMetadata().put("prevState", prevState);

				LOGGER.info("Generating Telemetry Event. | [Content ID: " + contentId + "]");
				String event = LogTelemetryEventUtil.logContentLifecycleEvent(contentId, node.getMetadata());

				LOGGER.info("Invoking Content to Vector. | [Content ID: " + contentId + "]");
				Content2VecUtil.invokeContent2Vec(contentId, event);
			}
		} catch (ClientException e) {
			throw e;
		} catch (ServerException e) {
			throw e;
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_PUBLISH.name(), e.getMessage());
		}

		LOGGER.info("Returning 'Response' Object.");
		return response;
	}

	/**
	 * Adds the relation.
	 *
	 * @param taxonomyId
	 *            the taxonomy id
	 * @param objectId1
	 *            the object id 1
	 * @param relation
	 *            the relation
	 * @param objectId2
	 *            the object id 2
	 * @return the response
	 */
	public Response addRelation(String taxonomyId, String objectId1, String relation, String objectId2) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Invalid taxonomy Id");
		if (StringUtils.isBlank(objectId1) || StringUtils.isBlank(objectId2))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(), "Object Id is blank");
		if (StringUtils.isBlank(relation))
			throw new ClientException(ContentErrorCodes.ERR_INVALID_RELATION_NAME.name(), "Relation name is blank");
		Request request = getRequest(taxonomyId, GraphEngineManagers.GRAPH_MANAGER, "createRelation");
		request.put(GraphDACParams.start_node_id.name(), objectId1);
		request.put(GraphDACParams.relation_type.name(), relation);
		request.put(GraphDACParams.end_node_id.name(), objectId2);
		Response response = getResponse(request, LOGGER);
		return response;
	}

	/**
	 * Gets the request.
	 *
	 * @param requestMap
	 *            the request map
	 * @return the request
	 */
	@SuppressWarnings("unchecked")
	protected Request getRequest(Map<String, Object> requestMap) {
		LOGGER.debug("Request Map: ", requestMap);

		Request request = new Request();
		if (null != requestMap && !requestMap.isEmpty()) {
			String id = (String) requestMap.get("id");
			String ver = (String) requestMap.get("ver");
			String ts = (String) requestMap.get("ts");

			LOGGER.info("Setting 'id': " + id);
			LOGGER.info("Setting 'ver': " + ver);
			LOGGER.info("Setting 'ts': " + ts);
			request.setId(id);
			request.setVer(ver);
			request.setTs(ts);

			LOGGER.info("Setting 'params'");
			Object reqParams = requestMap.get("params");
			if (null != reqParams) {
				try {
					RequestParams params = (RequestParams) mapper.convertValue(reqParams, RequestParams.class);
					request.setParams(params);
				} catch (Exception e) {
				}
			}

			LOGGER.info("Creating Request Object.");
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