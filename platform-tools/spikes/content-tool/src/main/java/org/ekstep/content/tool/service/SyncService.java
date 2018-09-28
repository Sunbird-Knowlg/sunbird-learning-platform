package org.ekstep.content.tool.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.content.tool.util.Input;
import org.ekstep.content.tool.util.InputList;
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
    public void ownerMigration(String createdBy, String channel, String[] createdFor, String[] organisation, String creator, String filter, String dryRun, String forceUpdate) {
        try {
            if (validChannel(channel)) {
                InputList inputList = getFromSource(filter);
                if (StringUtils.equalsIgnoreCase(dryRun, "true")) {
                    System.out.println("Content count to migrate: " + inputList.size() + "\n" + "Data : \n" + inputList.toString());
                } else {
                    updateOwnership(inputList, createdBy, channel, createdFor, organisation, creator, forceUpdate);

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
            InputList inputList = getFromSource(filter);
            if (StringUtils.equalsIgnoreCase("true", dryRun)) {
                System.out.println("Content count to sync: " + inputList.size() + "\n" + "Data : \n" + inputList.toString());
            } else {
                Map<String, InputList> response = syncData(inputList, forceUpdate);
                System.out.println("Contents synced : " + response.get("success").size() +  "\n" + response.get("success").toString());
                System.out.println("Contents skipped without syncing : " + response.get("failed").size() +  "\n" + response.get("failed").toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServerException("ERR_OWNER_MIG", "Error while syncing content data", e);
        }

    }

    private void updateOwnership(InputList inputList, String createdBy, String channel, String[] createdFor, String[] organisation, String creator, String forceUpdate) throws Exception {
        if (CollectionUtils.isNotEmpty(inputList.getInputList())) {
            Map<String, Object> request = getUpdateRequest(createdBy, channel, createdFor, organisation, creator);
            Map<String, InputList> response = updateData(inputList, request, channel, forceUpdate);

            System.out.println("Migrated content count: " + response.get("success").size() +  "\n" + response.get("success").toString());
            System.out.println("Skipped content count: " + response.get("failed").size() +  "\n" + response.get("failed").toString());

        } else {
            System.out.println("No contents to migrate");
        }
    }

    private Map<String, InputList> updateData(InputList inputList, Map<String, Object> request, String channel, String forceUpdate) throws Exception {
        Map<String, InputList> output = new HashMap<>();
        InputList successful = new InputList(new ArrayList<>());
        InputList failure = new InputList(new ArrayList<>());

        for (Input input : inputList.getInputList()) {
            Response readResponse = getContent(input.getId(), true, null);
            if (isSuccess(readResponse)) {
                Map<String, Object> destContent = (Map<String, Object>) readResponse.get("content");
                double srcPkgVersion = input.getPkgVersion();
                double destPkgVersion = ((Number) destContent.get("pkgVersion")).doubleValue();
                if (isForceupdate(forceUpdate) || (0 == Double.compare(srcPkgVersion, destPkgVersion))) {
                    if (!request.isEmpty()) {
                        TelemetryManager.info("Updating the content !!!!");
                        Response updateResponse = systemUpdate(input.getId(), request, channel, true);
                        Response updateSourceResponse = systemUpdate(input.getId(), request, channel, false);

                        System.out.println("Destination Update Response : "  + updateResponse.getResult());
                        System.out.println("Source Update Response : "  + updateSourceResponse.getResult());

                        if (isSuccess(updateResponse) && isSuccess(updateSourceResponse)) {
                            successful.add(input);
                            Response response = getContent(input.getId(), true, null);
                            copyEcar(response);
                        } else {
                            failure.add(input);
                        }
                    }else{
                        copyEcar(readResponse);
                    }
                    InputList children = new InputList( new ArrayList<>());
                    fetchChildren(readResponse, children);
                    if (children.isNotEmpty())
                        updateData(children, request, channel, forceUpdate);
                    if (StringUtils.equalsIgnoreCase(COLLECTION_MIMETYPE, (String) destContent.get("mimeType")))
                        syncHierarchy(input.getId());

                    if (containsItemsSet(destContent)) {
                        copyAssessmentItems((List<Map<String, Object>>) destContent.get("item_sets"));
                    }
                } else if (isForceupdate(forceUpdate) || (-1 == Double.compare(srcPkgVersion, destPkgVersion))) {
                    syncData(inputList, forceUpdate);
                    updateData(inputList, request, channel, forceUpdate);
                } else {
                    failure.add(input);
                }
            } else {
                failure.add(input);
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
            systemUpdate(id, request, (String) metadata.get("channel"), true);
        } finally {
            FileUtils.deleteDirectory(new File("tmp/" + id));
        }
    }

    private void fetchChildren(Response readResponse, InputList children) throws Exception {
        List<Map<String, Object>> childNodes = (List<Map<String, Object>>) ((Map<String, Object>) readResponse.get("content")).get("children");

        if (!CollectionUtils.isEmpty(childNodes)) {
            for (Map<String, Object> child : childNodes) {
                Response childContent = getContent((String) child.get("identifier"), true, null);
                Map<String, Object> contenMetadata = (Map<String, Object>) childContent.get("content");
                String visibility = (String) contenMetadata.get("visibility");

                if (StringUtils.isNotBlank(visibility) && StringUtils.equalsIgnoreCase("Parent", visibility)) {
                    Input childInput = new Input((String) contenMetadata.get("identifier"), (String) contenMetadata.get("name"), ((Number) contenMetadata.get("pkgVersion")).doubleValue(), (String) contenMetadata.get("objectType"), (String) contenMetadata.get("status"));
                    children.add(childInput);
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
    private Map<String, InputList> syncData(InputList inputList, String forceUpdate) {
        Map<String, InputList> response = new HashMap<>();
        InputList success = new InputList(new ArrayList<>());
        InputList failed = new InputList(new ArrayList<>());

        for (Input input : inputList.getInputList()) {
            try {
                Response destContent = getContent(input.getId(), true, null);
                if (isSuccess(destContent)) {
                    Response sourceContent = getContent(input.getId(), false, null);
                    if (isForceupdate(forceUpdate) || (Double.compare(((Number) ((Map<String, Object>) destContent.get("content")).get("pkgVersion")).doubleValue(), ((Number) ((Map<String, Object>) sourceContent.get("content")).get("pkgVersion")).doubleValue()) == -1)) {
                        updateMetadata(sourceContent, forceUpdate);
                        syncHierarchy(input.getId());
                        success.add(input);
                    } else {
                        failed.add(input);
                    }

                } else {
                    createContent(input.getId(), forceUpdate);
                    syncHierarchy(input.getId());
                    success.add(input);
                }
            } catch (Exception e) {
                e.printStackTrace();
                failed.add(input);
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
        double pkgVersion = ((Number) metadata.get("pkgVersion")).doubleValue();
        metadata.put("pkgVersion", pkgVersion);
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
                String mimeType = (String) metadata.get("mimeType");
                switch (mimeType) {
                    case "application/vnd.ekstep.ecml-archive":
                        localPath = downloadArtifact(id, (String) metadata.get("artifactUrl"), true);
                        copyAssets(localPath, forceUpdate);
                        break;
                    case "application/vnd.ekstep.content-collection":
                        List<Map<String, Object>> children = (List<Map<String, Object>>) metadata.get("children");
                        if (CollectionUtils.isNotEmpty(children)) {
                            List<Map<String, String>> childrenReq = new ArrayList<>();
                            for (Map<String, Object> child : children) {
                                String childId = (String) child.get("identifier");
                                createContent(childId, forceUpdate);
                                Map<String, String> childReq = new HashMap<>();
                                childReq.put("identifier", childId);
                                childrenReq.add(childReq);
                            }
                            if(CollectionUtils.isNotEmpty(childrenReq)) {
                                systemUpdate(id, makeContentRequest(childrenReq), channel, true);
                            }
                            syncHierarchy(id);
                        }
                        break;
                    case "application/vnd.ekstep.h5p-archive":
                    case "application/vnd.ekstep.html-archive":
                        uploadAndExtract(id, (String) metadata.get("artifactUrl"), mimeType, pkgVersion);
                        break;
                    default:
                        break;
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
    
    private Map<String, Object> makeContentRequest(Object metadata) {
        Map<String, Object> content = new HashMap<>();
        content.put("content", metadata);
        Map<String, Object> request = new HashMap<>();
        request.put("request", content);
        return request;
    }

}
