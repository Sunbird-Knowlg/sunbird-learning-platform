package org.ekstep.graph.model.node;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class RelationDefinition implements Serializable {

    private static final long serialVersionUID = -1396069778690991558L;
    private String relationName;
    private List<String> objectTypes;
    private String title;
    private String description;
    private boolean required;
    private String renderingHints;

    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public List<String> getObjectTypes() {
        return objectTypes;
    }

    public void setObjectTypes(List<String> objectTypes) {
        this.objectTypes = objectTypes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getRenderingHints() {
        return renderingHints;
    }

    public void setRenderingHints(String renderingHints) {
        this.renderingHints = renderingHints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelationDefinition that = (RelationDefinition) o;
        return Objects.equals(relationName, that.relationName) &&
                Objects.equals(objectTypes, that.objectTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(relationName, objectTypes);
    }
}
