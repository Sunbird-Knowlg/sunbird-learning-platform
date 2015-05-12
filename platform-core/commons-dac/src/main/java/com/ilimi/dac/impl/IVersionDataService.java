package com.ilimi.dac.impl;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.Response;

public interface IVersionDataService {

    public Response createVersion(Request request);
    public Response getAllVersions(Request request);
    public Response getVersion(Request request);
}
