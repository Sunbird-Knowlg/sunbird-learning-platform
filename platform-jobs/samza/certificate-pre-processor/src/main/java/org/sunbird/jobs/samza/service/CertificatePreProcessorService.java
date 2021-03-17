package org.sunbird.jobs.samza.service;

import com.datastax.driver.core.Session;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.sunbird.common.Platform;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.jobs.samza.exception.PlatformErrorCodes;
import org.sunbird.jobs.samza.service.ISamzaService;
import org.sunbird.jobs.samza.service.task.JobMetrics;
import org.sunbird.jobs.samza.util.FailedEventsUtil;
import org.sunbird.jobs.samza.util.JSONUtils;
import org.sunbird.jobs.samza.util.JobLogger;
import org.sunbird.jobs.samza.service.util.IssueCertificate;
import org.sunbird.jobs.samza.util.CassandraConnector;
import org.sunbird.jobs.samza.util.CourseCertificateParams;
import org.sunbird.jobs.samza.util.RedisConnect;
import redis.clients.jedis.Jedis;

import java.util.Map;

public class CertificatePreProcessorService implements ISamzaService {

    private static JobLogger LOGGER = new JobLogger(CertificatePreProcessorService.class);
    private SystemStream systemStream;
    private SystemStream certificateFailedSystemStream;
    private Config config = null;
    private int maxIterationCount = 2;
    private IssueCertificate issueCertificate = null;
    private Session cassandraSession = null;
    private SystemStream certificateAuditEventStream = null;
    /**
     * @param config
     * @throws Exception
     */
    @Override
    public void initialize(Config config) throws Exception {
        this.config = config;
        JSONUtils.loadProperties(config);
        LOGGER.info("Service config initialized");
        cassandraSession = new CassandraConnector(config).getSession();
        systemStream = new SystemStream("kafka", config.get("output.failed.events.topic.name"));
        certificateFailedSystemStream = new SystemStream("kafka", config.get("output.certificate.failed.events.topic.name"));
        certificateAuditEventStream = new SystemStream("kafka", config.get("telemetry_raw_topic"));
        maxIterationCount = Platform.config.hasPath("max.iteration.count.samza.job")? Platform.config.getInt("max.iteration.count.samza.job"): 2;
        issueCertificate = new IssueCertificate(cassandraSession);
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
            LOGGER.info("Ignoring the message because it is not valid for publishing.");
            return;
        }

        Map<String, Object> edata = (Map<String, Object>) message.get(CourseCertificateParams.edata.name());
        Map<String, Object> object = (Map<String, Object>) message.get(CourseCertificateParams.object.name());
        if(!validEdata(edata) || MapUtils.isEmpty(object)) {
            LOGGER.info("Ignoring the message because it is not valid for course-certificate-generator.");
            return;
        }
        try {
            String objectId = (String) object.get(CourseCertificateParams.id.name());
            if (StringUtils.isNotBlank(objectId)) {
                String action = (String) edata.getOrDefault("action", "");
                if(StringUtils.isNotBlank(action) && StringUtils.equalsIgnoreCase("issue-certificate", action)) {
                    LOGGER.info("Certificate issue process started ");
                    issueCertificate.issue(edata, collector);
                    LOGGER.info("Pushed certificate generation event");
                }
            }
        } catch(ClientException e) {
            LOGGER.error("CertificateGeneratorService:processMessage: Error while serving the event : "  + message, e);
            FailedEventsUtil.pushEventForRetry(certificateFailedSystemStream, message, metrics, collector,
                    PlatformErrorCodes.PROCESSING_ERROR.name(), e);
        } catch(ServerException e) {
            LOGGER.error("CertificateGeneratorService:processMessage: Error while serving the event : "  + message, e);
            FailedEventsUtil.pushEventForRetry(certificateFailedSystemStream, message, metrics, collector,
                    PlatformErrorCodes.SYSTEM_ERROR.name(), e);
        } catch(Exception e) {
            LOGGER.error("CertificateGeneratorService:processMessage: Error while serving the event : "  + message, e);
            FailedEventsUtil.pushEventForRetry(certificateFailedSystemStream, message, metrics, collector,
                    PlatformErrorCodes.SYSTEM_ERROR.name(), e);
        }
    }

    private boolean validEdata(Map<String, Object> edata) {
        if(MapUtils.isNotEmpty(edata)){
            Integer iteration = (Integer) edata.get(CourseCertificateParams.iteration.name());
            if ((iteration <= maxIterationCount)) {
                return true;
            }
        }
        return false;
    }
}
