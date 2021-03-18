package org.sunbird.graph.model.node;

import java.io.Serializable;

public class TagDefinition implements Serializable {

    private static final long serialVersionUID = 7143268173918292647L;

    private String name;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
