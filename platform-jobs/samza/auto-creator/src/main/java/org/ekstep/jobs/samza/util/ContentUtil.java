package org.ekstep.jobs.samza.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.Tika;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
	private static final List<String> SEARCH_FIELDS = Arrays.asList("identifier", "mimeType", "pkgVersion", "channel", "status", "origin", "originData");
	private static final List<String> SEARCH_EXISTS_FIELDS = Arrays.asList("originData");
	private static final List<String> FINAL_STATUS = Arrays.asList("Live", "Unlisted", "Processing");
	private static final String DEFAULT_CONTENT_TYPE = "application/json";
	private static ObjectMapper mapper = new ObjectMapper();
	private static Tika tika = new Tika();
	private static JobLogger LOGGER = new JobLogger(ContentUtil.class);


	public Boolean validateMetadata(Map<String, Object> metadata) {
		List<String> reqFields = REQUIRED_METADATA_FIELDS.stream().filter(x -> null == metadata.get(x)).collect(Collectors.toList());
		return CollectionUtils.isEmpty(reqFields) ? true : false;
	}

	public void process(String channelId, String identifier, String repository, Map<String, Object> metadata) throws Exception {
		LOGGER.info("ContentUtil :: process :: started processing for: " + identifier + " | Channel : " + channelId + " | Metadata : " + metadata);
		String contentStage = "";
		String internalId = "";
		Double pkgVersion = (Double) metadata.getOrDefault(AutoCreatorParams.pkgVersion.name(), 0.0);
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
				upload(channelId, internalId, getFile(internalId, (String) metadata.get(AutoCreatorParams.artifactUrl.name())));
			}
			case "publish": {
				publish(channelId, internalId, (String) metadata.get(AutoCreatorParams.lastPublishedBy.name()));
				break;
			}
			default: {
				LOGGER.info("ContentUtil :: process :: Event Skipped for: " + identifier + " | Content Stage : " + contentStage);
			}
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

	private void upload(String channelId, String identifier, File file) throws Exception {
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
	}

	private void publish(String channelId, String identifier, String lastPublishedBy) throws Exception {
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
			if (StringUtils.isNotBlank(publishStatus))
				LOGGER.info("ContentUtil :: publish :: Content sent for publish successfully for : " + identifier);
			else
				throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Content Publish Call Failed For : " + identifier);
		} else {
			LOGGER.info("ContentUtil :: publish :: Invalid Response received while publishing content for : " + identifier + " | Response Code : " + resp.getResponseCode().toString() + " | Result : " + resp.getResult() + " | Error Message : " + resp.getParams().getErrmsg());
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Invalid Response received while publishing content for : " + identifier);
		}

	}

	private String getPreSignedUrl(String identifier, String fileName) throws Exception {
		String preSignedUrl = "";
		Map<String, Object> request = new HashMap<>();
		Map<String, Object> content = new HashMap<String, Object>();
		Map<String, String> header = new HashMap<String, String>(){{
			put("Content-Type","application/json");
		}};
		content.put("fileName", fileName);
		request.put("content", content);
		Map<String, Object> presignedReq = new HashMap<String, Object>();
		presignedReq.put("request", request);
		String url = KP_CS_BASE_URL + "/content/v3/upload/url/" + identifier;
		Response resp = UnirestUtil.post(url, presignedReq, header);
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

	private String getBasePath(String objectId) {
		return StringUtils.isNotBlank(objectId) ? TEMP_FILE_LOCATION + File.separator + System.currentTimeMillis() + "_temp" + File.separator + objectId : "";
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

}
