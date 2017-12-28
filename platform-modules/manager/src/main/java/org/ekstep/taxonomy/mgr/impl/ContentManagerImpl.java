package org.ekstep.taxonomy.mgr.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.dto.ResponseParams.StatusType;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.optimizr.Optimizr;
import org.ekstep.common.slugs.Slug;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.content.dto.ContentSearchCriteria;
import org.ekstep.content.enums.ContentMetadata;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.content.mimetype.mgr.IMimeTypeManager;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.content.publish.PublishManager;
import org.ekstep.content.util.MimeTypeManagerFactory;
import org.ekstep.contentstore.util.ContentStoreOperations;
import org.ekstep.contentstore.util.ContentStoreParams;
import org.ekstep.graph.common.DateUtils;
import org.ekstep.graph.common.Identifier;
import org.ekstep.graph.dac.enums.AuditProperties;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.enums.RelationTypes;
import org.ekstep.graph.dac.enums.SystemNodeTypes;
import org.ekstep.graph.dac.enums.SystemProperties;
import org.ekstep.graph.dac.model.Filter;
import org.ekstep.graph.dac.model.MetadataCriterion;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.graph.dac.model.SearchConditions;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.graph.model.node.MetadataDefinition;
import org.ekstep.graph.model.node.RelationDefinition;
import org.ekstep.graph.service.common.DACConfigurationConstants;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.learning.common.enums.LearningActorNames;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.springframework.stereotype.Component;

import org.ekstep.common.dto.NodeDTO;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.mgr.ConvertGraphNode;
import org.ekstep.common.mgr.ConvertToGraphNode;
import org.ekstep.common.router.RequestRouterPool;
import org.ekstep.taxonomy.common.LanguageCodeMap;
import org.ekstep.taxonomy.enums.TaxonomyAPIParams;
import org.ekstep.taxonomy.mgr.IContentManager;
import org.ekstep.telemetry.logger.PlatformLogger;
import org.ekstep.telemetry.util.LogTelemetryEventUtil;

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
public class ContentManagerImpl extends BaseContentManager implements IContentManager {

	/** The Disk Location where the operations on file will take place. */
	private static final String tempFileLocation = "/data/contentBundle/";

	/** The Default Manifest Version */
	private static final String DEFAULT_CONTENT_MANIFEST_VERSION = "1.2";

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

	private PublishManager publishManager = new PublishManager();

	private static List<String> contentTypeList = new ArrayList<String>();

	static {
		contentTypeList.add("Story");
		contentTypeList.add("Worksheet");
		contentTypeList.add("Game");
		contentTypeList.add("Simulation");
		contentTypeList.add("Puzzle");
		contentTypeList.add("Diagnostic");
		contentTypeList.add("ContentTemplate");
		contentTypeList.add("ItemTemplate");
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
		Response getNodeRes = getResponse(request);
		return getNodeRes;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ekstep.taxonomy.mgr.IContentManager#upload(java.lang.String,
	 * java.lang.String, java.io.File, java.lang.String)
	 */
	@SuppressWarnings("unused")
	@Override
	public Response upload(String contentId, String taxonomyId, File uploadedFile, String mimeType) {
		PlatformLogger.log("Graph ID: " + taxonomyId + "Content ID: "+contentId);
	
		
		
		boolean updateMimeType = false;

		try {
			if (StringUtils.isBlank(taxonomyId))
				throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
						"Taxonomy Id is blank.");
			if (StringUtils.isBlank(contentId))
				throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
						"Content Object Id is blank.");
			if (null == uploadedFile)
				throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_UPLOAD_OBJECT.name(),
						"Upload file is blank.");
			PlatformLogger.log("Uploaded File: ", uploadedFile.getAbsolutePath());
			
			if (StringUtils.endsWithIgnoreCase(contentId, DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX))
				throw new ClientException(ContentErrorCodes.OPERATION_DENIED.name(),
						"Invalid Content Identifier. | [Content Identifier does not Exists.]");

			Node node = getNodeForOperation(taxonomyId, contentId, "upload");

			isNodeUnderProcessing(node, "Upload");
			if (StringUtils.isBlank(mimeType)) {
				mimeType = getMimeType(node);
			} else {
				setMimeTypeForUpload(mimeType, node);
				updateMimeType = true;
			}

			PlatformLogger.log("Mime-Type: " + mimeType + " | [Content ID: " + contentId + "]");
			PlatformLogger.log(
					"Fetching Mime-Type Factory For Mime-Type: " + mimeType + " | [Content ID: " + contentId + "]");
			String contentType = (String) node.getMetadata().get("contentType");
			IMimeTypeManager mimeTypeManager = MimeTypeManagerFactory.getManager(contentType, mimeType);
			Response res = mimeTypeManager.upload(contentId, node, uploadedFile, false);

			if (updateMimeType && !checkError(res)) {
				node.getMetadata().put("versionKey", res.getResult().get("versionKey"));
				Response response = updateMimeType(contentId, mimeType);
				if (checkError(response))
					return response;
			}

			return checkAndReturnUploadResponse(res);
		} catch (ClientException e) {
			throw e;
		} catch (ServerException e) {
			return ERROR(e.getErrCode(), e.getMessage(), ResponseCode.SERVER_ERROR);
		} catch (Exception e) {
			String message = "Something went wrong while processing uploaded file.";
			PlatformLogger.log(message, null, e);
			return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), message, ResponseCode.SERVER_ERROR);
		} finally {
			if (null != uploadedFile && uploadedFile.exists())
				uploadedFile.delete();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ekstep.taxonomy.mgr.IContentManager#upload(java.lang.String,
	 * java.lang.String, java.io.File, java.lang.String)
	 */
	@Override
	public Response upload(String contentId, String taxonomyId, String fileUrl, String mimeType) {
		PlatformLogger
				.log("Graph ID: " + taxonomyId + " :: " + "Content ID: " + contentId + " :: " + "File URL:" + fileUrl);
		boolean updateMimeType = false;
		try {
			if (StringUtils.isBlank(taxonomyId))
				throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
						"Taxonomy Id is blank.");
			if (StringUtils.isBlank(contentId))
				throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
						"Content Object Id is blank.");
			if (StringUtils.isBlank(fileUrl))
				throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_UPLOAD_OBJECT.name(),
						"fileUrl is blank.");
			isImageContentId(contentId);
			Node node = getNodeForOperation(taxonomyId, contentId, "upload");
			isNodeUnderProcessing(node, "Upload");
			if (StringUtils.isBlank(mimeType)) {
				mimeType = getMimeType(node);
			} else {
				setMimeTypeForUpload(mimeType, node);
				updateMimeType = true;
			}

			PlatformLogger.log(
					"Fetching Mime-Type Factory For Mime-Type: " + mimeType + " | [Content ID: " + contentId + "]");
			String contentType = (String) node.getMetadata().get("contentType");
			IMimeTypeManager mimeTypeManager = MimeTypeManagerFactory.getManager(contentType, mimeType);
			Response res = mimeTypeManager.upload(contentId, node, fileUrl);

			if (updateMimeType && !checkError(res)) {
				node.getMetadata().put("versionKey", res.getResult().get("versionKey"));
				Response response = updateMimeType(contentId, mimeType);
				if (checkError(response))
					return response;
			}

			return checkAndReturnUploadResponse(res);
		} catch (ClientException e) {
			throw e;
		} catch (ServerException e) {
			return ERROR(e.getErrCode(), e.getMessage(), ResponseCode.SERVER_ERROR);
		} catch (Exception e) {
			String message = "Something went wrong while processing uploaded file.";
			PlatformLogger.log(message, null, e);
			return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), message, ResponseCode.SERVER_ERROR);
		}
	}

	private void setMimeTypeForUpload(String mimeType, Node node) {
		node.getMetadata().put("mimeType", mimeType);
		updateDefaultValuesByMimeType(node.getMetadata(), mimeType);
	}

	private Response checkAndReturnUploadResponse(Response res) {
		if (checkError(res)) {
			return res;
		} else {
			String nodeId = (String) res.getResult().get("node_id");
			String returnNodeId = getActualIdentifier(nodeId);
			res.getResult().replace("node_id", nodeId, returnNodeId);
			return res;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ekstep.taxonomy.mgr.IContentManager#bundle(org.ekstep.common.dto.
	 * Request, java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Response bundle(Request request, String taxonomyId, String version) {
		PlatformLogger.log("Request Object: ", request);
		PlatformLogger.log("Graph ID: " + taxonomyId);
		PlatformLogger.log("Version: " + version);

		String bundleFileName = (String) request.get("file_name");
		List<String> contentIds = (List<String>) request.get("content_identifiers");
		PlatformLogger.log("Bundle File Name: " + bundleFileName);
		PlatformLogger.log("Total No. of Contents: ", contentIds.size());
		if (contentIds.size() > 1 && StringUtils.isBlank(bundleFileName))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_INVALID_BUNDLE_CRITERIA.name(),
					"ECAR file name should not be blank");

		PlatformLogger.log("Fetching all the Nodes.");
		Response response = searchNodes(taxonomyId, contentIds);
		Response listRes = copyResponse(response);
		if (checkError(response)) {
			PlatformLogger.log("Erroneous Response.");
			return response;
		} else {
			List<Object> list = (List<Object>) response.get(ContentAPIParams.contents.name());
			List<Node> nodes = new ArrayList<Node>();
			List<Node> imageNodes = new ArrayList<Node>();
			if (null != list && !list.isEmpty()) {
				PlatformLogger.log("Iterating Over the List.");
				for (Object obj : list) {
					List<Node> nodelist = (List<Node>) obj;
					if (null != nodelist && !nodelist.isEmpty())
						nodes.addAll(nodelist);
				}

				PlatformLogger.log("Validating the Input Nodes.");
				validateInputNodesForBundling(nodes);

				for (Node node : nodes) {
					String contentImageId = getContentImageIdentifier(node.getIdentifier());
					Response getNodeResponse = getDataNode(taxonomyId, contentImageId);
					if (!checkError(getNodeResponse)) {
						node = (Node) getNodeResponse.get(GraphDACParams.node.name());
					}
					String body = getContentBody(node.getIdentifier());
					node.getMetadata().put(ContentAPIParams.body.name(), body);
					imageNodes.add(node);
					PlatformLogger.log("Body fetched from content store");
				}
				if (imageNodes.size() == 1 && StringUtils.isBlank(bundleFileName))
					bundleFileName = (String) imageNodes.get(0).getMetadata().get(ContentAPIParams.name.name()) + "_"
							+ System.currentTimeMillis() + "_" + (String) imageNodes.get(0).getIdentifier();
			}
			bundleFileName = Slug.makeSlug(bundleFileName, true);
			String fileName = bundleFileName + ".ecar";
			PlatformLogger.log("Bundle File Name: " + bundleFileName);

			PlatformLogger.log("Preparing the Parameter Map for 'Bundle' Pipeline.");
			InitializePipeline pipeline = new InitializePipeline(tempFileLocation, "node");
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(ContentAPIParams.nodes.name(), imageNodes);
			parameterMap.put(ContentAPIParams.bundleFileName.name(), fileName);
			parameterMap.put(ContentAPIParams.contentIdList.name(), contentIds);
			parameterMap.put(ContentAPIParams.manifestVersion.name(), DEFAULT_CONTENT_MANIFEST_VERSION);

			PlatformLogger.log("Calling Content Workflow 'Bundle' Pipeline.");
			listRes.getResult().putAll(pipeline.init(ContentAPIParams.bundle.name(), parameterMap).getResult());

			PlatformLogger.log("Returning Response.");
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
		Response response = getResponse(requests, GraphDACParams.node_list.name(), ContentAPIParams.contents.name());
		return response;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ekstep.taxonomy.mgr.IContentManager#optimize(java.lang.String,
	 * java.lang.String)
	 */
	public Response optimize(String taxonomyId, String contentId) {
		PlatformLogger.log("Graph ID: " + taxonomyId);
		PlatformLogger.log("Content ID: " + contentId);

		Response response = new Response();
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_ID.name(), "Content Id is blank");

		Node node = getNodeForOperation(taxonomyId, contentId, "optimize");
		PlatformLogger.log("Got Node: ", node);

		isNodeUnderProcessing(node, "Optimize");

		PlatformLogger.log("Given Content is not in Processing Status.");

		String status = (String) node.getMetadata().get(ContentAPIParams.status.name());
		PlatformLogger.log("Content Status: " + status);
		if (!StringUtils.equalsIgnoreCase(ContentAPIParams.Live.name(), status) || !StringUtils.equalsIgnoreCase(ContentAPIParams.Unlisted.name(), status))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(),
					"UnPublished content cannot be optimized");

		String downloadUrl = (String) node.getMetadata().get(ContentAPIParams.downloadUrl.name());
		PlatformLogger.log("Download Url: " + downloadUrl);
		if (StringUtils.isBlank(downloadUrl))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(),
					"ECAR file not available for content");

		if (!StringUtils.equalsIgnoreCase(ContentAPIParams.ecar.name(), FilenameUtils.getExtension(downloadUrl)))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(),
					"Content package is not an ECAR file");

		String optStatus = (String) node.getMetadata().get(ContentAPIParams.optStatus.name());
		PlatformLogger.log("Optimization Process Status: " + optStatus);
		if (StringUtils.equalsIgnoreCase(ContentAPIParams.Processing.name(), optStatus))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(),
					"Content optimization is in progress. Please try after the current optimization is complete");

		node.getMetadata().put(ContentAPIParams.optStatus.name(), ContentAPIParams.Processing.name());
		updateDataNode(node);
		Optimizr optimizr = new Optimizr();
		try {
			PlatformLogger.log("Invoking the Optimizer For Content Id: " + contentId);
			File minEcar = optimizr.optimizeECAR(downloadUrl);
			PlatformLogger.log("Optimized File: " + minEcar.getName() + " | [Content Id: " + contentId + "]");

			String folder = getFolderName(downloadUrl);
			PlatformLogger.log("Folder Name: " + folder + " | [Content Id: " + contentId + "]");

			String[] arr = AWSUploader.uploadFile(folder, minEcar);
			response.put("url", arr[1]);
			PlatformLogger.log("URL: " + arr[1] + " | [Content Id: " + contentId + "]");

			PlatformLogger.log("Updating the Optimization Status. | [Content Id: " + contentId + "]");
			node.getMetadata().put(ContentAPIParams.optStatus.name(), "Complete");
			updateDataNode(node);
			PlatformLogger.log("Node Updated. | [Content Id: " + contentId + "]");

			PlatformLogger.log("Directory Cleanup. | [Content Id: " + contentId + "]");
			FileUtils.deleteDirectory(minEcar.getParentFile());
		} catch (Exception e) {
			node.getMetadata().put(ContentAPIParams.optStatus.name(), "Error");
			updateDataNode(node);
			response = ERROR(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(), e.getMessage(), ResponseCode.SERVER_ERROR);
		}
		return response;
	}

	public Response preSignedURL(String taxonomyId, String contentId, String fileName) {
		// TODO: Check content exist or not.
		Response response = new Response();
		String preSignedURL = AWSUploader.preSignedURL(contentId, fileName);
		response.put(ContentAPIParams.content_id.name(), contentId);
		response.put(ContentAPIParams.pre_signed_url.name(), preSignedURL);
		response.put(ContentAPIParams.url_expiry.name(), S3PropertyReader.getProperty("s3.upload.url.expiry"));
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
		PlatformLogger.log("[updateNode] | Node: ", node);
		Response response = new Response();
		if (null != node) {
			String contentId = node.getIdentifier();
			// Checking if Content Image Object is being Updated, then return
			// the Original Content Id
			if (BooleanUtils.isTrue((Boolean) node.getMetadata().get(TaxonomyAPIParams.isImageObject.name()))) {
				node.getMetadata().remove(TaxonomyAPIParams.isImageObject.name());
				node.setIdentifier(node.getIdentifier() + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX);
			}

			PlatformLogger.log("Getting Update Node Request For Node ID: " + node.getIdentifier());
			Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "updateDataNode");
			updateReq.put(GraphDACParams.node.name(), node);
			updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());

			PlatformLogger.log("Updating the Node ID: " + node.getIdentifier());
			response = getResponse(updateReq);

			response.put(TaxonomyAPIParams.node_id.name(), contentId);
			PlatformLogger.log("Returning Node Update Response.");
		}
		return response;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ekstep.taxonomy.mgr.IContentManager#publish(java.lang.String,
	 * java.lang.String)
	 */
	public Response publish(String taxonomyId, String contentId, Map<String, Object> requestMap) {
		PlatformLogger.log("Graph ID: " + taxonomyId + " | Content ID: " + contentId);

		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_ID.name(), "Content Id is blank");

		Response response = new Response();

		Node node = getNodeForOperation(taxonomyId, contentId, "publish");
		PlatformLogger.log("Got Node: ", node);

		isNodeUnderProcessing(node, "Publish");

		PlatformLogger.log("Given Content is not in Processing Status.");

		String publisher = null;
		if (null != requestMap && !requestMap.isEmpty()) {
			publisher = (String) requestMap.get("lastPublishedBy");
			node.getMetadata().putAll(requestMap);
		}
		if (StringUtils.isNotBlank(publisher)) {
			PlatformLogger.log("LastPublishedBy: " + publisher);
			node.getMetadata().put(GraphDACParams.lastUpdatedBy.name(), publisher);
		} else {
			node.getMetadata().put("lastPublishedBy", null);
			node.getMetadata().put(GraphDACParams.lastUpdatedBy.name(), null);
		}

		try {
			response = publishManager.publish(contentId, node);
		} catch (ClientException e) {
			throw e;
		} catch (ServerException e) {
			throw e;
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_PUBLISH.name(),
					"Error occured during content publish");
		}

		PlatformLogger.log("Returning 'Response' Object.");
		if (StringUtils.endsWith(response.getResult().get("node_id").toString(), ".img")) {
			String identifier = (String) response.getResult().get("node_id");
			String new_identifier = identifier.replace(".img", "");
			PlatformLogger.log("replacing image id with content id in response" + identifier + new_identifier);
			response.getResult().replace("node_id", identifier, new_identifier);
		}
		return response;
	}

	@Override
	public Response review(String taxonomyId, String contentId, Request request) {
		PlatformLogger.log("Graph Id: ", taxonomyId);
		PlatformLogger.log("Content Id: ", contentId);
		PlatformLogger.log("Request: ", request);

		PlatformLogger.log("Validating The Input Parameter.");
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_ID.name(), "Content Id is blank");

		Response response = new Response();

		Node node = getNodeForOperation(taxonomyId, contentId, "review");
		PlatformLogger.log("Node: ", node);

		isNodeUnderProcessing(node, "Review");

		PlatformLogger.log("Given Content is not in Processing Status.");

		String body = getContentBody(node.getIdentifier());
		node.getMetadata().put(ContentAPIParams.body.name(), body);
		PlatformLogger.log("Body Fetched From Content Store.");

		PlatformLogger.log("Putting the last Submitted On TimeStamp.");
		node.getMetadata().put(TaxonomyAPIParams.lastSubmittedOn.name(), DateUtils.formatCurrentDate());

		String mimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
		if (StringUtils.isBlank(mimeType)) {
			mimeType = "assets";
		}
		PlatformLogger.log("Mime-Type" + mimeType + " | [Content ID: " + contentId + "]");

		PlatformLogger.log("Getting Mime-Type Manager Factory. | [Content ID: " + contentId + "]");
		String contentType = (String) node.getMetadata().get("contentType");
		IMimeTypeManager mimeTypeManager = MimeTypeManagerFactory.getManager(contentType, mimeType);

		response = mimeTypeManager.review(contentId, node, false);

		PlatformLogger.log("Returning 'Response' Object: ", response);
		return response;
	}

	@Override
	public Response getHierarchy(String graphId, String contentId, String mode) {
		PlatformLogger.log("Graph Id: ", graphId);
		PlatformLogger.log("Content Id: ", contentId);
		Node node = getContentNode(graphId, contentId, mode);

		PlatformLogger.log("Collecting Hierarchical Data For Content Id: " + node.getIdentifier());
		DefinitionDTO definition = getDefinition(graphId, node.getObjectType());
		Map<String, Object> map = getContentHierarchyRecursive(graphId, node, definition, mode);
		Map<String, Object> dataMap = contentCleanUp(map);
		Response response = new Response();
		response.put("content", dataMap);
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
	private Map<String, Object> getContentHierarchyRecursive(String graphId, Node node, DefinitionDTO definition,
			String mode) {
		Map<String, Object> contentMap = ConvertGraphNode.convertGraphNode(node, graphId, definition, null);
		List<NodeDTO> children = (List<NodeDTO>) contentMap.get("children");

		// Collections sort method is used to sort the child content list on the
		// basis of index.
		if (null != children && !children.isEmpty()) {
			Collections.sort(children, new Comparator<NodeDTO>() {
				@Override
				public int compare(NodeDTO o1, NodeDTO o2) {
					return o1.getIndex() - o2.getIndex();
				}
			});
		}

		if (null != children && !children.isEmpty()) {
			List<Map<String, Object>> childList = new ArrayList<Map<String, Object>>();
			for (NodeDTO dto : children) {
				Node childNode = getContentNode(graphId, dto.getIdentifier(), mode);
				Map<String, Object> childMap = getContentHierarchyRecursive(graphId, childNode, definition, mode);
				childMap.put("index", dto.getIndex());
				Map<String, Object> childData = contentCleanUp(childMap);
				childList.add(childData);
			}
			contentMap.put("children", childList);
		}
		return contentMap;
	}

	private Map<String, Object> contentCleanUp(Map<String, Object> map) {
		if (map.containsKey(TaxonomyAPIParams.identifier.name())) {
			String identifier = (String) map.get(TaxonomyAPIParams.identifier.name());
			PlatformLogger.log("Checking if identifier ends with .img" + identifier);
			if (StringUtils.endsWithIgnoreCase(identifier, DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX)) {
				String newIdentifier = identifier.replace(DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX, "");
				PlatformLogger.log("replacing image id with content id in response " + identifier + newIdentifier);
				map.replace(TaxonomyAPIParams.identifier.name(), identifier, newIdentifier);
			}
		}
		return map;
	}

	private Node getContentNode(String graphId, String contentId, String mode) {

		if (StringUtils.equalsIgnoreCase("edit", mode)) {
			String contentImageId = getContentImageIdentifier(contentId);
			Response responseNode = getDataNode(graphId, contentImageId);
			if (!checkError(responseNode)) {
				Node content = (Node) responseNode.get(GraphDACParams.node.name());
				PlatformLogger.log("Got draft version of node: ", content);
				return content;
			}
		}
		Response responseNode = getDataNode(graphId, contentId);
		if (checkError(responseNode))
			throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
					"Content not found with id: " + contentId);

		Node content = (Node) responseNode.get(GraphDACParams.node.name());
		PlatformLogger.log("Got Node: ", content);
		return content;
	}

	private DefinitionDTO getDefinition(String graphId, String objectType) {
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
				GraphDACParams.object_type.name(), objectType);
		Response response = getResponse(request);
		if (!checkError(response)) {
			DefinitionDTO definition = (DefinitionDTO) response.get(GraphDACParams.definition_node.name());
			return definition;
		}
		return null;
	}

	@SuppressWarnings("unused")
	private String getContentBody(String contentId, String mode) {
		String body = "";
		if (StringUtils.equalsIgnoreCase(TaxonomyAPIParams.edit.name(), mode))
			body = getContentBody(getContentImageIdentifier(contentId));
		if (StringUtils.isBlank(body))
			body = getContentBody(contentId);
		return body;
	}

	private String getContentBody(String contentId) {
		Request request = new Request();
		request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name());
		request.setOperation(ContentStoreOperations.getContentBody.name());
		request.put(ContentStoreParams.content_id.name(), contentId);
		Response response = makeLearningRequest(request);
		String body = (String) response.get(ContentStoreParams.body.name());
		return body;
	}

	private Response getContentProperties(String contentId, List<String> properties) {
		Request request = new Request();
		request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name());
		request.setOperation(ContentStoreOperations.getContentProperties.name());
		request.put(ContentStoreParams.content_id.name(), contentId);
		request.put(ContentStoreParams.properties.name(), properties);
		Response response = makeLearningRequest(request);
		return response;
	}

	private Response updateContentProperties(String contentId, Map<String, Object> properties) {
		Request request = new Request();
		request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name());
		request.setOperation(ContentStoreOperations.updateContentProperties.name());
		request.put(ContentStoreParams.content_id.name(), contentId);
		request.put(ContentStoreParams.properties.name(), properties);
		Response response = makeLearningRequest(request);
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
	private Response makeLearningRequest(Request request) {
		ActorRef router = LearningRequestRouterPool.getRequestRouter();
		try {
			Future<Object> future = Patterns.ask(router, request, RequestRouterPool.REQ_TIMEOUT);
			Object obj = Await.result(future, RequestRouterPool.WAIT_TIMEOUT.duration());
			if (obj instanceof Response) {
				Response response = (Response) obj;
				PlatformLogger.log("Response Params: " + response.getParams() + " | Code: " + response.getResponseCode()
						+ " | Result: " + response.getResult().keySet());
				return response;
			} else {
				return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
			}
		} catch (Exception e) {
			PlatformLogger.log("Error! Something went wrong", e.getMessage(), e);
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", e);
		}
	}

	private String getContentImageIdentifier(String contentId) {
		String contentImageId = "";
		if (StringUtils.isNotBlank(contentId))
			contentImageId = contentId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
		return contentImageId;
	}

	private Node getNodeForOperation(String taxonomyId, String contentId, String operation) {
		PlatformLogger.log("Taxonomy Id: " + taxonomyId + " | Content Id: " + contentId);
		Node node = new Node();

		PlatformLogger.log("Fetching the Content Node. | [Content ID: " + contentId + "]");
		String contentImageId = getContentImageIdentifier(contentId);
		Response response = getDataNode(taxonomyId, contentImageId);
		if (checkError(response)) {
			PlatformLogger.log("Unable to Fetch Content Image Node for Content Id: " + contentId);

			PlatformLogger.log("Trying to Fetch Content Node (Not Image Node) for Content Id: " + contentId);
			response = getDataNode(taxonomyId, contentId);

			PlatformLogger.log("Checking for Fetched Content Node (Not Image Node) for Content Id: " + contentId);
			if (checkError(response))
				throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_INVALID_CONTENT.name(),
						"Error! While Fetching the Content for Operation | [Content Id: " + contentId + "]");

			// Content Image Node is not Available so assigning the original
			// Content Node as node
			node = (Node) response.get(GraphDACParams.node.name());

			if (!StringUtils.equalsIgnoreCase(operation, "publish")
					&& !StringUtils.equalsIgnoreCase(operation, "review")) {
				// Checking if given Content Id is Image Node
				if (null != node && isContentImageObject(node))
					throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_INVALID_CONTENT.name(),
							"Invalid Content Identifier! | [Given Content Identifier '" + node.getIdentifier()
									+ "' does not Exist.]");

				PlatformLogger.log("Fetched Content Node: ", node);
				String status = (String) node.getMetadata().get(TaxonomyAPIParams.status.name());
				if (StringUtils.isNotBlank(status)
						&& (StringUtils.equalsIgnoreCase(TaxonomyAPIParams.Live.name(), status)
								|| StringUtils.equalsIgnoreCase(TaxonomyAPIParams.Unlisted.name(), status)
								|| StringUtils.equalsIgnoreCase(TaxonomyAPIParams.Flagged.name(), status)))
					node = createContentImageNode(taxonomyId, contentImageId, node);
			}
		} else {
			// Content Image Node is Available so assigning it as node
			node = (Node) response.get(GraphDACParams.node.name());
			PlatformLogger.log("Getting Content Image Node and assigning it as node" + node.getIdentifier());
		}

		PlatformLogger.log("Returning the Node for Operation with Identifier: " + node.getIdentifier());
		return node;
	}

	private Node createContentImageNode(String taxonomyId, String contentImageId, Node node) {
		PlatformLogger.log("Taxonomy Id: " + taxonomyId);
		PlatformLogger.log("Content Id: " + contentImageId);
		PlatformLogger.log("Node: ", node);

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
		PlatformLogger.log("Returning Content Image Node Identifier" + nodeData.getIdentifier());
		return nodeData;
	}

	private Response createDataNode(Node node) {
		PlatformLogger.log("Node :", node);
		Response response = new Response();
		if (null != node) {
			Request request = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "createDataNode");
			request.put(GraphDACParams.node.name(), node);

			PlatformLogger.log("Creating the Node ID: " + node.getIdentifier());
			response = getResponse(request);
		}
		return response;
	}

	private void validateInputNodesForBundling(List<Node> nodes) {
		if (null != nodes && !nodes.isEmpty()) {
			for (Node node : nodes) {
				// Validating for Content Image Node
				if (null != node && isContentImageObject(node))
					throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_INVALID_CONTENT.name(),
							"Invalid Content Identifier! | [Given Content Identifier '" + node.getIdentifier()
									+ "' does not Exist.]");
			}
		}
	}

	private boolean isContentImageObject(Node node) {
		boolean isContentImage = false;
		if (null != node && StringUtils.equalsIgnoreCase(node.getObjectType(),
				ContentWorkflowPipelineParams.ContentImage.name()))
			isContentImage = true;
		return isContentImage;
	}

	public Response createContent(Map<String, Object> map) throws Exception {
		if (null == map)
			return ERROR("ERR_CONTENT_INVALID_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);

		// Checking for resourceType if contentType resource
		// validateNodeForContentType(map);

		DefinitionDTO definition = getDefinition(GRAPH_ID, CONTENT_OBJECT_TYPE);

		// Restrict create API to create with Status
		validateForStatusUpdate(definition, map);

		String mimeType = (String) map.get("mimeType");
		if (StringUtils.isNotBlank(mimeType)) {
			if (!StringUtils.equalsIgnoreCase("application/vnd.android.package-archive", mimeType))
				map.put("osId", "org.ekstep.quiz.app");
			String contentType = (String) map.get("contentType");
			if (StringUtils.isNotBlank(contentType)) {
				List<String> parentVisibilityList = Platform.config.getStringList("content.metadata.visibility.parent");
				if (parentVisibilityList.contains(contentType.toLowerCase()))
					map.put("visibility", "Parent");
			}

			if (StringUtils.equalsIgnoreCase("application/vnd.ekstep.plugin-archive", mimeType)) {
				String code = (String) map.get("code");
				if (null == code || StringUtils.isBlank(code))
					return ERROR("ERR_PLUGIN_CODE_REQUIRED", "Unique code is mandatory for plugins",
							ResponseCode.CLIENT_ERROR);
				map.put("identifier", map.get("code"));
			}

			updateDefaultValuesByMimeType(map, mimeType);

			Map<String, Object> externalProps = new HashMap<String, Object>();
			List<String> externalPropsList = getExternalPropsList(definition);
			if (null != externalPropsList && !externalPropsList.isEmpty()) {
				for (String prop : externalPropsList) {
					if (null != map.get(prop))
						externalProps.put(prop, map.get(prop));
					map.remove(prop);
				}
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
			return ERROR("ERR_CONTENT_INVALID_CONTENT_MIMETYPE_TYPE", "Mime Type cannot be empty",
					ResponseCode.CLIENT_ERROR);
		}
	}

	@SuppressWarnings("unchecked")
	public Response updateContent(String contentId, Map<String, Object> map) throws Exception {
		if (null == map)
			return ERROR("ERR_CONTENT_INVALID_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);

		DefinitionDTO definition = getDefinition(GRAPH_ID, CONTENT_OBJECT_TYPE);

		// Restrict Update API to Update Status
		validateForStatusUpdate(definition, map);

		String originalId = contentId;
		String objectType = CONTENT_OBJECT_TYPE;
		map.put("objectType", CONTENT_OBJECT_TYPE);
		map.put("identifier", contentId);

		String mimeType = (String) map.get(TaxonomyAPIParams.mimeType.name());
		updateDefaultValuesByMimeType(map, mimeType);

		boolean isImageObjectCreationNeeded = false;
		boolean imageObjectExists = false;

		String contentImageId = contentId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
		Response getNodeResponse = getDataNode(GRAPH_ID, contentImageId);
		if (checkError(getNodeResponse)) {
			PlatformLogger.log("Content image not found: " + contentImageId);
			isImageObjectCreationNeeded = true;
			getNodeResponse = getDataNode(GRAPH_ID, contentId);
			PlatformLogger.log("Content node response: " + getNodeResponse);
		} else
			imageObjectExists = true;

		if (checkError(getNodeResponse)) {
			PlatformLogger.log("Content not found: " + contentId);
			return getNodeResponse;
		}

		if (map.containsKey(ContentAPIParams.body.name()))
			map.put(ContentAPIParams.artifactUrl.name(), null);

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
		PlatformLogger.log("Graph node found: " + graphNode.getIdentifier());
		Map<String, Object> metadata = graphNode.getMetadata();
		String status = (String) metadata.get("status");
		boolean isReviewState = StringUtils.equalsIgnoreCase("Review", status);
		boolean isFlaggedReviewState = StringUtils.equalsIgnoreCase("FlagReview", status);
		boolean isFlaggedState = StringUtils.equalsIgnoreCase("Flagged", status);
		boolean isLiveState = StringUtils.equalsIgnoreCase("Live", status);
		boolean isUnlistedState = StringUtils.equalsIgnoreCase("Unlisted", status);
		
		String inputStatus = (String) map.get("status");
		if (null != inputStatus) {
			boolean updateToReviewState = StringUtils.equalsIgnoreCase("Review", inputStatus);
			boolean updateToFlagReviewState = StringUtils.equalsIgnoreCase("FlagReview", inputStatus);
			if ((updateToReviewState || updateToFlagReviewState) && (!isReviewState || !isFlaggedReviewState))
				map.put("lastSubmittedOn", DateUtils.format(new Date()));
		}

		boolean checkError = false;
		Response createResponse = null;
		if (isLiveState || isUnlistedState || isFlaggedState) {
			if (isImageObjectCreationNeeded) {
				graphNode.setIdentifier(contentImageId);
				graphNode.setObjectType(CONTENT_IMAGE_OBJECT_TYPE);
				metadata.put("status", "Draft");
				Object lastUpdatedBy = map.get("lastUpdatedBy");
				if (null != lastUpdatedBy)
					metadata.put("lastUpdatedBy", lastUpdatedBy);
				graphNode.setGraphId(GRAPH_ID);
				PlatformLogger.log("Creating content image: " + graphNode.getIdentifier());
				createResponse = createDataNode(graphNode);
				checkError = checkError(createResponse);
				if (!checkError) {
					PlatformLogger.log("Updating external props for: " + contentImageId);
					Response bodyResponse = getContentProperties(contentId, externalPropsList);
					checkError = checkError(bodyResponse);
					if (!checkError) {
						Map<String, Object> extValues = (Map<String, Object>) bodyResponse
								.get(ContentStoreParams.values.name());
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
		PlatformLogger.log("Updating content node: " + contentId);
		Node domainObj = ConvertToGraphNode.convertToGraphNode(map, definition, graphNode);
		domainObj.setGraphId(GRAPH_ID);
		domainObj.setIdentifier(contentId);
		domainObj.setObjectType(objectType);
		createResponse = updateDataNode(domainObj);
		checkError = checkError(createResponse);
		if (checkError)
			return createResponse;

		createResponse.put(GraphDACParams.node_id.name(), originalId);
		
		if (null != externalProps && !externalProps.isEmpty()) {
			Response externalPropsResponse = updateContentProperties(contentId, externalProps);
			if (checkError(externalPropsResponse))
				return externalPropsResponse;
		}
		return createResponse;
	}

	private void updateDefaultValuesByMimeType(Map<String, Object> map, String mimeType) {
		if (StringUtils.isNotBlank(mimeType)) {
			if (mimeType.endsWith("archive") || mimeType.endsWith("vnd.ekstep.content-collection")
					|| mimeType.endsWith("epub"))
				map.put(TaxonomyAPIParams.contentEncoding.name(), ContentMetadata.ContentEncoding.gzip.name());
			else
				map.put(TaxonomyAPIParams.contentEncoding.name(), ContentMetadata.ContentEncoding.identity.name());

			if (mimeType.endsWith("youtube") || mimeType.endsWith("x-url"))
				map.put(TaxonomyAPIParams.contentDisposition.name(), ContentMetadata.ContentDisposition.online.name());
			else
				map.put(TaxonomyAPIParams.contentDisposition.name(), ContentMetadata.ContentDisposition.inline.name());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response find(String graphId, String contentId, String mode, List<String> fields) {
		PlatformLogger.log("Graph Id: ", graphId);
		PlatformLogger.log("Content Id: ", contentId);
		Response response = new Response();

		Node node = getContentNode(graphId, contentId, mode);

		PlatformLogger.log("Fetching the Data For Content Id: " + node.getIdentifier());
		DefinitionDTO definition = getDefinition(graphId, node.getObjectType());
		List<String> externalPropsList = getExternalPropsList(definition);
		if (null == fields)
			fields = new ArrayList<String>();
		else
			fields = new ArrayList<String>(fields);

		// TODO: this is only for backward compatibility. remove after this
		// release.
		if (fields.contains("tags")) {
			fields.remove("tags");
			fields.add("keywords");
		}

		List<String> externalPropsToFetch = (List<String>) CollectionUtils.intersection(fields, externalPropsList);
		Map<String, Object> contentMap = ConvertGraphNode.convertGraphNode(node, graphId, definition, fields);

		if (null != externalPropsToFetch && !externalPropsToFetch.isEmpty()) {
			Response getContentPropsRes = getContentProperties(node.getIdentifier(), externalPropsToFetch);
			if (!checkError(getContentPropsRes)) {
				Map<String, Object> resProps = (Map<String, Object>) getContentPropsRes
						.get(TaxonomyAPIParams.values.name());
				if (null != resProps)
					contentMap.putAll(resProps);
			}
		}

		// Get all the languages for a given Content
		List<String> languages = convertStringArrayToList(
				(String[]) node.getMetadata().get(TaxonomyAPIParams.language.name()));

		// Eval the language code for all Content Languages
		List<String> languageCodes = new ArrayList<String>();
		for (String language : languages)
			languageCodes.add(LanguageCodeMap.getLanguageCode(language.toLowerCase()));
		if (null != languageCodes && languageCodes.size() == 1)
			contentMap.put(TaxonomyAPIParams.languageCode.name(), languageCodes.get(0));
		else
			contentMap.put(TaxonomyAPIParams.languageCode.name(), languageCodes);

		response.put(TaxonomyAPIParams.content.name(), contentCleanUp(contentMap));
		response.setParams(getSucessStatus());
		return response;
	}

	public Response updateAllContentNodes(String originalId, Map<String, Object> map) throws Exception {
		if (null == map)
			return ERROR("ERR_CONTENT_INVALID_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);

		DefinitionDTO definition = getDefinition(GRAPH_ID, CONTENT_OBJECT_TYPE);
		String graphPassportKey = Platform.config.getString(DACConfigurationConstants.PASSPORT_KEY_BASE_PROPERTY);
		map.put("versionKey", graphPassportKey);
		Node domainObj = ConvertToGraphNode.convertToGraphNode(map, definition, null);
		Response updateResponse = updateNode(originalId, CONTENT_OBJECT_TYPE, domainObj);
		if (checkError(updateResponse))
			return updateResponse;
		updateResponse.put(GraphDACParams.node_id.name(), originalId);

		Node imgDomainObj = ConvertToGraphNode.convertToGraphNode(map, definition, null);
		updateNode(originalId + ".img", CONTENT_IMAGE_OBJECT_TYPE, imgDomainObj);
		return updateResponse;
	}

	private Response updateNode(String identifier, String objectType, Node domainNode) {
		domainNode.setGraphId(GRAPH_ID);
		domainNode.setIdentifier(identifier);
		domainNode.setObjectType(objectType);
		return updateDataNode(domainNode);
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

	@SuppressWarnings("unchecked")
	@Override
	public Response updateHierarchy(Map<String, Object> data) {
		String graphId = GRAPH_ID;
		if (null != data && !data.isEmpty()) {
			Map<String, Object> modifiedNodes = (Map<String, Object>) data.get("nodesModified");
			Map<String, Object> hierarchy = (Map<String, Object>) data.get("hierarchy");
			Map<String, String> idMap = new HashMap<String, String>();
			Map<String, String> newIdMap = new HashMap<String, String>();
			Map<String, Node> nodeMap = new HashMap<String, Node>();
			String rootNodeId = null;
			if (null != modifiedNodes && !modifiedNodes.isEmpty()) {
				DefinitionDTO definition = getDefinition(graphId, CONTENT_OBJECT_TYPE);
				Map<String, RelationDefinition> inRelDefMap = new HashMap<String, RelationDefinition>();
				Map<String, RelationDefinition> outRelDefMap = new HashMap<String, RelationDefinition>();
				getRelationDefMaps(definition, inRelDefMap, outRelDefMap);
				for (Entry<String, Object> entry : modifiedNodes.entrySet()) {
					Map<String, Object> map = (Map<String, Object>) entry.getValue();
					Response nodeResponse = createNodeObject(graphId, entry, idMap, nodeMap, newIdMap, definition,
							inRelDefMap, outRelDefMap);
					if (null != nodeResponse)
						return nodeResponse;
					Boolean root = (Boolean) map.get("root");
					if (BooleanUtils.isTrue(root))
						rootNodeId = idMap.get(entry.getKey());
				}
			}
			if (null != hierarchy && !hierarchy.isEmpty()) {
				for (Entry<String, Object> entry : hierarchy.entrySet()) {
					updateNodeHierarchyRelations(graphId, entry, idMap, nodeMap);
					if (StringUtils.isBlank(rootNodeId)) {
						Map<String, Object> map = (Map<String, Object>) entry.getValue();
						Boolean root = (Boolean) map.get("root");
						if (BooleanUtils.isTrue(root))
							rootNodeId = idMap.get(entry.getKey());
					}
				}
			}
			if (null != nodeMap && !nodeMap.isEmpty()) {
				List<Node> nodes = new ArrayList<Node>(nodeMap.values());
				Request request = getRequest(graphId, GraphEngineManagers.GRAPH_MANAGER, "bulkUpdateNodes");
				request.put(GraphDACParams.nodes.name(), nodes);
				PlatformLogger.log("Sending bulk update request | Total nodes: " + nodes.size());
				Response response = getResponse(request);
				if (StringUtils.isNotBlank(rootNodeId)) {
					if (StringUtils.endsWithIgnoreCase(rootNodeId, DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX))
						rootNodeId = rootNodeId.replace(DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX, "");
					response.put(ContentAPIParams.content_id.name(), rootNodeId);
				}
				if (null != newIdMap && !newIdMap.isEmpty())
					response.put(ContentAPIParams.identifiers.name(), newIdMap);
				return response;
			}
		} else {
			throw new ClientException("ERR_INVALID_HIERARCHY_DATA", "Hierarchy data is empty");
		}
		return new Response();
	}

	@SuppressWarnings("unchecked")
	private Response createNodeObject(String graphId, Entry<String, Object> entry, Map<String, String> idMap,
			Map<String, Node> nodeMap, Map<String, String> newIdMap, DefinitionDTO definition,
			Map<String, RelationDefinition> inRelDefMap, Map<String, RelationDefinition> outRelDefMap) {
		String nodeId = entry.getKey();
		String id = nodeId;
		String objectType = CONTENT_OBJECT_TYPE;
		Node tmpnode = null;
		Map<String, Object> map = (Map<String, Object>) entry.getValue();
		Boolean isNew = (Boolean) map.get("isNew");
		if (BooleanUtils.isTrue(isNew)) {
			id = Identifier.getIdentifier(graphId, Identifier.getUniqueIdFromTimestamp());
			newIdMap.put(nodeId, id);
		} else {
			tmpnode = getNodeForOperation(graphId, id, "create");
			if (null != tmpnode && StringUtils.isNotBlank(tmpnode.getIdentifier())) {
				id = tmpnode.getIdentifier();
				objectType = tmpnode.getObjectType();
			} else {
				throw new ResourceNotFoundException("ERR_CONTENT_NOT_FOUND",
						"Content not found with identifier: " + id);
			}
		}
		idMap.put(nodeId, id);
		Map<String, Object> metadata = (Map<String, Object>) map.get("metadata");
		metadata.put("identifier", id);
		metadata.put("objectType", objectType);
		if (BooleanUtils.isTrue(isNew)) {
			metadata.put("isNew", true);
			metadata.put("code", nodeId);
			metadata.put("status", "Draft");
			metadata.put(GraphDACParams.versionKey.name(), System.currentTimeMillis() + "");
			metadata.put(AuditProperties.createdOn.name(), DateUtils.formatCurrentDate());
			Boolean root = (Boolean) map.get("root");
			if (BooleanUtils.isNotTrue(root))
				metadata.put("visibility", "Parent");
		}
		metadata.put(AuditProperties.lastUpdatedOn.name(), DateUtils.formatCurrentDate());
		Response validateNodeResponse = validateNode(graphId, nodeId, metadata, tmpnode, definition);
		if (checkError(validateNodeResponse))
			return validateNodeResponse;
		try {
			Node node = ConvertToGraphNode.convertToGraphNode(metadata, definition, null);
			node.setGraphId(graphId);
			node.setNodeType(SystemNodeTypes.DATA_NODE.name());
			getRelationsToBeDeleted(node, metadata, inRelDefMap, outRelDefMap);
			nodeMap.put(id, node);
		} catch (Exception e) {
			throw new ClientException("ERR_CREATE_CONTENT_OBJECT", "Error creating content for the node: " + nodeId, e);
		}
		return null;
	}

	private Response validateNode(String graphId, String nodeId, Map<String, Object> metadata, Node tmpnode,
			DefinitionDTO definition) {
		Node node = null;
		try {
			node = ConvertToGraphNode.convertToGraphNode(metadata, definition, null);
		} catch (Exception e) {
			throw new ClientException("ERR_CREATE_CONTENT_OBJECT", "Error creating content for the node: " + nodeId, e);
		}
		if (null == tmpnode) {
			tmpnode = new Node();
			tmpnode.setGraphId(graphId);
			tmpnode.setObjectType(CONTENT_OBJECT_TYPE);
		}
		if (null != tmpnode.getMetadata() && !tmpnode.getMetadata().isEmpty()) {
			if (null == node.getMetadata())
				node.setMetadata(tmpnode.getMetadata());
			else {
				for (Entry<String, Object> entry : tmpnode.getMetadata().entrySet()) {
					if (!node.getMetadata().containsKey(entry.getKey()))
						node.getMetadata().put(entry.getKey(), entry.getValue());
				}
			}
		}
		if (null == node.getInRelations())
			node.setInRelations(tmpnode.getInRelations());
		if (null == node.getOutRelations())
			node.setOutRelations(tmpnode.getOutRelations());
		Request request = getRequest(graphId, GraphEngineManagers.NODE_MANAGER, "validateNode");
		request.put(GraphDACParams.node.name(), node);
		Response response = getResponse(request);
		return response;
	}

	// Method is introduced to decide whether image node should be created for
	// the content or not.
	private Node getNodeForUpdateHierarchy(String taxonomyId, String contentId, String operation, boolean isRoot) {
		Response response;
		if (isRoot)
			return getNodeForOperation(taxonomyId, contentId, operation);
		else {
			response = getDataNode(taxonomyId, contentId);

			PlatformLogger.log("Checking for Fetched Content Node (Not Image Node) for Content Id: " + contentId);
			if (checkError(response)) {
				throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_INVALID_CONTENT.name(),
						"Error! While Fetching the Content for Operation | [Content Id: " + contentId + "]");
			} else {
				Node node = (Node) response.get(GraphDACParams.node.name());
				if("Parent".equalsIgnoreCase(node.getMetadata().get("visibility").toString())) {
					return getNodeForOperation(taxonomyId, contentId, operation);
				}else {
					return node;
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void updateNodeHierarchyRelations(String graphId, Entry<String, Object> entry, Map<String, String> idMap,
			Map<String, Node> nodeMap) {
		String nodeId = entry.getKey();
		String id = idMap.get(nodeId);
		if (StringUtils.isBlank(id)) {
			Map<String, Object> map = (Map<String, Object>) entry.getValue();
			Boolean root = (Boolean) map.get("root");
			Node tmpnode = getNodeForUpdateHierarchy(graphId, nodeId, "update", root);
			if (null != tmpnode) {
				id = tmpnode.getIdentifier();
				tmpnode.setOutRelations(null);
				tmpnode.setInRelations(null);
				String visibility = (String) tmpnode.getMetadata().get("visibility");
				if (StringUtils.equalsIgnoreCase("Parent", visibility) || BooleanUtils.isTrue(root)) {
					idMap.put(nodeId, id);
					nodeMap.put(id, tmpnode);
				}
			} else {
				throw new ResourceNotFoundException("ERR_CONTENT_NOT_FOUND",
						"Content not found with identifier: " + id);
			}
		}
		if (StringUtils.isNotBlank(id)) {
			Node node = nodeMap.get(id);
			if (null != node) {
				Map<String, Object> map = (Map<String, Object>) entry.getValue();
				List<String> children = (List<String>) map.get("children");
				if (null != children) {
					List<Relation> outRelations = node.getOutRelations();
					if (null == outRelations)
						outRelations = new ArrayList<Relation>();
					int index = 1;
					for (String childId : children) {
						if (idMap.containsKey(childId))
							childId = idMap.get(childId);
						Relation rel = new Relation(id, RelationTypes.SEQUENCE_MEMBERSHIP.relationName(), childId);
						Map<String, Object> metadata = new HashMap<String, Object>();
						metadata.put(SystemProperties.IL_SEQUENCE_INDEX.name(), index);
						index += 1;
						rel.setMetadata(metadata);
						outRelations.add(rel);
					}
					Relation dummyContentRelation = new Relation(id, RelationTypes.SEQUENCE_MEMBERSHIP.relationName(),
							null);
					dummyContentRelation.setEndNodeObjectType(CONTENT_OBJECT_TYPE);
					outRelations.add(dummyContentRelation);
					Relation dummyContentImageRelation = new Relation(id,
							RelationTypes.SEQUENCE_MEMBERSHIP.relationName(), null);
					dummyContentImageRelation.setEndNodeObjectType(CONTENT_IMAGE_OBJECT_TYPE);
					outRelations.add(dummyContentImageRelation);
					node.setOutRelations(outRelations);
				}
			}
		}
	}

	private void getRelationsToBeDeleted(Node node, Map<String, Object> metadata,
			Map<String, RelationDefinition> inRelDefMap, Map<String, RelationDefinition> outRelDefMap) {
		if (null != metadata) {
			List<Relation> inRelations = node.getInRelations();
			if (null == inRelations)
				inRelations = new ArrayList<Relation>();
			List<Relation> outRelations = node.getOutRelations();
			if (null == outRelations)
				outRelations = new ArrayList<Relation>();
			for (Entry<String, Object> entry : metadata.entrySet()) {
				if (inRelDefMap.containsKey(entry.getKey())) {
					RelationDefinition rDef = inRelDefMap.get(entry.getKey());
					List<String> objectTypes = rDef.getObjectTypes();
					if (null != objectTypes) {
						for (String objectType : objectTypes) {
							Relation dummyInRelation = new Relation(null, rDef.getRelationName(), node.getIdentifier());
							dummyInRelation.setStartNodeObjectType(objectType);
							inRelations.add(dummyInRelation);
						}
					}
				} else if (outRelDefMap.containsKey(entry.getKey())) {
					RelationDefinition rDef = outRelDefMap.get(entry.getKey());
					List<String> objectTypes = rDef.getObjectTypes();
					if (null != objectTypes) {
						for (String objectType : objectTypes) {
							Relation dummyOutRelation = new Relation(node.getIdentifier(), rDef.getRelationName(),
									null);
							dummyOutRelation.setEndNodeObjectType(objectType);
							outRelations.add(dummyOutRelation);
						}
					}
				}
			}
			if (!inRelations.isEmpty())
				node.setInRelations(inRelations);
			if (!outRelations.isEmpty())
				node.setOutRelations(outRelations);
		}
	}

	private void getRelationDefMaps(DefinitionDTO definition, Map<String, RelationDefinition> inRelDefMap,
			Map<String, RelationDefinition> outRelDefMap) {
		if (null != definition) {
			if (null != definition.getInRelations() && !definition.getInRelations().isEmpty()) {
				for (RelationDefinition rDef : definition.getInRelations()) {
					if (StringUtils.isNotBlank(rDef.getTitle()) && null != rDef.getObjectTypes())
						inRelDefMap.put(rDef.getTitle(), rDef);
				}
			}
			if (null != definition.getOutRelations() && !definition.getOutRelations().isEmpty()) {
				for (RelationDefinition rDef : definition.getOutRelations()) {
					if (StringUtils.isNotBlank(rDef.getTitle()) && null != rDef.getObjectTypes())
						outRelDefMap.put(rDef.getTitle(), rDef);
				}
			}
		}
	}

	private Response updateMimeType(String contentId, String mimeType) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("mimeType", mimeType);
		map.put("versionKey", Platform.config.getString(DACConfigurationConstants.PASSPORT_KEY_BASE_PROPERTY));
		return updateContent(contentId, map);
	}

	private void validateNodeForContentType(Map<String, Object> map) {
		if (contentTypeList.contains((String) map.get(ContentAPIParams.contentType.name())))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_INVALID_CONTENT.name(),
					((String) map.get(ContentAPIParams.contentType.name())) + " is not a valid value for contentType");
	}

	private void validateForStatusUpdate(DefinitionDTO definition, Map<String, Object> map) throws Exception {
		if(BooleanUtils.isFalse(((Boolean) definition.getMetadata().get("allowStatusUpdate")))) {
			if (map.containsKey(ContentAPIParams.status.name()))
				throw new ClientException(ContentErrorCodes.ERR_CONTENT_UPDATE.name(),
					"Error! Status cannot be set while updating the Content.");
		}
	}
	
	/**
	 * This Method will update Content Node with DIAL Code in Neo4j.
	 * 
	 * Added for DIAL Code Implementation 
	 * 
	 * @author gauraw
	 * 
	 * 
	 * */
	public Response linkDialCode(String contentId,Map<String,Object>map) throws Exception{
		DefinitionDTO definition = getDefinition(GRAPH_ID, CONTENT_OBJECT_TYPE);
		Response responseNode = getDataNode(GRAPH_ID, contentId);
		if (checkError(responseNode))
			throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
					"Content not found with id: " + contentId);
		Node contentNode = (Node) responseNode.get(GraphDACParams.node.name());
		
		String graphPassportKey = Platform.config.getString(DACConfigurationConstants.PASSPORT_KEY_BASE_PROPERTY);
		map.put("versionKey", graphPassportKey);
		try {
			Node graphObj = ConvertToGraphNode.convertToGraphNode(map, definition, contentNode);
			graphObj.setGraphId(GRAPH_ID);
			graphObj.setIdentifier(contentId);
			graphObj.setObjectType(CONTENT_OBJECT_TYPE);
			Response updateResponse = updateDataNode(graphObj);
			return updateResponse;
		} catch (Exception e) {
			return ERROR("ERR_SERVER_ERROR", "Internal error", ResponseCode.SERVER_ERROR, e.getMessage(), null);
		}
		
	}
}
