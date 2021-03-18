package org.sunbird.content.operation.finalizer;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.common.optimizr.ThumbnailGenerator;
import org.sunbird.common.util.HttpDownloadUtility;
import org.sunbird.common.util.S3PropertyReader;
import org.sunbird.common.util.ZipUtility;
import org.sunbird.content.common.ContentConfigurationConstants;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.entity.Plugin;
import org.sunbird.content.enums.ContentErrorCodeConstants;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.content.pipeline.BasePipeline;
import org.sunbird.content.util.ECRFToJSONConvertor;
import org.sunbird.content.util.ECRFToXMLConvertor;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.engine.router.GraphEngineManagers;
import org.sunbird.learning.common.enums.ContentAPIParams;
import org.sunbird.telemetry.logger.TelemetryManager;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class BaseFinalizer is a BaseClass for all Finalizers, extends
 * BasePipeline which mainly holds Common Methods and operations of a
 * ContentNode. BaseFinalizer holds Common methods of ContentNode and
 * ContentPackage
 */
public class BaseFinalizer extends BasePipeline {

	/** The logger. */

	/** The Constant IDX_S3_URL. */
	private static final int IDX_S3_URL = 1;

	protected static final String ARTEFACT_FOLDER = "cloud_storage.artefact.folder";

	/**
	 * Creates Thumbline.
	 * 
	 * @param Node
	 *            the ContentNode
	 * @param basePath
	 *            the filePath checks if node metadata contains appIcon and
	 *            downloadFile checks if fileIsNotEmpty & fileIsFile and generates
	 *            thumbline for it checks if thumbline isNotEmpty and creates
	 *            thumbFile uploads thumbFile to s3
	 */
	protected void createThumbnail(String basePath, Node node) {
		try {
			if (null != node) {
				String appIcon = (String) node.getMetadata().get(ContentWorkflowPipelineParams.appIcon.name());

				// checks if node contains appIcon and downloads File
				if (!StringUtils.isBlank(appIcon)) {
					TelemetryManager.log("Content Id: " + node.getIdentifier() + " | App Icon: " + appIcon);
					File appIconFile = HttpDownloadUtility.downloadFile(appIcon, basePath);

					// checks if file is not empty and isFile nd generates
					// thumbline
					if (null != appIconFile && appIconFile.exists() && appIconFile.isFile()) {
						boolean generated = ThumbnailGenerator.generate(appIconFile);
						if (generated) {
							String thumbnail = appIconFile.getParent() + File.separator
									+ FilenameUtils.getBaseName(appIconFile.getPath()) + ".thumb."
									+ FilenameUtils.getExtension(appIconFile.getPath());
							File thumbFile = new File(thumbnail);

							// uploads thumbfile to s3 and set node metadata
							if (thumbFile.exists()) {
								TelemetryManager.log("Thumbnail created for Content Id: " + node.getIdentifier());
								String folderName = S3PropertyReader.getProperty(ARTEFACT_FOLDER);
								String[] urlArray = uploadToAWS(thumbFile,
										getUploadFolderName(node.getIdentifier(), folderName));
								if (null != urlArray && urlArray.length >= 2) {
									String thumbUrl = urlArray[IDX_S3_URL];
									node.getMetadata().put(ContentWorkflowPipelineParams.appIcon.name(), thumbUrl);
									node.getMetadata().put(ContentWorkflowPipelineParams.posterImage.name(), appIcon);
								}
								try {
									thumbFile.delete();
									TelemetryManager.log("Deleted local Thumbnail file");
								} catch (Exception e) {
									TelemetryManager.error("Error! While deleting the Thumbnail File: " + thumbFile, e);
								}
							}
						}
						try {
							appIconFile.delete();
							TelemetryManager.log("Deleted local AppIcon file");
						} catch (Exception e) {
							TelemetryManager.error("Error! While deleting the App Icon File: " + appIcon, e);
						}
					}
				}

				try {
					String[] stageIconsStr = (String[]) node.getMetadata()
							.get(ContentWorkflowPipelineParams.screenshots.name());
					if (null != stageIconsStr) {
						List<String> stageIcons = Arrays.asList(stageIconsStr);
						TelemetryManager.log("Processing Stage Icons" + stageIcons);
						String path = basePath + File.separator + ContentWorkflowPipelineParams.screenshots.name();
						if (null != stageIcons && !stageIcons.isEmpty()) {
							List<String> stageIconsS3Url = new ArrayList<>();
							for (String stageIcon : stageIcons) {
								if (!isS3Url(stageIcon)) {
									String iconUrl = getThumbnailFiles(path, node, stageIcon);
									if (StringUtils.isNotBlank(iconUrl))
										stageIconsS3Url.add(iconUrl);
								} else {
									stageIconsS3Url.add(stageIcon);
								}
							}
							node.getMetadata().put(ContentWorkflowPipelineParams.screenshots.name(), stageIconsS3Url);
						}
					}
				} catch (Exception e) {
					TelemetryManager
							.error("Error!Unable to downnload Stage Icon for Content Id: " + node.getIdentifier(), e);
					throw new ServerException(ContentErrorCodeConstants.DOWNLOAD_ERROR.name(),
							ContentErrorMessageConstants.STAGE_ICON_DOWNLOAD_ERROR
									+ " | [Unable to download Stage Icon for Content Id: '" + node.getIdentifier()
									+ "' ]",
							e);
				}
			}
		} catch (Exception e) {
			TelemetryManager.error("Error! Unable to download appIcon for content Id: " + node.getIdentifier(), e);
			throw new ServerException(ContentErrorCodeConstants.DOWNLOAD_ERROR.name(),
					ContentErrorMessageConstants.APP_ICON_DOWNLOAD_ERROR
							+ " | [Unable to Download App Icon for Content Id: '" + node.getIdentifier() + "' ]",
					e);
		}
	}

	private boolean isS3Url(String url) {
		if (null != url) {
			try {
				new URL(url.toString());
				if (StringUtils.containsIgnoreCase(url, "s3") && StringUtils.containsIgnoreCase(url, "ekstep-public")) {
					return true;
				}
			} catch (MalformedURLException e) {
				return false;
			}
			return false;
		} else {
			return false;
		}
	}

	private File downloadStageIconFiles(String basePath, String stageIconFile, String stageIconId) {
		File file = null;
		try {
			TelemetryManager.log("Downloading stageIcons");
			String base64Image = stageIconFile.split(",")[1];
			String mimeType = stageIconFile.split(";")[0];
			mimeType = mimeType.split(":")[1];
			mimeType = mimeType.split("/")[1];
			byte[] imageBytes = Base64.decodeBase64(base64Image);
			BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
			if (null == stageIconId) {
				stageIconId = "stage.png";
				mimeType = ".png";
			} else {
				stageIconId = stageIconId + "." + mimeType;
			}
			File saveFile = new File(basePath);
			if (!saveFile.exists()) {
				saveFile.mkdirs();
			}
			file = new File(basePath + File.separator + stageIconId);

			ImageIO.write(bufferedImage, mimeType, file);
			TelemetryManager.log("StageIcon Downloaded File: " + stageIconId);
		} catch (Exception e) {
			TelemetryManager.error("Something went wrong when downloading base64 image", e);
			throw new ServerException(ContentErrorCodeConstants.DOWNLOAD_ERROR.name(),
					ContentErrorMessageConstants.STAGE_ICON_DOWNLOAD_ERROR + " | [Unable to Upload File.]");
		}
		return file;
	}

	@SuppressWarnings("unchecked")
	private String getThumbnailFiles(String basePath, Node node, String stageIconFile) {
		String thumbUrl = "";
		String stageIconId = null;
		ObjectMapper mapper = new ObjectMapper();
		File stageIcon = null;
		try {
			if (stageIconFile.contains("http")) {
				stageIconFile = StringUtils.removeEnd(stageIconFile, "?format=base64");
				stageIconId = stageIconFile.substring((stageIconFile.indexOf("/stage") + 7), stageIconFile.length());

				HttpURLConnection httpConn = null;
				TelemetryManager.log("Start Downloading for File: " + stageIconId);

				URL url = new URL(stageIconFile);
				httpConn = (HttpURLConnection) url.openConnection();
				int responseCode = httpConn.getResponseCode();
				TelemetryManager.log("Response Code: " + responseCode);

				// always check HTTP response code first
				if (responseCode == HttpURLConnection.HTTP_OK) {
					TelemetryManager.log("Response is OK.");
					BufferedReader br = new BufferedReader(new InputStreamReader((httpConn.getInputStream())));
					StringBuilder sb = new StringBuilder();
					String output;
					while ((output = br.readLine()) != null) {
						sb.append(output);
					}
					String result = sb.toString();
					Map<String, Object> dataMap = mapper.readValue(result, Map.class);
					dataMap = (Map<String, Object>) dataMap.get("result");
					String base64Data = (String) dataMap.get("result");
					stageIcon = downloadStageIconFiles(basePath, base64Data, stageIconId);

				}
			} else {
				stageIcon = downloadStageIconFiles(basePath, stageIconFile, stageIconId);
			}

			if (stageIcon.exists()) {
				TelemetryManager.log("Thumbnail created for Content Id: " + node.getIdentifier());
				String folderName = S3PropertyReader.getProperty(ARTEFACT_FOLDER) + "/"
						+ ContentWorkflowPipelineParams.screenshots.name();
				String[] urlArray = uploadToAWS(stageIcon, getUploadFolderName(node.getIdentifier(), folderName));
				if (null != urlArray && urlArray.length >= 2) {
					thumbUrl = urlArray[IDX_S3_URL];
				}
				stageIcon.delete();
				TelemetryManager.log("Deleted local Thumbnail file");
			}
		} catch (Exception e) {
			TelemetryManager.error("Error! While Processing the StageIcon File.", e);

		}
		return thumbUrl;
	}

	/**
	 * writes ECML to File.
	 * 
	 * @param ecml
	 *            the string ECML
	 * @param type
	 *            the EcmlType
	 * @param basePath
	 *            the filePath checks if ecml string or ecmlType is empty, throws
	 *            ClientException else creates a File and writes the ecml to file
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

			TelemetryManager.log("ECML File Type: " + ecmlType);
			File file = new File(basePath + File.separator + ContentConfigurationConstants.DEFAULT_ECML_FILE_NAME
					+ ContentConfigurationConstants.FILENAME_EXTENSION_SEPERATOR + ecmlType);
			TelemetryManager.log("Creating ECML File With Name: " + file.getAbsolutePath());
			FileUtils.writeStringToFile(file, ecml);
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodeConstants.ECML_FILE_WRITE.name(),
					ContentErrorMessageConstants.ECML_FILE_WRITE_ERROR + " | [Unable to Write ECML File.]", e);
		}
	}

	/**
	 * gets the ECML string from ECRF Object.
	 * 
	 * @param ecrf
	 *            the ECRF
	 * @param type
	 *            the EcmlType checks if ecmlType is JSON/ECML converts ECRF to ecml
	 * @return ecml
	 */
	protected String getECMLString(Plugin ecrf, String ecmlType) {
		String ecml = "";
		if (null != ecrf) {
			TelemetryManager.log("Converting ECML From ECRF Object.");
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
	 * @param basePath
	 *            the filePath creates zipPackage from the filePath
	 */
	protected void createZipPackage(String basePath, String zipFileName) {
		if (!StringUtils.isBlank(zipFileName)) {
			TelemetryManager.log("Creating Zip File: " + zipFileName);
			ZipUtility appZip = new ZipUtility(basePath, zipFileName);
			appZip.generateFileList(new File(basePath));
			appZip.zipIt(zipFileName);
		}
	}

	protected Node getNodeForOperation(String taxonomyId, Node node) {
		Node nodeForOperation = new Node();
		if (null != node && null != node.getMetadata() && StringUtils.isNotBlank(taxonomyId)) {
			String status = (String) node.getMetadata().get(ContentWorkflowPipelineParams.status.name());

			if (StringUtils.equalsIgnoreCase(node.getObjectType(), ContentWorkflowPipelineParams.ContentImage.name())
					|| (!StringUtils.equalsIgnoreCase(status, ContentWorkflowPipelineParams.Flagged.name())
							&& !StringUtils.equalsIgnoreCase(status, ContentWorkflowPipelineParams.Live.name())))
				nodeForOperation = node;
			else {
				String contentImageId = getContentImageIdentifier(node.getIdentifier());
				TelemetryManager.log("Fetching the Content Node. | [Content ID: " + node.getIdentifier() + "]");

				TelemetryManager.log("Fetching the Content Image Node for Content Id: " + node.getIdentifier());
				Response response = getDataNode(taxonomyId, contentImageId);
				if (!checkError(response)) {
					// Content Image Node is Available so assigning it as node
					nodeForOperation = (Node) response.get(GraphDACParams.node.name());
					TelemetryManager.log(
							"Getting Content Image Node and assigning it as node" + nodeForOperation.getIdentifier());
				} else {
					nodeForOperation = createContentImageNode(taxonomyId, contentImageId, node);
				}
			}
		}
		String body = getContentBody(nodeForOperation.getIdentifier());
		nodeForOperation.getMetadata().put(ContentAPIParams.body.name(), body);
		TelemetryManager.log("Body fetched from content store");
		return nodeForOperation;

	}

	protected String getContentImageIdentifier(String contentId) {
		String contentImageId = "";
		if (StringUtils.isNotBlank(contentId))
			contentImageId = contentId + ContentConfigurationConstants.DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
		return contentImageId;
	}

	protected Node createContentImageNode(String taxonomyId, String contentImageId, Node node) {
		TelemetryManager.log("Taxonomy Id: " + taxonomyId + " :: " + "Content Id: " + contentImageId);
		Node imageNode = new Node(taxonomyId, SystemNodeTypes.DATA_NODE.name(),
				ContentConfigurationConstants.CONTENT_IMAGE_OBJECT_TYPE);
		imageNode.setGraphId(taxonomyId);
		imageNode.setIdentifier(contentImageId);
		imageNode.setMetadata(node.getMetadata());
		imageNode.setInRelations(node.getInRelations());
		imageNode.setOutRelations(node.getOutRelations());
		imageNode.getMetadata().put(ContentWorkflowPipelineParams.status.name(),
				ContentWorkflowPipelineParams.Draft.name());
		Response response = createDataNode(imageNode);
		if (checkError(response))
			throw new ServerException(ContentErrorCodeConstants.IMAGE_NODE_CREATION_ERROR.name(),
					"Error! Something went wrong while performing the operation. | [Content Id: " + node.getIdentifier()
							+ "]");
		Response resp = getDataNode(taxonomyId, contentImageId);
		Node nodeData = (Node) resp.get(GraphDACParams.node.name());
		TelemetryManager.log("Returning Content Image Node Identifier" + nodeData.getIdentifier());
		return nodeData;
	}

	protected Response createDataNode(Node node) {
		Response response = new Response();
		if (null != node) {
			Request request = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "createDataNode");
			request.put(GraphDACParams.node.name(), node);

			TelemetryManager.log("Creating the Node ID: " + node.getIdentifier());
			response = getResponse(request);
		}
		return response;
	}

}