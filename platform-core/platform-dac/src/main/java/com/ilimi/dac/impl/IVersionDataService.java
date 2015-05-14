package com.ilimi.dac.impl;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

public interface IVersionDataService {

    public Response createVersion(Request request);
    public Response getAllVersions(Request request);
    public Response getVersion(Request request);
}
