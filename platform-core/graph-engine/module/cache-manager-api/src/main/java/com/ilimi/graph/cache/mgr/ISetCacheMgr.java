package com.ilimi.graph.cache.mgr;

import java.util.List;

import com.ilimi.common.dto.Request;

public interface ISetCacheMgr {

    void createSet(Request request);

    void addSetMember(Request request);

    void addSetMembers(Request request);

    void removeSetMember(Request request);

    void dropSet(Request request);

    List<String> getSetMembers(Request request);

    Long getSetCardinality(Request request);

    Boolean isSetMember(Request request);
}
