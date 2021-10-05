package org.sunbird.graph.model;

import java.util.List;
import java.util.Map;

import org.sunbird.common.dto.Request;
import org.sunbird.graph.dac.model.Node;

public interface INode extends IPropertyContainer {

    String getNodeId();

    String getSystemNodeType();

    String getFunctionalObjectType();

    Node toNode();

    void updateMetadata(Request request);

    Map<String, List<String>> validateNode(Request request);

}
