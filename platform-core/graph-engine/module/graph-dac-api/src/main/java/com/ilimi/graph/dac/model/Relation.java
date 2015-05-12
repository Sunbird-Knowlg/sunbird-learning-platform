package com.ilimi.graph.dac.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.ilimi.graph.common.dto.BaseValueObject;
import com.ilimi.graph.common.exception.ServerException;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.exception.GraphDACErrorCodes;

public class Relation extends BaseValueObject {

    private static final long serialVersionUID = -7207054262120122453L;
    private long id;
    private String graphId;
    private String relationType;
    private String startNodeId;
    private String endNodeId;
    private String startNodeName;
    private String endNodeName;
    private String startNodeType;
    private String endNodeType;
    private String startNodeObjectType;
    private String endNodeObjectType;
    private Map<String, Object> metadata;

    public Relation() {

    }

    public Relation(String startNodeId, String relationType, String endNodeId) {
        this.startNodeId = startNodeId;
        this.endNodeId = endNodeId;
        this.relationType = relationType;
    }

    public Relation(String graphId, Relationship neo4jRel) {
        if (null == neo4jRel)
            throw new ServerException(GraphDACErrorCodes.ERR_GRAPH_NULL_DB_REL.name(),
                    "Failed to create relation object. Relation from database is null.");
        this.graphId = graphId;

        Node startNode = neo4jRel.getStartNode();
        Node endNode = neo4jRel.getEndNode();
        this.startNodeId = (String) startNode.getProperty(SystemProperties.IL_UNIQUE_ID.name());
        this.endNodeId = (String) endNode.getProperty(SystemProperties.IL_UNIQUE_ID.name());
        this.startNodeName = getName(startNode);
        this.endNodeName = getName(endNode);
        this.startNodeType = getNodeType(startNode);
        this.endNodeType = getNodeType(endNode);
        this.startNodeObjectType = getObjectType(startNode);
        this.endNodeObjectType = getObjectType(endNode);
        this.relationType = neo4jRel.getType().name();
        this.metadata = new HashMap<String, Object>();
        Iterable<String> keys = neo4jRel.getPropertyKeys();
        if (null != keys && null != keys.iterator()) {
            for (String key : keys) {
                this.metadata.put(key, neo4jRel.getProperty(key));
            }
        }
    }

    private String getName(Node node) {
        String name = (String) node.getProperty("name", null);
        if (StringUtils.isBlank(name)) {
            name = (String) node.getProperty("title", null);
            if (StringUtils.isBlank(name)) {
                name = (String) node.getProperty(SystemProperties.IL_TAG_NAME.name(), null);
                if (StringUtils.isBlank(name)) {
                    name = (String) node.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), null);
                    if (StringUtils.isBlank(name))
                        name = (String) node.getProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), null);
                }
            }
        }
        return name;
    }

    private String getNodeType(Node node) {
        String nodeType = (String) node.getProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), null);
        return nodeType;
    }

    private String getObjectType(Node node) {
        String objectType = (String) node.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), null);
        return objectType;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public String getStartNodeId() {
        return startNodeId;
    }

    public void setStartNodeId(String startNodeId) {
        this.startNodeId = startNodeId;
    }

    public String getEndNodeId() {
        return endNodeId;
    }

    public void setEndNodeId(String endNodeId) {
        this.endNodeId = endNodeId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getGraphId() {
        return graphId;
    }

    public void setGraphId(String graphId) {
        this.graphId = graphId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStartNodeName() {
        return startNodeName;
    }

    public void setStartNodeName(String startNodeName) {
        this.startNodeName = startNodeName;
    }

    public String getEndNodeName() {
        return endNodeName;
    }

    public void setEndNodeName(String endNodeName) {
        this.endNodeName = endNodeName;
    }

    public String getStartNodeType() {
        return startNodeType;
    }

    public void setStartNodeType(String startNodeType) {
        this.startNodeType = startNodeType;
    }

    public String getEndNodeType() {
        return endNodeType;
    }

    public void setEndNodeType(String endNodeType) {
        this.endNodeType = endNodeType;
    }

    public String getStartNodeObjectType() {
        return startNodeObjectType;
    }

    public void setStartNodeObjectType(String startNodeObjectType) {
        this.startNodeObjectType = startNodeObjectType;
    }

    public String getEndNodeObjectType() {
        return endNodeObjectType;
    }

    public void setEndNodeObjectType(String endNodeObjectType) {
        this.endNodeObjectType = endNodeObjectType;
    }
}
