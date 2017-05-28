package org.ekstep.content.operation.finalizer;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.optimizr.ThumbnailGenerator;
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.common.util.ZipUtility;
import org.ekstep.content.common.ContentConfigurationConstants;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.entity.Plugin;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.content.pipeline.BasePipeline;
import org.ekstep.content.util.ECRFToJSONConvertor;
import org.ekstep.content.util.ECRFToXMLConvertor;

import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.dac.model.Node;

/**
 * The Class BaseFinalizer is a BaseClass for all Finalizers, extends BasePipeline which
 * mainly holds Common Methods and operations of a ContentNode.
 * BaseFinalizer holds Common methods of ContentNode and ContentPackage
 */
public class BaseFinalizer extends BasePipeline {
	
	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(BaseFinalizer.class.getName());
	
	/** The Constant IDX_S3_URL. */
	private static final int IDX_S3_URL = 1;
	
	private static final String s3Artifact = "s3.artifact.folder";
	
	/**
	 * Creates Thumbline.
	 * 
	 * @param Node the ContentNode
	 * @param basePath the filePath
	 * checks if node metadata contains appIcon and downloadFile
	 * checks if fileIsNotEmpty & fileIsFile and generates thumbline for it
	 * checks if thumbline isNotEmpty and creates thumbFile
	 * uploads thumbFile to s3
	 */
	@SuppressWarnings("unchecked")
	protected void createThumbnail(String basePath, Node node) {
		try {
			if (null != node) {
				String appIcon = (String) node.getMetadata().get(ContentWorkflowPipelineParams.appIcon.name());
				
				// checks if node contains appIcon and downloads File
				if (!StringUtils.isBlank(appIcon)) {
					LOGGER.info("Content Id: " + node.getIdentifier() + " | App Icon: " + appIcon);
					File appIconFile = HttpDownloadUtility.downloadFile(appIcon, basePath);
					
					// checks if file is not empty and isFile nd generates thumbline
					if (null != appIconFile && appIconFile.exists() && appIconFile.isFile()) {
						boolean generated = ThumbnailGenerator.generate(appIconFile);
						if (generated) {
							String thumbnail = appIconFile.getParent() + File.separator
									+ FilenameUtils.getBaseName(appIconFile.getPath()) + ".thumb."
									+ FilenameUtils.getExtension(appIconFile.getPath());
							File thumbFile = new File(thumbnail);
					
							// uploads thumbfile to s3 and set node metadata
							if (thumbFile.exists()) {
								LOGGER.info("Thumbnail created for Content Id: " + node.getIdentifier());
								String folderName = S3PropertyReader.getProperty(s3Artifact);
								String[] urlArray = uploadToAWS(thumbFile, getUploadFolderName(node.getIdentifier(), folderName));
								if (null != urlArray && urlArray.length >= 2) {
									String thumbUrl = urlArray[IDX_S3_URL];
									node.getMetadata().put(ContentWorkflowPipelineParams.appIcon.name(), thumbUrl);
									node.getMetadata().put(ContentWorkflowPipelineParams.posterImage.name(), appIcon);
								}
								try {
									thumbFile.delete();
									LOGGER.info("Deleted local Thumbnail file");
								} catch (Exception e) {
									LOGGER.error("Error! While deleting the Thumbnail File.", e);
								}
							}
						}
						try {
							appIconFile.delete();
							LOGGER.info("Deleted local AppIcon file");
						} catch (Exception e) {
							LOGGER.error("Error! While deleting the App Icon File.", e);
						}
					}
				}
				
				LOGGER.info("Processing Stage Icons");
				List<String> stageIcons = (List<String>) node.getMetadata()
						.get(ContentWorkflowPipelineParams.thumbnail.name());
				String path = basePath + File.separator + "thumbnails";
				if (null != stageIcons && !stageIcons.isEmpty()) {
					List<String> stageIconsS3Url = new ArrayList<>();
					for (String stageIcon : stageIcons) {
						stageIconsS3Url.add(getThumbnailFiles(path, node, stageIcon));
					}
					node.getMetadata().put(ContentWorkflowPipelineParams.thumbnail.name(), stageIconsS3Url);
				}

			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new ServerException(ContentErrorCodeConstants.DOWNLOAD_ERROR.name(),
					ContentErrorMessageConstants.APP_ICON_DOWNLOAD_ERROR
							+ " | [Unable to Download App Icon for Content Id: '" + node.getIdentifier() + "' ]",
					e);
		}
	}

	private File downloadStageIconFiles(String basePath, String stageIconFile, String stageIconId) {
		File file = null;
		try {
			LOGGER.info("Downloading stageIcons");
			String base64Image = stageIconFile.split(",")[1];
			byte[] imageBytes = Base64.decodeBase64(base64Image);
			BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
			file = new File(basePath + File.separator + stageIconId);
			ImageIO.write(bufferedImage, "png", file);
		} catch (Exception e) {
			LOGGER.error("Something went wrong when downloading base64 image" + e.getMessage());
			throw new ServerException(ContentErrorCodeConstants.DOWNLOAD_ERROR.name(),
					ContentErrorMessageConstants.STAGE_ICON_DOWNLOAD_ERROR + " | [Unable to Upload File.]");
		}
		return file;
	}

	private String getThumbnailFiles(String basePath, Node node, String stageIconFile) {
		String thumbUrl = "";
		try {
			File stageIcon = downloadStageIconFiles(basePath, stageIconFile, "");
			if (stageIcon.exists()) {
				LOGGER.info("Thumbnail created for Content Id: " + node.getIdentifier());
				String folderName = S3PropertyReader.getProperty(s3Artifact) + "/" + "thumbnails";
				String[] urlArray = uploadToAWS(stageIcon, getUploadFolderName(node.getIdentifier(), folderName));
				if (null != urlArray && urlArray.length >= 2) {
					thumbUrl = urlArray[IDX_S3_URL];
				}
				stageIcon.delete();
				LOGGER.info("Deleted local Thumbnail file");
			}
		} catch (Exception e) {
			LOGGER.error("Error! While deleting the Thumbnail File.", e);
			throw new ServerException(ContentErrorCodeConstants.UPLOAD_ERROR.name(),
					ContentErrorMessageConstants.FILE_UPLOAD_ERROR + " | [Unable to Upload File.]");
		}
		return thumbUrl;
	}
	
	/**
	 * writes ECML to File.
	 * 
	 * @param ecml the string ECML
	 * @param type the EcmlType
	 * @param basePath the filePath
	 * checks if ecml string or ecmlType is empty, throws ClientException
	 * else creates a File and writes the ecml to file
	 */
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
	/**
	 * gets the ECML string from ECRF Object.
	 * 
	 * @param ecrf the ECRF
	 * @param type the EcmlType
	 * checks if ecmlType is JSON/ECML
	 * converts ECRF to ecml
	 * @return ecml
	 */
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
	
	/**
	 * Creates ZipPackage.
	 * 
	 * @param ZipFileName
	 * @param basePath the filePath
	 * creates zipPackage from the filePath
	 */
	protected void createZipPackage(String basePath, String zipFileName) {
		if (!StringUtils.isBlank(zipFileName)) {
			LOGGER.info("Creating Zip File: " + zipFileName);
			ZipUtility appZip = new ZipUtility(basePath, zipFileName);
			appZip.generateFileList(new File(basePath));
			appZip.zipIt(zipFileName);
		}
	}

}