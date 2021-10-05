package org.sunbird.content.util;

import akka.dispatch.Dispatcher;
import akka.dispatch.ExecutionContexts;
import akka.dispatch.OnComplete;
import akka.dispatch.OnSuccess;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.Slug;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.common.util.HttpDownloadUtility;
import org.sunbird.common.util.S3PropertyReader;
import org.sunbird.common.util.UnzipUtility;
import org.sunbird.content.common.ContentConfigurationConstants;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.common.ExtractionType;
import org.sunbird.content.enums.ContentErrorCodeConstants;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.learning.common.enums.ContentAPIParams;
import org.sunbird.learning.common.enums.ContentErrorCodes;
import org.sunbird.learning.util.CloudStore;
import org.sunbird.telemetry.logger.TelemetryManager;
import scala.Option;
import scala.collection.immutable.List;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.impl.ExecutionContextImpl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

/**
 * The Class ContentPackageExtractionUtil.
 * 
 * @author Mohammad Azharuddin
 */
public class ContentPackageExtractionUtil {
	
	/** The Constant AWS_UPLOAD_RESULT_URL_INDEX. */
//	private static final int AWS_UPLOAD_RESULT_URL_INDEX = 1;

	/** The Constant DASH. */
	private static final String DASH = "-";

	/** The Constant s3Content. */
	private static final String CONTENT_FOLDER = "cloud_storage.content.folder";

	/** The Constant S3_CONTENT_PLUGIN_DIRECTORY. */
	private static final String CONTENT_PLUGIN_DIRECTORY = "content-plugins";

	/** The Constant TEMP_FILE_LOCATION. */
	private static final String TEMP_FILE_LOCATION = "/data/contentBundle/";

	private static final String H5P_MIMETYPE = "application/vnd.ekstep.h5p-archive";

	/** The extractable mime types. */
	private static Map<String, String> extractableMimeTypes = new HashMap<>();
	private static Map<String, String> extractablePackageExtensions = new HashMap<>();

	static {
		extractableMimeTypes.put("application/vnd.ekstep.ecml-archive", "ECML Type Content");
		extractableMimeTypes.put("application/vnd.ekstep.html-archive", "HTML Type Content");
		extractableMimeTypes.put("application/vnd.ekstep.plugin-archive", "Plugin Type Content");
		extractableMimeTypes.put("application/vnd.ekstep.h5p-archive", "H5P Type Content");

		extractablePackageExtensions.put(".zip", "Zip File");
		extractablePackageExtensions.put(".h5p", "H5P File");
		extractablePackageExtensions.put(".epub", "EPUB File");
	}

	public void copyExtractedContentPackage(String contentId, Node node, ExtractionType extractionType) {
		// Validating the Parameters
		TelemetryManager.log("Validating Node Object.");
		if (null == node)
			throw new ClientException(ContentErrorCodes.INVALID_NODE.name(),
					"Error! Content (Node Object) cannot be 'null'");

		TelemetryManager.log("Validating Extraction Type.");
		if (null == extractionType)
			throw new ClientException(ContentErrorCodes.INVALID_EXTRACTION.name(),
					"Error! Invalid Content Extraction Type.");

		String mimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
		if (extractableMimeTypes.containsKey(mimeType)) {

			// Checking if the Snapshost Extraction of Content is in place or not
			if (!isExtractedSnapshotExist(node, extractionType))
				throw new ClientException(ContentErrorCodes.INVALID_SNAPSHOT.name(),
						"Error! Snapshot Type Extraction doesn't Exists.");

			// Fetching Source Prefix For Copy Objects in S3
			String sourcePrefix = getExtractionPath(contentId, node, ExtractionType.snapshot);
			TelemetryManager.log("Source Prefix: " + sourcePrefix);

			// Fetching Destination Prefix For Copy Objects in S3
			String destinationPrefix = getExtractionPath(contentId, node, extractionType);
			TelemetryManager.log("Destination Prefix: " + destinationPrefix);

			// Copying Objects
			TelemetryManager.log("Copying Objects...STARTED");
			if(ExtractionType.version.name().equals(extractionType.name())){
				ExecutorService pool = null;
				try {
					pool = Executors.newFixedThreadPool(1);
					pool.execute(new Runnable() {
						@Override
						public void run() {
							CloudStore.copyObjectsByPrefix(sourcePrefix, destinationPrefix, true);
						}
					});
				} catch (Exception e) {
					TelemetryManager.error("Error while copying object by prefix", e);
				} finally {
					if (null != pool)
						pool.shutdown();
				}
				TelemetryManager.log("Copying Objects...DONE | Under: " + destinationPrefix);
			} else if(ExtractionType.latest.name().equals(extractionType.name())){
				try	{
					CloudStore.copyObjectsByPrefix(sourcePrefix, destinationPrefix, true);
					TelemetryManager.log("Copying Objects...DONE | Under: " + destinationPrefix);
				} catch(Exception e) {
					TelemetryManager.error("Error while copying object by prefix", e);
				}
			}
		}
	}

	/**
	 * Extract content package.
	 * @param contentId content identifier
	 * @param node
	 *            the node
	 * @param uploadedFile
	 *            the uploaded file
	 * @param extractionType
	 *            the extraction type
     * @param slugFile slug file
	 */
	public void extractContentPackage(String contentId, Node node, File uploadedFile, ExtractionType extractionType,
			boolean slugFile) {
		uploadedFile = Slug.createSlugFile(uploadedFile);
		TelemetryManager.log("Node: " + node);
		TelemetryManager.log("Uploaded File: " + uploadedFile.getName() + " - " + uploadedFile.exists() + " - "
				+ uploadedFile.getAbsolutePath());
		TelemetryManager.log("Extraction Type: " + extractionType);

		// Validating the Parameters
		TelemetryManager.log("Validating Node Object.");
		if (null == node)
			throw new ClientException(ContentErrorCodes.INVALID_NODE.name(),
					"Error! Content (Node Object) cannot be 'null'");

		TelemetryManager.log("Validating Uploaded File.");
		String mimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
		TelemetryManager.log("Mime Type: " + mimeType);
		if (!uploadedFile.exists()
				|| (!isValidSnapshotFile(uploadedFile.getName()) && extractableMimeTypes.containsKey(mimeType)))
			throw new ClientException(ContentErrorCodes.INVALID_FILE.name(), "Error! File doesn't Exist.");

		TelemetryManager.log("Validating Extraction Type.");
		if (null == extractionType)
			throw new ClientException(ContentErrorCodes.INVALID_EXTRACTION.name(),
					"Error! Invalid Content Extraction Type.");

		if (extractableMimeTypes.containsKey(mimeType)) {
			TelemetryManager.log("Given Content Belongs to Extractable MimeType Category.");
			String extractionBasePath = getBasePath(contentId);
			try {
				UnzipUtility unzipUtility = new UnzipUtility();
				if (StringUtils.equalsIgnoreCase(H5P_MIMETYPE,
						(String) node.getMetadata().get(ContentAPIParams.mimeType.name()))) {
					// Download the H5P Libraries
					String h5pLibraryDownloadPath = getBasePath(contentId);
					File h5pLibraryPackageFile = HttpDownloadUtility.downloadFile(getH5PLibraryPath(),
							h5pLibraryDownloadPath);
					// Un-Zip the H5P Library Files
					unzipUtility.unzip(h5pLibraryPackageFile.getAbsolutePath(), extractionBasePath);
					// Cleanup
					try {
						FileUtils.deleteDirectory(new File(h5pLibraryDownloadPath));
					} catch (Exception e) {
						TelemetryManager.error("Unable to delete H5P library directory.",  e);
					}
					// UnZip the Content Package
					unzipUtility.unzip(uploadedFile.getAbsolutePath(), extractionBasePath + "/content");
				} else {
					// UnZip the Content Package
					unzipUtility.unzip(uploadedFile.getAbsolutePath(), extractionBasePath);
				}

				// upload Extracted Content Package
				uploadExtractedPackage(contentId, node, extractionBasePath, extractionType, slugFile);
			} catch (IOException e) {
				TelemetryManager.error("Error! While unzipping the content package file: "+ e.getMessage(), e);
			} catch (Exception e) {
				TelemetryManager.error("Error! Something went wrong while extracting the content package file.", e);
			}
		}
	}

	private String getH5PLibraryPath() {
		String path = Platform.config.getString(ContentConfigurationConstants.DEFAULT_H5P_LIBRARY_PATH_PROPERTY_KEY);
		if (StringUtils.isBlank(path))
			throw new ClientException(ContentErrorCodeConstants.INVALID_LIBRARY.name(),
					ContentErrorMessageConstants.INVALID_H5P_LIBRARY + " | [Invalid H5P Library Package Path.]");
		TelemetryManager.info("Fetched H5P Library Path: " + path);
		return path;
	}

	/**
	 * Extract package.
	 *
     * @param contentId  content identifier
	 * @param node
	 *            the node
	 * @param basePath
	 *            the base path
	 * @param extractionType
	 *            the extraction type
     * @param slugFile slug file
	 */
	public void uploadExtractedPackage(String contentId, Node node, String basePath, ExtractionType extractionType,
			boolean slugFile) {

		if (StringUtils.isBlank(basePath))
			throw new ClientException(ContentErrorCodes.UPLOAD_DENIED.name(),
					"Error! Base Path cannot be Empty or 'null' for Content Package Extraction over Storage Space.");

		String cloudExtractionPath = "";

		try {

			// Get extracted folder
			File extractedDir = new File(basePath);
			//Get Cloud folder
			cloudExtractionPath = getExtractionPath(contentId, node, extractionType);
			// Upload Directory to Cloud
			directoryUpload(extractedDir, cloudExtractionPath, slugFile);
		}  catch (Exception e) {
			cleanUpCloudFolder(cloudExtractionPath);
			throw new ServerException(ContentErrorCodes.EXTRACTION_ERROR.name(),
					"Error! Something went wrong while extracting the Content Package on Storage Space.", e);
		} finally {
			try {
				TelemetryManager.log("Deleting Locally Extracted File.");
				File dir = new File(basePath);
				if (dir.exists())
					dir.delete();
			} catch (SecurityException e) {
				TelemetryManager.error("Error! While deleting the local extraction directory: " + basePath, e);
			} catch (Exception e) {
				TelemetryManager.error("Error! Something Went wrong while deleting the local extraction directory: " + basePath, e);
			}
		}
	}



	public Future<List<String>> uploadH5pExtractedPackage(String contentId, Node node, String basePath, ExtractionType
	extractionType, boolean slugFile, ExecutionContext context) {

		if (StringUtils.isBlank(basePath))
			throw new ClientException(ContentErrorCodes.UPLOAD_DENIED.name(),
					"Error! Base Path cannot be Empty or 'null' for Content Package Extraction over Storage Space.");

		String cloudExtractionPath = "";

		try {

			// Get extracted folder
			File extractedDir = new File(basePath);
			//Get Cloud folder
			cloudExtractionPath = getExtractionPath(contentId, node, extractionType);
			// Upload Directory to Cloud
			return directoryH5pUpload(extractedDir, cloudExtractionPath, slugFile, context);
		}  catch (Exception e) {
			cleanUpCloudFolder(cloudExtractionPath);
			throw new ServerException(ContentErrorCodes.EXTRACTION_ERROR.name(),
					"Error! Something went wrong while extracting the Content Package on Storage Space.", e);
		} finally {
			try {
				TelemetryManager.log("Deleting Locally Extracted File.");
				/*File dir = new File(basePath);
				if (dir.exists())
					dir.delete();*/
			} catch (SecurityException e) {
				TelemetryManager.error("Error! While deleting the local extraction directory: " + basePath, e);
			} catch (Exception e) {
				TelemetryManager.error("Error! Something Went wrong while deleting the local extraction directory: " + basePath, e);
			}
		}
	}

	/**
	 * Clean up Cloud folder.
	 *
	 * @param cloudFolderPath
	 *            the Cloud folder path
	 */
	private void cleanUpCloudFolder(String cloudFolderPath) {
		try {
			TelemetryManager.log("Cleaning AWS Folder Path: " + cloudFolderPath);
			if (StringUtils.isNoneBlank(cloudFolderPath))
				CloudStore.deleteFile(cloudFolderPath, true);
		} catch (Exception ex) {
			TelemetryManager.error("Error! While Cleanup of Half Extracted Folder from S3: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Directory upload.
	 *
	 * @param directory
	 *            the directory to be uploaded
	 * @param cloudFolderPath
	 *            the Cloud folder path
	 * @return the list
	 */
	private void directoryUpload(File directory, String cloudFolderPath, boolean slugFile) {
		// Validating directory to be uploaded
		if (!directory.exists())
			throw new ClientException(ContentErrorCodes.UPLOAD_DENIED.name(),
					"Error! Extraction File cannot be Empty or 'null' for Content Package Extraction over Storage Space.");
		if (directory.isFile())
			throw new ClientException(ContentErrorCodes.UPLOAD_DENIED.name(),
					"Error! Not a Directory");
		if (directory.listFiles().length < 1)
			throw new ClientException(ContentErrorCodes.UPLOAD_DENIED.name(),
					"Error! Atleast One file is needed for Content Package Extraction.");

		TelemetryManager.log("Uploading Extracted Directory to Cloud");
		TelemetryManager.log("Folder Name For Storage Space Extraction: " + cloudFolderPath);
		CloudStore.uploadDirectory(cloudFolderPath, directory, slugFile);
	}

	/**
	 * Gets the extraction path.
	 *
	 * @param node
	 *            the node
	 * @param extractionType
	 *            the extraction type
	 * @return the extraction path
	 */
	@SuppressWarnings("unused")
	private String getExtractionPath(String contentId, Node node, ExtractionType extractionType) {
		String path = "";
		String contentFolder = S3PropertyReader.getProperty(CONTENT_FOLDER);

		// Getting the Path Suffix
		String mimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
		String pathSuffix = extractionType.name();
		if (StringUtils.equalsIgnoreCase(extractionType.name(), ContentAPIParams.version.name())) {
			String version = String.valueOf((double) node.getMetadata().get(ContentAPIParams.pkgVersion.name()));
			if (StringUtils.equals("application/vnd.ekstep.plugin-archive", mimeType)) {
				String semanticVersion = (String) node.getMetadata().get(ContentAPIParams.semanticVersion.name());
				pathSuffix = StringUtils.isNotBlank(semanticVersion) ? semanticVersion : version;
			} else {
				pathSuffix = version;
			}
		}
		TelemetryManager.log("Path Suffix: " + pathSuffix);
		switch (mimeType) {
		case "application/vnd.ekstep.ecml-archive":
			path += contentFolder + File.separator + ContentAPIParams.ecml.name() + File.separator + contentId + DASH
					+ pathSuffix;
			break;
		case "application/vnd.ekstep.html-archive":
			path += contentFolder + File.separator + ContentAPIParams.html.name() + File.separator + contentId + DASH
					+ pathSuffix;
			break;
		case "application/vnd.ekstep.h5p-archive":
			path += contentFolder + File.separator + ContentAPIParams.h5p.name() + File.separator + contentId + DASH
					+ pathSuffix;
			break;
		case "application/vnd.ekstep.plugin-archive":
			path += CONTENT_PLUGIN_DIRECTORY + File.separator + contentId + DASH + pathSuffix;
			break;

		default:
			break;
		}
		TelemetryManager.log("Storage Space Path: " + path);
		return path;
	}

	/**
	 * Gets the base path.
	 *
	 * @param contentId
	 *            the content id
	 * @return the base path
	 */
	private String getBasePath(String contentId) {
		String path = "";
		if (!StringUtils.isBlank(contentId))
			path = TEMP_FILE_LOCATION + File.separator + System.currentTimeMillis() + ContentAPIParams._temp.name()
					+ File.separator + contentId;
		return path;
	}

	private boolean isExtractedSnapshotExist(Node node, ExtractionType extractionType) {
		String artifactUrl = (String) node.getMetadata().get(ContentAPIParams.artifactUrl.name());
		return isValidSnapshotFile(artifactUrl);
	}

	private boolean isValidSnapshotFile(String artifactUrl) {
		boolean isValid = false;
		if (StringUtils.isNotBlank(artifactUrl)) {
			for (String key : extractablePackageExtensions.keySet())
				if (StringUtils.endsWithIgnoreCase(artifactUrl, key)) {
					isValid = true;
					break;
				}
		}
		return isValid;
	}
	
	public String getS3URL(String contentId, Node node, ExtractionType extractionType) {
		String path = getExtractionPath(contentId, node, extractionType);
		String mimeType = (String) node.getMetadata().get("mimeType");
		boolean isDirectory = false;
		if (extractableMimeTypes.containsKey(mimeType))
			isDirectory = true;
		return CloudStore.getURI(path, Option.apply(isDirectory));
	}


	private Future<List<String>> directoryH5pUpload(File directory, String cloudFolderPath, boolean slugFile, ExecutionContext context) {
		// Validating directory to be uploaded
		if (!directory.exists())
			throw new ClientException(ContentErrorCodes.UPLOAD_DENIED.name(),
					"Error! Extraction File cannot be Empty or 'null' for Content Package Extraction over Storage Space.");
		if (directory.isFile())
			throw new ClientException(ContentErrorCodes.UPLOAD_DENIED.name(),
					"Error! Not a Directory");
		if (directory.listFiles().length < 1)
			throw new ClientException(ContentErrorCodes.UPLOAD_DENIED.name(),
					"Error! Atleast One file is needed for Content Package Extraction.");

		TelemetryManager.log("Uploading Extracted Directory to Cloud");
		TelemetryManager.log("Folder Name For Storage Space Extraction: " + cloudFolderPath);
		long startTime = System.currentTimeMillis();
		Future<List<String>> response = CloudStore.uploadH5pDirectory(cloudFolderPath, directory, slugFile, context);
		return response;


	}
}