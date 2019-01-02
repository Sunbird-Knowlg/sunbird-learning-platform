package org.ekstep.content.mgr.impl.operation.plugin;

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
import org.ekstep.taxonomy.mgr.impl.DummyBaseContentManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CopyOperation extends DummyBaseContentManager {

    @Autowired private HierarchyManager hierarchyManager;

    public Response copyContent(String contentId, Map<String, Object> requestMap, String mode) {
        Node existingNode = validateCopyContentRequest(contentId, requestMap, mode);

        String mimeType = (String) existingNode.getMetadata().get("mimeType");
        Map<String, String> idMap /*= new HashMap<>()*/;
        if (!isCollectionMimeType(mimeType)) {
        /*if (!StringUtils.equalsIgnoreCase(mimeType, "application/vnd.ekstep.content-collection")) {*/
            idMap = copyContentData(existingNode, requestMap);
        } else {
            idMap = copyCollectionContent(existingNode, requestMap, mode);
        }

        return OK("node_id", idMap);

    }

    /**
     * @param contentId
     * @param requestMap
     * @param mode
     */
    private Node validateCopyContentRequest(String contentId, Map<String, Object> requestMap, String mode) {

        validateOrThrowExceptionForEmptyKeys(requestMap, "Content", "createdBy", "createdFor", "organization");

        Node node = getContentNode(TAXONOMY_ID, contentId, mode);
        List<String> notCoppiedContent = null;
        if (Platform.config.hasPath("learning.content.type.not.copied.list")) {
            notCoppiedContent = Platform.config.getStringList("learning.content.type.not.copied.list");
        }
        if (notCoppiedContent != null && notCoppiedContent.contains(getContentTypeFrom(node)/*(String) node.getMetadata().get("contentType")*/)) {
            throw new ClientException(ContentErrorCodes.CONTENTTYPE_ASSET_CAN_NOT_COPY.name(),
                    "ContentType " + getContentTypeFrom(node)/*(String) node.getMetadata().get("contentType")*/ + " can not be coppied.");
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
    protected Map<String, String> copyContentData(Node existingNode, Map<String, Object> requestMap) {

        Node copyNode = copyMetdata(existingNode, requestMap);
        Response response = createDataNode(copyNode);
        if (checkError(response)) {
            throw new ServerException(response.getParams().getErr(), response.getParams().getErrmsg());
        }
        uploadArtifactUrl(existingNode, copyNode);
        uploadExternalProperties(existingNode, copyNode);

        Map<String, String> idMap = new HashMap<>();
        idMap.put(existingNode.getIdentifier(), copyNode.getIdentifier());
        return idMap;
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

        copyNode.setMetadata(metaData);
        copyNode.setGraphId(existingNode.getGraphId());

        copyNode.getMetadata().putAll(requestMap);
        copyNode.getMetadata().put("status", "Draft");
        List<String> nullPropList = Platform.config.getStringList("learning.content.copy.null_prop_list");
        Map<String, Object> nullPropMap = new HashMap<>();
        nullPropList.forEach(i -> nullPropMap.put(i, null));
        copyNode.getMetadata().putAll(nullPropMap);
        copyNode.getMetadata().put("origin", existingNode.getIdentifier());

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
        Map<String, String> idMap = copyContentData(existingNode, requestMap);
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
        DefinitionDTO definition = getDefinition(TAXONOMY_ID, CONTENT_OBJECT_TYPE);
        Map<String, Object> contentMap = util.getContentHierarchyRecursive(existingNode.getGraphId(), existingNode,
                definition, mode, true);

        Map<String, Object> updateRequest = prepareUpdateHierarchyRequest(
                (List<Map<String, Object>>) contentMap.get("children"), existingNode, idMap);

        Response response = this.hierarchyManager.update(updateRequest);
        if (checkError(response)) {
            throw new ServerException(response.getParams().getErr(), response.getParams().getErrmsg());
        }
    }

    private void uploadArtifactUrl(Node existingNode, Node copyNode) {
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
                    File file = copyURLToFile(artifactUrl);
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

        List<String> nullPropList = Platform.config.getStringList("learning.content.copy.null_prop_list");
        Map<String, Object> nullPropMap = new HashMap<>();
        nullPropList.forEach(i -> nullPropMap.put(i, null));
        Map<String, Object> parentHierarchy = new HashMap<>();
        parentHierarchy.put("children", new ArrayList<>());
        parentHierarchy.put("root", true);
        parentHierarchy.put("contentType", existingNode.getMetadata().get("contentType"));
        hierarchy.put(idMap.get(existingNode.getIdentifier()), parentHierarchy);
        populateHierarchy(children, nodesModified, hierarchy, idMap.get(existingNode.getIdentifier()), nullPropMap);

        Map<String, Object> data = new HashMap<>();
        data.put("nodesModified", nodesModified);
        data.put("hierarchy", hierarchy);

        return data;

    }

}
