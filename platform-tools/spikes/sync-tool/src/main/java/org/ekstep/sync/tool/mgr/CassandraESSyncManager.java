/**
 * @author: Rhea Fernandes
 * @created: 13th May 2019
 */
package org.ekstep.sync.tool.mgr;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.mgr.ConvertToGraphNode;
import org.ekstep.common.util.RequestValidatorUtil;
import org.ekstep.content.entity.Manifest;
import org.ekstep.content.entity.Media;
import org.ekstep.content.entity.Plugin;
import org.ekstep.content.operation.initializer.BaseInitializer;
import org.ekstep.graph.cache.util.RedisStoreUtil;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.graph.service.common.DACConfigurationConstants;
import org.ekstep.learning.contentstore.ContentStore;
import org.ekstep.learning.hierarchy.store.HierarchyStore;
import org.ekstep.learning.util.CloudStore;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.sync.tool.util.ElasticSearchConnector;
import org.ekstep.sync.tool.util.GraphUtil;
import org.ekstep.sync.tool.util.SyncMessageGenerator;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CassandraESSyncManager {

    private ControllerUtil util = new ControllerUtil();
    private ObjectMapper mapper = new ObjectMapper();
    private String graphId = "domain";
    private final String objectType = "Content";
    private final String nodeType = "DATA_NODE";
    DefinitionDTO definitionDTO;
    Map<String, Object> definition = new HashMap<>();
    Map<String, String> relationMap = new HashMap<>();


    private HierarchyStore hierarchyStore = new HierarchyStore();
    private ContentStore contentStore = new ContentStore();
    private ElasticSearchConnector searchConnector = new ElasticSearchConnector();
    private static final String COLLECTION_MIMETYPE = "application/vnd.ekstep.content-collection";
    private static String graphPassportKey = Platform.config.getString(DACConfigurationConstants.PASSPORT_KEY_BASE_PROPERTY);
    private static List<String> nestedFields = Platform.config.getStringList("nested.fields");
    private List<String> relationshipProperties = Platform.config.hasPath("content.relationship.properties") ?
            Arrays.asList(Platform.config.getString("content.relationship.properties").split(",")) : Collections.emptyList();

    private static final String CACHE_PREFIX = "hierarchy_";


    @PostConstruct
    private void init() throws Exception {
        definitionDTO = util.getDefinition(graphId, objectType);
        definition = getDefinition();
        relationMap = GraphUtil.getRelationMap(objectType, definition);
    }

    public void syncAllIds(String graphId, List<String> resourceIds, List<String> bookmarkIds) {
        if(CollectionUtils.isNotEmpty(resourceIds)) {
            if(CollectionUtils.size(resourceIds) > 1) {
                if(CollectionUtils.isNotEmpty(bookmarkIds))
                    System.out.println("Bookmark Id's shouldn't be provided for Multiple textbooks");
                resourceIds.forEach(textbook->{
                    Boolean flag = syncByBookmarkId(graphId, textbook, null, false);
                        System.out.println("Textbook id : " + textbook + " Sync status : " + flag);
                });
            } else
                resourceIds.forEach(textbook->{
                    Boolean flag = syncByBookmarkId(graphId, textbook, bookmarkIds, false);
                    System.out.println("Textbook id : " + textbook + " Sync status : " + flag);
                });        }
    }

    public void syncLeafNodesCountByIds(String graphId, List<String> resourceIds) {
        if(CollectionUtils.isNotEmpty(resourceIds)) {
            resourceIds.forEach(collectionId->{
                Boolean flag = syncByBookmarkId(graphId, collectionId, null, true);
                System.out.println("Collection id : " + collectionId + " Sync status : " + flag);
            });
        }
    }

    public Boolean syncByBookmarkId(String graphId, String resourceId, List<String> bookmarkIds, boolean refreshLeafNodeCount) {
        this.graphId = RequestValidatorUtil.isEmptyOrNull(graphId) ? "domain" : graphId;
        try {
            Map<String, Object> hierarchy = getTextbookHierarchy(resourceId);
            if (MapUtils.isNotEmpty(hierarchy)) {
                if(refreshLeafNodeCount) {
                    updateLeafNodeCount(hierarchy, resourceId);
                }
                Map<String, Object> units = getUnitsMetadata(hierarchy, bookmarkIds);
                if(MapUtils.isNotEmpty(units)){
                    pushToElastic(units);
                    List<String> collectionUnitIds = new ArrayList<>(units.keySet());

                    //Clear TextBookUnits from Cassandra Hierarchy Store
                    hierarchyStore.deleteHierarchy(collectionUnitIds);

                    //Clear TextBook from Redis Cache
                    RedisStoreUtil.delete(CACHE_PREFIX + resourceId);

                    //Clear TextBookUnits from Redis Cache
                    collectionUnitIds.forEach(id -> RedisStoreUtil.delete(CACHE_PREFIX + id));

                    //print success message
                    printMessages("success", collectionUnitIds, resourceId);
                }
                return true;
            } else {
                System.out.println(resourceId + " is not a type of Collection or it is not live.");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return false;
        }
    }

    private void updateLeafNodeCount(Map<String, Object> hierarchy, String resourceId) throws Exception {
            //Update Collection leafNodesCount in the hierarchy
            int collectionLeafNodesCount = getLeafNodesCount(hierarchy);
            hierarchy.put("leafNodesCount", collectionLeafNodesCount);
            // Update RootNode in Neo4j
            updateTextBookNode(resourceId, "leafNodesCount", collectionLeafNodesCount);

            //Update leafNodesCount of children in the hierarchy
            updateLeafNodesCountInHierarchyMetadata((List<Map<String, Object>>) hierarchy.get("children"));

            //Update cassandra with updatedHierarchy
            hierarchyStore.saveOrUpdateHierarchy(resourceId, hierarchy);

            //Clear Redis Cache of hierarchy data
            RedisStoreUtil.delete("hierarchy_" + resourceId);
    }

    private Boolean updateElasticSearch(List<Map<String, Object>> units, List<String> bookmarkIds, String resourceId) throws Exception {
        if (CollectionUtils.isNotEmpty(units)) {
            List<String> syncedUnits = getSyncedUnitIds(units);
            List<String> failedUnits = getFailedUnitIds(units, bookmarkIds);
            Map<String, Object> esDocs = getESDocuments(units);
            if (MapUtils.isNotEmpty(esDocs)) {
                pushToElastic(esDocs);
                printMessages("success", syncedUnits, resourceId);
            }
            if (CollectionUtils.isNotEmpty(failedUnits)) {
                printMessages("failed", failedUnits, resourceId);
                return false;
            }
        }
        return true;
    }


    public Map<String, Object> getTextbookHierarchy(String resourceId) throws Exception {
        Map<String, Object> hierarchy;
        if (RequestValidatorUtil.isEmptyOrNull(resourceId))
            throw new ClientException("BLANK_IDENTIFIER", "Identifier is blank.");
        hierarchy = hierarchyStore.getHierarchy(resourceId);
        return hierarchy;
    }


    public Map<String, Object> getUnitsMetadata(Map<String, Object> hierarchy, List<String> bookmarkIds) {
        Boolean flag = false;
        Map<String, Object> unitsMetadata = new HashMap<>();
        if(CollectionUtils.isEmpty(bookmarkIds))
            flag = true;
        List<Map<String, Object>> children = (List<Map<String, Object>>)hierarchy.get("children");
        getUnitsToBeSynced(unitsMetadata, children, bookmarkIds, flag);
        return unitsMetadata;
    }

    private void getUnitsToBeSynced(Map<String, Object> unitsMetadata, List<Map<String, Object>> children, List<String> bookmarkIds, Boolean flag) {
        if (CollectionUtils.isNotEmpty(children)) {
            children.forEach(child -> {
                if (child.containsKey("visibility") && StringUtils.equalsIgnoreCase((String) child.get("visibility"), "parent")) {
                		if (flag || bookmarkIds.contains(child.get("identifier"))){
                		    populateESDoc(unitsMetadata, child);
                        }
                    if (child.containsKey("children")) {
                        List<Map<String,Object>> newChildren = mapper.convertValue(child.get("children"), new TypeReference<List<Map<String, Object>>>(){});
                        getUnitsToBeSynced(unitsMetadata, newChildren , bookmarkIds, flag);
                    }
                }
            });
        }
    }

    private void populateESDoc(Map<String, Object> unitsMetadata, Map<String, Object> child) {
        Map<String, Object> childData = new HashMap<>();
        childData.putAll(child);
        childData.remove("children");
        try {
            Node node = ConvertToGraphNode.convertToGraphNode(childData, definitionDTO, null);
            node.setGraphId(graphId);
            node.setObjectType(objectType);
            node.setNodeType(nodeType);
            Map<String, Object> nodeMap = SyncMessageGenerator.getMessage(node);
            Map<String, Object>  message = SyncMessageGenerator.getJSONMessage(nodeMap, relationMap);
            childData = refactorUnit(child);
            Object variants = message.get("variants");
            if(null != variants && !(variants instanceof String))
                message.put("variants", mapper.writeValueAsString(variants));
            updateRelationData(message, childData);
            unitsMetadata.put((String) childData.get("identifier"), message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateRelationData(Map<String, Object> message, Map<String, Object> childData) {
        if(CollectionUtils.isNotEmpty(relationshipProperties)){
            relationshipProperties.forEach(relationProperty -> {
                if(CollectionUtils.isNotEmpty((List) childData.get(relationProperty)))
                    message.put(relationProperty, childData.get(relationProperty));
            });
        }
    }

    private Map<String, Object> refactorUnit(Map<String, Object> child) {
    		Map<String, Object> childData = new HashMap<>();
        childData.putAll(child);
        for(String property : relationshipProperties) {
        		if(childData.containsKey(property)) {
        			List<Map<String, Object>> nextLevelNodes = (List<Map<String, Object>>) childData.get(property);
        	        List<String> finalPropertyList = new ArrayList<>();
        			if (CollectionUtils.isNotEmpty(nextLevelNodes)) {
        				finalPropertyList = nextLevelNodes.stream().map(nextLevelNode -> {
        					String identifier = (String)nextLevelNode.get("identifier");
        					return identifier;
        				}).collect(Collectors.toList());
        			}
        			childData.remove(property);
        			childData.put(property, finalPropertyList);
        		}
        }
        return childData;
	}

    private List<String> getFailedUnitIds(List<Map<String, Object>> units, List<String> bookmarkIds) {
        List<String> failedUnits = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(bookmarkIds)) {
        	    if (units.size() == bookmarkIds.size())
                return failedUnits;
        	    failedUnits.addAll(bookmarkIds);
            units.forEach(unit -> {
                if (bookmarkIds.contains(unit.get("identifier")))
                		failedUnits.remove(unit.get("identifier"));
            });
        }
        return failedUnits;
    }
    private List<String> getSyncedUnitIds(List<Map<String, Object>> units){
    		List<String> syncedUnits = new ArrayList<>();
    		units.forEach(unit -> {
    			syncedUnits.add((String)unit.get("identifier"));
    		});
    		return syncedUnits;
    }

    private Map<String,Object> getTBMetaData(String textBookId) throws Exception {
        Node node = util.getNode(graphId, textBookId);
        if (RequestValidatorUtil.isEmptyOrNull(node))
            throw new ClientException("RESOURCE_NOT_FOUND", "Enter a Valid Textbook id");
        String status = (String) node.getMetadata().get("status");
        if (StringUtils.isNotEmpty(status) && (!StringUtils.equalsIgnoreCase(status,"live")))
            throw new ClientException("RESOURCE_NOT_FOUND", "Text book must be live");
        Map<String,Object> metadata = node.getMetadata();
        metadata.put("identifier",node.getIdentifier());
        metadata.put("nodeUniqueId",node.getId());
        return metadata;
    }

    private Map<String, Object> getESDocuments(List<Map<String, Object>> units) throws Exception {
        List<String> indexablePropslist;
        DefinitionDTO definition =util.getDefinition("domain", "Content");
        List<Node> nodes = units.stream().map(unit -> {
            try {
                return ConvertToGraphNode.convertToGraphNode(unit, definition, null);
            } catch (Exception e) {
            }
            return null;
        }).filter(node -> null!=node).collect(Collectors.toList());

        Map<String, Object> esDocument = SyncMessageGenerator.getMessages(nodes, "Content", new HashMap<>());
        return esDocument;
    }

    private void putAdditionalFields(Map<String, Object> unit, String identifier) {
        unit.put("graph_id", graphId);
        unit.put("identifier", identifier);
        unit.put("objectType", objectType);
        unit.put("nodeType", nodeType);
    }

    private Map<String, Object> getDefinition() {
    		this.graphId = RequestValidatorUtil.isEmptyOrNull(graphId) ? "domain" : graphId;
        if (RequestValidatorUtil.isEmptyOrNull(definitionDTO)) {
            throw new ServerException("ERR_DEFINITION_NOT_FOUND", "No Definition found for " + objectType);
        }
        return mapper.convertValue(definitionDTO, new TypeReference<Map<String, Object>>() {
        });
    }

    //Return a list of all failed units
    public void pushToElastic(Map<String, Object> esDocument) {
        try {
            searchConnector.bulkImport(esDocument);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getLocalizedMessage());
        }
    }

    private List<String> getIndexableProperties(Map<String, Object> definition) {
        List<String> propsList = new ArrayList<>();
        List<Map<String, Object>> properties = (List<Map<String, Object>>) definition.get("properties");
        for (Map<String, Object> property : properties) {
            if ((Boolean) property.get("indexed")) {
                propsList.add((String) property.get("propertyName"));
            }
        }
        return propsList;
    }

    private static void filterIndexableProps(Map<String, Object> documentMap, final List<String> indexablePropsList) {
        documentMap.keySet().removeIf(propKey -> !indexablePropsList.contains(propKey));
    }

    private void printMessages(String status, List<String> bookmarkIds, String id) {
        switch (status) {
            case "failed": {
                System.out.println("The units " + bookmarkIds + " of textbook with " + id + " failed. Check if valid unit.");
                break;
            }
            case "success": {
                System.out.println("The units " + bookmarkIds + " of textbook with " + id + " success");
                break;
            }
        }

    }

    private void updateLeafNodesCountInHierarchyMetadata(List<Map<String, Object>> children) {
        if(CollectionUtils.isNotEmpty(children)) {
            for(Map<String, Object> child : children) {
                if(StringUtils.equalsIgnoreCase("Parent",
                        (String)child.get("visibility"))){
                    //set child metadata -- leafNodesCount
                    child.put("leafNodesCount", getLeafNodesCount(child));
                    updateLeafNodesCountInHierarchyMetadata((List<Map<String,Object>>)child.get("children"));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Integer getLeafNodesCount(Map<String, Object> data) {
        Set<String> leafNodeIds = new HashSet<>();
        getLeafNodesIds(data, leafNodeIds);
        return leafNodeIds.size();
    }


    private void updateTextBookNode(String id,String propName, Object propValue) throws Exception {
        DefinitionDTO definition =util.getDefinition("domain", "Content");
        Node node = util.getNode("domain", id);
        Map<String, Object> map = new HashMap<String, Object>() {{
            put(propName, propValue);
            put("versionKey", graphPassportKey);
        }};
        Node domainObj = ConvertToGraphNode.convertToGraphNode(map, definition, null);
        domainObj.setGraphId("domain");
        domainObj.setObjectType("Content");
        domainObj.setIdentifier(id);
        Response response = util.updateNodeWithoutValidation(domainObj) ;
        if(util.checkError(response)){
            System.out.println("Status :::::::" + response.getParams().getStatus());
            System.out.println(mapper.writeValueAsString(response));
            throw new ServerException("Error while updating RootNode" , response.getParams().getErr() + " :: "+ response.getParams().getErrmsg() + " :: " + response.getResult());
        }

    }

    public void syncLeafNodesByIds(List<String> rootIds) {
        if(CollectionUtils.isNotEmpty(rootIds)) {
            rootIds.forEach(collectionId->{
                Boolean flag = updateLeafNodeIds(collectionId);
                System.out.println("Collection id : " + collectionId + " Sync status : " + flag);
            });
        }
    }

    private Boolean updateLeafNodeIds(String collectionId) {
        try {
            Map<String, Object> hierarchy = getTextbookHierarchy(collectionId);
            if(MapUtils.isNotEmpty(hierarchy)){
                RedisStoreUtil.delete(collectionId);
                RedisStoreUtil.delete(CACHE_PREFIX + collectionId);

                Set<String> leafNodeIds = new HashSet<>();
                getLeafNodesIds(hierarchy, leafNodeIds);
                System.out.println("LeafNodes are :" + leafNodeIds);
                List<String> leafNodes = new ArrayList<>(leafNodeIds);
                updateTextBookNode(collectionId, "leafNodes", leafNodes);
                hierarchy.put("leafNodes", leafNodes);
                hierarchyStore.saveOrUpdateHierarchy(collectionId, hierarchy);
                return true;
            } else {
                System.out.println(collectionId + " is not a type of Collection or it is not live.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void getLeafNodesIds(Map<String, Object> data, Set<String> leafNodeIds) {
        List<Map<String,Object>> children = (List<Map<String,Object>>)data.get("children");
        if(CollectionUtils.isNotEmpty(children)) {
            for(Map<String, Object> child : children) {
                getLeafNodesIds(child, leafNodeIds);
            }
        } else {
            if (!StringUtils.equalsIgnoreCase(COLLECTION_MIMETYPE, (String) data.get("mimeType"))) {
                leafNodeIds.add((String) data.get("identifier"));
            }
        }
    }
    
    public void syncECMLContent(List<String> contentIds) {
    	if(CollectionUtils.isNotEmpty(contentIds)) {
    		System.out.println("Content came for handling external link:  " + contentIds.toString());
    		List<String> contentWithNoBody = new ArrayList<>();
    		contentIds.stream().forEach(x -> handleAssetWithExternalLink(contentWithNoBody, x));
    		System.out.println("Content Body not exists for content:  " + contentWithNoBody.toString());
    	}
    }
    
    public void handleAssetWithExternalLink(List<String> contentWithNoBody, String contentId) {
    	String contentBody = contentStore.getContentBody(contentId);
    	
    	if(StringUtils.isNoneBlank(contentBody)) {
    		BaseInitializer baseInitializer = new BaseInitializer();
    		Plugin plugin = baseInitializer.getPlugin(contentBody);
    		
    		if (null != plugin) {
    			try {
    				Manifest manifest = plugin.getManifest();
    				if (null != manifest) {
    					List<Media> medias = manifest.getMedias();
    					if(CollectionUtils.isNotEmpty(medias)) {
    						List<Map<String, Object>> externalLink = new ArrayList<Map<String,Object>>();
    						for (Media media: medias) {
    							TelemetryManager.log("Validating Asset for External link: " + media.getId());
    							if(validateAssetMediaForExternalLink(media)) {
    								Map<String, Object> assetMap = new HashMap<String, Object>();
    								assetMap.put("id", media.getId());
    								assetMap.put("src", media.getSrc());
    								assetMap.put("type", media.getType());
    								externalLink.add(assetMap);
    							}
    						}
    						contentStore.updateExternalLink(contentId, externalLink);
    					}
    				}
    			}catch(Exception e) {
    				TelemetryManager.error("Error while pushing externalLink details of content Id: " + contentId +" into cassandra.", e);
    			}
    		}
    	}else {
    		contentWithNoBody.add(contentId);
    	}
    }
	
	protected boolean validateAssetMediaForExternalLink(Media media){
		boolean isExternal = false;
		UrlValidator validator = new UrlValidator();
		String urlLink = media.getSrc();
		if(StringUtils.isNotBlank(urlLink) && 
				validator.isValid(media.getSrc()) &&
				!StringUtils.contains(urlLink, CloudStore.getContainerName()))
			isExternal = true; 
		return isExternal;
	}
}