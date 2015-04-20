package com.ilimi.graph.cache.mgr;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.dto.BaseValueObjectList;
import com.ilimi.graph.common.dto.BooleanValue;
import com.ilimi.graph.common.dto.LongIdentifier;
import com.ilimi.graph.common.dto.StringValue;

public interface ISetCacheMgr {

    void createSet(Request request);

    void addSetMember(Request request);
    
    void addSetMembers(Request request);

    void removeSetMember(Request request);

    void dropSet(Request request);

    BaseValueObjectList<StringValue> getSetMembers(Request request);

    LongIdentifier getSetCardinality(Request request);

    BooleanValue isSetMember(Request request);
}
