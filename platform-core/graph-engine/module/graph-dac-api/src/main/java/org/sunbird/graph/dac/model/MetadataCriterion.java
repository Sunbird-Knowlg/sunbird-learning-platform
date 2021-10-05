package org.sunbird.graph.dac.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.common.exception.GraphEngineErrorCodes;

public class MetadataCriterion implements Serializable {

    private static final long serialVersionUID = -6805135850224649022L;
    private List<Filter> filters;
    private String op;
    private List<MetadataCriterion> metadata;
    private static final String defaultOp = SearchConditions.LOGICAL_AND;
    
    private MetadataCriterion() {}
    
    public static MetadataCriterion create(List<Filter> filters, List<MetadataCriterion> metadata) {
        return create(filters, metadata, defaultOp);
    }
    
    public static MetadataCriterion create(List<Filter> filters, List<MetadataCriterion> metadata, String op) {
        if(null == filters && null == metadata) throw new ServerException(GraphEngineErrorCodes.ERR_META_CRITERION_INVALID_DATA.name(), "Filters and metadata criterion list is null.");
        MetadataCriterion criterion = new MetadataCriterion();
        criterion.filters = filters;
        criterion.metadata = metadata;
        if(StringUtils.isNotBlank(op)) 
            criterion.op = op;
        else
            criterion.op = defaultOp;
        return criterion;
    }
    
    public static MetadataCriterion create(List<Filter> filters) {
        return create(filters, defaultOp);
    }
    
    public static MetadataCriterion create(List<Filter> filters, String op) {
        if(null == filters) throw new ServerException(GraphEngineErrorCodes.ERR_META_CRITERION_INVALID_DATA.name(), "Filters list is null.");
        MetadataCriterion criterion = new MetadataCriterion();
        criterion.filters = filters;
        criterion.setOp(op);
        return criterion;
    }
        
    public static MetadataCriterion createWithNestedCriterion(List<MetadataCriterion> metadata) {
        return createWithNestedCriterion(metadata,  defaultOp);
    }
    
    public static MetadataCriterion createWithNestedCriterion(List<MetadataCriterion> metadata, String op) {
        if(null == metadata) throw new ServerException(GraphEngineErrorCodes.ERR_META_CRITERION_INVALID_DATA.name(), "Metadata criterion list is null.");
        MetadataCriterion criterion = new MetadataCriterion();
        criterion.metadata = metadata;
        criterion.setOp(op);
        return criterion;
    }
    
    public String getOp() {
        if (StringUtils.isBlank(this.op))
            this.op = defaultOp;
        return op;
    }

    public void setOp(String op) {
        if (StringUtils.equalsIgnoreCase(SearchConditions.LOGICAL_OR, op))
            this.op = SearchConditions.LOGICAL_OR;
        else
            this.op = defaultOp;
    }

    public List<Filter> getFilters() {
        return filters;
    }
    
    public List<MetadataCriterion> getMetadata() {
        return metadata;
    }

    public void addFilter(Filter filter) {
        if (null == filters)
            filters = new ArrayList<Filter>();
        filters.add(filter);
    }
    
    public void addMetadata(MetadataCriterion criterion) {
        if (null == metadata)
            metadata = new ArrayList<MetadataCriterion>();
        metadata.add(criterion);
    }

    public String getCypher(SearchCriteria sc, String param) {
        StringBuilder sb = new StringBuilder();
        
        if (null != filters && filters.size() > 0) {
            sb.append("( ");
            for (int i = 0; i < filters.size(); i++) {
                String filterCypher = filters.get(i).getCypher(sc, param);
                if(StringUtils.isNotBlank(filterCypher)) {
                    sb.append(filterCypher);
                    if (i < filters.size() - 1)
                        sb.append(" ").append(getOp()).append(" ");
                }
            }
        }
        if (null != metadata && metadata.size() > 0) {
            if (null != filters && filters.size() > 0)
                sb.append(" ").append(getOp()).append(" ");
            for (int i = 0; i < metadata.size(); i++) {
                String metadataCypher = metadata.get(i).getCypher(sc, param);
                if(StringUtils.isNotBlank(metadataCypher)) {
                    sb.append(metadataCypher);
                    if (i < metadata.size() - 1)
                        sb.append(" ").append(getOp()).append(" ");
                }
            }
        }
        if (null != filters && filters.size() > 0)
            sb.append(") ");
        return sb.toString();
    }
}
