package org.sunbird.graph.reader;

import java.util.List;
import java.util.Map;

import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;

/**
 * 
 * @author mahesh
 *
 */

public interface GraphReader {

    public List<Node> getDefinitionNodes();
    public void setDefinitionNodes(List<Node> definitionNodes);
    public List<Node> getDataNodes();
    public void setDataNodes(List<Node> dataNodes);
    public List<Relation> getRelations();
    public void setRelations(List<Relation> relations);
    public List<String> getValidations();
    public Map<String, List<String>> getTagMembersMap();
    public void setTagMembersMap(Map<String, List<String>> tagMembersMap);
}
