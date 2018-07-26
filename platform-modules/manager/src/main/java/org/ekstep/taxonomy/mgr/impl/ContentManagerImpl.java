package org.ekstep.taxonomy.mgr.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.Slug;
import org.ekstep.common.dto.ExecutionContext;
import org.ekstep.common.dto.HeaderParam;
import org.ekstep.common.dto.NodeDTO;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.mgr.ConvertGraphNode;
import org.ekstep.common.mgr.ConvertToGraphNode;
import org.ekstep.common.optimizr.Optimizr;
import org.ekstep.common.router.RequestRouterPool;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.common.util.HttpRestUtil;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.content.dto.ContentSearchCriteria;
import org.ekstep.content.enums.ContentMetadata;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.content.mimetype.mgr.IMimeTypeManager;
import org.ekstep.content.mimetype.mgr.impl.BaseMimeTypeManager;
import org.ekstep.content.mimetype.mgr.impl.H5PMimeTypeMgrImpl;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.content.publish.PublishManager;
import org.ekstep.content.util.MimeTypeManagerFactory;
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
import org.ekstep.learning.contentstore.ContentStoreOperations;
import org.ekstep.learning.contentstore.ContentStoreParams;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.learning.util.CloudStore;
import org.ekstep.taxonomy.common.LanguageCodeMap;
import org.ekstep.taxonomy.enums.DialCodeEnum;
import org.ekstep.taxonomy.enums.TaxonomyAPIParams;
import org.ekstep.taxonomy.mgr.IContentManager;
import org.ekstep.taxonomy.util.YouTubeDataAPIV3Service;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.stereotype.Component;


import akka.actor.ActorRef;
import akka.pattern.Patterns;
import scala.Option;
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

	private static final String TAXONOMY_ID = "domain";

	private PublishManager publishManager = new PublishManager();

	private List<String> contentTypeList = Arrays.asList("Story", "Worksheet", "Game", "Simulation", "Puzzle",
			"Diagnostic", "ContentTemplate", "ItemTemplate");
	private List<String> finalStatus = Arrays.asList("Flagged", "Live", "Unlisted");
	private List<String> reviewStatus = Arrays.asList("Review", "FlagReview");

	private static final String ERR_DIALCODE_LINK_REQUEST = "Invalid Request.";
	private static final String DIALCODE_SEARCH_URI = Platform.config.hasPath("dialcode.search.uri")
			? Platform.config.getString("dialcode.search.uri") : "v3/dialcode/search";
			
	/*private BaseStorageService storageService;
	
	@PostConstruct
	public void init() {
		String storageKey = Platform.config.getString("azure_storage_key");
		String storageSecret = Platform.config.getString("azure_storage_secret");
		storageService = StorageServiceFactory.getStorageService(new StorageConfig("azure", storageKey, storageSecret));
	}*/

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ekstep.taxonomy.mgr.IContentManager#upload(java.lang.String,
	 * java.lang.String, java.io.File, java.lang.String)
	 */
	@Override
	public Response upload(String contentId, File uploadedFile, String mimeType) {
		boolean updateMimeType = false;

		try {
			if (StringUtils.isBlank(contentId))
				throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
						"Content Object Id is blank.");
			if (null == uploadedFile)
				throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_UPLOAD_OBJECT.name(),
						"Upload file is blank.");
			TelemetryManager.log("Uploaded File: " + uploadedFile.getAbsolutePath());

			if (StringUtils.endsWithIgnoreCase(contentId, DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX))
				throw new ClientException(ContentErrorCodes.OPERATION_DENIED.name(),
						"Invalid Content Identifier. | [Content Identifier does not Exists.]");

			Node node = getNodeForOperation(contentId, "upload");

			isNodeUnderProcessing(node, "Upload");
			if (StringUtils.isBlank(mimeType)) {
				mimeType = getMimeType(node);
			} else {
				setMimeTypeForUpload(mimeType, node);
				updateMimeType = true;
			}

			TelemetryManager.log("Mime-Type: " + mimeType + " | [Content ID: " + contentId + "]");
			TelemetryManager.log(
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
			TelemetryManager.error(message, e);
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
	public Response upload(String contentId, String fileUrl, String mimeType) {
		boolean updateMimeType = false;
		try {
			if (StringUtils.isBlank(contentId))
				throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
						"Content Object Id is blank.");
			if (StringUtils.isBlank(fileUrl))
				throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_UPLOAD_OBJECT.name(),
						"fileUrl is blank.");
			isImageContentId(contentId);
			Node node = getNodeForOperation(contentId, "upload");
			isNodeUnderProcessing(node, "Upload");
			if (StringUtils.isBlank(mimeType)) {
				mimeType = getMimeType(node);
			} else {
				setMimeTypeForUpload(mimeType, node);
				updateMimeType = true;
			}
			if (StringUtils.equals("video/x-youtube", mimeType))
				checkYoutubeLicense(fileUrl, node);
			TelemetryManager.log(
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
			TelemetryManager.error(message, e);
			return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), message, ResponseCode.SERVER_ERROR);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.ekstep.taxonomy.mgr.IContentManager#bundle(org.ekstep.common.dto.
	 * Request, java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Response bundle(Request request, String version) {
		String bundleFileName = (String) request.get("file_name");
		List<String> contentIds = (List<String>) request.get("content_identifiers");
		if (contentIds.size() > 1 && StringUtils.isBlank(bundleFileName))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_INVALID_BUNDLE_CRITERIA.name(),
					"ECAR file name should not be blank");

		Response response = searchNodes(TAXONOMY_ID, contentIds);
		Response listRes = copyResponse(response);
		if (checkError(response)) {
			return response;
		} else {
			List<Object> list = (List<Object>) response.get(ContentAPIParams.contents.name());
			List<Node> nodes = new ArrayList<Node>();
			List<Node> imageNodes = new ArrayList<Node>();
			if (null != list && !list.isEmpty()) {
				for (Object obj : list) {
					List<Node> nodelist = (List<Node>) obj;
					if (null != nodelist && !nodelist.isEmpty())
						nodes.addAll(nodelist);
				}

				validateInputNodesForBundling(nodes);

				for (Node node : nodes) {
					String contentImageId = getImageId(node.getIdentifier());
					Response getNodeResponse = getDataNode(TAXONOMY_ID, contentImageId);
					if (!checkError(getNodeResponse)) {
						node = (Node) getNodeResponse.get(GraphDACParams.node.name());
					}
					// Fetch body from content store.
					String body = getContentBody(node.getIdentifier());
					node.getMetadata().put(ContentAPIParams.body.name(), body);
					imageNodes.add(node);
				}
				if (imageNodes.size() == 1 && StringUtils.isBlank(bundleFileName))
					bundleFileName = (String) imageNodes.get(0).getMetadata().get(ContentAPIParams.name.name()) + "_"
							+ System.currentTimeMillis() + "_" + (String) imageNodes.get(0).getIdentifier();
			}
			bundleFileName = Slug.makeSlug(bundleFileName, true);
			String fileName = bundleFileName + ".ecar";

			// Preparing the Parameter Map for 'Bundle' Pipeline;
			InitializePipeline pipeline = new InitializePipeline(tempFileLocation, "node");
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(ContentAPIParams.nodes.name(), imageNodes);
			parameterMap.put(ContentAPIParams.bundleFileName.name(), fileName);
			parameterMap.put(ContentAPIParams.contentIdList.name(), contentIds);
			parameterMap.put(ContentAPIParams.manifestVersion.name(), DEFAULT_CONTENT_MANIFEST_VERSION);

			// Calling Content Workflow 'Bundle' Pipeline.
			listRes.getResult().putAll(pipeline.init(ContentAPIParams.bundle.name(), parameterMap).getResult());

			return listRes;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ekstep.taxonomy.mgr.IContentManager#optimize(java.lang.String,
	 * java.lang.String)
	 */
	public Response optimize(String contentId) {

		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_ID.name(), "Content Id is blank");

		Response response = new Response();
		Node node = getNodeForOperation(contentId, "optimize");

		isNodeUnderProcessing(node, "Optimize");

		String status = (String) node.getMetadata().get(ContentAPIParams.status.name());
		TelemetryManager.log("Content Status: " + status);
		if (!StringUtils.equalsIgnoreCase(ContentAPIParams.Live.name(), status)
				|| !StringUtils.equalsIgnoreCase(ContentAPIParams.Unlisted.name(), status))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(),
					"UnPublished content cannot be optimized");

		String downloadUrl = (String) node.getMetadata().get(ContentAPIParams.downloadUrl.name());
		TelemetryManager.log("Download Url: " + downloadUrl);
		if (StringUtils.isBlank(downloadUrl))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(),
					"ECAR file not available for content");

		if (!StringUtils.equalsIgnoreCase(ContentAPIParams.ecar.name(), FilenameUtils.getExtension(downloadUrl)))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(),
					"Content package is not an ECAR file");

		String optStatus = (String) node.getMetadata().get(ContentAPIParams.optStatus.name());
		TelemetryManager.log("Optimization Process Status: " + optStatus);
		if (StringUtils.equalsIgnoreCase(ContentAPIParams.Processing.name(), optStatus))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(),
					"Content optimization is in progress. Please try after the current optimization is complete");

		node.getMetadata().put(ContentAPIParams.optStatus.name(), ContentAPIParams.Processing.name());
		updateDataNode(node);
		Optimizr optimizr = new Optimizr();
		try {
			TelemetryManager.log("Invoking the Optimizer for Content Id: " + contentId);
			File minEcar = optimizr.optimizeECAR(downloadUrl);
			TelemetryManager.log("Optimized File: " + minEcar.getName() + " | [Content Id: " + contentId + "]");

			String folder = getFolderName(downloadUrl);
			TelemetryManager.log("Folder Name: " + folder + " | [Content Id: " + contentId + "]");

			//String[] arr = AWSUploader.uploadFile(folder, minEcar);
			String[] arr = CloudStore.uploadFile(folder, minEcar, true);
			response.put("url", arr[1]);
			TelemetryManager.log("URL: " + arr[1] + " | [Content Id: " + contentId + "]");

			TelemetryManager.log("Updating the Optimization Status. | [Content Id: " + contentId + "]");
			node.getMetadata().put(ContentAPIParams.optStatus.name(), "Complete");
			updateDataNode(node);
			TelemetryManager.log("Node Updated. | [Content Id: " + contentId + "]");

			TelemetryManager.log("Directory Cleanup. | [Content Id: " + contentId + "]");
			FileUtils.deleteDirectory(minEcar.getParentFile());
		} catch (Exception e) {
			node.getMetadata().put(ContentAPIParams.optStatus.name(), "Error");
			updateDataNode(node);
			response = ERROR(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(), e.getMessage(), ResponseCode.SERVER_ERROR);
		}
		return response;
	}

	public Response preSignedURL(String contentId, String fileName) {
		Response contentResp = getDataNode(TAXONOMY_ID, contentId);
		if (checkError(contentResp))
			return contentResp;
		Response response = new Response();
		String objectKey = S3PropertyReader.getProperty("s3.asset.folder")+"/"+contentId+"/"+ Slug.makeSlug(fileName);
		//String containerName = Platform.config.getString("azure_storage_container");
		String preSignedURL = CloudStore.getCloudStoreService().getSignedURL(CloudStore.getContainerName(), objectKey, Option.apply(600), Option.apply("w")); // storageService.getSignedURL(containerName, objectKey, Option.apply(600), Option.apply("w"));
		response.put(ContentAPIParams.content_id.name(), contentId);
		response.put(ContentAPIParams.pre_signed_url.name(), preSignedURL);
		response.put(ContentAPIParams.url_expiry.name(), S3PropertyReader.getProperty("s3.upload.url.expiry"));
		return response;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ekstep.taxonomy.mgr.IContentManager#publish(java.lang.String,
	 * java.lang.String)
	 */
	public Response publish(String contentId, Map<String, Object> requestMap) {

		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_ID.name(), "Content Id is blank");

		Response response = new Response();

		Node node = getNodeForOperation(contentId, "publish");
		isNodeUnderProcessing(node, "Publish");

		String publisher = null;
		if (null != requestMap && !requestMap.isEmpty()) {
			if (!validateList(requestMap.get("publishChecklist"))) {
				requestMap.put("publishChecklist", null);
			}
			publisher = (String) requestMap.get("lastPublishedBy");
			node.getMetadata().putAll(requestMap);

			node.getMetadata().put("rejectReasons", null);
			node.getMetadata().put("rejectComment", null);
		}
		if (StringUtils.isNotBlank(publisher)) {
			TelemetryManager.log("LastPublishedBy: " + publisher);
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

		TelemetryManager.log("Returning 'Response' Object.");
		if (StringUtils.endsWith(response.getResult().get("node_id").toString(), ".img")) {
			String identifier = (String) response.getResult().get("node_id");
			String new_identifier = identifier.replace(".img", "");
			TelemetryManager.log("replacing image id with content id in response" + identifier + new_identifier);
			response.getResult().replace("node_id", identifier, new_identifier);
		}
		return response;
	}

	@Override
	public Response review(String contentId, Request request) throws Exception {
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_ID.name(), "Content Id is blank");

		Response response = new Response();

		Node node = getNodeForOperation(contentId, "review");

		isNodeUnderProcessing(node, "Review");

		// Fetching body from Content Store.
		String body = getContentBody(node.getIdentifier());
		node.getMetadata().put(ContentAPIParams.body.name(), body);

		node.getMetadata().put(TaxonomyAPIParams.lastSubmittedOn.name(), DateUtils.formatCurrentDate());

		String mimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
		if (StringUtils.isBlank(mimeType)) {
			mimeType = "assets";
		}

		TelemetryManager.log("Mime-Type" + mimeType + " | [Content ID: " + contentId + "]");
		String artifactUrl = (String) node.getMetadata().get(ContentAPIParams.artifactUrl.name());
		String license = (String) node.getMetadata().get("license");
		if (StringUtils.equals("video/x-youtube", mimeType) && null != artifactUrl && StringUtils.isBlank(license))
			checkYoutubeLicense(artifactUrl, node);
		TelemetryManager.log("Getting Mime-Type Manager Factory. | [Content ID: " + contentId + "]");

		String contentType = (String) node.getMetadata().get("contentType");
		IMimeTypeManager mimeTypeManager = MimeTypeManagerFactory.getManager(contentType, mimeType);

		response = mimeTypeManager.review(contentId, node, false);

		TelemetryManager.log("Returning 'Response' Object: ", response.getResult());
		return response;
	}

	@Override
	public Response getHierarchy(String contentId, String mode) {
		Node node = getContentNode(TAXONOMY_ID, contentId, mode);

		TelemetryManager.log("Collecting Hierarchical Data For Content Id: " + node.getIdentifier());
		DefinitionDTO definition = getDefinition(TAXONOMY_ID, node.getObjectType());
		Map<String, Object> map = getContentHierarchyRecursive(TAXONOMY_ID, node, definition, mode);
		Map<String, Object> dataMap = contentCleanUp(map);
		Response response = new Response();
		response.put("content", dataMap);
		response.setParams(getSucessStatus());
		return response;
	}

	public Response create(Map<String, Object> map) throws Exception {
		if (null == map)
			return ERROR("ERR_CONTENT_INVALID_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);

		// Checking for resourceType if contentType resource
		// validateNodeForContentType(map);

		DefinitionDTO definition = getDefinition(TAXONOMY_ID, CONTENT_OBJECT_TYPE);

		restrictProps(definition, map, "status");
		String framework = (String) map.get("framework");
		if (StringUtils.isBlank(framework))
			map.put("framework", getDefaultFramework());

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
				node.setGraphId(TAXONOMY_ID);
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
	@Override
	public Response find(String contentId, String mode, List<String> fields) {
		Response response = new Response();

		Node node = getContentNode(TAXONOMY_ID, contentId, mode);

		TelemetryManager.log("Fetching the Data For Content Id: " + node.getIdentifier());
		DefinitionDTO definition = getDefinition(TAXONOMY_ID, node.getObjectType());
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
		Map<String, Object> contentMap = ConvertGraphNode.convertGraphNode(node, TAXONOMY_ID, definition, fields);

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

	public Response updateAllContents(String originalId, Map<String, Object> map) throws Exception {
		if (null == map)
			return ERROR("ERR_CONTENT_INVALID_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);

		DefinitionDTO definition = getDefinition(TAXONOMY_ID, CONTENT_OBJECT_TYPE);
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

	@SuppressWarnings("unchecked")
	public Response update(String contentId, Map<String, Object> map) throws Exception {
		if (null == map)
			return ERROR("ERR_CONTENT_INVALID_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);

		if (map.containsKey("dialcodes")) {
			map.remove("dialcodes");
		}
		DefinitionDTO definition = getDefinition(TAXONOMY_ID, CONTENT_OBJECT_TYPE);
		restrictProps(definition, map, "status", "framework");

		String originalId = contentId;
		String objectType = CONTENT_OBJECT_TYPE;
		map.put("objectType", CONTENT_OBJECT_TYPE);
		map.put("identifier", contentId);

		String mimeType = (String) map.get(TaxonomyAPIParams.mimeType.name());
		updateDefaultValuesByMimeType(map, mimeType);

		boolean isImageObjectCreationNeeded = false;
		boolean imageObjectExists = false;

		String contentImageId = contentId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
		Response getNodeResponse = getDataNode(TAXONOMY_ID, contentImageId);
		if (checkError(getNodeResponse)) {
			TelemetryManager.log("Content image not found: " + contentImageId);
			isImageObjectCreationNeeded = true;
			getNodeResponse = getDataNode(TAXONOMY_ID, contentId);
			TelemetryManager.log("Content node response: " + getNodeResponse);
		} else
			imageObjectExists = true;

		if (checkError(getNodeResponse)) {
			TelemetryManager.log("Content not found: " + contentId);
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
				if (StringUtils.equalsIgnoreCase(ContentAPIParams.screenshots.name(), prop) && null != map.get(prop)) {
					map.put(prop, null);
				} else {
					map.remove(prop);
				}

			}
		}

		Node graphNode = (Node) getNodeResponse.get(GraphDACParams.node.name());
		TelemetryManager.log("Graph node found: " + graphNode.getIdentifier());
		Map<String, Object> metadata = graphNode.getMetadata();
		String status = (String) metadata.get("status");
		String inputStatus = (String) map.get("status");
		if (null != inputStatus) {
			if (reviewStatus.contains(inputStatus) && !reviewStatus.contains(status)) {
				map.put("lastSubmittedOn", DateUtils.format(new Date()));
			}
		}

		boolean checkError = false;
		Response createResponse = null;
		if (finalStatus.contains(status)) {
			if (isImageObjectCreationNeeded) {
				graphNode.setIdentifier(contentImageId);
				graphNode.setObjectType(CONTENT_IMAGE_OBJECT_TYPE);
				metadata.put("status", "Draft");
				Object lastUpdatedBy = map.get("lastUpdatedBy");
				if (null != lastUpdatedBy)
					metadata.put("lastUpdatedBy", lastUpdatedBy);
				graphNode.setGraphId(TAXONOMY_ID);
				createResponse = createDataNode(graphNode);
				checkError = checkError(createResponse);
				if (!checkError) {
					TelemetryManager.log("Updating external props for: " + contentImageId);
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

		TelemetryManager.log("Updating content node: " + contentId);
		Node domainObj = ConvertToGraphNode.convertToGraphNode(map, definition, graphNode);
		domainObj.setGraphId(TAXONOMY_ID);
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

	@SuppressWarnings("unchecked")
	@Override
	public Response updateHierarchy(Map<String, Object> data) {
		String graphId = TAXONOMY_ID;
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
				TelemetryManager.log("Sending bulk update request | Total nodes: " + nodes.size());
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

	/**
	 * @param contents
	 * @param dialcodes
	 * @param resultMap
	 * @throws Exception
	 */
	private void updateDialCodeToContents(List<String> contents, List<String> dialcodes,
			Map<String, Set<String>> resultMap) throws Exception {
		Response resp;
		for (String contentId : contents) {
			Map<String, Object> map = new HashMap<String, Object>();
			if (!dialcodes.isEmpty())
				map.put(DialCodeEnum.dialcodes.name(), dialcodes);
			else
				map.put(DialCodeEnum.dialcodes.name(), null);

			Response responseNode = getDataNode(TAXONOMY_ID, contentId);
			if (checkError(responseNode)) {
				resultMap.get("invalidContentList").add(contentId);
			} else {
				resp = updateDialCode(map, contentId);
				if (!checkError(resp))
					resultMap.get("updateSuccessList").add(contentId);
				else
					resultMap.get("updateFailedList").add(contentId);
			}
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
			String returnNodeId = getId(nodeId);
			res.getResult().replace("node_id", nodeId, returnNodeId);
			return res;
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
		Response response = new Response();
		if (null != node) {
			String contentId = node.getIdentifier();
			// Checking if Content Image Object is being Updated, then return
			// the Original Content Id
			if (BooleanUtils.isTrue((Boolean) node.getMetadata().get(TaxonomyAPIParams.isImageObject.name()))) {
				node.getMetadata().remove(TaxonomyAPIParams.isImageObject.name());
				node.setIdentifier(node.getIdentifier() + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX);
			}

			TelemetryManager.log("Getting Update Node Request For Node ID: " + node.getIdentifier());
			Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "updateDataNode");
			updateReq.put(GraphDACParams.node.name(), node);
			updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());

			TelemetryManager.log("Updating the Node ID: " + node.getIdentifier());
			response = getResponse(updateReq);

			response.put(TaxonomyAPIParams.node_id.name(), contentId);
			TelemetryManager.log("Returning Node Update Response.");
		}
		return response;
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
		// TODO: Not the best Solution, need to optimize
		contentMap.remove("collections");
		contentMap.remove("usedByContent");
		contentMap.remove("item_sets");
		contentMap.remove("methods");
		contentMap.remove("libraries");
		return contentMap;
	}

	private Map<String, Object> contentCleanUp(Map<String, Object> map) {
		if (map.containsKey(TaxonomyAPIParams.identifier.name())) {
			String identifier = (String) map.get(TaxonomyAPIParams.identifier.name());
			TelemetryManager.log("Checking if identifier ends with .img" + identifier);
			if (StringUtils.endsWithIgnoreCase(identifier, DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX)) {
				String newIdentifier = identifier.replace(DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX, "");
				TelemetryManager.log("replacing image id with content id in response " + identifier + newIdentifier);
				map.replace(TaxonomyAPIParams.identifier.name(), identifier, newIdentifier);
			}
		}
		return map;
	}

	private Node getContentNode(String graphId, String contentId, String mode) {

		if (StringUtils.equalsIgnoreCase("edit", mode)) {
			String contentImageId = getImageId(contentId);
			Response responseNode = getDataNode(graphId, contentImageId);
			if (!checkError(responseNode)) {
				Node content = (Node) responseNode.get(GraphDACParams.node.name());
				return content;
			}
		}
		Response responseNode = getDataNode(graphId, contentId);
		if (checkError(responseNode))
			throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
					"Content not found with id: " + contentId);

		Node content = (Node) responseNode.get(GraphDACParams.node.name());
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
			body = getContentBody(getImageId(contentId));
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
				TelemetryManager.log("Response Params: " + response.getParams() + " | Code: "
						+ response.getResponseCode() + " | Result: " + response.getResult().keySet());
				return response;
			} else {
				return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
			}
		} catch (Exception e) {
			TelemetryManager.error("Error! Something went wrong: " + e.getMessage(), e);
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", e);
		}
	}

	private Node getNodeForOperation(String contentId, String operation) {
		Node node = new Node();

		TelemetryManager.log("Fetching the Content Node. | [Content ID: " + contentId + "]");
		String contentImageId = getImageId(contentId);
		Response response = getDataNode(TAXONOMY_ID, contentImageId);
		if (checkError(response)) {
			TelemetryManager.log("Unable to Fetch Content Image Node for Content Id: " + contentId);

			TelemetryManager.log("Trying to Fetch Content Node (Not Image Node) for Content Id: " + contentId);
			response = getDataNode(TAXONOMY_ID, contentId);

			TelemetryManager.log("Checking for Fetched Content Node (Not Image Node) for Content Id: " + contentId);
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

				String status = (String) node.getMetadata().get(TaxonomyAPIParams.status.name());
				if (StringUtils.isNotBlank(status)
						&& (StringUtils.equalsIgnoreCase(TaxonomyAPIParams.Live.name(), status)
								|| StringUtils.equalsIgnoreCase(TaxonomyAPIParams.Unlisted.name(), status)
								|| StringUtils.equalsIgnoreCase(TaxonomyAPIParams.Flagged.name(), status)))
					node = createContentImageNode(TAXONOMY_ID, contentImageId, node);
			}
		} else {
			// Content Image Node is Available so assigning it as node
			node = (Node) response.get(GraphDACParams.node.name());
			TelemetryManager.log("Getting Content Image Node and assigning it as node" + node.getIdentifier());
		}

		TelemetryManager.log("Returning the Node for Operation with Identifier: " + node.getIdentifier());
		return node;
	}

	private Node createContentImageNode(String taxonomyId, String contentImageId, Node node) {

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
		TelemetryManager.log("Returning Content Image Node Identifier" + nodeData.getIdentifier());
		return nodeData;
	}

	private Response createDataNode(Node node) {
		Response response = new Response();
		if (null != node) {
			Request request = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "createDataNode");
			request.put(GraphDACParams.node.name(), node);

			TelemetryManager.log("Creating the Node ID: " + node.getIdentifier());
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

	private String getDefaultFramework() {
		String channel = (String) ExecutionContext.getCurrent().getGlobalContext().get(HeaderParam.CHANNEL_ID.name());
		// TODO: check channel for default framework.
		if (Platform.config.hasPath("platform.framework.default"))
			return Platform.config.getString("platform.framework.default");
		else
			return "NCF";
	}

	// TODO: push this to publish-pipeline.
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

	private Response updateNode(String identifier, String objectType, Node domainNode) {
		domainNode.setGraphId(TAXONOMY_ID);
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
			tmpnode = getNodeForOperation(id, "create");
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
		if (metadata.containsKey("dialcodes")) {
			metadata.remove("dialcodes");
		}
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
			return getNodeForOperation(contentId, operation);
		else {
			response = getDataNode(taxonomyId, contentId);

			TelemetryManager.log("Checking for Fetched Content Node (Not Image Node) for Content Id: " + contentId);
			if (checkError(response)) {
				throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_INVALID_CONTENT.name(),
						"Error! While Fetching the Content for Operation | [Content Id: " + contentId + "]");
			} else {
				Node node = (Node) response.get(GraphDACParams.node.name());
				if ("Parent".equalsIgnoreCase(node.getMetadata().get("visibility").toString())) {
					return getNodeForOperation(contentId, operation);
				} else {
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
				children = children.stream().distinct().collect(Collectors.toList());
				if (null != children) {
					List<Relation> outRelations = node.getOutRelations();
					if (null == outRelations)
						outRelations = new ArrayList<Relation>();
					int index = 1;
					for (String childId : children) {
						if (idMap.containsKey(childId)) {
							childId = idMap.get(childId);
						} else {
							Node nodeForRelation = getNodeForUpdateHierarchy(graphId, childId, "update", false);
							childId = nodeForRelation.getIdentifier();
						}

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
		return update(contentId, map);
	}

	private void validateNodeForContentType(Map<String, Object> map) {
		if (contentTypeList.contains((String) map.get(ContentAPIParams.contentType.name())))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_INVALID_CONTENT.name(),
					((String) map.get(ContentAPIParams.contentType.name())) + " is not a valid value for contentType");
	}

	private void restrictProps(DefinitionDTO definition, Map<String, Object> map, String... props) {
		for (String prop : props) {
			Object allow = definition.getMetadata().get("allowupdate_" + prop);
			if (allow == null || BooleanUtils.isFalse((Boolean) allow)) {
				if (map.containsKey(prop))
					throw new ClientException(ContentErrorCodes.ERR_CONTENT_UPDATE.name(),
							"Error! " + prop + " can't be set for the content.");
			}

		}
	}

	/**
	 * @param param
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static List<String> getList(Object param) {
		List<String> paramList = null;
		try {
			paramList = (List<String>) param;
		} catch (Exception e) {
			String str = (String) param;
			paramList = Arrays.asList(str);
		}
		if (null != paramList) {
			paramList = paramList.stream().filter(x -> x != null && x != "" && x != " ").collect(Collectors.toList());
		}
		return paramList;
	}

	/**
	 * @param map
	 * @param contentId
	 * @return
	 */
	private Response updateDialCode(Map<String, Object> map, String contentId) {
		Response resp;
		try {
			String contentImageId = contentId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
			Response imageNodeResponse = getDataNode(TAXONOMY_ID, contentImageId);
			if (!checkError(imageNodeResponse)) {
				resp = updateDataNodes(map, Arrays.asList(contentId, contentImageId), TAXONOMY_ID);
			} else
				resp = updateDataNodes(map, Arrays.asList(contentId), TAXONOMY_ID);

			return resp;
		} catch (Exception e) {
			return ERROR(DialCodeEnum.ERR_DIALCODE_LINK.name(), DialCodeEnum.ERR_DIALCODE_LINK.name(),
					ResponseCode.SERVER_ERROR, e.getMessage(), null);
		}
	}

	/**
	 * @param channelId
	 * @param dialcodesList
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked" })
	private void validateDialCodes(String channelId, Set<String> dialcodesList) throws Exception {
		if (!dialcodesList.isEmpty()) {
			List<Object> resultList = null;
			List<String> dialcodes = new ArrayList<String>(dialcodesList);
			List<String> invalidDialCodeList = new ArrayList<String>(dialcodes);
			Integer dialcodeCount = dialcodes.size();

			Map<String, Object> requestMap = new HashMap<String, Object>();
			Map<String, Object> searchMap = new HashMap<String, Object>();
			Map<String, Object> data = new HashMap<String, Object>();
			data.put(ContentAPIParams.identifier.name(), dialcodes);
			searchMap.put("search", data);
			requestMap.put("request", searchMap);

			Map<String, String> headerParam = new HashMap<String, String>();
			headerParam.put("X-Channel-Id", channelId);

			Response searchResponse = HttpRestUtil.makePostRequest(DIALCODE_SEARCH_URI, requestMap, headerParam);
			if (searchResponse.getResponseCode() == ResponseCode.OK) {
				Map<String, Object> result = searchResponse.getResult();
				Integer count = (Integer) result.get(DialCodeEnum.count.name());
				if (dialcodeCount != count) {
					resultList = (List<Object>) result.get(DialCodeEnum.dialcodes.name());
					for (Object obj : resultList) {
						Map<String, Object> map = (Map<String, Object>) obj;
						String identifier = (String) map.get(ContentAPIParams.identifier.name());
						invalidDialCodeList.remove(identifier);
					}
					throw new ResourceNotFoundException(DialCodeEnum.ERR_DIALCODE_LINK.name(),
							"DIAL Code not found with id(s):" + invalidDialCodeList);
				}
			} else {
				throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(),
						"Something Went Wrong While Processing Your Request. Please Try Again After Sometime!");
			}
		}
	}

	/**
	 * This method will check YouTube License and Insert as Node MetaData
	 * 
	 * @param String
	 * @param Node
	 * @return
	 */
	private void checkYoutubeLicense(String artifactUrl, Node node) throws Exception {
		Boolean isValReq = Platform.config.hasPath("learning.content.youtube.validate.license")
				? Platform.config.getBoolean("learning.content.youtube.validate.license") : false;

		if (isValReq) {
			String licenseType = YouTubeDataAPIV3Service.getLicense(artifactUrl);
			if (StringUtils.equalsIgnoreCase("youtube", licenseType))
				node.getMetadata().put("license", "Standard YouTube License");
			else if (StringUtils.equalsIgnoreCase("creativeCommon", licenseType))
				node.getMetadata().put("license", "Creative Commons Attribution (CC BY)");
			else {
				TelemetryManager.log("Got Unsupported Youtube License Type : " + licenseType + " | [Content ID: "
						+ node.getIdentifier() + "]");
				throw new ClientException(TaxonomyErrorCodes.ERR_YOUTUBE_LICENSE_VALIDATION.name(),
						"Unsupported Youtube License!");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.taxonomy.mgr.IContentManager#linkDialCode(java.lang.String,
	 * java.util.List)
	 */
	@Override
	public Response linkDialCode(String channelId, Object reqObj) throws Exception {
		List<String> dialcodeList = new ArrayList<String>();
		List<String> contentList = new ArrayList<String>();
		Map<String, Set<String>> resultMap = initializeResultMap();
		List<Map<String, Object>> reqList = getRequestList(reqObj);

		validateDialCodeLinkRequest(channelId, reqList);

		for (Map<String, Object> map : reqList) {
			Object dialObj = map.get(DialCodeEnum.dialcode.name());
			Object contentObj = map.get("identifier");
			List<String> dialcodes = getList(dialObj);
			List<String> contents = getList(contentObj);
			dialcodeList.addAll(dialcodes);
			contentList.addAll(contents);
			updateDialCodeToContents(contents, dialcodes, resultMap);
		}

		Response resp = prepareResponse(resultMap);

		if (!checkError(resp) && ResponseCode.OK.name().equals(resp.getResponseCode().name())) {
			Map<String, Object> props = new HashMap<String, Object>();
			props.put(DialCodeEnum.dialcode.name(), dialcodeList);
			props.put("identifier", contentList);
			TelemetryManager.info("DIAL code linked to content", props);
		} else
			TelemetryManager.error(resp.getParams().getErrmsg());

		return resp;
	}

	/**
	 * @param reqObj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> getRequestList(Object reqObj) {
		List<Map<String, Object>> reqList = null;
		try {
			reqList = (List<Map<String, Object>>) reqObj;
		} catch (Exception e) {
			Map<String, Object> reqMap = (Map<String, Object>) reqObj;
			if (null != reqMap)
				reqList = Arrays.asList(reqMap);
		}
		return reqList;
	}

	/**
	 * @return
	 */
	private Map<String, Set<String>> initializeResultMap() {
		Map<String, Set<String>> resultMap = new HashMap<String, Set<String>>();
		resultMap.put("invalidContentList", new HashSet<String>());
		resultMap.put("updateFailedList", new HashSet<String>());
		resultMap.put("updateSuccessList", new HashSet<String>());
		return resultMap;
	}

	/**
	 * @param resultMap
	 * @return
	 */
	private Response prepareResponse(Map<String, Set<String>> resultMap) {
		Response resp;
		Set<String> invalidContentList = (Set<String>) resultMap.get("invalidContentList");
		Set<String> updateFailedList = (Set<String>) resultMap.get("updateFailedList");
		Set<String> updateSuccessList = (Set<String>) resultMap.get("updateSuccessList");

		if (invalidContentList.isEmpty() && updateFailedList.isEmpty()) {
			resp = new Response();
			resp.setParams(getSucessStatus());
			resp.setResponseCode(ResponseCode.OK);
		} else if (!invalidContentList.isEmpty() && updateSuccessList.size() == 0) {
			resp = new Response();
			resp.setResponseCode(ResponseCode.RESOURCE_NOT_FOUND);
			resp.setParams(getErrorStatus(DialCodeEnum.ERR_DIALCODE_LINK.name(),
					"Content not found with id(s):" + invalidContentList));
		} else {
			resp = new Response();
			resp.setResponseCode(ResponseCode.PARTIAL_SUCCESS);
			List<String> messages = new ArrayList<String>();
			if (!invalidContentList.isEmpty())
				messages.add("Content not found with id(s): " + String.join(",", invalidContentList));
			if (!updateFailedList.isEmpty())
				messages.add("Content link with dialcode(s) fialed for id(s): " + String.join(",", updateFailedList));

			resp.setParams(getErrorStatus(DialCodeEnum.ERR_DIALCODE_LINK.name(), String.join(",", messages)));
		}

		return resp;
	}

	/**
	 * @param channelId
	 * @param reqList
	 */
	private void validateDialCodeLinkRequest(String channelId, List<Map<String, Object>> reqList) throws Exception {
		if (null == reqList || 0 == reqList.size())
			throw new ClientException(DialCodeEnum.ERR_DIALCODE_LINK_REQUEST.name(), ERR_DIALCODE_LINK_REQUEST);

		Set<String> dialCodeList = new HashSet<String>();
		for (Map<String, Object> map : reqList) {
			if (null == map)
				throw new ClientException(DialCodeEnum.ERR_DIALCODE_LINK_REQUEST.name(), ERR_DIALCODE_LINK_REQUEST);
			Object dialObj = map.get(DialCodeEnum.dialcode.name());
			Object contentObj = map.get("identifier");
			List<String> dialcodes = getList(dialObj);
			List<String> contents = getList(contentObj);
			validateReqStructure(dialcodes, contents);
			if (!dialcodes.isEmpty())
				dialCodeList.addAll(dialcodes);
		}
		Boolean isValReq = Platform.config.hasPath("content.link.enable.dialcode.validation")
				? Platform.config.getBoolean("content.link.enable.dialcode.validation") : false;
		if (true == isValReq)
			validateDialCodes(channelId, dialCodeList);
	}

	/**
	 * @param dialcodes
	 * @param contents
	 */
	private void validateReqStructure(List<String> dialcodes, List<String> contents) {
		if (null == dialcodes || null == contents || contents.isEmpty())
			throw new ClientException(DialCodeEnum.ERR_DIALCODE_LINK_REQUEST.name(),
					"Pelase provide required properties in request.");

		int maxLimit = 10;
		if (Platform.config.hasPath("dialcode.link.content.max"))
			maxLimit = Platform.config.getInt("dialcode.link.content.max");

		if (dialcodes.size() >= maxLimit || contents.size() >= maxLimit)
			throw new ClientException(DialCodeEnum.ERR_DIALCODE_LINK_REQUEST.name(),
					"Max limit for link content to dialcode in a request is " + maxLimit);
	}

	/**
	 * @param map
	 * @param asList
	 * @param graphId
	 * @return
	 */
	private Response updateDataNodes(Map<String, Object> map, List<String> idList, String graphId) {
		Response response = new Response();
		TelemetryManager.log("Getting Update Node Request For Node ID: " + idList);
		Request updateReq = getRequest(graphId, GraphEngineManagers.NODE_MANAGER, "updateDataNodes");
		updateReq.put(GraphDACParams.node_ids.name(), idList);
		updateReq.put(GraphDACParams.metadata.name(), map);
		TelemetryManager.log("Updating DialCodes for :" + idList);
		response = getResponse(updateReq);
		TelemetryManager.log("Returning Node Update Response.");
		return response;
	}

	@Override
	public Response copyContent(String contentId, Map<String, Object> requestMap, String mode) {
		Node existingNode = validateCopyContentRequest(contentId, requestMap, mode);

		String mimeType = (String) existingNode.getMetadata().get("mimeType");
		Map<String, String> idMap = new HashMap<>();
		if (!StringUtils.equalsIgnoreCase(mimeType, "application/vnd.ekstep.content-collection")) {
			idMap = copyContentData(existingNode, requestMap);
		} else {
			idMap = copyCollectionContent(existingNode, requestMap, mode);
		}

		return OK("node_id", idMap);

	}

	/**
	 * @param existingNode
	 * @param requestMap
	 * @return
	 */
	private Map<String, String> copyContentData(Node existingNode, Map<String, Object> requestMap) {

		Node copyNode = copyMetdata(existingNode, requestMap);
		Response response = createDataNode(copyNode);
		if (checkError(response)) {
			throw new ServerException(response.getParams().getErr(), response.getParams().getErrmsg());
		}
		uploadArtifactUrl(existingNode, copyNode);
		uploadExternalProperties(existingNode, copyNode);

		Map<String, String> idMap = new HashMap<>();
		idMap.put(existingNode.getIdentifier(), copyNode.getIdentifier());
		return idMap;
	}

	private void uploadExternalProperties(Node existingNode, Node copyNode) {
		DefinitionDTO definition = getDefinition(TAXONOMY_ID, CONTENT_OBJECT_TYPE);
		// Copy the externalProperties in cassandra
		List<String> externalPropsList = getExternalPropsList(definition);
		Response bodyResponse = getContentProperties(existingNode.getIdentifier(), externalPropsList);
		if (!checkError(bodyResponse)) {
			Map<String, Object> extValues = (Map<String, Object>) bodyResponse.get(ContentStoreParams.values.name());
			if (null != extValues && !extValues.isEmpty()) {
				updateContentProperties(copyNode.getIdentifier(), extValues);
			}
		}
	}

	private void uploadArtifactUrl(Node existingNode, Node copyNode) {
		String artifactUrl = (String) existingNode.getMetadata().get("artifactUrl");
		if (StringUtils.isNotBlank(artifactUrl)) {
			Response response = null;
			String mimeType = (String) copyNode.getMetadata().get("mimeType");
			String contentType = (String) copyNode.getMetadata().get("contentType");

			if (!(StringUtils.equalsIgnoreCase("application/vnd.ekstep.ecml-archive", mimeType)
					|| StringUtils.equalsIgnoreCase("application/vnd.ekstep.content-collection", mimeType))) {
				IMimeTypeManager mimeTypeManager = MimeTypeManagerFactory.getManager(contentType, mimeType);
				BaseMimeTypeManager baseMimeTypeManager = new BaseMimeTypeManager();

				if (baseMimeTypeManager.isS3Url(artifactUrl)) {
					File file = copyURLToFile(artifactUrl);
					if (StringUtils.equalsIgnoreCase(mimeType, "application/vnd.ekstep.h5p-archive")) {
						H5PMimeTypeMgrImpl h5pManager = new H5PMimeTypeMgrImpl();
						response = h5pManager.upload(copyNode.getIdentifier(), copyNode, true, file);
					} else {
						response = mimeTypeManager.upload(copyNode.getIdentifier(), copyNode, file, false);
					}

				} else {
					response = mimeTypeManager.upload(copyNode.getIdentifier(), copyNode, artifactUrl);
				}

				if (null == response || checkError(response)) {
					throw new ClientException("ARTIFACT_NOT_COPIED", "ArtifactUrl not coppied.");
				}
			}

		}
	}

	/**
	 * @param existingNode
	 * @param requestMap
	 * @return
	 */
	private Map<String, String> copyCollectionContent(Node existingNode, Map<String, Object> requestMap, String mode) {
		// Copying Root Node
		Map<String, String> idMap = copyContentData(existingNode, requestMap);
		// Generating update hierarchy with copied parent content and calling
		// update hierarchy.
		copyhierarchy(existingNode, idMap, mode);
		return idMap;
	}

	/**
	 * @param existingNode
	 * @param idMap
	 * @param mode
	 */
	@SuppressWarnings("unchecked")
	private void copyhierarchy(Node existingNode, Map<String, String> idMap, String mode) {
		DefinitionDTO definition = getDefinition(TAXONOMY_ID, CONTENT_OBJECT_TYPE);
		Map<String, Object> contentMap = getContentHierarchyRecursive(existingNode.getGraphId(), existingNode,
				definition, mode);

		Map<String, Object> updateRequest = prepareUpdateHierarchyRequest(
				(List<Map<String, Object>>) contentMap.get("children"), existingNode, idMap);

		Response response = updateHierarchy(updateRequest);
		if (checkError(response)) {
			throw new ServerException(response.getParams().getErr(), response.getParams().getErrmsg());
		}
	}

	/**
	 * @param contentMap
	 * @param existingNode
	 * @param idMap
	 * @return
	 */
	private Map<String, Object> prepareUpdateHierarchyRequest(List<Map<String, Object>> children, Node existingNode,
			Map<String, String> idMap) {
		Map<String, Object> nodesModified = new HashMap<>();
		Map<String, Object> hierarchy = new HashMap<>();

		List<String> nullPropList = Platform.config.getStringList("learning.content.copy.null_prop_list");
		Map<String, Object> nullPropMap = new HashMap<>();
		nullPropList.forEach(i -> nullPropMap.put(i, null));
		Map<String, Object> parentHierarchy = new HashMap<>();
		parentHierarchy.put("children", new ArrayList<>());
		parentHierarchy.put("root", true);
		parentHierarchy.put("contentType", existingNode.getMetadata().get("contentType"));
		hierarchy.put(idMap.get(existingNode.getIdentifier()), parentHierarchy);
		populateHierarchy(children, nodesModified, hierarchy, idMap.get(existingNode.getIdentifier()), nullPropMap);

		Map<String, Object> data = new HashMap<>();
		data.put("nodesModified", nodesModified);
		data.put("hierarchy", hierarchy);

		return data;

	}

	/**
	 * @param children
	 * @param nodesModified
	 * @param hierarchy
	 * @param idMap
	 */
	private void populateHierarchy(List<Map<String, Object>> children, Map<String, Object> nodesModified,
			Map<String, Object> hierarchy, String parentId, Map<String, Object> nullPropMap) {
		if (null != children && !children.isEmpty()) {
			for (Map<String, Object> child : children) {
				String id = (String) child.get("identifier");
				if (StringUtils.equalsIgnoreCase("Parent", (String) child.get("visibility"))) {
					// NodesModified and hierarchy
					id = UUID.randomUUID().toString();
					Map<String, Object> metadata = new HashMap<>();
					metadata.putAll(child);
					metadata.putAll(nullPropMap);
					metadata.put("children", new ArrayList<>());
					metadata.remove("identifier");

					// TBD: Populate artifactUrl

					Map<String, Object> modifiedNode = new HashMap<>();
					modifiedNode.put("metadata", metadata);
					modifiedNode.put("root", false);
					modifiedNode.put("isNew", true);
					nodesModified.put(id, modifiedNode);
				}
				Map<String, Object> parentHierarchy = new HashMap<>();
				parentHierarchy.put("children", new ArrayList<>());
				parentHierarchy.put("root", false);
				parentHierarchy.put("contentType", child.get("contentType"));
				hierarchy.put(id, parentHierarchy);
				((List) ((Map<String, Object>) hierarchy.get(parentId)).get("children")).add(id);

				populateHierarchy((List<Map<String, Object>>) child.get("children"), nodesModified, hierarchy, id,
						nullPropMap);
			}
		}

	}

	/**
	 * @param existingNode
	 * @param requestMap
	 * @return
	 */
	private Node copyMetdata(Node existingNode, Map<String, Object> requestMap) {
		String newId = Identifier.getIdentifier(existingNode.getGraphId(), Identifier.getUniqueIdFromTimestamp());
		Node copyNode = new Node(newId, existingNode.getNodeType(), existingNode.getObjectType());
		Map<String, Object> metaData = new HashMap<>();
		metaData.putAll(existingNode.getMetadata());

		copyNode.setMetadata(metaData);
		copyNode.setGraphId(existingNode.getGraphId());

		copyNode.getMetadata().putAll(requestMap);
		copyNode.getMetadata().put("status", "Draft");
		List<String> nullPropList = Platform.config.getStringList("learning.content.copy.null_prop_list");
		Map<String, Object> nullPropMap = new HashMap<>();
		nullPropList.forEach(i -> nullPropMap.put(i, null));
		copyNode.getMetadata().putAll(nullPropMap);
		copyNode.getMetadata().put("origin", existingNode.getIdentifier());

		List<Relation> existingNodeOutRelations = existingNode.getOutRelations();
		List<Relation> copiedNodeOutRelations = new ArrayList<>();
		if (null != existingNodeOutRelations && !existingNodeOutRelations.isEmpty()) {
			for (Relation rel : existingNodeOutRelations) {
				if (!Arrays.asList("Content", "ContentImage").contains(rel.getEndNodeObjectType())) {
					copiedNodeOutRelations.add(new Relation(newId, rel.getRelationType(), rel.getEndNodeId()));
				}
			}
		}
		copyNode.setOutRelations(copiedNodeOutRelations);

		return copyNode;
	}

	/**
	 * @param contentId
	 * @param requestMap
	 * @param mode
	 */

	private Node validateCopyContentRequest(String contentId, Map<String, Object> requestMap, String mode) {
		if (null == requestMap)
			throw new ClientException("ERR_INVALID_REQUEST", "Please provide valid request");

		if (StringUtils.isBlank((String) requestMap.get("createdBy")))
			throw new ClientException("ERR_INVALID_CREATEDBY", "Please provide valid createdBy value");

		if (!validateList(requestMap.get("createdFor"))) {
			throw new ClientException("ERR_INVALID_CREATEDFOR", "Please provide valid createdFor value.");
		}
		if (!validateList(requestMap.get("organization"))) {
			throw new ClientException("ERR_INVALID_ORGANIZATION", "Please provide valid Organization value.");
		}

		Node node = getContentNode(TAXONOMY_ID, contentId, mode);
		List<String> notCoppiedContent = null;
		if (Platform.config.hasPath("learning.content.type.not.copied.list")) {
			notCoppiedContent = Platform.config.getStringList("learning.content.type.not.copied.list");
		}
		if (notCoppiedContent != null && notCoppiedContent.contains((String) node.getMetadata().get("contentType"))) {
			throw new ClientException(ContentErrorCodes.CONTENTTYPE_ASSET_CAN_NOT_COPY.name(),
					"ContentType " + (String) node.getMetadata().get("contentType") + " can not be coppied.");
		}

		String status = (String) node.getMetadata().get("status");
		List<String> invalidStatusList = Platform.config.getStringList("learning.content.copy.invalid_status_list");
		if (invalidStatusList.contains(status))
			throw new ClientException("ERR_INVALID_REQUEST",
					"Cannot copy content in " + status.toLowerCase() + " status");

		return node;
	}

	/**
	 * @param publishChecklistObj
	 */
	protected boolean validateList(Object publishChecklistObj) {
		try {
			List<String> publishChecklist = (List<String>) publishChecklistObj;
			if (null == publishChecklist || publishChecklist.isEmpty()) {
				return false;
			}

		} catch (Exception e) {
			return false;
		}
		return true;
	}

	protected File copyURLToFile(String fileUrl) {
		try {
			String fileName = getFieNameFromURL(fileUrl);
			File file = new File(fileName);
			FileUtils.copyURLToFile(new URL(fileUrl), file);
			return file;
		} catch (IOException e) {
			throw new ClientException(TaxonomyErrorCodes.ERR_INVALID_UPLOAD_FILE_URL.name(), "fileUrl is invalid.");
		}
	}

	protected String getFieNameFromURL(String fileUrl) {
		String fileName = FilenameUtils.getBaseName(fileUrl) + "_" + System.currentTimeMillis();
		if (!FilenameUtils.getExtension(fileUrl).isEmpty())
			fileName += "." + FilenameUtils.getExtension(fileUrl);
		return fileName;
	}
}
