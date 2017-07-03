package com.ilimi.taxonomy.mgr.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.optimizr.Optimizr;
import org.ekstep.common.slugs.Slug;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.content.dto.ContentSearchCriteria;
import org.ekstep.content.mimetype.mgr.IMimeTypeManager;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.content.util.ContentMimeTypeFactoryUtil;
import org.ekstep.contentstore.util.ContentStoreOperations;
import org.ekstep.contentstore.util.ContentStoreParams;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.learning.common.enums.LearningActorNames;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.NodeDTO;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.common.mgr.ConvertGraphNode;
import com.ilimi.common.mgr.ConvertToGraphNode;
import com.ilimi.common.router.RequestRouterPool;
import com.ilimi.common.util.LogTelemetryEventUtil;
import com.ilimi.graph.common.DateUtils;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Filter;
import com.ilimi.graph.dac.model.MetadataCriterion;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.DefinitionDTO;
import com.ilimi.graph.model.node.MetadataDefinition;
import com.ilimi.taxonomy.enums.TaxonomyAPIParams;
import com.ilimi.taxonomy.mgr.IContentManager;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;

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

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(ContentManagerImpl.class.getName());

	/** The Disk Location where the operations on file will take place. */
	private static final String tempFileLocation = "/data/contentBundle/";

	/** The Default Manifest Version */
	private static final String DEFAULT_CONTENT_MANIFEST_VERSION = "1.2";

	/**
	 * The Default 'ContentImage' Object Suffix (Content_Object_Identifier +
	 * ".img")
	 */
	private static final String DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX = ".img";

	/**
	 * Content Image Object Type
	 */
	private static final String CONTENT_IMAGE_OBJECT_TYPE = "ContentImage";
	
	/**
	 * Content Object Type
	 */
	private static final String CONTENT_OBJECT_TYPE = "Content";
	
	private static final String GRAPH_ID = "domain";

	/**
	 * Is Content Image Object flag property key
	 */
	@SuppressWarnings("unused")
	private static final String IS_IMAGE_OBJECT_FLAG_KEY = "isImageObject";

	/** Default name of URL field */
	protected static final String URL_FIELD = "URL";

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
	 * @see com.ilimi.taxonomy.mgr.IContentManager#upload(java.lang.String,
	 * java.lang.String, java.io.File, java.lang.String)
	 */
	@SuppressWarnings("unused")
	@Override
	public Response upload(String contentId, String taxonomyId, File uploadedFile) {
		LOGGER.debug("Content ID: " + contentId);
		LOGGER.debug("Graph ID: " + taxonomyId);
		LOGGER.debug("Uploaded File: " + uploadedFile.getAbsolutePath());

		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank.");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
					"Content Object Id is blank.");
		if (null == uploadedFile)
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_UPLOAD_OBJECT.name(),
					"Upload file is blank.");
		if (StringUtils.endsWithIgnoreCase(contentId, DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX))
			throw new ClientException(ContentErrorCodes.OPERATION_DENIED.name(),
					"Invalid Content Identifier. | [Content Identifier does not Exists.]");

		Node node = getNodeForOperation(taxonomyId, contentId);
		LOGGER.debug("Node: ", node);

		String mimeType = (String) node.getMetadata().get("mimeType");
		if (StringUtils.isBlank(mimeType)) {
			mimeType = "assets";
		}
		LOGGER.info("Mime-Type: " + mimeType + " | [Content ID: " + contentId + "]");

		LOGGER.info("Fetching Mime-Type Factory For Mime-Type: " + mimeType + " | [Content ID: " + contentId + "]");
		IMimeTypeManager mimeTypeManager = ContentMimeTypeFactoryUtil.getImplForService(mimeType);
		Response res = mimeTypeManager.upload(contentId, node, uploadedFile, false);
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
		if(StringUtils.endsWith(res.getResult().get("node_id").toString(), ".img")){
			 String identifier = (String)res.getResult().get("node_id");
			 String new_identifier = identifier.replace(".img", "");
			 LOGGER.info("replacing image id with content id in response" + identifier + new_identifier);
			 res.getResult().replace("node_id", identifier, new_identifier);
		}
		return res;
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
			List<Node> imageNodes = new ArrayList<Node>();
			if (null != list && !list.isEmpty()) {
				LOGGER.info("Iterating Over the List.");
				for (Object obj : list) {
					List<Node> nodelist = (List<Node>) obj;
					if (null != nodelist && !nodelist.isEmpty())
						nodes.addAll(nodelist);
				}
				for (Node node : nodes) {
					String contentImageId = getContentImageIdentifier(node.getIdentifier());
					Response getNodeResponse = getDataNode(taxonomyId, contentImageId);
					if (!checkError(getNodeResponse)) {
						node = (Node) getNodeResponse.get(GraphDACParams.node.name());
					}
					String body = getContentBody(node.getIdentifier());
					node.getMetadata().put(ContentAPIParams.body.name(), body);
					imageNodes.add(node);
					LOGGER.debug("Body fetched from content store");
				}
				if (imageNodes.size() == 1 && StringUtils.isBlank(bundleFileName))
					bundleFileName = (String) imageNodes.get(0).getMetadata().get(ContentAPIParams.name.name()) + "_"
							+ System.currentTimeMillis() + "_" + (String) imageNodes.get(0).getIdentifier();
			}
			bundleFileName = Slug.makeSlug(bundleFileName, true);
			String fileName = bundleFileName + ".ecar";
			LOGGER.info("Bundle File Name: " + bundleFileName);

			LOGGER.info("Preparing the Parameter Map for 'Bundle' Pipeline.");
			InitializePipeline pipeline = new InitializePipeline(tempFileLocation, "node");
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(ContentAPIParams.nodes.name(), imageNodes);
			parameterMap.put(ContentAPIParams.bundleFileName.name(), fileName);
			parameterMap.put(ContentAPIParams.contentIdList.name(), contentIds);
			parameterMap.put(ContentAPIParams.manifestVersion.name(), DEFAULT_CONTENT_MANIFEST_VERSION);

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

		Node node = getNodeForOperation(taxonomyId, contentId);
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
		updateDataNode(node);
		Optimizr optimizr = new Optimizr();
		try {
			LOGGER.info("Invoking the Optimizer For Content Id: " + contentId);
			File minEcar = optimizr.optimizeECAR(downloadUrl);
			LOGGER.info("Optimized File: " + minEcar.getName() + " | [Content Id: " + contentId + "]");

			String folder = getFolderName(downloadUrl);
			LOGGER.info("Folder Name: " + folder + " | [Content Id: " + contentId + "]");

			String[] arr = AWSUploader.uploadFile(folder, minEcar);
			response.put("url", arr[1]);
			LOGGER.info("URL: " + arr[1] + " | [Content Id: " + contentId + "]");

			LOGGER.info("Updating the Optimization Status. | [Content Id: " + contentId + "]");
			node.getMetadata().put(ContentAPIParams.optStatus.name(), "Complete");
			updateDataNode(node);
			LOGGER.info("Node Updated. | [Content Id: " + contentId + "]");

			LOGGER.info("Directory Cleanup. | [Content Id: " + contentId + "]");
			FileUtils.deleteDirectory(minEcar.getParentFile());
		} catch (Exception e) {
			node.getMetadata().put(ContentAPIParams.optStatus.name(), "Error");
			updateDataNode(node);
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
	private Response updateDataNode(Node node) {
		LOGGER.debug("[updateNode] | Node: ", node);
		Response response = new Response();
		if (null != node) {
			String contentId = node.getIdentifier();
			// Checking if Content Image Object is being Updated, then return
			// the Original Content Id
			if (BooleanUtils.isTrue((Boolean) node.getMetadata().get(TaxonomyAPIParams.isImageObject.name()))) {
				node.getMetadata().remove(TaxonomyAPIParams.isImageObject.name());
				node.setIdentifier(node.getIdentifier() + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX);
			}

			LOGGER.info("Getting Update Node Request For Node ID: " + node.getIdentifier());
			Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "updateDataNode");
			updateReq.put(GraphDACParams.node.name(), node);
			updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());

			LOGGER.info("Updating the Node ID: " + node.getIdentifier());
			response = getResponse(updateReq, LOGGER);

			response.put(TaxonomyAPIParams.node_id.name(), contentId);
			LOGGER.info("Returning Node Update Response.");
		}
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.taxonomy.mgr.IContentManager#publish(java.lang.String,
	 * java.lang.String)
	 */
	public Response publish(String taxonomyId, String contentId, Map<String, Object> requestMap) {
		LOGGER.debug("Graph ID: " + taxonomyId);
		LOGGER.debug("Content ID: " + contentId);

		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_ID.name(), "Content Id is blank");

		Response response = new Response();

		Node node = getNodeForOperation(taxonomyId, contentId);
		LOGGER.debug("Got Node: ", node);

		String body = getContentBody(node.getIdentifier());
		node.getMetadata().put(ContentAPIParams.body.name(), body);
		LOGGER.debug("Body fetched from content store");

		String mimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
		if (StringUtils.isBlank(mimeType)) {
			mimeType = "assets";
		}
		LOGGER.info("Mime-Type" + mimeType + " | [Content ID: " + contentId + "]");

		String publisher = null;
		if (null != requestMap && !requestMap.isEmpty()) {
			publisher = (String) requestMap.get("lastPublishedBy");
			node.getMetadata().putAll(requestMap);
		}
		if (StringUtils.isNotBlank(publisher)) {
			LOGGER.debug("LastPublishedBy: " + publisher);
			node.getMetadata().put(GraphDACParams.lastUpdatedBy.name(), publisher);
		} else {
			node.getMetadata().put("lastPublishedBy", null);
			node.getMetadata().put(GraphDACParams.lastUpdatedBy.name(), null);
		}
		LOGGER.info("Getting Mime-Type Manager Factory. | [Content ID: " + contentId + "]");
		IMimeTypeManager mimeTypeManager = ContentMimeTypeFactoryUtil.getImplForService(mimeType);

		try {
			response = mimeTypeManager.publish(contentId, node, true);
		} catch (ClientException e) {
			throw e;
		} catch (ServerException e) {
			throw e;
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_PUBLISH.name(), "Error occured during content publish");
		}

		LOGGER.info("Returning 'Response' Object.");
		if(StringUtils.endsWith(response.getResult().get("node_id").toString(), ".img")){
			 String identifier = (String)response.getResult().get("node_id");
			 String new_identifier = identifier.replace(".img", "");
			 LOGGER.info("replacing image id with content id in response" + identifier + new_identifier);
			 response.getResult().replace("node_id", identifier, new_identifier);
		}
		return response;
	}

	@Override
	public Response review(String taxonomyId, String contentId, Request request) {
		LOGGER.debug("Graph Id: ", taxonomyId);
		LOGGER.debug("Content Id: ", contentId);
		LOGGER.debug("Request: ", request);

		LOGGER.info("Validating The Input Parameter.");
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_ID.name(), "Content Id is blank");

		Response response = new Response();

		Node node = getNodeForOperation(taxonomyId, contentId);
		LOGGER.debug("Node: ", node);

		String body = getContentBody(node.getIdentifier());
		node.getMetadata().put(ContentAPIParams.body.name(), body);
		LOGGER.debug("Body Fetched From Content Store.");

		String mimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
		if (StringUtils.isBlank(mimeType)) {
			mimeType = "assets";
		}
		LOGGER.info("Mime-Type" + mimeType + " | [Content ID: " + contentId + "]");

		LOGGER.info("Getting Mime-Type Manager Factory. | [Content ID: " + contentId + "]");
		IMimeTypeManager mimeTypeManager = ContentMimeTypeFactoryUtil.getImplForService(mimeType);

		response = mimeTypeManager.review(contentId, node, false);

		LOGGER.debug("Returning 'Response' Object: ", response);
		return response;
	}

	@Override
	public Response getHierarchy(String graphId, String contentId, String mode) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Content Id: ", contentId);
		Node node = getContentNode(graphId, contentId, mode);

		LOGGER.info("Collecting Hierarchical Data For Content Id: " + node.getIdentifier());
		DefinitionDTO definition = getDefinition(graphId, node.getObjectType());
		Map<String, Object> map = getContentHierarchyRecursive(graphId, node, definition, mode);
		
		if(map.containsKey("identifier")) {
			String identifier = (String)map.get("identifier");
			if(StringUtils.endsWithIgnoreCase(identifier, ".img")){
			 String new_identifier = identifier.replace(".img", "");
			 LOGGER.info("replacing image id with content id in response" + identifier + new_identifier);
			 map.replace("identifier", identifier, new_identifier);
			}
		}
		Response response = new Response();
		response.put("content", map);
		response.setParams(getSucessStatus());
		return response;
	}
	
	@Override
	public Response getById(String graphId, String contentId, String mode) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Content Id: ", contentId);
		Response response = new Response();
		
		Node node = getContentNode(graphId, contentId, mode);
		
		LOGGER.info("Fetching the Data For Content Id: " + node.getIdentifier());
		DefinitionDTO definition = getDefinition(graphId, node.getObjectType());
		Map<String, Object> contentMap = ConvertGraphNode.convertGraphNode(node, graphId, definition, null);
		
		response.put("content", contentMap);
		response.setParams(getSucessStatus());
		return response;
	}

	protected ResponseParams getSucessStatus() {
		ResponseParams params = new ResponseParams();
		params.setErr("0");
		params.setStatus(StatusType.successful.name());
		params.setErrmsg("Operation successful");
		return params;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getContentHierarchyRecursive(String graphId, Node node, DefinitionDTO definition, String mode) {
		Map<String, Object> contentMap = ConvertGraphNode.convertGraphNode(node, graphId, definition, null);
		List<NodeDTO> children = (List<NodeDTO>) contentMap.get("children");
		if (null != children && !children.isEmpty()) {
			List<Map<String, Object>> childList = new ArrayList<Map<String, Object>>();
			for (NodeDTO dto : children) {
				Node childNode = getContentNode(graphId, dto.getIdentifier(), mode);
				Map<String, Object> childMap = getContentHierarchyRecursive(graphId, childNode, definition, mode);
				childMap.put("index", dto.getIndex());
				childList.add(childMap);
			}
			contentMap.put("children", childList);
		} else {

		}
		return contentMap;
	}

	private Node getContentNode(String graphId, String contentId, String mode) {
		
		if (StringUtils.equalsIgnoreCase("edit", mode)) {
			String contentImageId = getContentImageIdentifier(contentId);
			Response responseNode = getDataNode(graphId, contentImageId);
			if (!checkError(responseNode)) {
				Node content = (Node) responseNode.get(GraphDACParams.node.name());
				LOGGER.debug("Got draft version of node: ", content);
				return content;
			}
		}
		Response responseNode = getDataNode(graphId, contentId);
		if (checkError(responseNode))
			throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
					"Content not found with id: " + contentId);

		Node content = (Node) responseNode.get(GraphDACParams.node.name());
		LOGGER.debug("Got Node: ", content);
		return content;
	}

	private DefinitionDTO getDefinition(String graphId, String objectType) {
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
				GraphDACParams.object_type.name(), objectType);
		Response response = getResponse(request, LOGGER);
		if (!checkError(response)) {
			DefinitionDTO definition = (DefinitionDTO) response.get(GraphDACParams.definition_node.name());
			return definition;
		}
		return null;
	}

	private String getContentBody(String contentId) {
		Request request = new Request();
		request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name());
		request.setOperation(ContentStoreOperations.getContentBody.name());
		request.put(ContentStoreParams.content_id.name(), contentId);
		Response response = makeLearningRequest(request, LOGGER);
		String body = (String) response.get(ContentStoreParams.body.name());
		return body;
	}
	
	private Response getContentProperties(String contentId, List<String> properties) {
		Request request = new Request();
		request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name());
		request.setOperation(ContentStoreOperations.getContentProperties.name());
		request.put(ContentStoreParams.content_id.name(), contentId);
		request.put(ContentStoreParams.properties.name(), properties);
		Response response = makeLearningRequest(request, LOGGER);
		return response;
	}
	
	private Response updateContentProperties(String contentId, Map<String, Object> properties) {
		Request request = new Request();
		request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name());
		request.setOperation(ContentStoreOperations.updateContentProperties.name());
		request.put(ContentStoreParams.content_id.name(), contentId);
		request.put(ContentStoreParams.properties.name(), properties);
		
		Response response = makeLearningRequest(request, LOGGER);
		return response;
	}

	/**
	 * Make a sync request to LearningRequestRouter
	 * 
	 * @param request
	 *            the request object
	 * @param logger
	 *            the logger object
	 * @return the LearningActor response
	 */
	private Response makeLearningRequest(Request request, Logger logger) {
		ActorRef router = LearningRequestRouterPool.getRequestRouter();
		try {
			Future<Object> future = Patterns.ask(router, request, RequestRouterPool.REQ_TIMEOUT);
			Object obj = Await.result(future, RequestRouterPool.WAIT_TIMEOUT.duration());
			if (obj instanceof Response) {
				Response response = (Response) obj;
				logger.info("Response Params: " + response.getParams() + " | Code: " + response.getResponseCode()
						+ " | Result: " + response.getResult().keySet());
				return response;
			} else {
				return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", e);
		}
	}

	private String getContentImageIdentifier(String contentId) {
		String contentImageId = "";
		if (StringUtils.isNotBlank(contentId)) {
			contentImageId = contentId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
			// TODO: Put below the Logic to read contentImageId from Cache
			// (Redis).
		}
		return contentImageId;
	}

	private Node getNodeForOperation(String taxonomyId, String contentId) {
		LOGGER.debug("Taxonomy Id: " + taxonomyId);
		LOGGER.debug("Content Id: " + contentId);

		LOGGER.info("Fetching Node for Operation for Content Id: " + contentId);
		Node node = new Node();

		String contentImageId = getContentImageIdentifier(contentId);
		LOGGER.info("Fetching the Content Node. | [Content ID: " + contentId + "]");

		LOGGER.debug("Fetching the Content Image Node for Content Id: " + contentId);
		Response response = getDataNode(taxonomyId, contentImageId);
		if (checkError(response)) {
			LOGGER.debug("Unable to Fetch Content Image Node for Content Id: " + contentId);

			LOGGER.debug("Trying to Fetch Content Node (Not Image Node) for Content Id: " + contentId);
			response = getDataNode(taxonomyId, contentId);

			LOGGER.info("Checking for Fetched Content Node (Not Image Node) for Content Id: " + contentId);
			if (checkError(response))
				throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_INVALID_CONTENT.name(),
						"Error! While Fetching the Content for Operation | [Content Id: " + contentId + "]");

			// Content Image Node is not Available so assigning the original
			// Content Node as node
			node = (Node) response.get(GraphDACParams.node.name());

			LOGGER.info("Fetched Content Node: ", node);
			String status = (String) node.getMetadata().get(TaxonomyAPIParams.status.name());
			if (StringUtils.isNotBlank(status) && (StringUtils.equalsIgnoreCase(TaxonomyAPIParams.Live.name(), status)
					|| StringUtils.equalsIgnoreCase(TaxonomyAPIParams.Flagged.name(), status)))
				node = createContentImageNode(taxonomyId, contentImageId, node);
		} else{
			// Content Image Node is Available so assigning it as node
			node = (Node) response.get(GraphDACParams.node.name());
			LOGGER.info("Getting Content Image Node and assigning it as node" + node.getIdentifier());
		}
		// Assigning the original 'identifier' to the Node
		//node.setIdentifier(contentId);

		LOGGER.info("Returning the Node for Operation with Identifier: " + node.getIdentifier());
		return node;
	}

	private Node createContentImageNode(String taxonomyId, String contentImageId, Node node) {
		LOGGER.debug("Taxonomy Id: " + taxonomyId);
		LOGGER.debug("Content Id: " + contentImageId);
		LOGGER.debug("Node: ", node);

		Node imageNode = new Node(taxonomyId, SystemNodeTypes.DATA_NODE.name(), CONTENT_IMAGE_OBJECT_TYPE);
		imageNode.setGraphId(taxonomyId);
		imageNode.setIdentifier(contentImageId);
		imageNode.setMetadata(node.getMetadata());
		imageNode.setInRelations(node.getInRelations());
		imageNode.setOutRelations(node.getOutRelations());
		imageNode.setTags(node.getTags());
		imageNode.getMetadata().put(TaxonomyAPIParams.status.name(), TaxonomyAPIParams.Draft.name());
		Response response = createDataNode(imageNode);
		if (checkError(response))
			throw new ServerException(TaxonomyErrorCodes.ERR_NODE_CREATION.name(),
					"Error! Something went wrong while performing the operation. | [Content Id: " + node.getIdentifier()
							+ "]");
		Response resp = getDataNode(taxonomyId, contentImageId);
		Node nodeData = (Node) resp.get(GraphDACParams.node.name());
		LOGGER.info("Returning Content Image Node Identifier"+ nodeData.getIdentifier());
		return nodeData;
	}

	private Response createDataNode(Node node) {
		LOGGER.debug("Node :", node);
		Response response = new Response();
		if (null != node) {
			Request request = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "createDataNode");
			request.put(GraphDACParams.node.name(), node);

			LOGGER.info("Creating the Node ID: " + node.getIdentifier());
			response = getResponse(request, LOGGER);
		}
		return response;
	}
	
	public Response createContent(Map<String, Object> map) {
		if (null == map)
			return ERROR("ERR_CONTENT_INVALID_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		DefinitionDTO definition = getDefinition(GRAPH_ID, CONTENT_OBJECT_TYPE);
		Object mimeType = map.get("mimeType");
		if (null != mimeType && StringUtils.isNotBlank(mimeType.toString())) {
			if (!StringUtils.equalsIgnoreCase("application/vnd.android.package-archive", mimeType.toString()))
				map.put("osId", "org.ekstep.quiz.app");
			Object contentType = map.get("contentType");
			if (null != contentType && StringUtils.isNotBlank(contentType.toString())) {
				if (StringUtils.equalsIgnoreCase("TextBookUnit", contentType.toString()))
					map.put("visibility", "Parent");
			}
			
			Map<String, Object> externalProps = new HashMap<String, Object>();
			List<String> externalPropsList = getExternalPropsList(definition);
			if (null != externalPropsList && !externalPropsList.isEmpty()) {
				for (String prop : externalPropsList) {
					if (null != map.get(prop))
						externalProps.put(prop, map.get(prop));
					map.remove(prop);
				}
			}
			
			if (StringUtils.equalsIgnoreCase("application/vnd.ekstep.plugin-archive", mimeType.toString())) {
				Object code = map.get("code");
				if (null == code || StringUtils.isBlank(code.toString()))
					return ERROR("ERR_PLUGIN_CODE_REQUIRED", "Unique code is mandatory for plugins", ResponseCode.CLIENT_ERROR);
				map.put("identifier", map.get("code"));
			}
			
			try {
				Node node = ConvertToGraphNode.convertToGraphNode(map, definition, null);
				node.setObjectType(CONTENT_OBJECT_TYPE);
				node.setGraphId(GRAPH_ID);
				Response response = createDataNode(node);
				if (checkError(response)) 
					return response;
				else {
					String contentId = (String) response.get(GraphDACParams.node_id.name());
					if (null != externalProps && !externalProps.isEmpty()) {
						Response externalPropsResponse = updateContentProperties(contentId, externalProps);
						if (checkError(externalPropsResponse))
							return externalPropsResponse;
					}
					return response;
				}
			} catch (Exception e) {
				return ERROR("ERR_CONTENT_SERVER_ERROR", "Internal error", ResponseCode.SERVER_ERROR);
			}
		} else {
			return ERROR("ERR_CONTENT_INVALID_CONTENT_MIMETYPE_TYPE", "Mime Type cannot be empty", ResponseCode.CLIENT_ERROR);
		}
	}
	
	@SuppressWarnings("unchecked")
	public Response updateContent(String contentId, Map<String, Object> map) throws Exception {
		if (null == map)
			return ERROR("ERR_CONTENT_INVALID_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		
		DefinitionDTO definition = getDefinition(GRAPH_ID, CONTENT_OBJECT_TYPE);
		String originalId = contentId;
		String objectType = CONTENT_OBJECT_TYPE;
		map.put("objectType", CONTENT_OBJECT_TYPE);
		map.put("identifier", contentId);
		
		boolean isImageObjectCreationNeeded = false;
		boolean imageObjectExists = false;
		
		String contentImageId = contentId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
		Response getNodeResponse = getDataNode(GRAPH_ID, contentImageId);
		if (checkError(getNodeResponse)) {
			LOGGER.info("Content image not found: " + contentImageId);
			isImageObjectCreationNeeded = true;
			getNodeResponse = getDataNode(GRAPH_ID, contentId);
			LOGGER.info("Content node response: " + getNodeResponse);
		} else
			imageObjectExists = true;
		
		if (checkError(getNodeResponse)) {
			LOGGER.info("Content not found: " + contentId);
			return getNodeResponse;
		}
		
		Map<String, Object> externalProps = new HashMap<String, Object>();
		List<String> externalPropsList = getExternalPropsList(definition);
		if (null != externalPropsList && !externalPropsList.isEmpty()) {
			for (String prop : externalPropsList) {
				if (null != map.get(prop))
					externalProps.put(prop, map.get(prop));
				map.remove(prop);
			}
		}
		
		Node graphNode = (Node) getNodeResponse.get(GraphDACParams.node.name());
		LOGGER.info("Graph node found: " + graphNode.getIdentifier());
		Map<String, Object> metadata = graphNode.getMetadata();
		String status = (String) metadata.get("status");
		boolean isReviewState = StringUtils.equalsIgnoreCase("Review", status);
		boolean isFlaggedReviewState = StringUtils.equalsIgnoreCase("FlagReview", status);
		boolean isFlaggedState = StringUtils.equalsIgnoreCase("Flagged", status);
		boolean isLiveState = StringUtils.equalsIgnoreCase("Live", status);
		boolean logEvent = false;
		Object inputStatus = map.get("status");
		if (null != inputStatus) {
			boolean updateToReviewState = StringUtils.equalsIgnoreCase("Review", inputStatus.toString());
			boolean updateToFlagReviewState = StringUtils.equalsIgnoreCase("FlagReview", inputStatus.toString());
			if ( (updateToReviewState || updateToFlagReviewState) && (!isReviewState || !isFlaggedReviewState))
				map.put("lastSubmittedOn", DateUtils.format(new Date()));
			if (!StringUtils.equalsIgnoreCase(status, inputStatus.toString()))
				logEvent = true;
		}
		
		boolean checkError = false;
		Response createResponse = null;
		if (isLiveState || isFlaggedState) {
			if (isImageObjectCreationNeeded) {
				graphNode.setIdentifier(contentImageId);
				graphNode.setObjectType(CONTENT_IMAGE_OBJECT_TYPE);
				metadata.put("status", "Draft");
				Object lastUpdatedBy = map.get("lastUpdatedBy");
				if (null != lastUpdatedBy)
					metadata.put("lastUpdatedBy", lastUpdatedBy);
				graphNode.setGraphId(GRAPH_ID);
				LOGGER.info("Creating content image: " + graphNode.getIdentifier());
				createResponse = createDataNode(graphNode);
				checkError = checkError(createResponse);
				if (!checkError) {
					LOGGER.info("Updating external props for: " + contentImageId);
					Response bodyResponse = getContentProperties(contentId, externalPropsList);
					checkError = checkError(bodyResponse);
					if (!checkError) {
						Map<String, Object> extValues = (Map<String, Object>) bodyResponse.get(ContentStoreParams.values.name());
						if (null != extValues && !extValues.isEmpty()) {
							updateContentProperties(contentImageId, extValues);
						}
					}
					map.put("versionKey", createResponse.get("versionKey"));
				}
			}
			objectType = CONTENT_IMAGE_OBJECT_TYPE;
			contentId = contentImageId;
		} else if (imageObjectExists) {
			objectType = CONTENT_IMAGE_OBJECT_TYPE;
			contentId = contentImageId;
		}
		
		if (checkError)
			return createResponse;
		LOGGER.info("Updating content node: " + contentId);
		Node domainObj = ConvertToGraphNode.convertToGraphNode(map, definition, graphNode);
		domainObj.setGraphId(GRAPH_ID);
		domainObj.setIdentifier(contentId);
		domainObj.setObjectType(objectType);
		createResponse = updateDataNode(domainObj);
		checkError = checkError(createResponse);
		if (checkError)
			return createResponse;
		
		createResponse.put(GraphDACParams.node_id.name(), originalId);
		if (logEvent) {
			metadata.putAll(map);
			metadata.put("prevState", status);
			LogTelemetryEventUtil.logContentLifecycleEvent(originalId, metadata);
		}
		
		if (null != externalProps && !externalProps.isEmpty()) {
			Response externalPropsResponse = updateContentProperties(contentId, externalProps);
			if (checkError(externalPropsResponse))
				return externalPropsResponse;
		}
		return createResponse;
	}
	
	private List<String> getExternalPropsList(DefinitionDTO definition) {
		List<String> list = new ArrayList<String>();
		if (null != definition) {
			List<MetadataDefinition> props = definition.getProperties();
			if (null != props && !props.isEmpty()) {
				for (MetadataDefinition prop : props) {
					if (StringUtils.equalsIgnoreCase("external", prop.getDataType())) {
						list.add(prop.getPropertyName().trim());
					}
				}
			}
		}
		return list;
	}

}