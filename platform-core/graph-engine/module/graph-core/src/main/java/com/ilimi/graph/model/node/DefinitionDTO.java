package com.ilimi.graph.model.node;

import java.io.Serializable;
import java.util.List;

import com.ilimi.graph.model.node.MetadataDefinition;
import com.ilimi.graph.model.node.RelationDefinition;
import com.ilimi.graph.model.node.TagDefinition;

public class DefinitionDTO implements Serializable {

    private static final long serialVersionUID = -228044920246692592L;
    private String identifier;
    private String objectType;
    private List<MetadataDefinition> properties;
    private List<RelationDefinition> inRelations;
    private List<RelationDefinition> outRelations;
    private List<TagDefinition> systemTags;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public List<MetadataDefinition> getProperties() {
        return properties;
    }

    public void setProperties(List<MetadataDefinition> properties) {
        this.properties = properties;
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

    public List<TagDefinition> getSystemTags() {
        return systemTags;
    }

    public void setSystemTags(List<TagDefinition> systemTags) {
        this.systemTags = systemTags;
    }
}
