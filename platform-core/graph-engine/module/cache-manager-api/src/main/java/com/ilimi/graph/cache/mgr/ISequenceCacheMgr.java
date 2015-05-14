package com.ilimi.graph.cache.mgr;

import java.util.List;

import com.ilimi.common.dto.Request;

public interface ISequenceCacheMgr {

    void createSequence(Request request);

    Long addSequenceMember(Request request);

    void removeSequenceMember(Request request);

    void dropSequence(Request request);

    List<String> getSequenceMembers(Request request);

    Long getSequenceCardinality(Request request);

    Boolean isSequenceMember(Request request);
}
