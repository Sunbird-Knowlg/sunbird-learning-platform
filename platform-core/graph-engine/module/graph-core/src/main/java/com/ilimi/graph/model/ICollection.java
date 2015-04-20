package com.ilimi.graph.model;

import com.ilimi.graph.common.Request;

public interface ICollection extends INode {

    void addMember(Request request);
    
    void addMembers(Request request);
    
    void removeMember(Request request);
    
    void getMembers(Request request);
    
    void isMember(Request request);
    
    void getCardinality(Request request);
    
}
