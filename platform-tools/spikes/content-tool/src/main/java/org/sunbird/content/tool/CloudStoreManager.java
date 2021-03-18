package org.sunbird.content.tool;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.exception.ServerException;
import org.sunbird.telemetry.logger.TelemetryManager;
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
    private String cloudSrcBaseURL = Platform.config.getString("cloud.src.baseurl");
    private String cloudDestBaseURL = Platform.config.getString("cloud.dest.baseurl");


    protected static Map<String, String> extractMimeType = new HashMap<>();

    static {
        extractMimeType.put("application/vnd.ekstep.h5p-archive", "h5p");
        extractMimeType.put("application/vnd.ekstep.ecml-archive", "ecml");
        extractMimeType.put("application/vnd.ekstep.html-archive", "html");
    }

    public String getDestUrl(String url) {
        String destUrl = "";
        String relativePath = StringUtils.replace(url, cloudSrcBaseURL, "");
        relativePath = StringUtils.replace(relativePath, cloudDestBaseURL, "");
        if (StringUtils.isNotBlank(relativePath))
            destUrl = cloudDestBaseURL + relativePath;
        return destUrl;
    }

    public boolean urlAvailable(String url) throws Exception {
        Process p = Runtime.getRuntime().exec("curl -I " + url);
        String response = IOUtils.toString(p.getInputStream());
        return StringUtils.contains(response, "HTTP/1.1 200 OK");
    }

    public Map<String, Object> copyEcar(Map<String, Object> metadata) throws Exception {
        Map<String, Object> updateUrls = new HashMap<>();
        String id = (String) metadata.get("identifier");
        String mimeType = (String) metadata.get("mimeType");
        try {
            String downloadUrl = (String) metadata.get("downloadUrl");
            if(StringUtils.isNotBlank(downloadUrl)){
                String destDownloadUrl = getDestUrl(downloadUrl);
                if (!urlAvailable(destDownloadUrl)) {
                    String path = downloadEcar(id, downloadUrl);
                    destDownloadUrl = uploadEcar(id, destStorageType, path);
                } else {
                    TelemetryManager.info("downloadUrl available in destination: " + destDownloadUrl);
                }
                if (StringUtils.isNotBlank(destDownloadUrl)) {
                    updateUrls.put("downloadUrl", destDownloadUrl);
                }
            }


            Map<String, Object> variants = (Map<String, Object>) metadata.get("variants");
            if (null != variants && CollectionUtils.isNotEmpty(variants.keySet())) {
                String spineEcarUrl = (String) ((Map<String, Object>) variants.get("spine")).get("ecarUrl");
                if (StringUtils.isNotBlank(spineEcarUrl)) {
                    String destSpineEcar = getDestUrl(spineEcarUrl);
                    if (!urlAvailable(destSpineEcar)) {
                        String spinePath = downloadEcar(id, spineEcarUrl);
                        destSpineEcar = uploadEcar(id, destStorageType, spinePath);
                    } else {
                        TelemetryManager.info("variants.spine.ecarUrl available in destination: " + destSpineEcar);
                    }
                    ((Map<String, Object>) variants.get("spine")).put("ecarUrl", destSpineEcar);
                    updateUrls.put("variants", variants);
                }
            }

            String appIconUrl = (String) metadata.get("appIcon");
            if(StringUtils.isNotBlank(appIconUrl)){
                String destAppIconUrl = getDestUrl(appIconUrl);
                if (!urlAvailable(destAppIconUrl)) {
                    String appIconPath = downloadArtifact(id, appIconUrl, false);
                    destAppIconUrl = uploadArtifact(id, appIconPath, destStorageType);
                } else {
                    TelemetryManager.info("appIcon available in destination: " + destAppIconUrl);
                }
                if (StringUtils.isNotBlank(destAppIconUrl)) {
                    updateUrls.put("appIcon", destAppIconUrl);
                }
            }

            String posterImageUrl = (String) metadata.get("posterImage");
            if(StringUtils.isNotBlank(posterImageUrl)){
                String destPosterImageUrl = getDestUrl(posterImageUrl);
                if (!urlAvailable(destPosterImageUrl)) {
                    String posterImagePath = downloadArtifact(id, posterImageUrl, false);
                    destPosterImageUrl = uploadArtifact(id, posterImagePath, destStorageType);
                } else {
                    TelemetryManager.info("posterImage available in destination: " + destPosterImageUrl);
                }
                if (StringUtils.isNotBlank(destPosterImageUrl)) {
                    updateUrls.put("posterImage", destPosterImageUrl);
                }
            }

            String tocUrl = (String) metadata.get("toc_url");
            if(StringUtils.isNotBlank(tocUrl)){
                String destTocUrlUrl = getDestUrl(tocUrl);
                if (!urlAvailable(destTocUrlUrl)) {
                    String tocUrlPath = downloadArtifact(id, tocUrl, false);
                    destTocUrlUrl = uploadArtifact(id, tocUrlPath, destStorageType);
                } else {
                    TelemetryManager.info("toc_url available in destination: " + destTocUrlUrl);
                }
                if (StringUtils.isNotBlank(destTocUrlUrl)) {
                    updateUrls.put("toc_url", destTocUrlUrl);
                }
            }

            if (!StringUtils.equals(mimeType, "video/x-youtube")) {
                String artefactUrl = (String) metadata.get("artifactUrl");
                if(StringUtils.isNotBlank(artefactUrl)){
                    String destArtefactUrl = getDestUrl(artefactUrl);
                    if (!urlAvailable(destArtefactUrl)) {
                        String artefactPath = downloadArtifact(id, artefactUrl, false);
                        destArtefactUrl = uploadArtifact(id, artefactPath, destStorageType);
                    } else {
                        TelemetryManager.info("toc_url available in destination: " + destArtefactUrl);
                    }

                    if (StringUtils.isNotBlank(destArtefactUrl)) {
                        updateUrls.put("artifactUrl", destArtefactUrl);
                    }

                    if(extractMimeType.keySet().contains(metadata.get("mimeType"))){
                        extractArchives(id, (String) metadata.get("mimeType"), artefactUrl, ((Number) metadata.get("pkgVersion")).doubleValue());
                    }
                }
            }

            metadata.remove("status");
            Map<String, Object> content = new HashMap<>();
            content.put("content", updateUrls);
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
        String url = getcloudService(cloudStoreType).upload(getContainerName(cloudStoreType), file.getAbsolutePath(), objectKey, Option.apply(false), Option.apply(1), Option.apply(5), Option.empty());
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
        String folder = "ecar_files/" + id;
        File file = new File(path);
        String objectKey = folder + "/" + file.getName();
        String url = getcloudService(cloudStoreType).upload(getContainerName(cloudStoreType), file.getAbsolutePath(), objectKey, Option.apply(false), Option.apply(1), Option.apply(5), Option.empty());
        return url;
    }

    public void uploadAndExtract(String id, String artefactUrl, String mimeType, double pkgVersion) throws Exception {
        if(StringUtils.isNotBlank(artefactUrl)){
            String destArtefactUrl = getDestUrl(artefactUrl);
            if (!urlAvailable(destArtefactUrl)) {
                String artefactPath = downloadArtifact(id, artefactUrl, false);
                destArtefactUrl = uploadArtifact(id, artefactPath, destStorageType);
                TelemetryManager.info("Content destination URL: "+ destArtefactUrl);
            } else {
                TelemetryManager.info("artifactUrl available in destination but syncing extracted paths: " + destArtefactUrl);
            }


            extractArchives(id, mimeType, destArtefactUrl, pkgVersion);
        }
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
