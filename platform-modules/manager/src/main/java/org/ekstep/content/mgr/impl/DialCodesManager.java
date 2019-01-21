package org.ekstep.content.mgr.impl;

import org.ekstep.common.dto.Response;
import org.ekstep.content.mgr.impl.operation.dialcodes.LinkDialCodeOperation;
import org.ekstep.content.mgr.impl.operation.dialcodes.ReleaseDialcodesOperation;
import org.ekstep.content.mgr.impl.operation.dialcodes.ReserveDialcodesOperation;

import java.util.Map;

public class DialCodesManager {

	private final LinkDialCodeOperation linkDialCodeOperation = new LinkDialCodeOperation();
    private final ReserveDialcodesOperation reserveDialcodesOperation = new ReserveDialcodesOperation();
    private final ReleaseDialcodesOperation releaseDialcodesOperation = new ReleaseDialcodesOperation();

    public Response link(String channelId, Object reqObj) throws Exception {
        return this.linkDialCodeOperation.linkDialCode(channelId, reqObj);
    }

    public Response reserve(String contentId, String channelId, Map<String, Object> request) throws Exception {
        return this.reserveDialcodesOperation.reserveDialCode(contentId, channelId, request);
    }

    public Response release(String contentId, String channelId) throws Exception {
        return this.releaseDialcodesOperation.releaseDialCodes(contentId, channelId);
    }

}
