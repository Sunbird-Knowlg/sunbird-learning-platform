package com.ilimi.graph.dac.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.graph.dac.enums.SystemProperties;

public class RelationCriterion implements Serializable {

    private static final long serialVersionUID = 4077508345374294817L;
    private String name;
    private String objectType;
    private String op;
    private List<MetadataCriterion> metadata;
    private List<RelationCriterion> relations;
    private TagCriterion tag;
    private boolean optional;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
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

    public TagCriterion getTag() {
        return tag;
    }

    public void setTag(TagCriterion tag) {
        this.tag = tag;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public String getCypher(SearchCriteria sc, String prevParam) {
        StringBuilder sb = new StringBuilder();
        sb.append("WITH n ");
        if (StringUtils.isNotBlank(prevParam))
            sb.append(", ").append(prevParam).append(" ");
        if (optional)
            sb.append("OPTIONAL ");
        String param = "n" + sc.index;
        sc.index += 1;
        sb.append("MATCH (");
        if (StringUtils.isNotBlank(prevParam))
            sb.append(prevParam);
        else
            sb.append("n");
        sb.append(")-[:").append(name).append("]->").append("(").append(param).append(") ");
        if (StringUtils.isNotBlank(objectType) || (null != metadata && metadata.size() > 0)) {
            sb.append("WHERE ( ");
            if (StringUtils.isNotBlank(objectType)) {
                sb.append(param).append(".").append(SystemProperties.IL_FUNC_OBJECT_TYPE.name()).append(" = {").append(sc.pIndex)
                        .append("} ");
                sc.params.put("" + sc.pIndex, objectType);
                sc.pIndex += 1;
            }
            if (null != metadata && metadata.size() > 0) {
                if (StringUtils.isNotBlank(objectType))
                    sb.append("AND ");
                for (int i = 0; i < metadata.size(); i++) {
                    sb.append(metadata.get(i).getCypher(sc, param));
                    if (i < metadata.size() - 1)
                        sb.append(" ").append(getOp()).append(" ");
                }
            }
            sb.append(") ");
        }
        if (null != tag)
            sb.append(tag.getCypher(sc, param));
        if (null != relations && relations.size() > 0) {
            for (RelationCriterion rel : relations) {
                sb.append(rel.getCypher(sc, param));
            }
        }
        return sb.toString();
    }
}
