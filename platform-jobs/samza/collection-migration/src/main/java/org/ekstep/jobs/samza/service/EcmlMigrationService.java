/**
 * author: Rhea Shalom Ann Fernandes
 * created on : April 23rd 2019
 */
package org.ekstep.jobs.samza.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.mgr.ConvertToGraphNode;
import org.ekstep.common.router.RequestRouterPool;
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.content.mimetype.mgr.impl.AssetsMimeTypeMgrImpl;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.processor.SearchProcessor;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import scala.concurrent.Await;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EcmlMigrationService {
    public final String DOMAIN = "domain";
    public final String VERSION = "version";
    public final int VERSION_NUM = 2;
    public final String ECML_MIGRATION_FAILED = "ECML_MIGRATION_FAILED";
    public final String MIMETYPE = "mimeType";
    public SearchProcessor processor = new SearchProcessor();

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
        if (StringUtils.isNotBlank(contentBody))
            return (List<Map<String, Object>>) ((Map<String, Object>)
                    ((Map<String, Object>) contentBodyMap.get("theme")).get("manifest")).get("media");
        else
            return new ArrayList<>();
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

    public Map<String, List> downloadDriveContents(String driveUrl, String dirUrl) throws Exception{
        Map<String, List> fileMap = new HashMap<>();
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
                        throw new ClientException(ECML_MIGRATION_FAILED, "Google Drive content should be downloadable");
                    }
                } else {
                    throw new ClientException(ECML_MIGRATION_FAILED, "Google Drive content cannot be private");
                }
            } else {
                throw new ClientException(ECML_MIGRATION_FAILED, "404 Resource Not Found, Make sure resource is available");
            }
        return fileMap;
    }

    public String doesAssetExists(String driveUrl) {
        try {

            Map<String, Object> properties = new HashMap<>();
            properties.put("objectType","Content");
            properties.put("contentType","Asset");
            properties.put("migratedUrl", driveUrl);
            properties.put("status", new ArrayList<>());
            SearchDTO searchDto = new SearchDTO();
            searchDto.setFuzzySearch(false);
            searchDto.setProperties(setSearchProperties(properties));
            searchDto.setOperation("AND");
            searchDto.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
            searchDto.setFields(getFields());
            List<Object> searchResult = Await.result(
                    processor.processSearchQuery(searchDto, false, CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, false),
                    RequestRouterPool.WAIT_TIMEOUT.duration());
            if (searchResult.size() > 0)
                return (String) ((Map) searchResult.get(0)).get("artifactUrl");
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private List<String> getFields() {
        List<String> fields = new ArrayList<String>();
        fields.add("identifier");
        fields.add("migratedUrl");
        fields.add("artifactUrl");
        fields.add("status");
        return fields;
    }

    public List<Map> setSearchProperties(Map<String, Object> properties) {
        List<Map> props = new ArrayList<>();
        properties.keySet().forEach(property -> {
            Map<String, Object> prop = new HashMap<>();
            prop.put("operation", "EQ");
            prop.put("propertyName", property);
            prop.put("values", properties.get(property));
            props.add(prop);
        });
        return props;
    }

    public String createAsset(Map<String, List> fileMap, String driveUrl) {
        String contentId = "";
        try {
            Node node = ConvertToGraphNode.convertToGraphNode(prepData(fileMap, driveUrl), util.getDefinition("domain", "Content"), null);
            node.setObjectType("Content");
            node.setGraphId("domain");
            Response response = util.createDataNode(node);
            if (response.getResponseCode() == ResponseCode.OK) {
                contentId = (String) response.getResult().get("node_id");
                assetIdList.add(contentId);
            } else
                throw new ClientException(ECML_MIGRATION_FAILED, "Asset Creation Failed");
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage(), e);
        }
        return contentId;
    }


    private Map<String, Object> prepData(Map<String, List> fileMap, String driveUrl) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", ((File) fileMap.get(driveUrl).get(0)).getName());
        map.put("code", ((File) fileMap.get(driveUrl).get(0)).getName());
        map.put("mimeType", fileMap.get(driveUrl).get(1));
        map.put("contentType", "Asset");
        map.put("mediaType", ((String) fileMap.get(driveUrl).get(1)).split("/")[0]);
        map.put("migratedUrl", driveUrl);
        return map;
    }

    public String uploadAsset(File media, String contentId) {
        try {
            if (StringUtils.isBlank(contentId))
                throw new ClientException(ECML_MIGRATION_FAILED, "ContentId of created Asset can't be blank");
            Response response = new AssetsMimeTypeMgrImpl().upload(contentId, util.getNode(DOMAIN, contentId), media, true);
            if (response.getResponseCode() == ResponseCode.OK) {
                return (String) response.getResult().get("content_url");
            } else
                throw new ClientException(ECML_MIGRATION_FAILED, "Upload content has failed");
        }catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public void deleteDownloadedContent(String fileUrl) {
        File index = new File(fileUrl);
        String[] entries = index.list();
        for (String s : entries) {
            File currentFile = new File(index.getPath(), s);
            currentFile.delete();
        }
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

    public void ecmlOldBodyUpdate(String contentBody, String contentId) throws Exception {
        if (StringUtils.isNotBlank(contentBody)) {
            Response response = util.updateContentOldBody(contentId, contentBody);
            if (response.getResponseCode() != ResponseCode.OK)
                throw new ClientException(ECML_MIGRATION_FAILED, "Cassandra Old Body update failed");
        } else
            throw new ClientException(ECML_MIGRATION_FAILED, "ECML Old Body update was not possible");
    }

    public void updateEcmlNode(List<Node> nodes) throws Exception {
        Map<String, Object> newMetaData = new HashMap<>();
        newMetaData.put(VERSION, VERSION_NUM);
        Response response = util.updateNodes(nodes, newMetaData);
        if (response.getResponseCode() != ResponseCode.OK)
            throw new ClientException(ECML_MIGRATION_FAILED, "ECML nodes were not updated");
    }
}
