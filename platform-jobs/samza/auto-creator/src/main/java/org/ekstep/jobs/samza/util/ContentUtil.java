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
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.learning.util.CloudStore;

import java.io.File;
import java.io.IOException;
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
	private static final List<String> CONTENT_CREATE_PROPS = Platform.config.hasPath("auto_creator.content_create_props") ? Arrays.asList(Platform.config.getString("auto_creator.content_create_props").split(",")) : new ArrayList<String>();
	private static final List<String> ALLOWED_ARTIFACT_SOURCE = Platform.config.hasPath("auto_creator.artifact_upload.allowed_source") ? Arrays.asList(Platform.config.getString("auto_creator.artifact_upload.allowed_source").split(",")) : new ArrayList<String>();
	private static final Integer API_CALL_DELAY = Platform.config.hasPath("auto_creator.api_call_delay") ? Platform.config.getInt("auto_creator.api_call_delay") : 5;
	public static final List<String> ALLOWED_CONTENT_STAGE = Platform.config.hasPath("auto_creator.allowed_content_stages") ? Arrays.asList(Platform.config.getString("auto_creator.allowed_content_stages").split(",")) : Arrays.asList("create", "upload", "review", "publish");
	private static ObjectMapper mapper = new ObjectMapper();
	private static Tika tika = new Tika();
	private static JobLogger LOGGER = new JobLogger(ContentUtil.class);


	public Boolean validateMetadata(Map<String, Object> metadata) {
		List<String> reqFields = REQUIRED_METADATA_FIELDS.stream().filter(x -> null == metadata.get(x)).collect(Collectors.toList());
		return CollectionUtils.isEmpty(reqFields) ? true : false;
	}

	public Boolean validateStage(String stage) {
		return StringUtils.isNotBlank(stage) ? ALLOWED_CONTENT_STAGE.contains(stage) : true;
	}

	public void process(String channelId, String identifier, Map<String, Object> edata) throws Exception {
		String stage = (String) edata.getOrDefault(AutoCreatorParams.stage.name(), "");
		String repository = (String) edata.getOrDefault(AutoCreatorParams.repository.name(), "");
		Map<String, Object> metadata = (Map<String, Object>) edata.getOrDefault(AutoCreatorParams.metadata.name(), new HashMap<String, Object>());
		Map<String, Object> filteredMetadata = metadata.entrySet().stream().filter(x -> !METADATA_FIELDS_TO_BE_REMOVED.contains(x.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		String mimeType = (String) metadata.getOrDefault(AutoCreatorParams.mimeType.name(), "");
		Integer delayUpload = StringUtils.equalsIgnoreCase(mimeType, "application/vnd.ekstep.h5p-archive") ? 6 * API_CALL_DELAY : API_CALL_DELAY;
		List<Map<String, Object>> collection = (List<Map<String, Object>>) edata.getOrDefault(AutoCreatorParams.collection.name(), new ArrayList<HashMap<String, Object>>());
		Map<String, Object> textbookInfo = (Map<String, Object>) edata.getOrDefault(AutoCreatorParams.textbookInfo.name(), new HashMap<String, Object>());
		String newIdentifier = (String) edata.get(AutoCreatorParams.identifier.name());
		LOGGER.info("ContentUtil :: process :: started processing for: " + identifier + " | Channel : " + channelId + " | Metadata : " + metadata+ " | collection :"+collection +" | textbookInfo : "+textbookInfo);
		String contentStage = "";
		String internalId = "";
		Boolean isCreated = false;
		Boolean isUploaded = false;
		Boolean isReviewed = false;
		Boolean isPublished = false;
		Double pkgVersion = Double.parseDouble(String.valueOf(metadata.getOrDefault(AutoCreatorParams.pkgVersion.name(), "0.0")));
		Map<String, Object> createMetadata = filteredMetadata.entrySet().stream().filter(x -> CONTENT_CREATE_PROPS.contains(x.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		Map<String, Object> updateMetadata = filteredMetadata.entrySet().stream().filter(x->!CONTENT_CREATE_PROPS.contains(x.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		Map<String, Object> contentMetadata = searchContent(identifier);
		if (MapUtils.isEmpty(contentMetadata)) {
			contentStage = "create";
		} else {
			contentStage = getContentStage(identifier, pkgVersion, contentMetadata);
			internalId = (String) contentMetadata.get("contentId");
		}

		try {
			switch (contentStage) {
				case "create": {
					Map<String, Object> result = create(channelId, identifier, newIdentifier, repository, createMetadata);
					internalId = (String) result.get(AutoCreatorParams.identifier.name());
					if (StringUtils.isNotBlank(internalId)) {
						isCreated = true;
						updateMetadata.put(AutoCreatorParams.versionKey.name(), (String) result.get(AutoCreatorParams.versionKey.name()));
					}
				}
				case "update": {
					if (!isCreated) {
						Map<String, Object> readMetadata = read(channelId, internalId);
						updateMetadata.put(AutoCreatorParams.versionKey.name(), (String) readMetadata.get(AutoCreatorParams.versionKey.name()));
					}
					update(channelId, internalId, updateMetadata);
					if (StringUtils.equalsIgnoreCase("create", stage))
						break;
					delay(API_CALL_DELAY);
				}
				case "upload": {
					isUploaded = upload(channelId, internalId, metadata);
					if(StringUtils.equalsIgnoreCase("upload", stage))
						break;
					delay(delayUpload);
				}
				case "review": {
					isReviewed = review(channelId, internalId);
					if(StringUtils.equalsIgnoreCase("review", stage))
						break;
					delay(API_CALL_DELAY);
				}
				case "publish": {
					isPublished = publish(channelId, internalId, (String) metadata.get(AutoCreatorParams.lastPublishedBy.name()));
					break;
				}
				default: {
					LOGGER.info("ContentUtil :: process :: Event Skipped for operations (create, upload, publish) for: " + identifier + " | Content Stage : " + contentStage);
				}
			}
		}catch (Exception e) {
			updateStatus(channelId, internalId, e.getMessage());
			throw e;
		}

		if(CollectionUtils.isNotEmpty(collection) && (isUploaded || isReviewed || isPublished || StringUtils.equalsIgnoreCase("na", contentStage))) {
			linkCollection(channelId, identifier, collection, internalId);
		} else if(MapUtils.isNotEmpty(textbookInfo) && (isUploaded || isReviewed || isPublished || StringUtils.equalsIgnoreCase("na", contentStage))) {
			linkTextbook(channelId, identifier, textbookInfo, internalId);
		}else {
			LOGGER.info("ContentUtil :: process :: Textbook Linking Skipped because received empty collection/textbookInfo for : " + identifier);
		}
		LOGGER.info("ContentUtil :: process :: finished processing for: " + identifier);
	}

	private void updateStatus(String channelId, String identifier, String message) throws Exception {
		String errorMsg = StringUtils.isNotBlank(message) ? message : "Processing Error";
		String url = KP_LEARNING_BASE_URL + "/system/v3/content/update/" + identifier;
		Map<String, Object> request = new HashMap<String, Object>() {{
			put("request", new HashMap<String, Object>() {{
				put("content", new HashMap<String, Object>() {{
					put(AutoCreatorParams.importError.name(), errorMsg);
					put(AutoCreatorParams.status.name(), "Failed");
				}});
			}});
		}};
		Map<String, String> header = new HashMap<String, String>() {{
			put("X-Channel-Id", channelId);
			put("Content-Type", DEFAULT_CONTENT_TYPE);
		}};
		Response resp = UnirestUtil.patch(url, request, header);
		if ((null != resp && resp.getResponseCode() == ResponseCode.OK) && MapUtils.isNotEmpty(resp.getResult())) {
			String node_id = (String) resp.getResult().get("node_id");
			if (StringUtils.isNotBlank(node_id)) {
				LOGGER.info("ContentUtil :: updateStatus :: Content failed status successfully updated for : " + identifier);
			}
			else
				throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Content update status Call Failed For : " + identifier);
		} else {
			LOGGER.info("ContentUtil :: updateStatus :: Invalid Response received while updating failed status for : " + identifier + " | Response Code : " + resp.getResponseCode().toString() + " | Result : " + resp.getResult() + " | Error Message : " + resp.getParams().getErrmsg());
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Invalid Response received while updating content status for : " + identifier);
		}
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
			result = StringUtils.isNotBlank(artifactUrl) ? "review" : "update";
		else if (pkgVersion > pkgVer)
			result = "update";
		else
			LOGGER.info("ContentUtil :: getContentStage :: Skipped Processing for : " + identifier + " | Internal Identifier : " + metadata.get("contentId") + " ,Status : " + status + " , artifactUrl : " + artifactUrl);
		return result;
	}


	private Map<String, Object> create(String channelId, String identifier, String newIdentifier, String repository, Map<String, Object> metadata) throws Exception {
		String contentId = "";
		String url = KP_CS_BASE_URL + "/content/v3/create";
		Map<String, Object> metaFields = new HashMap<String, Object>();
		metaFields.putAll(metadata);
		if(StringUtils.isNotBlank(newIdentifier))
			metaFields.put(AutoCreatorParams.identifier.name(), newIdentifier);
		else {
			metaFields.put(AutoCreatorParams.origin.name(), identifier);
			metaFields.put(AutoCreatorParams.originData.name(), new HashMap<String, Object>(){{
				put(AutoCreatorParams.identifier.name(), identifier);
				put(AutoCreatorParams.repository.name(), repository);
			}});
		}
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
		return resp.getResult();
	}

	private Map<String, Object> read(String channelId, String identifier) throws Exception {
		String contentId = "";
		String url = KP_CS_BASE_URL + "/content/v3/read/" + identifier;
		LOGGER.info("ContentUtil :: read :: Reading content having identifier : "+identifier);
		Map<String, String> header = new HashMap<String, String>() {{
			put("X-Channel-Id", channelId);
			put("Content-Type", DEFAULT_CONTENT_TYPE);
		}};
		Response resp = UnirestUtil.get(url, "mode=edit", header);
		if ((null != resp && resp.getResponseCode() == ResponseCode.OK) && MapUtils.isNotEmpty(resp.getResult())) {
			contentId = (String) ((Map<String, Object>)resp.getResult().getOrDefault("content", new HashMap<String, Object>())).get("identifier");
			if(StringUtils.equalsIgnoreCase(identifier, contentId))
			LOGGER.info("ContentUtil :: read :: Content Fetched Successfully with identifier : " + contentId);
			else throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Invalid Response received while reading content for : " + identifier);
		} else {
			LOGGER.info("ContentUtil :: read :: Invalid Response received while reading content for : " + identifier + " | Response Code : " + resp.getResponseCode().toString() + " | Result : " + resp.getResult() + " | Error Message : " + resp.getParams().getErrmsg());
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Invalid Response received while reading content for : " + identifier);
		}
		return ((Map<String, Object>) resp.getResult().getOrDefault("content", new HashMap<String, Object>()));
	}

	private void update(String channelId, String internalId, Map<String, Object> updateMetadata) throws Exception {
		String url = KP_CS_BASE_URL + "/content/v3/update/" + internalId;
		Map<String, Object> request = new HashMap<String, Object>() {{
			put("request", new HashMap<String, Object>() {{
				put("content", updateMetadata);
			}});
		}};
		LOGGER.info("ContentUtil :: update :: update request : "+request);
		Map<String, String> header = new HashMap<String, String>() {{
			put("X-Channel-Id", channelId);
			put("Content-Type", DEFAULT_CONTENT_TYPE);
		}};
		Response resp = UnirestUtil.patch(url, request, header);
		if ((null != resp && resp.getResponseCode() == ResponseCode.OK) && MapUtils.isNotEmpty(resp.getResult())) {
			String contentId = (String) resp.getResult().get("identifier");
			LOGGER.info("ContentUtil :: update :: Content Update Successfully having identifier : " + contentId);
		} else {
			LOGGER.info("ContentUtil :: update :: Invalid Response received while updating content for : " + internalId + " | Response Code : " + resp.getResponseCode().toString() + " | Result : " + resp.getResult() + " | Error Message : " + resp.getParams().getErrmsg());
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Invalid Response received while updating content for : " + internalId + "| Response Code : " + resp.getResponseCode().toString() + " | Result : " + resp.getResult() + " | Error Message : " + resp.getParams().getErrmsg());
		}
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

	private Boolean upload(String channelId, String identifier, Map<String, Object> metadata) throws Exception {
		Response resp = null;
		Long downloadStartTime = System.currentTimeMillis();
		String sourceUrl = (String) metadata.get(AutoCreatorParams.artifactUrl.name());
		if (CollectionUtils.isNotEmpty(ALLOWED_ARTIFACT_SOURCE) && CollectionUtils.isEmpty(ALLOWED_ARTIFACT_SOURCE.stream().filter(x -> sourceUrl.contains(x)).collect(Collectors.toList()))) {
			LOGGER.info("Artifact Source is not from allowed one for : " + identifier + " | artifactUrl: " + sourceUrl + " | Allowed Sources : " + ALLOWED_ARTIFACT_SOURCE);
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Artifact Source is not from allowed one for : " + identifier + " | artifactUrl: " + sourceUrl + " | Allowed Sources : " + ALLOWED_ARTIFACT_SOURCE);
		}
		File file = getFile(identifier, sourceUrl);
		Long downloadEndTime = System.currentTimeMillis();
		LOGGER.info("ContentUtil :: upload :: Total time taken for download: " + (downloadEndTime - downloadStartTime));
		String mimeType = (String) metadata.getOrDefault("mimeType", "");
		if (null == file || !file.exists()) {
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Error Occurred while downloading file for " + identifier + " | File Url : "+sourceUrl);
		}
		LOGGER.info("ContentUtil :: upload :: File Path for " + identifier + "is : " + file.getAbsolutePath() + " | File Size : " + file.length());
		Long size = FileUtils.sizeOf(file);
		LOGGER.info("ContentUtil :: upload :: file size (MB): " + (size / 1048576));
		String url = KP_CS_BASE_URL + "/content/v3/upload/" + identifier + "?validation=false";
		if (StringUtils.isNotBlank(mimeType) && (StringUtils.equalsIgnoreCase("application/vnd.ekstep.h5p-archive", mimeType) && !StringUtils.equalsIgnoreCase("h5p", FilenameUtils.getExtension(file.getAbsolutePath()))))
			url = url + "&fileFormat=composed-h5p-zip";
		LOGGER.info("Upload API URL : " + url);
		Map<String, String> header = new HashMap<String, String>() {{
			put("X-Channel-Id", channelId);
		}};
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
			if (StringUtils.isNotBlank(artifactUrl)) {
				LOGGER.info("ContentUtil :: upload :: Content Uploaded Successfully for : " + identifier + " | artifactUrl : " + artifactUrl);
				return true;
			}
		} else {
			LOGGER.info("ContentUtil :: upload :: Invalid Response received while uploading for: " + identifier + " | Response Code : " + resp.getResponseCode().toString() + " | Result : " + resp.getResult() + " | Error Message : " + resp.getParams().getErrmsg());
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Invalid Response received while uploading : " + identifier + " | Response Code : " + resp.getResponseCode().toString() + " | Result : " + resp.getResult() + " | Error Message : " + resp.getParams().getErrmsg());
		}
		return false;
	}

	private Boolean review(String channelId, String identifier) throws Exception {
		String url = KP_LEARNING_BASE_URL + "/content/v3/review/" + identifier;
		Map<String, Object> request = new HashMap<String, Object>() {{
			put("request", new HashMap<String, Object>() {{
				put("content", new HashMap<String, Object>());
			}});
		}};

		Map<String, String> header = new HashMap<String, String>() {{
			put("X-Channel-Id", channelId);
			put("Content-Type", DEFAULT_CONTENT_TYPE);
		}};
		Response resp = UnirestUtil.post(url, request, header);
		if ((null != resp && resp.getResponseCode() == ResponseCode.OK) && MapUtils.isNotEmpty(resp.getResult())) {
			String contentId = (String) resp.getResult().get("node_id");
			if(StringUtils.isNotBlank(contentId)) {
				LOGGER.info("ContentUtil :: review :: Content Sent For Review Successfully having identifier : " + contentId);
				return true;
			}
		} else {
			LOGGER.info("ContentUtil :: review :: Invalid Response received while sending content to review for : " + identifier + " | Response Code : " + resp.getResponseCode().toString() + " | Result : " + resp.getResult() + " | Error Message : " + resp.getParams().getErrmsg());
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Invalid Response received while sending content to review for  : " + identifier + "| Response Code : " + resp.getResponseCode().toString() + " | Result : " + resp.getResult() + " | Error Message : " + resp.getParams().getErrmsg());
		}
		return false;
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
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Invalid Response received while publishing content for : " + identifier + " | Response Code : " + resp.getResponseCode().toString() + " | Result : " + resp.getResult() + " | Error Message : " + resp.getParams().getErrmsg());
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

	private void linkCollection(String channel, String eventObjectId, List<Map<String, Object>> collection, String resourceId) throws Exception {
		for (Map<String, Object> textbookInfo : collection) {
			String textbookId = (String) textbookInfo.getOrDefault(AutoCreatorParams.identifier.name(), "");
			String unitId = (String) textbookInfo.getOrDefault(AutoCreatorParams.unitId.name(), "");
			if (StringUtils.isNotBlank(textbookId) && StringUtils.isNotEmpty(unitId)) {
				Map<String, Object> rootHierarchy = null;
				rootHierarchy = getHierarchy(textbookId);
				if (validateHierarchy(textbookId, rootHierarchy, unitId)) {
					Map<String, Object> hierarchyReq = new HashMap<String, Object>() {{
						put(AutoCreatorParams.request.name(), new HashMap<String, Object>() {{
							put(AutoCreatorParams.rootId.name(), textbookId);
							put(AutoCreatorParams.unitId.name(), unitId);
							put(AutoCreatorParams.children.name(), Arrays.asList(resourceId));
						}});
					}};
					addToHierarchy(channel, textbookId, hierarchyReq);
				} else {
					LOGGER.info("ContentUtil :: linkCollection :: Hierarchy Validation Failed For : " + textbookId);
				}
			} else {
				LOGGER.info("ContentUtil :: linkCollection :: Collection Linking Skipped because required data is not available for : " + eventObjectId);
			}
		}
	}

	//TODO: Remove this method in release-3.3.0, Added only for backward compatibility
	private void linkTextbook(String channel, String eventObjectId, Map<String, Object> textbookInfo, String resourceId) throws Exception {
		String textbookId = (String) textbookInfo.getOrDefault(AutoCreatorParams.identifier.name(), "");
		List<String> unitIdentifiers = (List<String>) textbookInfo.getOrDefault(AutoCreatorParams.unitIdentifiers.name(), new ArrayList<String>());
		if (StringUtils.isNotBlank(textbookId) && CollectionUtils.isNotEmpty(unitIdentifiers)) {
			Map<String, Object> rootHierarchy = getHierarchy(textbookId);
			List<String> childNodes = (List<String>) rootHierarchy.getOrDefault(AutoCreatorParams.childNodes.name(), new ArrayList<String>());
			if (CollectionUtils.isNotEmpty(childNodes) && childNodes.containsAll(unitIdentifiers)) {
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

	private boolean validateHierarchy(String textbookId, Map<String, Object> rootHierarchy, String unitId) {
		List<String> childNodes = (List<String>) rootHierarchy.getOrDefault(AutoCreatorParams.childNodes.name(), new ArrayList<String>());
		if (CollectionUtils.isNotEmpty(childNodes) && childNodes.contains(unitId)) {
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
		return StringUtils.isNotBlank(objectId) ? TEMP_FILE_LOCATION + File.separator + objectId + File.separator + "_temp_" + System.currentTimeMillis(): TEMP_FILE_LOCATION + File.separator + "_temp_" + System.currentTimeMillis();
	}

	private String getFileNameFromURL(String fileUrl) {
		String fileName = FilenameUtils.getBaseName(fileUrl) + "_" + System.currentTimeMillis();
		if (!FilenameUtils.getExtension(fileUrl).isEmpty())
			fileName += "." + FilenameUtils.getExtension(fileUrl);
		return fileName;
	}

	private File getFile(String identifier, String fileUrl) {
		try {
			//String fileName = getBasePath(identifier) + File.separator + getFileNameFromURL(fileUrl);
			//File file = new File(fileName);
			//FileUtils.copyURLToFile(new URL(fileUrl), file);
			File file = HttpDownloadUtility.downloadFile(fileUrl, getBasePath(identifier));
			return file;
		} catch (Exception e) {
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

	private void delay(long time) {
		try {
			Thread.sleep(time * 1000);
		} catch (Exception e) {

		}
	}

}
