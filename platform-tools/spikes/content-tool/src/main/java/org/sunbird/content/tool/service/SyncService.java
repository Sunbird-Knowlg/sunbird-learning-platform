package org.sunbird.content.tool.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ServerException;
import org.sunbird.content.tool.util.Input;
import org.sunbird.content.tool.util.InputList;
import org.sunbird.content.tool.util.ShellCommandUtils;
import org.sunbird.telemetry.logger.TelemetryManager;
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
                        System.out.println("No contents to migrate");
                    }
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
                System.out.println("Data count to sync: " + count + "\n" + "Data : \n" + inputList.toString());
            } else {
                if(count > 0) {
                    System.out.println("Total No. of data : " + count);
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
        switch(input.getObjectType()) {
            case "Content" : return migrateContent(input, request, channel, forceUpdate);
            case "AssessmentItem": return migrateQuestion(input, request, channel);
            default: return false;
        }

    }

    private boolean migrateQuestion(Input input, Map<String, Object> request, String channel) throws Exception {
        ShellCommandUtils.print(input.getId() + " Fetching Question from destination...");
        Response destQuestion = readQuestion(input.getId(), true);
        if(isSuccess(destQuestion)) {
            ShellCommandUtils.print(input.getId() + " Fetched Question from destination...");
            ShellCommandUtils.print(input.getId() + " Fetching Question from source...");
            Response sourceQuestion = readQuestion(input.getId(), false);
            if(isSuccess(sourceQuestion) && (!request.isEmpty())) {
                ShellCommandUtils.print(input.getId() + " Fetched Question from source...");
                ShellCommandUtils.print(input.getId() + " Preparing the update Request...");
                Map<String, Object> questionRequest = prepareQuestionRequest(sourceQuestion);
                if(StringUtils.isBlank(channel)) {
                    channel = (String) ((Map<String, Object>)destQuestion.getResult().get("assessment_item")).get("channel");
                }
                String createdBy = (String) ((Map<String, Object>)((Map<String, Object>)request.get("request")).get("content")).get("createdBy");
                ((Map<String, Object>)((Map<String, Object>)((Map<String, Object>)questionRequest.get("request")).get("assessment_item")).get("metadata")).put("createdBy", createdBy);
                ShellCommandUtils.print(input.getId() + " Prepared the update Request...");
                ShellCommandUtils.print(input.getId() + " Updating question in destination...");
                Response destUpdate = updateQuestion(input.getId(),questionRequest, channel, true);
                ShellCommandUtils.print(input.getId() + " Updated question in destination...");
                ShellCommandUtils.print(input.getId() + " Updating question in source...");
                Response sourceUpdate = updateQuestion(input.getId(), questionRequest, channel, false);
                ShellCommandUtils.print(input.getId() + " Updated question in source...");
                return (isSuccess(destUpdate) && isSuccess(sourceUpdate));
            }else{
                ShellCommandUtils.print(input.getId() + " Fetching Question from source failed or request is empty...");
                return false;
            }
        }else{
            ShellCommandUtils.print(input.getId() + " Question not present in destination... Sync in Progress...");
            syncQuestion(input);
            ShellCommandUtils.print(input.getId() + " Question Synced...");
            return migrateQuestion(input, request, channel);
        }
    }

    private boolean migrateContent(Input input, Map<String, Object> request, String channel, String forceUpdate) throws Exception {
        ShellCommandUtils.print(input.getId() + " Fetching Content from destination...");
        Response readResponse = getContent(input.getId(), true, null);
        if (isSuccess(readResponse)) {
            ShellCommandUtils.print(input.getId() + " Fetched Content from destination...");
            Map<String, Object> destContent = (Map<String, Object>) readResponse.get("content");
            double srcPkgVersion = input.getPkgVersion();
            double destPkgVersion = (null!= destContent.get("pkgVersion"))? ((Number) destContent.get("pkgVersion")).doubleValue(): 0d;
            if (isForceupdate(forceUpdate) || (0 == Double.compare(srcPkgVersion, destPkgVersion))) {
                if (!request.isEmpty()) {
                    if(StringUtils.isBlank(channel)) {
                        channel = (String) destContent.get("channel");
                    }
                    ShellCommandUtils.print(input.getId() + " Updating content in destination...");
                    Response updateResponse = systemUpdate(input.getId(), request, channel, true);
                    ShellCommandUtils.print(input.getId() + " Updated content in destination...");
                    ShellCommandUtils.print(input.getId() + " Updating content in source...");
                    Response updateSourceResponse = systemUpdate(input.getId(), request, channel, false);
                    ShellCommandUtils.print(input.getId() + " Updated content in source...");

                    if (isSuccess(updateResponse) && isSuccess(updateSourceResponse)) {
                        Response response = getContent(input.getId(), false, null);
                        ShellCommandUtils.print(input.getId() + " Updating ecarURls information...");
                        updateEcarInfo(input.getId(), (Map<String, Object>) response.get("content"));
                        ShellCommandUtils.print(input.getId() + " Updated ecarURls information...");
                    } else {
                        ShellCommandUtils.print(input.getId() + " Updating content failed...");
                        return false;
                    }
                }else{
                    ShellCommandUtils.print(input.getId() + " Updating ecarURls information...");
                    updateEcarInfo(input.getId(), (Map<String, Object>) readResponse.get("content"));
                    ShellCommandUtils.print(input.getId() + " Updated ecarURls information...");
                }
                InputList children = new InputList( new ArrayList<>());
                fetchChildren(readResponse, children);
                if (children.isNotEmpty())
                    ShellCommandUtils.print(input.getId() + " Updating children in destination...");
                    updateData(children, request, channel, forceUpdate);
                    ShellCommandUtils.print(input.getId() + " Updated children in destination...");
                if (StringUtils.equalsIgnoreCase(COLLECTION_MIMETYPE, (String) destContent.get("mimeType")))
                    ShellCommandUtils.print(input.getId() + " Syncing Hierarchy updates...");
                    syncHierarchy(input.getId());
                    ShellCommandUtils.print(input.getId() + " Synced Hierarchy updates...");
                if (containsItemsSet(destContent)) {
                    ShellCommandUtils.print(input.getId() + " Copying Questions...");
                    copyAssessmentItems((List<Map<String, Object>>) destContent.get("questions"));
                    ShellCommandUtils.print(input.getId() + " Copied Questions...");
                }
                return true;
            } else if (-1 == Double.compare(srcPkgVersion, destPkgVersion)) {
                ShellCommandUtils.print(input.getId() + " Syncing content before migration...");
                sync(input, forceUpdate);
                ShellCommandUtils.print(input.getId() + " Synced content before migration...");
                return migrateOwner(input, request, channel, forceUpdate);
            } else {
                ShellCommandUtils.print(input.getId() + " Failed migration...");
                return false;
            }
        } else {
            ShellCommandUtils.print(input.getId() + " Syncing content before migration...");
            sync(input, "true");
            ShellCommandUtils.print(input.getId() + " Synced content before migration...");
            return migrateOwner(input, request, channel, "true");
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
        switch(input.getObjectType()) {
            case "Content":
                return syncContent(input, forceUpdate);
            case "AssessmentItem":
                return syncQuestion(input);
            default:
                return false;
        }
    }

    private boolean syncQuestion(Input input) throws Exception {
       return  copyQuestion(input.getId());
    }

    private boolean syncContent(Input input, String forceUpdate) throws Exception {
        Response destContent = getContent(input.getId(), true, null);
        if (isSuccess(destContent)) {
            Response sourceContent = getContent(input.getId(), false, null);
            if (isForceupdate(forceUpdate) || (Double.compare(((Number) ((Map<String, Object>) destContent.get("content")).get("pkgVersion")).doubleValue(), ((Number) ((Map<String, Object>) sourceContent.get("content")).get("pkgVersion")).doubleValue()) == -1)) {
                ShellCommandUtils.print(input.getId() + " updating metadata...");
                updateMetadata(sourceContent, forceUpdate);
                ShellCommandUtils.print(input.getId() + " update metadata complete...");
                return true;
            } else {
                return false;
            }

        } else {
            ShellCommandUtils.print(input.getId() + " creating content...");
            createContent(input.getId(), forceUpdate);
            ShellCommandUtils.print(input.getId() + " create content complete...");
            return true;
        }

    }

    private void createContent(String id, String forceUpdate) throws Exception {
        Response sourceContent = getContent(id, false, null);
        Map<String, Object> metadata = (Map<String, Object>) sourceContent.get("content");

        //Fix for SB-12281
        System.out.println("metadata before conversion:" + metadata);
        changeAttributeTypeFromStringToList(metadata, "subject");
        changeAttributeTypeFromStringToList(metadata, "medium");
        System.out.println("metadata after conversion:" + metadata);
        System.out.println("contentRequest:" + makeContentRequest(metadata));

        String channel = (String) metadata.get("channel");
        if(null != metadata.get("pkgVersion"))
            metadata.put("pkgVersion", ((Number) metadata.get("pkgVersion")).doubleValue());
        metadata.remove("collections");
        Response response = systemUpdate(id, makeContentRequest(metadata), channel, true);
        if (isSuccess(response)) {
            ShellCommandUtils.print(id + " updating external props of content...");
            updateExternalProps(id, channel);
            ShellCommandUtils.print( id + " updated external props of content...");
            updateMimeType(id, metadata, forceUpdate);
        }else{
            throw new ServerException("ERR_CONTENT_SYNC","Error while creating content " + id +" in destination env : "+  response.getParams().getErrmsg());
        }

    }

    private void changeAttributeTypeFromStringToList(Map<String, Object> metadata, String attribute) {
        String value = (String) metadata.get(attribute);
        List<String> valueList = new ArrayList();
        if(null != value) {
            valueList.add(value);
        }
        metadata.put(attribute, valueList);
    }

    private void updateMimeType(String id, Map<String, Object> metadata, String forceUpdate) throws Exception {
        String localPath = null;
        try {
            String mimeType = (String) metadata.get("mimeType");
            double pkgVersion = (null!= metadata.get("pkgVersion"))?((Number) metadata.get("pkgVersion")).doubleValue(): 0d;
            String channel = (String) metadata.get("channel");
            switch (mimeType) {
                case "application/vnd.ekstep.ecml-archive":
                    localPath = cloudStoreManager.downloadArtifact(id, (String) metadata.get("artifactUrl"), true);
                    ShellCommandUtils.print(id + " copying content assets...");
                    copyAssets(localPath, forceUpdate);
                    ShellCommandUtils.print(id + " copied content assets...");
                    break;
                case "application/vnd.ekstep.content-collection":
                    List<Map<String, Object>> children = (List<Map<String, Object>>) metadata.get("children");
                    if (CollectionUtils.isNotEmpty(children)) {
                        ShellCommandUtils.print(id + " creating children of the content...");
                        List<Map<String, String>> childrenReq = new ArrayList<>();
                        for (Map<String, Object> child : children) {
                            String childId = (String) child.get("identifier");
                            createContent(childId, forceUpdate);
                            Map<String, String> childReq = new HashMap<>();
                            childReq.put("identifier", childId);
                            childrenReq.add(childReq);
                        }
                        if(CollectionUtils.isNotEmpty(childrenReq)) {
                            Map<String, Object> childRequest = new HashMap<>();
                            childRequest.put("children", childrenReq);
                            systemUpdate(id, makeContentRequest(childRequest), channel, true);
                        }
                        syncHierarchy(id);
                        ShellCommandUtils.print(id + " created children of the content...");
                    }
                    break;
                case "application/vnd.ekstep.h5p-archive":
                case "application/vnd.ekstep.html-archive":
                    ShellCommandUtils.print(id + " uploading h5p/html artifactUrl of the content...");
                    cloudStoreManager.uploadAndExtract(id, (String) metadata.get("artifactUrl"), mimeType, pkgVersion);
                    ShellCommandUtils.print(id + " uploaded h5p/html artifactUrl of the content...");
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
        systemUpdate(id, urlUpdateReq, (String) metadata.get("channel"), false);
    }

    private boolean isForceupdate(String forceUpdate) {
        return StringUtils.equalsIgnoreCase("true", forceUpdate);
    }

    private boolean containsItemsSet(Map<String, Object> content) {
        return CollectionUtils.isNotEmpty((List<Map<String, Object>>) content.get("questions"));
    }

    private void copyAssessmentItems(List<Map<String, Object>> questions) throws Exception {
        if(CollectionUtils.isNotEmpty(questions)){
            for(Map<String, Object> question: questions) {
                copyQuestion( (String) question.get("identifier"));
            }
        }
    }

    private boolean copyQuestion(String id) throws Exception {
        Response destQuestion = readQuestion(id, true);
        if(!isSuccess(destQuestion)) {
            Response sourceQuest = readQuestion(id, false);
            if(isSuccess(sourceQuest)){
                Map<String, Object> request = prepareQuestionRequest(sourceQuest);
                String channel = (String) ((Map<String, Object>)((Map<String, Object>)((Map<String, Object>)request.get("request")).get("assessment_item")).get("metadata")).get("channel");
                Response createResp = createQuestion(request, channel, true);
                if(!isSuccess(createResp)){
                    TelemetryManager.error("Error while creating Questions : " + createResp.getParams().getErrmsg() + " : "  + createResp.getResult());
                    return false;
                }
                return true;
            }
            else{
                TelemetryManager.error("Error while fetcing Questions " + id + " from source : " + sourceQuest.getParams().getErrmsg() + " : "  + sourceQuest.getResult());
                return false;
            }
        }else{
            TelemetryManager.info("Question with ID " + id + " already exists in destination");
            return false;
        }
    }

    private Map<String,Object> prepareQuestionRequest(Response sourceQuest) {
        Map<String, Object> question = (Map<String, Object>) sourceQuest.get("assessment_item");
        Map<String, Object> metadata = new HashMap<>(question);
        metadata.remove("identifier");
        metadata.remove("objectType");
        metadata.remove("concepts");
        metadata.remove("subject");

        if(null != question.get("concepts")){
            List<Map<String, Object>> concepts = (List<Map<String, Object>>) question.get("concepts");
            List<Map<String, Object>> outRelations = new ArrayList<>();
            for(Map<String, Object> concept : concepts) {
                Map<String, Object> relation = new HashMap<>();
                relation.put("endNodeId", concept.get("identifier"));
                relation.put("relationType", concept.get("relation"));
                outRelations.add(relation);
            }
            question.put("outRelations", outRelations);
            question.remove("concepts");
        }
        Map<String, Object> item = new HashMap<>();
        item.put("identifier", question.get("identifier"));
        item.put("objectType", "AssessmentItem");
        item.put("metadata", metadata);
        item.put("outRelations", question.get("outRelations"));

        Map<String, Object> assessmentItem = new HashMap<>();
        assessmentItem.put("assessment_item", item);

        Map<String, Object> request = new HashMap<>();
        request.put("request", assessmentItem);
        return request;
    }


    private void fetchChildren(Response readResponse, InputList children) throws Exception {
        List<Map<String, Object>> childNodes = (List<Map<String, Object>>) ((Map<String, Object>) readResponse.get("content")).get("children");
        if (!CollectionUtils.isEmpty(childNodes)) {
            for (Map<String, Object> child : childNodes) {
                Response childContent = getContent((String) child.get("identifier"), true, null);
                Map<String, Object> contenMetadata = (Map<String, Object>) childContent.get("content");
                String visibility = (String) contenMetadata.get("visibility");

                if (StringUtils.isNotBlank(visibility) && StringUtils.equalsIgnoreCase("Parent", visibility)) {
                    double pkgVersion = (null != contenMetadata.get("pkgVersion"))?((Number) contenMetadata.get("pkgVersion")).doubleValue():0d;
                    Input childInput = new Input((String) contenMetadata.get("identifier"), (String) contenMetadata.get("name"), pkgVersion, (String) contenMetadata.get("objectType"), (String) contenMetadata.get("status"));
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
            metadata.put("organisation", Arrays.asList(organisation));
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
