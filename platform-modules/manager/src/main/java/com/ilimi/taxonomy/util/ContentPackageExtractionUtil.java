package com.ilimi.taxonomy.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.slugs.Slug;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.common.util.UnzipUtility;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;

import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.enums.ExtractionType;

/**
 * The Class ContentPackageExtractionUtil.
 * 
 * @author Mohammad Azharuddin
 */
public class ContentPackageExtractionUtil {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(ContentPackageExtractionUtil.class.getName());

	/** The Constant AWS_UPLOAD_RESULT_URL_INDEX. */
	private static final int AWS_UPLOAD_RESULT_URL_INDEX = 1;

	/** The Constant DASH. */
	private static final String DASH = "-";

	/** The Constant s3Content. */
	private static final String S3_CONTENT = "s3.content.folder";
	
	private static final String S3_BUCKET_PREFIX = "s3.bucket.";

	/** The Constant S3_ENVIRONMENT. */
	private static final String S3_ENVIRONMENT = "s3.env";

	/** The Constant S3_CONTENT_PLUGIN_DIRECTORY. */
	private static final String S3_CONTENT_PLUGIN_DIRECTORY = "content-plugins";

	/** The Constant TEMP_FILE_LOCATION. */
	private static final String TEMP_FILE_LOCATION = "/data/contentBundle/";

	/** The extractable mime types. */
	private static Map<String, String> extractableMimeTypes = new HashMap<String, String>();

	static {
		extractableMimeTypes.put("application/vnd.ekstep.ecml-archive", "ECML Type Content");
		extractableMimeTypes.put("application/vnd.ekstep.html-archive", "HTML Type Content");
		extractableMimeTypes.put("application/vnd.ekstep.plugin-archive", "Plugin Type Content");
	}

	public void copyExtractedContentPackage(Node node, ExtractionType extractionType) {
		LOGGER.debug("Node: ", node);
		LOGGER.debug("Extraction Type: ", extractionType);

		// Validating the Parameters
		LOGGER.info("Validating Node Object.");
		if (null == node)
			throw new ClientException(ContentErrorCodes.INVALID_NODE.name(),
					"Error! Content (Node Object) cannot be 'null'");

		LOGGER.info("Validating Extraction Type.");
		if (null == extractionType)
			throw new ClientException(ContentErrorCodes.INVALID_EXTRACTION.name(),
					"Error! Invalid Content Extraction Type.");

		String mimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
		if (extractableMimeTypes.containsKey(mimeType)) {
			
			// Checking if the Snapshost Extraction of Content is in place or not
			if (!isExtractedSnapshotExist(node, extractionType))
				throw new ClientException(ContentErrorCodes.INVALID_SNAPSHOT.name(),
						"Error! Snapshot Type Extraction doesn't Exists.");
			
			// Fetching Environment Name
			String s3Environment = S3PropertyReader.getProperty(S3_ENVIRONMENT);
			LOGGER.info("Currently Working Environment: " + s3Environment);
			
			// Fetching Bucket Name
			String s3Bucket = S3PropertyReader.getProperty(S3_BUCKET_PREFIX + s3Environment);
			LOGGER.info("Current Storage Space Bucket Name: " + s3Bucket);
			
			// Fetching Source Prefix For Copy Objects in S3 
			String sourcePrefix = getExtractionPath(node, ExtractionType.snapshot);
			LOGGER.info("Source Prefix: " + sourcePrefix);

			// Fetching Destination Prefix For Copy Objects in S3
			String destinationPrefix = getExtractionPath(node, extractionType);
			LOGGER.info("Source Prefix: " + destinationPrefix);
			
			// Copying Objects
			LOGGER.info("Copying Objects...STARTED");
			ExecutorService pool = null;
			try {
				pool = Executors.newFixedThreadPool(1);
				pool.execute(new Runnable() {
					@Override
					public void run() {
						AWSUploader.copyObjectsByPrefix(s3Bucket, s3Bucket, sourcePrefix, destinationPrefix);
					}
				});
			} catch (Exception e) {
				LOGGER.error("Error sending Content2Vec request", e);
			} finally {
				if (null != pool)
					pool.shutdown();
			}
			LOGGER.info("Copying Objects...DONE | Under: " + destinationPrefix);
		}
	}

	/**
	 * Extract ECAR package.
	 *
	 * @param node
	 *            the node
	 * @param extractionType
	 *            the extraction type
	 */
	public void extractContentPackage(Node node, ExtractionType extractionType) {
		LOGGER.debug("Node: ", node);
		LOGGER.debug("Extraction Type: ", extractionType);

		// Validating the Parameters
		LOGGER.info("Validating Node Object.");
		if (null == node)
			throw new ClientException(ContentErrorCodes.INVALID_NODE.name(),
					"Error! Content (Node Object) cannot be 'null'");

		LOGGER.info("Validating Extraction Type.");
		if (null == extractionType)
			throw new ClientException(ContentErrorCodes.INVALID_EXTRACTION.name(),
					"Error! Invalid Content Extraction Type.");

		// Reading Content Package (artifact) URL
		String artifactUrl = (String) node.getMetadata().get(ContentAPIParams.artifactUrl.name());
		String mimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
		if (StringUtils.isBlank(artifactUrl)
				|| (!StringUtils.endsWithIgnoreCase(artifactUrl, ContentAPIParams.zip.name())
						&& extractableMimeTypes.containsKey(mimeType)))
			throw new ClientException(ContentErrorCodes.INVALID_ECAR.name(), "Error! Invalid ECAR Url.");

		if (extractableMimeTypes.containsKey(mimeType)) {
			LOGGER.info("Given Content Belongs to Extractable Category of MimeTypes.");
			String extractionBasePath = getBasePath(node.getIdentifier());
			String contentPackageDownloadPath = getBasePath(node.getIdentifier());
			try {
				// Download Content Package
				File contentPackageFile = HttpDownloadUtility.downloadFile(artifactUrl, contentPackageDownloadPath);
				if (null == contentPackageFile)
					throw new ServerException(ContentErrorCodes.INVALID_ARTIFACT.name(),
							"Error! Unable to download the Content Package (from artifact Url) for Content Package Extraction on Storage Space.");

				// UnZip the Content Package
				UnzipUtility unzipUtility = new UnzipUtility();
				unzipUtility.unzip(contentPackageFile.getAbsolutePath(), extractionBasePath);

				// Extract Content Package
				extractPackage(node, extractionBasePath, extractionType);
			} catch (IOException e) {
				LOGGER.error("Error! While Unzipping the Content Package [Content Package Extraction to Storage Space]",
						e);
			} finally {
				try {
					LOGGER.info("Deleting Locally Extracted File.");
					File dir = new File(contentPackageDownloadPath);
					if (dir.exists())
						dir.delete();
				} catch (SecurityException e) {
					LOGGER.error("Error! While Deleting the Local Download Directory: " + contentPackageDownloadPath,
							e);
				} catch (Exception e) {
					LOGGER.error("Error! Something Went Wrong While Deleting the Local Download Directory: "
							+ contentPackageDownloadPath, e);
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
	public void extractContentPackage(Node node, File uploadedFile, ExtractionType extractionType) {
		uploadedFile = Slug.createSlugFile(uploadedFile);
		LOGGER.info("Node: " + node);
		LOGGER.info("Uploaded File: " + uploadedFile.getName() + " - " + uploadedFile.exists() + " - " + uploadedFile.getAbsolutePath());
		LOGGER.info("Extraction Type: " + extractionType);

		// Validating the Parameters
		LOGGER.info("Validating Node Object.");
		if (null == node)
			throw new ClientException(ContentErrorCodes.INVALID_NODE.name(),
					"Error! Content (Node Object) cannot be 'null'");

		LOGGER.info("Validating Uploaded File.");
		String mimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
		LOGGER.info("Mime Type: " + mimeType);
		if (!uploadedFile.exists()
				|| (!StringUtils.endsWithIgnoreCase(uploadedFile.getName(), ContentAPIParams.zip.name())
						&& extractableMimeTypes.containsKey(mimeType)))
			throw new ClientException(ContentErrorCodes.INVALID_FILE.name(), "Error! File doesn't Exist.");

		LOGGER.info("Validating Extraction Type.");
		if (null == extractionType)
			throw new ClientException(ContentErrorCodes.INVALID_EXTRACTION.name(),
					"Error! Invalid Content Extraction Type.");

		if (extractableMimeTypes.containsKey(mimeType)) {
			LOGGER.info("Given Content Belongs to Extractable MimeType Category.");
			String extractionBasePath = getBasePath(node.getIdentifier());
			try {
				// UnZip the Content Package
				UnzipUtility unzipUtility = new UnzipUtility();
				unzipUtility.unzip(uploadedFile.getAbsolutePath(), extractionBasePath);

				// Extract Content Package
				extractPackage(node, extractionBasePath, extractionType);
			} catch (IOException e) {
				LOGGER.error("Error! While Unzipping the Content Package File.", e);
			} catch (Exception e) {
				LOGGER.error("Error! Something Went Wrong While Extracting the Content Package File.", e);
			}
		}
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
	private void extractPackage(Node node, String basePath, ExtractionType extractionType) {
		List<String> lstUploadedFilesUrl = new ArrayList<String>();
		String awsFolderPath = "";
		try {
			// Get List of All the Files in the Extracted Folder
			File extractionDir = new File(basePath);
			List<File> lstFilesToUpload = (List<File>) FileUtils.listFiles(extractionDir, TrueFileFilter.INSTANCE,
					TrueFileFilter.INSTANCE);

			// Upload All the File to S3 Recursively and Concurrently
			awsFolderPath = getExtractionPath(node, extractionType);
			lstUploadedFilesUrl = bulkFileUpload(lstFilesToUpload, awsFolderPath, basePath);
		} catch (InterruptedException e) {
			cleanUpAWSFolder(awsFolderPath);
			throw new ServerException(ContentErrorCodes.EXTRACTION_ERROR.name(),
					"Error! The Extraction Process was Interrupted.", e);
		} catch (ExecutionException e) {
			cleanUpAWSFolder(awsFolderPath);
			throw new ServerException(ContentErrorCodes.EXTRACTION_ERROR.name(),
					"Error! Something went wrong while Executing bulk upload of Files during Extraction.", e);
		} catch (Exception e) {
			cleanUpAWSFolder(awsFolderPath);
			throw new ServerException(ContentErrorCodes.EXTRACTION_ERROR.name(),
					"Error! Something went wrong while extracting the Content Package on Storage Space.", e);
		} finally {
			try {
				LOGGER.info("Total Uploaded Files: " + lstUploadedFilesUrl.size());
				LOGGER.info("Deleting Locally Extracted File.");
				File dir = new File(basePath);
				if (dir.exists())
					dir.delete();
			} catch (SecurityException e) {
				LOGGER.error("Error! While Deleting the Local Extraction Directory: " + basePath, e);
			} catch (Exception e) {
				LOGGER.error("Error! Something Went Wrong While Deleting the Local Extraction Directory: " + basePath,
						e);
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
			LOGGER.info("Cleaning AWS Folder Path: " + AWSFolderPath);
			if (StringUtils.isNoneBlank(AWSFolderPath))
				AWSUploader.deleteFile(AWSFolderPath);
		} catch (Exception ex) {
			LOGGER.error("Error! While Cleanup of Half Extracted Folder from S3.", ex);
		}
	}

	/**
	 * Bulk file upload.
	 *
	 * @param files
	 *            the files
	 * @param AWSFolderPath
	 *            the AWS folder path
	 * @param basePath
	 *            the base path
	 * @return the list
	 * @throws InterruptedException
	 *             the interrupted exception
	 * @throws ExecutionException
	 *             the execution exception
	 */
	private List<String> bulkFileUpload(List<File> files, String AWSFolderPath, String basePath)
			throws InterruptedException, ExecutionException {
		LOGGER.debug("Files: ", files);
		LOGGER.debug("AWS Folder Path: ", AWSFolderPath);
		LOGGER.debug("Local Base Path of Extraction: ", basePath);

		// Validating Parameters
		if (null == files || files.size() < 1)
			throw new ClientException(ContentErrorCodes.UPLOAD_DENIED.name(),
					"Error! Atleast One file is needed for Content Package Extraction.");

		if (StringUtils.isBlank(basePath))
			throw new ClientException(ContentErrorCodes.UPLOAD_DENIED.name(),
					"Error! Base Path cannot be Empty or 'null' for Content Package Extraction over Storage Space.");

		List<String> lstUploadedFileUrls = new ArrayList<String>();
		LOGGER.info("Starting the Fan-out for Upload.");
		ExecutorService pool = Executors.newFixedThreadPool(10);
		List<Callable<Map<String, String>>> tasks = new ArrayList<Callable<Map<String, String>>>(files.size());

		LOGGER.info("Adding All Files to Upload.");
		for (final File file : files) {
			tasks.add(new Callable<Map<String, String>>() {
				public Map<String, String> call() throws Exception {
					Map<String, String> uploadMap = new HashMap<String, String>();
					if (file.exists() && !file.isDirectory()) {
						String folderName = AWSFolderPath;
						String path = getFolderPath(file, basePath);
						if (StringUtils.isNotBlank(path))
							folderName += File.separator + path;
						LOGGER.info("Folder Name For Storage Space Extraction: " + folderName);
						String[] uploadedFileUrl = AWSUploader.uploadFile(folderName, file);
						if (null != uploadedFileUrl && uploadedFileUrl.length > 1)
							uploadMap.put(file.getAbsolutePath(), uploadedFileUrl[AWS_UPLOAD_RESULT_URL_INDEX]);
					}

					return uploadMap;
				}
			});
		}
		List<Future<Map<String, String>>> results = pool.invokeAll(tasks);
		for (Future<Map<String, String>> uMap : results) {
			Map<String, String> m = uMap.get();
			if (null != m)
				lstUploadedFileUrls.addAll(m.values());
		}
		pool.shutdown();

		return lstUploadedFileUrls;
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
	private String getExtractionPath(Node node, ExtractionType extractionType) {
		String path = "";
		String contentFolder = S3PropertyReader.getProperty(S3_CONTENT);
		String s3Environment = S3PropertyReader.getProperty(S3_ENVIRONMENT);

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
		LOGGER.info("Path Suffix: " + pathSuffix);
		switch (mimeType) {
		case "application/vnd.ekstep.ecml-archive":
			path += contentFolder + File.separator + ContentAPIParams.ecml.name() + File.separator + node.getIdentifier() + DASH
					+ pathSuffix;
			break;
		case "application/vnd.ekstep.html-archive":
			path += contentFolder + File.separator + ContentAPIParams.html.name() + File.separator + node.getIdentifier() + DASH
					+ pathSuffix;
			break;
		case "application/vnd.ekstep.plugin-archive":
			path += S3_CONTENT_PLUGIN_DIRECTORY + File.separator
					+ node.getIdentifier() + DASH + pathSuffix;
			break;

		default:
			break;
		}
		LOGGER.info("Storage Space Path: " + path);
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
		LOGGER.info("Cleaned File Path: " + filePath + "[Get Folder Path]");
		String base = new File(basePath).getPath();
		path = filePath.replace(base, "");
		path = FilenameUtils.getPathNoEndSeparator(path);
		LOGGER.info("Cleaned Base Path: " + base + "[Get Folder Path]");
		return path;
	}

	private boolean isExtractedSnapshotExist(Node node, ExtractionType extractionType) {
		boolean isSnapshotExits = false;
		String artifactUrl = (String) node.getMetadata().get(ContentAPIParams.artifactUrl.name());
		if (StringUtils.isNotBlank(artifactUrl)
				&& StringUtils.endsWithIgnoreCase(artifactUrl, ContentAPIParams.zip.name()))
			isSnapshotExits = true;
		return isSnapshotExits;
	}
}
