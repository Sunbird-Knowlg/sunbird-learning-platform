package org.ekstep.content.operation.finalizer;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.ekstep.content.util.ContentMimeTypeFactoryUtil;
import org.ekstep.content.util.ContentPackageExtractionUtil;
import org.ekstep.content.util.PublishWebHookInvoker;
import org.ekstep.graph.service.common.DACConfigurationConstants;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.util.LogTelemetryEventUtil;
import com.ilimi.graph.common.mgr.Configuration;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.rits.cloning.Cloner;

/**
 * The Class BundleFinalizer, extends BaseFinalizer which mainly holds common
 * methods and operations of a ContentBody. PublishFinalizer holds methods which
 * perform ContentPublishPipeline operations
 */
public class PublishFinalizer extends BaseFinalizer {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(PublishFinalizer.class.getName());

	private static String COLLECTION_MIMETYPE = "application/vnd.ekstep.content-collection";

	/** The Constant IDX_S3_KEY. */
	private static final int IDX_S3_KEY = 0;

	/** The Constant IDX_S3_URL. */
	private static final int IDX_S3_URL = 1;

	/** The BasePath. */
	protected String basePath;

	/** The ContentId. */
	protected String contentId;

	private static final String s3Artifact = "s3.artifact.folder";

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
		LOGGER.info("Compression Applied ? " + isCompressionApplied);
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
			LOGGER.info("Zip File Name: " + zipFileName);
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

		// If user doesn't send 'compatibilityLevel' Then platform will set
		if (StringUtils
				.isBlank((String) node.getMetadata().get(ContentWorkflowPipelineParams.compatibilityLevel.name()))) {
			LOGGER.info("setting compatability level default values as 1.");
			node.getMetadata().put(ContentWorkflowPipelineParams.compatibilityLevel.name(), 1);

			LOGGER.info("setting compatability level for textbook");
			if (StringUtils.equalsIgnoreCase(
					(String) node.getMetadata().get(ContentWorkflowPipelineParams.contentType.name()),
					ContentWorkflowPipelineParams.TextBook.name())
					|| StringUtils.equalsIgnoreCase(
							(String) node.getMetadata().get(ContentWorkflowPipelineParams.contentType.name()),
							ContentWorkflowPipelineParams.TextBookUnit.name()))
				node.getMetadata().put(ContentWorkflowPipelineParams.compatibilityLevel.name(), 2);

			LOGGER.info("setting compatability level for youtube, pdf and doc");
			if (StringUtils
					.containsIgnoreCase((String) node.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name()),
							ContentWorkflowPipelineParams.youtube.name())
					|| StringUtils.containsIgnoreCase(
							(String) node.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name()),
							ContentWorkflowPipelineParams.pdf.name())
					|| StringUtils.containsIgnoreCase(
							(String) node.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name()),
							ContentWorkflowPipelineParams.msword.name()))
				node.getMetadata().put(ContentWorkflowPipelineParams.compatibilityLevel.name(), 4);
		}

		LOGGER.info("checking is the contentType is Asset");
		if (BooleanUtils.isFalse(isAssetTypeContent)) {
			// Create ECAR Bundle
			List<Node> nodes = new ArrayList<Node>();
			node.getMetadata().put(ContentWorkflowPipelineParams.status.name(),
					ContentWorkflowPipelineParams.Live.name());
			nodes.add(node);
			List<Map<String, Object>> contents = new ArrayList<Map<String, Object>>();
			List<String> childrenIds = new ArrayList<String>();
			getContentBundleData(node.getGraphId(), nodes, contents, childrenIds);

			LOGGER.info("Publishing the Un-Published Children.");
			publishChildren(nodes.stream().filter(n -> !n.getIdentifier().equalsIgnoreCase(node.getIdentifier()))
					.collect(Collectors.toList()));

			// TODO: Refactor this part since it is being called twice and
			// cloned
			contents = new ArrayList<Map<String, Object>>();
			childrenIds = new ArrayList<String>();
			getContentBundleData(node.getGraphId(), nodes, contents, childrenIds);
			// Cloning contents to spineContent
			Cloner cloner = new Cloner();
			List<Map<String, Object>> spineContents = cloner.deepClone(contents);

			LOGGER.info("Initialising the ECAR variant Map For Content Id: " + node.getIdentifier());
			Map<String, Object> variants = new HashMap<String, Object>();

			LOGGER.debug("Creating Full ECAR For Content Id: " + node.getIdentifier());
			String bundleFileName = getBundleFileName(contentId, node, EcarPackageType.FULL);
			ContentBundle contentBundle = new ContentBundle();
			Map<Object, List<String>> downloadUrls = contentBundle.createContentManifestData(contents, childrenIds,
					null, EcarPackageType.FULL);
			String[] urlArray = contentBundle.createContentBundle(contents, bundleFileName,
					ContentConfigurationConstants.DEFAULT_CONTENT_MANIFEST_VERSION, downloadUrls, node.getIdentifier());
			downloadUrl = urlArray[IDX_S3_URL];
			s3Key = urlArray[IDX_S3_KEY];
			LOGGER.info("Set 'downloadUrl' and 's3Key' i.e. Full Ecar Url and s3Key.");

			LOGGER.info("Creating Spine ECAR For Content Id: " + node.getIdentifier());
			Map<String, Object> spineEcarMap = new HashMap<String, Object>();
			String spineEcarFileName = getBundleFileName(contentId, node, EcarPackageType.SPINE);
			downloadUrls = contentBundle.createContentManifestData(spineContents, childrenIds, null,
					EcarPackageType.SPINE);
			urlArray = contentBundle.createContentBundle(spineContents, spineEcarFileName,
					ContentConfigurationConstants.DEFAULT_CONTENT_MANIFEST_VERSION, downloadUrls, node.getIdentifier());
			spineEcarMap.put(ContentWorkflowPipelineParams.ecarUrl.name(), urlArray[IDX_S3_URL]);
			spineEcarMap.put(ContentWorkflowPipelineParams.size.name(), getS3FileSize(urlArray[IDX_S3_KEY]));

			LOGGER.info("Adding Spine Ecar Information to Variants Map For Content Id: " + node.getIdentifier());
			variants.put(ContentWorkflowPipelineParams.spine.name(), spineEcarMap);

			LOGGER.info("Adding variants to Content Id: " + node.getIdentifier());
			node.getMetadata().put(ContentWorkflowPipelineParams.variants.name(), variants);
		}

		// Delete local compressed artifactFile
		Object artifact = node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name());
		if (null != artifact && artifact instanceof File) {
			File packageFile = (File) artifact;
			if (packageFile.exists())
				packageFile.delete();
			LOGGER.info("Deleting Local Artifact Package File: " + packageFile.getAbsolutePath());
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

		if (BooleanUtils.isTrue(ContentConfigurationConstants.IS_ECAR_EXTRACTION_ENABLED)) {
			ContentPackageExtractionUtil contentPackageExtractionUtil = new ContentPackageExtractionUtil();
			contentPackageExtractionUtil.copyExtractedContentPackage(contentId, newNode, ExtractionType.version);

			contentPackageExtractionUtil.copyExtractedContentPackage(contentId, newNode, ExtractionType.latest);
		}

		try {
			LOGGER.info("Deleting the temporary folder: " + basePath);
			delete(new File(basePath));
		} catch (Exception e) {
			LOGGER.error("Error deleting the temporary folder: " + basePath, e);
		}

		// Setting default version key for internal node update
		String graphPassportKey = Configuration.getProperty(DACConfigurationConstants.PASSPORT_KEY_BASE_PROPERTY);
		newNode.getMetadata().put(GraphDACParams.versionKey.name(), graphPassportKey);

		// Setting the Status of Content Image Node as 'Retired' since it's a
		// last Node Update in Publishing
		newNode.getMetadata().put(ContentWorkflowPipelineParams.status.name(),
				ContentWorkflowPipelineParams.Retired.name());

		newNode.setInRelations(node.getInRelations());
		newNode.setOutRelations(node.getOutRelations());
		newNode.setTags(node.getTags());

		LOGGER.info("Migrating the Image Data to the Live Object. | [Content Id: " + contentId + ".]");
		Response response = migrateContentImageObjectData(contentId, newNode);

		// delete image..
		Request request = getRequest(ContentConfigurationConstants.GRAPH_ID, GraphEngineManagers.NODE_MANAGER,
				"deleteDataNode");
		request.put(ContentWorkflowPipelineParams.node_id.name(), contentId + ".img");
		getResponse(request, LOGGER);

		PublishWebHookInvoker.invokePublishWebKook(contentId, ContentWorkflowPipelineParams.Live.name(), null);
		LOGGER.info("Generating Telemetry Event. | [Content ID: " + contentId + "]");
		newNode.getMetadata().put(ContentWorkflowPipelineParams.prevState.name(),
				ContentWorkflowPipelineParams.Processing.name());
		LogTelemetryEventUtil.logContentLifecycleEvent(contentId, newNode.getMetadata());
		return response;
	}

	private void publishChildren(List<Node> nodes) {
		LOGGER.debug("Node List: ", nodes);
		if (null != nodes && !nodes.isEmpty()) {
			LOGGER.info("Node is not Empty.");

			LOGGER.info("Fetching the Non-Collection Content Nodes.");
			List<Node> nonCollectionNodes = new ArrayList<Node>();
			List<Node> collectionNodes = new ArrayList<Node>();
			for (Node node : nodes) {
				if (null != node && null != node.getMetadata()) {
					LOGGER.info("Content Id: " + node.getIdentifier() + " has MimeType: "
							+ node.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name()));
					if (StringUtils.equalsIgnoreCase(
							(String) node.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name()),
							COLLECTION_MIMETYPE))
						collectionNodes.add(node);
					else
						nonCollectionNodes.add(node);
				}
			}

			// Publishing all Non-Collection nodes
			for (Node node : nonCollectionNodes)
				publishChild(node);

			// Publishing all Collection nodes
			for (Node node : collectionNodes)
				publishChild(node);
		}
	}

	private void publishChild(Node node) {
		if (null != node && null != node.getMetadata()) {
			LOGGER.info("Checking Node Id: " + node.getIdentifier());
			if ((StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.Draft.name(),
					(String) node.getMetadata().get(ContentWorkflowPipelineParams.status.name()))
					|| StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.Review.name(),
							(String) node.getMetadata().get(ContentWorkflowPipelineParams.status.name())))
					&& StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.Parent.name(),
							(String) node.getMetadata().get(ContentWorkflowPipelineParams.visibility.name()))) {
				LOGGER.info("Fetching 'MimeType' for Content Id: " + node.getIdentifier());
				String mimeType = (String) node.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name());
				if (StringUtils.isBlank(mimeType))
					throw new ClientException(ContentErrorCodeConstants.INVALID_MIME_TYPE.name(),
							ContentErrorMessageConstants.INVALID_CONTENT_MIMETYPE
									+ " | [Invalid or 'null' MimeType for Content Id: " + node.getIdentifier() + "]");
				LOGGER.info("MimeType: " + mimeType + " | [Content Id: " + node.getIdentifier() + "]");

				LOGGER.info("Publishing Content Id: " + node.getIdentifier());
				ContentMimeTypeFactoryUtil.getImplForService(mimeType).publish(node.getIdentifier(), node, false);
			}
		}
	}

	private String getS3KeyFromUrl(String s3Url) {
		String s3Key = "";
		if (StringUtils.isNotBlank(s3Url)) {
			try {
				URL url = new URL(s3Url);
				s3Key = url.getPath();
			} catch (Exception e) {
				LOGGER.error("Something Went Wrong While Getting 's3Key' from s3Url.", e);
			}
		}
		return s3Key;
	}

	private String getBundleFileName(String contentId, Node node, EcarPackageType packageType) {
		LOGGER.debug("Generating Bundle File Name For ECAR Package Type: " + packageType.name());
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
		LOGGER.debug("Content Id: " + contentId);
		LOGGER.debug("Content Image: ", contentImage);
		Response response = new Response();
		if (null != contentImage && StringUtils.isNotBlank(contentId)) {
			String contentImageId = contentId + ContentConfigurationConstants.DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;

			// Setting the Appropriate Metadata
			contentImage.setIdentifier(contentId);
			contentImage.setObjectType(ContentWorkflowPipelineParams.Content.name());
			contentImage.getMetadata().put(ContentWorkflowPipelineParams.status.name(),
					ContentWorkflowPipelineParams.Live.name());

			LOGGER.info("Migrating the Content Body. | [Content Id: " + contentId + "]");
			String imageBody = getContentBody(contentImageId);
			if (StringUtils.isNotBlank(imageBody)) {
				response = updateContentBody(contentId, getContentBody(contentImageId));
				if (checkError(response))
					throw new ServerException(ContentErrorCodeConstants.PUBLISH_ERROR.name(),
							ContentErrorMessageConstants.CONTENT_BODY_MIGRATION_ERROR + " | [Content Id: " + contentId
									+ "]");
			}

			LOGGER.info("Migrating the Content Object Metadata. | [Content Id: " + contentId + "]");
			response = updateNode(contentImage);
			if (checkError(response))
				throw new ServerException(ContentErrorCodeConstants.PUBLISH_ERROR.name(),
						ContentErrorMessageConstants.CONTENT_IMAGE_MIGRATION_ERROR + " | [Content Id: " + contentId
								+ "]");
		}

		LOGGER.debug("Returning the Response Object After Migrating the Content Body and Metadata.", response);
		return response;
	}

}
