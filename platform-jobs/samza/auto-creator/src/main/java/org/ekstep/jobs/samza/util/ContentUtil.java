package org.ekstep.jobs.samza.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.Tika;
import org.ekstep.common.Platform;
import org.ekstep.common.Slug;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.learning.util.CloudStore;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ContentUtil {

	private static final String KP_CS_BASE_URL = Platform.config.getString("kp.content_service.base_url");
	private static final String KP_LEARNING_BASE_URL = Platform.config.getString("kp.learning_service.base_url");
	private static final String KP_SEARCH_URL = Platform.config.getString("kp.search_service_base_url") + "/v3/search";
	private static final String PASSPORT_KEY = Platform.config.getString("graph.passport.key.base");
	private static final String TEMP_FILE_LOCATION = Platform.config.hasPath("lp.tempfile.location") ?
			Platform.config.getString("lp.tempfile.location") : "/tmp/content";
	public static final List<String> REQUIRED_METADATA_FIELDS = Arrays.asList(Platform.config.getString("auto_creator.content_mandatory_fields").split(","));
	public static final List<String> METADATA_FIELDS_TO_BE_REMOVED = Arrays.asList(Platform.config.getString("auto_creator.content_props_to_removed").split(","));
	private static final List<String> SEARCH_FIELDS = Arrays.asList("identifier", "mimeType", "pkgVersion", "channel", "status", "origin", "originData","artifactUrl");
	private static final List<String> SEARCH_EXISTS_FIELDS = Arrays.asList("originData");
	private static final List<String> FINAL_STATUS = Arrays.asList("Live", "Unlisted", "Processing");
	private static final String DEFAULT_CONTENT_TYPE = "application/json";
	private static final int IDX_CLOUD_KEY = 0;
	private static final int IDX_CLOUD_URL = 1;
	private static final String CONTENT_FOLDER = "cloud_storage.content.folder";
	private static final String ARTEFACT_FOLDER = "cloud_storage.artefact.folder";
	private static final Long CONTENT_UPLOAD_ARTIFACT_MAX_SIZE = Platform.config.hasPath("auto_creator.artifact_upload.max_size") ? Platform.config.getLong("auto_creator.artifact_upload.max_size") : 62914560;
	private static final List<String> BULK_UPLOAD_MIMETYPES = Platform.config.hasPath("auto_creator.bulk_upload.mime_types") ? Arrays.asList(Platform.config.getString("auto_creator.bulk_upload.mime_types").split(",")) : new ArrayList<String>();
	private static ObjectMapper mapper = new ObjectMapper();
	private static Tika tika = new Tika();
	private static JobLogger LOGGER = new JobLogger(ContentUtil.class);


	public Boolean validateMetadata(Map<String, Object> metadata) {
		List<String> reqFields = REQUIRED_METADATA_FIELDS.stream().filter(x -> null == metadata.get(x)).collect(Collectors.toList());
		return CollectionUtils.isEmpty(reqFields) ? true : false;
	}

	public void process(String channelId, String identifier, String repository, Map<String, Object> metadata, Map<String, Object> textbookInfo) throws Exception {
		LOGGER.info("ContentUtil :: process :: started processing for: " + identifier + " | Channel : " + channelId + " | Metadata : " + metadata+ " | textbookInfo :"+textbookInfo);
		String contentStage = "";
		String internalId = "";
		Boolean isPublished = false;
		Double pkgVersion = Double.parseDouble((String) metadata.getOrDefault(AutoCreatorParams.pkgVersion.name(), "0.0"));
		Map<String, Object> createMetadata = new HashMap<String, Object>();
		Map<String, Object> contentMetadata = searchContent(identifier);
		if (MapUtils.isEmpty(contentMetadata)) {
			contentStage = "create";
			createMetadata = metadata.entrySet().stream().filter(x -> !METADATA_FIELDS_TO_BE_REMOVED.contains(x.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		} else {
			contentStage = getContentStage(identifier, pkgVersion, contentMetadata);
			internalId = (String) contentMetadata.get("contentId");
		}

		switch (contentStage) {
			case "create": {
				internalId = create(channelId, identifier, repository, createMetadata);
			}
			case "upload": {
				upload(channelId, internalId, metadata);
			}
			case "publish": {
				isPublished = publish(channelId, internalId, (String) metadata.get(AutoCreatorParams.lastPublishedBy.name()));
				break;
			}
			default: {
				LOGGER.info("ContentUtil :: process :: Event Skipped for operations (create, upload, publish) for: " + identifier + " | Content Stage : " + contentStage);
			}
		}
		if(MapUtils.isNotEmpty(textbookInfo) && (isPublished || StringUtils.equalsIgnoreCase("na", contentStage))) {
			linkTextbook(channelId, identifier, textbookInfo, internalId);
		} else {
			LOGGER.info("ContentUtil :: process :: Textbook Linking Skipped because received empty textbookInfo for : " + identifier);
		}
		LOGGER.info("ContentUtil :: process :: finished processing for: " + identifier);
	}

	private Map<String, Object> searchContent(String identifier) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, String> header = new HashMap<String, String>() {{
			put("Content-Type", DEFAULT_CONTENT_TYPE);
		}};

		Map<String, Object> request = new HashMap<String, Object>() {{
			put(AutoCreatorParams.request.name(), new HashMap<String, Object>() {{
				put(AutoCreatorParams.filters.name(), new HashMap<String, Object>() {{
					put(AutoCreatorParams.objectType.name(), "Content");
					put(AutoCreatorParams.status.name(), Arrays.asList());
					put(AutoCreatorParams.origin.name(), identifier);
				}});
				put("exists", SEARCH_EXISTS_FIELDS);
				put("fields", SEARCH_FIELDS);
			}});
		}};

		Response resp = UnirestUtil.post(KP_SEARCH_URL, request, header);
		if ((null != resp && resp.getResponseCode() == ResponseCode.OK)) {
			if (MapUtils.isNotEmpty(resp.getResult()) && (Integer) resp.getResult().get(AutoCreatorParams.count.name()) > 0) {
				List<Object> contents = (List<Object>) resp.getResult().get(AutoCreatorParams.content.name());
				contents.stream().map(obj -> (Map<String, Object>) obj).forEach(map -> {
					String contentId = (String) map.get(AutoCreatorParams.identifier.name());
					Map<String, Object> originData = null;
					try {
						originData = mapper.readValue((String) map.get(AutoCreatorParams.originData.name()), Map.class);
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (MapUtils.isNotEmpty(originData)) {
						String originId = (String) originData.get(AutoCreatorParams.identifier.name());
						String repository = (String) originData.get(AutoCreatorParams.repository.name());
						if (StringUtils.equalsIgnoreCase(identifier, originId) && StringUtils.isNotBlank(repository)) {
							result.put("contentId", contentId);
							result.put(AutoCreatorParams.status.name(), map.get(AutoCreatorParams.status.name()));
							result.put(AutoCreatorParams.artifactUrl.name(), map.get(AutoCreatorParams.artifactUrl.name()));
							result.put(AutoCreatorParams.pkgVersion.name(), map.get(AutoCreatorParams.pkgVersion.name()));
							LOGGER.info("ContentUtil :: searchContent :: Internal Content Found with Identifier : " + contentId + " for :" + identifier + " | Result : " + result);
						}
					} else
						LOGGER.info("Received empty originData for " + identifier);
				});
			} else
				LOGGER.info("ContentUtil :: searchContent :: Received 0 count while searching content for : " + identifier);

		} else {
			LOGGER.info("ContentUtil :: searchContent :: Invalid Response received while searching content for : " + identifier + " | Response Code : " + resp.getResponseCode().toString());
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Invalid Response received while searching content for : " + identifier);
		}
		return result;
	}

	private String getContentStage(String identifier, Double pkgVersion, Map<String, Object> metadata) {
		String result = "na";
		String status = (String) metadata.get(AutoCreatorParams.status.name());
		String artifactUrl = (String) metadata.get(AutoCreatorParams.artifactUrl.name());
		Double pkgVer = (Double) metadata.getOrDefault(AutoCreatorParams.pkgVersion.name(), 0.0);
		if (!FINAL_STATUS.contains(status))
			result = StringUtils.isNotBlank(artifactUrl) ? "publish" : "upload";
		else if (pkgVersion > pkgVer)
			result = "upload";
		else
			LOGGER.info("ContentUtil :: getContentStage :: Skipped Processing for : " + identifier + " | Internal Identifier : " + metadata.get("contentId") + " ,Status : " + status + " , artifactUrl : " + artifactUrl);
		return result;
	}


	private String create(String channelId, String identifier, String repository, Map<String, Object> metadata) throws Exception {
		String contentId = "";
		String url = KP_CS_BASE_URL + "/content/v3/create";
		Map<String, Object> metaFields = new HashMap<String, Object>();
		metaFields.putAll(metadata);
		metaFields.put(AutoCreatorParams.origin.name(), identifier);
		metaFields.put(AutoCreatorParams.originData.name(), new HashMap<String, Object>(){{
			put(AutoCreatorParams.identifier.name(), identifier);
			put(AutoCreatorParams.repository.name(), repository);
		}});
		Map<String, Object> request = new HashMap<String, Object>() {{
			put("request", new HashMap<String, Object>() {{
				put("content", metaFields);
			}});
		}};
		LOGGER.info("ContentUtil :: create :: create request : "+request);
		Map<String, String> header = new HashMap<String, String>() {{
			put("X-Channel-Id", channelId);
			put("Content-Type", DEFAULT_CONTENT_TYPE);
		}};
		Response resp = UnirestUtil.post(url, request, header);
		if ((null != resp && resp.getResponseCode() == ResponseCode.OK) && MapUtils.isNotEmpty(resp.getResult())) {
			contentId = (String) resp.getResult().get("identifier");
			LOGGER.info("ContentUtil :: create :: Content Created Successfully with identifier : " + contentId);
		} else {
			LOGGER.info("ContentUtil :: create :: Invalid Response received while creating content for : " + identifier + " | Response Code : " + resp.getResponseCode().toString() + " | Result : " + resp.getResult() + " | Error Message : " + resp.getParams().getErrmsg());
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Invalid Response received while creating content for : " + identifier);
		}
		return contentId;
	}

	/*private void upload(String channelId, String identifier, File file) throws Exception {
		if (null != file && !file.exists())
			LOGGER.info("ContentUtil :: upload :: File Path for " + identifier + "is : " + file.getAbsolutePath() + " | File Size : " + file.length());
		String preSignedUrl = getPreSignedUrl(identifier, file.getName());
		String fileUrl = preSignedUrl.split("\\?")[0];
		Boolean isUploaded = uploadBlob(identifier, preSignedUrl, file);
		if (isUploaded) {
			String url = KP_CS_BASE_URL + "/content/v3/upload/" + identifier;
			Map<String, String> header = new HashMap<String, String>() {{
				put("X-Channel-Id", channelId);
			}};
			Response resp = UnirestUtil.post(url, "fileUrl", fileUrl, header);
			if ((null != resp && resp.getResponseCode() == ResponseCode.OK) && MapUtils.isNotEmpty(resp.getResult())) {
				String artifactUrl = (String) resp.getResult().get(AutoCreatorParams.artifactUrl.name());
				if (StringUtils.isNotBlank(artifactUrl) && StringUtils.equalsIgnoreCase(fileUrl, artifactUrl))
					LOGGER.info("ContentUtil :: upload :: Content Uploaded Successfully for : " + identifier + " | artifactUrl : " + artifactUrl);
			} else {
				LOGGER.info("ContentUtil :: upload :: Invalid Response received while uploading for: " + identifier + " | Response Code : " + resp.getResponseCode().toString() + " | Result : " + resp.getResult() + " | Error Message : " + resp.getParams().getErrmsg());
				throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Invalid Response received while uploading : " + identifier);
			}
		} else {
			LOGGER.info("ContentUtil :: upload :: Blob upload failed for: " + identifier);
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Upload failed for: " + identifier);
		}
	}*/

	private void upload(String channelId, String identifier, Map<String, Object> metadata) throws Exception {
		Response resp = null;
		Long downloadStartTime = System.currentTimeMillis();
		File file = getFile(identifier, (String) metadata.get(AutoCreatorParams.artifactUrl.name()));
		Long downloadEndTime = System.currentTimeMillis();
		LOGGER.info("ContentUtil :: upload :: Total time taken for download: " + (downloadEndTime - downloadStartTime));
		String mimeType = (String) metadata.getOrDefault("mimeType", "");
		if (null != file && !file.exists())
			LOGGER.info("ContentUtil :: upload :: File Path for " + identifier + "is : " + file.getAbsolutePath() + " | File Size : " + file.length());
		Long size = FileUtils.sizeOf(file);
		LOGGER.info("ContentUtil :: upload :: file size (MB): " + (size / 1048576));
		String url = KP_CS_BASE_URL + "/content/v3/upload/" + identifier + "?validation=false";
		if (StringUtils.isNotBlank(mimeType) && (StringUtils.equalsIgnoreCase("application/vnd.ekstep.h5p-archive", mimeType) && ".h5p" != FilenameUtils.getExtension(file.getAbsolutePath())))
			url = url + "&fileFormat=composed-h5p-zip";
		Map<String, String> header = new HashMap<String, String>() {{
			put("X-Channel-Id", channelId);
		}};
		//TODO: Add Validate for Url Host. Allow fileUrls only from specific host
		if (size > CONTENT_UPLOAD_ARTIFACT_MAX_SIZE) {
			if (BULK_UPLOAD_MIMETYPES.contains(mimeType)) {
				Long uploadStartTime = System.currentTimeMillis();
				String[] urls = uploadArtifact(file, identifier);
				Long uploadEndTime = System.currentTimeMillis();
				LOGGER.info("ContentUtil :: upload :: Total time taken for upload: " + (uploadEndTime - uploadStartTime));
				if (null != urls && StringUtils.isNotBlank(urls[1])) {
					String uploadUrl = urls[IDX_CLOUD_URL];
					LOGGER.info("ContentUtil :: upload :: Artifact Uploaded Successfully to cloud for : " + identifier + " | uploadUrl : " + uploadUrl);
					resp = UnirestUtil.post(url, "fileUrl", uploadUrl, header);
				}
			} else {
				LOGGER.info("ContentUtil :: upload :: File Size is larger than allowed file size allowed in upload api for : " + identifier + " | File Size (MB): " + (size / 1048576) + " | mimeType : " + mimeType);
				throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "File Size is larger than allowed file size allowed in upload api for : " + identifier + " | File Size (MB): " + (size / 1048576) + " | mimeType : " + mimeType);
			}
		} else {
			resp = UnirestUtil.post(url, "file", file, header);
		}
		if ((null != resp && resp.getResponseCode() == ResponseCode.OK) && MapUtils.isNotEmpty(resp.getResult())) {
			String artifactUrl = (String) resp.getResult().get(AutoCreatorParams.artifactUrl.name());
			if (StringUtils.isNotBlank(artifactUrl))
				LOGGER.info("ContentUtil :: upload :: Content Uploaded Successfully for : " + identifier + " | artifactUrl : " + artifactUrl);
		} else {
			LOGGER.info("ContentUtil :: upload :: Invalid Response received while uploading for: " + identifier + " | Response Code : " + resp.getResponseCode().toString() + " | Result : " + resp.getResult() + " | Error Message : " + resp.getParams().getErrmsg());
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Invalid Response received while uploading : " + identifier);
		}
	}

	private Boolean publish(String channelId, String identifier, String lastPublishedBy) throws Exception {
		String url = KP_LEARNING_BASE_URL + "/content/v3/publish/" + identifier;
		Map<String, Object> request = new HashMap<String, Object>() {{
			put("request", new HashMap<String, Object>() {{
				put("content", new HashMap<String, Object>() {{
					put(AutoCreatorParams.lastPublishedBy.name(), lastPublishedBy);
				}});
			}});
		}};
		Map<String, String> header = new HashMap<String, String>() {{
			put("X-Channel-Id", channelId);
			put("Content-Type", DEFAULT_CONTENT_TYPE);
		}};
		Response resp = UnirestUtil.post(url, request, header);
		if ((null != resp && resp.getResponseCode() == ResponseCode.OK) && MapUtils.isNotEmpty(resp.getResult())) {
			String publishStatus = (String) resp.getResult().get("publishStatus");
			if (StringUtils.isNotBlank(publishStatus)) {
				LOGGER.info("ContentUtil :: publish :: Content sent for publish successfully for : " + identifier);
				return true;
			}
			else
				throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Content Publish Call Failed For : " + identifier);
		} else {
			LOGGER.info("ContentUtil :: publish :: Invalid Response received while publishing content for : " + identifier + " | Response Code : " + resp.getResponseCode().toString() + " | Result : " + resp.getResult() + " | Error Message : " + resp.getParams().getErrmsg());
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Invalid Response received while publishing content for : " + identifier);
		}

	}

	private String getPreSignedUrl(String identifier, String fileName) throws Exception {
		String preSignedUrl = "";
		Map<String, Object> request = new HashMap<String, Object>(){{
			put("request", new HashMap<String, Object>(){{
				put("content", new HashMap<String, Object>(){{
					put("fileName", fileName);
				}});
			}});
		}};
		Map<String, String> header = new HashMap<String, String>(){{
			put("Content-Type","application/json");
		}};
		String url = KP_CS_BASE_URL + "/content/v3/upload/url/" + identifier;
		Response resp = UnirestUtil.post(url, request, header);
		if ((null != resp && resp.getResponseCode() == ResponseCode.OK) && MapUtils.isNotEmpty(resp.getResult())) {
			preSignedUrl = (String) resp.getResult().get("pre_signed_url");
			return preSignedUrl;
		} else {
			LOGGER.info("ContentUtil :: getPreSignedUrl :: Invalid Response received while generating pre-signed url for : " + identifier + " | Response Code : " + resp.getResponseCode().toString());
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Invalid Response received while generating pre-signed url for : " + identifier);
		}
	}

	private Boolean uploadBlob(String identifier, String url, File file) throws Exception {
		Boolean result = false;
		String contentType = tika.detect(file);
		LOGGER.info("contentType of file : "+contentType);
		Map<String, String> header = new HashMap<String, String>(){{
			put("x-ms-blob-type", "BlockBlob");
			put("Content-Type", contentType);
		}};
		HttpResponse<String> response = Unirest.put(url).headers(header).field("file", new File(file.getAbsolutePath())).asString();
		if (null != response && response.getStatus()==201) {
			result = true;
		} else {
			LOGGER.info("ContentUtil :: uploadBlob :: Invalid Response received while uploading file to blob store for : " + identifier + " | Response Code : " + response.getStatus());
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Invalid Response received while uploading file to blob store for : " + identifier);
		}
		return result;
	}

	private void linkTextbook(String channel, String eventObjectId, Map<String, Object> textbookInfo, String resourceId) throws Exception {
		String textbookId = (String) textbookInfo.getOrDefault(AutoCreatorParams.identifier.name(), "");
		List<String> unitIdentifiers = (List<String>) textbookInfo.getOrDefault(AutoCreatorParams.unitIdentifiers.name(), new ArrayList<String>());
		if (StringUtils.isNotBlank(textbookId) && CollectionUtils.isNotEmpty(unitIdentifiers)) {
			Map<String, Object> rootHierarchy = getHierarchy(textbookId);
			if (validateHierarchy(textbookId, rootHierarchy, unitIdentifiers)) {
				Map<String, Object> hierarchyReq = new HashMap<String, Object>() {{
					put(AutoCreatorParams.request.name(), new HashMap<String, Object>() {{
						put(AutoCreatorParams.rootId.name(), textbookId);
						put(AutoCreatorParams.unitId.name(), unitIdentifiers.get(0));
						put(AutoCreatorParams.children.name(), Arrays.asList(resourceId));
					}});
				}};
				addToHierarchy(channel, textbookId, hierarchyReq);
			} else {
				LOGGER.info("ContentUtil :: linkTextbook :: Hierarchy Validation Failed For : " + textbookId);
			}
		} else {
			LOGGER.info("ContentUtil :: linkTextbook :: Textbook Linking Skipped because required data is not available for : " + eventObjectId);
		}
	}

	private boolean validateHierarchy(String textbookId, Map<String, Object> rootHierarchy, List<String> units) {
		List<String> childNodes = (List<String>) rootHierarchy.getOrDefault(AutoCreatorParams.childNodes.name(), new ArrayList<String>());
		if (CollectionUtils.isNotEmpty(childNodes) && childNodes.containsAll(units)) {
			return true;
		} else {
			LOGGER.info("ContentUtil :: validateHierarchy :: Unit Identifier is not found under : " + textbookId);
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Unit Identifier is not found under : " + textbookId);
		}
	}

	private Boolean addToHierarchy(String channel, String textbookId, Map<String, Object> hierarchyReq) throws Exception {
		Boolean result = false;
		String url = KP_CS_BASE_URL + "/content/v3/hierarchy/add";
		Map<String, String> header = new HashMap<String, String>() {{
			put("X-Channel-Id", channel);
			put("Content-Type", DEFAULT_CONTENT_TYPE);
		}};
		Response resp = UnirestUtil.patch(url, hierarchyReq, header);
		if ((null != resp && resp.getResponseCode() == ResponseCode.OK) && MapUtils.isNotEmpty(resp.getResult())) {
			String contentId = (String) resp.getResult().get("rootId");
			if (StringUtils.equalsIgnoreCase(contentId, textbookId)) {
				LOGGER.info("ContentUtil :: addToHierarchy :: Content Hierarchy Updated Successfully for: " + textbookId);
				result = true;
			}
		} else {
			LOGGER.info("ContentUtil :: updateHierarchy :: Invalid Response received while adding resource to hierarchy for : " + textbookId + " | Response Code : " + resp.getResponseCode().toString() + " | Result : " + resp.getResult() + " | Error Message : " + resp.getParams().getErrmsg());
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Invalid Response received while adding resource to hierarchy for : " + textbookId);
		}
		return result;
	}

	private Map<String, Object> getHierarchy(String identifier) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		String url = KP_CS_BASE_URL + "/content/v3/hierarchy/" + identifier;
		Map<String, String> header = new HashMap<String, String>(){{
			put("Content-Type", DEFAULT_CONTENT_TYPE);
		}};
		Response resp = UnirestUtil.get(url, "mode=edit", header);
		if ((null != resp && resp.getResponseCode() == ResponseCode.OK) && MapUtils.isNotEmpty(resp.getResult())) {
			result = (Map<String, Object>) resp.getResult().getOrDefault("content", new HashMap<String, Object>());
			return result;
		} else {
			LOGGER.info("ContentUtil :: getHierarchy :: Invalid Response received while fetching hierarchy for : " + identifier + " | Response Code : " + resp.getResponseCode().toString());
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Invalid Response received while fetching hierarchy for : " + identifier);
		}
	}

	private String getBasePath(String objectId) {
		return StringUtils.isNotBlank(objectId) ? TEMP_FILE_LOCATION + File.separator + objectId + "_temp_" + System.currentTimeMillis(): "";
	}

	private String getFileNameFromURL(String fileUrl) {
		String fileName = FilenameUtils.getBaseName(fileUrl) + "_" + System.currentTimeMillis();
		if (!FilenameUtils.getExtension(fileUrl).isEmpty())
			fileName += "." + FilenameUtils.getExtension(fileUrl);
		return fileName;
	}

	private File getFile(String identifier, String fileUrl) {
		try {
			String fileName = getBasePath(identifier) + File.separator + getFileNameFromURL(fileUrl);
			File file = new File(fileName);
			FileUtils.copyURLToFile(new URL(fileUrl), file);
			return file;
		} catch (IOException e) {
			LOGGER.info("Invalid fileUrl received for : " + identifier + " | fileUrl : " + fileUrl + "Exception is : " + e.getMessage());
			throw new ServerException(TaxonomyErrorCodes.ERR_INVALID_UPLOAD_FILE_URL.name(), "Invalid fileUrl received for : " + identifier + " | fileUrl : " + fileUrl);
		}
	}

	private String[] uploadArtifact(File uploadedFile, String identifier) {
		String[] urlArray = new String[] {};
		try {
			String folder = S3PropertyReader.getProperty(CONTENT_FOLDER);
			folder = folder + "/" + Slug.makeSlug(identifier, true) + "/" + S3PropertyReader.getProperty(ARTEFACT_FOLDER);
			urlArray = CloudStore.uploadFile(folder, uploadedFile, true);
		} catch (Exception e) {
			LOGGER.info("ContentUtil :: uploadArtifact ::  Exception occurred while uploading artifact for : " + identifier + "Exception is : " + e.getMessage());
			e.printStackTrace();
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_UPLOAD_FILE.name(),
					"Error while uploading the File.", e);
		}
		return urlArray;
	}

}
