package org.ekstep.graph.model;

import java.util.List;
import java.util.Map;

import org.ekstep.common.dto.Request;

/**
 * @author rayulu
 * 
 */
public interface IRelation extends IPropertyContainer {

    void validate(Request request);

    String getRelationType();

    String getStartNodeId();

    String getEndNodeId();
    
    Map<String, Object> getMetadata();

    boolean isType(String relationType);

	String createRelation(final Request req);
    
	String deleteRelation(Request req);

	Map<String, List<String>> validateRelation(Request request);

}
