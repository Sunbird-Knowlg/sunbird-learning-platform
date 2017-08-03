package org.ekstep.content.util;

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
import org.ekstep.common.slugs.Slug;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.common.util.UnzipUtility;
import org.ekstep.content.common.ExtractionType;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;

import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.graph.dac.model.Node;

/**
 * The Class ContentPackageExtractionUtil.
 * 
 * @author Mohammad Azharuddin
 */
public class ContentPackageExtractionUtil {

	/** The logger. */
	

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

	public void copyExtractedContentPackage(String contentId, Node node, ExtractionType extractionType) {
		PlatformLogger.log("Node: ", node);
		PlatformLogger.log("Extraction Type: ", extractionType);

		// Validating the Parameters
		PlatformLogger.log("Validating Node Object.");
		if (null == node)
			throw new ClientException(ContentErrorCodes.INVALID_NODE.name(),
					"Error! Content (Node Object) cannot be 'null'");

		PlatformLogger.log("Validating Extraction Type.");
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
			PlatformLogger.log("Currently Working Environment: " + s3Environment);
			
			// Fetching Bucket Name
			String s3Bucket = S3PropertyReader.getProperty(S3_BUCKET_PREFIX + s3Environment);
			PlatformLogger.log("Current Storage Space Bucket Name: " + s3Bucket);
			
			// Fetching Source Prefix For Copy Objects in S3 
			String sourcePrefix = getExtractionPath(contentId, node, ExtractionType.snapshot);
			PlatformLogger.log("Source Prefix: " + sourcePrefix);

			// Fetching Destination Prefix For Copy Objects in S3
			String destinationPrefix = getExtractionPath(contentId, node, extractionType);
			PlatformLogger.log("Source Prefix: " + destinationPrefix);
			
			// Copying Objects
			PlatformLogger.log("Copying Objects...STARTED");
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
				PlatformLogger.log("Error sending Content2Vec request", e.getMessage(), e);
			} finally {
				if (null != pool)
					pool.shutdown();
			}
			PlatformLogger.log("Copying Objects...DONE | Under: " + destinationPrefix);
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
	public void extractContentPackage(String contentId, Node node, ExtractionType extractionType, boolean slugFile) {
		PlatformLogger.log("Node: ", node);
		PlatformLogger.log("Extraction Type: ", extractionType);

		// Validating the Parameters
		PlatformLogger.log("Validating Node Object.");
		if (null == node)
			throw new ClientException(ContentErrorCodes.INVALID_NODE.name(),
					"Error! Content (Node Object) cannot be 'null'");

		PlatformLogger.log("Validating Extraction Type.");
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
			PlatformLogger.log("Given Content Belongs to Extractable Category of MimeTypes.");
			String extractionBasePath = getBasePath(contentId);
			String contentPackageDownloadPath = getBasePath(contentId);
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
				extractPackage(contentId, node, extractionBasePath, extractionType, slugFile);
			} catch (IOException e) {
				PlatformLogger.log("Error! While Unzipping the Content Package [Content Package Extraction to Storage Space]",
						e.getMessage(), e);
			} finally {
				try {
					PlatformLogger.log("Deleting Locally Extracted File.");
					File dir = new File(contentPackageDownloadPath);
					if (dir.exists())
						dir.delete();
				} catch (SecurityException e) {
					PlatformLogger.log("Error! While Deleting the Local Download Directory: " + contentPackageDownloadPath,
							e.getMessage(), e);
				} catch (Exception e) {
					PlatformLogger.log("Error! Something Went Wrong While Deleting the Local Download Directory: "
							+ contentPackageDownloadPath, e.getMessage(), e);
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
	public void extractContentPackage(String contentId, Node node, File uploadedFile, ExtractionType extractionType, boolean slugFile) {
		uploadedFile = Slug.createSlugFile(uploadedFile);
		PlatformLogger.log("Node: " + node);
		PlatformLogger.log("Uploaded File: " + uploadedFile.getName() + " - " + uploadedFile.exists() + " - " + uploadedFile.getAbsolutePath());
		PlatformLogger.log("Extraction Type: " + extractionType);

		// Validating the Parameters
		PlatformLogger.log("Validating Node Object.");
		if (null == node)
			throw new ClientException(ContentErrorCodes.INVALID_NODE.name(),
					"Error! Content (Node Object) cannot be 'null'");

		PlatformLogger.log("Validating Uploaded File.");
		String mimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
		PlatformLogger.log("Mime Type: " + mimeType);
		if (!uploadedFile.exists()
				|| (!StringUtils.endsWithIgnoreCase(uploadedFile.getName(), ContentAPIParams.zip.name())
						&& extractableMimeTypes.containsKey(mimeType)))
			throw new ClientException(ContentErrorCodes.INVALID_FILE.name(), "Error! File doesn't Exist.");

		PlatformLogger.log("Validating Extraction Type.");
		if (null == extractionType)
			throw new ClientException(ContentErrorCodes.INVALID_EXTRACTION.name(),
					"Error! Invalid Content Extraction Type.");

		if (extractableMimeTypes.containsKey(mimeType)) {
			PlatformLogger.log("Given Content Belongs to Extractable MimeType Category.");
			String extractionBasePath = getBasePath(contentId);
			try {
				// UnZip the Content Package
				UnzipUtility unzipUtility = new UnzipUtility();
				unzipUtility.unzip(uploadedFile.getAbsolutePath(), extractionBasePath);

				// Extract Content Package
				extractPackage(contentId, node, extractionBasePath, extractionType, slugFile);
			} catch (IOException e) {
				PlatformLogger.log("Error! While Unzipping the Content Package File.", e.getMessage(), e);
			} catch (Exception e) {
				PlatformLogger.log("Error! Something Went Wrong While Extracting the Content Package File.", e.getMessage(), e);
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
	private void extractPackage(String contentId, Node node, String basePath, ExtractionType extractionType, boolean slugFile) {
		List<String> lstUploadedFilesUrl = new ArrayList<String>();
		String awsFolderPath = "";
		try {
			// Get List of All the Files in the Extracted Folder
			File extractionDir = new File(basePath);
			List<File> lstFilesToUpload = (List<File>) FileUtils.listFiles(extractionDir, TrueFileFilter.INSTANCE,
					TrueFileFilter.INSTANCE);

			// Upload All the File to S3 Recursively and Concurrently
			awsFolderPath = getExtractionPath(contentId, node, extractionType);
			lstUploadedFilesUrl = bulkFileUpload(lstFilesToUpload, awsFolderPath, basePath, slugFile);
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
				PlatformLogger.log("Total Uploaded Files: " + lstUploadedFilesUrl.size());
				PlatformLogger.log("Deleting Locally Extracted File.");
				File dir = new File(basePath);
				if (dir.exists())
					dir.delete();
			} catch (SecurityException e) {
				PlatformLogger.log("Error! While Deleting the Local Extraction Directory: " + basePath, e.getMessage(), e);
			} catch (Exception e) {
				PlatformLogger.log("Error! Something Went Wrong While Deleting the Local Extraction Directory: " + basePath,
						e.getMessage(), e);
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
			PlatformLogger.log("Cleaning AWS Folder Path: " + AWSFolderPath);
			if (StringUtils.isNoneBlank(AWSFolderPath))
				AWSUploader.deleteFile(AWSFolderPath);
		} catch (Exception ex) {
			PlatformLogger.log("Error! While Cleanup of Half Extracted Folder from S3.", ex.getMessage(), ex);
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
	private List<String> bulkFileUpload(List<File> files, String AWSFolderPath, String basePath, boolean slugFile)
			throws InterruptedException, ExecutionException {
		PlatformLogger.log("Files: ", files);
		PlatformLogger.log("AWS Folder Path: ", AWSFolderPath);
		PlatformLogger.log("Local Base Path of Extraction: ", basePath);

		// Validating Parameters
		if (null == files || files.size() < 1)
			throw new ClientException(ContentErrorCodes.UPLOAD_DENIED.name(),
					"Error! Atleast One file is needed for Content Package Extraction.");

		if (StringUtils.isBlank(basePath))
			throw new ClientException(ContentErrorCodes.UPLOAD_DENIED.name(),
					"Error! Base Path cannot be Empty or 'null' for Content Package Extraction over Storage Space.");

		List<String> lstUploadedFileUrls = new ArrayList<String>();
		PlatformLogger.log("Starting the Fan-out for Upload.");
		ExecutorService pool = Executors.newFixedThreadPool(10);
		List<Callable<Map<String, String>>> tasks = new ArrayList<Callable<Map<String, String>>>(files.size());

		PlatformLogger.log("Adding All Files to Upload.");
		for (final File file : files) {
			tasks.add(new Callable<Map<String, String>>() {
				public Map<String, String> call() throws Exception {
					Map<String, String> uploadMap = new HashMap<String, String>();
					if (file.exists() && !file.isDirectory()) {
						String folderName = AWSFolderPath;
						String path = getFolderPath(file, basePath);
						if (StringUtils.isNotBlank(path))
							folderName += File.separator + path;
						PlatformLogger.log("Folder Name For Storage Space Extraction: " , folderName);
						String[] uploadedFileUrl = AWSUploader.uploadFile(folderName, file, slugFile);
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
	@SuppressWarnings("unused")
	private String getExtractionPath(String contentId, Node node, ExtractionType extractionType) {
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
		PlatformLogger.log("Path Suffix: " + pathSuffix);
		switch (mimeType) {
		case "application/vnd.ekstep.ecml-archive":
			path += contentFolder + File.separator + ContentAPIParams.ecml.name() + File.separator + contentId + DASH
					+ pathSuffix;
			break;
		case "application/vnd.ekstep.html-archive":
			path += contentFolder + File.separator + ContentAPIParams.html.name() + File.separator + contentId + DASH
					+ pathSuffix;
			break;
		case "application/vnd.ekstep.plugin-archive":
			path += S3_CONTENT_PLUGIN_DIRECTORY + File.separator
					+ contentId + DASH + pathSuffix;
			break;

		default:
			break;
		}
		PlatformLogger.log("Storage Space Path: " + path);
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
		PlatformLogger.log("Cleaned File Path: " + filePath + "[Get Folder Path]");
		String base = new File(basePath).getPath();
		path = filePath.replace(base, "");
		path = FilenameUtils.getPathNoEndSeparator(path);
		PlatformLogger.log("Cleaned Base Path: " + base + "[Get Folder Path]");
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
