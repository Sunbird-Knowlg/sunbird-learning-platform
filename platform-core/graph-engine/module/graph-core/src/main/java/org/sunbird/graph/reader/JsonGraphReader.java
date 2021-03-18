package org.sunbird.graph.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.sunbird.common.exception.ClientException;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.graph.dac.enums.RelationTypes;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.model.collection.Sequence;
import org.sunbird.graph.model.node.DataNode;
import org.sunbird.graph.model.node.DefinitionNode;
import org.sunbird.graph.model.node.MetadataDefinition;
import org.sunbird.graph.model.node.RelationDefinition;
import org.sunbird.graph.model.node.TagDefinition;

public class JsonGraphReader implements GraphReader {

    private ObjectMapper mapper;
    private List<Node> definitionNodes;
    private List<Node> dataNodes;
    private Map<String, List<String>> tagMembersMap;
    private List<Relation> relations;
    private List<String> validations;
    private BaseGraphManager manager;

    @SuppressWarnings("unchecked")
    public JsonGraphReader(BaseGraphManager manager, ObjectMapper mapper, String graphId, InputStream inputStream)
            throws JsonParseException, JsonMappingException, IOException {
        this.manager = manager;
        this.mapper = mapper;
        validations = new ArrayList<String>();
        tagMembersMap = new HashMap<String, List<String>>();
        Map<String, Object> inputMap = mapper.readValue(inputStream, Map.class);
        createDefinitionNodes(graphId, (List<Map<String, Object>>) inputMap.get("definitionNodes"));
        createDataNodes(graphId, (List<Map<String, Object>>) inputMap.get("nodes"));
        createRelations((List<Map<String, Object>>) inputMap.get("relations"));
    }

    @SuppressWarnings("unchecked")
    public JsonGraphReader(BaseGraphManager manager, ObjectMapper mapper, String graphId, String json) throws JsonParseException,
            JsonMappingException, IOException {
        this.manager = manager;
        this.mapper = mapper;
        validations = new ArrayList<String>();
        tagMembersMap = new HashMap<String, List<String>>();
        Map<String, Object> inputMap = mapper.readValue(json, Map.class);
        createDefinitionNodes(graphId, (List<Map<String, Object>>) inputMap.get("definitionNodes"));
        createDataNodes(graphId, (List<Map<String, Object>>) inputMap.get("nodes"));
        createRelations((List<Map<String, Object>>) inputMap.get("relations"));
    }

    public JsonGraphReader(BaseGraphManager manager) {
        this.manager = manager;
        mapper = new ObjectMapper();
    }

    @Override
    public List<Node> getDefinitionNodes() {
        return definitionNodes;
    }

    @Override
    public List<Node> getDataNodes() {
        return dataNodes;
    }

    @Override
    public List<Relation> getRelations() {
        return relations;
    }

    /**
     * @param definitionNodes
     *            the definitionNodes to set
     */
    @Override
    public void setDefinitionNodes(List<Node> definitionNodes) {
        this.definitionNodes = definitionNodes;
    }

    /**
     * @param dataNodes
     *            the dataNodes to set
     */
    @Override
    public void setDataNodes(List<Node> dataNodes) {
        this.dataNodes = dataNodes;
    }

    /**
     * @param relations
     *            the relations to set
     */
    @Override
    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }

    @SuppressWarnings("unchecked")
    private void createDefinitionNodes(String graphId, List<Map<String, Object>> inputNodeList) {
        definitionNodes = new ArrayList<Node>();
        if (null != inputNodeList) {
            for (Map<String, Object> inputNode : inputNodeList) {
                String objectType = (String) inputNode.get("objectType");

                List<Map<String, Object>> propertiesMapList = (List<Map<String, Object>>) inputNode.get("properties");
                List<MetadataDefinition> properties = getMetadataDefinitions(propertiesMapList);

                List<MetadataDefinition> indexedMetadata = new ArrayList<MetadataDefinition>();
                List<MetadataDefinition> nonIndexedMetadata = new ArrayList<MetadataDefinition>();
                if (null != properties && !properties.isEmpty()) {
                    for (MetadataDefinition def : properties) {
                        if (def.isIndexed()) {
                            indexedMetadata.add(def);
                        } else {
                            nonIndexedMetadata.add(def);
                        }
                    }
                }

                List<Map<String, Object>> inRelationMapList = (List<Map<String, Object>>) inputNode.get("inRelations");
                List<RelationDefinition> inRelations = new ArrayList<RelationDefinition>();
                for (Map<String, Object> relationItem : inRelationMapList) {
                    RelationDefinition relationDefinition = mapper.convertValue(relationItem, RelationDefinition.class);
                    if (relationDefinition != null)
                        inRelations.add(relationDefinition);
                }

                List<Map<String, Object>> outRelationMapList = (List<Map<String, Object>>) inputNode.get("outRelations");
                List<RelationDefinition> outRelations = new ArrayList<RelationDefinition>();
                for (Map<String, Object> relationItem : outRelationMapList) {
                    RelationDefinition relationDefinition = mapper.convertValue(relationItem, RelationDefinition.class);
                    if (relationDefinition != null)
                        outRelations.add(relationDefinition);
                }

                List<Map<String, Object>> systemTagMapList = (List<Map<String, Object>>) inputNode.get("systemTags");
                List<TagDefinition> systemTags = new ArrayList<TagDefinition>();
                for (Map<String, Object> sysTagItem : systemTagMapList) {
                    TagDefinition tagDefinition = mapper.convertValue(sysTagItem, TagDefinition.class);
                    if (tagDefinition != null)
                        systemTags.add(tagDefinition);
                }
                
                DefinitionNode definitionNode = new DefinitionNode(manager, graphId, objectType, indexedMetadata, nonIndexedMetadata,
                        inRelations, outRelations, systemTags);
                Map<String, Object> metadata = (Map<String, Object>) inputNode.get("metadata");
                definitionNode.setMetadata(metadata);
                definitionNodes.add(definitionNode.toNode());
            }
        }
    }

    private List<MetadataDefinition> getMetadataDefinitions(List<Map<String, Object>> metaMapList) {
        List<MetadataDefinition> metaDefinitions = new ArrayList<MetadataDefinition>();
        for (Map<String, Object> metaItem : metaMapList) {
            MetadataDefinition metaDefinition = mapper.convertValue(metaItem, MetadataDefinition.class);
            if (metaDefinition != null)
                metaDefinitions.add(metaDefinition);
        }
        return metaDefinitions;
    }

    @SuppressWarnings("unchecked")
    private void createDataNodes(String graphId, List<Map<String, Object>> inputNodeList) {
        dataNodes = new ArrayList<Node>();
        if (null != inputNodeList) {
            for (Map<String, Object> inputNode : inputNodeList) {
                String uniqueId = (String) inputNode.get("uniqueId");
                String objectType = (String) inputNode.get("objectType");
                String nodeType = (String) inputNode.get("nodeType");
                Map<String, Object> metadata = (Map<String, Object>) inputNode.get("metadata");
                removeNullProperties(metadata);
                if (SystemNodeTypes.DATA_NODE.name().equals(nodeType)) {
                    DataNode dataNode = new DataNode(manager, graphId, uniqueId, objectType, metadata);
                    dataNodes.add(dataNode.toNode());
                } else if (SystemNodeTypes.SEQUENCE.name().equals(nodeType)) {
                    Sequence sequence = new Sequence(manager, graphId, uniqueId);
                    dataNodes.add(sequence.toNode());
                } else {

                }
            }
        }
    }

    private void removeNullProperties(Map<String, Object> metadata) {
        Iterator<Entry<String, Object>> it = metadata.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> e = it.next();
            Object value = e.getValue();
            if (null == value) {
                it.remove();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void createRelations(List<Map<String, Object>> relationMapList) {
        relations = new ArrayList<Relation>();
        if (null != relationMapList) {
            for (Map<String, Object> relationMap : relationMapList) {
                String relationType = (String) relationMap.get("type");
                String startNodeId = (String) relationMap.get("startNode");
                String endNodeId = (String) relationMap.get("endNode");
                if (null != relationMap.get("type") && RelationTypes.isValidRelationType((String) relationMap.get("type"))) {
                    Relation relation = new Relation(startNodeId, relationType, endNodeId);
                    Map<String, Object> metadata = (Map<String, Object>) relationMap.get("metadata");
                    if (null == metadata) {
                        metadata = new HashMap<String, Object>();
                    } else {
                        removeNullProperties(metadata);
                    }
                    relation.setMetadata(metadata);
                    relations.add(relation);
                } else {
                    throw new ClientException("", "Relation Type is invalid: " + relationMap);
                }
            }

        }
    }

    @Override
    public List<String> getValidations() {
        return validations;
    }

    /**
     * @return the tagMembersMap
     */
    public Map<String, List<String>> getTagMembersMap() {
        return tagMembersMap;
    }

    /**
     * @param tagMembersMap
     *            the tagMembersMap to set
     */
    public void setTagMembersMap(Map<String, List<String>> tagMembersMap) {
        this.tagMembersMap = tagMembersMap;
    }
}
