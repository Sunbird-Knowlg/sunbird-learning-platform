package org.ekstep.content.operation.finalizer;

import com.rits.cloning.Cloner;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.Slug;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
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
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.graph.service.common.DACConfigurationConstants;
import org.ekstep.learning.hierarchy.store.HierarchyStore;
import org.ekstep.learning.contentstore.VideoStreamingJobRequest;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The Class BundleFinalizer, extends BaseFinalizer which mainly holds common
 * methods and operations of a ContentBody. PublishFinalizer holds methods which
 * perform ContentPublishPipeline operations
 */
public class PublishFinalizer extends BaseFinalizer {

	private VideoStreamingJobRequest streamJobRequest = new VideoStreamingJobRequest();
	
	private static final String TAXONOMY_ID = "domain";
	
	/** The Constant IDX_S3_KEY. */
	private static final int IDX_S3_KEY = 0;

	/** The Constant IDX_S3_URL. */
	private static final int IDX_S3_URL = 1;

	/** The BasePath. */
	protected String basePath;

	/** The ContentId. */
	protected String contentId;

	private static final String COLLECTION_MIMETYPE = "application/vnd.ekstep.content-collection";
	private static final String ECML_MIMETYPE = "application/vnd.ekstep.ecml-archive";
	
	private static ContentPackageExtractionUtil contentPackageExtractionUtil = new ContentPackageExtractionUtil();

	private ControllerUtil util = new ControllerUtil();

	private HierarchyStore hierarchyStore = new HierarchyStore();


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
	 * @param parameterMap
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
		if (node.getIdentifier().endsWith(".img")) {
			String updatedVersion = preUpdateNode(node.getIdentifier());
			node.getMetadata().put(GraphDACParams.versionKey.name(), updatedVersion);
		}

		node.setIdentifier(contentId);
		node.setObjectType(ContentWorkflowPipelineParams.Content.name());
		TelemetryManager.log("Compression Applied ? " + isCompressionApplied);
		// Create 'artifactUrl' Package
		String artifactUrl = null;
		String downloadUrl = null;
		String s3Key = null;
		File packageFile=null;
		boolean isAssetTypeContent = StringUtils.equalsIgnoreCase(
				(String) node.getMetadata().get(ContentWorkflowPipelineParams.contentType.name()),
				ContentWorkflowPipelineParams.Asset.name());

		if (BooleanUtils.isTrue(isCompressionApplied)) {
			// Get Content String
			String ecml = getECMLString(ecrf, ecmlType);
			// Write ECML File
			writeECMLFile(basePath, ecml, ecmlType);

			//upload snapshot of content into aws
			contentPackageExtractionUtil.uploadExtractedPackage(contentId, node, basePath, ExtractionType.snapshot, true);

			// Create 'ZIP' Package
			String zipFileName = basePath + File.separator + System.currentTimeMillis() + "_" + Slug.makeSlug(contentId)
					+ ContentConfigurationConstants.FILENAME_EXTENSION_SEPERATOR
					+ ContentConfigurationConstants.DEFAULT_ZIP_EXTENSION;
			TelemetryManager.info("Zip file name: " + zipFileName);
			createZipPackage(basePath, zipFileName);
			// Upload Package
			packageFile = new File(zipFileName);
			if (packageFile.exists()) {
				// Upload to S3
				String folderName = S3PropertyReader.getProperty(ARTEFACT_FOLDER);
				String[] urlArray = uploadToAWS(packageFile, getUploadFolderName(contentId, folderName));
				if (null != urlArray && urlArray.length >= 2)
					artifactUrl = urlArray[IDX_S3_URL];

				// Set artifact file For Node
				node.getMetadata().put(ContentWorkflowPipelineParams.artifactUrl.name(), artifactUrl);
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

		setPragma(node);

		
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
				TelemetryManager.log("Disabled full ECAR generation for collections. So not generating for collection id: " + node.getIdentifier());
			} else {
				TelemetryManager.log("Creating Full ECAR For Content Id: " + node.getIdentifier());
				String bundleFileName = getBundleFileName(contentId, node, EcarPackageType.FULL);
				
				downloadUrls = contentBundle.createContentManifestData(contents, childrenIds,
						null, EcarPackageType.FULL);
				urlArray = contentBundle.createContentBundle(contents, bundleFileName,
						ContentConfigurationConstants.DEFAULT_CONTENT_MANIFEST_VERSION, downloadUrls, node.getIdentifier());
				TelemetryManager.log("Full ECAR created For Content Id: " + node.getIdentifier());
				downloadUrl = urlArray[IDX_S3_URL];
				s3Key = urlArray[IDX_S3_KEY];
				TelemetryManager.log("Set 'downloadUrl' and 's3Key' i.e. Full Ecar Url and s3Key.");
			}

			TelemetryManager.log("Creating Spine ECAR For Content Id: " + node.getIdentifier());
			Map<String, Object> spineEcarMap = new HashMap<String, Object>();
			String spineEcarFileName = getBundleFileName(contentId, node, EcarPackageType.SPINE);
			downloadUrls = contentBundle.createContentManifestData(spineContents, childrenIds, null,
					EcarPackageType.SPINE);
			urlArray = contentBundle.createContentBundle(spineContents, spineEcarFileName,
					ContentConfigurationConstants.DEFAULT_CONTENT_MANIFEST_VERSION, downloadUrls, node.getIdentifier());
			TelemetryManager.log("Spine ECAR created For Content Id: " + node.getIdentifier());
			spineEcarMap.put(ContentWorkflowPipelineParams.ecarUrl.name(), urlArray[IDX_S3_URL]);
			spineEcarMap.put(ContentWorkflowPipelineParams.size.name(), getCloudStorageFileSize(urlArray[IDX_S3_KEY]));

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
			File pkgFile = (File) artifact;
			if (pkgFile.exists())
				pkgFile.delete();
			TelemetryManager.log("Deleting Local Artifact Package File: " + pkgFile.getAbsolutePath());
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
		node.getMetadata().put(ContentWorkflowPipelineParams.size.name(), getCloudStorageFileSize(s3Key));
		
		Node newNode = new Node(node.getIdentifier(), node.getNodeType(), node.getObjectType());
		newNode.setGraphId(node.getGraphId());
		newNode.setMetadata(node.getMetadata());
		newNode.setTags(node.getTags());

		if (BooleanUtils.isTrue(ContentConfigurationConstants.IS_ECAR_EXTRACTION_ENABLED)) {
			contentPackageExtractionUtil.copyExtractedContentPackage(contentId, newNode, ExtractionType.version);
			contentPackageExtractionUtil.copyExtractedContentPackage(contentId, newNode, ExtractionType.latest);
		}

		//update previewUrl for content streaming
		if (BooleanUtils.isFalse(isAssetTypeContent)) {
			updatePreviewURL(newNode);
		}

		try {
			TelemetryManager.log("Deleting the temporary folder: " + basePath);
			delete(new File(basePath));
		} catch (Exception e) {
			e.printStackTrace();
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

		TelemetryManager.log("Migrating the Image Data to the Live Object. | [Content Id: " + contentId + ".]");
		Response response = migrateContentImageObjectData(contentId, newNode);

		// delete image..
		Request request = getRequest(TAXONOMY_ID, GraphEngineManagers.NODE_MANAGER,
				"deleteDataNode");
		request.put(ContentWorkflowPipelineParams.node_id.name(), contentId + ".img");

		getResponse(request);

		List<String> streamableMimeType = Platform.config.hasPath("stream.mime.type") ?
				Arrays.asList(Platform.config.getString("stream.mime.type").split(",")) : Arrays.asList("video/mp4");
		if (streamableMimeType.contains((String) node.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name()))) {
			streamJobRequest.insert(contentId, (String) node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name()),
					(String) node.getMetadata().get(ContentWorkflowPipelineParams.channel.name()),
					String.valueOf(node.getMetadata().get(ContentWorkflowPipelineParams.pkgVersion.name())));
		}
		
		if (StringUtils.equalsIgnoreCase(
				((String) newNode.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name())),
				COLLECTION_MIMETYPE)) {
			Node publishedNode = util.getNode(TAXONOMY_ID, contentId);
			publishHierarchy(publishedNode);
		}

		if(Platform.config.hasPath("content.publish.invoke_web_hook") && StringUtils.equalsIgnoreCase("true",Platform.config.getString("content.publish.invoke_web_hook"))){
			PublishWebHookInvoker.invokePublishWebKook(contentId, ContentWorkflowPipelineParams.Live.name(), null);
		}
		TelemetryManager.log("Generating Telemetry Event. | [Content ID: " + contentId + "]");
		newNode.getMetadata().put(ContentWorkflowPipelineParams.prevState.name(),
				ContentWorkflowPipelineParams.Processing.name());
		return response;
	}


	private void publishHierarchy(Node publishedNode) {
		DefinitionDTO definition = util.getDefinition(publishedNode.getGraphId(), publishedNode.getObjectType());
		Map<String, Object> hierarchy = util.getHierarchyMap(publishedNode.getGraphId(), publishedNode.getIdentifier(), definition, null, null);
		hierarchyStore.saveOrUpdateHierarchy(publishedNode.getIdentifier(), hierarchy);
	}

	/**
	 * @param identifier
	 * @return
	 */
	private String preUpdateNode(String identifier) {
		identifier = identifier.replace(".img", "");
		Response response = getDataNode(TAXONOMY_ID, identifier);
		if (!checkError(response)) {
			Node node = (Node) response.get(GraphDACParams.node.name());
			Response updateResp = updateNode(node);
			if (!checkError(updateResp)) {
				return (String) updateResp.get(GraphDACParams.versionKey.name());
			} else {
				throw new ServerException(ContentErrorCodeConstants.PUBLISH_ERROR.name(),
						ContentErrorMessageConstants.CONTENT_IMAGE_MIGRATION_ERROR + " | [Content Id: " + contentId
								+ "]");
			}
		} else {
			throw new ServerException(ContentErrorCodeConstants.PUBLISH_ERROR.name(),
					ContentErrorMessageConstants.CONTENT_IMAGE_MIGRATION_ERROR + " | [Content Id: " + contentId + "]");
		}
	}

	private void setPragma(Node node) {
		List<String> mimeTypes = Arrays.asList("video/x-youtube", "application/pdf");
		if (mimeTypes.contains((String) node.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name()))) {
			Object obj = node.getMetadata().get("pragma");
			List<String> pragma = null;
			String value = "external";
			if (obj instanceof List) {
				pragma = (List<String>) obj;
			} else {
				pragma = new ArrayList<>();
			}
			if (!pragma.contains(value))
				pragma.add(value);
			node.getMetadata().put("pragma", pragma);
		}
	}

	private void updatePreviewURL(Node content) {
		if (null != content) {
			String mimeType = (String) content.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name());
			if (StringUtils.isNotBlank(mimeType)) {
				TelemetryManager.log("Checking Required Fields For: " + mimeType);
				switch (mimeType) {
					case "application/vnd.ekstep.content-collection":
						break;
					case "application/vnd.ekstep.plugin-archive":
						break;
					case "application/vnd.android.package-archive":
						break;
					case "assets":
						break;
					case "application/vnd.ekstep.ecml-archive":
					case "application/vnd.ekstep.html-archive":
					case "application/vnd.ekstep.h5p-archive":
						copyLatestS3Url(content);
						break;
					default:
						String artifactUrl = (String) content.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name());
						content.getMetadata().put(ContentWorkflowPipelineParams.previewUrl.name(), artifactUrl);
						List<String> streamableMimeType = Platform.config.hasPath("stream.mime.type")?
								Arrays.asList(Platform.config.getString("stream.mime.type").split(",")) : Arrays.asList("video/mp4");
						if(!streamableMimeType.contains(mimeType))
							content.getMetadata().put(ContentWorkflowPipelineParams.streamingUrl.name(), artifactUrl);
						break;
				}
			}
		}
	}
	
	private void copyLatestS3Url(Node content) {
		try {
			String latestFolderS3Url = contentPackageExtractionUtil.getS3URL(contentId, content, ExtractionType.latest);
			//copy into previewUrl
			content.getMetadata().put(ContentWorkflowPipelineParams.previewUrl.name(), latestFolderS3Url);
			content.getMetadata().put(ContentWorkflowPipelineParams.streamingUrl.name(), latestFolderS3Url);
		} catch (Exception e) {
			TelemetryManager.error("Something Went Wrong While copying latest s3 folder path to preveiwUrl for the content" + contentId, e);
		}
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
	
	private void removeExtraProperties(Node imgNode) {
		Response originalResponse = getDataNode(TAXONOMY_ID, imgNode.getIdentifier());
		if (checkError(originalResponse))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_INVALID_CONTENT.name(),
					"Error! While Fetching the Content for Operation | [Content Id: " + imgNode.getIdentifier() + "]");
		Node originalNode = (Node)originalResponse.get(GraphDACParams.node.name());
		
		Set<String> originalNodeMetaDataSet = originalNode.getMetadata().keySet();
		Set<String> imgNodeMetaDataSet = imgNode.getMetadata().keySet();
		Set<String> extraMetadata = originalNodeMetaDataSet.stream().filter(element -> !imgNodeMetaDataSet.contains(element)).collect(Collectors.toSet());
		
		if(!extraMetadata.isEmpty()) {
			for(String prop : extraMetadata) {
				imgNode.getMetadata().put(prop, null);
			}
		}
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
			if(ContentWorkflowPipelineParams.Unlisted.name().equalsIgnoreCase(publishType)) {
				contentImage.getMetadata().put(ContentWorkflowPipelineParams.status.name(), ContentWorkflowPipelineParams.Unlisted.name());
			} else {
				contentImage.getMetadata().put(ContentWorkflowPipelineParams.status.name(), ContentWorkflowPipelineParams.Live.name());
			}
			contentImage.getMetadata().put(ContentWorkflowPipelineParams.publish_type.name(), null);
			if (null != dbNode) {
				contentImage.setInRelations(dbNode.getInRelations());
				contentImage.setOutRelations(dbNode.getOutRelations());
				if(null == contentImage.getInRelations()) 
					contentImage.setInRelations(new ArrayList<>());
				if(null == contentImage.getOutRelations())
					contentImage.setOutRelations(new ArrayList<>());
				removeExtraProperties(contentImage);
			}
			TelemetryManager.info("Migrating the Content Body. | [Content Id: " + contentId + "]");

			// Get body only for ECML content.
			String mimeType = (String) contentImage.getMetadata().get("mimeType");
			if (StringUtils.equalsIgnoreCase(ECML_MIMETYPE, mimeType)) {
				System.out.println("Fetching content body for node: "+ contentId + " :: mimeType: " + mimeType);
				String imageBody = getContentBody(contentImageId);
				if (StringUtils.isNotBlank(imageBody)) {
					response = updateContentBody(contentId, imageBody);
					if (checkError(response))
						throw new ServerException(ContentErrorCodeConstants.PUBLISH_ERROR.name(),
								ContentErrorMessageConstants.CONTENT_BODY_MIGRATION_ERROR + " | [Content Id: " + contentId
										+ "]");
				}
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