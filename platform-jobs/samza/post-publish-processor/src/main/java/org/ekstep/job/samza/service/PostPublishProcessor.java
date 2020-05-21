package org.ekstep.job.samza.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.Platform;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.job.samza.util.CourseBatchUtil;
import org.ekstep.job.samza.util.DIALCodeUtil;
import org.ekstep.job.samza.util.QRImageUtil;
import org.ekstep.job.samza.util.ShallowPublishUtil;
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
    private List<String> ACTIONS = null;
    private List<String> CONTENT_TYPES = null;
    private Integer MAX_ITERATION_COUNT = null;
    private List<String> COURSE_TYPE = null;
    private ControllerUtil util = new ControllerUtil();
    private DIALCodeUtil dialUtil = null;
    private CourseBatchUtil courseBatchUtil = null;
    private ShallowPublishUtil publishUtil = null;

    /**
     * @param config
     * @throws Exception
     */
    @Override
    public void initialize(Config config) throws Exception {
        this.config = config;
        JSONUtils.loadProperties(config);
        LOGGER.info("Service config initialized");
        ACTIONS = Platform.config.hasPath("post_publish_processor.actions") ?
                Arrays.asList(Platform.config.getString("post_publish_processor.actions").split(",")) : Collections.emptyList();
        CONTENT_TYPES = Platform.config.hasPath("post_publish_processor.contentTypes") ?
                Arrays.asList(Platform.config.getString("post_publish_processor.contentTypes").split(",")) : Collections.emptyList();
        MAX_ITERATION_COUNT = (Platform.config.hasPath("max.iteration.count.samza.job")) ?
                Platform.config.getInt("max.iteration.count.samza.job") : 1;
        COURSE_TYPE = Platform.config.hasPath("post_publish_processor.courseTypes") ?
                Arrays.asList(Platform.config.getString("post_publish_processor.courseTypes").split(",")) : Arrays.asList("CurriculumCourse");
        LearningRequestRouterPool.init();
        LOGGER.info("Learning Actor System initialized");
        dialUtil = new DIALCodeUtil();
        LOGGER.info("DIAL Util initialized");
        courseBatchUtil = new CourseBatchUtil();
        LOGGER.info("Course Batch Util initialized");
        publishUtil = new ShallowPublishUtil();
        LOGGER.info("Shallow Publish Util initialized");
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
            LOGGER.info("Event Ignored. Event Validation Failed for post-publish-processor operation : "+edata.get("action"));
            return;
        }

        switch (((String) edata.get("action")).toLowerCase()) {
            case "link-dialcode": {
                if(!validateContentType(edata)){
                    LOGGER.info("Event Ignored. Event Validation Failed for link-dialcode operation.");
                    return;
                }
                String nodeId = (String) object.get("id");
                LOGGER.info("Started processing of link-dialcode operation for : " + nodeId);
                processDIALEvent(nodeId);
                LOGGER.info("Completed processing of link-dialcode operation for : " + nodeId);
                break;
            }

            case "coursebatch-sync" : {
                if(!validateContentType(edata)){
                    LOGGER.info("Event Ignored. Event Validation Failed for coursebatch-sync operation.");
                    return;
                }
                String nodeId = (String) object.get("id");
                LOGGER.info("Started Syncing the courseBatch enrollment for : " + nodeId);
                courseBatchUtil.syncCourseBatch(nodeId, collector);
                LOGGER.info("Synced the courseBatch enrollment for : " + nodeId);
                break;
            }

            case "publish-shallow-content": {
                String nodeId = (String) object.get("id");
                LOGGER.info("Started processing of publish-shallow-content operation for : " + nodeId);
                Double pkgVersion = (Double) edata.getOrDefault("pkgVersion", 0.0);
                LOGGER.info("pkgVersion (Origin Node) : " + pkgVersion);
                String status = (String) edata.getOrDefault("status", "");
                publishUtil.publish(nodeId, status, collector);
                LOGGER.info("Completed processing of publish-shallow-content operation for : " + nodeId);
                break;
            }

            case "coursebatch-create" : {
                if(!validateContentType(edata)) {
                    LOGGER.info("Event Ignored. Event Validation Failed for coursebatch-create operation. | edata : " + edata);
                    return;
                }
                String nodeId = (String) object.get("id");
                String name = (String) edata.getOrDefault("name", "");
                Double pkgVersion = (Double) edata.getOrDefault("pkgVersion", 0.0);
                LOGGER.info("Started processing of course batch creation for : " + nodeId +" | pkgVersion :"+pkgVersion);
                courseBatchUtil.create(nodeId, name, pkgVersion);
                LOGGER.info("Completed processing of course batch creation for  : " + nodeId);
                break;
            }

            default: {
                LOGGER.info("Event Ignored. Event Action Doesn't match for post-publish-processor operations.");
            }
        }
    }


    private boolean validateContentType(Map<String, Object> edata) {
        String contentType = (String) edata.get("contentType");
        return CONTENT_TYPES.contains(contentType);
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
        return (ACTIONS.contains(action) && iteration <= MAX_ITERATION_COUNT);
    }

    private void processDIALEvent(String identifier) {
        Node node = util.getNode(SamzaCommonParams.domain.name(), identifier);
        if (null != node && MapUtils.isNotEmpty(node.getMetadata())) {
            List<String> dialcodes = dialUtil.getDialCodes(node);
            if (CollectionUtils.isNotEmpty(dialcodes)) {
                Boolean isQRImagePresent = validateQRImage(dialcodes.get(0));
                if (isQRImagePresent)
                    LOGGER.info("Event Skipped. Target Object [" + identifier + "] already has DIAL Code and its QR Image.| DIAL Codes : " + dialcodes);
                else {
                    LOGGER.info("QR Image Not Found for [" + identifier + "] having DIAL Code " + dialcodes+". So Generating QR Image.");
                    dialUtil.generateQRImage(node, dialcodes.get(0));
                }
            } else {
                dialUtil.linkDialCode(node);
            }
        } else {
            LOGGER.info("Event Skipped. Target Object (" + identifier + ") metadata is null.");
        }
    }

    /**
     * This method checks whether QR Image Url Exist for given DIAL Code or not.
     * @param dial
     * @return Boolean
     */
    private Boolean validateQRImage(String dial) {
        Boolean result = false;
        try {
            String url = QRImageUtil.getQRImageRecord(dial);
            return StringUtils.isNotBlank(url) ? true : false;
        } catch (Exception e) {
            LOGGER.error("Exception Occurred While Validating QR Image Record. | Exception is : ", e);
            return false;
        }
    }

}
