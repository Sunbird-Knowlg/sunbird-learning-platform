package com.ilimi.graph.model;

import java.util.List;
import java.util.Map;

import scala.concurrent.Future;

import com.ilimi.common.dto.Request;
import com.ilimi.graph.dac.model.Node;

public interface INode extends IPropertyContainer {

    String getNodeId();

    String getSystemNodeType();

    String getFunctionalObjectType();

    Node toNode();

    void updateMetadata(Request request);

    Future<Map<String, List<String>>> validateNode(Request request);

}
