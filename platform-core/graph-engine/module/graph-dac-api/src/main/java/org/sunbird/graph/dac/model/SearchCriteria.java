package org.sunbird.graph.dac.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.sunbird.graph.dac.enums.SystemProperties;

public class SearchCriteria implements Serializable {

    private static final long serialVersionUID = -6991536066924072138L;
    private String graphId;
	private String nodeType;
    private String objectType;
    private String op;
    private List<MetadataCriterion> metadata;
    private List<RelationCriterion> relations;
    private boolean countQuery;
    private int resultSize = 0;
    private int startPosition = 0;
    private List<String> fields = new LinkedList<String>();
    private List<Sort> sortOrder = new LinkedList<Sort>();

    Map<String, Object> params = new HashMap<String, Object>();
    int pIndex = 1;
    int index = 1;

    public Map<String, Object> getParams() {
        return this.params;
    }

    public List<MetadataCriterion> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<MetadataCriterion> metadata) {
        this.metadata = metadata;
    }

    public void addMetadata(MetadataCriterion mc) {
        if (null == metadata)
            metadata = new ArrayList<MetadataCriterion>();
        metadata.add(mc);
    }

    public String getOp() {
        if (StringUtils.isBlank(this.op))
            this.op = SearchConditions.LOGICAL_AND;
        return op;
    }

    public void setOp(String op) {
        if (StringUtils.equalsIgnoreCase(SearchConditions.LOGICAL_OR, op))
            this.op = SearchConditions.LOGICAL_OR;
        else
            this.op = SearchConditions.LOGICAL_AND;
    }

    public boolean isCountQuery() {
        return countQuery;
    }

    public void setCountQuery(boolean countQuery) {
        this.countQuery = countQuery;
    }

    public int getResultSize() {
        return resultSize;
    }

    public void setResultSize(int resultSize) {
        this.resultSize = resultSize;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }
    
    public String getGraphId() {
		return graphId;
	}

	public void setGraphId(String graphId) {
		this.graphId = graphId;
	}

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public List<RelationCriterion> getRelations() {
        return relations;
    }

    public void setRelations(List<RelationCriterion> relations) {
        this.relations = relations;
    }

    public void addRelationCriterion(RelationCriterion rc) {
        if (null == relations)
            relations = new ArrayList<RelationCriterion>();
        relations.add(rc);
    }

    @JsonIgnore
    public String getQuery() {
        StringBuilder sb = new StringBuilder();
        pIndex = 1;
        sb.append("MATCH (ee:" + (StringUtils.isBlank(graphId) ? "NODE" : graphId) + ") ");
        if (StringUtils.isNotBlank(nodeType) || StringUtils.isNotBlank(objectType)
                || (null != metadata && metadata.size() > 0)) {
            sb.append("WHERE ( ");
            if (StringUtils.isNotBlank(nodeType)) {
                sb.append(" ee.").append(SystemProperties.IL_SYS_NODE_TYPE.name()).append(" = {").append(pIndex)
                        .append("} ");
                params.put("" + pIndex, nodeType);
                pIndex += 1;
            }
            if (StringUtils.isNotBlank(objectType)) {
                if (pIndex > 1)
                    sb.append("AND ");
                sb.append(" ee.").append(SystemProperties.IL_FUNC_OBJECT_TYPE.name()).append(" = {").append(pIndex)
                        .append("} ");
                params.put("" + pIndex, objectType);
                pIndex += 1;
            }
            if (null != metadata && metadata.size() > 0) {
                if (pIndex > 1)
                    sb.append("AND ");
                for (int i = 0; i < metadata.size(); i++) {
                    String metadataCypher = metadata.get(i).getCypher(this, "ee");
                    if (StringUtils.isNotBlank(metadataCypher)) {
                        sb.append(metadataCypher);
                        if (i < metadata.size() - 1)
                            sb.append(" ").append(getOp()).append(" ");
                    }
                }
            }
            sb.append(") ");
        }
        if (null != relations && relations.size() > 0) {
            for (RelationCriterion rel : relations)
                sb.append(rel.getCypher(this, null));
        }
        if (!countQuery) {
        	boolean returnNode = true;
            if (null == fields || fields.isEmpty()) {
            	sb.append("WITH DISTINCT ee ");
            	if (null != sortOrder && sortOrder.size() > 0) {
                    sb.append("ORDER BY ");
                    for (int i = 0; i < sortOrder.size(); i++) {
                        Sort sort = sortOrder.get(i);
                        sb.append("ee.").append(sort.getSortField()).append(" ");
                        if (StringUtils.equals(Sort.SORT_DESC, sort.getSortOrder())) {
                            sb.append("DESC ");
                        }
                        if (i < sortOrder.size() - 1)
                            sb.append(", ");
                    }
                }
            	if (startPosition > 0)
                    sb.append("SKIP ").append(startPosition).append(" ");
                if (resultSize > 0)
                    sb.append("LIMIT ").append(resultSize).append(" ");
                sb.append("OPTIONAL MATCH (ee)-[r]-() RETURN ee, r, startNode(r) as __startNode, endNode(r) as __endNode ");
            } else {
            	returnNode = false;
                sb.append("RETURN ");
                for (int i = 0; i < fields.size(); i++) {
                    sb.append("ee.").append(fields.get(i)).append(" as ").append(fields.get(i)).append(" ");
                    if (i < fields.size() - 1)
                        sb.append(", ");
                }
            }
            if (!returnNode) {
            	if (null != sortOrder && sortOrder.size() > 0) {
                    sb.append("ORDER BY ");
                    for (int i = 0; i < sortOrder.size(); i++) {
                        Sort sort = sortOrder.get(i);
                        sb.append("ee.").append(sort.getSortField()).append(" ");
                        if (StringUtils.equals(Sort.SORT_DESC, sort.getSortOrder())) {
                            sb.append("DESC ");
                        }
                        if (i < sortOrder.size() - 1)
                            sb.append(", ");
                    }
                }
            	if (startPosition > 0) {
                    sb.append("SKIP ").append(startPosition).append(" ");
                }
                if (resultSize > 0) {
                    sb.append("LIMIT ").append(resultSize).append(" ");
                }
            }
        } else {
            sb.append("RETURN count(ee) as __count");
        }
        return sb.toString();
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public List<Sort> getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(List<Sort> sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void sort(Sort sort) {
        if (null == sortOrder)
            sortOrder = new LinkedList<Sort>();
        sortOrder.add(sort);
    }

}
