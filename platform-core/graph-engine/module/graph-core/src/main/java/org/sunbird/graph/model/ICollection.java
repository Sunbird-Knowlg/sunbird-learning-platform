package org.sunbird.graph.model;

import org.sunbird.common.dto.Request;

public interface ICollection extends INode {

    void addMember(Request request);
    
    void addMembers(Request request);
    
    void removeMember(Request request);
    
    void removeMembers(Request request);
    
    void getMembers(Request request);
    
    void isMember(Request request);
    
    void getCardinality(Request request);
    
}
