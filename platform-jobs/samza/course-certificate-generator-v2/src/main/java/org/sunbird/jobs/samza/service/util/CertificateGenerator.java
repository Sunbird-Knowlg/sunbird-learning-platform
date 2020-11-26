package org.sunbird.jobs.samza.service.util;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TypeTokens;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.graph.cache.util.RedisStoreUtil;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.telemetry.util.LogTelemetryEventUtil;
import org.sunbird.jobs.samza.util.CourseCertificateParams;
import org.sunbird.jobs.samza.util.SunbirdCassandraUtil;
import redis.clients.jedis.Jedis;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class CertificateGenerator {

    private static final String LEARNER_SERVICE_PRIVATE_URL = Platform.config.hasPath("learner_service.base_url")
            ? Platform.config.getString("learner_service.base_url"): "http://localhost:9000";
    private static final String CERT_SERVICE_URL = Platform.config.hasPath("cert_service.base_url")
            ? Platform.config.getString("cert_service.base_url"): "http://localhost:9000";
    private static final String CERT_REG_SERVICE_BASE_URL = Platform.config.hasPath("cert_reg_service.base_url")
            ? Platform.config.getString("cert_reg_service.base_url"): "http://localhost:9000";;
    protected static ObjectMapper mapper = new ObjectMapper();

    private static final String KEYSPACE = Platform.config.hasPath("courses.keyspace.name")
            ? Platform.config.getString("courses.keyspace.name") : "sunbird_courses";
    private static final String USER_COURSES_TABLE = "user_enrolments";
    private SimpleDateFormat formatter = null;
    private SimpleDateFormat dateFormatter = null;
    private static final String CERTIFICATE_BASE_PATH = Platform.config.hasPath("certificate.base_path")
            ? Platform.config.getString("certificate.base_path"): "http://localhost:9000/certs";
    protected static final String KP_CONTENT_SERVICE_BASE_URL = Platform.config.hasPath("kp.content_service.base_url")
            ? Platform.config.getString("kp.content_service.base_url"): "http://localhost:9000";

    private static final String CERT_GENERATE_URL = Platform.config.hasPath("certificate.generate.url")
            ? Platform.config.getString("certificate.generate.url"): "/v2/certs/generate";
    private static final String CERT_REGISTRY_ADD_URL = Platform.config.hasPath("certificate.registry.add.url")
            ? Platform.config.getString("certificate.registry.add.url"): "/certs/v2/registry/add";
    private static final String certGenerateURL = CERT_SERVICE_URL + CERT_GENERATE_URL;
    private static final String certRegistryAddURL = CERT_REG_SERVICE_BASE_URL + CERT_REGISTRY_ADD_URL;
    
    private static final Boolean certificateGenerateNotificationEnable = Platform.config.hasPath("certificate.generate.notification.enable")?
            Platform.config.getBoolean("certificate.generate.notification.enable"): false;

    private static JobLogger LOGGER = new JobLogger(CertificateGenerator.class);
    private Session cassandraSession = null;
    private Jedis redisConnect =null;
    private static final String NOTIFICATION_URL = Platform.config.hasPath("notification.api.endpoint")
            ? Platform.config.getString("notification.api.endpoint"): "/v2/notification";

    private static final String DEFAULT_CHANNEL_ID = Platform.config.hasPath("channel.default") ? Platform.config.getString("channel.default") : "in.ekstep";
    private SystemStream certificateAuditEventStream = null;
    private String feedMessage = Platform.config.getString("user.feed.message");

    private static final String CREATE_USER_FEED_URL = Platform.config.hasPath("create.user.feed.url")
            ? Platform.config.getString("create.user.feed.url"): "/private/user/feed/v1/create";

    private static final String ASSET_READ_URL = Platform.config.hasPath("asset.read.url")
            ? Platform.config.getString("asset.read.url"): "/asset/v4/read/";

    public CertificateGenerator(Jedis redisConnect, Session cassandraSession) {
        formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.cassandraSession = cassandraSession;
        this.redisConnect = redisConnect;
    }

    public CertificateGenerator(Jedis redisConnect, Session cassandraSession, SystemStream certificateAuditEventStream) {
        formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.cassandraSession = cassandraSession;
        this.redisConnect = redisConnect;
        this.certificateAuditEventStream = certificateAuditEventStream;
    }

    public void generate(Map<String, Object> edata, MessageCollector collector) {
        String batchId = (String) edata.get("batchId");
        String userId = (String) edata.get("userId");
        String courseId = (String) edata.get("courseId");
        Map<String, Object> template = (Map<String, Object>) edata.get("template");
        String templateUrl = (String)template.getOrDefault("url", getCertTemplate((String) template.getOrDefault("identifier", "")));

        if(StringUtils.isBlank(templateUrl) || !StringUtils.endsWith(templateUrl, ".svg")) {
        	LOGGER.info("CertificateGenerator:generate: Certificate is not generated for batchId : " + batchId + ", courseId : " + courseId + " and userId : " + userId + ". TemplateId: "+ (String) template.get("identifier") + " with Url: " + templateUrl + " is not supported.");
        	return;
        }
        template.put("url", templateUrl);
        boolean reIssue = (null != edata.get(CourseCertificateParams.reIssue.name()))
                ? (Boolean) edata.get(CourseCertificateParams.reIssue.name()): false;

        if(MapUtils.isNotEmpty(template)) {
            try {
                Map<String, Object> dataToFetch = new HashMap<String, Object>() {{
                    put(CourseCertificateParams.userId.name(), userId);
                    put(CourseCertificateParams.courseId.name(), courseId);
                    put(CourseCertificateParams.batchId.name(), batchId);
                }};
                ResultSet resulSet = SunbirdCassandraUtil.read(cassandraSession, KEYSPACE, USER_COURSES_TABLE, dataToFetch);
                List<Row> rows = resulSet.all();
                if(CollectionUtils.isNotEmpty(rows)) {
                    for (Row row : rows) {
                        List<Map<String, String>> certificates = row.getList(CourseCertificateParams.issued_certificates.name(), TypeTokens.mapOf(String.class, String.class))
                                .stream().filter(cert -> StringUtils.equalsIgnoreCase((String) template.get("name"), (String) cert.get(CourseCertificateParams.name.name()))).collect(Collectors.toList());
                        Date issuedOn = row.getTimestamp("completedOn");
                        if (CollectionUtils.isNotEmpty(certificates) && reIssue) {
                            issueCertificate(certificates, courseId, template, batchId, userId, issuedOn, true, collector);
                        } else if (CollectionUtils.isEmpty(certificates)) {
                            certificates = (null != row.getList(CourseCertificateParams.issued_certificates.name(), TypeTokens.mapOf(String.class, String.class)))
                                    ? row.getList(CourseCertificateParams.issued_certificates.name(), TypeTokens.mapOf(String.class, String.class)) : new ArrayList<>();
                            issueCertificate(certificates, courseId, template, batchId, userId, issuedOn, false, collector);
                        } else {
                            LOGGER.info("CertificateGenerator:generate: Certificate is available for batchId : " + batchId + ", courseId : " + courseId + " and userId : " + userId + ". Not applied for reIssue.");
                            throw new ClientException("ERR_GENERATE_CERTIFICATE", "Certificate is available for batchId : " + batchId + ", courseId : " + courseId + " and userId : " + userId + ". Not applied for reIssue.");
                        }
                    }
                } else {
                    LOGGER.info("CertificateGenerator:generate: This userId : " + userId + " is not enrolled for batchId : " + batchId + " and courseId : " + courseId);
                    throw new ClientException("ERR_GENERATE_CERTIFICATE", "This userId : " + userId + " is not enrolled for batchId : " + batchId + " and courseId : " + courseId);
                }
            } catch (Exception e) {
                LOGGER.error("CertificateGenerator:generate: Certificate is not generated : " + e.getMessage(), e);
                throw new ServerException("ERR_GENERATE_CERTIFICATE", e.getMessage());
            }
        } else {
            LOGGER.info("CertificateGenerator:generate: Certificate template is not available for batchId : " + batchId + " and courseId : " + courseId);
            throw new ClientException("ERR_GENERATE_CERTIFICATE", "Certificate template is not available for batchId : " + batchId + " and courseId : " + courseId);
        }
    }

    private void issueCertificate(List<Map<String, String>> certificates, String courseId, Map<String, Object> certTemplate, String batchId, String userId, Date issuedOn, boolean reIssue, MessageCollector collector) {
        // get Course metadata from KP
        Map<String, Object> courseMetadata = getContent(courseId,null);
        if(MapUtils.isNotEmpty(courseMetadata)){
            String courseName = (String) courseMetadata.get("name");
            if(MapUtils.isNotEmpty(certTemplate)) {
                //Get Username from user get by Id.
                Map<String, Object> userResponse = getUserDetails(userId); // call user Service
                // Save certificate to user_courses table cassandra
                if(MapUtils.isNotEmpty(userResponse)){
                    generateCertificate(certificates, courseId, courseName, batchId, userId, userResponse, certTemplate, issuedOn, reIssue, collector);
                } else {
                    LOGGER.info("CertificateGenerator:issueCertificate: User not found for userId: " + userId);
                    throw new ClientException("ERR_GENERATE_CERTIFICATE", "User not found for userId: " + userId);
                }
            } else {
                LOGGER.info("CertificateGenerator:issueCertificate: Certificate template is not available for batchId : " + batchId + " and courseId : " + courseId);
                throw new ClientException("ERR_GENERATE_CERTIFICATE", "Certificate template is not available for batchId : " + batchId + " and courseId : " + courseId);
            }
        } else {
            LOGGER.info( "CertificateGenerator:issueCertificate: Course not found for courseId: " + courseId);
            throw new ClientException("ERR_GENERATE_CERTIFICATE", "Course not found for courseId: " + courseId);
        }
    }

    private void generateCertificate(List<Map<String, String>> certificates, String courseId, String courseName, String batchId, String userId, Map<String, Object> userResponse,
                                     Map<String, Object> certTemplate, Date issuedOn, boolean reIssue, MessageCollector collector) {
        try{
            String oldId = null;
            if(reIssue) {
                oldId = certificates.stream().filter(cert -> StringUtils.equalsIgnoreCase((String)certTemplate.get("name"), cert.get("name"))).map(cert -> {return  cert.get("identifier");}).findFirst().orElse("");
            }
            String recipientName = getRecipientName(userResponse);
            Map<String, Object> certServiceRequest = prepareCertServiceRequest(courseName, batchId, userId, userResponse, certTemplate, issuedOn);
            HttpResponse<String> httpResponse = Unirest.post(certGenerateURL).header("Content-Type", "application/json").body(mapper.writeValueAsString(certServiceRequest)).asString();
            if(200 == httpResponse.getStatus()) {
                Response response = mapper.readValue(httpResponse.getBody(), Response.class);
                List<Map<String, String>> updatedCerts = certificates.stream().filter(cert -> !StringUtils.equalsIgnoreCase((String)certTemplate.get("name"), cert.get("name"))).collect(Collectors.toList());
                Map<String, Object> certificate = ((List<Map<String, Object>>)response.get("response")).get(0);
                populateCreatedCertificate(updatedCerts, certificate, (String)certTemplate.get("name"), issuedOn, reIssue);

                if(CollectionUtils.isNotEmpty(updatedCerts)) {
                    Map<String, Object> dataToUpdate = new HashMap<String, Object>() {{
                        put(CourseCertificateParams.issued_certificates.name(), updatedCerts);
                    }};
                    Map<String, Object> dataToSelect = new HashMap<String, Object>() {{
                        put(CourseCertificateParams.userId.name(), userId);
                        put(CourseCertificateParams.courseId.name(), courseId);
                        put(CourseCertificateParams.batchId.name(), batchId);
                    }};
                    SunbirdCassandraUtil.update(cassandraSession, KEYSPACE, USER_COURSES_TABLE, dataToUpdate, dataToSelect);
                }

                pushAuditEvent(userId, courseId, batchId, certificate, collector);

                if(addCertificateToUser(certificate, courseId, batchId, oldId, recipientName, (String)certTemplate.get("name")) && certificateGenerateNotificationEnable) {
                	notifyUser(userId, certTemplate, courseName, userResponse, issuedOn);
                    createUserFeed(userId, courseId, courseName, issuedOn);
                }
            } else {
                LOGGER.info("CertificateGenerator:generateCertificate: Error while generation certificate for batchId : " + batchId +  ", courseId : " + courseId + " and userId : " + userId + " with error response : "  +  + httpResponse.getStatus()  + " :: " + httpResponse.getBody());
                throw new ClientException("ERR_GENERATE_CERTIFICATE", "Error while generation certificate for batchId : " + batchId +  ", courseId : " + courseId + " and userId : " + userId + " with error response : "  +  + httpResponse.getStatus()  + " :: " + httpResponse.getBody());
            }
        } catch (Exception e) {
            LOGGER.error("CertificateGenerator:generateCertificate: Error while generation certificate for batchId : " + batchId +  ", courseId : " + courseId + " and userId : " + userId, e);
            throw new ServerException("ERR_GENERATE_CERTIFICATE", "Error while generation certificate for batchId : " + batchId +  ", courseId : " + courseId + " and userId : " + userId, e);
        }
    }

    private boolean addCertificateToUser(Map<String, Object> certificate, String courseId, String batchId, String oldId, String recipientName, String certName) {
        try {
            Request request = new Request();
            request.put(CourseCertificateParams.recipientId.name(), certificate.get(CourseCertificateParams.recipientId.name()));
            request.put(CourseCertificateParams.recipientName.name(), recipientName);
            request.put(CourseCertificateParams.accessCode.name(), certificate.get(CourseCertificateParams.accessCode.name()));
            request.put(CourseCertificateParams.jsonData.name(), certificate.get(CourseCertificateParams.jsonData.name()));
            request.put(CourseCertificateParams.jsonUrl.name(), certificate.get(CourseCertificateParams.jsonUrl.name()));
            request.put(CourseCertificateParams.id.name(), certificate.get(CourseCertificateParams.id.name()));
            request.put("related", new HashMap<String, Object>(){{
                put("type", certName.toLowerCase());
                put(CourseCertificateParams.courseId.name(), courseId);
                put(CourseCertificateParams.batchId.name(), batchId);
            }});
            if(StringUtils.isNotBlank(oldId))
                request.put(CourseCertificateParams.oldId.name(), oldId);
            LOGGER.debug("CertificateGenerator:addCertificateToUser: Add certificate to registry request : " + mapper.writeValueAsString(request));
            HttpResponse<String> response = Unirest.post(certRegistryAddURL).header("Content-Type", "application/json").body(mapper.writeValueAsString(request)).asString();
            LOGGER.debug("CertificateGenerator:addCertificateToUser: Add certificate to registry response for batchid: " + batchId  +" and courseid: " + courseId + " is : " + response.getStatus() + " :: "+ response.getBody());
            return (200 == response.getStatus());
        } catch(Exception e) {
            LOGGER.error("Error while adding the certificate to user: " + certificate, e);
        }
        return false;
    }

    private boolean notifyUser(String userId, Map<String, Object> certTemplate, String courseName, Map<String, Object> userResponse, Date issuedOn) {
        if(certTemplate.containsKey("notifyTemplate")) {
            Map<String, Object> notifyTemplate = getNotificationTemplate(certTemplate);
            String url = LEARNER_SERVICE_PRIVATE_URL + NOTIFICATION_URL;
            Request request = new Request();
            notifyTemplate.entrySet().forEach(entry -> request.put(entry.getKey(), entry.getValue()));
            request.put("firstName", (String) userResponse.get("firstName"));
            request.put("TraningName", courseName);
            request.put("heldDate", dateFormatter.format(issuedOn));
            request.put("recipientUserIds", Arrays.asList(userId));
            request.put("body", "email body");
            try {
                HttpResponse<String> response = Unirest.post(url).header("Content-Type", "application/json").body(mapper.writeValueAsString(request)).asString();
                if (response.getStatus() == 200)
                    LOGGER.info("email response.getStatus()" + response.getStatus());
                else
                    LOGGER.info("email response.getStatus()" + response.getStatus() + " :: " + response.getBody());
            } catch (Exception e) {
                LOGGER.error("Error while sending email notification to user : " + userId, e);
            }
            if (userResponse.containsKey("maskedPhone") && StringUtils.isNotEmpty((String) userResponse.get("maskedPhone")) && !"null".equalsIgnoreCase((String) userResponse.get("maskedPhone"))) {
                request.put("mode", "sms");
                String smsBody = Platform.config.getString("notification.sms.body").replaceAll("@@TRAINING_NAME@@", courseName).replaceAll("@@HELD_DATE@@", dateFormatter.format(issuedOn));
                request.put("body", smsBody);
                try {
                    HttpResponse<String> response = Unirest.post(url).header("Content-Type", "application/json").body(mapper.writeValueAsString(request)).asString();
                    if (response.getStatus() == 200)
                        LOGGER.info("phone response.getStatus()" + response.getStatus());
                    else
                        LOGGER.info("phone response.getStatus()" + response.getStatus() + " :: " + response.getBody());
                } catch (Exception e) {
                    LOGGER.error("Error while sending phone notification to user : " + userId, e);
                }
            }
            return true;
        }
        return false;
    }

    private Map<String, Object> getNotificationTemplate(Map<String, Object> certTemplate)  {
        Object notifyTemplate = certTemplate.get("notifyTemplate");
        if(notifyTemplate instanceof String) {
            try {
                return mapper.readValue((String) notifyTemplate, Map.class);
            } catch (Exception e) {
                LOGGER.error("Error while fetching notify template : " , e);
                return new HashMap<>();
            }
        }else {
            return (Map)notifyTemplate;
        }
    }

    private Map<String,Object> prepareCertServiceRequest(String courseName, String batchId, String userId, Map<String, Object> userResponse, Map<String, Object> certTemplate, Date issuedOn) {
        String recipientName = getRecipientName(userResponse);
        String rootOrgId = (String) userResponse.get("rootOrgId");
        Map<String, Object> keys = getKeysFromOrg(rootOrgId);
        Map<String, Object> request = new HashMap<String, Object>() {{
           put(CourseCertificateParams.request.name(), new HashMap<String, Object>() {{
               put(CourseCertificateParams.certificate.name(), new HashMap<String, Object>() {{
                   put(CourseCertificateParams.data.name(), new ArrayList<Map<String, Object>>() {{
                        add(new HashMap<String, Object>() {{
                            put(CourseCertificateParams.recipientName.name(), recipientName);
                            put(CourseCertificateParams.recipientId.name(), userId);
                        }});
                   }});
                   put(CourseCertificateParams.courseName.name(), courseName);
                   put(CourseCertificateParams.name.name(), certTemplate.get(CourseCertificateParams.name.name()));
                   put(CourseCertificateParams.issuer.name(), getIssuerDetails(certTemplate));
                   put(CourseCertificateParams.signatoryList.name(), getSignatoryList(certTemplate));
                   put(CourseCertificateParams.svgTemplate.name(), certTemplate.get("url"));
                   put(CourseCertificateParams.tag.name(),  rootOrgId + "_" + batchId);
                   put(CourseCertificateParams.issuedDate.name(), dateFormatter.format(issuedOn));
                   if(MapUtils.isNotEmpty(keys))
                    put(CourseCertificateParams.keys.name(), keys);
                   put(CourseCertificateParams.criteria.name(), getCriteria(certTemplate));
                   put(CourseCertificateParams.basePath.name(), CERTIFICATE_BASE_PATH);
               }});
           }});
        }};

        return request;
    }

    private String getRecipientName(Map<String, Object> userResponse) {
        String firstName = (StringUtils.isNotBlank((String) userResponse.get("firstName"))
                && (!StringUtils.equalsIgnoreCase("null", (String) userResponse.get("firstName"))))
                ? (String) userResponse.get("firstName") : "";
        String lastName = (StringUtils.isNotBlank((String) userResponse.get("lastName"))
                && (!StringUtils.equalsIgnoreCase("null", (String) userResponse.get("lastName"))))
                ? (String) userResponse.get("lastName") : "";
        return StringUtils.trimToEmpty(firstName + " " + lastName);
    }

    private Map<String,Object> getContent(String courseId, String fields) {
        try {
            String courseData = redisConnect.get(courseId);
            Map<String, Object> content = new HashMap<>();
            if(StringUtils.isNotBlank(courseData)){
                content = mapper.readValue(courseData, new TypeReference<Map<String, Object>>(){});    
            }
            if(MapUtils.isEmpty(content)) {
                String url = KP_CONTENT_SERVICE_BASE_URL + "/content/v3/read/" + courseId;
                if(StringUtils.isNotBlank(fields))
                    url += "?fields=" + fields;

                HttpResponse<String> httpResponse = Unirest.get(url).header("Content-Type", "application/json").asString();
                if(200 != httpResponse.getStatus()){
                    System.err.println("Error while reading content from KP : " + courseId + " : " + httpResponse.getStatus() + " : " + httpResponse.getBody());
                    throw new ServerException("ERR_COURSE_BATCH_SAMZA", "Error while reading content from KP : " + courseId + " : " + httpResponse.getStatus() + " : " + httpResponse.getBody());
                }
                Response response = null;
                response = mapper.readValue(httpResponse.getBody(), Response.class);
                content = (Map<String, Object>) response.getResult().get("content");
            }
            return content;
        } catch (Exception e) {
            LOGGER.error("Error while reading course : " + courseId, e);
            return null;
        }
    }

    private Map<String,Object> getUserDetails(String userId) {
        try{
            String url = LEARNER_SERVICE_PRIVATE_URL + "/private/user/v1/search";
            String userSearchRequest = prepareUserSearchRequest(userId);
            HttpResponse<String> httpResponse = Unirest.post(url).header("Content-Type", "application/json").body(userSearchRequest).asString();
            if(200 == httpResponse.getStatus()) {
                Response response = mapper.readValue(httpResponse.getBody(), Response.class);
                Map<String, Object> content = ((List<Map<String, Object>>) ((Map<String, Object>) response.getResult().get("response")).get("content")).get(0);
                return content;
            }
        } catch(Exception e){
            LOGGER.error("Error while searching for user : " + userId, e);
        }
        return null;
    }

    private String prepareUserSearchRequest(String userId) throws Exception {
        Request request = new Request();
        request.put("filters", new HashMap<String, Object>(){{
            put("identifier", userId);
        }});
        request.put("fields", Arrays.asList("firstName", "lastName", "userName", "rootOrgName", "rootOrgId","maskedPhone"));
        return mapper.writeValueAsString(request);
    }

    /**
     * This method needs to be updated with actual issuer details implementation.
     * @param certTemplate
     * @return
     */
    private Map<String, Object> getIssuerDetails(Map<String, Object> certTemplate) {
        if(MapUtils.isNotEmpty((Map) certTemplate.get("issuer"))) {
            return (Map<String, Object>) certTemplate.get("issuer");
        }
        return null;
    }

    /**
     * This method needs to be updated with actual signatoryList implementation
     * @param certTemplate
     * @return
     */
    private List<Map<String, Object>> getSignatoryList(Map<String, Object> certTemplate) {
        if(CollectionUtils.isNotEmpty((List) certTemplate.get("signatoryList"))) {
            return (List<Map<String, Object>>) certTemplate.get("signatoryList");
        }
        return null;
    }

    /**
     * this method gives "What was the criteria for issuing this certificate"
     * if criteria is not given by defaults to "course completion certificate"
     * @param certTemplate
     * @return
     */
    private Map<String, Object> getCriteria(Map<String, Object> certTemplate) {
        Map<String , Object> criteria = new HashMap<>();
        criteria.put(CourseCertificateParams.narrative.name(), certTemplate.getOrDefault("name", "course completion certificate"));
        return criteria;
    }

    private void populateCreatedCertificate(List<Map<String, String>> updatedCerts, Map<String, Object> certificate, String certificateName, Date issuedOn, boolean reIssue) {
        updatedCerts.add(new HashMap<String, String>(){{
            put(CourseCertificateParams.name.name(), certificateName);
            put(CourseCertificateParams.identifier.name(), (String) certificate.get(CourseCertificateParams.id.name()));
            put(CourseCertificateParams.token.name(), (String) certificate.get(CourseCertificateParams.accessCode.name()));
            put(CourseCertificateParams.lastIssuedOn.name(), formatter.format(new Date()));
            if(reIssue){
                put(CourseCertificateParams.lastIssuedOn.name(), formatter.format(new Date()));
            }
        }});
    }


    private Map<String, Object> getKeysFromOrg(String orgId) {
        try{
            String url = LEARNER_SERVICE_PRIVATE_URL + "/v1/org/read";
            Request request = new Request();
            request.put("organisationId", orgId);
            HttpResponse<String> httpResponse = Unirest.post(url).header("Content-Type", "application/json").body(mapper.writeValueAsString(request)).asString();
            if(200 == httpResponse.getStatus()) {
                Response response = mapper.readValue(httpResponse.getBody(), Response.class);
                Map<String, Object> keys = (Map<String, Object>) ((Map<String, Object>) response.getResult().get("response")).get("keys");
                if(MapUtils.isNotEmpty(keys) && (CollectionUtils.isNotEmpty((List<String>) keys.get("signKeys")))) {
                    Map<String, Object> signKeys = new HashMap<String, Object>(){{
                       put("id", ((List<String>)keys.get("signKeys")).get(0)) ;
                    }};
                    return signKeys;
                }
                return keys;
            }
        } catch(Exception e){
            LOGGER.error("Error while reading organisation : " + orgId, e);
        }
        return null;

    }

    private void pushAuditEvent(String userId, String courseId, String batchId, Map<String, Object> certificate, MessageCollector collector) {
        try {
            Map<String, Object> certificateAuditEvent = generateAuditEvent(userId, courseId, batchId, certificate);
            LOGGER.info("CertificateGenerator:pushAuditEvent: audit event generated for certificate : "
                    + ((Map<String, Object>) certificateAuditEvent.getOrDefault("object", "")).getOrDefault("id", "")
                    + " with mid : " + certificateAuditEvent.getOrDefault("mid", ""));
            collector.send(new OutgoingMessageEnvelope(certificateAuditEventStream, certificateAuditEvent));
            LOGGER.info("CertificateGenerator:pushAuditEvent: certificate audit event success");
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("CertificateGenerator:pushAuditEvent: certificate audit event failed : ", e);
        }
    }

    private Map<String, Object> generateAuditEvent(String userId, String courseId, String batchId, Map<String, Object> certificate) throws Exception {
        Map<String, Object> actor = new HashMap<>();
        Map<String, Object> context = new HashMap<>();
        Map<String, Object> object = new HashMap<>();
        Map<String, Object> edata = new HashMap<>();

        actor.putAll(new HashMap<String, Object>() {{
            put(CourseCertificateParams.id.name(), userId);
            put(CourseCertificateParams.type.name(), "User");
        }});

        context.putAll(new HashMap<String, Object>() {{
            put(CourseCertificateParams.channel.name(), DEFAULT_CHANNEL_ID);
            put(CourseCertificateParams.pdata.name(), new HashMap<String, Object>() {{
                put(CourseCertificateParams.id.name(), "org.sunbird.learning.platform");
                put(CourseCertificateParams.pid.name(), "course-certificate-generator");
                put(CourseCertificateParams.ver.name(), "1.0");
            }});
            put(CourseCertificateParams.env.name(), "Course");
            put(CourseCertificateParams.cdata.name(), new ArrayList<HashMap<String, Object>>() {{
                add(new HashMap<String, Object>() {{
                    put(CourseCertificateParams.type.name(), "CourseBatch");
                    put(CourseCertificateParams.id.name(), batchId);
                }});
            }});
        }});

        object.putAll(new HashMap<String, Object>() {{
            put(CourseCertificateParams.id.name(), certificate.get("id"));
            put(CourseCertificateParams.type.name(), "Certificate");
            put(CourseCertificateParams.rollup.name(), new HashMap<String, Object>() {{
                put(CourseCertificateParams.l1.name(), courseId);
            }});
        }});

        edata.putAll(new HashMap<String, Object>() {{
            put(CourseCertificateParams.props.name(), new ArrayList<String>() {{
                add("certificates");
            }});
            put(CourseCertificateParams.type.name(), "certificate-issued-svg");
        }});
        Map<String, Object> certificateAuditEvent = new HashMap<String, Object>() {{
            put(CourseCertificateParams.eid.name(), "AUDIT");
            put("ets", System.currentTimeMillis());
            put(CourseCertificateParams.mid.name(), "LP.AUDIT."+System.currentTimeMillis()+"."+ UUID.randomUUID());
            put("actor", actor);
            put("context", context);
            put("edata", edata);
            put("object", object);
            put(CourseCertificateParams.ver.name(), "3.0");
        }};
        return certificateAuditEvent;
    }

    private void createUserFeed(String userId, String courseId, String courseName, Date issuedOn) {
        try{
            Request request = new Request();
            request.put("userId", userId);
            request.put("category", "Notification");
            request.put("priority", 1);
            request.put("data", new HashMap<String, Object>(){{
                put("type", 1);
                put("action", new HashMap<String, Object>() {{
                    put("actionType", "certificateUpdate");
                    put("title", courseName);
                    put("description", feedMessage);
                    put("identifier", courseId);
                }});
            }});
            HttpResponse<String> httpResponse = Unirest.post(LEARNER_SERVICE_PRIVATE_URL + CREATE_USER_FEED_URL).header("Content-Type", "application/json").body(mapper.writeValueAsString(request)).asString();
            LOGGER.info("Create User Feed response: " + httpResponse.getStatus() + " :: " + httpResponse.getBody() );
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("CertificateGenerator:createUserFeed: createUserFeed failed : ", e);
        }
    }

    /**
     * Get Certificate Template
     * @param id
     * @return
     */
    protected static String getCertTemplate(String id) {
        try {
            if(StringUtils.isNotBlank(id)) {
                Map<String, Object> certTemplate = new HashMap<>();
                String assetStr = RedisStoreUtil.get(id);
                if(StringUtils.isNotBlank(assetStr)) {
                    certTemplate = mapper.readValue(assetStr, Map.class);
                    return (String) certTemplate.getOrDefault("artifactUrl", ""); 
                }
                String url = KP_CONTENT_SERVICE_BASE_URL + ASSET_READ_URL + id;
                HttpResponse<String> httpResponse = Unirest.get(url).header("Content-Type", "application/json").asString();
                if(200 == httpResponse.getStatus()) {
                    Response response = mapper.readValue(httpResponse.getBody(), Response.class);
                    certTemplate = (Map<String, Object>) response.getResult().getOrDefault("content", new HashMap<>());
                    if(MapUtils.isNotEmpty(certTemplate)) {
                        RedisStoreUtil.save(id, mapper.writeValueAsString(certTemplate), 600);
                    }
                    return (String) certTemplate.getOrDefault("artifactUrl", "");
                }
            }
        } catch(Exception e) {
            LOGGER.error("Error while fetching the certificate template : " , e);
        }
        return null;
    }
}
