package org.sunbird.content.tool.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ServerException;
import org.sunbird.content.tool.CloudStoreManager;
import org.sunbird.content.tool.PlatformAPIManager;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import scala.Option;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class BaseService extends PlatformAPIManager {

    protected String destStorageType = Platform.config.getString("destination.storage_type");

    protected CloudStoreManager cloudStoreManager = new CloudStoreManager();

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

    protected Response uploadAsset(String path, String id, String src) throws Exception {
        File file = new File(path);
        String objectKey = src.replaceAll("assets/public/", "");
        String url = cloudStoreManager.getcloudService(destStorageType).upload(cloudStoreManager.getContainerName(destStorageType), file.getAbsolutePath(), objectKey, Option.apply(false), Option.apply(1), Option.apply(5), Option.empty());
        String uploadUrl = url.split("\\?")[0];
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("fileUrl", uploadUrl);
        HttpResponse<String> uploadResponse = Unirest.post(destUrl + "/content/v3/upload/" + id).queryString(parameters).header("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW").header("Authorization", destKey).asString();
        Response response = mapper.readValue(uploadResponse.getBody(), Response.class);
        return response;
    }


    /*public static void main(String[] args) throws Exception {
        BaseService service  = new BaseService();
        String path = service.downloadArtifact("do_112598819372515328131", "https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/do_112598819372515328131/artifact/1537941895508_do_112598819372515328131.zip", "aws", true);
        Map<String, Object> assets = service.readECMLFile(path + "/"  + "index.ecml");

        for(String assetId: assets.keySet()) {
            Response response = service.uploadAsset(path + "/assets/" + (String) assets.get(assetId), assetId, (String) assets.get(assetId));
        }
    }*/
}

