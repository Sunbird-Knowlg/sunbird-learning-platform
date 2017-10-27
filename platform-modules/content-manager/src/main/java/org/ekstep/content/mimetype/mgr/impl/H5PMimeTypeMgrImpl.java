package org.ekstep.content.mimetype.mgr.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.slugs.Slug;
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.common.util.UnzipUtility;
import org.ekstep.content.common.ContentConfigurationConstants;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.common.ContentOperations;
import org.ekstep.content.common.ExtractionType;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.mimetype.mgr.IMimeTypeManager;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.content.util.AsyncContentOperationUtil;
import org.ekstep.content.util.ContentPackageExtractionUtil;
import org.ekstep.learning.common.enums.ContentAPIParams;

import com.ilimi.common.Platform;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.graph.dac.model.Node;

public class H5PMimeTypeMgrImpl extends BaseMimeTypeManager implements IMimeTypeManager {

	@Override
	public Response upload(String contentId, Node node, File uploadFile, boolean isAsync) {
		Response response = new Response();
		if (isValidH5PContentPackage(uploadFile)) {
			response = uploadH5PContent(contentId, node, uploadFile);
		}
		return response;
	}

	@Override
	public Response upload(String contentId, Node node, String fileUrl) {
		File file = null;
		try {
			file = copyURLToFile(fileUrl);
			return upload(contentId, node, file, false);
		} catch (Exception e) {
			throw e;
		} finally {
			if (null != file && file.exists())
				file.delete();
		}
	}

	@Override
	public Response publish(String contentId, Node node, boolean isAsync) {
		PlatformLogger.log("Node: ", node.getIdentifier());

		Response response = new Response();
		PlatformLogger.log("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + contentId);
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);

		PlatformLogger.log("Adding 'isPublishOperation' Flag to 'true'");
		parameterMap.put(ContentAPIParams.isPublishOperation.name(), true);

		PlatformLogger.log("Calling the 'Review' Initializer for Node Id: " + contentId);
		response = pipeline.init(ContentAPIParams.review.name(), parameterMap);
		PlatformLogger.log("Review Operation Finished Successfully for Node ID: ", contentId);

		if (BooleanUtils.isTrue(isAsync)) {
			AsyncContentOperationUtil.makeAsyncOperation(ContentOperations.PUBLISH, contentId, parameterMap);
			PlatformLogger.log("Publish Operation Started Successfully in 'Async Mode' for Node Id: ", contentId);

			response.put(ContentAPIParams.publishStatus.name(),
					"Publish Operation for Content Id '" + contentId + "' Started Successfully!");
		} else {
			PlatformLogger.log("Publish Operation Started Successfully in 'Sync Mode' for Node Id: ", contentId);
			response = pipeline.init(ContentAPIParams.publish.name(), parameterMap);
		}

		return response;
	}

	@Override
	public Response review(String contentId, Node node, boolean isAsync) {
		PlatformLogger.log("Node: ", node.getIdentifier());

		PlatformLogger.log("Preparing the Parameter Map for Initializing the Pipeline For Node ID: ",
				node.getIdentifier());
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);

		PlatformLogger.log("Calling the 'Review' Initializer for Node ID: ", node.getIdentifier());
		return pipeline.init(ContentAPIParams.review.name(), parameterMap);
	}

	private boolean isValidH5PContentPackage(File uploadedFile) {
		boolean isValid = true;
		if (BooleanUtils
				.isFalse(hasGivenFile(uploadedFile, ContentConfigurationConstants.DEFAULT_H5P_JSON_FILE_LOCATION)))
			throw new ClientException(ContentErrorCodeConstants.UPLOAD_ERROR.name(),
					ContentErrorMessageConstants.INVALID_CONTENT_PACKAGE_STRUCTURE_ERROR
							+ " | ['h5p.json' File is Missing.]");
		return isValid;
	}

	private Response uploadH5PContent(String contentId, Node node, File uploadFile) {
		Response response = new Response();
		String extractionBasePath = null;
		File h5pLibraryPackageFile = null;
		try {
			extractionBasePath = getBasePath(contentId);
			// Download the H5P Libraries
			String h5pLibraryDownloadPath = getBasePath(contentId);
			h5pLibraryPackageFile = HttpDownloadUtility.downloadFile(getH5PLibraryPath(), h5pLibraryDownloadPath);
			// Un-Zip the H5P Library Files
			UnzipUtility unzipUtility = new UnzipUtility();
			unzipUtility.unzip(h5pLibraryPackageFile.getAbsolutePath(), extractionBasePath);
			// UnZip the Content Package
			unzipUtility.unzip(uploadFile.getAbsolutePath(), extractionBasePath + "/content");
			// Delete 'H5P' Library Package Zip File after Extraction
			if (h5pLibraryPackageFile.exists())
				h5pLibraryPackageFile.delete();
			List<Path> paths = Files.walk(Paths.get(extractionBasePath)).filter(Files::isRegularFile)
					.collect(Collectors.toList());
			List<File> files = new ArrayList<File>();
			for (Path path : paths)
				files.add(path.toFile());
			// Create 'ZIP' Package
			String zipFileName = extractionBasePath + File.separator + System.currentTimeMillis() + "_"
					+ Slug.makeSlug(contentId) + ContentConfigurationConstants.FILENAME_EXTENSION_SEPERATOR
					+ ContentConfigurationConstants.DEFAULT_ZIP_EXTENSION;
			createZipPackage(extractionBasePath, zipFileName);
			String[] urlArray = uploadArtifactToAWS(new File(zipFileName), contentId);
			node.getMetadata().put("s3Key", urlArray[IDX_S3_KEY]);
			node.getMetadata().put(ContentAPIParams.artifactUrl.name(), urlArray[IDX_S3_URL]);
			File zipFile = new File(zipFileName);
			if (zipFile.exists())
				zipFile.delete();
			// Extract Content Package
			ContentPackageExtractionUtil contentPackageExtractionUtil = new ContentPackageExtractionUtil();
			contentPackageExtractionUtil.extractPackage(contentId, node, extractionBasePath, ExtractionType.snapshot,
					false);
			response = updateContentNode(contentId, node, urlArray[IDX_S3_URL]);
		} catch (IOException e) {
			PlatformLogger.log("Error! While Unzipping the Content Package File.", e.getMessage(), e);
		} catch (ClientException e) {
			throw e;
		} catch (ServerException e) {
			throw e;
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodeConstants.UPLOAD_ERROR.name(),
					ContentErrorMessageConstants.FILE_UPLOAD_ERROR
							+ " | [Something Went Wrong While Uploading the 'H5P' Content.]");
		} finally {
			// Cleanup
			try {
				FileUtils.deleteDirectory(new File(extractionBasePath));
				h5pLibraryPackageFile.delete();
			} catch (Exception e) {
				PlatformLogger.log("Unable to Delete H5P Library Directory.", null, e);
			}
		}
		return response;
	}

	private String getH5PLibraryPath() {
		String path = Platform.config.getString(ContentConfigurationConstants.DEFAULT_H5P_LIBRARY_PATH_PROPERTY_KEY);
		if (StringUtils.isBlank(path))
			throw new ServerException(ContentErrorCodeConstants.INVALID_LIBRARY.name(),
					ContentErrorMessageConstants.INVALID_H5P_LIBRARY + " | [Invalid H5P Library Package Path.]");
		PlatformLogger.log("Fetched H5P Library Path: " + path, null, "INFO");
		return path;
	}

}
