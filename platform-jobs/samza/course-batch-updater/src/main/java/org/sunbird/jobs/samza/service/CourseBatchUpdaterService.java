package org.sunbird.jobs.samza.service;

import com.datastax.driver.core.Session;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.Platform;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.JSONUtils;
import org.ekstep.jobs.samza.util.JobLogger;
import org.sunbird.jobs.samza.service.util.BatchStatusUpdater;
import org.sunbird.jobs.samza.util.CourseBatchParams;
import org.apache.commons.collections.MapUtils;
import org.sunbird.jobs.samza.service.util.BatchCountUpdater;

import java.util.Map;

public class CourseBatchUpdaterService implements ISamzaService {
    private static JobLogger LOGGER = new JobLogger(CourseBatchUpdaterService.class);
    private SystemStream systemStream;
    private Config config = null;
    private static int MAXITERTIONCOUNT = 2;
    private BatchStatusUpdater batchStatusUpdater = null;
    private BatchCountUpdater batchCountUpdater = null;
    private Session cassandraSession = null;

    public CourseBatchUpdaterService(Session cassandraSession) {
        this.cassandraSession = cassandraSession;
    }

    @Override
    public void initialize(Config config) throws Exception {
        this.config = config;
        JSONUtils.loadProperties(config);
        LOGGER.info("Service config initialized");
        systemStream = new SystemStream("kafka", config.get("output.failed.events.topic.name"));
        batchStatusUpdater = new BatchStatusUpdater(cassandraSession);
        batchCountUpdater = new BatchCountUpdater(cassandraSession);
    }

    @Override
    public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector) throws Exception {
        if (null == message) {
            LOGGER.info("Ignoring the message because it is not valid for publishing.");
            return;
        }

        Map<String, Object> edata = (Map<String, Object>) message.get(CourseBatchParams.edata.name());
        Map<String, Object> object = (Map<String, Object>) message.get(CourseBatchParams.object.name());
        if (!validateObject(edata) || null == object) {
            LOGGER.info("Ignoring the message because it is not valid for course-batch-updater.");
            return;
        }
        String objectId = (String) object.get(CourseBatchParams.id.name());
        if (StringUtils.isNotBlank(objectId)) {
            String action = (String) edata.get("action");
            switch (action) {
                case "batch-status-update":
                    batchStatusUpdater.update(edata);
                    break;
                case "course-batch-update":
                    LOGGER.info("Batch Count update for : " + edata);
                    batchCountUpdater.update(edata);
                    break;
                default:
                    System.out.println("Invalid action provided: " + message);
                    break;
            }
        }

    }

    private boolean validateObject(Map<String, Object> edata) {
        if(MapUtils.isNotEmpty(edata)){
            Integer iteration = (Integer) edata.get(CourseBatchParams.iteration.name());
            if ((iteration <= getMaxIterations())) {
                return true;
            }
        }
        return false;
    }

    protected int getMaxIterations() {
        if (Platform.config.hasPath("max.iteration.count.samza.job"))
            return Platform.config.getInt("max.iteration.count.samza.job");
        else
            return MAXITERTIONCOUNT;
    }
}
