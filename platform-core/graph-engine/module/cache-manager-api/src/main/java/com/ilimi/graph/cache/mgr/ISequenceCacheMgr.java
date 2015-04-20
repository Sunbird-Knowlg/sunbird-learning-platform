package com.ilimi.graph.cache.mgr;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.dto.BaseValueObjectList;
import com.ilimi.graph.common.dto.BooleanValue;
import com.ilimi.graph.common.dto.LongIdentifier;
import com.ilimi.graph.common.dto.StringValue;

public interface ISequenceCacheMgr {

    void createSequence(Request request);

    LongIdentifier addSequenceMember(Request request);

    void removeSequenceMember(Request request);

    void dropSequence(Request request);

    BaseValueObjectList<StringValue> getSequenceMembers(Request request);

    LongIdentifier getSequenceCardinality(Request request);
    
    BooleanValue isSequenceMember(Request request);
}
