package com.ilimi.graph.dac.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemProperties;

public class TagCriterion implements Serializable {

    private static final long serialVersionUID = -8964080910603198695L;
    private List<String> tags;
    private boolean optional;
    
    public TagCriterion(List<String> tags) {
        
        this.tags = tags;
    }

    public List<String> getTags() {
        return tags;
    }

    public void addTag(String tag) {
        if(null == tags)
            tags = new ArrayList<String>();
        if(StringUtils.isNotBlank(tag))
            tags.add(tag);
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public String getCypher(SearchCriteria sc, String param) {
        StringBuilder sb = new StringBuilder();
        if (null != tags && tags.size() > 0) {
            String tagParam = "n" + sc.index;
            sc.index += 1;
            sb.append(" WITH n ");
            if (!StringUtils.equals("n", param))
                sb.append(", ").append(param).append(", ");
            sb.append("MATCH ");
            if (optional)
                sb.append("OPTIONAL ");
            sb.append("(").append(param).append(")<-[:").append(RelationTypes.SET_MEMBERSHIP.relationName()).append("]-(").append(tagParam)
                    .append(") WHERE ( ");
            sb.append(tagParam).append(".").append(SystemProperties.IL_TAG_NAME.name()).append(" IN {").append(sc.pIndex).append("} ");
            sc.params.put("" + sc.pIndex, tags);
            sc.pIndex += 1;
            sb.append(") ");
        }
        return sb.toString();
    }
}
