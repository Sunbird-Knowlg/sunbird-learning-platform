package org.ekstep.content.mgr.impl.dialcodes;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.util.HttpRestUtil;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.taxonomy.mgr.impl.DummyBaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReserveDialcodesManager extends DummyBaseContentManager {

    public Response reserveDialCode(String contentId, String channelId, Map<String, Object> request) throws Exception {
        if(null == request || request.isEmpty())
            throw new ClientException(ContentErrorCodes.ERR_REQUEST_BLANK.name(),
                    "Request can not be blank.");

        if(StringUtils.isBlank(channelId)) {
            throw new ClientException(ContentErrorCodes.ERR_CHANNEL_BLANK_OBJECT.name(),
                    "Channel can not be blank.");
        }
        if(StringUtils.isBlank(contentId)) {
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT.name(),
                    "Content Id Can Not be blank.");
        }
        Node node = getContentNode(TAXONOMY_ID, contentId, "edit");
        Map<String, Object> metaData = node.getMetadata();

        validateContentForReservedDialcodes(metaData);

        validateChannel(metaData, channelId);

        validateCountForReservingDialCode(request);

        boolean updateContent = false;
        List<String> dialCodes = getReservedDialCodes(node).orElseGet(ArrayList::new);

        int reqDialcodesCount = (Integer) request.get(ContentAPIParams.count.name());
        while(dialCodes.size()<reqDialcodesCount) {
            dialCodes.addAll(generateDialcode(channelId, contentId, reqDialcodesCount-dialCodes.size(), (String) request.get(ContentAPIParams.publisher.name())));
            updateContent = true;
        }

        Response updateResponse;
        if(updateContent) {
            Map<String, Object> reqMap = new HashMap<>();
            reqMap.put(ContentAPIParams.reservedDialcodes.name(), dialCodes);
            updateResponse = updateAllContents(contentId, reqMap);
        }else {
            updateResponse = getClientErrorResponse();
            updateResponse.put(ContentAPIParams.messages.name(),
                    "No new DIAL Codes have been generated, as requested count is less or equal to existing reserved dialcode count.");
            updateResponse.put(ContentAPIParams.count.name(), dialCodes.size());
            updateResponse.put(ContentAPIParams.reservedDialcodes.name(), dialCodes);
            updateResponse.put(ContentAPIParams.node_id.name(), contentId);
            return updateResponse;
        }
        if(updateResponse.getResponseCode() == ResponseCode.OK) {
            updateResponse.put(ContentAPIParams.count.name(), dialCodes.size());
            updateResponse.put(ContentAPIParams.reservedDialcodes.name(), dialCodes);
            updateResponse.put(ContentAPIParams.node_id.name(), contentId);
            TelemetryManager.info("DIAL Codes generated and reserved.", updateResponse.getResult());
            return updateResponse;
        }else {
            return updateResponse;
        }

    }

    protected void validateCountForReservingDialCode(Map<String, Object> request) {
        if(null == request.get(ContentAPIParams.count.name()) ||
                !(request.get(ContentAPIParams.count.name()) instanceof Integer)) {
            throw new ClientException(ContentErrorCodes.ERR_INVALID_COUNT.name(),
                    "Invalid dialcode count.");
        }
        int count = (Integer)request.get(ContentAPIParams.count.name());
        int maxCount = Platform.config.hasPath("learnig.reserve_dialcode.max_count") ?
                Platform.config.getInt("learnig.reserve_dialcode.max_count") : 250;
        if(count<1 || count>maxCount)
            throw new ClientException(ContentErrorCodes.ERR_INVALID_COUNT.name(),
                    "Invalid dialcode count range. Its hould be between 1 to " + maxCount + ".");
    }

    private List<String> generateDialcode(String channelId, String contentId, int dialcodeCount, String publisher) throws Exception{
        Map<String, Object> request = new HashMap<>();
        Map<String, Object> dialcodeMap = new HashMap<>();
        dialcodeMap.put(ContentAPIParams.count.name(), dialcodeCount);
        dialcodeMap.put(ContentAPIParams.publisher.name(), publisher);
        dialcodeMap.put(ContentAPIParams.batchCode.name(), contentId);
        request.put(ContentAPIParams.dialcodes.name(), dialcodeMap);
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put(ContentAPIParams.request.name(), request);
        Map<String, String> headerParam = new HashMap<String, String>();
        headerParam.put("X-Channel-Id", channelId);
        Response generateResponse = HttpRestUtil.makePostRequest(DIALCODE_GENERATE_URI, requestMap, headerParam);
        if (generateResponse.getResponseCode() == ResponseCode.OK || generateResponse.getResponseCode() == ResponseCode.PARTIAL_SUCCESS) {
            Map<String, Object> result = generateResponse.getResult();
            List<String> generatedDialCodes = (List<String>)result.get(ContentAPIParams.dialcodes.name());
            if(!generatedDialCodes.isEmpty())
                return generatedDialCodes;
            else
                throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(),
                        "Dialcode generated list is empty. Please Try Again After Sometime!");
        }else {
            if (generateResponse.getResponseCode() == ResponseCode.CLIENT_ERROR) {
                TelemetryManager.error("Client Error during Generate Dialcode: " + generateResponse.getParams().getErrmsg() + " :: " + generateResponse.getResult());
                throw new ClientException(generateResponse.getParams().getErr(), generateResponse.getParams().getErrmsg());
            }
            else {
                TelemetryManager.error("Server Error during Generate Dialcode: " + generateResponse.getParams().getErrmsg() + " :: " + generateResponse.getResult());
                throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(),
                        "Error During generate Dialcode. Please Try Again After Sometime!");
            }
        }

    }

}
