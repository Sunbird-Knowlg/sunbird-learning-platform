package com.ilimi.graph.common.dto;

import java.util.List;

/**
 * Value Object for an identifier
 * 
 * @author rayulu
 * 
 */
public class IdentifierList extends BaseValueObject {

    /**
     * 
     */
    private static final long serialVersionUID = 8704350330157971654L;

    public IdentifierList(List<Integer> idsList) {
        super();
        this.idsList = idsList;
    }

    private List<Integer> idsList;

    /**
     * @return the idsList
     */
    public List<Integer> getIdsList() {
        return idsList;
    }

    /**
     * @param idsList
     *            the idsList to set
     */
    public void setIdsList(List<Integer> idsList) {
        this.idsList = idsList;
    }

}
