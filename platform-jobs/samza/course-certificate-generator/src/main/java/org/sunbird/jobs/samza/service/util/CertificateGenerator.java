package org.sunbird.jobs.samza.service.util;

import com.datastax.driver.core.Row;
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
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.sunbird.job.samza.util.CourseCertificateParams;
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
    protected static ObjectMapper mapper = new ObjectMapper();
    private static final String KEYSPACE = Platform.config.hasPath("") ? Platform.config.getString("") : "sunbird_courses";
    private static final String USER_COURSES_TABLE = "user_courses";
    protected static final String KP_LEARNING_BASE_URL = Platform.config.hasPath("kp.learning_service.base_url")
            ? Platform.config.getString("kp.learning_service.base_url"): "http://localhost:8080/learning-service";
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-ddThh:mm:ss.SSSZ");
    private static final String ES_INDEX_NAME = "user-courses";
    private static final String ES_DOC_TYPE = "_doc";

    public CertificateGenerator() {
        ElasticSearchUtil.initialiseESClient(ES_INDEX_NAME, Platform.config.getString("search.es_conn_info"));
    }

    public void generate(Map<String, Object> edata) {
        String batchId = (String) edata.get("batchId");
        String userId = (String) edata.get("userId");
        String courseId = (String) edata.get("courseId");
        String certificateName = (String) edata.get("certificateName");

        Map<String, Object> dataToFetch = new HashMap<String, Object>() {{
            put(CourseCertificateParams.batchId.name(), batchId);
            put(CourseCertificateParams.userId.name(), userId);
        }};

        List<Row> rows = SunbirdCassandraUtil.read(KEYSPACE, USER_COURSES_TABLE, dataToFetch);

        for(Row row: rows) {
            List<Map> certificates = row.getList(CourseCertificateParams.certificates.name(), Map.class);
            if(CollectionUtils.isNotEmpty(certificates) && (Boolean) edata.get(CourseCertificateParams.reIssue.name())) {
                issueCertificate(courseId, certificateName, batchId, userId,dataToFetch,  true);
            } else if(CollectionUtils.isEmpty(certificates)) {
                issueCertificate(courseId, certificateName, batchId, userId,dataToFetch, false);
            }
        }

    }

    private void issueCertificate(String courseId, String certificateName,String batchId, String userId, Map<String, Object> dataToSelect,  boolean reIssue) {
        // get Course metadata from KP
        Map<String, Object> courseMetadata = getContent(courseId,null);
        if(MapUtils.isNotEmpty(courseMetadata)){
            String courseName = (String) courseMetadata.get("name");
            Map<String, Object> certTemplate= (Map<String, Object>) courseMetadata.get("certTemplate");

            //Get Username from user get by Id.
            Map<String, Object> userResponse = getUserDetails(userId); // call user Service
            // Save certificate to user_courses table cassandra
            List<Map<String, Object>> certificates = generateCertificate(courseName, certificateName, batchId, userId, userResponse, certTemplate);;
            if(CollectionUtils.isNotEmpty(certificates)) {
                Map<String, Object> dataToUpdate = new HashMap<String, Object>() {{
                    put(CourseCertificateParams.certificates.name(), certificates);
                }};
                SunbirdCassandraUtil.update(KEYSPACE, USER_COURSES_TABLE, dataToUpdate, dataToSelect);
                updatedES(ES_INDEX_NAME, ES_DOC_TYPE, dataToUpdate, dataToSelect);
            }
        }
    }

    private List<Map<String, Object>> generateCertificate(String courseName, String certificateName, String batchId, String userId, Map<String, Object> userResponse, Map<String, Object> certTemplate) {
        List<Map<String, Object>> certificates = new ArrayList<>();
        try{
            Map<String, Object> certServiceRequest = prepareCertServiceRequest(courseName, certificateName, batchId, userId, userResponse, certTemplate);
            String url = LEARNER_SERVICE_PRIVATE_URL + "/v1/certs/generate";
            HttpResponse<String> httpResponse = Unirest.post(url).header("Content-Type", "application/json").body(mapper.writeValueAsString(certServiceRequest)).asString();
            if(200 == httpResponse.getStatus()) {
                Response response = mapper.readValue(httpResponse.getBody(), Response.class);
                Map<String, Object> certificate = (Map<String, Object>)response.get("certificate");
                certificates.add(new HashMap<String, Object>(){{
                    put(CourseCertificateParams.name.name(), certificateName);
                    put(CourseCertificateParams.id.name(), certificate.get(CourseCertificateParams.id.name()));
                    put(CourseCertificateParams.url.name(), certificate.get(CourseCertificateParams.pdfUrl.name()));
                    put(CourseCertificateParams.lastIssuedOn.name(), formatter.format(new Date()));
                }});

                addCertificateToUser(certificate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return certificates;
    }

    private void addCertificateToUser(Map<String, Object> certificate) {
        try{
            String url = LEARNER_SERVICE_PRIVATE_URL + "/user/v1/certs/add";
            Request request = new Request();
            request.put(CourseCertificateParams.userId.name(), certificate.get(CourseCertificateParams.recipientId.name()));
            request.put(CourseCertificateParams.accessToken.name(), certificate.get(CourseCertificateParams.accessToken.name()));
            request.put(CourseCertificateParams.jsonData.name(), certificate.get(CourseCertificateParams.jsonData.name()));
            request.put(CourseCertificateParams.pdfUrl.name(), certificate.get(CourseCertificateParams.pdfUrl.name()));
            Unirest.post(url).body(mapper.writeValueAsString(request)).asString();
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    private Map<String,Object> prepareCertServiceRequest(String courseName, String certificateName, String batchId, String userId, Map<String, Object> userResponse, Map<String, Object> certTemplate) {
        String recipientName = (String) userResponse.get("firstName") + " " + (String) userResponse.get("lastName");
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
                   put(CourseCertificateParams.orgId.name(), rootOrgId);
               }});
           }});
        }};

        return request;
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
            e.printStackTrace();
            return null;
        }
    }

    private Map<String,Object> getUserDetails(String userId) {
        try{
            String url = LEARNER_SERVICE_PRIVATE_URL + "/user/v1/search";
            String userSearchRequest = prepareUserSearchRequest(userId);
            HttpResponse<String> httpResponse = Unirest.post(url).header("Content-Type", "application/json").body(userSearchRequest).asString();
            if(200 == httpResponse.getStatus()) {
                Response response = mapper.readValue(httpResponse.getBody(), Response.class);
                Map<String, Object> content = (Map<String, Object>) response.getResult().get("content");
                return content;
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private String prepareUserSearchRequest(String userId) throws Exception {
        Request request = new Request();
        request.put("filters", new HashMap<String, Object>(){{
            put("identifier", userId);
        }});
        request.put("fields", Arrays.asList("firstName", "lastName", "userName", "rootOrgName", "rootOrgId"));
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
            e.printStackTrace();
        }

    }

}
