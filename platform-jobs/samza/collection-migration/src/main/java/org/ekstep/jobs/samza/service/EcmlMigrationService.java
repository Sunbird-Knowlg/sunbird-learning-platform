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
    public final String REGEXGDRIVE = "\\w+://drive.google.com/\\w+\\?export=download&id=";

    private final ControllerUtil util = new ControllerUtil();

    public static List<String> assetIdList = new ArrayList<>();
    public static Map<String, String> driveArtifactMap = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private JobLogger LOGGER = new JobLogger(EcmlMigrationService.class);

    public Boolean checkIfValidMigrationRequest(Node node) {
        Number version = (Number) node.getMetadata().get(VERSION);
        if (version != null && version.intValue() >= 2) {
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


    public Boolean downloadCreateandUpload(String contentUrl, String dirUrl, String lpUrl, String channelId) throws Exception {
        HttpResponse response = makeGetRequest(contentUrl);
        if (response.getStatus() == 200) {
            String contentType = response.getHeaders().get("Content-Type").get(0);
            if (!StringUtils.equalsIgnoreCase("text/html; charset=UTF-8", contentType)) {
                File media = HttpDownloadUtility.downloadFile(contentUrl, dirUrl);
                if (media != null) {
                    String contentId = createAsset(channelId, lpUrl, media, contentType);
                    if (uploadAsset(media, contentId, contentUrl))
                        return true;
                }
            } else {
                throw new Exception("Google Drive content cannot be private");
            }
        } else {
            throw new Exception("404 Resource Not Found, Make sure resource is available");
        }
        return false;
    }

    public String createAsset(String channelId, String lpUrl, File media, String contentType) throws Exception {
        if (StringUtils.isBlank(channelId))
            channelId = CHANNEL_ID;
        String contentId = null;
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
            throw new Exception("Asset Creation Failed");
        return contentId;
    }

    private Map<String, Object> prepData(File media, String contentType) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", media.getName());
        map.put("code", media.getName());
        map.put("mimeType", contentType);
        map.put("contentType", "Asset");
        return map;
    }

    public Boolean uploadAsset(File media, String contentId, String driveUrl) {
        Response response = new AssetsMimeTypeMgrImpl().upload(contentId, util.getNode(DOMAIN, contentId), media, true);
        if (response.getResponseCode() == ResponseCode.OK) {
            driveArtifactMap.put(driveUrl, (String) response.getResult().get("content_url"));
            return true;
        }
        return false;
    }

    public Boolean deleteDownloadedContent(String fileUrl) {
        File file = new File(fileUrl);
        try {
            if (file.listFiles() != null) {
                FileUtils.cleanDirectory(file);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                    throw new Exception("All asset are not live");
                }
            } catch (Exception e) {
                LOGGER.info(e.getMessage() + ", Migration failed.");
            }
        });
    }

    public void ecmlBodyUpdate(String contentBody, String contentId) throws Exception {
        for (String driveUrl : driveArtifactMap.keySet()) {
            if (contentBody.contains(driveUrl)) {
                String id = driveUrl.split("id=")[1];
                contentBody = contentBody.replaceAll(REGEXGDRIVE + id, driveArtifactMap.get(driveUrl));
            }
        }
        System.out.println("Post Update of Content Body, Body is :" + contentBody);
        if (StringUtils.isNotBlank(contentBody)) {
            Response response = util.updateContentBody(contentId, contentBody);
            if (response.getResponseCode() != ResponseCode.OK)
                throw new Exception("ECML Body update was not possible");
        } else
            throw new Exception("ECML Body update was not possible");
    }

    public void updateEcmlNode(String contentId) throws Exception {
        Node node = util.getNode(DOMAIN, contentId);
        Map existingMetaData = node.getMetadata();
        existingMetaData.put(VERSION, VERSION_NUM);
        Response response = util.updateNode(node);
        if (response.getResponseCode() != ResponseCode.OK)
            throw new Exception("ECML node was not updated");
    }

}
