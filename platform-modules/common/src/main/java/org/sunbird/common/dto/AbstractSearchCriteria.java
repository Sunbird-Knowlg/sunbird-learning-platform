package org.sunbird.common.dto;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.MetadataCriterion;
import org.sunbird.graph.dac.model.RelationCriterion;
import org.sunbird.graph.dac.model.SearchCriteria;
import org.sunbird.graph.dac.model.Sort;

public abstract class AbstractSearchCriteria {

    private MetadataCriterion metadata;
    private List<RelationCriterion> relations;
    private int resultSize = 50;
    private int startPosition = 0;
    private List<Sort> sortOrder = new LinkedList<Sort>();

    public MetadataCriterion getMetadata() {
        return metadata;
    }

    public void setMetadata(MetadataCriterion metadata) {
        this.metadata = metadata;
    }

    public List<RelationCriterion> getRelations() {
        return relations;
    }

    public void setRelations(List<RelationCriterion> relations) {
        this.relations = relations;
    }

    public int getResultSize() {
        return resultSize;
    }

    public void setResultSize(int resultSize) {
        if(resultSize > 0)
            this.resultSize = resultSize;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int startPosition) {
        if(startPosition >= 0)
            this.startPosition = startPosition;
    }
    
    @JsonIgnore
    public abstract SearchCriteria getSearchCriteria();
    
    protected SearchCriteria getSearchCriteria(String objectType) {
        SearchCriteria sc = new SearchCriteria();
        sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
        if(StringUtils.isNotBlank(objectType)) sc.setObjectType(objectType);
        if(null != getMetadata()) sc.addMetadata(getMetadata());
        if(null != getRelations() && getRelations().size() > 0) {
            sc.setRelations(getRelations());
        }
        sc.setResultSize(getResultSize());
        sc.setStartPosition(getStartPosition());
        if (null != getSortOrder() && getSortOrder().size() > 0) {
            sc.setSortOrder(sortOrder);
        }
        return sc;
    }

    public List<Sort> getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(List<Sort> sortOrder) {
        this.sortOrder = sortOrder;
    }
}
