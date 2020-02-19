package org.ekstep.content.mgr.impl.operation.plugin;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.content.mgr.impl.HierarchyManager;
import org.ekstep.content.mimetype.mgr.IMimeTypeManager;
import org.ekstep.content.mimetype.mgr.impl.BaseMimeTypeManager;
import org.ekstep.content.mimetype.mgr.impl.H5PMimeTypeMgrImpl;
import org.ekstep.content.util.MimeTypeManagerFactory;
import org.ekstep.graph.common.Identifier;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.learning.contentstore.ContentStoreParams;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

public class CopyOperation extends BaseContentManager {

    private final List<String> graphValidationErrors = Arrays.asList("ERR_GRAPH_ADD_NODE_VALIDATION_FAILED", "ERR_GRAPH_UPDATE_NODE_VALIDATION_FAILED");
    private final HierarchyManager hierarchyManager = new HierarchyManager();

    public Response copyContent(String contentId, Map<String, Object> requestMap, String mode, String translate) {
        Node existingNode = validateCopyContentRequest(contentId, requestMap, mode);
        return isCollectionMimeType((String) existingNode.getMetadata().get("mimeType")) ? 
        		OK("node_id", copyCollectionContent(existingNode, requestMap, mode)) :
        			OK("node_id", copyContentData(existingNode, requestMap, translate));
    }

    /**
     * @param contentId
     * @param requestMap
     * @param mode
     */
    private Node validateCopyContentRequest(String contentId, Map<String, Object> requestMap, String mode) {
       if (null == requestMap)
            throw new ClientException("ERR_INVALID_REQUEST", "Please provide valid request");

        validateOrThrowExceptionForEmptyKeys(requestMap, "Content", Arrays.asList("createdBy", "createdFor",
                "organisation", "framework"));

        Node node = getContentNode(TAXONOMY_ID, contentId, mode);
        List<String> notCoppiedContent = null;
        if (Platform.config.hasPath("learning.content.type.not.copied.list")) {
            notCoppiedContent = Platform.config.getStringList("learning.content.type.not.copied.list");
        }
        if (notCoppiedContent != null && notCoppiedContent.contains(getContentTypeFrom(node))) {
            throw new ClientException(ContentErrorCodes.CONTENTTYPE_ASSET_CAN_NOT_COPY.name(),
                    "ContentType " + getContentTypeFrom(node) + " can not be copied.");
        }

        String status = (String) node.getMetadata().get("status");
        List<String> invalidStatusList = Platform.config.getStringList("learning.content.copy.invalid_status_list");
        if (invalidStatusList.contains(status))
            throw new ClientException("ERR_INVALID_REQUEST",
                    "Cannot copy content in " + status.toLowerCase() + " status");

        return node;
    }

    /**
     * @param existingNode
     * @param requestMap
     * @return
     */
    protected Map<String, String> copyContentData(Node existingNode, Map<String, Object> requestMap, String translation) {
        Node copyNode = copyMetdata(existingNode, requestMap);
        Response response = createDataNode(copyNode);
        if (checkError(response)) {
            TelemetryManager.error("CopyContent: Error while creating new content: " + response.getParams().getErr() + " :: " + response.getParams().getErrmsg() + response.getResult());
            if(MapUtils.isNotEmpty(response.getResult()) && graphValidationErrors.contains(response.getParams().getErr()))
                throw new ServerException(response.getParams().getErr(), (response.getResult().toString()));
            else
                throw new ServerException(response.getParams().getErr(), response.getParams().getErrmsg());
        }
        uploadArtifactUrl(existingNode, copyNode);
        TelemetryManager.info("CopyContent: Uploaded artefact for Id: " + copyNode.getIdentifier());
        uploadExternalProperties(existingNode, copyNode);
        Map<String, String> idMap = new HashMap<>();
        idMap.put(existingNode.getIdentifier(), copyNode.getIdentifier());
        if(StringUtils.isNotBlank(translation) && isEcmlMimeType((String) existingNode.getMetadata().get("mimeType"))) {
            transateEcmlText(copyNode.getIdentifier());
        }
        return idMap;
    }

    private void transateEcmlText(String identifier) {
        String body = getContentBody(identifier);
        if(StringUtils.isNotBlank(body)){
            try {
                Map<String, Object> ecmlMap = objectMapper.readValue(body, Map.class);
                List<Map<String, Object>> stages = ((List<Map<String, Object>>)((Map<String, Object>)ecmlMap.get("theme")).get("stage"));
                for(Map<String, Object> stage: stages) {
                    List<Map<String, Object>> textList = (List<Map<String, Object>>) stage.get("org.ekstep.text");
                    if(CollectionUtils.isNotEmpty(textList)){
                        for(Map<String, Object> textMap: textList) {
                            if(MapUtils.isNotEmpty((Map<String, Object>)textMap.get("config"))) {
                                String textString = (String) ((Map<String, Object>)textMap.get("config")).get("__cData");
                                if(StringUtils.isNotBlank(textString)) {
                                    Map<String, Object> actualTextMap = objectMapper.readValue(textString, Map.class);
                                    String textToBeTranslated = (String) actualTextMap.get("text");
                                    if(StringUtils.isNotBlank(textToBeTranslated)){
                                        String translatedText = callAnuvadAPI(textToBeTranslated);
                                        if(StringUtils.isNotBlank(translatedText)){
                                            actualTextMap.put("text", translatedText);
                                            ((Map<String, Object>)textMap.get("config")).put("__cData", objectMapper.writeValueAsString(actualTextMap));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                updateContentProperties(identifier, new HashMap<String, Object>(){{put("body", objectMapper.writeValueAsString(ecmlMap));}});
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UnirestException e) {
                e.printStackTrace();
            }
        }
    }

    private String callAnuvadAPI(String textToBeTranslated) throws UnirestException, IOException {
        List<Map<String, Object>> request = Arrays.asList(new HashMap<String, Object>() {{
            put("src", textToBeTranslated);
            put("id", 56);
        }});
        HttpResponse<String> httpResponse = Unirest.post("http://50.1.0.11:3003/translator/translation_en").header("Content-Type", "application/x-www-form-urlencoded").body(request).asString();
        if(httpResponse.getStatus() == 200) {
            Map<String, Object> responseMap = objectMapper.readValue(httpResponse.getBody(), Map.class);
            String translatedString = (String) ((List<Map<String, Object>>)responseMap.get("response_body")).get(0).get("tgt");
            return translatedString;
        }else {
            return "";
        }
    }

    /**
     * @param existingNode
     * @param requestMap
     * @return
     */
    private Node copyMetdata(Node existingNode, Map<String, Object> requestMap) {
        String newId = Identifier.getIdentifier(existingNode.getGraphId(), Identifier.getUniqueIdFromTimestamp());
        Node copyNode = new Node(newId, existingNode.getNodeType(), existingNode.getObjectType());
        
        Map<String, Object> metaData = new HashMap<>();
        metaData.putAll(existingNode.getMetadata());
        
        Map<String, Object> originData = new HashMap<>();
        
        List<String> originNodeMetadataList = Platform.config.hasPath("learning.content.copy.origin_data")
        		? Platform.config.getStringList("learning.content.copy.origin_data") : null;
        if(CollectionUtils.isNotEmpty(originNodeMetadataList))
	        	originNodeMetadataList.forEach(meta -> {
	        		if(metaData.containsKey(meta))
	        			originData.put(meta, metaData.get(meta));
	        		});
        
        List<String> nullPropList = Platform.config.hasPath("learning.content.copy.props_to_remove")
        		? Platform.config.getStringList("learning.content.copy.props_to_remove"): null;

        // TODO: Remove the below loop in 2.3.0
        List<String> batchCountPropList = metaData.keySet().stream().filter(key -> key.endsWith("_batch_count")).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(batchCountPropList))
            nullPropList.addAll(batchCountPropList);

        if(CollectionUtils.isNotEmpty(nullPropList))
        		nullPropList.forEach(prop -> metaData.remove(prop));

        copyNode.setMetadata(metaData);
        copyNode.setGraphId(existingNode.getGraphId());
        copyNode.getMetadata().putAll(requestMap);
        copyNode.getMetadata().put("status", "Draft");
        copyNode.getMetadata().put("origin", existingNode.getIdentifier());
        if(MapUtils.isNotEmpty(originData))
        		copyNode.getMetadata().put("originData", originData);

        List<Relation> existingNodeOutRelations = existingNode.getOutRelations();
        List<Relation> copiedNodeOutRelations = new ArrayList<>();
        if (null != existingNodeOutRelations && !existingNodeOutRelations.isEmpty()) {
            for (Relation rel : existingNodeOutRelations) {
                if (!Arrays.asList("Content", "ContentImage").contains(rel.getEndNodeObjectType())) {
                    copiedNodeOutRelations.add(new Relation(newId, rel.getRelationType(), rel.getEndNodeId()));
                }
            }
        }
        copyNode.setOutRelations(copiedNodeOutRelations);

        return copyNode;
    }

    private void uploadExternalProperties(Node existingNode, Node copyNode) {
        DefinitionDTO definition = getDefinition(TAXONOMY_ID, CONTENT_OBJECT_TYPE);
        // Copy the externalProperties in cassandra
        List<String> externalPropsList = getExternalPropsList(definition);
        Response bodyResponse = getContentProperties(existingNode.getIdentifier(), externalPropsList);
        if (!checkError(bodyResponse)) {
            Map<String, Object> extValues = (Map<String, Object>) bodyResponse.get(ContentStoreParams.values.name());
            if (null != extValues && !extValues.isEmpty()) {
                updateContentProperties(copyNode.getIdentifier(), extValues);
            }
        }
    }

    /**
     * @param existingNode
     * @param requestMap
     * @return
     */
    private Map<String, String> copyCollectionContent(Node existingNode, Map<String, Object> requestMap, String mode) {
        // Copying Root Node
        Map<String, String> idMap = copyContentData(existingNode, requestMap, null);
        // Generating update hierarchy with copied parent content and calling
        // update hierarchy.
        copyHierarchy(existingNode, idMap, mode);
        return idMap;
    }

    /**
     * @param existingNode
     * @param idMap
     * @param mode
     */
    @SuppressWarnings("unchecked")
    private void copyHierarchy(Node existingNode, Map<String, String> idMap, String mode) {
        Response readResponse = hierarchyManager.getContentHierarchy(existingNode.getIdentifier(), null, mode, null);
        if(checkError(readResponse)) {
            TelemetryManager.error("CopyContent: Error while reading hierarchy: " + readResponse.getParams().getErr() + " :: " + readResponse.getParams().getErrmsg() + readResponse.getResult());
            if(MapUtils.isNotEmpty(readResponse.getResult()) && graphValidationErrors.contains(readResponse.getParams().getErr()))
                throw new ServerException(readResponse.getParams().getErr(), readResponse.getResult().toString());
            else
                throw new ServerException(readResponse.getParams().getErr(), readResponse.getParams().getErrmsg());
        }
        Map<String, Object> contentMap = (Map<String, Object>) readResponse.getResult().get("content");

        Map<String, Object> updateRequest = prepareUpdateHierarchyRequest(
                (List<Map<String, Object>>) contentMap.get("children"), existingNode, idMap);

        Response response = this.hierarchyManager.update(updateRequest);
        if (checkError(response)) {
            TelemetryManager.error("CopyContent: Error while updating hierarchy: " + response.getParams().getErr() + " :: " + response.getParams().getErrmsg() + response.getResult());
            if(MapUtils.isNotEmpty(response.getResult()) && graphValidationErrors.contains(response.getParams().getErr()))
                throw new ServerException(readResponse.getParams().getErr(), readResponse.getResult().toString());
            else
                throw new ServerException(readResponse.getParams().getErr(), readResponse.getParams().getErrmsg());
        }
    }

    private void uploadArtifactUrl(Node existingNode, Node copyNode) {
        File file =null;
        try {
            String artifactUrl = (String) existingNode.getMetadata().get("artifactUrl");
            if (StringUtils.isNotBlank(artifactUrl)) {
                Response response = null;
                String mimeType = (String) copyNode.getMetadata().get("mimeType");
                String contentType = (String) copyNode.getMetadata().get("contentType");

                if (!isEcmlMimeType(mimeType) || isCollectionMimeType(mimeType)) {
            /*if (!(StringUtils.equalsIgnoreCase("application/vnd.ekstep.ecml-archive", mimeType)
                    || StringUtils.equalsIgnoreCase("application/vnd.ekstep.content-collection", mimeType))) {*/
                    IMimeTypeManager mimeTypeManager = MimeTypeManagerFactory.getManager(contentType, mimeType);
                    BaseMimeTypeManager baseMimeTypeManager = new BaseMimeTypeManager();

                    if (baseMimeTypeManager.isS3Url(artifactUrl)) {
                        file = copyURLToFile(artifactUrl);
                        if (isH5PMimeType(mimeType)) {
                            H5PMimeTypeMgrImpl h5pManager = new H5PMimeTypeMgrImpl();
                            response = h5pManager.upload(copyNode.getIdentifier(), copyNode, true, file);
                        } else {
                            response = mimeTypeManager.upload(copyNode.getIdentifier(), copyNode, file, false);
                        }

                    } else {
                        response = mimeTypeManager.upload(copyNode.getIdentifier(), copyNode, artifactUrl);
                    }

                    if (null == response || checkError(response)) {
                        throw new ClientException("ARTIFACT_NOT_COPIED", "ArtifactUrl not coppied.");
                    }
                }

            }
        } finally {
            if(null != file && file.exists())
                file.delete();
        }
    }

    protected File copyURLToFile(String fileUrl) {
        try {
            String fileName = getFileNameFromURL(fileUrl);
            File file = new File(fileName);
            FileUtils.copyURLToFile(new URL(fileUrl), file);
            return file;
        } catch (IOException e) {
            throw new ClientException(TaxonomyErrorCodes.ERR_INVALID_UPLOAD_FILE_URL.name(), "fileUrl is invalid.");
        }
    }

    protected String getFileNameFromURL(String fileUrl) {
        String fileName = FilenameUtils.getBaseName(fileUrl) + "_" + System.currentTimeMillis();
        if (!FilenameUtils.getExtension(fileUrl).isEmpty())
            fileName += "." + FilenameUtils.getExtension(fileUrl);
        return fileName;
    }

    /**
     * @param existingNode
     * @param idMap
     * @return
     */
    private Map<String, Object> prepareUpdateHierarchyRequest(List<Map<String, Object>> children, Node existingNode,
                                                              Map<String, String> idMap) {
        Map<String, Object> nodesModified = new HashMap<>();
        Map<String, Object> hierarchy = new HashMap<>();

        Map<String, Object> parentHierarchy = new HashMap<>();
        parentHierarchy.put("children", new ArrayList<>());
        parentHierarchy.put("root", true);
        parentHierarchy.put("contentType", existingNode.getMetadata().get("contentType"));
        hierarchy.put(idMap.get(existingNode.getIdentifier()), parentHierarchy);
        populateHierarchy(children, nodesModified, hierarchy, idMap.get(existingNode.getIdentifier()));

        Map<String, Object> data = new HashMap<>();
        data.put("nodesModified", nodesModified);
        data.put("hierarchy", hierarchy);

        return data;

    }

    /**
     * @param children
     * @param nodesModified
     * @param hierarchy
     */
    private void populateHierarchy(List<Map<String, Object>> children, Map<String, Object> nodesModified,
                                     Map<String, Object> hierarchy, String parentId) {
        List<String> nullPropList = Platform.config.getStringList("learning.content.copy.props_to_remove");
        if (null != children && !children.isEmpty()) {
            for (Map<String, Object> child : children) {
                String id = (String) child.get("identifier");
                if (equalsIgnoreCase("Parent", (String) child.get("visibility"))) {
                    // NodesModified and hierarchy
                    id = UUID.randomUUID().toString();
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.putAll(child);
                    nullPropList.forEach(prop -> metadata.remove(prop));
                    metadata.put("children", new ArrayList<>());
                    metadata.remove("identifier");
                    metadata.remove("parent");
                    metadata.remove("index");
                    metadata.remove("depth");

                    // TBD: Populate artifactUrl

                    Map<String, Object> modifiedNode = new HashMap<>();
                    modifiedNode.put("metadata", metadata);
                    modifiedNode.put("root", false);
                    modifiedNode.put("isNew", true);
                    nodesModified.put(id, modifiedNode);
                }
                Map<String, Object> parentHierarchy = new HashMap<>();
                parentHierarchy.put("children", new ArrayList<>());
                parentHierarchy.put("root", false);
                parentHierarchy.put("contentType", child.get("contentType"));
                hierarchy.put(id, parentHierarchy);
                ((List) ((Map<String, Object>) hierarchy.get(parentId)).get("children")).add(id);

                populateHierarchy((List<Map<String, Object>>) child.get("children"), nodesModified, hierarchy, id);
            }
        }
    }

}
