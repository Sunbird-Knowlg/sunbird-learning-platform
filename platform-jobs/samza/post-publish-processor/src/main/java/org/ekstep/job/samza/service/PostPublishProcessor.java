package org.ekstep.job.samza.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.Platform;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.job.samza.util.DIALCodeUtil;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.JSONUtils;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.jobs.samza.util.SamzaCommonParams;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.learning.util.ControllerUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Service Class Which Will Perform the activity based on event action
 *
 * @author Kumar Gauraw
 */
public class PostPublishProcessor implements ISamzaService {

    private static JobLogger LOGGER = new JobLogger(PostPublishProcessor.class);
    private Config config = null;
    private static final List<String> ACTIONS = Platform.config.hasPath("post_publish_processor.actions") ?
            Arrays.asList(Platform.config.getString("post_publish_processor.actions").split(",")) : Collections.emptyList();
    private static final List<String> CONTENT_TYPES = Platform.config.hasPath("post_publish_processor.contentTypes") ?
            Arrays.asList(Platform.config.getString("post_publish_processor.contentTypes").split(",")) : Collections.emptyList();
    private static final Integer MAX_ITERATION_COUNT = (Platform.config.hasPath("event.max.iteration.count")) ?
            Platform.config.getInt("event.max.iteration.count") : 1;
    private ControllerUtil util = new ControllerUtil();
    private DIALCodeUtil dialUtil = new DIALCodeUtil();


    /**
     * @param config
     * @throws Exception
     */
    @Override
    public void initialize(Config config) throws Exception {
        this.config = config;
        JSONUtils.loadProperties(config);
        LOGGER.info("Service config initialized");
        LearningRequestRouterPool.init();
        LOGGER.info("Learning Actor System initialized");
    }

    /**
     * The class processMessage is mainly responsible for processing the messages sent from consumers based on required
     * specifications
     *
     * @param message
     * @param metrics
     * @param collector
     */
    @Override
    public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector) throws Exception {
        if (null == message) {
            LOGGER.info("Null Event Received. So Skipped Processing.");
            return;
        }
        Map<String, Object> edata = (Map<String, Object>) message.get(SamzaCommonParams.edata.name());
        Map<String, Object> object = (Map<String, Object>) message.get(SamzaCommonParams.object.name());

        if (!validateEvent(edata, object)) {
            LOGGER.info("Event Ignored. Event Validation Failed for post-publish-processor operations.");
            return;
        }

        switch (((String) edata.get("action")).toLowerCase()) {
            case "dialcode_link": {
                String nodeId = (String) object.get("id");
                Node node = util.getNode(SamzaCommonParams.domain.name(), nodeId);
                if (null != node && MapUtils.isNotEmpty(node.getMetadata())) {
                    List<String> dialcodes = dialUtil.getDialCodes(node);
                    if (CollectionUtils.isNotEmpty(dialcodes)) {
                        LOGGER.info("Event Skipped. Target Object (" + nodeId + ") already has DIAL Code.| DIAL Codes : " + dialcodes);
                    } else {
                        dialUtil.linkDialCode(node);
                    }
                } else {
                    LOGGER.info("Event Skipped. Target Object (" + nodeId + ") metadata is null.");
                }
                break;
            }

            default: {
                LOGGER.info("Event Ignored. Event Action Doesn't match for post-publish-processor operations.");
            }
        }
    }

    /**
     * This Method Performs Basic Validation of Event.
     *
     * @param edata
     * @param object
     * @return
     */
    private boolean validateEvent(Map<String, Object> edata, Map<String, Object> object) {
        if (MapUtils.isEmpty(object) || StringUtils.isBlank((String) object.get("id")) ||
                MapUtils.isEmpty(edata) || StringUtils.isBlank((String) edata.get("action")))
            return false;
        String action = (String) edata.get("action");
        Integer iteration = (Integer) edata.get(SamzaCommonParams.iteration.name());
        String contentType = (String) edata.get("contentType");
        return (ACTIONS.contains(action) && iteration <= MAX_ITERATION_COUNT && CONTENT_TYPES.contains(contentType));
    }

}
