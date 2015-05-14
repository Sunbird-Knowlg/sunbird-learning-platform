package com.ilimi.graph.cache.mgr;

import java.util.List;

import com.ilimi.common.dto.Request;

public interface ITagCacheMgr {

    void createTag(Request request);

    void addTagMember(Request request);

    void addTagMembers(Request request);

    void removeTagMember(Request request);

    void dropTag(Request request);

    List<String> getTagMembers(Request request);

    Long getCardinality(Request request);

    Boolean isTagMember(Request request);
}
