package org.ekstep.content.mgr.impl;

import org.ekstep.common.dto.Response;
import org.ekstep.content.mgr.impl.dialcodes.LinkDialCodeManager;
import org.ekstep.content.mgr.impl.dialcodes.ReleaseDialcodesManager;
import org.ekstep.content.mgr.impl.dialcodes.ReserveDialcodesManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class DialCodesManager {

    @Autowired
    private LinkDialCodeManager linkDialCodeManager;

    @Autowired private ReserveDialcodesManager reserveDialcodesManager;

    @Autowired private ReleaseDialcodesManager releaseDialcodesManager;

    public Response link(String channelId, Object reqObj) throws Exception {
        return this.linkDialCodeManager.linkDialCode(channelId, reqObj);
    }

    public Response reserve(String contentId, String channelId, Map<String, Object> request) throws Exception {
        return this.reserveDialcodesManager.reserveDialCode(contentId, channelId, request);
    }

    public Response release(String contentId, String channelId) throws Exception {
        return this.releaseDialcodesManager.releaseDialCodes(contentId, channelId);
    }

}
