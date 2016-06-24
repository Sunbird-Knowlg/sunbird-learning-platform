package com.ilimi.graph.model.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.dispatch.Futures;
import akka.dispatch.OnComplete;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.graph.cache.actor.GraphCacheActorPoolMgr;
import com.ilimi.graph.cache.actor.GraphCacheManagers;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.router.GraphDACActorPoolMgr;
import com.ilimi.graph.dac.router.GraphDACManagers;
import com.ilimi.graph.exception.GraphEngineErrorCodes;
import com.ilimi.graph.model.cache.DefinitionCache;

public class DefinitionNode extends AbstractNode {

    public static final String INDEXABLE_METADATA_KEY = SystemProperties.IL_INDEXABLE_METADATA_KEY.name();
    public static final String NON_INDEXABLE_METADATA_KEY = SystemProperties.IL_NON_INDEXABLE_METADATA_KEY.name();
    public static final String IN_RELATIONS_KEY = SystemProperties.IL_IN_RELATIONS_KEY.name();
    public static final String OUT_RELATIONS_KEY = SystemProperties.IL_OUT_RELATIONS_KEY.name();
    public static final String REQUIRED_PROPERTIES = SystemProperties.IL_REQUIRED_PROPERTIES.name();
    public static final String SYSTEM_TAGS_KEY = SystemProperties.IL_SYSTEM_TAGS_KEY.name();

    private String objectType;
    private List<MetadataDefinition> indexedMetadata;
    private List<MetadataDefinition> nonIndexedMetadata;
    private List<RelationDefinition> inRelations;
    private List<RelationDefinition> outRelations;
    private List<TagDefinition> systemTags;
    private ObjectMapper mapper = new ObjectMapper();

    public DefinitionNode(BaseGraphManager manager, String graphId, String objectType, List<MetadataDefinition> indexedMetadata,
            List<MetadataDefinition> nonIndexedMetadata, List<RelationDefinition> inRelations, List<RelationDefinition> outRelations,
            List<TagDefinition> systemTags) {
        super(manager, graphId, SystemNodeTypes.DEFINITION_NODE.name() + "_" + objectType, null);
        this.objectType = objectType;
        this.indexedMetadata = indexedMetadata;
        this.nonIndexedMetadata = nonIndexedMetadata;
        this.inRelations = inRelations;
        this.outRelations = outRelations;
        this.systemTags = systemTags;
    }

    public DefinitionNode(BaseGraphManager manager, Node defNode) {
        super(manager, defNode.getGraphId(), defNode.getIdentifier(), null);
        this.objectType = defNode.getObjectType();
        fromNode(defNode);
    }
    
    @SuppressWarnings("unchecked")
    private void fromNode(Node defNode) {
        Map<String, Object> metadata = defNode.getMetadata();
        if (null != metadata && !metadata.isEmpty()) {
            Map<String, Object> otherMetadata = new HashMap<String, Object>();
            otherMetadata.putAll(metadata);
            String indexableMetadata = (String) metadata.get(INDEXABLE_METADATA_KEY);
            if (StringUtils.isNotBlank(indexableMetadata)) {
                try {
                    this.indexedMetadata = new ArrayList<MetadataDefinition>();
                    List<Map<String, Object>> listMap = (List<Map<String, Object>>) mapper.readValue(indexableMetadata, List.class);
                    for (Map<String, Object> metaMap : listMap) {
                        this.indexedMetadata.add((MetadataDefinition) mapper.convertValue(metaMap, MetadataDefinition.class));
                    }
                    otherMetadata.remove(INDEXABLE_METADATA_KEY);
                } catch (Exception e) {
                }
            }
            String nonIndexableMetadata = (String) metadata.get(NON_INDEXABLE_METADATA_KEY);
            if (StringUtils.isNotBlank(nonIndexableMetadata)) {
                try {
                    this.nonIndexedMetadata = new ArrayList<MetadataDefinition>();
                    List<Map<String, Object>> listMap = (List<Map<String, Object>>) mapper.readValue(nonIndexableMetadata, List.class);
                    for (Map<String, Object> metaMap : listMap) {
                        this.nonIndexedMetadata.add((MetadataDefinition) mapper.convertValue(metaMap, MetadataDefinition.class));
                    }
                    otherMetadata.remove(NON_INDEXABLE_METADATA_KEY);
                } catch (Exception e) {
                }
            }
            String inRelationsMetadata = (String) metadata.get(IN_RELATIONS_KEY);
            if (StringUtils.isNotBlank(inRelationsMetadata)) {
                try {
                    this.inRelations = new ArrayList<RelationDefinition>();
                    List<Map<String, Object>> listMap = (List<Map<String, Object>>) mapper.readValue(inRelationsMetadata, List.class);
                    for (Map<String, Object> metaMap : listMap) {
                        this.inRelations.add((RelationDefinition) mapper.convertValue(metaMap, RelationDefinition.class));
                    }
                    otherMetadata.remove(IN_RELATIONS_KEY);
                } catch (Exception e) {
                }
            }
            String outRelationsMetadata = (String) metadata.get(OUT_RELATIONS_KEY);
            if (StringUtils.isNotBlank(outRelationsMetadata)) {
                try {
                    this.outRelations = new ArrayList<RelationDefinition>();
                    List<Map<String, Object>> listMap = (List<Map<String, Object>>) mapper.readValue(outRelationsMetadata, List.class);
                    for (Map<String, Object> metaMap : listMap) {
                        this.outRelations.add((RelationDefinition) mapper.convertValue(metaMap, RelationDefinition.class));
                    }
                    otherMetadata.remove(OUT_RELATIONS_KEY);
                } catch (Exception e) {
                }
            }
            String sysTags = (String) metadata.get(SYSTEM_TAGS_KEY);
            if (StringUtils.isNotBlank(sysTags)) {
                try {
                    this.systemTags = new ArrayList<TagDefinition>();
                    List<Map<String, Object>> listMap = (List<Map<String, Object>>) mapper.readValue(sysTags, List.class);
                    for (Map<String, Object> metaMap : listMap) {
                        this.systemTags.add((TagDefinition) mapper.convertValue(metaMap, TagDefinition.class));
                    }
                    otherMetadata.remove(SYSTEM_TAGS_KEY);
                } catch (Exception e) {
                }
            }
            try {
                otherMetadata.remove(REQUIRED_PROPERTIES);
            } catch (Exception e) {
            }
            setMetadata(otherMetadata);
        }
    }

    public DefinitionDTO getValueObject() {
        DefinitionDTO dto = new DefinitionDTO();
        dto.setIdentifier(getNodeId());
        dto.setObjectType(getFunctionalObjectType());
        List<MetadataDefinition> properties = new ArrayList<MetadataDefinition>();
        if (null != indexedMetadata && !indexedMetadata.isEmpty()) {
            properties.addAll(indexedMetadata);
        }
        if (null != nonIndexedMetadata && !nonIndexedMetadata.isEmpty()) {
            properties.addAll(nonIndexedMetadata);
        }
        dto.setProperties(properties);
        dto.setInRelations(inRelations);
        dto.setOutRelations(outRelations);
        dto.setSystemTags(systemTags);
        dto.setMetadata(metadata);
        return dto;
    }

    @Override
    public Node toNode() {
        Node node = new Node(getNodeId(), SystemNodeTypes.DEFINITION_NODE.name(), objectType);
        if (null == metadata)
            metadata = new HashMap<String, Object>();
        try {
            List<String> requiredKeys = new ArrayList<String>();
            if (null != indexedMetadata && !indexedMetadata.isEmpty()) {
                metadata.put(INDEXABLE_METADATA_KEY, mapper.writeValueAsString(indexedMetadata));
                for (MetadataDefinition def : indexedMetadata) {
                    if (def.isRequired())
                        requiredKeys.add(def.getPropertyName());
                }
            }
            if (null != nonIndexedMetadata && !nonIndexedMetadata.isEmpty()) {
                metadata.put(NON_INDEXABLE_METADATA_KEY, mapper.writeValueAsString(nonIndexedMetadata));
                for (MetadataDefinition def : nonIndexedMetadata) {
                    if (def.isRequired())
                        requiredKeys.add(def.getPropertyName());
                }
            }
            if (null != inRelations && !inRelations.isEmpty()) {
                metadata.put(IN_RELATIONS_KEY, mapper.writeValueAsString(inRelations));
            }
            if (null != outRelations && !outRelations.isEmpty()) {
                metadata.put(OUT_RELATIONS_KEY, mapper.writeValueAsString(outRelations));
            }
            if (null != systemTags && !systemTags.isEmpty()) {
                metadata.put(SYSTEM_TAGS_KEY, mapper.writeValueAsString(systemTags));
            }
            if (null != requiredKeys && !requiredKeys.isEmpty())
                metadata.put(REQUIRED_PROPERTIES, convertListToArray(requiredKeys));
        } catch (Exception e) {
        }
        node.setMetadata(metadata);
        return node;
    }

    @Override
    public String getSystemNodeType() {
        return SystemNodeTypes.DEFINITION_NODE.name();
    }

    @Override
    public String getFunctionalObjectType() {
        return this.objectType;
    }

    @Override
    public void create(final Request req) {
        List<String> messages = validateDefinitionNode();
        if (null != messages && !messages.isEmpty()) {
            List<String> voList = new ArrayList<String>();
            for (String msg : messages) {
                voList.add(msg);
            }
            manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_NODE_INVALID_DEFINITION_NODE.name(), "Invalid Definition",
                    ResponseCode.CLIENT_ERROR, GraphDACParams.messages.name(), voList, getParent());
        } else {
            try {
                ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
                final Request request = new Request(req);
                request.setManagerName(GraphDACManagers.DAC_NODE_MANAGER);
                request.setOperation("upsertNode");
                request.put(GraphDACParams.node.name(), toNode());
                Future<Object> response = Patterns.ask(dacRouter, request, timeout);
                manager.onFailureResponse(response, getParent());
                response.onSuccess(new OnSuccess<Object>() {
                    @Override
                    public void onSuccess(Object arg0) throws Throwable {
                        if (arg0 instanceof Response) {
                            Response res = (Response) arg0;
                            if (manager.checkError(res)) {
                                manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_NODE_VALIDATION_FAILED.name(),
                                        manager.getErrorMessage(res), res.getResponseCode(), getParent());
                            } else {
                                ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
                                DefinitionCache.cacheDefinitionNode(graphId, getValueObject());
                                loadToCache(cacheRouter, req);
                                manager.OK(GraphDACParams.node_id.name(), getNodeId(), getParent());
                            }
                        } else {
                            manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_NODE_UNKNOWN_ERROR.name(),
                                    "Failed to create definition node", ResponseCode.SERVER_ERROR, getParent());
                        }
                    }
                }, manager.getContext().dispatcher());

            } catch (Exception e) {
                manager.ERROR(e, getParent());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void update(final Request req) {
        final List<MetadataDefinition> definitions = (List<MetadataDefinition>) req.get(GraphDACParams.metadata_definitions.name());
        if (!manager.validateRequired(definitions)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SAVE_DEF_NODE_MISSING_REQ_PARAMS.name(),
                    "Required parameters are missing");
        } else {
            try {
                ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
                Request request = new Request(req);
                request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
                request.setOperation("searchNodes");
                SearchCriteria sc = new SearchCriteria();
                sc.setNodeType(SystemNodeTypes.DEFINITION_NODE.name());
                sc.setObjectType(objectType);
                sc.setResultSize(1);
                request.put(GraphDACParams.search_criteria.name(), sc);
                Future<Object> response = Patterns.ask(dacRouter, request, timeout);
                response.onComplete(new OnComplete<Object>() {
                    @Override
                    public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                        boolean valid = manager.checkResponseObject(arg0, arg1, getParent(),
                                GraphEngineErrorCodes.ERR_GRAPH_SAVE_DEF_NODE_MISSING_REQ_PARAMS.name(),
                                "Failed to get definition node for " + objectType);
                        if (valid) {
                            Response res = (Response) arg1;
                            List<Node> nodes = (List<Node>) res.get(GraphDACParams.node_list.name());
                            if (null == nodes || nodes.isEmpty()) {
                                manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_SAVE_DEF_NODE_MISSING_REQ_PARAMS.name(),
                                        "Failed to get definition node for " + objectType, ResponseCode.RESOURCE_NOT_FOUND, getParent());
                            } else {
                                Node dbNode = nodes.get(0);
                                fromNode(dbNode);
                                Map<String, MetadataDefinition> map = new HashMap<String, MetadataDefinition>();
                                for (MetadataDefinition def : definitions) {
                                    map.put(def.getPropertyName(), def);
                                }
                                if (null != indexedMetadata && !indexedMetadata.isEmpty()) {
                                    for (MetadataDefinition def : indexedMetadata) {
                                        if (map.containsKey(def.getPropertyName()))
                                            map.remove(def.getPropertyName());
                                    }
                                }
                                if (null != nonIndexedMetadata && !nonIndexedMetadata.isEmpty()) {
                                    for (MetadataDefinition def : nonIndexedMetadata) {
                                        if (map.containsKey(def.getPropertyName()))
                                            map.remove(def.getPropertyName());
                                    }
                                }
                                if (!map.isEmpty()) {
                                    for (Entry<String, MetadataDefinition> entry : map.entrySet()) {
                                        MetadataDefinition newDef = entry.getValue();
                                        if (StringUtils.isBlank(newDef.getDataType()))
                                            newDef.setDataType("Text");
                                        if (StringUtils.isBlank(newDef.getDisplayProperty()))
                                            newDef.setDisplayProperty("Editable");
                                        if (StringUtils.isBlank(newDef.getCategory()))
                                            newDef.setCategory("general");
                                        if (StringUtils.isBlank(newDef.getTitle()))
                                            newDef.setTitle(newDef.getPropertyName());
                                        newDef.setDraft(true);
                                        nonIndexedMetadata.add(newDef);
                                    }
                                    create(req);
                                } else {
                                    manager.OK(getParent());
                                }
                            }
                        }
                    }
                }, manager.getContext().dispatcher());
            } catch (Exception e) {
                manager.ERROR(e, getParent());
            }
        }
    }

    @Override
    public Future<Map<String, List<String>>> validateNode(Request request) {
        List<String> messages = validateDefinitionNode();
        Future<List<String>> validation = Futures.successful(messages);
        return getMessageMap(validation, manager.context().dispatcher());
    }

    public List<String> validateDefinitionNode() {
        List<String> messages = new ArrayList<String>();
        if (StringUtils.isBlank(objectType)) {
            messages.add("Object Type cannot be blank for a Definition Node");
        }
        List<String> propertyNames = new ArrayList<String>();
        if (null != indexedMetadata && !indexedMetadata.isEmpty()) {
            for (MetadataDefinition def : indexedMetadata) {
                validateMetadataDefinition(def, messages, propertyNames);
            }
        }
        if (null != nonIndexedMetadata && !nonIndexedMetadata.isEmpty()) {
            for (MetadataDefinition def : nonIndexedMetadata) {
                validateMetadataDefinition(def, messages, propertyNames);
            }
        }
        List<String> relationNames = new ArrayList<String>();
        if (null != inRelations && !inRelations.isEmpty()) {
            for (RelationDefinition def : inRelations) {
                validateRelationDefinition(def, messages, relationNames);
            }
        }
        relationNames = new ArrayList<String>();
        if (null != outRelations && !outRelations.isEmpty()) {
            for (RelationDefinition def : outRelations) {
                validateRelationDefinition(def, messages, relationNames);
            }
        }
        return messages;
    }

    public void loadToCache(ActorRef cacheRouter, Request req) {
        Request cacheReq = new Request(req);
        cacheReq.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
        cacheReq.setOperation("saveDefinitionNode");
        cacheReq.put(GraphDACParams.object_type.name(), new String(getFunctionalObjectType()));
        List<String> indexedFields = new ArrayList<String>();
        List<String> nonIndexedFields = new ArrayList<String>();
        List<String> requiredFields = new ArrayList<String>();
        getMetadataFieldLists(indexedFields, nonIndexedFields, requiredFields);
        cacheReq.put(GraphDACParams.indexable_metadata_key.name(), indexedFields);
        cacheReq.put(GraphDACParams.non_indexable_metadata_key.name(), nonIndexedFields);
        cacheReq.put(GraphDACParams.required_metadata_key.name(), requiredFields);
        List<String> inRelationObjects = new ArrayList<String>();
        List<String> outRelationObjects = new ArrayList<String>();
        getRelationObjects(inRelationObjects, outRelationObjects);
        cacheReq.put(GraphDACParams.in_relations_key.name(), inRelationObjects);
        cacheReq.put(GraphDACParams.out_relations_key.name(), outRelationObjects);
        cacheRouter.tell(cacheReq, manager.getSelf());
    }

    private void validateRelationDefinition(RelationDefinition def, List<String> messages, List<String> relationNames) {
        if (StringUtils.isBlank(def.getRelationName()) || !RelationTypes.isValidRelationType(def.getRelationName())) {
            messages.add("Invalid relation type: " + def.getRelationName() + ". Object Type: " + objectType);
        } else {
            List<String> objectTypes = def.getObjectTypes();
            if (null != objectTypes && !objectTypes.isEmpty()) {
                for (String type : objectTypes) {
                    if (relationNames.contains(def.getRelationName().toLowerCase() + "_" + type.toLowerCase())) {
                        messages.add("Duplicate Relation Definition: " + def.getRelationName() + ", with object type: " + type);
                    } else {
                        relationNames.add(def.getRelationName().toLowerCase() + "_" + type.toLowerCase());
                    }
                }
            } else {
                messages.add("Invalid Relation Definition: " + def.getRelationName() + ". No object types are specified.");
            }
        }
        if (StringUtils.isBlank(def.getTitle())) {
            messages.add("Relation title is blank for " + def.getRelationName() + ". Object Type: " + objectType);
        }
    }

    private void validateMetadataDefinition(MetadataDefinition def, List<String> messages, List<String> propertyNames) {
        String propName = def.getPropertyName();
        if (StringUtils.isBlank(propName)) {
            messages.add("A property name is blank for object type: " + objectType);
        } else {
            if (propertyNames.contains(propName.toLowerCase())) {
                messages.add("Duplicate Metadata definition for property: " + propName + ". Object Type: " + objectType);
            } else {
                propertyNames.add(propName.toLowerCase());
            }
            if (checkForWhiteSpace(propName) || checkForCharacter(propName, ".") || checkForCharacter(propName, ":")) {
                messages.add("Property name cannot contain '.', ':' or spaces: " + propName + ". Object Type: " + objectType);
            }
        }
        if (StringUtils.isBlank(def.getTitle())) {
            messages.add("Metadata title is blank for " + propName + ". Object Type: " + objectType);
        }
        String dataType = def.getDataType();
        if (!MetadataDefinition.VALID_DATA_TYPES.contains(dataType.toLowerCase())) {
            messages.add("Invalid data type '" + dataType + "' for " + propName + ". Object Type: " + objectType);
        }
        if (StringUtils.equalsIgnoreCase("select", dataType) || StringUtils.equalsIgnoreCase("multi-select", dataType)) {
            if (null == def.getRange() || def.getRange().isEmpty()) {
                messages.add("Range is not provided for " + propName + ". Object Type: " + objectType);
            }
        }
        if (def.isRequired() && null == def.getDefaultValue()) {
            messages.add("Default value must be provided for required property " + propName + ". Object Type: " + objectType);
        }
    }

    private void getMetadataFieldLists(List<String> indexedFields, List<String> nonIndexedFields, List<String> requiredFields) {
        if (null != indexedMetadata && !indexedMetadata.isEmpty()) {
            for (MetadataDefinition def : indexedMetadata) {
                indexedFields.add(def.getPropertyName());
                if (def.isRequired())
                    requiredFields.add(def.getPropertyName());
            }
        }
        if (null != nonIndexedMetadata && !nonIndexedMetadata.isEmpty()) {
            for (MetadataDefinition def : nonIndexedMetadata) {
                nonIndexedFields.add(def.getPropertyName());
                if (def.isRequired())
                    requiredFields.add(def.getPropertyName());
            }
        }
    }

    private void getRelationObjects(List<String> inRelationObjects, List<String> outRelationObjects) {
        if (null != inRelations && !inRelations.isEmpty()) {
            for (RelationDefinition def : inRelations) {
                if (null != def.getObjectTypes() && !def.getObjectTypes().isEmpty()) {
                    for (String objType : def.getObjectTypes()) {
                        inRelationObjects.add(def.getRelationName() + ":" + objType);
                    }
                }
            }
        }
        if (null != outRelations && !outRelations.isEmpty()) {
            for (RelationDefinition def : outRelations) {
                if (null != def.getObjectTypes() && !def.getObjectTypes().isEmpty()) {
                    for (String objType : def.getObjectTypes()) {
                        outRelationObjects.add(def.getRelationName() + ":" + objType);
                    }
                }
            }
        }
    }

    public List<MetadataDefinition> getIndexedMetadata() {
        return indexedMetadata;
    }

    public void setIndexedMetadata(List<MetadataDefinition> indexedMetadata) {
        this.indexedMetadata = indexedMetadata;
    }

    public List<MetadataDefinition> getNonIndexedMetadata() {
        return nonIndexedMetadata;
    }

    public void setNonIndexedMetadata(List<MetadataDefinition> nonIndexedMetadata) {
        this.nonIndexedMetadata = nonIndexedMetadata;
    }

    public List<RelationDefinition> getInRelations() {
        return inRelations;
    }

    public void setInRelations(List<RelationDefinition> inRelations) {
        this.inRelations = inRelations;
    }

    public List<RelationDefinition> getOutRelations() {
        return outRelations;
    }

    public void setOutRelations(List<RelationDefinition> outRelations) {
        this.outRelations = outRelations;
    }
}
