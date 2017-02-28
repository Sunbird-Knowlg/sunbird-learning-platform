package com.ilimi.taxonomy.mgr.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.optimizr.Optimizr;
import org.ekstep.common.slugs.Slug;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.contentstore.util.ContentStoreOperations;
import org.ekstep.contentstore.util.ContentStoreParams;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.learning.common.enums.LearningActorNames;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.common.router.RequestRouterPool;
import com.ilimi.common.util.LogTelemetryEventUtil;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Filter;
import com.ilimi.graph.dac.model.MetadataCriterion;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.taxonomy.content.ContentMimeTypeFactory;
import com.ilimi.taxonomy.content.common.ContentConfigurationConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.pipeline.initializer.InitializePipeline;
import com.ilimi.taxonomy.dto.ContentSearchCriteria;
import com.ilimi.taxonomy.mgr.IContentManager;
import com.ilimi.taxonomy.mgr.IMimeTypeManager;
import com.ilimi.taxonomy.util.ConceptTagger;

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

	@Autowired
	private ContentMimeTypeFactory contentFactory;

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(ContentManagerImpl.class.getName());

	/** The Disk Location where the operations on file will take place. */
	private static final String tempFileLocation = "/data/contentBundle/";

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
		Response res = mimeTypeManager.upload(node, uploadedFile, false);
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
				for (Node node : nodes) {
					String body = getContentBody(node.getIdentifier());
					node.getMetadata().put(ContentWorkflowPipelineParams.body.name(), body);
					LOGGER.debug("Body fetched from content store");
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

			String[] arr = AWSUploader.uploadFile(folder, minEcar);
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
	public Response publish(String taxonomyId, String contentId, Map<String, Object> requestMap) {
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
		
		String body = getContentBody(contentId);
		node.getMetadata().put(ContentWorkflowPipelineParams.body.name(), body);
		LOGGER.debug("Body fetched from content store");

		String mimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
		if (StringUtils.isBlank(mimeType)) {
			mimeType = "assets";
		}
		LOGGER.info("Mime-Type" + mimeType + " | [Content ID: " + contentId + "]");

		String prevState = (String) node.getMetadata().get(ContentAPIParams.status.name());
		String publisher = null;
		if (null != requestMap && !requestMap.isEmpty()) {
			publisher = (String) requestMap.get("lastPublishedBy");
			node.getMetadata().putAll(requestMap);
		}
		if(StringUtils.isNotBlank(publisher)){
			LOGGER.debug("LastPublishedBy: " + publisher);
			node.getMetadata().put(GraphDACParams.lastUpdatedBy.name(), publisher);
		} else {
			node.getMetadata().put("lastPublishedBy", null);
			node.getMetadata().put(GraphDACParams.lastUpdatedBy.name(), null);
		}
		LOGGER.info("Getting Mime-Type Manager Factory. | [Content ID: " + contentId + "]");
		IMimeTypeManager mimeTypeManager = contentFactory.getImplForService(mimeType);

		try {
			response = mimeTypeManager.publish(node, true);
			String contentType = (String) node.getMetadata().get("contentType");
			if (!checkError(response) && !StringUtils.equalsIgnoreCase("Asset", contentType)) {
				node.getMetadata().put("prevState", prevState);

				LOGGER.info("Generating Telemetry Event. | [Content ID: " + contentId + "]");
				LogTelemetryEventUtil.logContentLifecycleEvent(contentId, node.getMetadata());

				LOGGER.info("Tagging concepts for content. | [Content ID: " + contentId + "]");
				ConceptTagger tagger = new ConceptTagger();
				tagger.tagConcepts(taxonomyId, contentId, node);
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
		Response getNodeRes = getDataNode(taxonomyId, contentId);
		response = copyResponse(getNodeRes);
		if (checkError(response)) {
			return response;
		}
		
		Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
		LOGGER.debug("Node: ", node);
		
		String body = getContentBody(contentId);
		node.getMetadata().put(ContentWorkflowPipelineParams.body.name(), body);
		LOGGER.debug("Body Fetched From Content Store.");
		
		String mimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
		if (StringUtils.isBlank(mimeType)) {
			mimeType = "assets";
		}
		LOGGER.info("Mime-Type" + mimeType + " | [Content ID: " + contentId + "]");
		
		LOGGER.info("Getting Mime-Type Manager Factory. | [Content ID: " + contentId + "]");
		IMimeTypeManager mimeTypeManager = contentFactory.getImplForService(mimeType);
		
		response = mimeTypeManager.review(node, false);
		
		LOGGER.debug("Returning 'Response' Object: ", response);
		return response;
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
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
		}
	}
	
}