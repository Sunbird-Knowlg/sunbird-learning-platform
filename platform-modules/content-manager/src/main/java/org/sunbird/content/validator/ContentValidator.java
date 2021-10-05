package org.sunbird.content.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.CoverageIgnore;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.common.optimizr.FileUtils;
import org.sunbird.common.util.HttpDownloadUtility;
import org.sunbird.content.common.AssetsMimeTypeMap;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.enums.ContentErrorCodeConstants;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.learning.common.enums.ContentErrorCodes;
import org.sunbird.telemetry.logger.TelemetryManager;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * The Class ContentValidator, mainly used for validating ContentNode and
 * ContentPackage.
 */
public class ContentValidator {

	/** The Constant DEF_CONTENT_PACKAGE_MIME_TYPE. */
	private static final String DEF_CONTENT_PACKAGE_MIME_TYPE = "application/zip";

	/** The Constant BUNDLE_PATH. */
	private static final String BUNDLE_PATH = "/tmp";

	/** The youtubeUrl regex */
	//TODO: Use youtube Regex from config
	private static final String YOUTUBE_REGEX = "^(http(s)?:\\/\\/)?((w){3}.)?youtu(be|.be)?(\\.com)?\\/.+";

	/** The pdf mimeType */
	private static final String PDF_MIMETYPE = "application/pdf";
	
	/** The doc mimeType */
	private static final String DOC_MIMETYPE =  "application/msword";
	
	private static final String EPUB_MIMETYPE = "application/epub";
	
	/** The allowed extensions */
	//TODO: Get it from config.
	private static Set<String> allowed_file_extensions = new HashSet<String>();

	private static Tika tika = new Tika();

	static {
		allowed_file_extensions.add("doc");
		allowed_file_extensions.add("docx");
		allowed_file_extensions.add("ppt");
		allowed_file_extensions.add("pptx");
		allowed_file_extensions.add("key");
		allowed_file_extensions.add("odp");
		allowed_file_extensions.add("pps");
		allowed_file_extensions.add("odt");
		allowed_file_extensions.add("wpd");
		allowed_file_extensions.add("wps");
		allowed_file_extensions.add("wks");
	}

	/**
	 * validates the Uploaded ContentPackage File.
	 *
	 * @param file
	 *            the file
	 * checks MimeType(application/zip), FolderStructure(assets, data and
	 *         widgets), FileSize
	 * @return true if uploaded package meets all checks else return false
	 */
	public boolean isValidContentPackage(File file) {
		boolean isValidContentPackage = false;
		try {
			if (file.exists()) {
				TelemetryManager.log("Validating File: " + file.getName());
				if (!isValidContentMimeType(file))
					throw new ClientException(ContentErrorCodeConstants.VALIDATOR_ERROR.name(),
							ContentErrorMessageConstants.INVALID_CONTENT_PACKAGE_FILE_MIME_TYPE_ERROR
									+ " | [The uploaded package is invalid]");
				if (!isValidContentPackageStructure(file))
					throw new ClientException(ContentErrorCodeConstants.VALIDATOR_ERROR.name(),
							ContentErrorMessageConstants.INVALID_CONTENT_PACKAGE_STRUCTURE_ERROR
									+ " | ['index' file and other folders (assets, data & widgets) should be at root location]");
				if (!isValidContentSize(file))
					throw new ClientException(ContentErrorCodeConstants.VALIDATOR_ERROR.name(),
							ContentErrorMessageConstants.INVALID_CONTENT_PACKAGE_SIZE_ERROR
									+ " | [Content Package file size is too large]");

				isValidContentPackage = true;
			}
		} catch (ClientException ce) {
			throw ce;
		} catch (IOException e) {
			throw new ServerException(ContentErrorCodeConstants.VALIDATOR_ERROR.name(),
					ContentErrorMessageConstants.CONTENT_PACKAGE_FILE_OPERATION_ERROR
							+ " | [Something went wrong while processing the Package file.]",
					e);
		} catch (Exception e) {
			throw new ClientException(ContentErrorCodeConstants.VALIDATOR_ERROR.name(),
					ContentErrorMessageConstants.CONTENT_PACKAGE_VALIDATOR_ERROR
							+ " | [Something went wrong while validating the Package file.]",
					e);
		}
		TelemetryManager.log("Is it a valid Content Package File ? : " + isValidContentPackage);
		return isValidContentPackage;
	}

	/**
	 * Validates the Uploaded Plugin package File.
	 *
	 * @param file
	 *            the file
	 * checks MimeType(application/zip), FolderStructure(manifest.json)
	 * @return true if uploaded package meets all @checks else return false
	 */
	public boolean isValidPluginPackage(File file) {
		boolean isValidContentPackage = false;
		try {
			if (file.exists()) {
				TelemetryManager.log("Validating File: " + file.getName());
				if (!isValidContentMimeType(file))
					throw new ClientException(ContentErrorCodeConstants.VALIDATOR_ERROR.name(),
							ContentErrorMessageConstants.INVALID_CONTENT_PACKAGE_FILE_MIME_TYPE_ERROR
									+ " | [The uploaded package is invalid]");
				if (!isValidPluginPackageStructure(file))
					throw new ClientException(ContentErrorCodeConstants.VALIDATOR_ERROR.name(),
							ContentErrorMessageConstants.INVALID_CONTENT_PACKAGE_STRUCTURE_ERROR
									+ " | [manifest.json should be at root location]");
				isValidContentPackage = true;
			}
		} catch (ClientException ce) {
			throw ce;
		} catch (IOException e) {
			throw new ServerException(ContentErrorCodeConstants.VALIDATOR_ERROR.name(),
					ContentErrorMessageConstants.CONTENT_PACKAGE_FILE_OPERATION_ERROR
							+ " | [Something went wrong while processing the Package file.]",
					e);
		} catch (Exception e) {
			throw new ClientException(ContentErrorCodeConstants.VALIDATOR_ERROR.name(),
					ContentErrorMessageConstants.CONTENT_PACKAGE_VALIDATOR_ERROR
							+ " | [Something went wrong while validating the Package file.]",
					e);
		}
		TelemetryManager.log("Is it a valid Plugin Package File ? : " + isValidContentPackage);
		return isValidContentPackage;
	}

	/**
	 * validates the contentNode
	 *
	 * @param node
	 *            the node
	 * checks metadata, MimeType, artifact url and Content body
	 * @return true if ContentNode meets all @checks else return false
	 */
	@CoverageIgnore
	public boolean isValidContentNode(Node node) {
		boolean isValidContentNode = false;
		try {
			if (null != node) {
				TelemetryManager.log("Validating Content Node: " + node.getIdentifier());
				if (null == node.getMetadata())
					throw new ClientException(ContentErrorCodeConstants.VALIDATOR_ERROR.name(),
							ContentErrorMessageConstants.INVALID_CONTENT_METADATA + " | [Invalid Metadata.]");
				if (!isAllRequiredFieldsAvailable(node))
					throw new ClientException(ContentErrorCodeConstants.VALIDATOR_ERROR.name(),
							ContentErrorMessageConstants.MISSING_REQUIRED_FIELDS + " | [Content of Mime-Type '"
									+ node.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name())
									+ "' require few mandatory fields for further processing.]");

				isValidContentNode = true;
			}
		} catch (ClientException e) {
			throw e;
		} catch (ServerException e) {
			throw e;
		} catch (Exception e) {
			throw new ClientException(ContentErrorCodeConstants.VALIDATOR_ERROR.name(),
					ContentErrorMessageConstants.CONTENT_NODE_VALIDATION_ERROR
							+ " | [Something went wrong while validating Content Node.",
					e);
		}
		TelemetryManager.log("Is it a valid Content Node ? : " + isValidContentNode);
		return isValidContentNode;
	}

	/**
	 * validates the Uploaded ContentPackage's MimeType
	 *
	 * @param file
	 *            the file
	 * @return true if Uploaded file's MimeType is valid('application/zip') else
	 *         return false throws IOException if erroe occurs during file
	 *         processing
	 */
	private boolean isValidContentMimeType(File file) throws IOException {
		boolean isValidMimeType = false;
		if (file.exists()) {
			TelemetryManager.log("Validating File For MimeType: " + file.getName());
			String mimeType = tika.detect(file);
			isValidMimeType = StringUtils.equalsIgnoreCase(DEF_CONTENT_PACKAGE_MIME_TYPE, mimeType);
		}
		return isValidMimeType;
	}

	/**
	 * validates the Uploaded ContentPackage's file size
	 *
	 * @param file
	 *            the file
	 * @return true if Uploaded file's size is valid(within
	 *         ContentPackageFileSizeLimit) else return false
	 */
	private boolean isValidContentSize(File file) {
		boolean isValidSize = false;
		if (file.exists()) {
			TelemetryManager.log("Validating File For Size: " + file.getName());
			if (file.length() <= getContentPackageFileSizeLimit())
				isValidSize = true;
		}
		return isValidSize;
	}

	/**
	 * gets the ContentPackageFileSizeLimit
	 * 
	 * @return FileSizeLimit(configurable)
	 */
	private double getContentPackageFileSizeLimit() {
		double size = 52428800;
		if(Platform.config.hasPath(ContentWorkflowPipelineParams.MAX_CONTENT_PACKAGE_FILE_SIZE_LIMIT.name()))
			size = Platform.config.getDouble(ContentWorkflowPipelineParams.MAX_CONTENT_PACKAGE_FILE_SIZE_LIMIT.name());
		return size;
	}

	/**
	 * validates the Uploaded ContentPackage's folderStructure
	 *
	 * @param file
	 *            the file
	 * @return true if Uploaded file's folderStructure contains
	 *         ('index.json'/index.ecml') else return false
	 */
	private boolean isValidContentPackageStructure(File file) throws IOException {
		final String JSON_ECML_FILE_NAME = "index.json";
		final String XML_ECML_FILE_NAME = "index.ecml";
		boolean isValidPackage = false;
		if (file.exists()) {
			TelemetryManager.log("Validating File For Folder Structure: " + file.getName());
			try (ZipFile zipFile = new ZipFile(file)) {
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					if (StringUtils.equalsIgnoreCase(entry.getName(), JSON_ECML_FILE_NAME)
							|| StringUtils.equalsIgnoreCase(entry.getName(), XML_ECML_FILE_NAME)) {
						isValidPackage = true;
						break;
					}
				}
			}
		}
		return isValidPackage;
	}

	/**
	 * validates the Uploaded ContentPackage's folderStructure
	 *
	 * @param file
	 *            the file
	 * @return true if Uploaded file's folderStructure contains
	 *         ('manifest.json') else return false
	 */
	private boolean isValidPluginPackageStructure(File file) throws IOException {
		final String MANIFEST_FILE_NAME = "manifest.json";
		boolean isValidPackage = false;
		if (file.exists()) {
			TelemetryManager.log("Validating File For Folder Structure: " + file.getName());
			try (ZipFile zipFile = new ZipFile(file)) {
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					if (StringUtils.equalsIgnoreCase(entry.getName(), MANIFEST_FILE_NAME)) {
						isValidPackage = true;
						break;
					}
				}
			}
		}
		return isValidPackage;
	}

	/**
	 * validates the Uploaded ContentNode has all required fields and properties
	 *
	 * @param node
	 *            the node
	 * @checks ContentBody and artifact-url
	 * @return true if the ContentNode meets all @checks else return false
	 */
	//TODO: Validation logic can be moved to definition
	@CoverageIgnore
	private boolean isAllRequiredFieldsAvailable(Node node) {
		boolean isValid = false;
		if (null != node) {
			String name = (String) node.getMetadata().get(ContentWorkflowPipelineParams.name.name());
			String mimeType = (String) node.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name());
			if (StringUtils.isNotBlank(mimeType)) {
				TelemetryManager.log("Checking Required Fields For: " + mimeType);
				switch (mimeType) {
				case "application/vnd.ekstep.ecml-archive":
					/** Either 'body' or 'artifactUrl' is needed */
					if (StringUtils
							.isNotBlank((String) node.getMetadata().get(ContentWorkflowPipelineParams.body.name()))
							|| StringUtils.isNotBlank(
									(String) node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name())))
						isValid = true;
					else
						throw new ClientException(ContentErrorCodeConstants.VALIDATOR_ERROR.name(),
								ContentErrorMessageConstants.MISSING_REQUIRED_FIELDS
										+ " | [Either 'body' or 'artifactUrl' are required for processing of ECML content '"
										+ name + "']");
					break;

				case "application/vnd.ekstep.html-archive":
					/** 'artifactUrl is needed' */
					if (StringUtils.isNotBlank(
							(String) (node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name()))))
						isValid = true;
					else
						throw new ClientException(ContentErrorCodeConstants.VALIDATOR_ERROR.name(),
								ContentErrorMessageConstants.MISSING_REQUIRED_FIELDS
										+ " | [HTML archive should be uploaded for further processing of HTML content '"
										+ name + "']");
					break;

				case "application/vnd.android.package-archive":
					/** 'artifactUrl is needed' */
					if (StringUtils.isNotBlank(
							(String) (node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name()))))
						isValid = true;
					else
						throw new ClientException(ContentErrorCodeConstants.VALIDATOR_ERROR.name(),
								ContentErrorMessageConstants.MISSING_REQUIRED_FIELDS
										+ " | [APK file should be uploaded for further processing of APK content '"
										+ name + "']");
					break;
					
				case "application/vnd.ekstep.h5p-archive":
					/** 'artifactUrl is needed' */
					if (StringUtils.isNotBlank(
							(String) (node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name()))))
						isValid = true;
					else
						throw new ClientException(ContentErrorCodeConstants.VALIDATOR_ERROR.name(),
								ContentErrorMessageConstants.MISSING_REQUIRED_FIELDS
										+ " | [H5P file should be uploaded for further processing of H5P content '"
										+ name + "']");
					break;

				case "application/vnd.ekstep.content-collection":
					isValid = true;
					break;
				
				case "video/x-youtube":
				case "video/youtube":
					if (StringUtils.isNotBlank(
							(String) node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name()))) {
						Boolean isValidYouTubeUrl = Pattern.matches(YOUTUBE_REGEX,
								node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name()).toString());
						TelemetryManager.log("Validating if the given youtube url is valid or not" + isValidYouTubeUrl);
						if (!isValidYouTubeUrl)
							throw new ClientException(ContentErrorCodes.INVALID_YOUTUBE_URL.name(),
									ContentErrorMessageConstants.INVALID_YOUTUBE_URL,
									" | [Invalid or 'null' operation.] Publish Operation Failed");
						else
							isValid = true;
					} else {
						throw new ClientException(ContentErrorCodes.MISSING_YOUTUBE_URL.name(),
								ContentErrorMessageConstants.MISSING_YOUTUBE_URL,
								" | [Invalid or 'missing' youtube Url.] Publish Operation Failed");
					}
					break;
				
				case "text/x-url":
					if (StringUtils.isBlank(
							(String) node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name())))
							throw new ClientException(ContentErrorCodes.INVALID_ARTIFACT.name(),
									ContentErrorMessageConstants.INVALID_URL,
									" | [Invalid or 'null' operation.] Publish Operation Failed");
						else
							isValid = true;
							break;

				case "application/pdf":
					if (StringUtils.isNotBlank(
							(String) node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name()))) {
						String artifactUrl = (String) node.getMetadata()
								.get(ContentWorkflowPipelineParams.artifactUrl.name());
						if (isValidUrl(artifactUrl, mimeType))
							isValid = true;
					} else {
						throw new ClientException(ContentErrorCodeConstants.VALIDATOR_ERROR.name(),
								ContentErrorMessageConstants.MISSING_REQUIRED_FIELDS
										+ " |Invalid or 'null' operation, Publish Operation Failed '" + name + "']");
					}
					break;

				case "application/epub":
					if (StringUtils.isNotBlank(
							(String) node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name()))) {
						String artifactUrl = (String) node.getMetadata()
								.get(ContentWorkflowPipelineParams.artifactUrl.name());
						if (isValidUrl(artifactUrl, mimeType))
							isValid = true;
							
					} else {
						throw new ClientException(ContentErrorCodeConstants.VALIDATOR_ERROR.name(),
								ContentErrorMessageConstants.MISSING_REQUIRED_FIELDS
										+ " |Invalid or 'null' operation, Publish Operation Failed '" + name + "']");
					}
					break;
					
				case "application/msword":
					if (StringUtils.isNotBlank(
							(String) node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name()))) {
						String artifactUrl = (String) node.getMetadata().get("artifactUrl");
						if (isValidUrl(artifactUrl, mimeType))
							isValid = true;

					} else {
						throw new ClientException(ContentErrorCodeConstants.VALIDATOR_ERROR.name(),
								ContentErrorMessageConstants.MISSING_REQUIRED_FIELDS
										+ " |Invalid or 'null' operation, Publish Operation Failed '" + name + "']");
					}
					break;

				case "application/vnd.ekstep.plugin-archive":
					isValid = true;
					break;

				case "assets":
					isValid = true;
					break;
				default:
					TelemetryManager.log("Deafult Case for Mime-Type: " + mimeType);
					if (AssetsMimeTypeMap.isAllowedMimeType(mimeType) && StringUtils.isNotBlank(
							(String) (node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name()))))
						isValid = true;
					break;
				}
			}
		}
		return isValid;
	}

	/**
	 * This method validates if the uploaded file belongs the node allowed file
	 * extensions for that mimeType
	 * 
	 * @param fileURL file URL
	 * @param mimeType MimeType of the content
	 * @return boolean
	 */
	public Boolean isValidUrl(String fileURL, String mimeType) {
		Boolean isValid = false;
		File file = HttpDownloadUtility.downloadFile(fileURL, BUNDLE_PATH);
		try {
			if (exceptionChecks(mimeType, file)) {
				return true;
			}
		} finally {
			FileUtils.deleteFile(file);
		}
		return isValid;
	}

	public Boolean exceptionChecks(String mimeType, File file) {
		String extension = FilenameUtils.getExtension(file.getPath());
		try {
			TelemetryManager.log("Validating File For MimeType: " + file.getName());
			Tika tika = new Tika();
			String file_type = tika.detect(file);
			if (StringUtils.equalsIgnoreCase(mimeType, PDF_MIMETYPE)) {
				if (StringUtils.equalsIgnoreCase(extension, ContentWorkflowPipelineParams.pdf.name()) && file_type.equals("application/pdf")) {
					return true;
				} else {
					throw new ClientException(ContentErrorCodes.INVALID_FILE.name(),
							ContentErrorMessageConstants.INVALID_UPLOADED_FILE_EXTENSION_ERROR
									+ "Uploaded file is not a pdf file");
				}
			}
			if (StringUtils.equalsIgnoreCase(mimeType, EPUB_MIMETYPE)) {
				if (StringUtils.equalsIgnoreCase(extension, ContentWorkflowPipelineParams.epub.name()) && file_type.equals("application/epub+zip")) {
					return true;
				} else {
					throw new ClientException(ContentErrorCodes.INVALID_FILE.name(),
							ContentErrorMessageConstants.INVALID_UPLOADED_FILE_EXTENSION_ERROR
									+ "Uploaded file is not a epub file");
				}
			}
			if (StringUtils.equalsIgnoreCase(mimeType, DOC_MIMETYPE)) {
				if(StringUtils.isNotBlank(extension)){
					if (StringUtils.isNotBlank(extension) && allowed_file_extensions.contains(extension)) {
						return true;
					}
					else {
						throw new ClientException(ContentErrorCodes.INVALID_FILE.name(),
								ContentErrorMessageConstants.INVALID_UPLOADED_FILE_EXTENSION_ERROR
										+ " | Uploaded file should be among the Allowed_file_extensions for mimeType doc"
										+ allowed_file_extensions);
					}
				}
				//proper validations needs to be done - backlog
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}