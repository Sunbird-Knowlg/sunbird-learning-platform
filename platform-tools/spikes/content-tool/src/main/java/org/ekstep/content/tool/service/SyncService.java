package org.ekstep.content.tool.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component("contentSyncService")
public class SyncService extends BaseService implements ISyncService {


    private static final String COLLECTION_MIMETYPE = "application/vnd.ekstep.content-collection";

    @Override
    public void dryRun() {

    }

    @Override
    public void ownerMigration(String createdBy, String channel, String[] createdFor, String[] organisation, String creator, String filter, String dryRun, String forceUpdate) {
        try {
            if (validChannel(channel)) {
                Map<String, Map<String, Object>> identifiers = getFromSource(filter);
                if (StringUtils.equalsIgnoreCase(dryRun, "true")) {
                    System.out.println("content count to migrate: " + identifiers.keySet().size() + " " + identifiers.keySet());
                } else {
                    updateOwnership(identifiers, createdBy, channel, createdFor, organisation, creator, forceUpdate);

                }

            } else {
                throw new ClientException("ERR_INVALID_REQUEST", "Invalid Channel Id");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServerException("ERR_OWNER_MIG", "Error while ownership migration", e);
        }
    }

    @Override
    public void sync(String filter, String dryRun, String forceUpdate) {
        try {
            Map<String, Map<String, Object>> identifiers = getFromSource(filter);
            if (StringUtils.equalsIgnoreCase("true", dryRun)) {
                System.out.println("content count to sync: " + identifiers.keySet().size() + " " + identifiers.values());
            } else {
                Map<String, Object> response = syncData(identifiers, forceUpdate);
                System.out.println("Contents synced : " + response.get("success"));
                System.out.println("Contents skipped without syncing : " + response.get("failed"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServerException("ERR_OWNER_MIG", "Error while syncing content data", e);
        }

    }

    private void updateOwnership(Map<String, Map<String, Object>> identifiers, String createdBy, String channel, String[] createdFor, String[] organisation, String creator, String forceUpdate) throws Exception {
        if (!identifiers.isEmpty()) {
            Map<String, Object> request = getUpdateRequest(createdBy, channel, createdFor, organisation, creator);
            Map<String, Object> response = updateData(identifiers, request, channel, forceUpdate);

            System.out.println("Migrated content count: " + ((List<String>) response.get("success")).size() + " : " + response.get("success"));
            System.out.println("Skipped content count: " + ((List<String>) response.get("failed")).size() + " : " + response.get("failed"));

        } else {
            System.out.println("No contents to migrate");
        }
    }

    private Map<String, Object> updateData(Map<String, Map<String, Object>> identifiers, Map<String, Object> request, String channel, String forceUpdate) throws Exception {
        Map<String, Object> output = new HashMap<>();
        List<String> successful = new ArrayList<>();
        List<String> failure = new ArrayList<>();

        for (String id : identifiers.keySet()) {
            Response readResponse = getContent(id, true, null);
            if (isSuccess(readResponse)) {
                Map<String, Object> destContent = (Map<String, Object>) readResponse.get("content");
                double srcPkgVersion = ((Number) identifiers.get(id).get("pkgVersion")).doubleValue();
                double destPkgVersion = ((Number) destContent.get("pkgVersion")).doubleValue();
                if (isForceupdate(forceUpdate) || (0 == Double.compare(srcPkgVersion, destPkgVersion))) {
                    if (!request.isEmpty()) {
                        TelemetryManager.info("Updating the content !!!!");
                        Response updateResponse = systemUpdate(id, request, channel, true);
                        Response updateSourceResponse = systemUpdate(id, request, channel, false);

                        System.out.println("Destination Update Response : "  + updateResponse.getResult());
                        System.out.println("Source Update Response : "  + updateSourceResponse.getResult());

                        if (isSuccess(updateResponse) && isSuccess(updateSourceResponse)) {
                            successful.add(id);
                            Response response = getContent(id, true, null);
                            copyEcar(response);
                        } else {
                            failure.add(id);
                        }
                    }else{
                        copyEcar(readResponse);
                    }
                    Map<String, Map<String, Object>> children = new HashMap<>();
                    fetchChildren(readResponse, children);
                    if (!children.isEmpty())
                        updateData(children, request, channel, forceUpdate);
                    if (StringUtils.equalsIgnoreCase(COLLECTION_MIMETYPE, (String) destContent.get("mimeType")))
                        syncHierarchy(id);

                    if (containsItemsSet(destContent)) {
                        copyAssessmentItems((List<Map<String, Object>>) destContent.get("item_sets"));
                    }
                } else if (isForceupdate(forceUpdate) || (-1 == Double.compare(srcPkgVersion, destPkgVersion))) {
                    syncData(identifiers, forceUpdate);
                    updateData(identifiers, request, channel, forceUpdate);
                } else {
                    failure.add(id);
                }
            } else {
                failure.add(id);
            }
        }

        output.put("success", successful);
        output.put("failed", failure);

        return output;
    }

    private boolean isForceupdate(String forceUpdate) {
        return StringUtils.equalsIgnoreCase("true", forceUpdate);
    }

    private boolean containsItemsSet(Map<String, Object> content) {
        return CollectionUtils.isNotEmpty((List<Map<String, Object>>) content.get("item_sets"));
    }

    private void copyAssessmentItems(List<Map<String, Object>> itemSets) throws Exception {
        /*for(Map<String, Object> itemSet: itemSets) {
            String id = (String) itemSet.get("identifier");
            Response response = executeGet(destUrl + "/assessment/v3/itemsets/" + id, destKey);
            if(!isSuccess(response)) {
                Response sourceItemSet = getContent(id, false);
                if(isSuccess(sourceItemSet)){
                    Map<String, Object> metadata = (Map<String, Object>) sourceItemSet.get("assessment_item_set");

                }
            }
        }*/

    }

    private void copyEcar(Response readResponse) throws Exception {
        Map<String, Object> metadata = (Map<String, Object>) readResponse.get("content");
        String id = (String) metadata.get("identifier");
        String mimeType = (String) metadata.get("mimeType");
        try {
            String downloadUrl = (String) metadata.get("downloadUrl");
            String path = downloadEcar(id, downloadUrl, sourceStorageType);
            String destDownloadUrl = uploadEcar(id, destStorageType, path);

            if (StringUtils.isNotBlank(destDownloadUrl)) {
                metadata.put("downloadUrl", destDownloadUrl);
            }

            Map<String, Object> variants = (Map<String, Object>) metadata.get("variants");
            if (CollectionUtils.isNotEmpty(variants.keySet())) {
                String spineEcarUrl = (String) ((Map<String, Object>) variants.get("spine")).get("ecarUrl");
                if (StringUtils.isNotBlank(spineEcarUrl)) {
                    String spinePath = downloadEcar(id, spineEcarUrl, sourceStorageType);
                    String destSpineEcar = uploadEcar(id, destStorageType, spinePath);
                    ((Map<String, Object>) variants.get("spine")).put("ecarUrl", destSpineEcar);
                    metadata.put("variants", variants);
                }
            }
            
            String appIconUrl = (String) metadata.get("appIcon");
            if(StringUtils.isNotBlank(appIconUrl)){
                String appIconPath = downloadArtifact(id, appIconUrl, sourceStorageType, false);
                String destAppIconUrl = uploadArtifact(id, appIconPath, destStorageType);
                if (StringUtils.isNotBlank(destAppIconUrl)) {
                    metadata.put("appIcon", destAppIconUrl);
                }
            }

            String posterImageUrl = (String) metadata.get("posterImage");
            if(StringUtils.isNotBlank(posterImageUrl)){
                String posterImagePath = downloadArtifact(id, posterImageUrl, sourceStorageType, false);
                String destPosterImageUrl = uploadArtifact(id, posterImagePath, destStorageType);
                if (StringUtils.isNotBlank(destPosterImageUrl)) {
                    metadata.put("posterImage", destPosterImageUrl);
                }
            }

            String tocUrl = (String) metadata.get("toc_url");
            if(StringUtils.isNotBlank(tocUrl)){
                String tocUrlPath = downloadArtifact(id, tocUrl, sourceStorageType, false);
                String destTocUrlUrl = uploadArtifact(id, tocUrlPath, destStorageType);
                if (StringUtils.isNotBlank(destTocUrlUrl)) {
                    metadata.put("toc_url", destTocUrlUrl);
                }
            }

            if (!StringUtils.equals(mimeType, "video/x-youtube")) {
	        		String artefactUrl = (String) metadata.get("artifactUrl");
	            String artefactPath = downloadArtifact(id, artefactUrl, sourceStorageType, false);
	            String destArtefactUrl = uploadArtifact(id, artefactPath, destStorageType);
	            if (StringUtils.isNotBlank(destArtefactUrl)) {
	                metadata.put("artifactUrl", destArtefactUrl);
	            }
	            
	            if(extractMimeType.keySet().contains(metadata.get("mimeType"))){
	                extractArchives(id, (String) metadata.get("mimeType"), artefactUrl, ((Number) metadata.get("pkgVersion")).doubleValue());
	            }
	        }

            Map<String, Object> content = new HashMap<>();
            content.put("content", metadata);
            Map<String, Object> request = new HashMap<>();
            request.put("request", content);
            systemUpdate(id, request, (String) metadata.get("channel"), true);
        } finally {
            FileUtils.deleteDirectory(new File("tmp/" + id));
        }
    }

    private void fetchChildren(Response readResponse, Map<String, Map<String, Object>> children) throws Exception {
        List<Map<String, Object>> childNodes = (List<Map<String, Object>>) ((Map<String, Object>) readResponse.get("content")).get("children");

        if (!CollectionUtils.isEmpty(childNodes)) {
            for (Map<String, Object> child : childNodes) {
                Response childContent = getContent((String) child.get("identifier"), true, null);
                Map<String, Object> contenMetadata = (Map<String, Object>) childContent.get("content");
                String visibility = (String) contenMetadata.get("visibility");

                if (StringUtils.isNotBlank(visibility) && StringUtils.equalsIgnoreCase("Parent", visibility)) {
                    children.put((String) contenMetadata.get("identifier"), contenMetadata);
                    fetchChildren(childContent, children);
                }
            }
        }


    }

    private Map<String, Object> getUpdateRequest(String createdBy, String channel, String[] createdFor, String[] organisation, String creator) {
        Map<String, Object> metadata = new HashMap<>();
        if (StringUtils.isNotBlank(channel))
            metadata.put("channel", channel);
        if (StringUtils.isNotBlank(createdBy))
            metadata.put("createdBy", createdBy);
        if (null != createdFor && createdFor.length > 0)
            metadata.put("createdFor", Arrays.asList(createdFor));
        if (null != organisation && organisation.length > 0)
            metadata.put("organization", Arrays.asList(organisation));
        if (StringUtils.isNotBlank(creator))
            metadata.put("creator", creator);

        Map<String, Object> content = new HashMap<>();
        content.put("content", metadata);

        Map<String, Object> request = new HashMap<>();
        request.put("request", content);

        System.out.println("Request : " + request);
        return request;
    }


    /**
     * For each ID
     * - read the content from dest
     * - if not exists, create the content, upload the artefact and publish
     * - if exists, check pkgVersion(<=) then update the content
     * - download the ecar or artifact and upload the same using upload api
     * - update the collection hierarchy
     **/
    private Map<String, Object> syncData(Map<String, Map<String, Object>> identifiers, String forceUpdate) {
        Map<String, Object> response = new HashMap<>();
        List<String> success = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        for (String id : identifiers.keySet()) {
            try {
                Response destContent = getContent(id, true, null);
                if (isSuccess(destContent)) {
                    Response sourceContent = getContent(id, false, null);
                    if (isForceupdate(forceUpdate) || (Double.compare(((Number) ((Map<String, Object>) destContent.get("content")).get("pkgVersion")).doubleValue(), ((Number) ((Map<String, Object>) sourceContent.get("content")).get("pkgVersion")).doubleValue()) == -1)) {
                        updateMetadata(sourceContent, forceUpdate);
                        syncHierarchy(id);
                        success.add(id);
                    } else {
                        failed.add(id);
                    }

                } else {
                    createContent(id, forceUpdate);
                    syncHierarchy(id);
                    success.add(id);
                }
            } catch (Exception e) {
                e.printStackTrace();
                failed.add(id);
            }

        }

        response.put("success", success);
        response.put("failed", failed);

        return response;

    }

    private void createContent(String id, String forceUpdate) throws Exception {
        Response sourceContent = getContent(id, false, null);
        Map<String, Object> metadata = (Map<String, Object>) sourceContent.get("content");
        String channel = (String) metadata.get("channel");
        metadata.put("pkgVersion", ((Number) metadata.get("pkgVersion")).doubleValue());
        Map<String, Object> content = new HashMap<>();
        content.put("content", metadata);
        Map<String, Object> request = new HashMap<>();
        request.put("request", content);

        Response response = systemUpdate(id, request, channel, true);

        if (isSuccess(response)) {
            String localPath = null;
            try {
                String externalFields = Platform.config.getString("content.external_fields");
                Response contentExt = getContent(id, false, externalFields);
                request.put("request", contentExt.getResult());
                Response extResp = systemUpdate(id, request, channel, true);                
                if (StringUtils.equalsIgnoreCase("application/vnd.ekstep.ecml-archive", (String) metadata.get("mimeType"))) {
                		localPath = downloadArtifact(id, (String) metadata.get("artifactUrl"), sourceStorageType, true);
                		copyAssets(localPath, forceUpdate);
                }
                List<Map<String, Object>> children = (List<Map<String, Object>>) metadata.get("children");
                if (CollectionUtils.isNotEmpty(children)) {
                    for (Map<String, Object> child : children) {
                        createContent((String) child.get("identifier"), forceUpdate);
                    }
                }
            } finally {
                if (StringUtils.isNotBlank(localPath))
                    FileUtils.deleteDirectory(new File(localPath));
            }

        }

    }

    private void copyAssets(String localPath, String forceUpdate) throws Exception {
        if (StringUtils.isNotBlank(localPath)) {
            Map<String, Object> assets = readECMLFile(localPath + "/index.ecml");
            for (String assetId : assets.keySet()) {
                Response destAsset = getContent(assetId, true, null);
                if (isForceupdate(forceUpdate) || !isSuccess(destAsset)) {
                    Response sourceAsset = getContent(assetId, false, null);
                    if (isSuccess(sourceAsset)) {
                        Map<String, Object> assetRequest = (Map<String, Object>) sourceAsset.get("content");
                        assetRequest.remove("variants");
                        assetRequest.remove("downloadUrl");
                        assetRequest.remove("artifactUrl");
                        assetRequest.remove("status");
                        Map<String, Object> content = new HashMap<>();
                        content.put("content", assetRequest);
                        Map<String, Object> request = new HashMap<>();
                        request.put("request", content);
                        Response createResponse = systemUpdate(assetId, request, (String) assetRequest.get("channel"), true);
                        Response response = uploadAsset(localPath + File.separator + "assets" + File.separator + assets.get(assetId), assetId, (String) assets.get(assetId));
                    }
                }
            }
        }
    }

    private void cleanMetadata(Map<String, Object> metadata) {
        metadata.remove("downloadUrl");
        metadata.remove("artifactUrl");
        metadata.remove("posterImage");
        metadata.remove("pkgVersion");
        metadata.remove("s3key");
        metadata.remove("variants");
    }

    private void syncHierarchy(String id) throws Exception {
        executePost(destUrl + "/content/v3/hierarchy/sync/" + id, destKey, new HashMap<>(), null);
    }

    private void updateMetadata(Response sourceContent, String forceUpdate) throws Exception {
        createContent((String) ((Map<String, Object>) sourceContent.get("content")).get("identifier"), forceUpdate);

    }

}
