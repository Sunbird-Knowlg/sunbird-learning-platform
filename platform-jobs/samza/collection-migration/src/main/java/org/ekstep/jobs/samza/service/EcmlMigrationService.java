/**
 * author: Rhea Shalom Ann Fernandes
 * created on : April 23rd 2019
 */
package org.ekstep.jobs.samza.service;

import com.mashape.unirest.http.Unirest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import com.mashape.unirest.http.HttpResponse;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.common.util.HttpRestUtil;
import org.ekstep.content.mimetype.mgr.impl.AssetsMimeTypeMgrImpl;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.learning.util.ControllerUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EcmlMigrationService {
    public final String CHANNEL_ID = Platform.config.hasPath("channel.default") ?
            Platform.config.getString("channel.default") : "in.ekstep";
    public final String CONTENT_TYPE = "application/json";
    public final String DOMAIN = "domain";
    public final String VERSION = "version";
    public final int VERSION_NUM = 2;
    public final String ECML_MIGRATION_FAILED = "ECML_MIGRATION_FAILED";
    public final String MIMETYPE = "mimeType";

    private final ControllerUtil util = new ControllerUtil();

    public List<String> assetIdList = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private JobLogger LOGGER = new JobLogger(EcmlMigrationService.class);

    public Boolean checkIfValidMigrationRequest(Node node) {
        Number version = (Number) node.getMetadata().get(VERSION);
        String mimeType = (String) node.getMetadata().get(MIMETYPE);
        if (mimeType != null && StringUtils.equalsIgnoreCase(mimeType, "application/vnd.ekstep.ecml-archive") && version != null && version.intValue() >= 2) {
            LOGGER.info("Migration is already completed for Content ID: " + node.getIdentifier() + ". So, skipping this message.");
            return false;
        } else
            return true;
    }

    public List<Map<String, Object>> getMediaContents(String contentBody) throws Exception {
        Map<String, Object> contentBodyMap = mapper.readValue(contentBody, new TypeReference<Map<String, Object>>() {
        });
        return (List<Map<String, Object>>) ((Map<String, Object>)
                ((Map<String, Object>) contentBodyMap.get("theme")).get("manifest")).get("media");
    }


    public List<Map<String, Object>> getMediasWithDriveUrl(List<Map<String, Object>> mediaContents) {
        List<Map<String, Object>> mediasDriveUrl = new ArrayList<>();
        mediaContents.forEach(media -> {
            if (media.get("src") != null && StringUtils.contains((String) media.get("src"), "drive.google.com")
                    || StringUtils.contains((String) media.get("src"), "goo.gl")) {
                mediasDriveUrl.add(media);
            }
        });

        return mediasDriveUrl;
    }

    public Map<String, List> downloadDriveContents(String driveUrl, String dirUrl) {
        Map<String, List> fileMap = new HashMap<>();
        try {
            HttpResponse response = makeGetRequest(driveUrl);
            if (response.getStatus() == 200) {
                String contentType = response.getHeaders().get("Content-Type").get(0);
                if (!StringUtils.equalsIgnoreCase("text/html; charset=UTF-8", contentType)) {
                    File media = HttpDownloadUtility.downloadFile(driveUrl, dirUrl);
                    if (media != null) {
                        List fileAndMimeType = new ArrayList();
                        fileAndMimeType.add(media);
                        fileAndMimeType.add(contentType);
                        fileMap.put(driveUrl, fileAndMimeType);
                    } else {
                        throw new ClientException(ECML_MIGRATION_FAILED, "Google Drive should be downloadable");
                    }
                } else {
                    throw new ClientException(ECML_MIGRATION_FAILED, "Google Drive content cannot be private");
                }
            } else {
                throw new ClientException(ECML_MIGRATION_FAILED, "404 Resource Not Found, Make sure resource is available");
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage(),e);
        }
        return fileMap;
    }

    public String createAsset(String channelId, String lpUrl, File media, String contentType) throws Exception {
        if (StringUtils.isBlank(channelId))
            channelId = CHANNEL_ID;
        String contentId;
        Map<String, String> headerParam = new HashMap<String, String>();
        Map<String, Object> requestMap = new HashMap<String, Object>();
        Map<String, Object> contentMap = new HashMap<String, Object>();
        Map<String, Object> data;
        headerParam.put("Content-Type", CONTENT_TYPE);
        headerParam.put("X-Channel-Id", channelId);
        data = prepData(media, contentType);
        contentMap.put("content", data);
        requestMap.put("request", contentMap);
        Response response = HttpRestUtil.makePostRequest(lpUrl + "/content/v3/create", requestMap, headerParam);
        if (response.getResponseCode() == ResponseCode.OK) {
            contentId = (String) response.getResult().get("node_id");
            assetIdList.add(contentId);
        } else
            throw new ClientException(ECML_MIGRATION_FAILED, "Asset Creation Failed");
        return contentId;
    }

    private Map<String, Object> prepData(File media, String mimeType) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("name", media.getName());
        map.put("code", media.getName());
        map.put("mimeType", mimeType);
        map.put("contentType", "Asset");
        map.put("mediaType", mimeType.split("/")[0]);
        return map;
    }

    public String uploadAsset(File media, String contentId) {
        Response response = new AssetsMimeTypeMgrImpl().upload(contentId, util.getNode(DOMAIN, contentId), media, true);
        if (response.getResponseCode() == ResponseCode.OK) {
            return (String) response.getResult().get("content_url");
        }
        return null;
    }

    public Boolean deleteDownloadedContent(String fileUrl) {
        File file = new File(fileUrl);
        try {
            if (file.listFiles() != null) {
                FileUtils.cleanDirectory(file);
                LOGGER.info("Successfully deleted all temp media");
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("Deleting media unsuccesful", e);
            return false;
        }
        return false;
    }

    public HttpResponse makeGetRequest(String url) throws Exception {
        return Unirest.get(url).asString();
    }

    public void validateNodes() {
        assetIdList.forEach(assetId -> {
            try {
                if (!StringUtils.equalsIgnoreCase((String) util.getNode(DOMAIN, assetId).getMetadata().get("status"), "LIVE")) {
                    throw new ClientException(ECML_MIGRATION_FAILED, "All asset are not live");
                }
            } catch (Exception e) {
                LOGGER.info(e.getMessage() + ", Migration failed.");
            }
        });
    }

    public void ecmlBodyUpdate(String contentBody, String contentId, Map<String, String> driveArtifactMap) throws Exception {
        for (String driveUrl : driveArtifactMap.keySet()) {
            if (contentBody.contains(driveUrl)) {
                String driveRegexUrl = driveUrl.replace("?", "\\?");
                contentBody = contentBody.replaceAll(driveRegexUrl, driveArtifactMap.get(driveUrl));
            }
        }
        System.out.println("Post Update of Content Body, Body is :" + contentBody);
        if (StringUtils.isNotBlank(contentBody)) {
            Response response = util.updateContentBody(contentId, contentBody);
            if (response.getResponseCode() != ResponseCode.OK)
                throw new ClientException(ECML_MIGRATION_FAILED, "Cassandra Body update failed");
        } else
            throw new ClientException(ECML_MIGRATION_FAILED, "ECML Body update was not possible");
    }

    public void updateEcmlNode(List<Node> nodes) throws Exception {
        Map<String, Object> newMetaData = new HashMap<>();
        newMetaData.put(VERSION, VERSION_NUM);
        Response response = util.updateNodes(nodes, newMetaData);
        if (response.getResponseCode() != ResponseCode.OK)
            throw new ClientException(ECML_MIGRATION_FAILED, "ECML nodes were not updated");
    }
}
