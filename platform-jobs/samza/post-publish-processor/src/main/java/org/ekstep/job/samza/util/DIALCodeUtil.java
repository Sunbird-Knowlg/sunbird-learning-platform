package org.ekstep.job.samza.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.util.HttpRestUtil;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.learning.util.ControllerUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility Class for DIAL Code Related Operations
 *
 * @author Kumar Gauraw
 */
public class DIALCodeUtil {

    private static final String RESERVE_DIAL_API_URL = Platform.config.hasPath("reserve_dial_api.url")? Platform.config.getString("reserve_dial_api.url") : "/content/v3/dialcode/reserve/";
    private static final String KP_LEARNING_BASE_URL = Platform.config.hasPath("kp.learning_service.base_url")
            ? Platform.config.getString("kp.learning_service.base_url") : "http://localhost:8080/learning-service";
    private static final String PASSPORT_KEY = Platform.config.getString("graph.passport.key.base");

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static JobLogger LOGGER = new JobLogger(DIALCodeUtil.class);
    private ControllerUtil util = new ControllerUtil();


    /**
     * @param node
     */
    public void linkDialCode(Node node) {
        Map<String, Integer> reservedDials = getReservedDialCodes(node);
        if (MapUtils.isEmpty(reservedDials))
            reservedDials = reserveDialCodes(node);
        // get 0th index dialcode from reserved dials
        String dial = reservedDials.entrySet().stream().filter(entry -> entry.getValue() == 0).map(entry -> entry.getKey()).findFirst().get();
        updateNode(node, dial);
        String channel = (String) node.getMetadata().get(PostPublishParams.channel.name());
        //Generate DIAL Image and upload it to cloud storage
        String qrImageUrl = QRImageUtil.getQRImageUrl(node, dial, channel);
        // Insert QR Image Record into Cassandra DB
        if(StringUtils.isNotBlank(qrImageUrl)){
            QRImageUtil.createQRImageRecord(channel, dial, qrImageUrl);
        }else{
            LOGGER.info("DIAL Code Image Url is Null for" + node.getIdentifier() + " | So Skipping Cassandra DB Update.");
        }
    }

    /**
     * @param node
     * @return
     */
    public List<String> getDialCodes(Node node) {
        Map<String, Object> metadata = node.getMetadata();
        if (MapUtils.isNotEmpty(metadata) && metadata.containsKey(PostPublishParams.dialcodes.name())) {
            try {
                List<String> dialcodes = objectMapper.convertValue(metadata.get(PostPublishParams.dialcodes.name()), new TypeReference<List<String>>() {
                });
                return (dialcodes.stream().filter(f -> StringUtils.isNotBlank(f)).collect(Collectors.toList()));
            } catch (Exception e) {
                LOGGER.info("Exception Occurred While Parsing dialcodes for " + node.getIdentifier() + " | Exception is: " + e);
            }

        }
        return new ArrayList<>();
    }

    /**
     * @param node
     * @return
     */
    private Map<String, Integer> getReservedDialCodes(Node node) {
        try {
            String reservedDialcode = (String) node.getMetadata().get(PostPublishParams.reservedDialcodes.name());
            if (StringUtils.isNotBlank(reservedDialcode))
                return objectMapper.readValue((String) node.getMetadata().get(PostPublishParams.reservedDialcodes.name()), new TypeReference<Map<String, Integer>>() {
                });
        } catch (Exception e) {
            LOGGER.info("Exception Occurred While Parsing reservedDialcodes for " + node.getIdentifier() + " | Exception is: " + e);
        }
        return null;
    }

    /**
     * @param node
     * @return
     */
    private Map<String, Integer> reserveDialCodes(Node node) {
        Map<String, Integer> reservedDials = null;
        try {
            Map<String, Object> request = new HashMap<String, Object>() {{
                put(PostPublishParams.request.name(), new HashMap<String, Object>() {{
                    put(PostPublishParams.dialcodes.name(), new HashMap<String, Object>() {{
                        // Count Value Hard Coded
                        put(PostPublishParams.count.name(), 1);
                        put(PostPublishParams.qrCodeSpec.name(), new HashMap<String, Object>() {{
                            put(PostPublishParams.errorCorrectionLevel.name(), "H");
                        }});
                    }});
                }});
            }};

            Map<String, String> headerParam = new HashMap<String, String>() {{
                put("X-Channel-Id", (String) node.getMetadata().get(PostPublishParams.channel.name()));
            }};

            Response response = HttpRestUtil.makePostRequest(KP_LEARNING_BASE_URL + RESERVE_DIAL_API_URL + node.getIdentifier(), request, headerParam);
            if (response.getResponseCode() == ResponseCode.OK) {
                if (MapUtils.isNotEmpty(response.getResult()))
                    reservedDials = (Map<String, Integer>) response.getResult().get(PostPublishParams.reservedDialcodes.name());
                else
                    LOGGER.info("Empty Result Received While Reserving DialCode for " + node.getIdentifier());
            } else {
                LOGGER.info("Error Response Received While Reserving DialCode for " + node.getIdentifier() + " | Error Response is :" + response);
            }
        } catch (Exception e) {
            LOGGER.info("Exception Occurred While Reserving DialCode for " + node.getIdentifier() + " | Exception is :" + e);
        }
        return reservedDials;
    }

    /**
     *
     * @param node
     * @param dial
     */
    private void updateNode(Node node, String dial) {
        node.getMetadata().put(PostPublishParams.dialcodes.name(), Arrays.asList(dial));
        node.getMetadata().put(PostPublishParams.versionKey.name(), PASSPORT_KEY);
        Response resp = util.updateNode(node);
        if (null != resp && resp.getResponseCode() == ResponseCode.OK)
            LOGGER.info("DIAL Code Linked Successfully for Node : " + node.getIdentifier() + " | DIAL Code is: " + dial);
        else
            LOGGER.info("DIAL Code Link (Node Update) Failed for Node : " + node.getIdentifier() + " | DIAL Code is: " + dial);
    }
}
