package org.ekstep.content.tool.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.exception.ServerException;
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
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseService {

    protected ObjectMapper mapper = new ObjectMapper();
    protected String sourceKey = Platform.config.getString("source.key");
    protected String destKey = Platform.config.getString("destination.key");

    protected String sourceUrl = Platform.config.getString("source.url");
    protected String destUrl = Platform.config.getString("destination.url");
    protected String sourceStorageType = Platform.config.getString("source.storage_type");
    protected String destStorageType = Platform.config.getString("destination.storage_type");

    protected static Map<String, String> extractMimeType = new HashMap<>();

    protected BaseStorageService awsService = StorageServiceFactory.getStorageService(new StorageConfig("aws", Platform.config.getString("aws_storage_key"), Platform.config.getString("aws_storage_secret")));
    protected BaseStorageService azureService = StorageServiceFactory.getStorageService((new StorageConfig("azure", Platform.config.getString("azure_storage_key"), Platform.config.getString("azure_storage_secret"))));

    static{
        extractMimeType.put("application/vnd.ekstep.h5p-archive", "h5p");
        extractMimeType.put("application/vnd.ekstep.ecml-archive", "ecml");
        extractMimeType.put("application/vnd.ekstep.html-archive", "html");
    }

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

    protected Map<String, Map<String, Object>> getFromSource(String filter) throws Exception {
        Map<String, Map<String, Object>> identifiers = new HashMap<>();
        Map<String, Object> filters = mapper.readValue(filter, Map.class);
        filters.remove("status");
        Map<String, Object> searchRequest = new HashMap<>();

        searchRequest.put("filters", filters);
        searchRequest.put("fields", Arrays.asList("identifier", "name", "pkgVersion"));

        Map<String, Object> request = new HashMap<>();
        request.put("request", searchRequest);

        Response searchResponse = executePost(sourceUrl + "/composite/v3/search", sourceKey, request, null);
        if (StringUtils.equals(ResponseParams.StatusType.successful.name(), searchResponse.getParams().getStatus())) {
            int count = (int) searchResponse.getResult().get("count");
            getIdsFromResponse(searchResponse.getResult(), count, identifiers, 0, request);
        }

        return identifiers;
    }

    private void getIdsFromResponse(Map<String, Object> result, int count, Map<String, Map<String, Object>> identifiers, int offset, Map<String, Object> request) throws Exception {
        if ((count - 100) >= 0) {
            for (Map<String, Object> res : (List<Map<String, Object>>) result.get("content")) {
                identifiers.put((String) res.get("identifier"), res);
            }
            count -= 100;
            offset += 100;

            ((Map<String, Object>) request.get("request")).put("offset", offset);

            Response searchResponse = executePost(sourceUrl + "/composite/v3/search", sourceKey, request, null);
            if (isSuccess(searchResponse)) {
                getIdsFromResponse(searchResponse.getResult(), count, identifiers, 0, request);
            } else {
                throw new ServerException("ERR_SYNC_SERVICE", "Error while fetching identifiers", searchResponse.getParams().getErr());
            }

        } else {
            for (Map<String, Object> res : (List<Map<String, Object>>) result.get("content")) {
                identifiers.put((String) res.get("identifier"), res);
            }
        }
    }

    protected boolean validChannel(String channel) throws Exception {
        Response readResponse = executeGet(destUrl + "/channel/v3/read/" + channel, destKey);
        return isSuccess(readResponse);
    }

    protected boolean isSuccess(Response response) {
        return StringUtils.equals(ResponseParams.StatusType.successful.name(), response.getParams().getStatus());
    }

    protected String uploadArtifact(String id, String path, String cloudStoreType) {
        String folder = "content" + File.separator + id + File.separator + "artifact";
        File file = new File(path);
        String objectKey = folder + "/" + file.getName();
        System.out.println("Uploading Artifact path : " + file.getAbsolutePath());
        String url = getcloudService(cloudStoreType).upload(getContainerName(cloudStoreType), file.getAbsolutePath(), objectKey, Option.apply(false), Option.apply(false), Option.empty(), Option.empty());
        return url;

    }

    protected String downloadArtifact(String id, String artifactUrl, String cloudStoreType, boolean extractFile) throws Exception {
        String folder = "content" + File.separator + id + File.separator + "artifact";
        if(StringUtils.isNotBlank(artifactUrl)){
            String localPath = "tmp/" + id + File.separator;
            String[] fileUrl = artifactUrl.split("/");
            String filename = fileUrl[fileUrl.length - 1];
            String objectKey = folder + "/" + filename;
            File file = new File(localPath + filename);
            FileUtils.copyURLToFile(new URL(artifactUrl), file);
            //getcloudService(cloudStoreType).download(getContainerName(cloudStoreType), objectKey, localPath, Option.apply(false));

            if(extractFile){
                CommonUtil.unZip(localPath + "/" + filename, localPath);
                return localPath;
            }else{
                return file.getAbsolutePath();
            }
        }
        return null;

    }

    protected String downloadEcar(String id, String downloadUrl, String cloudStoreType) throws Exception {
        String localPath = "tmp/" + id + File.separator;
        String[] fileUrl = downloadUrl.split("/");
        String filename = fileUrl[fileUrl.length - 1];
        String objectKey = "ecar-files" + File.separator + id + File.separator + filename;
        File file = new File(localPath + filename);
        FileUtils.copyURLToFile(new URL(downloadUrl), file);
        return file.getAbsolutePath();
    }

    protected String uploadEcar(String id, String cloudStoreType, String path) {
        String folder = "ecar-files/" + id;
        File file = new File(path);
        String objectKey = folder + "/" + file.getName();
        String url = getcloudService(cloudStoreType).upload(getContainerName(cloudStoreType), file.getAbsolutePath(), objectKey, Option.apply(false), Option.apply(false), Option.empty(), Option.empty());
        return url;
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


    public Map<String, Object> readECMLFile(String filePath) {
        final Map<String, Object> mediaIdMap = new HashMap<>();
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
                            if (StringUtils.isNotBlank(src) && StringUtils.equalsIgnoreCase("image", attributes.getValue("type"))) {
                                String assetId = attributes.getValue("assetId");
                                mediaIdMap.put(id, src);
                            }
                        }
                    }
                }

                public void endElement(String uri, String localName, String qName) throws SAXException {
                }
            };
            saxParser.parse(filePath, handler);
        } catch (Exception e) {
            throw new ServerException("ERR_CONTENT_EXTRACT", "Error while extracting the zipFile");
        }
        return mediaIdMap;
    }

    protected Response getContent(String id, boolean isDestination, String fields) throws Exception {
        if(isDestination) {
            String url = destUrl + "/content/v3/read/" + id;
            if(StringUtils.isNotBlank(fields))
                url += "?fields=" + fields;
            return executeGet(url, destKey);
        }
        else{
            String url = sourceUrl + "/content/v3/read/" + id;
            if(StringUtils.isNotBlank(fields))
                url += "?fields=" + fields;
            return executeGet(url, sourceKey);
        }
    }


    protected Response systemUpdate(String id, Map<String, Object> request, String channel, boolean isDestination) throws Exception {
        if(isDestination)
            return executePatch(destUrl + "/system/v3/content/update/" + id, destKey, request, channel);
        else
            return executePatch(sourceUrl + "/system/v3/content/update/" + id, sourceKey, request, channel);
    }

    protected Response uploadAsset(String path, String id) throws Exception {
        File file = new File(path);
        String objectKey = "assets" + File.separator + id + File.separator + file.getName();
        String signedUrl = getcloudService(destStorageType).getSignedURL(getContainerName(destStorageType), objectKey, Option.apply(null), Option.apply("w"));
        HttpResponse<String> httpResponse = Unirest.put(signedUrl).header("x-ms-blob-type", "BlockBlob").field("file", file).asString();
        if(httpResponse.getStatus() == 200 || httpResponse.getStatus() == 201) {
            String uploadUrl = signedUrl.split("\\?")[0];
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("fileUrl", uploadUrl);
            HttpResponse<String> uploadResponse = Unirest.post(destUrl + "/content/v3/upload/" + id).queryString(parameters).header("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW").header("Authorization", destKey).asString();
            Response response = mapper.readValue(uploadResponse.getBody(), Response.class);
            return response;
        }else{
            throw new ServerException("ERR_ASSET_UPLOAD", "Failed while uploading Asset: " + id);
        }



    }


    protected void extractArchives(String id, String mimetype, String artefactUrl, double pkgVersion) {
        String[] fileUrl = artefactUrl.split("/");
        String filename = fileUrl[fileUrl.length - 1];
        String objectkey = "content" + File.separator + id + File.separator + "artifact" + filename;
        String tokey = "content" + File.separator + extractMimeType.get(mimetype) + File.separator + id;
        getcloudService(destStorageType).extractArchive(getContainerName(destStorageType), objectkey, tokey + "-snapshot");
        getcloudService(destStorageType).extractArchive(getContainerName(destStorageType), objectkey, tokey+ "-latest");
        getcloudService(destStorageType).extractArchive(getContainerName(destStorageType), objectkey, tokey+ "-" + pkgVersion);
    }


   /* public static void main(String[] args) throws Exception {
        BaseService service  = new BaseService();
        //service.downloadArtifact("do_21259266313247948813016", "https://ekstep-public-qa.s3-ap-south-1.amazonaws.com/content/do_21259266313247948813016/artifact/1537190425252_do_21259266313247948813016.zip", "aws");
        Map<String, Object> assets = service.readECMLFile("/Users/pradyumna/Downloads/1537190425252_do_21259266313247948813016/index.ecml");

        for(String assetId: assets.keySet()) {
            Response response = service.uploadAsset("/Users/pradyumna/Downloads/1537190425252_do_21259266313247948813016/assets/" + (String) assets.get(assetId), assetId);
        }
    }*/
}

