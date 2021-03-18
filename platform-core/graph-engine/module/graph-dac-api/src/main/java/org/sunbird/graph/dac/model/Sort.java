package org.sunbird.graph.dac.model;

import java.io.Serializable;

public class Sort implements Serializable {

    private static final long serialVersionUID = -4465970370806149413L;
    public static final String SORT_ASC = "ASC";
    public static final String SORT_DESC = "DESC";

    private String sortField;
    private String sortOrder = SORT_ASC;

    public Sort() {
    }

    public Sort(String sortField) {
        this.sortField = sortField;
    }

    public Sort(String sortField, String sortOrder) {
        this.sortField = sortField;
        setSortOrder(sortOrder);
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        if (null == sortOrder || (!sortOrder.equalsIgnoreCase(SORT_ASC) && !sortOrder.equalsIgnoreCase(SORT_DESC)))
            sortOrder = SORT_ASC;
        this.sortOrder = sortOrder.trim().toUpperCase();
    }
    
}
