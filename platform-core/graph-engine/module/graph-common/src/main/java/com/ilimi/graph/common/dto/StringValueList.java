package com.ilimi.graph.common.dto;

import java.util.List;

/**
 * Value Object for an identifier
 * 
 * @author rayulu
 * 
 */
public class StringValueList extends BaseValueObject {

    private static final long serialVersionUID = -1178349422723507345L;

    public StringValueList(List<String> idsList) {
        super();
        this.idsList = idsList;
    }

    public StringValueList() {
        super();
    }

    private List<String> idsList;

    /**
     * @return the idsList
     */
    public List<String> getIdsList() {
        return idsList;
    }

    /**
     * @param idsList
     *            the idsList to set
     */
    public void setIdsList(List<String> idsList) {
        this.idsList = idsList;
    }

}
