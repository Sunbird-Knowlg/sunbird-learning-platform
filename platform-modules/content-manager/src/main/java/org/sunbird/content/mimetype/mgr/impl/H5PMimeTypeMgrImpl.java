package org.sunbird.content.mimetype.mgr.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import akka.dispatch.ExecutionContexts;
import akka.dispatch.OnComplete;
import akka.dispatch.OnSuccess;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.Slug;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.common.util.HttpDownloadUtility;
import org.sunbird.common.util.UnzipUtility;
import org.sunbird.content.common.ContentConfigurationConstants;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.common.ContentOperations;
import org.sunbird.content.common.ExtractionType;
import org.sunbird.content.enums.ContentErrorCodeConstants;
import org.sunbird.content.mimetype.mgr.IMimeTypeManager;
import org.sunbird.content.pipeline.initializer.InitializePipeline;
import org.sunbird.content.util.AsyncContentOperationUtil;
import org.sunbird.content.util.ContentPackageExtractionUtil;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.learning.common.enums.ContentAPIParams;
import org.sunbird.telemetry.logger.TelemetryManager;
import scala.concurrent.Await;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;


public class H5PMimeTypeMgrImpl extends BaseMimeTypeManager implements IMimeTypeManager {

	@Override
	public Response upload(String contentId, Node node, File uploadFile, boolean isAsync) {
		Response response = new Response();
		if (isValidH5PContentPackage(uploadFile)) {
			response = uploadH5PContent(contentId, node, uploadFile);
		}
		return response;
	}
	
	public Response upload(String contentId, Node node,boolean isZip, File uploadFile) {
		Response response = null;
		if(isZip) {
			String extractionBasePath = getBasePath(contentId);
			try {
				//response = uploadH5PContent(contentId, node, uploadFile);
				
				UnzipUtility unzipUtility = new UnzipUtility();
				unzipUtility.unzip(uploadFile.getAbsolutePath(), extractionBasePath);
				String[] urlArray = uploadArtifactToAWS(uploadFile, contentId);
				node.getMetadata().put("s3Key", urlArray[IDX_S3_KEY]);
				node.getMetadata().put(ContentAPIParams.artifactUrl.name(), urlArray[IDX_S3_URL]);
				ContentPackageExtractionUtil contentPackageExtractionUtil = new ContentPackageExtractionUtil();
				ExecutionContext context = ExecutionContexts.fromExecutor(new ForkJoinPool(20));
				Future<scala.collection.immutable.List<String>> futureResponse = contentPackageExtractionUtil.uploadH5pExtractedPackage(contentId, node, extractionBasePath,
						ExtractionType.snapshot, false, context);
                Await.result(futureResponse, Duration.create(3, TimeUnit.MINUTES));
				response = updateContentNode(contentId, node, urlArray[IDX_S3_URL]);
			}catch (IOException e) {
				TelemetryManager.error("Error! While unzipping the content package file.", e);
			} catch (ClientException e) {
				throw e;
			} catch (ServerException e) {
				throw e;
			} catch (Exception e) {
				throw new ServerException(ContentErrorCodeConstants.UPLOAD_ERROR.name(),
						ContentErrorMessageConstants.FILE_UPLOAD_ERROR
								+ " | [Something Went Wrong While Uploading the 'H5P' Content.]");
			} finally {
				try {
					FileUtils.deleteDirectory(new File(extractionBasePath));
				} catch (Exception e) {
					TelemetryManager.error("Unable to delete H5P library directory.", e);
				}
			}
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
		Response response = new Response();
		TelemetryManager.log("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + contentId);
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);

		TelemetryManager.log("Adding 'isPublishOperation' Flag to 'true'");
		parameterMap.put(ContentAPIParams.isPublishOperation.name(), true);

		TelemetryManager.log("Calling the 'Review' Initializer for Node Id: " + contentId);
		response = pipeline.init(ContentAPIParams.review.name(), parameterMap);
		TelemetryManager.log("Review Operation Finished Successfully for Node ID: "+ contentId);

		if (BooleanUtils.isTrue(isAsync)) {
			AsyncContentOperationUtil.makeAsyncOperation(ContentOperations.PUBLISH, contentId, parameterMap);
			TelemetryManager.log("Publish Operation Started Successfully in 'Async Mode' for Node Id: " + contentId);

			response.put(ContentAPIParams.publishStatus.name(),
					"Publish Operation for Content Id '" + contentId + "' Started Successfully!");
		} else {
			TelemetryManager.log("Publish Operation Started Successfully in 'Sync Mode' for Node Id: " + contentId);
			response = pipeline.init(ContentAPIParams.publish.name(), parameterMap);
		}

		return response;
	}

	@Override
	public Response review(String contentId, Node node, boolean isAsync) {
		TelemetryManager.log("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + node.getIdentifier());
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);

		TelemetryManager.log("Calling the 'Review' Initializer for Node ID: " + node.getIdentifier());
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
			// upload Extract Content Package
			ContentPackageExtractionUtil contentPackageExtractionUtil = new ContentPackageExtractionUtil();
			ExecutionContext context = ExecutionContexts.fromExecutor(new ForkJoinPool(20));
			long startTime = System.currentTimeMillis();
			Future<scala.collection.immutable.List<String>> futureResponse = contentPackageExtractionUtil
					.uploadH5pExtractedPackage(contentId, node, extractionBasePath,
					ExtractionType.snapshot, false, context);

			Await.result(futureResponse, Duration.create(3, TimeUnit.MINUTES));
			response = updateContentNode(contentId, node, urlArray[IDX_S3_URL]);
		} catch (IOException e) {
			TelemetryManager.error("Error! While unzipping the content package file.", e);
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
				TelemetryManager.error("Unable to delete H5P library directory.", e);
			}
		}
		return response;
	}

	private String getH5PLibraryPath() {
		String path = Platform.config.getString(ContentConfigurationConstants.DEFAULT_H5P_LIBRARY_PATH_PROPERTY_KEY);
		if (StringUtils.isBlank(path))
			throw new ServerException(ContentErrorCodeConstants.INVALID_LIBRARY.name(),
					ContentErrorMessageConstants.INVALID_H5P_LIBRARY + " | [Invalid H5P Library Package Path.]");
		TelemetryManager.info("Fetched H5P Library Path: " + path);
		return path;
	}

}
