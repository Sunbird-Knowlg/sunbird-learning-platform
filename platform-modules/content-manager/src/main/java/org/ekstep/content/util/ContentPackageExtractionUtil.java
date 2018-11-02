package org.ekstep.content.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.Slug;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.common.util.UnzipUtility;
import org.ekstep.content.common.ContentConfigurationConstants;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.common.ExtractionType;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.learning.util.CloudStore;
import org.ekstep.telemetry.logger.TelemetryManager;
import scala.Option;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
							CloudStore.copyObjectsByPrefix(sourcePrefix, destinationPrefix);
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
					CloudStore.copyObjectsByPrefix(sourcePrefix, destinationPrefix);
					TelemetryManager.log("Copying Objects...DONE | Under: " + destinationPrefix);
				} catch(Exception e) {
					TelemetryManager.error("Error while copying object by prefix", e);
				}
			}
		}
	}

	/**
	 * Extract content package.
	 *
	 * @param node
	 *            the node
	 * @param uploadedFile
	 *            the uploaded file
	 * @param extractionType
	 *            the extraction type
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
	 * @param node
	 *            the node
	 * @param basePath
	 *            the base path
	 * @param extractionType
	 *            the extraction type
	 */
	public void uploadExtractedPackage(String contentId, Node node, String basePath, ExtractionType extractionType,
			boolean slugFile) {
		List<String> lstUploadedFilesUrl = new ArrayList<>();
		String awsFolderPath = "";
		try {
			// Get Extracted Folder
			File extractionDir = new File(basePath);

			// Upload Directory to S3
			awsFolderPath = getExtractionPath(contentId, node, extractionType);
			directoryUpload(extractionDir, awsFolderPath, basePath, slugFile);
		}  catch (Exception e) {
			cleanUpAWSFolder(awsFolderPath);
			throw new ServerException(ContentErrorCodes.EXTRACTION_ERROR.name(),
					"Error! Something went wrong while extracting the Content Package on Storage Space.", e);
		} finally {
			try {
				// TelemetryManager.log("Total Uploaded Files: " + lstUploadedFilesUrl.size());
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

	/**
	 * Clean up AWS folder.
	 *
	 * @param AWSFolderPath
	 *            the AWS folder path
	 */
	private void cleanUpAWSFolder(String AWSFolderPath) {
		try {
			TelemetryManager.log("Cleaning AWS Folder Path: " + AWSFolderPath);
			if (StringUtils.isNoneBlank(AWSFolderPath))
				CloudStore.deleteFile(AWSFolderPath, true);
		} catch (Exception ex) {
			TelemetryManager.error("Error! While Cleanup of Half Extracted Folder from S3: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Directory upload.
	 *
	 * @param dir
	 *            the files
	 * @param AWSFolderPath
	 *            the AWS folder path
	 * @param basePath
	 *            the base path
	 * @return the list
	 */
	private void directoryUpload(File dir, String AWSFolderPath, String basePath, boolean slugFile) {
		// Validating Parameters
		if(null == dir)
			throw new ClientException(ContentErrorCodes.UPLOAD_DENIED.name(),
					"Error! Atleast One file is needed for Content Package Extraction.");
		if(dir.isFile())
			throw new ClientException(ContentErrorCodes.UPLOAD_DENIED.name(),
					"Error! Not a Directory");
		if (dir.listFiles().length < 1)
			throw new ClientException(ContentErrorCodes.UPLOAD_DENIED.name(),
					"Error! Atleast One file is needed for Content Package Extraction.");
		if (StringUtils.isBlank(basePath))
			throw new ClientException(ContentErrorCodes.UPLOAD_DENIED.name(),
					"Error! Base Path cannot be Empty or 'null' for Content Package Extraction over Storage Space.");

//		List<String> lstUploadedFileUrls = new ArrayList<>();
		TelemetryManager.log("Adding Extracted Directory to Upload.");
		String folderName = AWSFolderPath;
		String path = getFolderPath(dir, basePath);
		if (StringUtils.isNotBlank(path))
			folderName += File.separator + path;
		TelemetryManager.log("Folder Name For Storage Space Extraction: " + folderName);
		CloudStore.uploadDirectory(folderName, dir, slugFile);
//		String[] uploadedFileUrl = CloudStore.uploadDirectory(folderName, dir, slugFile);
//		if (null != uploadedFileUrl && uploadedFileUrl.length > 1)
//			lstUploadedFileUrls = Arrays.asList(uploadedFileUrl[AWS_UPLOAD_RESULT_URL_INDEX].
//					substring(5, uploadedFileUrl[AWS_UPLOAD_RESULT_URL_INDEX].length() - 1).split(", "));
//
//		return lstUploadedFileUrls;
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

	/**
	 * Gets the folder path.
	 *
	 * @param file
	 *            the file
	 * @param basePath
	 *            the base path
	 * @return the folder path
	 */
	private String getFolderPath(File file, String basePath) {
		String path = "";
		String filePath = file.getAbsolutePath();
		TelemetryManager.log("Cleaned File Path: " + filePath + "[Get Folder Path]");
		String base = new File(basePath).getPath();
		path = filePath.replace(base, "");
		path = FilenameUtils.getPathNoEndSeparator(path);
		TelemetryManager.log("Cleaned Base Path: " + base + "[Get Folder Path]");
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
}