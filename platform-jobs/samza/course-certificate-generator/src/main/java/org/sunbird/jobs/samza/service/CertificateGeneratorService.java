package org.sunbird.jobs.samza.service;

/**
 * @author Pradyumna
 */

import com.datastax.driver.core.Session;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.jobs.samza.exception.PlatformErrorCodes;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.FailedEventsUtil;
import org.ekstep.jobs.samza.util.JSONUtils;
import org.ekstep.jobs.samza.util.JobLogger;
import org.sunbird.jobs.samza.service.util.CertificateGenerator;
import org.sunbird.jobs.samza.service.util.IssueCertificate;
import org.sunbird.jobs.samza.util.CassandraConnector;
import org.sunbird.jobs.samza.util.CourseCertificateParams;
import org.sunbird.jobs.samza.util.RedisConnect;
import redis.clients.jedis.Jedis;

import java.util.Map;

public class CertificateGeneratorService implements ISamzaService {

    private static JobLogger LOGGER = new JobLogger(CertificateGeneratorService.class);
    private SystemStream systemStream;
    private SystemStream certificateFailedSystemStream;
    private Config config = null;
    private static int MAXITERTIONCOUNT = 2;
    private CertificateGenerator certificateGenerator =null;
    private IssueCertificate issueCertificate = null;
    private Jedis redisConnect = null;
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
        redisConnect = new RedisConnect(config).getConnection();
        cassandraSession = new CassandraConnector(config).getSession();
        systemStream = new SystemStream("kafka", config.get("output.failed.events.topic.name"));
        certificateFailedSystemStream = new SystemStream("kafka", config.get("output.certificate.failed.events.topic.name"));
        certificateAuditEventStream = new SystemStream("kafka", config.get("certificate.audit.event.topic.name"));
        certificateGenerator = new CertificateGenerator(redisConnect, cassandraSession, certificateAuditEventStream);
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
                String action = (String) edata.get("action");
                switch (action) {
                    case "generate-course-certificate" :
                        LOGGER.info("Certificate generation process started ");
                        certificateGenerator.generate(edata, collector);
                        LOGGER.info("Certificate is generated");
                        break;
                    case "issue-certificate":
                        LOGGER.info("Certificate issue process started ");
                        issueCertificate.issue(edata, collector);
                        LOGGER.info("Pushed certificate generation event");
                        break;
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
