package com.ilimi.graph.common.dto;

import java.util.Map;

/**
 * Value Object for an Map<String, String>
 * 
 * @author rayulu
 * 
 */
public class StringValueMap extends BaseValueObject {

    private static final long serialVersionUID = 7787251219842727144L;

    public StringValueMap(Map<String, String> valueMap) {
        super();
        this.valueMap = valueMap;
    }

    public StringValueMap() {
        super();
    }

    private Map<String, String> valueMap;

    /**
     * @return the valueMap
     */
    public Map<String, String> getValueMap() {
        return valueMap;
    }

    /**
     * @param valueMap
     *            the valueMap to set
     */
    public void setValueMap(Map<String, String> valueMap) {
        this.valueMap = valueMap;
    }

}
