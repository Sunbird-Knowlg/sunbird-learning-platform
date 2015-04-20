package com.ilimi.graph.importer;

import java.util.List;
import java.util.Map;

import com.ilimi.graph.common.dto.StringValue;
import com.ilimi.graph.common.dto.BaseValueObject;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

public class ImportData extends BaseValueObject {

    private static final long serialVersionUID = 4500122507402085192L;

    private List<Node> definitionNodes;
    private List<Node> dataNodes;
    private Map<String, List<StringValue>> tagMembersMap;
    private List<Relation> relations;

    public ImportData(List<Node> definitionNodes, List<Node> dataNodes, List<Relation> relations,
            Map<String, List<StringValue>> tagMembersMap) {
        this.definitionNodes = definitionNodes;
        this.dataNodes = dataNodes;
        this.tagMembersMap = tagMembersMap;
        this.relations = relations;
    }

    /**
     * @return the definitionNodes
     */
    public List<Node> getDefinitionNodes() {
        return definitionNodes;
    }

    /**
     * @param definitionNodes
     *            the definitionNodes to set
     */
    public void setDefinitionNodes(List<Node> definitionNodes) {
        this.definitionNodes = definitionNodes;
    }

    /**
     * @return the dataNodes
     */
    public List<Node> getDataNodes() {
        return dataNodes;
    }

    /**
     * @param dataNodes
     *            the dataNodes to set
     */
    public void setDataNodes(List<Node> dataNodes) {
        this.dataNodes = dataNodes;
    }

    /**
     * @return the relations
     */
    public List<Relation> getRelations() {
        return relations;
    }

    /**
     * @param relations
     *            the relations to set
     */
    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }

    /**
     * @return the tagMembersMap
     */
    public Map<String, List<StringValue>> getTagMembersMap() {
        return tagMembersMap;
    }

    /**
     * @param tagMembersMap
     *            the tagMembersMap to set
     */
    public void setTagMembersMap(Map<String, List<StringValue>> tagMembersMap) {
        this.tagMembersMap = tagMembersMap;
    }

}
