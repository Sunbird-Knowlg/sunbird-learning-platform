package org.ekstep.jobs.samza.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.util.RequestValidatorUtil;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.enums.RelationTypes;
import org.ekstep.graph.dac.enums.SystemProperties;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.jobs.samza.exception.PlatformErrorCodes;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.FailedEventsUtil;
import org.ekstep.jobs.samza.util.JSONUtils;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.learning.hierarchy.store.HierarchyStore;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.learning.util.ControllerUtil;


import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class CollectionMigrationService implements ISamzaService {

    protected static final String DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX = ".img";
    private static JobLogger LOGGER = new JobLogger(CollectionMigrationService.class);
    private Config config = null;
    private ObjectMapper mapper = new ObjectMapper();
    private SystemStream systemStream = null;
    private ControllerUtil util = new ControllerUtil();
    private HierarchyStore hierarchyStore = null;
    private List<String> publishedStatus = Arrays.asList("Live", "Unlisted");
    private final String COLLECTION_MIGRATION = "collection-migration";
    private final String ECML_MIGRATION = "ecml-migration";
    private EcmlMigrationService migrationService;
    private String downloadFileLocation;

    public void initialize(Config config) throws Exception {
        this.config = config;
        JSONUtils.loadProperties(config);
        LOGGER.info("Service config initialized.");
        LearningRequestRouterPool.init();
        LOGGER.info("Akka actors initialized");
        systemStream = new SystemStream("kafka", config.get("output.failed.events.topic.name"));
        LOGGER.info("Stream initialized for Failed Events");
        hierarchyStore = new HierarchyStore();
        migrationService = new EcmlMigrationService();
        downloadFileLocation = Platform.config.hasPath("tmp.download.directory") ? Platform.config.getString("tmp.download.directory") : "/data/tmp/temp/";
    }

    @Override
    public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector) throws Exception {
        if (null == message) {
            LOGGER.info("Ignoring the message because it is not valid for collection migration.");
            return;
        }
        Map<String, Object> edata = (Map<String, Object>) message.get("edata");
        Map<String, Object> object = (Map<String, Object>) message.get("object");

        if (!validateEdata(edata) || null == object) {
            LOGGER.info("Ignoring the message because it is not valid for collection migration.");
            return;
        }
        switch ((String) edata.get("action")) {
            case COLLECTION_MIGRATION: {
                migrateCollections(message, metrics, collector, object);
                break;
            }
            case ECML_MIGRATION: {
                migrateECML(message, metrics, object);
                break;
            }
            default:
                LOGGER.info("Ecml migration is already done");
                break;
        }
    }

    private void migrateCollections(Map<String, Object> message, JobMetrics metrics,
                                    MessageCollector collector, Map<String, Object> object) throws Exception {
        try {
            boolean migrationSuccess = true;
            String nodeId = (String) object.get("id");
            if (StringUtils.isNotBlank(nodeId)) {
                Node node = getNode(nodeId);
                if (null != node && validNode(node)) {
                    Number version = (Number) node.getMetadata().get("version");
                    if (version != null && version.intValue() >= 2) {
                        LOGGER.info("Migration is already completed for Content ID: " + node.getIdentifier() + ". So, skipping this message.");
                        return;
                    }

                    List<String> idsToDelete = new ArrayList<>();
                    LOGGER.info("Initializing migration for collection ID: " + node.getIdentifier());
                    DefinitionDTO definition = getDefinition("domain", "Content");

                    // Live Collection Node hierarchy validation and identifying nodes to delete.
                    Node liveNode = util.getNode("domain", nodeId);
                    String status = (String) liveNode.getMetadata().get("status");
                    if (publishedStatus.contains(status)) {
                        Map<String, Object> liveHierarchy = hierarchyStore.getHierarchy(nodeId);
                        if (MapUtils.isEmpty(liveHierarchy)) {
                            LOGGER.info("LIVE MIGRATION: Hierarchy not available for Live Collection. Migrating: " + liveNode.getIdentifier());
                            liveHierarchy = util.getHierarchyMap(liveNode.getGraphId(), nodeId, definition, null, null);
                            this.hierarchyStore.saveOrUpdateHierarchy(nodeId, liveHierarchy);
                            LOGGER.info("LIVE MIGRATION: completed for Live Collection: " + nodeId);
                        } else {
                            LOGGER.info("Hierarchy available for Live Collection: " + liveNode.getIdentifier());
                        }
                        List<Map<String, Object>> children = (List<Map<String, Object>>) liveHierarchy.get("children");
                        getCollectionIdsToDelete(children, idsToDelete);
                    }


                    // Edit Node data structure migration and identifying nodes to delete.
                    LOGGER.info("Fetching hierarchy from Neo4J DB for Collection: " + node.getIdentifier());
                    Map<String, Object> editHierarchy = util.getHierarchyMap("domain", node.getIdentifier(), definition, "edit", null);
                    LOGGER.info("Got hierarchy data from Neo4J DB for Collection: " + node.getIdentifier());
                    if (MapUtils.isNotEmpty(editHierarchy)) {
                        String nodeImgId = nodeId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
                        List<Map<String, Object>> children = (List<Map<String, Object>>) editHierarchy.get("children");
                        getCollectionIdsToDelete(children, idsToDelete);
                        LOGGER.info("Check for image node hierarchy already exist or not.");
                        if (MapUtils.isEmpty(hierarchyStore.getHierarchy(nodeImgId))) {
                            LOGGER.info("Saving hierarchy to Cassandra.");
                            updateCollectionsInHierarchy(children);
                            Map<String, Object> hierarchy = new HashMap<>();
                            hierarchy.put("identifier", nodeId);
                            hierarchy.put("children", children);
                            hierarchyStore.saveOrUpdateHierarchy(nodeImgId, hierarchy);
                            LOGGER.info("Saved hierarchy to Cassandra.");
                        } else {
                            LOGGER.info("SKIPPED CASSANDRA SAVE: Hierarchy already exists for image content: " + nodeImgId);
                        }

                        // Deleting all the collection nodes with visibility: Parent.
                        LOGGER.info("Total number of collections to delete: " + idsToDelete.size());
                        if (idsToDelete.size() > 0) {
                            List<String> liveIds = new ArrayList<>();
                            for (String id : idsToDelete) {
                                if (StringUtils.endsWith(id, DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX)) {
                                    liveIds.add(id.replace(DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX, ""));
                                }
                            }
                            idsToDelete.addAll(liveIds);
                            LOGGER.info("Nodes to delete: " + mapper.writeValueAsString(idsToDelete));
                            List<Response> delResponses = idsToDelete.stream().distinct()
                                    .map(id -> {
                                        return util.deleteNode("domain", id);
                                    }).collect(Collectors.toList());
                            LOGGER.info("Nodes delete status: " + new ObjectMapper().writeValueAsString(delResponses));
                            List<Response> failed = delResponses.stream()
                                    .filter(res -> res.getResponseCode() != ResponseCode.OK)
                                    .collect(Collectors.toList());
                            if (failed.size() > 0) {
                                migrationSuccess = false;
                            }
                        }


                        // Relations update for Live Node.
                        version = (Number) liveNode.getMetadata().get("version");
                        LOGGER.info("Live collection node version: " + version);
                        if (publishedStatus.contains(status) && (version == null || version.intValue() < 2)) {
                            liveNode = util.getNode("domain", nodeId);
                            System.out.println("Relations migration required for Content ID: " + nodeId);
                            Map<String, Object> liveHierarchy = hierarchyStore.getHierarchy(nodeId);
                            if (MapUtils.isNotEmpty(liveHierarchy)) {
                                List<Map<String, Object>> leafNodes = getLeafNodes(liveHierarchy, 1);
                                LOGGER.info("Total leaf nodes to create relation with root node are " + leafNodes.size());
                                if (leafNodes.size() > 0) {
                                    List<Relation> relations = getRelations(nodeId, leafNodes);
                                    List<Relation> outRelations = liveNode.getOutRelations();
                                    if (CollectionUtils.isNotEmpty(outRelations)) {
                                        List<Relation> filteredRels = outRelations.stream().filter(relation -> {
                                            Map<String, Object> metadata = relation.getEndNodeMetadata();
                                            if (MapUtils.isNotEmpty(metadata)) {
                                                String visibility = (String) metadata.get("visibility");
                                                return (!StringUtils.equalsIgnoreCase("Parent", visibility));
                                            } else {
                                                return true;
                                            }
                                        }).collect(Collectors.toList());
                                        relations.addAll(filteredRels);
                                    }
                                    LOGGER.info("Updating out relations with " + new ObjectMapper().writeValueAsString(relations));
                                    liveNode.setOutRelations(relations);
                                }
                                liveNode.getMetadata().put("version", 2);
                                Response response = util.updateNodeWithoutValidation(liveNode);
                                LOGGER.info("Relations update response: " + mapper.writeValueAsString(response));
                                if (!util.checkError(response)) {
                                    LOGGER.info("Updated the collection with new format of relations...");
                                } else {
                                    migrationSuccess = false;
                                    LOGGER.info("Failed to update relations in new format.");
                                }
                            } else {
                                LOGGER.info("Content Live node hierarchy is empty so, not creating relations for content: " + nodeId);
                            }
                        } else {
                            LOGGER.info("Content doesn't have Live or Unlisted node to migrate relations.");
                        }

                        if (migrationSuccess) {
                            LOGGER.info("Updating the node version to 2 for collection ID: " + node.getIdentifier());
                            node = getNode(nodeId);
                            updateNodeVersion(node);
                        } else {
                            LOGGER.info("Migration failed for collection ID: " + node.getIdentifier() + ". Please check the above logs for more details.");
                        }
                    } else {
                        LOGGER.info("There is no hierarchy data for the content ID: " + node.getIdentifier());
                    }
                } else {
                    metrics.incSkippedCounter();
                    FailedEventsUtil.pushEventForRetry(systemStream, message, metrics, collector,
                            PlatformErrorCodes.PROCESSING_ERROR.name(), new ServerException("ERR_COLLECTION_MIGRATION", "Please check neo4j connection or identifier to migrate."));
                    LOGGER.info("Invalid Node Object. It is not a collection. Unable to process the event", message);
                }
            } else {
                metrics.incSkippedCounter();
                LOGGER.info("Invalid NodeId. Unable to process the event", message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void migrateECML(Map<String, Object> message, JobMetrics metrics, Map<String, Object> object) {
        try {
            String contentId = (String) object.get("id");
            //Get both the image and original node if exists
            Boolean isImage = false;
            Node ecmlNode = util.getNode("domain", contentId);
            Node ecmlImageNode = util.getNode("domain", contentId + ".img");

            if (RequestValidatorUtil.isEmptyOrNull(ecmlImageNode) && RequestValidatorUtil.isEmptyOrNull(ecmlNode)) {
                throw new ClientException(migrationService.ECML_MIGRATION_FAILED, "No content with this id");
            }
            if (!RequestValidatorUtil.isEmptyOrNull(ecmlImageNode))
                isImage = true;
            //Checks if the content can be migrated or already has been
            if (migrationService.checkIfValidMigrationRequest(ecmlNode)) {
                String contentBody = util.getContentBody(contentId);
                String imageContentBody = "";
                if (RequestValidatorUtil.isEmptyOrNull(contentBody))
                    throw new ClientException(migrationService.ECML_MIGRATION_FAILED, "Ecml body cannot be null");
                //Add media maps of all contents (Both node and image node)
                List<Map<String, Object>> mediaContents = migrationService.getMediaContents(contentBody);
                if (isImage && migrationService.checkIfValidMigrationRequest(ecmlImageNode)) {
                    imageContentBody = util.getContentBody(contentId + ".img");
                    mediaContents.addAll(migrationService.getMediaContents(imageContentBody));
                    if (RequestValidatorUtil.isEmptyOrNull(imageContentBody))
                        throw new ClientException(migrationService.ECML_MIGRATION_FAILED, "Ecml body cannot be null");
                }
                //Add medias only with drive urls (Both node and image)
                List<Map<String, Object>> mediasWithDriveUrl = migrationService.getMediasWithDriveUrl(mediaContents);
                Set<String> contentUrls = new HashSet<>();
                mediasWithDriveUrl.forEach(media -> contentUrls.add((String) media.get("src")));

                List<Node> nodesForUpdate = new ArrayList<>();
                nodesForUpdate.add(ecmlNode);
                if (isImage)
                    nodesForUpdate.add(ecmlImageNode);
                //If no media contains drive urls then update node version
                if (CollectionUtils.isEmpty(contentUrls)) {
                    migrationService.updateEcmlNode(nodesForUpdate);
                    return;
                }
                //Get all the contents whose drive urls don't have an existing asset
                Set<String> contentUrlsWithNoAsset = new HashSet<>();
                Map<String, String> driveArtifactMap = new HashMap();
                contentUrls.forEach(contentUrl -> {
                    String artifactUrl = migrationService.doesAssetExists(contentUrl);
                    if (StringUtils.isBlank(artifactUrl))
                        contentUrlsWithNoAsset.add(contentUrl);
                    else
                        driveArtifactMap.put(contentUrl, artifactUrl);
                });
                //Download the content, create asset and upload the respective content
                Map<String, List> fileMap = new HashMap();
                contentUrlsWithNoAsset.forEach(contentUrl -> fileMap.putAll(migrationService.downloadDriveContents(contentUrl, downloadFileLocation)));
                fileMap.keySet().forEach(driveUrl -> {
                    String id = migrationService.createAsset(fileMap, driveUrl);
                    String artifactUrl = migrationService.uploadAsset((File) fileMap.get(driveUrl).get(0), id);
                    if (StringUtils.isNotBlank(artifactUrl))
                        driveArtifactMap.put(driveUrl, artifactUrl);
                });
                //Delete all the downloaded content
                migrationService.deleteDownloadedContent(downloadFileLocation);
                //Update the ecml body and the nodes
                if (MapUtils.isNotEmpty(driveArtifactMap)) {
                    if (isImage)
                        migrationService.ecmlBodyUpdate(imageContentBody, contentId + ".img", driveArtifactMap);
                    migrationService.ecmlBodyUpdate(contentBody, contentId, driveArtifactMap);
                    migrationService.updateEcmlNode(nodesForUpdate);
                } else
                    throw new ClientException(migrationService.ECML_MIGRATION_FAILED, "Drive Urls are not valid or not present");
            } else {
                metrics.incSkippedCounter();
                LOGGER.info("Migration is already done. Unable to process the event.", message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage(), e);
        }
    }



    private void updateNodeVersion(Node node) throws Exception {
        node.getMetadata().put("version", 2);
        Response response = util.updateNodeWithoutValidation(node);
        if (!util.checkError(response)) {
            LOGGER.info("Updated the node version to 2 for collection ID: " + node.getIdentifier());
            LOGGER.info("Migration completed for collection ID: " + node.getIdentifier());
        } else {
            LOGGER.error("Failed to update the node version to 2 for collection ID: " + node.getIdentifier() + " with error: " + response.getParams().getErrmsg(), response.getResult(), null);
            LOGGER.info("Migration failed for collection ID: " + node.getIdentifier() + ". Please check the above logs for more details.");
        }
    }

    private List<Map<String, Object>> getLeafNodes(Map<String, Object> hierarchy, int depth) {
        List<Map<String, Object>> children = (List<Map<String, Object>>) hierarchy.get("children");
        List<Map<String, Object>> leafNodes = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(children)) {
            int index = 1;
            for (Map<String, Object> child : children) {
                String visibility = (String) child.get("visibility");
                if (StringUtils.equalsIgnoreCase("Parent", visibility)) {
                    int currentDepth = depth + 1;
                    List<Map<String, Object>> nextLevelLeafNodes = getLeafNodes(child, currentDepth);
                    leafNodes.addAll(nextLevelLeafNodes);
                } else {
                    child.put("index", index);
                    child.put("depth", depth);
                    leafNodes.add(child);
                    index++;
                }
            }
        }
        return leafNodes;
    }

    private List<Relation> getRelations(String rootId, List<Map<String, Object>> leafNodes) {
        List<Relation> relations = new ArrayList<>();
        for (Map<String, Object> leafNode : leafNodes) {
            String id = (String) leafNode.get("identifier");
            int index = 1;
            Number num = (Number) leafNode.get("index");
            if (num != null) {
                index = num.intValue();
            }
            Relation rel = new Relation(rootId, RelationTypes.SEQUENCE_MEMBERSHIP.relationName(), id);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put(SystemProperties.IL_SEQUENCE_INDEX.name(), index);
            metadata.put("depth", leafNode.get("depth"));
            rel.setMetadata(metadata);
            relations.add(rel);
        }
        return relations;
    }

    private void getCollectionIdsToDelete(List<Map<String, Object>> children, List<String> idsToDelete) {
        if (CollectionUtils.isNotEmpty(children)) {
            children = children.stream().map(child -> {
                if (StringUtils.equalsIgnoreCase((String) child.get("mimeType"), "application/vnd.ekstep.content-collection") && StringUtils.equalsIgnoreCase((String) child.get("visibility"), "Parent")) {
                    idsToDelete.add((String) child.get("identifier"));
                }
                return child;
            }).collect(Collectors.toList());
            List<Map<String, Object>> nextChildren = children.stream()
                    .map(child -> (List<Map<String, Object>>) child.get("children"))
                    .filter(f -> CollectionUtils.isNotEmpty(f)).flatMap(f -> f.stream())
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(nextChildren)) {
                getCollectionIdsToDelete(nextChildren, idsToDelete);
            }
        } else {
            LOGGER.info("Children is empty: " + children);
        }
    }

    private void updateCollectionsInHierarchy(List<Map<String, Object>> children) {
        if (CollectionUtils.isNotEmpty(children)) {
            children = children.stream().map(child -> {
                if (StringUtils.equalsIgnoreCase((String) child.get("mimeType"), "application/vnd.ekstep.content-collection") && StringUtils.equalsIgnoreCase((String) child.get("visibility"), "Parent")) {
                    String id = ((String) child.get("identifier")).replaceAll(".img", "");
                    child.put("status", "Draft");
                    child.put("objectType", "Content");
                    child.put("identifier", id);
                }
                return child;
            }).collect(Collectors.toList());
            List<Map<String, Object>> nextChildren = children.stream()
                    .map(child -> (List<Map<String, Object>>) child.get("children"))
                    .filter(f -> CollectionUtils.isNotEmpty(f)).flatMap(f -> f.stream())
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(nextChildren)) {
                updateCollectionsInHierarchy(nextChildren);
            }
        } else {
            LOGGER.info("Children is empty: " + children);
        }
    }

    /**
     * Checking is it a valid node for migration.
     *
     * @param node
     * @return boolean
     */
    private boolean validNode(Node node) {
        Map<String, Object> metadata = node.getMetadata();
        String visibility = (String) metadata.get("visibility");
        String mimeType = (String) metadata.get("mimeType");
        return (StringUtils.equalsIgnoreCase("Default", visibility) && StringUtils.equalsIgnoreCase("application/vnd.ekstep.content-collection", mimeType));
    }

    private boolean validateEdata(Map<String, Object> edata) {
        String action = (String) edata.get("action");
        return (StringUtils.equalsIgnoreCase("collection-migration", action));
    }

    private Node getNode(String nodeId) {
        String imgNodeId = nodeId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
        Node node = util.getNode("domain", imgNodeId);
        if (null == node) {
            node = util.getNode("domain", nodeId);
        }
        return node;
    }

    protected DefinitionDTO getDefinition(String graphId, String objectType) {
        Request request = util.getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
                GraphDACParams.object_type.name(), objectType);
        Response response = util.getResponse(request);
        if (!util.checkError(response)) {
            DefinitionDTO definition = (DefinitionDTO) response.get(GraphDACParams.definition_node.name());
            return definition;
        }
        return null;
    }
}