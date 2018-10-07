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
    private int totalSuccess = 0;
    private int totalSkipped = 0;
    private int totalFailed = 0;

    @Override
    public void ownerMigration(String createdBy, String channel, String[] createdFor, String[] organisation, String creator,  Map<String, Object> filters, String dryRun, String forceUpdate) {
        initialise();
        try {
            if (validChannel(channel)) {
                int count = searchCount(new HashMap<>(filters));

                if (StringUtils.equalsIgnoreCase(dryRun, "true")) {
                    if(null == filters.get("limit") || (defaultLimit < (int) filters.get("limit")))
                        filters.put("limit", defaultLimit);
                    InputList inputList = search(filters);
                    System.out.println("Content count to migrate: " + count + "\n" + "Data : \n" + inputList.toString());
                } else {
                    if(count > 0) {
                        System.out.println("Total No. of Contents : " + count);
                        System.out.println("-----------------------------------------");
                        updateOwnership(filters, createdBy, channel, createdFor, organisation, creator, forceUpdate, count);
                        System.out.println("-----------------------------------------");
                        System.out.println("Total Success : " + totalSuccess);
                        System.out.println("Total Skipped  : " + totalSkipped);
                        System.out.println("Total Failed : " + totalFailed);
                    } else {
                        TelemetryManager.info("No contents to migrate");
                    }
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
    public void sync(Map<String, Object> filters, String dryRun, String forceUpdate) {
        initialise();
        try {
            int count = searchCount(new HashMap<>(filters));

            if (StringUtils.equalsIgnoreCase("true", dryRun)) {
                if(null == filters.get("limit") || (defaultLimit < (int) filters.get("limit")))
                    filters.put("limit", defaultLimit);
                InputList inputList = search(filters);
                System.out.println("Content count to sync: " + count + "\n" + "Data : \n" + inputList.toString());
            } else {
                if(count > 0) {
                    System.out.println("Total No. of Contents : " + count);
                    System.out.println("-----------------------------------------");
                    syncData(filters, forceUpdate, count);
                    System.out.println("-----------------------------------------");
                    System.out.println("Total Success : " + totalSuccess);
                    System.out.println("Total Skipped  : " + totalSkipped);
                    System.out.println("Total Failed : " + totalFailed);
                }
                else {
                    System.out.println("No data to sync");
                }
            }

        } catch (Exception e) {
            TelemetryManager.error("Error while syncing content data", e);
        }
    }


    private void syncData(Map<String, Object> filters, String forceUpdate, int maxLimit) throws Exception {
        int limit = (null != filters.get("limit")) ? (int) filters.get("limit") : 0;
        int offset = (null != filters.get("offset")) ? (int) filters.get("offset") : 0;
        maxLimit = getMaxLimit(limit, offset, maxLimit);
        while(maxLimit > 0) {
            if((limit == 0) || (defaultLimit < limit))
                filters.put("limit", defaultLimit);
            else
                filters.put("limit", limit);
            filters.put("offset", offset);
            InputList inputList = search(new HashMap<>(filters));
            Map<String, InputList> response = syncData(inputList, forceUpdate);
            System.out.println("Contents synced : " + response.get("success").size() + "\n" + response.get("success").toString());
            System.out.println("Contents skipped without syncing : " + response.get("skipped").size() + "\n" + response.get("skipped").toString());
            System.out.println("Contents failed without syncing : " + response.get("failed").size() + "\n" + response.get("failed").toString());
            totalSuccess += response.get("success").size();
            totalSkipped += response.get("skipped").size();
            totalFailed += response.get("failed").size();


            maxLimit -= ((int)filters.get("limit"));
            limit = ((limit - defaultLimit) >= 0)?(limit - defaultLimit):0;
            offset += ((int)filters.get("limit"));
        }
    }

    private int getMaxLimit(int limit, int offset, int maxLimit) {
        if(limit > 0) {
            return limit;
        }else{
            return (maxLimit - offset);
        }
    }

    private void updateOwnership(Map<String, Object> filters, String createdBy, String channel, String[] createdFor, String[] organisation, String creator, String forceUpdate, int maxLimit) throws Exception {
        Map<String, Object> request = getUpdateRequest(createdBy, channel, createdFor, organisation, creator);
        int limit = (null != filters.get("limit")) ? (int) filters.get("limit") : 0;
        int offset = (null != filters.get("offset")) ? (int) filters.get("offset") : 0;
        maxLimit = getMaxLimit(limit, offset, maxLimit);
        while(maxLimit > 0 ) {
            if((limit == 0) || (defaultLimit < limit))
                filters.put("limit", defaultLimit);
            else
                filters.put("limit", limit);
            filters.put("offset", offset);
            InputList inputList = search(new HashMap<>(filters));
            Map<String, InputList> response = updateData(inputList, request, channel, forceUpdate);

            System.out.println("Migrated content count: " + response.get("success").size() + "\n" + response.get("success").toString());
            System.out.println("Skipped content count: " + response.get("skipped").size() + "\n" + response.get("skipped").toString());
            System.out.println("Falied content count: " + response.get("failed").size() + "\n" + response.get("failed").toString());
            totalSuccess += response.get("success").size();
            totalSkipped += response.get("skipped").size();
            totalFailed += response.get("failed").size();

            maxLimit -= ((int)filters.get("limit"));
            limit = ((limit - defaultLimit) >= 0)?(limit - defaultLimit):0;
            offset += ((int)filters.get("limit"));
        }
    }


    private Map<String, InputList> updateData(InputList inputList, Map<String, Object> request, String channel, String forceUpdate) {
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
                return true;
            } else {
                return false;
            }

        } else {
            createContent(input.getId(), forceUpdate);
            return true;
        }

    }

    private void createContent(String id, String forceUpdate) throws Exception {
        Response sourceContent = getContent(id, false, null);
        Map<String, Object> metadata = (Map<String, Object>) sourceContent.get("content");
        String channel = (String) metadata.get("channel");
        metadata.put("pkgVersion", ((Number) metadata.get("pkgVersion")).doubleValue());
        metadata.remove("collections");
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
        Map<String, Object> externalRequest = makeExtPropRequest(contentExt, Platform.config.getString("content.external_fields"));
        if(CollectionUtils.isNotEmpty(externalRequest.keySet())){
            Response extResp = systemUpdate(id, makeContentRequest(externalRequest), channel, true);
            if(!isSuccess(extResp)){
                throw new ServerException("ERR_CONTENT_SYNC","Error while updating external fields to content " + id +" in destination env : "+  extResp.getParams().getErrmsg());
            }
        }
    }

    private Map<String,Object> makeExtPropRequest(Response contentExt, String extProps) {
       Map<String, Object>metadata = (Map<String, Object>) contentExt.getResult().get("content");
        Map<String, Object> externalRequest = new HashMap<>();
        List<String> externalProps = Arrays.asList(extProps.split(","));
       for(String key : metadata.keySet()) {
           if (externalProps.contains(key))
               externalRequest.put(key, metadata.get(key));
       }
        return externalRequest;
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

    private void initialise() {
        totalSuccess = 0;
        totalSkipped = 0;
        totalFailed = 0;
    }
}
