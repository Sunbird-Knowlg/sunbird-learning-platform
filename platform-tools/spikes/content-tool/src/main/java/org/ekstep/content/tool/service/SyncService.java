package org.ekstep.content.tool.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component("contentSyncService")
public class SyncService extends BaseService implements ISyncService {

    @Override
    public void dryRun() {

    }

    @Override
    public void ownerMigration(String createdBy, String channel, String[] createdFor, String[] organisation, String creator, String filter, String dryRun) {
        try {
            if (validChannel(channel)) {
                Map<String, Object> identifiers = getFromSource(filter);
                if (StringUtils.equalsIgnoreCase(dryRun, "true")) {
                    System.out.println("content count to migrate: " + identifiers.keySet().size() + " " + identifiers.keySet());
                } else {
                    updateOwnership(identifiers, createdBy, channel, createdFor, organisation, creator);

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
    public void sync(String filter, String dryRun) {
        try {
            Map<String, Object> identifiers = getFromSource(filter);
            Map<String, Object> response = syncData(identifiers);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateOwnership(Map<String, Object> identifiers, String createdBy, String channel, String[] createdFor, String[] organisation, String creator) throws Exception {
        if (!identifiers.isEmpty()) {
            Map<String, Object> request = getUpdateRequest(createdBy, channel, createdFor, organisation, creator);
            Map<String, Object> response = updateData(identifiers, request, channel);

            System.out.println("Migrated content count: " + ((List<String>) response.get("success")).size() + " : " + response.get("success"));
            System.out.println("Skipped content count: " + ((List<String>) response.get("failed")).size() + " : " + response.get("failed"));

        } else {
            System.out.println("No contents to migrate");
        }
    }

    private Map<String, Object> updateData(Map<String, Object> identifiers, Map<String, Object> request, String channel) throws Exception {
        Map<String, Object> output = new HashMap<>();
        List<String> successful = new ArrayList<>();
        List<String> failure = new ArrayList<>();

        for (String id : identifiers.keySet()) {
            Response readResponse = getContent(id, true); //executeGet(destUrl + "/content/" + destVersion + "/read/" + id, destKey);
            Response sourceContent = getContent(id, false);
            if (isSuccess(readResponse)) {
                if (0 == Double.compare((Double) identifiers.get(id), (Double) ((Map<String, Object>) readResponse.get("content")).get("pkgVersion"))) {
                    if(!request.isEmpty()) {
                        Response updateResponse = systemUpdate(id, request, channel, true); //executePatch(destUrl + "/system/" + destVersion + "/content/update/" + id, destKey, request, channel);
                        Response updateSourceResponse = systemUpdate(id, request, channel, false); //executePatch(sourceUrl + "/system/" + sourceVersion + "/content/update/" + id, sourceKey, request, channel);

                        Map<String, Object> children = new HashMap<>();
                        fetchChildren(readResponse, children);
                        if (!children.isEmpty())
                            updateData(children, request, channel);
                        if (isSuccess(updateResponse) && isSuccess(updateSourceResponse)) {
                            successful.add(id);
                        } else {
                            failure.add(id);
                        }
                    }
                    downloadArtifact(i)
                    //uploadArtifact();
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

    private void fetchChildren(Response readResponse, Map<String, Object> children) throws Exception {
        List<Map<String, Object>> childNodes = (List<Map<String, Object>>) readResponse.get("children");

        if (!CollectionUtils.isEmpty(childNodes)) {
            for (Map<String, Object> child : childNodes) {
                Response childContent = executeGet(destUrl + "/content" + destVersion + "/read" + child.get("identifier"), destKey);
                String visibility = (String) childContent.get("visibility");

                if (StringUtils.isNotBlank(visibility) && StringUtils.equalsIgnoreCase("Parent", visibility)) {
                    children.put(childContent.getId(), childContent.get("pkgVersion"));
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
        if (createdFor.length > 0)
            metadata.put("createdFor", Arrays.asList(createdFor));
        if (organisation.length > 0)
            metadata.put("organization", Arrays.asList(organisation));
        if (StringUtils.isNotBlank(creator))
            metadata.put("creator", creator);

        Map<String, Object> content = new HashMap<>();
        content.put("content", metadata);

        Map<String, Object> request = new HashMap<>();
        request.put("request", content);

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
    private Map<String, Object> syncData(Map<String, Object> identifiers) {
        Map<String, Object> response = new HashMap<>();
        List<String> success = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        for (String id : identifiers.keySet()) {
            try {
                Response destContent = getContent(id, true);//executeGet(destUrl + "/content/" + destVersion + "/read/" + id + "fields=identifier,pkgVersion", destKey);
                if (isSuccess(destContent)) {
                    Response sourceContent = getContent(id, false);//executeGet(sourceUrl + "/content/" + sourceVersion + "/read/" + id, sourceKey);
                    if (Double.compare(((Double) destContent.get("pkgVersion")), ((Double) sourceContent.get("pkgVersion"))) == -1) {
                        updateMetadata(sourceContent);
                        synchierarchy(id);
                        success.add(id);
                    } else {
                        failed.add(id);
                    }

                } else {
                    createContent(id);
                    synchierarchy(id);
                    success.add(id);
                }
            } catch (Exception e) {
                failed.add(id);
            }

        }
        return response;

    }

    private void createContent(String id) throws Exception {
        Response sourceContent = getContent(id, false); //executeGet(sourceUrl + "/content/" + sourceVersion + "/read/" + id, sourceKey);
        Map<String, Object> metadata = (Map<String, Object>) sourceContent.get("content");
        //cleanMetadata(metadata);
        String channel = (String) metadata.get("channel");
        Map<String, Object> content = new HashMap<>();
        content.put("content", metadata);
        Map<String, Object> request = new HashMap<>();
        request.put("request", content);

        Response response = systemUpdate(id, request, channel, true);//executePatch(destUrl + "/system/" + destVersion + "/content/update/"+ id, destKey, request, channel);

        if (isSuccess(response)) {
            String localPath  = downloadArtifact(id, (String) metadata.get("artifactUrl"), sourceStorageType);
            if(StringUtils.equalsIgnoreCase("application/vnd.ekstep.ecml-archive", (String) metadata.get("mimeType")))
                copyAssets(localPath);
            //uploadArtifact(id);
            List<Map<String, Object>> children = (List<Map<String, Object>>) metadata.get("children");

            for (Map<String, Object> child : children) {
                createContent((String) child.get("identifier"));
            }
        }

    }

    private void copyAssets(String localPath) throws Exception {
       Map<String, Object> assets =  readECMLFile(localPath + "/index.ecml");
       for(String assetId: assets.keySet()) {
           Response destAsset = getContent(assetId, true);
           if(!isSuccess(destAsset)) {
                Response sourceAsset = getContent(assetId, false);
                if(isSuccess(sourceAsset)){
                    Map<String, Object> assetRequest = (Map<String, Object>) sourceAsset.get("content");
                    assetRequest.remove("variants");
                    Response response = uploadAsset(localPath + assets.get(assetId), assetId);


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

    private void synchierarchy(String id) throws Exception {
        executePost(destUrl + "/content/" + destVersion + "/hierarchy/sync/" + id, destKey, new HashMap<>(), null);
    }

    private void updateMetadata(Response sourceContent) throws Exception {
        Map<String, Object> metadata = (Map<String, Object>) sourceContent.get("content");

        String channel = (String) metadata.get("channel");
        Map<String, Object> content = new HashMap<>();
        content.put("content", metadata);
        Map<String, Object> request = new HashMap<>();
        request.put("request", content);

        Response response = systemUpdate((String) metadata.get("identifier"), request, channel, true); //executePatch(destUrl + "/system/" + destVersion + "/content/update/", destKey, request, channel);

    }

}
