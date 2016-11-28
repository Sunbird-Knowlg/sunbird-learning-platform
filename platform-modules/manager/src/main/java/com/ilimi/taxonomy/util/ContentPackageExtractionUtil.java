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
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
	private static final String s3Content = "s3.content.folder";

	/** The Constant TEMP_FILE_LOCATION. */
	private static final String TEMP_FILE_LOCATION = "/data/contentBundle"; // No
																			// END
																			// Path
																			// Separator

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
		if (StringUtils.isBlank(artifactUrl)
				|| !StringUtils.endsWithIgnoreCase(artifactUrl, ContentAPIParams.zip.name()))
			throw new ClientException(ContentErrorCodes.INVALID_ECAR.name(), "Error! Invalid ECAR Url.");

		String extractionBasePath = getBasePath(node.getIdentifier());
		String contentPackageDownloadPath = getBasePath(node.getIdentifier());
		try {
			// Download Content Package
			File contentPackageFile = HttpDownloadUtility.downloadFile(artifactUrl, contentPackageDownloadPath);
			if (null == contentPackageDownloadPath)
				throw new ServerException(ContentErrorCodes.INVALID_ARTIFACT.name(),
						"Error! Unable to download the Content Package (from artifact Url) for Content Package Extraction on Storage Space.");

			// UnZip the Content Package
			UnzipUtility unzipUtility = new UnzipUtility();
			unzipUtility.unzip(contentPackageFile.getAbsolutePath(), extractionBasePath);

			// Extract Content Package
			extractPackage(node, extractionBasePath, extractionType);
		} catch (IOException e) {
			LOGGER.error("Error! While Unzipping the Content Package [Content Package Extraction to Storage Space]", e);
		} finally {
			try {
				LOGGER.info("Deleting Locally Extracted File.");
				File dir = new File(contentPackageDownloadPath);
				if (dir.exists())
					dir.delete();
			} catch (SecurityException e) {
				LOGGER.error("Error! While Deleting the Local Download Directory: " + contentPackageDownloadPath, e);
			} catch (Exception e) {
				LOGGER.error("Error! Something Went Wrong While Deleting the Local Download Directory: "
						+ contentPackageDownloadPath, e);
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
		LOGGER.debug("Node: ", node);
		LOGGER.debug("Uploaded File: ", uploadedFile);
		LOGGER.debug("Extraction Type: ", extractionType);

		// Validating the Parameters
		LOGGER.info("Validating Node Object.");
		if (null == node)
			throw new ClientException(ContentErrorCodes.INVALID_NODE.name(),
					"Error! Content (Node Object) cannot be 'null'");

		LOGGER.info("Validating Uploaded File.");
		if (!uploadedFile.exists()
				|| !StringUtils.endsWithIgnoreCase(uploadedFile.getName(), ContentAPIParams.zip.name()))
			throw new ClientException(ContentErrorCodes.INVALID_FILE.name(), "Error! File doesn't Exist.");

		LOGGER.info("Validating Extraction Type.");
		if (null == extractionType)
			throw new ClientException(ContentErrorCodes.INVALID_EXTRACTION.name(),
					"Error! Invalid Content Extraction Type.");

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
		String AWSFolderPath = "";
		try {
			// Get List of All the Files in the Extracted Folder
			File extractionDir = new File(basePath);
			List<File> lstFilesToUpload = (List<File>) FileUtils.listFiles(extractionDir, TrueFileFilter.INSTANCE,
					TrueFileFilter.INSTANCE);

			// Upload All the File to S3 Recursively and Concurrently
			AWSFolderPath = getExtractionPath(node, extractionType);
			lstUploadedFilesUrl = bulkFileUpload(lstFilesToUpload, AWSFolderPath, basePath);
		} catch (InterruptedException e) {
			cleanUpAWSFolder(AWSFolderPath);
			throw new ServerException(ContentErrorCodes.EXTRACTION_ERROR.name(),
					"Error! The Extraction Process was Interrupted.", e);
		} catch (ExecutionException e) {
			cleanUpAWSFolder(AWSFolderPath);
			throw new ServerException(ContentErrorCodes.EXTRACTION_ERROR.name(),
					"Error! Something went wrong while Executing bulk upload of Files during Extraction.", e);
		} catch (Exception e) {
			cleanUpAWSFolder(AWSFolderPath);
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
					if (file.exists()) {
						String folderName = AWSFolderPath;
						String path = file.getAbsolutePath().replace(basePath, "")
								.replace((File.separator + file.getName()), "");
						if (StringUtils.isNotBlank(path))
							folderName += path;
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
		String path = S3PropertyReader.getProperty(s3Content);

		// Getting the Path Suffix
		String pathSuffix = extractionType.name();
		if (StringUtils.equalsIgnoreCase(extractionType.name(), ContentAPIParams.version.name()))
			pathSuffix = String.valueOf((double) node.getMetadata().get(ContentAPIParams.pkgVersion.name()));
		LOGGER.info("Path Suffix: " + pathSuffix);

		switch (((String) node.getMetadata().get(ContentAPIParams.mimeType.name()))) {
		case "application/vnd.ekstep.ecml-archive":
			path += File.separator + ContentAPIParams.ecml.name() + File.separator + node.getIdentifier() + DASH
					+ pathSuffix;
			break;
		case "application/vnd.ekstep.html-archive":
			path += File.separator + ContentAPIParams.html.name() + File.separator + node.getIdentifier() + DASH
					+ pathSuffix;
			break;
		case "application/vnd.ekstep.plugin-archive":
			path += File.separator + ContentAPIParams.plugins.name() + File.separator + node.getIdentifier() + DASH
					+ pathSuffix;
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
}
