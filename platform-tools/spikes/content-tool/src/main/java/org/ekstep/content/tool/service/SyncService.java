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
                InputList inputList = search(filter);
                if (StringUtils.equalsIgnoreCase(dryRun, "true")) {
                    TelemetryManager.info("Content count to migrate: " + inputList.getCount() + "\n" + "Data : \n" + inputList.toString());
                } else {
                    updateOwnership(inputList, createdBy, channel, createdFor, organisation, creator, forceUpdate);
                }

            } else {
                TelemetryManager.error("Error while ownership migration", new ClientException("ERR_INVALID_REQUEST", "Invalid Channel Id"));
                System.out.println("Channel Id is invalid!! Please provide valid channel ID");
            }
        } catch (Exception e) {
            TelemetryManager.error("Error while ownership migration", e);
        }
    }

    @Override
    public void sync(String filter, String dryRun, String forceUpdate) {
        try {
            InputList inputList = search(filter);
            if (StringUtils.equalsIgnoreCase("true", dryRun)) {
                TelemetryManager.info("Content count to sync: " + inputList.getCount() + "\n" + "Data : \n" + inputList.toString());
            } else {
                Map<String, InputList> response = syncData(inputList, forceUpdate);
                TelemetryManager.info("Contents synced : " + response.get("success").size() +  "\n" + response.get("success").toString());
                TelemetryManager.info("Contents skipped without syncing : " + response.get("skipped").size() +  "\n" + response.get("skipped").toString());
                TelemetryManager.info("Contents failed without syncing : " + response.get("failed").size() +  "\n" + response.get("failed").toString());
            }

        } catch (Exception e) {
            TelemetryManager.error("Error while syncing content data", e);
        }
    }

    private void updateOwnership(InputList inputList, String createdBy, String channel, String[] createdFor, String[] organisation, String creator, String forceUpdate) throws Exception {
        if (CollectionUtils.isNotEmpty(inputList.getInputList())) {
            Map<String, Object> request = getUpdateRequest(createdBy, channel, createdFor, organisation, creator);
            Map<String, InputList> response = updateData(inputList, request, channel, forceUpdate);

            TelemetryManager.info("Migrated content count: " + response.get("success").size() +  "\n" + response.get("success").toString());
            TelemetryManager.info("Skipped content count: " + response.get("skipped").size() +  "\n" + response.get("skipped").toString());
            TelemetryManager.info("Falied content count: " + response.get("failed").size() +  "\n" + response.get("failed").toString());
        } else {
            TelemetryManager.info("No contents to migrate");
        }
    }

    private Map<String, InputList> updateData(InputList inputList, Map<String, Object> request, String channel, String forceUpdate) throws Exception {
        Map<String, InputList> output = new HashMap<>();
        InputList successful = new InputList(new ArrayList<>());
        InputList skipped = new InputList(new ArrayList<>());
        InputList failure = new InputList(new ArrayList<>());

        for (Input input : inputList.getInputList()) {
            try {
                if (migrateOwner(input, request, channel, forceUpdate)) {
                    successful.add(input);
                } else {
                    skipped.add(input);
                }
            } catch (Exception e) {
                TelemetryManager.error("Error while migrating ownership for ID: " + input.getId(), e);
                failure.add(input);
            }
        }

        output.put("success", successful);
        output.put("skipped", skipped);
        output.put("failed", failure);

        return output;
    }

    private boolean migrateOwner(Input input, Map<String, Object> request, String channel, String forceUpdate) throws Exception {
        Response readResponse = getContent(input.getId(), true, null);
        if (isSuccess(readResponse)) {
            Map<String, Object> destContent = (Map<String, Object>) readResponse.get("content");
            double srcPkgVersion = input.getPkgVersion();
            double destPkgVersion = ((Number) destContent.get("pkgVersion")).doubleValue();
            if (isForceupdate(forceUpdate) || (0 == Double.compare(srcPkgVersion, destPkgVersion))) {
                if (!request.isEmpty()) {
                    Response updateResponse = systemUpdate(input.getId(), request, channel, true);
                    Response updateSourceResponse = systemUpdate(input.getId(), request, channel, false);

                    if (isSuccess(updateResponse) && isSuccess(updateSourceResponse)) {
                        Response response = getContent(input.getId(), true, null);
                        updateEcarInfo(input.getId(), (Map<String, Object>) response.get("content"));
                        return true;
                    } else {
                       return false;
                    }
                }else{
                    updateEcarInfo(input.getId(), (Map<String, Object>) readResponse.get("content"));
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
                return true;
            } else if (-1 == Double.compare(srcPkgVersion, destPkgVersion)) {
                syncData(new InputList(Arrays.asList(input)), forceUpdate);
                return migrateOwner(input, request, channel, forceUpdate);
            } else {
                return false;
            }
        } else {
            throw new ServerException("ERR_CONTENT_SYNC", "Error while fetching the content " + input.getId() + " from destination env", readResponse.getParams().getErrmsg());
        }
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
        InputList skipped = new InputList(new ArrayList<>());
        InputList failed = new InputList(new ArrayList<>());

        for (Input input : inputList.getInputList()) {
            try {
                if(sync(input, forceUpdate)) {
                    success.add(input);
                }else{
                    skipped.add(input);
                }
            } catch (Exception e) {
                TelemetryManager.error("Error while syncing ID: " + input.getId(), e);
                failed.add(input);
            }
        }

        response.put("success", success);
        response.put("skipped", skipped);
        response.put("failed", failed);

        return response;

    }

    private boolean sync(Input input, String forceUpdate) throws Exception{
        Response destContent = getContent(input.getId(), true, null);
        if (isSuccess(destContent)) {
            Response sourceContent = getContent(input.getId(), false, null);
            if (isForceupdate(forceUpdate) || (Double.compare(((Number) ((Map<String, Object>) destContent.get("content")).get("pkgVersion")).doubleValue(), ((Number) ((Map<String, Object>) sourceContent.get("content")).get("pkgVersion")).doubleValue()) == -1)) {
                updateMetadata(sourceContent, forceUpdate);
                syncHierarchy(input.getId());
                return true;
            } else {
                return false;
            }

        } else {
            createContent(input.getId(), forceUpdate);
            syncHierarchy(input.getId());
            return true;
        }

    }

    private void createContent(String id, String forceUpdate) throws Exception {
        Response sourceContent = getContent(id, false, null);
        Map<String, Object> metadata = (Map<String, Object>) sourceContent.get("content");
        String channel = (String) metadata.get("channel");
        metadata.put("pkgVersion", ((Number) metadata.get("pkgVersion")).doubleValue());
        Response response = systemUpdate(id, makeContentRequest(metadata), channel, true);
        if (isSuccess(response)) {
            updateExternalProps(id, channel);
            updateMimeType(id, metadata, forceUpdate);
        }else{
            throw new ServerException("ERR_CONTENT_SYNC","Error while creating content " + id +" in destination env : "+  response.getParams().getErrmsg());
        }

    }

    private void updateMimeType(String id, Map<String, Object> metadata, String forceUpdate) throws Exception {
        String localPath = null;
        try {
            String mimeType = (String) metadata.get("mimeType");
            double pkgVersion = ((Number) metadata.get("pkgVersion")).doubleValue();
            String channel = (String) metadata.get("channel");
            switch (mimeType) {
                case "application/vnd.ekstep.ecml-archive":
                    localPath = cloudStoreManager.downloadArtifact(id, (String) metadata.get("artifactUrl"), true);
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
                    cloudStoreManager.uploadAndExtract(id, (String) metadata.get("artifactUrl"), mimeType, pkgVersion);
                    break;
                default:
                    break;
            }

        } finally {
            if (StringUtils.isNotBlank(localPath))
                FileUtils.deleteDirectory(new File(localPath));
        }
    }

    private void updateExternalProps(String id, String channel) throws Exception {
        String externalFields = Platform.config.getString("content.external_fields");
        Response contentExt = getContent(id, false, externalFields);
        Response extResp = systemUpdate(id, makeContentRequest(contentExt.getResult().get("content")), channel, true);
        if(!isSuccess(extResp)){
            throw new ServerException("ERR_CONTENT_SYNC","Error while updating external fields to content " + id +" in destination env : "+  extResp.getParams().getErrmsg());
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

    private void updateEcarInfo(String id, Map<String, Object> metadata) throws Exception {
        Map<String, Object> urlUpdateReq = cloudStoreManager.copyEcar(metadata);
        systemUpdate(id, urlUpdateReq, (String) metadata.get("channel"), true);
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
            Response response = executeGET(destUrl + "/assessment/v3/itemsets/" + id, destKey);
            if(!isSuccess(response)) {
                Response sourceItemSet = getContent(id, false);
                if(isSuccess(sourceItemSet)){
                    Map<String, Object> metadata = (Map<String, Object>) sourceItemSet.get("assessment_item_set");

                }
            }
        }*/

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

        return makeContentRequest(metadata);
    }
}
