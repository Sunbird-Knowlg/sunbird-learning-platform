package org.sunbird.content.mgr.impl;

import org.sunbird.common.dto.Response;
import org.sunbird.content.mgr.impl.operation.dialcodes.LinkDialCodeOperation;
import org.sunbird.content.mgr.impl.operation.dialcodes.ReleaseDialcodesOperation;
import org.sunbird.content.mgr.impl.operation.dialcodes.ReserveDialcodesOperation;

import java.util.Map;

public class DialCodesManager {

	private final LinkDialCodeOperation linkDialCodeOperation = new LinkDialCodeOperation();
    private final ReserveDialcodesOperation reserveDialcodesOperation = new ReserveDialcodesOperation();
    private final ReleaseDialcodesOperation releaseDialcodesOperation = new ReleaseDialcodesOperation();

    public Response link(String channelId, Object reqObj, String mode, String contentId) throws Exception {
        return this.linkDialCodeOperation.linkDialCode(channelId, reqObj, mode, contentId);
    }

    public Response reserve(String contentId, String channelId, Map<String, Object> request) throws Exception {
        return this.reserveDialcodesOperation.reserveDialCode(contentId, channelId, request);
    }

    public Response release(String contentId, String channelId) throws Exception {
        return this.releaseDialcodesOperation.releaseDialCodes(contentId, channelId);
    }

}
