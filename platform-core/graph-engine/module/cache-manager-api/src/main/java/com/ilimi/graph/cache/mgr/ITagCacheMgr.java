package com.ilimi.graph.cache.mgr;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.dto.BaseValueObjectList;
import com.ilimi.graph.common.dto.BooleanValue;
import com.ilimi.graph.common.dto.LongIdentifier;
import com.ilimi.graph.common.dto.StringValue;

public interface ITagCacheMgr {

    void createTag(Request request);

    void addTagMember(Request request);

    void addTagMembers(Request request);

    void removeTagMember(Request request);

    void dropTag(Request request);

    BaseValueObjectList<StringValue> getTagMembers(Request request);

    LongIdentifier getCardinality(Request request);

    BooleanValue isTagMember(Request request);
}
