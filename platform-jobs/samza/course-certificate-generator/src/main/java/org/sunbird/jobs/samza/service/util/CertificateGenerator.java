package org.sunbird.jobs.samza.service.util;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.TypeTokens;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ServerException;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.sunbird.jobs.samza.util.CourseCertificateParams;
import org.sunbird.jobs.samza.util.SunbirdCassandraUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CertificateGenerator {

    private static final String LEARNER_SERVICE_PRIVATE_URL = Platform.config.hasPath("learner_service.base_url")
            ? Platform.config.getString("learner_service.base_url"): "http://localhost:9000";
    private static final String CERT_SERVICE_URL = Platform.config.hasPath("cert_service.base_url")
            ? Platform.config.getString("cert_service.base_url"): "http://localhost:9000";
    protected static ObjectMapper mapper = new ObjectMapper();

    private static final String KEYSPACE = Platform.config.hasPath("courses.keyspace.name")
            ? Platform.config.getString("courses.keyspace.name") : "sunbird_courses";
    private static final String USER_COURSES_TABLE = "user_courses";
    protected static final String KP_LEARNING_BASE_URL = Platform.config.hasPath("kp.learning_service.base_url")
            ? Platform.config.getString("kp.learning_service.base_url"): "http://localhost:8080/learning-service";
    private SimpleDateFormat formatter = null;
    private SimpleDateFormat dateFormatter = null;
    private static final String ES_INDEX_NAME = "user-courses";
    private static final String ES_DOC_TYPE = "_doc";

    private static JobLogger LOGGER = new JobLogger(CertificateGenerator.class);

    public CertificateGenerator() {
        ElasticSearchUtil.initialiseESClient(ES_INDEX_NAME, Platform.config.getString("search.es_conn_info"));
        formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZ");
        dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public void generate(Map<String, Object> edata) {
        String batchId = (String) edata.get("batchId");
        String userId = (String) edata.get("userId");
        String courseId = (String) edata.get("courseId");
        String certificateName = (String) edata.get("certificate");
        boolean reIssue = (null != edata.get(CourseCertificateParams.reIssue.name()))
                ? (Boolean) edata.get(CourseCertificateParams.reIssue.name()): false;

        Map<String, Object> dataToFetch = new HashMap<String, Object>() {{
            put(CourseCertificateParams.batchId.name(), batchId);
            put(CourseCertificateParams.userId.name(), userId);
        }};

        List<Row> rows = SunbirdCassandraUtil.read(KEYSPACE, USER_COURSES_TABLE, dataToFetch);

        for(Row row: rows) {
            List<Map<String, String>> certificates = row.getList(CourseCertificateParams.certificates.name(), TypeTokens.mapOf(String.class, String.class));
            Date issuedOn = row.getTimestamp("completedOn");
            if(CollectionUtils.isNotEmpty(certificates) && reIssue) {
                issueCertificate(certificates, courseId, certificateName, batchId, userId, issuedOn,  true);
            } else if(CollectionUtils.isEmpty(certificates)) {
                certificates = new ArrayList<>();
                issueCertificate(certificates, courseId, certificateName, batchId, userId, issuedOn,  false);
            }
        }

    }

    private void issueCertificate(List<Map<String, String>> certificates, String courseId, String certificateName, String batchId, String userId, Date issuedOn, boolean reIssue) {
        // get Course metadata from KP
        Map<String, Object> courseMetadata = getContent(courseId,null);
        if(MapUtils.isNotEmpty(courseMetadata)){
            String courseName = (String) courseMetadata.get("name");
            List<Map<String, Object>> certTemplates= (List<Map<String, Object>>) courseMetadata.get("certTemplate");
            Map<String, Object> certTemplate = new HashMap<>();
            if(CollectionUtils.isNotEmpty(certTemplates)) {
                certTemplate = certTemplates.stream().filter(t -> StringUtils.equalsIgnoreCase(certificateName, (String) t.get("name"))).findFirst().get();
            }

            if(MapUtils.isNotEmpty(certTemplate)) {
                //Get Username from user get by Id.
                Map<String, Object> userResponse = getUserDetails(userId); // call user Service
                // Save certificate to user_courses table cassandra
                if(MapUtils.isNotEmpty(userResponse)){
                    generateCertificate(certificates, courseId, courseName, certificateName, batchId, userId, userResponse, certTemplate, issuedOn, reIssue);
                } else {
                    LOGGER.info("No User details fetched  for userid: " + userId + " : " + userResponse);
                }

            } else {
                LOGGER.info("No certificate template to generate certificates for: " + courseId);
            }
        } else {
            LOGGER.info( courseId+ " not found");
        }
    }

    private void generateCertificate(List<Map<String, String>> certificates, String courseId, String courseName, String certificateName, String batchId, String userId, Map<String, Object> userResponse,
                                     Map<String, Object> certTemplate, Date issuedOn, boolean reIssue) {
        try{
            String oldId = null;
            if(reIssue) {
                oldId = certificates.stream().filter(cert -> StringUtils.equalsIgnoreCase(certificateName, cert.get("name"))).map(cert -> {return  cert.get("id");}).findFirst().orElse("");
            }
            Map<String, Object> certServiceRequest = prepareCertServiceRequest(courseName, certificateName, batchId, userId, userResponse, certTemplate, issuedOn);
            String url = CERT_SERVICE_URL + "/v1/certs/generate";
            HttpResponse<String> httpResponse = Unirest.post(url).header("Content-Type", "application/json").body(mapper.writeValueAsString(certServiceRequest)).asString();
            if(200 == httpResponse.getStatus()) {
                Response response = mapper.readValue(httpResponse.getBody(), Response.class);
                List<Map<String, String>> updatedCerts = certificates.stream().filter(cert -> !StringUtils.equalsIgnoreCase(certificateName, cert.get("name"))).collect(Collectors.toList());

                Map<String, Object> certificate = ((List<Map<String, Object>>)response.get("response")).get(0);
                updatedCerts.add(new HashMap<String, String>(){{
                    put(CourseCertificateParams.name.name(), certificateName);
                    put(CourseCertificateParams.id.name(), (String) certificate.get(CourseCertificateParams.id.name()));
                    put(CourseCertificateParams.url.name(), (String) certificate.get(CourseCertificateParams.pdfUrl.name()));
                    put(CourseCertificateParams.token.name(), (String) certificate.get(CourseCertificateParams.accessCode.name()));
                    put(CourseCertificateParams.lastIssuedOn.name(), formatter.format(issuedOn));
                    if(reIssue){
                        put(CourseCertificateParams.lastIssuedOn.name(), formatter.format(new Date()));
                    }
                }});

                if(CollectionUtils.isNotEmpty(updatedCerts)) {
                    Map<String, Object> dataToUpdate = new HashMap<String, Object>() {{
                        put(CourseCertificateParams.certificates.name(), updatedCerts);
                    }};
                    Map<String, Object> dataToSelect = new HashMap<String, Object>() {{
                        put(CourseCertificateParams.batchId.name(), batchId);
                        put(CourseCertificateParams.userId.name(), userId);
                    }};
                    SunbirdCassandraUtil.update(KEYSPACE, USER_COURSES_TABLE, dataToUpdate, dataToSelect);
                    updatedES(ES_INDEX_NAME, ES_DOC_TYPE, dataToUpdate, dataToSelect);
                }
                if(addCertificateToUser(certificate, courseId, batchId, oldId)) {
                    notifyUser(userId, certTemplate, courseName, userResponse, issuedOn);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while generating the certificate for user " + userId +" with batch: " + batchId, e);
        }
    }

    private boolean addCertificateToUser(Map<String, Object> certificate, String courseId, String batchId, String oldId) {
        try{
            String url = LEARNER_SERVICE_PRIVATE_URL + "/private/user/v1/certs/add";
            Request request = new Request();
            request.put(CourseCertificateParams.userId.name(), certificate.get(CourseCertificateParams.recipientId.name()));
            request.put(CourseCertificateParams.accessCode.name(), certificate.get(CourseCertificateParams.accessCode.name()));
            request.put(CourseCertificateParams.jsonData.name(), certificate.get(CourseCertificateParams.jsonData.name()));
            request.put(CourseCertificateParams.jsonUrl.name(), certificate.get(CourseCertificateParams.jsonUrl.name()));
            request.put(CourseCertificateParams.id.name(), certificate.get(CourseCertificateParams.id.name()));
            request.put("pdfURL", certificate.get(CourseCertificateParams.pdfUrl.name()));
            request.put(CourseCertificateParams.courseId.name(), courseId);
            request.put(CourseCertificateParams.batchId.name(), batchId);
            if(StringUtils.isNotBlank(oldId))
                request.put(CourseCertificateParams.oldId.name(), oldId);
            HttpResponse<String> response = Unirest.post(url).header("Content-Type", "application/json").body(mapper.writeValueAsString(request)).asString();
            return (200 == response.getStatus());
        } catch(Exception e) {
            LOGGER.error("Error while adding the certificate to user: " + certificate, e);
        }
        return false;
    }

    private boolean notifyUser(String userId, Map<String, Object> certTemplate, String courseName, Map<String, Object> userResponse, Date issuedOn) {
        
            if(MapUtils.isNotEmpty((Map) certTemplate.get("notifyTemplate"))) {
                Map<String, Object> notifyTemplate = (Map<String, Object>) certTemplate.get("notifyTemplate");
                String url = LEARNER_SERVICE_PRIVATE_URL + "/v1/notification/email";
                Request request = new Request();
                notifyTemplate.entrySet().forEach(entry -> request.put(entry.getKey(), entry.getValue()));
                request.put("firstName", (String) userResponse.get("firstName"));
                request.put("TraningName", courseName);
                request.put("heldDate", dateFormatter.format(issuedOn));
                request.put("recipientUserIds", Arrays.asList(userId));
                request.put("body", "email body");
                try {
                	HttpResponse<String> response = Unirest.post(url).header("Content-Type", "application/json").body(mapper.writeValueAsString(request)).asString();
                	LOGGER.info("email response.getStatus()"+response.getStatus());
                } catch (Exception e) {
                    LOGGER.error("Error while sending email notification to user : " + userId, e);
                }
                if(userResponse.containsKey("maskedPhone") && StringUtils.isNotEmpty((String)userResponse.containsKey("maskedPhone"))) {
                	request.put("mode", "sms");
                	String smsBody = Platform.config.getString("notification.sms.body").replaceAll("@@TRAINING_NAME@@", courseName).replaceAll("@@HELD_DATE@@", dateFormatter.format(issuedOn));
                	request.put("body", smsBody);
                	try {
                    	HttpResponse<String> response = Unirest.post(url).header("Content-Type", "application/json").body(mapper.writeValueAsString(request)).asString();
                    	LOGGER.info("phone response.getStatus()"+response.getStatus());
                    } catch (Exception e) {
                        LOGGER.error("Error while sending phone notification to user : " + userId, e);
                    }
                }
                return true;
            }

        return false;
    }

    private Map<String,Object> prepareCertServiceRequest(String courseName, String certificateName, String batchId, String userId, Map<String, Object> userResponse, Map<String, Object> certTemplate, Date issuedOn) {
        String recipientName = getRecipientName(userResponse);
        String rootOrgId = (String) userResponse.get("rootOrgId");
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
                   put(CourseCertificateParams.name.name(), certificateName);
                   put(CourseCertificateParams.issuer.name(), getIssuerDetails(certTemplate));
                   put(CourseCertificateParams.signatoryList.name(), getSignatoryList(certTemplate));
                   put(CourseCertificateParams.htmlTemplate.name(), certTemplate.get(CourseCertificateParams.htmlTemplate.name()));
                   put(CourseCertificateParams.tag.name(), batchId);
                   put(CourseCertificateParams.issuedDate.name(), dateFormatter.format(issuedOn));
                   put(CourseCertificateParams.keys.name(), certTemplate.get(CourseCertificateParams.keys.name()));
                   put(CourseCertificateParams.orgId.name(), rootOrgId);
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
            String url = KP_LEARNING_BASE_URL + "/content/v3/read/" + courseId;
            if(StringUtils.isNotBlank(fields))
                url += "?fields=" + fields;

            HttpResponse<String> httpResponse = Unirest.get(url).header("Content-Type", "application/json").asString();
            if(200 != httpResponse.getStatus()){
                System.err.println("Error while reading content from KP : " + courseId + " : " + httpResponse.getStatus() + " : " + httpResponse.getBody());
                throw new ServerException("ERR_COURSE_BATCH_SAMZA", "Error while reading content from KP : " + courseId + " : " + httpResponse.getStatus() + " : " + httpResponse.getBody());
            }
            Response response = null;
            response = mapper.readValue(httpResponse.getBody(), Response.class);
            Map<String, Object> content = (Map<String, Object>) response.getResult().get("content");
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


    private void updatedES(String index, String type, Map<String, Object> dataToUpdate, Map<String, Object> dataToSelect) {
        try {
            String key = dataToSelect.entrySet().stream().map(entry -> (String) entry.getValue()).collect(Collectors.joining("_"));
            String documentJson = ElasticSearchUtil.getDocumentAsStringById(index, type, key);
            Map<String, Object> courseBatch = new HashMap<>();
            if(StringUtils.isNotBlank(documentJson))
                courseBatch = mapper.readValue(documentJson, Map.class);
            courseBatch.putAll(dataToUpdate);
            ElasticSearchUtil.updateDocument(index, type, mapper.writeValueAsString(courseBatch), key);
        } catch (Exception e) {
            LOGGER.error("Error while update to ES: ", e);
        }

    }

}
