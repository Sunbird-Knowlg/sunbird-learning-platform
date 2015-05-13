package com.ilimi.taxonomy.dto;

import java.io.Serializable;

public class NodeDTO implements Serializable {

    private static final long serialVersionUID = -3083582629330476187L;
    private String identifier;
    private String name;
    private String objectType;
    
    public NodeDTO() {
        
    }
    
    public NodeDTO(String identifier, String name, String objectType) {
        this.identifier = identifier;
        this.name = name;
        this.objectType = objectType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }
}
