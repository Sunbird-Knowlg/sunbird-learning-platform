package org.ekstep.content.tool.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.exception.ServerException;
import org.json.JSONObject;
import org.sunbird.cloud.storage.BaseStorageService;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;
import org.sunbird.cloud.storage.util.CommonUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import scala.Option;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseService {

    protected ObjectMapper mapper = new ObjectMapper();
    protected String sourceEnv = Platform.config.getString("source.env");
    protected String destEnv = Platform.config.getString("destination.env");
    protected String sourceKey = Platform.config.getString("source.key");
    protected String destKey = Platform.config.getString("destination.key");

    protected String sourceUrl = Platform.config.getString(sourceEnv + ".url");
    protected String destUrl = Platform.config.getString(destEnv + ".url");
    protected String sourceVersion = Platform.config.getString(sourceEnv + ".version");
    protected String destVersion = Platform.config.getString(destEnv + ".version");
    protected String sourceStorageType = Platform.config.getString(sourceEnv + ".storage_type");
    protected String destStorageType = Platform.config.getString(destEnv + ".storage_type");

    protected BaseStorageService awsService = StorageServiceFactory.getStorageService(new StorageConfig("aws", Platform.config.getString("aws_storage_key"), Platform.config.getString("aws_storage_secret")));
    protected BaseStorageService azureService = StorageServiceFactory.getStorageService((new StorageConfig("azure", Platform.config.getString("azure_storage_key"), Platform.config.getString("azure_storage_secret"))));

    protected Response executePost(String url, String authKey, Map<String, Object> request, String channel) throws Exception {
        if (StringUtils.isBlank(channel)) {
            channel = "in.ekstep";
        }
        HttpResponse<String> httpResponse = Unirest.post(url).header("Authorization", authKey).header("Content-Type", "application/json").header("X-Channel-ID", channel).body(mapper
                .writeValueAsString(request)).asString();
        Response response = mapper.readValue(httpResponse.getBody(), Response.class);
        return response;
    }

    protected Response executePatch(String url, String authKey, Map<String, Object> request, String channel) throws Exception {
        if (StringUtils.isBlank(channel)) {
            channel = "in.ekstep";
        }
        HttpResponse<String> httpResponse = Unirest.patch(url).header("Authorization", authKey).header("Content-Type", "application/json").header("X-Channel-ID", channel).body(mapper
                .writeValueAsString(request)).asString();
        Response response = mapper.readValue(httpResponse.getBody(), Response.class);
        return response;
    }


    protected Response executeGet(String url, String authKey) throws Exception {
        HttpResponse<String> httpResponse = Unirest.get(url).header("Authorization", authKey).asString();
        Response response = mapper.readValue(httpResponse.getBody(), Response.class);
        return response;
    }

    protected Map<String, Object> getFromSource(String filter) throws Exception {
        Map<String, Object> identifiers = new HashMap<>();
        Map<String, Object> filters = mapper.readValue(filter, Map.class);
        filters.remove("status");
        Map<String, Object> searchRequest = new HashMap<>();

        searchRequest.put("filters", filters);
        searchRequest.put("fields", Arrays.asList("identifier", "pkgVersion"));

        Map<String, Object> request = new HashMap<>();
        request.put("request", searchRequest);

        Response searchResponse = executePost(sourceUrl + "/composite/" + sourceVersion + "/search", sourceKey, request, null);
        if (StringUtils.equals(ResponseParams.StatusType.successful.name(), searchResponse.getParams().getStatus())) {
            int count = (int) searchResponse.getResult().get("count");
            getIdsFromResponse(searchResponse.getResult(), count, identifiers, 0, request);
        }

        return identifiers;
    }

    private void getIdsFromResponse(Map<String, Object> result, int count, Map<String, Object> identifiers, int offset, Map<String, Object> request) throws Exception {
        if ((count - 100) >= 0) {
            for (Map<String, Object> res : (List<Map<String, Object>>) result.get("content")) {
                identifiers.put((String) res.get("identifier"), res.get("pkgVersion"));
            }
            count -= 100;
            offset += 100;

            ((Map<String, Object>) request.get("request")).put("offset", offset);

            Response searchResponse = executePost(sourceUrl + "/composite/" + sourceVersion + "/search", sourceKey, request, null);
            if (isSuccess(searchResponse)) {
                getIdsFromResponse(searchResponse.getResult(), count, identifiers, 0, request);
            } else {
                throw new ServerException("ERR_SYNC_SERVICE", "Error while fetching identifiers", searchResponse.getParams().getErr());
            }

        } else {
            for (Map<String, Object> res : (List<Map<String, Object>>) result.get("content")) {
                identifiers.put((String) res.get("identifier"), res.get("pkgVersion"));
            }
        }
    }

    protected boolean validChannel(String channel) throws Exception {
        Response readResponse = executeGet(destUrl + "/channel/" + destVersion + "/read/" + channel, destKey);
        return isSuccess(readResponse);
    }

    protected boolean isSuccess(Response response) {
        return StringUtils.equals(ResponseParams.StatusType.successful.name(), response.getParams().getStatus());
    }

    protected void uploadArtifact(String id) {


    }

    protected void downloadArtifact(String id, String downloadUrl, String cloudStoreType) {
        String localPath = "/tmp/" + id;
        String[] fileUrl = downloadUrl.split("/");
        String filename = fileUrl[fileUrl.length -1];
        getcloudService(cloudStoreType).download(getContainerName(cloudStoreType), downloadUrl, localPath, Option.apply(false));
        CommonUtil.unZip(localPath + "/" + filename, localPath);
    }


    private static String getContainerName(String cloudStoreType) {
        if(StringUtils.equalsIgnoreCase(cloudStoreType, "azure")) {
            return Platform.config.getString("azure_storage_container");
        }else if(StringUtils.equalsIgnoreCase(cloudStoreType, "aws")) {
            return Platform.config.getString("aws_storage_container");
        }else {
            throw new ServerException("ERR_INVALID_CLOUD_STORAGE", "Error while getting container name");
        }
    }

    private BaseStorageService getcloudService(String cloudStoreType){
        if(StringUtils.equalsIgnoreCase(cloudStoreType, "azure")) {
            return azureService;
        }else if(StringUtils.equalsIgnoreCase(cloudStoreType, "aws")) {
            return awsService;
        }else {
            throw new ServerException("ERR_INVALID_CLOUD_STORAGE", "Error while getting container name");
        }
    }


    public Map<String, List<Object>> readECMLFile(String filePath) {
        final Map<String, List<Object>> mediaIdMap = new HashMap<String, List<Object>>();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            DefaultHandler handler = new DefaultHandler() {
                public void startElement(String uri, String localName, String qName, Attributes attributes)
                        throws SAXException {
                    if (qName.equalsIgnoreCase("media")) {
                        String id = attributes.getValue("id");
                        if (StringUtils.isNotBlank(id)) {
                            String src = attributes.getValue("src");
                            if (StringUtils.isNotBlank(src)) {
                                String assetId = attributes.getValue("assetId");
                                List<Object> mediaValues = new ArrayList<Object>();
                                mediaValues.add(src);
                                mediaValues.add(assetId);
                                mediaIdMap.put(id, mediaValues);
                            }
                        }
                    }
                }

                public void endElement(String uri, String localName, String qName) throws SAXException {
                    // System.out.println("End Element :" + qName);
                }
            };
            saxParser.parse(filePath, handler);
        } catch (Exception e) {
            throw new ServerException("ERR_CONTENT_EXTRACT", "Error while extracting the zipFile");
        }
        return mediaIdMap;
    }


    public static void main(String[] args) {
        BaseService service  = new BaseService();
        service.downloadArtifact("do_21259266313247948813016", "https://ekstep-public-qa.s3-ap-south-1.amazonaws.com/content/do_21259266313247948813016/artifact/1537190425252_do_21259266313247948813016.zip", "aws");
    }
}

