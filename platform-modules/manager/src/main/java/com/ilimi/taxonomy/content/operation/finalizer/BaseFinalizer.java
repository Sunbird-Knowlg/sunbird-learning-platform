package com.ilimi.taxonomy.content.operation.finalizer;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.optimizr.ThumbnailGenerator;
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.common.util.ZipUtility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.content.common.ContentConfigurationConstants;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.pipeline.BasePipeline;
import com.ilimi.taxonomy.content.util.ECRFToJSONConvertor;
import com.ilimi.taxonomy.content.util.ECRFToXMLConvertor;

public class BaseFinalizer extends BasePipeline {

	private static Logger LOGGER = LogManager.getLogger(BaseFinalizer.class.getName());

	private ObjectMapper mapper = new ObjectMapper();

	private static final int IDX_S3_URL = 1;

	protected void createThumbnail(String basePath, Node node) {
		try {
			if (null != node) {
				String appIcon = (String) node.getMetadata().get(ContentWorkflowPipelineParams.appIcon.name());
				if (!StringUtils.isBlank(appIcon)) {
					LOGGER.info("Content Id: " + node.getIdentifier() + " | App Icon: " + appIcon);
					File appIconFile = HttpDownloadUtility.downloadFile(appIcon, basePath);
					if (null != appIconFile && appIconFile.exists() && appIconFile.isFile()) {
						boolean generated = ThumbnailGenerator.generate(appIconFile);
						if (generated) {
							String thumbnail = appIconFile.getParent() + File.separator
									+ FilenameUtils.getBaseName(appIconFile.getPath()) + ".thumb."
									+ FilenameUtils.getExtension(appIconFile.getPath());
							File thumbFile = new File(thumbnail);
							if (thumbFile.exists()) {
								LOGGER.info("Thumbnail created for Content Id: " + node.getIdentifier());
								String[] urlArray = uploadToAWS(thumbFile, getUploadFolderName());
								if (null != urlArray && urlArray.length >= 2) {
									String thumbUrl = urlArray[IDX_S3_URL];
									node.getMetadata().put(ContentWorkflowPipelineParams.appIcon.name(), thumbUrl);
									node.getMetadata().put(ContentWorkflowPipelineParams.posterImage.name(), appIcon);
								}
								try {
									thumbFile.delete();
									LOGGER.info("Deleted local Thumbnail file");
								} catch (Exception e) {
									LOGGER.info("Error! While deleting the Thumbnail File.", e);
								}
							}
						}
						try {
							appIconFile.delete();
							LOGGER.info("Deleted local AppIcon file");
						} catch (Exception e) {
							LOGGER.info("Error! While deleting the App Icon File.", e);
						}
					}
				}
			}
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodeConstants.DOWNLOAD_ERROR.name(),
					ContentErrorMessageConstants.APP_ICON_DOWNLOAD_ERROR
							+ " | [Unable to Download App Icon for Content Id: '" + node.getIdentifier() + "' ]",
					e);
		}
	}

	protected void writeECMLFile(String basePath, String ecml, String ecmlType) {
		try {
			if (StringUtils.isBlank(ecml))
				throw new ClientException(ContentErrorCodeConstants.EMPTY_ECML.name(),
						ContentErrorMessageConstants.EMPTY_ECML_STRING + " | [Unable to write Empty ECML File.]");
			if (StringUtils.isBlank(ecmlType))
				throw new ClientException(ContentErrorCodeConstants.INVALID_ECML_TYPE.name(),
						ContentErrorMessageConstants.INVALID_ECML_TYPE
								+ " | [System is in a fix between (XML & JSON) ECML Type.]");

			LOGGER.info("ECML File Type: " + ecmlType);
			File file = new File(basePath + File.separator + ContentConfigurationConstants.DEFAULT_ECML_FILE_NAME
					+ ContentConfigurationConstants.FILENAME_EXTENSION_SEPERATOR + ecmlType);
			LOGGER.info("Creating ECML File With Name: " + file.getAbsolutePath());
			FileUtils.writeStringToFile(file, ecml);
		} catch (IOException e) {
			throw new ServerException(ContentErrorCodeConstants.ECML_FILE_WRITE.name(),
					ContentErrorMessageConstants.ECML_FILE_WRITE_ERROR + " | [Unable to Write ECML File.]");
		}
	}

	protected String getECMLString(Plugin ecrf, String ecmlType) {
		String ecml = "";
		if (null != ecrf) {
			LOGGER.info("Converting ECML From ECRF Object.");
			if (StringUtils.equalsIgnoreCase(ecmlType, ContentWorkflowPipelineParams.ecml.name())) {
				ECRFToXMLConvertor convertor = new ECRFToXMLConvertor();
				ecml = convertor.getContentXmlString(ecrf);
			} else if (StringUtils.equalsIgnoreCase(ecmlType, ContentWorkflowPipelineParams.json.name())) {
				ECRFToJSONConvertor convertor = new ECRFToJSONConvertor();
				ecml = convertor.getContentJsonString(ecrf);
			}
		}
		return ecml;
	}

	protected void createManifestFile(File manifestFileName, String manifestVersion, String expiresOn,
			List<Map<String, Object>> contents) {
		try {
			if (null == contents || contents.isEmpty())
				throw new ClientException(ContentErrorCodeConstants.MANIFEST_FILE_WRITE.name(),
						ContentErrorMessageConstants.MANIFEST_FILE_WRITE_ERROR
								+ " | [Content List is 'null' or Empty.]");
			if (StringUtils.isBlank(manifestVersion))
				manifestVersion = "1.0";

			LOGGER.info("Manifest Header Version: " + manifestVersion);

			String header = "{ \"id\": \"ekstep.content.archive\", \"ver\": \"" + manifestVersion + "\", \"ts\": \""
					+ getResponseTimestamp() + "\", \"params\": { \"resmsgid\": \"" + getUUID()
					+ "\"}, \"archive\": { \"count\": " + contents.size() + ", \"expires\": \"" + expiresOn
					+ "\", \"ttl\": 24, \"items\": ";

			LOGGER.info("Content Items in Manifest JSON: " + contents.size());

			// Convert to JSON String
			String manifestJSON = header + mapper.writeValueAsString(contents) + "}}";

			FileUtils.writeStringToFile(manifestFileName, manifestJSON);
			LOGGER.info("Manifest JSON Written");
		} catch (IOException e) {
			throw new ServerException(ContentErrorCodeConstants.MANIFEST_FILE_WRITE.name(),
					ContentErrorMessageConstants.MANIFEST_FILE_WRITE_ERROR + " | [Unable to Write Manifest File.]", e);
		}
	}

	protected void createZipPackage(String basePath, String zipFileName) {
		if (!StringUtils.isBlank(zipFileName)) {
			LOGGER.info("Creating Zip File: " + zipFileName);
			ZipUtility appZip = new ZipUtility(basePath, zipFileName);
			appZip.generateFileList(new File(basePath));
			appZip.zipIt(zipFileName);
		}
	}

	protected byte[] createECAR(List<File> files) throws IOException {
		// creating byteArray stream, make it bufferable and passing this buffer
		// to ZipOutputStream
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
				ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream)) {
			// packing files
			for (File file : files) {
				if (null != file) {
					String fileName = null;
					if (file.getName().toLowerCase().endsWith("manifest.json")) {
						fileName = file.getName();
					} else {
						fileName = file.getParent().substring(file.getParent().lastIndexOf(File.separator) + 1)
								+ File.separator + file.getName();
					}
					// new zip entry and copying inputstream with file to
					// zipOutputStream, after all closing streams
					zipOutputStream.putNextEntry(new ZipEntry(fileName));
					FileInputStream fileInputStream = new FileInputStream(file);

					IOUtils.copy(fileInputStream, zipOutputStream);

					fileInputStream.close();
					zipOutputStream.closeEntry();
				}
			}

			if (zipOutputStream != null) {
				zipOutputStream.finish();
				zipOutputStream.flush();
				IOUtils.closeQuietly(zipOutputStream);
			}
			IOUtils.closeQuietly(bufferedOutputStream);
			IOUtils.closeQuietly(byteArrayOutputStream);
			return byteArrayOutputStream.toByteArray();
		}
	}

	protected File createBundle(List<File> files, String bundleFileName) {
		File bundleFile = new File(bundleFileName);
		try {
			if (null == files || files.isEmpty())
				throw new ClientException(ContentErrorCodeConstants.BUNDLE_FILE_WRITE.name(),
						ContentErrorMessageConstants.NO_FILES_TO_BUNDLE + " | [Atleast one file is needed to bundle.]");
			if (StringUtils.isBlank(bundleFileName))
				throw new ClientException(ContentErrorCodeConstants.BUNDLE_FILE_WRITE.name(),
						ContentErrorMessageConstants.INVALID_BUNDLE_FILE_NAME + " | [Bundle File Name is Required.]");
			try (FileOutputStream stream = new FileOutputStream(bundleFileName)) {
				stream.write(createECAR(files));
			}
		} catch (Throwable e) {
			throw new ServerException(ContentErrorCodeConstants.BUNDLE_FILE_WRITE.name(),
					ContentErrorMessageConstants.BUNDLE_FILE_WRITE_ERROR + " | [Unable to Bundle File.]", e);
		}
		return bundleFile;
	}

}
