package com.ilimi.graph.model;

import java.util.List;
import java.util.Map;

import scala.concurrent.Future;

import com.ilimi.graph.common.Request;

/**
 * @author rayulu
 * 
 */
public interface IRelation extends IPropertyContainer {

    void validate(Request request);

    String getRelationType();

    String getStartNodeId();

    String getEndNodeId();

    boolean isType(String relationType);

    Future<String> createRelation(final Request req);
    
    Future<String> deleteRelation(Request req);

    Future<Map<String, List<String>>> validateRelation(Request request);

}
