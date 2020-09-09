package org.sunbird.jobs.samza.service.util;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TypeTokens;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.jobs.samza.util.JobLogger;
import org.sunbird.jobs.samza.util.CourseCertificateParams;
import org.sunbird.jobs.samza.util.SunbirdCassandraUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class IssueCertificate {

    private static final String LEARNER_SERVICE_PRIVATE_URL = Platform.config.hasPath("learner_service.base_url")
            ? Platform.config.getString("learner_service.base_url"): "http://localhost:9000";
    private static final String COURSE_BATCH_TABLE = "course_batch";
    private static final String USER_COURSES_TABLE = "user_enrolments";
    private static final String ASSESSMENT_AGGREGATOR_TABLE = "assessment_aggregator";
    protected static ObjectMapper mapper = new ObjectMapper();

    private static final String KEYSPACE = Platform.config.hasPath("courses.keyspace.name")
            ? Platform.config.getString("courses.keyspace.name") : "sunbird_courses";

    private static final String topic = Platform.config.getString("task.inputs").replace("kafka.", "");
    private static final List<String> certFilterKeys = Arrays.asList("enrollment", "assessment", "user");
    private static JobLogger LOGGER = new JobLogger(IssueCertificate.class);
    private Session cassandraSession = null;
    
    public IssueCertificate(Session cassandraSession) {
        this.cassandraSession = cassandraSession;
    }

    public void issue(Map<String, Object> edata, MessageCollector collector) throws Exception {
        String batchId = (String) edata.get(CourseCertificateParams.batchId.name());
        String courseId = (String) edata.get(CourseCertificateParams.courseId.name());
        List<String> userIds = (null != edata.get(CourseCertificateParams.userIds.name()))? (List<String>)edata.get(CourseCertificateParams.userIds.name()): new ArrayList<>();
        LOGGER.info("IssueCertificate:issue: userIds : " + userIds);
        LOGGER.info("IssueCertificate:issue: eData : " + mapper.writeValueAsString(edata));
        Boolean reissue = (null != edata.get(CourseCertificateParams.reIssue.name()))
                ? (Boolean) edata.get(CourseCertificateParams.reIssue.name()) : false;

        if (StringUtils.isNotBlank(batchId) && StringUtils.isNotBlank(courseId)) {
            Map<String, Map<String, String>> templates = fetchTemplates(batchId, courseId);
            if (MapUtils.isNotEmpty(templates)) {
                fetchUsersAndIssueCertificates(batchId, courseId, reissue, templates, collector, userIds);
            } else {
                LOGGER.info("IssueCertificate:issue: Certificate template is not available for batchId : "+ batchId + " and courseId : " + courseId);
                throw new ClientException("ERR_GENERATE_CERTIFICATE", "Certificate template is not available for batchId : "+ batchId + " and courseId : " + courseId);
            }
        } else {
            LOGGER.info("IssueCertificate:issue: batchId and/or courseId is empty");
            throw new ClientException("ERR_GENERATE_CERTIFICATE", "batchId and/or courseId is empty");
        }
    }

    private Map<String, Map<String, String>> fetchTemplates(String batchId, String courseId) {
        Map<String, Object> dataToFetch = new HashMap<String, Object>() {{
            put(CourseCertificateParams.batchId.name(), batchId);
            put(CourseCertificateParams.courseId.name(), courseId);
        }};
        ResultSet resultSet = SunbirdCassandraUtil.read(cassandraSession, KEYSPACE, COURSE_BATCH_TABLE, dataToFetch);
        Row row = resultSet.one();
        return row.getMap("cert_templates", TypeToken.of(String.class), TypeTokens.mapOf(String.class, String.class));
    }

    private void fetchUsersAndIssueCertificates(String batchId, String courseId, Boolean reIssue, Map<String, Map<String, String>> templates, MessageCollector collector, List<String> userIds) {
        try {
            LOGGER.info("IssueCertificate:fetchUsersAndIssueCertificates: userIds " + userIds);
            for (String key : templates.keySet()) {
                Map<String, String> template = templates.get(key);
                //String certName = template.getOrDefault("name", "");
                String criteriaString = template.get("criteria");
                if (StringUtils.isNotBlank(criteriaString)) {
                    Map<String, Object> criteria = mapper.readValue(criteriaString, new TypeReference<Map<String, Object>>() {
                    });
                    if (MapUtils.isNotEmpty(criteria) && CollectionUtils.isNotEmpty(CollectionUtils.intersection(criteria.keySet(), certFilterKeys))) {
                        List<String> enrollmentList = getUserFromEnrolmentCriteria((Map<String, Object>) criteria.get("enrollment"), batchId, courseId, userIds, template, reIssue);
                        List<String> assessmentList = getUsersFromAssessmentCriteria((Map<String, Object>) criteria.get("assessment"), batchId, courseId, userIds);
                        List<String> userList = getUsersFromUserCriteria((Map<String, Object>) criteria.get("user"), new ArrayList<String>() {{
                            addAll(enrollmentList);
                            addAll(assessmentList);
                        }});
                        List<String> templateFilterKeys = (List<String>) CollectionUtils.intersection(criteria.keySet(), certFilterKeys);
                        Map<String, List<String>> usersMap = new HashMap<String, List<String>>();
                        usersMap.put("enrollment", enrollmentList);
                        usersMap.put("assessment", assessmentList);
                        usersMap.put("user", userList);
                        List<String> usersToIssue = templateFilterKeys.stream()
                                .filter(templateFilterKey -> criteria.containsKey(templateFilterKey))
                                .map(templateFilterKey -> usersMap.get(templateFilterKey))
                                .reduce((a, b) -> {
                                    return (List<String>) CollectionUtils.intersection(a, b).stream().collect(Collectors.toList());
                                }).orElse(Arrays.asList());

                        generateCertificatesForEnrollment(usersToIssue, batchId, courseId, reIssue, template, collector);
                    } else {
                        LOGGER.info("IssueCertificate:fetchUsersAndIssueCertificates: Certificate template has empty/invalid criteria: " + criteria);
                        throw new ClientException("ERR_INVALID_CERTIFICATE_TEMPLATE", "Certificate template has empty/invalid criteria: " + criteria);
                    }
                } else {
                    LOGGER.info("IssueCertificate:fetchUsersAndIssueCertificates: Certificate template has empty criteria: " + criteriaString);
                    throw new ClientException("ERR_INVALID_CERTIFICATE_TEMPLATE", "Certificate template has empty criteria: " + criteriaString);
                }
            }
        } catch (Exception e) {
            LOGGER.error("IssueCertificate:fetchUsersAndIssueCertificates: Error while fetching user and generate certificates", e);
            throw new ServerException("ERR_GENERATE_CERTIFICATE", "Error while fetching user and generate certificates : " + e);
        }
    }

    private static List<String> getUsersFromUserCriteria(Map<String, Object> criteria, List<String> userIds) throws Exception {
        if(MapUtils.isEmpty(criteria))
            return new ArrayList<>();
        Integer batchSize = 50;
        String url = LEARNER_SERVICE_PRIVATE_URL + "/private/user/v1/search";
        List<List<String>> batchList = ListUtils.partition(userIds, batchSize);
        LOGGER.info("Users as batches: " + batchList);
        List<String> filteredUsers =  batchList.stream().map(batch -> {
            try {
                Request request = new Request();
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.putAll(criteria);
                filters.put("identifier", batch);
                request.put("filters", filters);
                request.put("limit", batchSize);
                String requestBody = mapper.writeValueAsString(request);
                LOGGER.info("requestBody for user search: " + requestBody);
                HttpResponse<String> httpResponse = Unirest.post(url).header("Content-Type", "application/json").body(requestBody).asString();
                if(200 == httpResponse.getStatus()) {
                    String responseBody = httpResponse.getBody();
                    Map<String, Object> responseMap = mapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {
                    });
                    Map<String, Object> respResult = (Map<String, Object>)  responseMap.getOrDefault("result", new HashMap<String, Object>());
                    Map<String, Object> response =  (Map<String, Object>)  respResult.getOrDefault("response", new HashMap<String, Object>());
                    List<Map<String, Object>> users = (List<Map<String, Object>>) response.getOrDefault("content", new ArrayList<Map<String, Object>>());
                    LOGGER.info("Users fetched from user search: " + users);
                    return users.stream()
                            .map(user -> (String) user.getOrDefault("identifier", ""))
                            .filter(identifier -> StringUtils.isNotBlank(identifier)).collect(Collectors.toList());
                } else {
                    LOGGER.error("Search users for given criteria failed to fetch data: "+  httpResponse, null);
                    return new ArrayList<String>();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).flatMap(List::stream).collect(Collectors.toList());
        LOGGER.info(" No. of users after applied user criteria: " + filteredUsers.size());
        return filteredUsers;
    }

    private List<String> getUsersFromAssessmentCriteria(Map<String, Object> assessmentCriteria, String batchId, String courseId, List<String> userIds) {
        List<String> assessedUsers = new ArrayList<>();
        if(MapUtils.isNotEmpty(assessmentCriteria)) {
            Map<String, Double> userScores = fetchAssessedUsersFromDB(batchId, courseId);
            Map<String, Object> criteria = getAssessmentOperation(assessmentCriteria);
            if(MapUtils.isNotEmpty(userScores)){
                for(String user: userScores.keySet()) {
                    if(isValidAssessUser(userScores.get(user), criteria)){
                        assessedUsers.add(user);
                    }
                }
            } else {
                LOGGER.info("No assessment score for batchID: " + batchId + " and courseId: " + courseId);
            }
        }
        if(CollectionUtils.isNotEmpty(userIds)){
            return (List<String>) CollectionUtils.intersection(assessedUsers, userIds);
        } else{
            LOGGER.info("No users satisfy assessment criteria for batchID: " + batchId + " and courseID: " + courseId);
            return assessedUsers;
        }
    }

    private boolean isValidAssessUser(Double actualScore, Map<String, Object> criteria) {
        String operation = (String) criteria.keySet().toArray()[0];
        Double score = ((Number)criteria.get(operation)).doubleValue();
        switch (operation) {
            case "EQ":
            case "eq":
            case "=": return (actualScore == score);

            case ">": return (actualScore > score);

            case "<": return (actualScore < score);

            case ">=": return (actualScore >= score);

            case "<=": return (actualScore <= score);

            case "ne":
            case "!=": return (actualScore != score);

            default: return false;
        }
    }

    private Map<String, Object> getAssessmentOperation(Map<String, Object> assessmentCriteria) {
        if (assessmentCriteria.get("score") instanceof Map) {
            return ((Map) assessmentCriteria.get("score"));
        } else {
            return new HashMap<String, Object>(){{put("EQ", ((Number)assessmentCriteria.get("score")).doubleValue());}};
        }
    }

    private Map<String, Double> fetchAssessedUsersFromDB(String batchId, String courseId) {
        String query = "SELECT user_id, max(total_score) as score, total_max_score FROM " + KEYSPACE +"." + ASSESSMENT_AGGREGATOR_TABLE +
                " where course_id='" +courseId + "' AND batch_id='" + batchId + "' " +
                "GROUP BY course_id,batch_id,user_id,content_id ORDER BY batch_id,user_id,content_id;";
        LOGGER.info("IssueCertificate : fetchAssessedUsersFromDB :: query " + query);
        ResultSet resultSet = SunbirdCassandraUtil.execute(cassandraSession, query);
        Iterator<Row> rows = resultSet.iterator();
        Map<String, Map<String, Double>> userScore = new HashMap<>();
        while(rows.hasNext()) {
            Row row = rows.next();
            String userID = row.getString("user_id");
            if(MapUtils.isNotEmpty(userScore.get(userID))) {
                Map<String, Double> scoreMap = userScore.get(userID);
                scoreMap.put("score", (scoreMap.get("score") + row.getDouble("score")));
                scoreMap.put("maxScore", (scoreMap.get("maxScore") + row.getDouble("total_max_score")));
            } else {
                userScore.put(userID, new HashMap<String, Double>() {{
                    put("score", row.getDouble("score"));
                    put("maxScore", row.getDouble("total_max_score"));
                }});
            }
        }
        LOGGER.info("UserScores for batchId: " + batchId + " and courseID: " + courseId + " is :" + userScore);
        Map<String, Double> result = userScore.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> ((entry.getValue().get("score") *100)/entry.getValue().get("maxScore"))));
        LOGGER.info("The users scores for batchID: " + batchId + "  and courseID: " + courseId + " are : " + result);
        return result;
    }

    public List<String> getUserFromEnrolmentCriteria(Map<String, Object> enrollment, String batchId, String courseId, List<String> userIds, Map<String, String> template, Boolean reIssue) {
        List<String> enrolledUsers = new ArrayList<>();
        String certName = template.getOrDefault("name", "");
        Map<String, Object> certTemplate = CertificateGenerator.getCertTemplate(template.getOrDefault("identifier", ""));
        String templateUrl = (String)certTemplate.get("template");
        
        try {
            if(MapUtils.isNotEmpty(enrollment)){
                Map<String, Object> dataToFetch = new HashMap<String, Object>() {{
                    put(CourseCertificateParams.batchId.name(), batchId);
                    put(CourseCertificateParams.courseId.name(), courseId);
                    putAll(enrollment);
                }};
                LOGGER.info("IssueCertificate:getUserFromEnrolmentCriteria: userIds " + userIds);
                if(CollectionUtils.isNotEmpty(userIds)) {
                    dataToFetch.put(CourseCertificateParams.userId.name(), userIds);
                }
                ResultSet resultSet = SunbirdCassandraUtil.read(cassandraSession, KEYSPACE, USER_COURSES_TABLE, dataToFetch);
                Iterator<Row> rowIterator = resultSet.iterator();
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    //TODO: Added code for validating issued_certificates and certificates with respect to template url. 
                    // Need to be removed while depricating course-certificate-generator v1 job
                    List<Map<String, String>> certificates=null;
                    if(StringUtils.isNotBlank(templateUrl) && StringUtils.endsWith(templateUrl, ".svg")) {
                    	certificates = row.getList("issued_certificates", TypeTokens.mapOf(String.class, String.class));
                    }else if(StringUtils.isNotBlank(templateUrl) && !StringUtils.endsWith(templateUrl, ".svg")) {
                    	certificates = row.getList("certificates", TypeTokens.mapOf(String.class, String.class));
                    }
                    boolean isCertIssued = CollectionUtils.isNotEmpty(certificates) && 
                            (CollectionUtils.isNotEmpty(certificates.stream().filter(map -> 
                            StringUtils.equalsIgnoreCase(certName, (String)map.getOrDefault("name", ""))).collect(Collectors.toList())));
                    
                    
                    if (row.getBool("active") && (!isCertIssued || reIssue)) {
                        enrolledUsers.add(row.getString("userid"));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while fetching users", e);
        }
        LOGGER.info("Users filtered after applying enrollment criteria: " + enrolledUsers.size());
        return enrolledUsers;
    }

    private void generateCertificatesForEnrollment(List<String> usersToIssue, String batchId, String courseId, Boolean reIssue, Map<String, String> template, MessageCollector collector) throws Exception {
        Map<String, Object> certTemplate = new HashMap<>();
        certTemplate.putAll(template);
        Map<String, Object> issuer = StringUtils.isNotBlank((String)certTemplate.get("issuer"))
                ? mapper.readValue((String)certTemplate.get("issuer"), new TypeReference<Map<String, Object>>(){}) : new HashMap<>();
        List<Map<String, Object>> signatoryList = StringUtils.isNotBlank((String)certTemplate.get("signatoryList"))
                ? mapper.readValue((String)certTemplate.get("signatoryList"), new TypeReference<List<Map<String, Object>>>(){}) : new ArrayList<>();
        Map<String, Object> criteria = mapper.readValue( (String) certTemplate.remove("criteria"), new TypeReference<Map<String, Object>>() {
        });
        certTemplate.put("issuer", issuer);
        certTemplate.put("signatoryList", signatoryList);

        if(CollectionUtils.isNotEmpty(usersToIssue)) {
            LOGGER.info(template.get("name") + " - " + "certificate will be issuing to : "+ usersToIssue);
            for(String userId: usersToIssue) {
                Map<String, Object> event = prepareCertificateEvent(batchId, courseId, userId, reIssue, certTemplate);
                collector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", topic), event));
            }
        } else {
            LOGGER.info("IssueCertificate:generateCertificatesForEnrollment: NO users satisfied the criteria for batchId: " + batchId + " and courseId: " + courseId);
            throw new ClientException("ERR_GENERATE_CERTIFICATE", "NO users satisfied the criteria for batchId: " + batchId + " and courseId: " + courseId);
        }
    }

    private Map<String, Object> prepareCertificateEvent(String batchId, String courseId, String userId, Boolean reIssue, Map<String, Object> template) {
        String templateId = (String) template.get("identifier");
        return new HashMap<String, Object>() {{
            put("actor", new HashMap<String, Object>() {{
                put("id", "Course Certificate Generator");
                put("type", "System");
            }});
            put("eid", "BE_JOB_REQUEST");
            put("edata", new HashMap<String, Object>() {{
                put("action", "generate-course-certificate");
                put("iteration", 1);
                put("batchId", batchId);
                put("userId", userId);
                put("courseId", courseId);
                put("template", template);
                put("reIssue", reIssue);
            }});
            put("ets", System.currentTimeMillis());
            put("pdata", new HashMap<String, Object>() {{
                put("ver", "1,0");
                put("id", "org.sunbird.platform");
            }});
            put("mid", "LP." + System.currentTimeMillis() + "." + UUID.randomUUID());
            put("object", new HashMap<String, Object>() {{
                put("id", DigestUtils.md5Hex(batchId + "_" + userId + "_" + templateId));
                put("type", "CourseCertificateGeneration");
            }});
        }};
    }
}
