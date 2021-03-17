package org.sunbird.graph.model.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MetadataDefinition implements Serializable {

    public static List<String> VALID_DATA_TYPES = new ArrayList<String>();
    
    public static List<String> PLATFORM_OBJECTS_AS_DATA_TYPE = Arrays.asList("term", "termlist");

    static {
        VALID_DATA_TYPES.add("text");
        VALID_DATA_TYPES.add("number");
        VALID_DATA_TYPES.add("boolean");
        VALID_DATA_TYPES.add("select");
        VALID_DATA_TYPES.add("multi-select");
        VALID_DATA_TYPES.add("list");
        VALID_DATA_TYPES.add("date");
        VALID_DATA_TYPES.add("url");
        VALID_DATA_TYPES.add("json");
        VALID_DATA_TYPES.add("xml");
        VALID_DATA_TYPES.add("external");
        VALID_DATA_TYPES.addAll(PLATFORM_OBJECTS_AS_DATA_TYPE);
    }

    private static final long serialVersionUID = -6210344089409649758L;
    private boolean required;
    // Text, Number, Boolean, Select, Multi-Select, List
    private String dataType = "Text";
    private String propertyName;
    private String title;
    private String description;
    private String category;
    private String displayProperty = "Editable";
    private List<Object> range;
    private Object defaultValue;
    private String renderingHints;
    private boolean indexed;
    private boolean draft;
    private boolean rangeValidation = true;
    
    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Object> getRange() {
        return range;
    }

    public void setRange(List<Object> range) {
        this.range = range;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getRenderingHints() {
        return renderingHints;
    }

    public void setRenderingHints(String renderingHints) {
        this.renderingHints = renderingHints;
    }

    public String getDisplayProperty() {
        return displayProperty;
    }

    public void setDisplayProperty(String displayProperty) {
        this.displayProperty = displayProperty;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    public boolean isDraft() {
        return draft;
    }

    public void setDraft(boolean draft) {
        this.draft = draft;
    }

	/**
	 * @return the rangeValidation
	 */
	public boolean getRangeValidation() {
		return rangeValidation;
	}

	/**
	 * @param rangeValidation the rangeValidation to set
	 */
	public void setRangeValidation(boolean rangeValidation) {
		this.rangeValidation = rangeValidation;
	}
    
    
}
