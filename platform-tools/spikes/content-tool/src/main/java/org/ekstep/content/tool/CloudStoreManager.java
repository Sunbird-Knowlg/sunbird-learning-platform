package org.ekstep.content.tool;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ServerException;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.sunbird.cloud.storage.BaseStorageService;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;
import org.sunbird.cloud.storage.util.CommonUtil;
import scala.Option;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CloudStoreManager {

    protected String destStorageType = Platform.config.getString("destination.storage_type");

    protected BaseStorageService awsService = StorageServiceFactory.getStorageService(new StorageConfig("aws", Platform.config.getString("aws_storage_key"), Platform.config.getString("aws_storage_secret")));
    protected BaseStorageService azureService = StorageServiceFactory.getStorageService((new StorageConfig("azure", Platform.config.getString("azure_storage_key"), Platform.config.getString("azure_storage_secret"))));

    protected static Map<String, String> extractMimeType = new HashMap<>();

    static {
        extractMimeType.put("application/vnd.ekstep.h5p-archive", "h5p");
        extractMimeType.put("application/vnd.ekstep.ecml-archive", "ecml");
        extractMimeType.put("application/vnd.ekstep.html-archive", "html");
    }

    public Map<String, Object> copyEcar(Map<String, Object> metadata) throws Exception {
        String id = (String) metadata.get("identifier");
        String mimeType = (String) metadata.get("mimeType");
        try {
            String downloadUrl = (String) metadata.get("downloadUrl");
            String path = downloadEcar(id, downloadUrl);
            String destDownloadUrl = uploadEcar(id, destStorageType, path);

            if (StringUtils.isNotBlank(destDownloadUrl)) {
                metadata.put("downloadUrl", destDownloadUrl);
            }

            Map<String, Object> variants = (Map<String, Object>) metadata.get("variants");
            if (CollectionUtils.isNotEmpty(variants.keySet())) {
                String spineEcarUrl = (String) ((Map<String, Object>) variants.get("spine")).get("ecarUrl");
                if (StringUtils.isNotBlank(spineEcarUrl)) {
                    String spinePath = downloadEcar(id, spineEcarUrl);
                    String destSpineEcar = uploadEcar(id, destStorageType, spinePath);
                    ((Map<String, Object>) variants.get("spine")).put("ecarUrl", destSpineEcar);
                    metadata.put("variants", variants);
                }
            }

            String appIconUrl = (String) metadata.get("appIcon");
            if(StringUtils.isNotBlank(appIconUrl)){
                String appIconPath = downloadArtifact(id, appIconUrl, false);
                String destAppIconUrl = uploadArtifact(id, appIconPath, destStorageType);
                if (StringUtils.isNotBlank(destAppIconUrl)) {
                    metadata.put("appIcon", destAppIconUrl);
                }
            }

            String posterImageUrl = (String) metadata.get("posterImage");
            if(StringUtils.isNotBlank(posterImageUrl)){
                String posterImagePath = downloadArtifact(id, posterImageUrl, false);
                String destPosterImageUrl = uploadArtifact(id, posterImagePath, destStorageType);
                if (StringUtils.isNotBlank(destPosterImageUrl)) {
                    metadata.put("posterImage", destPosterImageUrl);
                }
            }

            String tocUrl = (String) metadata.get("toc_url");
            if(StringUtils.isNotBlank(tocUrl)){
                String tocUrlPath = downloadArtifact(id, tocUrl, false);
                String destTocUrlUrl = uploadArtifact(id, tocUrlPath, destStorageType);
                if (StringUtils.isNotBlank(destTocUrlUrl)) {
                    metadata.put("toc_url", destTocUrlUrl);
                }
            }

            if (!StringUtils.equals(mimeType, "video/x-youtube")) {
                String artefactUrl = (String) metadata.get("artifactUrl");
                if(StringUtils.isNotBlank(artefactUrl)){
                    String artefactPath = downloadArtifact(id, artefactUrl, false);
                    String destArtefactUrl = uploadArtifact(id, artefactPath, destStorageType);
                    if (StringUtils.isNotBlank(destArtefactUrl)) {
                        metadata.put("artifactUrl", destArtefactUrl);
                    }

                    if(extractMimeType.keySet().contains(metadata.get("mimeType"))){
                        extractArchives(id, (String) metadata.get("mimeType"), artefactUrl, ((Number) metadata.get("pkgVersion")).doubleValue());
                    }
                }
            }

            Map<String, Object> content = new HashMap<>();
            content.put("content", metadata);
            Map<String, Object> request = new HashMap<>();
            request.put("request", content);
            return request;
        } finally {
            FileUtils.deleteDirectory(new File("tmp/" + id));
        }
    }

    public void extractArchives(String id, String mimetype, String artefactUrl, double pkgVersion) {
        String[] fileUrl = artefactUrl.split("/");
        String filename = fileUrl[fileUrl.length - 1];
        String objectkey = "content" + File.separator + id + File.separator + "artifact" + File.separator + filename;
        String tokey = "content" + File.separator + extractMimeType.get(mimetype) + File.separator + id;
        getcloudService(destStorageType).extractArchive(getContainerName(destStorageType), objectkey, tokey + "-snapshot" + File.separator);
        getcloudService(destStorageType).extractArchive(getContainerName(destStorageType), objectkey, tokey+ "-latest" + File.separator);
        getcloudService(destStorageType).extractArchive(getContainerName(destStorageType), objectkey, tokey+ "-" + pkgVersion + File.separator);
    }

    public String uploadArtifact(String id, String path, String cloudStoreType) {
        String folder = "content" + File.separator + id + File.separator + "artifact";
        File file = new File(path);
        String objectKey = folder + "/" + file.getName();
        TelemetryManager.info("Uploading Artifact path : " + file.getAbsolutePath());
        String url = getcloudService(cloudStoreType).upload(getContainerName(cloudStoreType), file.getAbsolutePath(), objectKey, Option.apply(false), Option.apply(false), Option.empty(), Option.empty());
        return url;

    }

    public String downloadArtifact(String id, String artifactUrl, boolean extractFile) throws Exception {
        if(StringUtils.isNotBlank(artifactUrl)){
            String localPath = "tmp/" + id + File.separator;
            String[] fileUrl = artifactUrl.split("/");
            String filename = fileUrl[fileUrl.length - 1];
            File file = new File(localPath + filename);
            FileUtils.copyURLToFile(new URL(artifactUrl), file);

            if(extractFile){
                CommonUtil.unZip(localPath + "/" + filename, localPath);
                return localPath;
            }else{
                return file.getAbsolutePath();
            }
        }
        return null;

    }

    public String downloadEcar(String id, String downloadUrl) throws Exception {
        String localPath = "tmp/" + id + File.separator;
        String[] fileUrl = downloadUrl.split("/");
        String filename = fileUrl[fileUrl.length - 1];
        File file = new File(localPath + filename);
        FileUtils.copyURLToFile(new URL(downloadUrl), file);
        return file.getAbsolutePath();
    }

    public String uploadEcar(String id, String cloudStoreType, String path) {
        String folder = "ecar-files/" + id;
        File file = new File(path);
        String objectKey = folder + "/" + file.getName();
        String url = getcloudService(cloudStoreType).upload(getContainerName(cloudStoreType), file.getAbsolutePath(), objectKey, Option.apply(false), Option.apply(false), Option.empty(), Option.empty());
        return url;
    }

    public void uploadAndExtract(String id, String artefactUrl, String mimeType, double pkgVersion) throws Exception {
        String artefactPath = downloadArtifact(id, artefactUrl, false);
        String destArtefactUrl = uploadArtifact(id, artefactPath, destStorageType);
        TelemetryManager.info("Content destination URL: "+ destArtefactUrl);
        extractArchives(id, mimeType, destArtefactUrl, pkgVersion);
    }


    public String getContainerName(String cloudStoreType) {
        if(StringUtils.equalsIgnoreCase(cloudStoreType, "azure")) {
            return Platform.config.getString("azure_storage_container");
        }else if(StringUtils.equalsIgnoreCase(cloudStoreType, "aws")) {
            return Platform.config.getString("aws_storage_container");
        }else {
            throw new ServerException("ERR_INVALID_CLOUD_STORAGE", "Error while getting container name");
        }
    }

    public BaseStorageService getcloudService(String cloudStoreType){
        if(StringUtils.equalsIgnoreCase(cloudStoreType, "azure")) {
            return azureService;
        }else if(StringUtils.equalsIgnoreCase(cloudStoreType, "aws")) {
            return awsService;
        }else {
            throw new ServerException("ERR_INVALID_CLOUD_STORAGE", "Error while getting container name");
        }
    }

}
