package org.ekstep.content.operation.finalizer;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.slugs.Slug;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.content.common.ContentConfigurationConstants;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.common.EcarPackageType;
import org.ekstep.content.common.ExtractionType;
import org.ekstep.content.entity.Plugin;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.content.util.ContentBundle;
import org.ekstep.content.util.ContentPackageExtractionUtil;
import org.ekstep.content.util.PublishWebHookInvoker;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.graph.service.common.DACConfigurationConstants;
import org.ekstep.telemetry.logger.TelemetryManager;

import com.rits.cloning.Cloner;

/**
 * The Class BundleFinalizer, extends BaseFinalizer which mainly holds common
 * methods and operations of a ContentBody. PublishFinalizer holds methods which
 * perform ContentPublishPipeline operations
 */
public class PublishFinalizer extends BaseFinalizer {

	/** The Constant IDX_S3_KEY. */
	private static final int IDX_S3_KEY = 0;

	/** The Constant IDX_S3_URL. */
	private static final int IDX_S3_URL = 1;

	/** The BasePath. */
	protected String basePath;

	/** The ContentId. */
	protected String contentId;

	private static final String s3Artifact = "s3.artifact.folder";
	
	private static final String COLLECTION_MIMETYPE = "application/vnd.ekstep.content-collection";

	/**
	 * Instantiates a new PublishFinalizer and sets the base path and current
	 * content id for further processing.
	 *
	 * @param basePath
	 *            the base path is the location for content package file
	 *            handling and all manipulations.
	 * @param contentId
	 *            the content id is the identifier of content for which the
	 *            Processor is being processed currently.
	 */
	public PublishFinalizer(String basePath, String contentId) {
		if (!isValidBasePath(basePath))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Path does not Exist.]");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Invalid Content Id.]");
		this.basePath = basePath;
		this.contentId = contentId;
	}

	/**
	 * finalize()
	 *
	 * @param Map
	 *            the parameterMap
	 * 
	 *            checks if Node, ecrfType,ecmlType exists in the parameterMap
	 *            else throws ClientException Output only ECML format create
	 *            'artifactUrl' Get Content String write ECML File Create 'ZIP'
	 *            Package Upload Package Upload to S3 Set artifact file For Node
	 *            Download App Icon and create thumbnail Set Package Version
	 *            Create ECAR Bundle Delete local compressed artifactFile
	 *            Populate Fields and Update Node
	 * @return the response
	 */
	public Response finalize(Map<String, Object> parameterMap) {
		Node node = (Node) parameterMap.get(ContentWorkflowPipelineParams.node.name());
		Plugin ecrf = (Plugin) parameterMap.get(ContentWorkflowPipelineParams.ecrf.name());
		// Output only ECML format
		String ecmlType = ContentWorkflowPipelineParams.ecml.name();
		boolean isCompressionApplied = (boolean) parameterMap
				.get(ContentWorkflowPipelineParams.isCompressionApplied.name());
		if (null == node)
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [Invalid or null Node.]");
		if (null == ecrf)
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [Invalid or null ECRF Object.]");
		node.setIdentifier(contentId);
		node.setObjectType(ContentWorkflowPipelineParams.Content.name());
		TelemetryManager.log("Compression Applied ? " + isCompressionApplied);
		// Create 'artifactUrl' Package
		String artifactUrl = null;
		String downloadUrl = null;
		String s3Key = null;
		boolean isAssetTypeContent = StringUtils.equalsIgnoreCase(
				(String) node.getMetadata().get(ContentWorkflowPipelineParams.contentType.name()),
				ContentWorkflowPipelineParams.Asset.name());

		if (BooleanUtils.isTrue(isCompressionApplied)) {
			// Get Content String
			String ecml = getECMLString(ecrf, ecmlType);
			// Write ECML File
			writeECMLFile(basePath, ecml, ecmlType);
			// Create 'ZIP' Package
			String zipFileName = basePath + File.separator + System.currentTimeMillis() + "_" + Slug.makeSlug(contentId)
					+ ContentConfigurationConstants.FILENAME_EXTENSION_SEPERATOR
					+ ContentConfigurationConstants.DEFAULT_ZIP_EXTENSION;
			TelemetryManager.info("Zip file name: " + zipFileName);
			createZipPackage(basePath, zipFileName);
			// Upload Package
			File packageFile = new File(zipFileName);
			if (packageFile.exists()) {
				// Upload to S3
				String folderName = S3PropertyReader.getProperty(s3Artifact);
				String[] urlArray = uploadToAWS(packageFile, getUploadFolderName(contentId, folderName));
				if (null != urlArray && urlArray.length >= 2)
					artifactUrl = urlArray[IDX_S3_URL];

				// Set artifact file For Node
				node.getMetadata().put(ContentWorkflowPipelineParams.artifactUrl.name(), packageFile);
			}
		}
		// Download App Icon and create thumbnail
		createThumbnail(basePath, node);

		// Set Package Version
		double version = 1.0;
		if (null != node && null != node.getMetadata()
				&& null != node.getMetadata().get(ContentWorkflowPipelineParams.pkgVersion.name()))
			version = getDoubleValue(node.getMetadata().get(ContentWorkflowPipelineParams.pkgVersion.name())) + 1;
		node.getMetadata().put(ContentWorkflowPipelineParams.pkgVersion.name(), version);
		node.getMetadata().put(ContentWorkflowPipelineParams.lastPublishedOn.name(), formatCurrentDate());
		node.getMetadata().put(ContentWorkflowPipelineParams.flagReasons.name(), null);
		node.getMetadata().put(ContentWorkflowPipelineParams.body.name(), null);
		node.getMetadata().put(ContentWorkflowPipelineParams.publishError.name(), null);
		node.getMetadata().put(ContentWorkflowPipelineParams.variants.name(), null);

		
		String mimeType = (String) node.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name());
		if (StringUtils.containsIgnoreCase(mimeType, ContentWorkflowPipelineParams.youtube.name())
				|| StringUtils.containsIgnoreCase(mimeType, ContentWorkflowPipelineParams.pdf.name())
				|| StringUtils.containsIgnoreCase(mimeType, ContentWorkflowPipelineParams.msword.name())
				|| StringUtils.containsIgnoreCase(mimeType, ContentWorkflowPipelineParams.epub.name())
				|| StringUtils.containsIgnoreCase(mimeType, ContentWorkflowPipelineParams.h5p.name())
				|| StringUtils.containsIgnoreCase(mimeType, "x-url")) {
			TelemetryManager.info("setting compatability level for youtube, pdf and doc and epub: "+ node.getIdentifier() + " as 4.");
			node.getMetadata().put(ContentWorkflowPipelineParams.compatibilityLevel.name(), 4);
		}

		if (StringUtils.equalsIgnoreCase(
				(String) node.getMetadata().get(ContentWorkflowPipelineParams.contentType.name()),
				ContentWorkflowPipelineParams.Course.name())
				|| StringUtils.equalsIgnoreCase(
						(String) node.getMetadata().get(ContentWorkflowPipelineParams.contentType.name()),
						ContentWorkflowPipelineParams.CourseUnit.name())) {
			TelemetryManager.info("setting compatability level for course and course unit: "+ node.getIdentifier() + " as 4.");
			node.getMetadata().put(ContentWorkflowPipelineParams.compatibilityLevel.name(), 4);
		}

		if (StringUtils.equalsIgnoreCase(
				(String) node.getMetadata().get(ContentWorkflowPipelineParams.contentType.name()),
				ContentWorkflowPipelineParams.LessonPlan.name())
				|| StringUtils.equalsIgnoreCase(
						(String) node.getMetadata().get(ContentWorkflowPipelineParams.contentType.name()),
						ContentWorkflowPipelineParams.LessonPlanUnit.name())) {
			TelemetryManager.info("setting compatability level for lesson plan and lesson plan unit: " + node.getIdentifier() + " as 4.");
			node.getMetadata().put(ContentWorkflowPipelineParams.compatibilityLevel.name(), 4);
		}

		if (BooleanUtils.isFalse(isAssetTypeContent)) {
			// Create ECAR Bundle
			List<Node> nodes = new ArrayList<Node>();
			
			String publishType = (String) node.getMetadata().get(ContentWorkflowPipelineParams.publish_type.name());
			if(ContentWorkflowPipelineParams.Unlisted.name().equalsIgnoreCase(publishType)) {
				node.getMetadata().put(ContentWorkflowPipelineParams.status.name(), ContentWorkflowPipelineParams.Unlisted.name());
			} else {
				node.getMetadata().put(ContentWorkflowPipelineParams.status.name(), ContentWorkflowPipelineParams.Live.name());
			}

			nodes.add(node);
			List<Map<String, Object>> contents = new ArrayList<Map<String, Object>>();
			List<String> childrenIds = new ArrayList<String>();
			getContentBundleData(node.getGraphId(), nodes, contents, childrenIds);

			// Cloning contents to spineContent
			Cloner cloner = new Cloner();
			List<Map<String, Object>> spineContents = cloner.deepClone(contents);

			TelemetryManager.info("Initialising the ECAR variant Map For Content Id: " + node.getIdentifier());
			Map<String, Object> variants = new HashMap<String, Object>();
			Map<Object, List<String>> downloadUrls = null;
			String[] urlArray = null;
			ContentBundle contentBundle = new ContentBundle();
			
			if (COLLECTION_MIMETYPE.equalsIgnoreCase(mimeType) && disableCollectionFullECAR()) {
				System.out.println("Disabled full ECAR generation for collections. So not generating for collection id: " + node.getIdentifier());
			} else {
				TelemetryManager.info("Creating Full ECAR For Content Id: " + node.getIdentifier());
				String bundleFileName = getBundleFileName(contentId, node, EcarPackageType.FULL);
				
				downloadUrls = contentBundle.createContentManifestData(contents, childrenIds,
						null, EcarPackageType.FULL);
				urlArray = contentBundle.createContentBundle(contents, bundleFileName,
						ContentConfigurationConstants.DEFAULT_CONTENT_MANIFEST_VERSION, downloadUrls, node.getIdentifier());
				downloadUrl = urlArray[IDX_S3_URL];
				s3Key = urlArray[IDX_S3_KEY];
				TelemetryManager.log("Set 'downloadUrl' and 's3Key' i.e. Full Ecar Url and s3Key.");
			}
			
			TelemetryManager.info("Creating Spine ECAR For Content Id: " + node.getIdentifier());
			Map<String, Object> spineEcarMap = new HashMap<String, Object>();
			String spineEcarFileName = getBundleFileName(contentId, node, EcarPackageType.SPINE);
			downloadUrls = contentBundle.createContentManifestData(spineContents, childrenIds, null,
					EcarPackageType.SPINE);
			urlArray = contentBundle.createContentBundle(spineContents, spineEcarFileName,
					ContentConfigurationConstants.DEFAULT_CONTENT_MANIFEST_VERSION, downloadUrls, node.getIdentifier());
			spineEcarMap.put(ContentWorkflowPipelineParams.ecarUrl.name(), urlArray[IDX_S3_URL]);
			spineEcarMap.put(ContentWorkflowPipelineParams.size.name(), getS3FileSize(urlArray[IDX_S3_KEY]));

			TelemetryManager.log("Adding Spine Ecar Information to Variants Map For Content Id: " + node.getIdentifier());
			variants.put(ContentWorkflowPipelineParams.spine.name(), spineEcarMap);

			TelemetryManager.log("Adding variants to Content Id: " + node.getIdentifier());
			node.getMetadata().put(ContentWorkflowPipelineParams.variants.name(), variants);
			
			// if collection full ECAR creation disabled set spine as download url.
			if (COLLECTION_MIMETYPE.equalsIgnoreCase(mimeType) && disableCollectionFullECAR()) {
				downloadUrl = urlArray[IDX_S3_URL];
				s3Key = urlArray[IDX_S3_KEY];
			}
		}

		// Delete local compressed artifactFile
		Object artifact = node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name());
		if (null != artifact && artifact instanceof File) {
			File packageFile = (File) artifact;
			if (packageFile.exists())
				packageFile.delete();
			TelemetryManager.info("Deleting Local Artifact Package File: " + packageFile.getAbsolutePath());
			node.getMetadata().remove(ContentWorkflowPipelineParams.artifactUrl.name());

			if (StringUtils.isNotBlank(artifactUrl))
				node.getMetadata().put(ContentWorkflowPipelineParams.artifactUrl.name(), artifactUrl);
		}

		if (BooleanUtils.isTrue(isAssetTypeContent)) {
			downloadUrl = (String) node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name());
			s3Key = getS3KeyFromUrl((String) node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name()));
		}

		// Populate Fields and Update Node
		node.getMetadata().put(ContentWorkflowPipelineParams.s3Key.name(), s3Key);
		node.getMetadata().put(ContentWorkflowPipelineParams.downloadUrl.name(), downloadUrl);
		node.getMetadata().put(ContentWorkflowPipelineParams.size.name(), getS3FileSize(s3Key));
		
		Node newNode = new Node(node.getIdentifier(), node.getNodeType(), node.getObjectType());
		newNode.setGraphId(node.getGraphId());
		newNode.setMetadata(node.getMetadata());
		newNode.setTags(node.getTags());

		if (BooleanUtils.isTrue(ContentConfigurationConstants.IS_ECAR_EXTRACTION_ENABLED)) {
			ContentPackageExtractionUtil contentPackageExtractionUtil = new ContentPackageExtractionUtil();
			contentPackageExtractionUtil.copyExtractedContentPackage(contentId, newNode, ExtractionType.version);

			contentPackageExtractionUtil.copyExtractedContentPackage(contentId, newNode, ExtractionType.latest);
		}

		try {
			TelemetryManager.log("Deleting the temporary folder: " + basePath);
			delete(new File(basePath));
		} catch (Exception e) {
			TelemetryManager.error("Error deleting the temporary folder: " + basePath, e);
		}

		// Setting default version key for internal node update
		String graphPassportKey = Platform.config.getString(DACConfigurationConstants.PASSPORT_KEY_BASE_PROPERTY);
		newNode.getMetadata().put(GraphDACParams.versionKey.name(), graphPassportKey);

		// Setting the Status of Content Image Node as 'Retired' since it's a
		// last Node Update in Publishing
		newNode.getMetadata().put(ContentWorkflowPipelineParams.status.name(),
				ContentWorkflowPipelineParams.Retired.name());

		newNode.setInRelations(node.getInRelations());
		newNode.setOutRelations(node.getOutRelations());

		TelemetryManager.info("Migrating the Image Data to the Live Object. | [Content Id: " + contentId + ".]");
		Response response = migrateContentImageObjectData(contentId, newNode);

		// delete image..
		Request request = getRequest(ContentConfigurationConstants.GRAPH_ID, GraphEngineManagers.NODE_MANAGER,
				"deleteDataNode");
		request.put(ContentWorkflowPipelineParams.node_id.name(), contentId + ".img");

		getResponse(request);

		PublishWebHookInvoker.invokePublishWebKook(contentId, ContentWorkflowPipelineParams.Live.name(), null);
		TelemetryManager.log("Generating Telemetry Event. | [Content ID: " + contentId + "]");
		newNode.getMetadata().put(ContentWorkflowPipelineParams.prevState.name(),
				ContentWorkflowPipelineParams.Processing.name());
		return response;
	}

	private String getS3KeyFromUrl(String s3Url) {
		String s3Key = "";
		if (StringUtils.isNotBlank(s3Url)) {
			try {
				URL url = new URL(s3Url);
				s3Key = url.getPath();
			} catch (Exception e) {
				TelemetryManager.error("Something Went Wrong While Getting 's3Key' from s3Url." + s3Url, e);
			}
		}
		return s3Key;
	}

	private String getBundleFileName(String contentId, Node node, EcarPackageType packageType) {
		TelemetryManager.info("Generating Bundle File Name For ECAR Package Type: " + packageType.name());
		String fileName = "";
		if (null != node && null != node.getMetadata() && null != packageType) {
			String suffix = "";
			if (packageType != EcarPackageType.FULL)
				suffix = "_" + packageType.name();
			fileName = Slug.makeSlug((String) node.getMetadata().get(ContentWorkflowPipelineParams.name.name()), true)
					+ "_" + System.currentTimeMillis() + "_" + contentId + "_"
					+ node.getMetadata().get(ContentWorkflowPipelineParams.pkgVersion.name()) + suffix + ".ecar";
		}
		return fileName;
	}

	private Response migrateContentImageObjectData(String contentId, Node contentImage) {
		Response response = new Response();
		if (null != contentImage && StringUtils.isNotBlank(contentId)) {
			String contentImageId = contentId + ContentConfigurationConstants.DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
			
			TelemetryManager.info("Fetching the Content Image Node for actual state . | [Content Id: " + contentImageId + "]");
			Response getDataNodeResponse = getDataNode(contentImage.getGraphId(), contentImageId);
			Node dbNode = (Node) getDataNodeResponse.get(ContentWorkflowPipelineParams.node.name());
			
			TelemetryManager.info("Setting the Metatdata for Image Node . | [Content Id: " + contentImageId + "]");
			// Setting the Appropriate Metadata
			contentImage.setIdentifier(contentId);
			contentImage.setObjectType(ContentWorkflowPipelineParams.Content.name());
			
			String publishType = (String) contentImage.getMetadata().get(ContentWorkflowPipelineParams.publish_type.name());
			TelemetryManager.info("In migrateContentImageObjectData ******* publishType: " + publishType + " ***");
			if(ContentWorkflowPipelineParams.Unlisted.name().equalsIgnoreCase(publishType)) {
				contentImage.getMetadata().put(ContentWorkflowPipelineParams.status.name(), ContentWorkflowPipelineParams.Unlisted.name());
			} else {
				contentImage.getMetadata().put(ContentWorkflowPipelineParams.status.name(), ContentWorkflowPipelineParams.Live.name());
			}
			contentImage.getMetadata().put(ContentWorkflowPipelineParams.publish_type.name(), null);
			if (null != dbNode) {
				contentImage.setInRelations(dbNode.getInRelations());
				contentImage.setOutRelations(dbNode.getOutRelations());
			}
				
			TelemetryManager.info("Migrating the Content Body. | [Content Id: " + contentId + "]");
			String imageBody = getContentBody(contentImageId);
			if (StringUtils.isNotBlank(imageBody)) {
				response = updateContentBody(contentId, getContentBody(contentImageId));
				if (checkError(response))
					throw new ServerException(ContentErrorCodeConstants.PUBLISH_ERROR.name(),
							ContentErrorMessageConstants.CONTENT_BODY_MIGRATION_ERROR + " | [Content Id: " + contentId
									+ "]");
			}

			TelemetryManager.log("Migrating the Content Object Metadata. | [Content Id: " + contentId + "]");
			response = updateNode(contentImage);
			if (checkError(response))
				throw new ServerException(ContentErrorCodeConstants.PUBLISH_ERROR.name(),
						ContentErrorMessageConstants.CONTENT_IMAGE_MIGRATION_ERROR + " | [Content Id: " + contentId
								+ "]");
		}

		TelemetryManager.log("Returning the Response Object After Migrating the Content Body and Metadata.");
		return response;
	}
	
	private boolean disableCollectionFullECAR() {
		if (Platform.config.hasPath("publish.collection.fullecar.disable"))
			return "true".equalsIgnoreCase(Platform.config.getString("publish.collection.fullecar.disable"));
		else 
			return false;
	}
}
