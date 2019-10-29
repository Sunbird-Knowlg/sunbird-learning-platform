package org.sunbird.jobs.samza.service.util;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.TypeTokens;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.Platform;
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

    private static final String CERT_SERVICE_URL = Platform.config.hasPath("cert_service.base_url")
            ? Platform.config.getString("cert_service.base_url") : "http://localhost:9000";
    private static final String COURSE_BATCH_TABLE = "course_batch";
    private static final String USER_COURSES_TABLE = "user_courses";
    private static final String ASSESSMENT_AGGREGATOR_TABLE = "assessment_aggregator";
    protected static ObjectMapper mapper = new ObjectMapper();
    private static final List<String> COLUMNS_WITH_TIMESTAMP = Arrays.asList("completedon", "datetime");

    private static final String KEYSPACE = Platform.config.hasPath("courses.keyspace.name")
            ? Platform.config.getString("courses.keyspace.name") : "sunbird_courses";

    private static final String topic = Platform.config.getString("task.inputs").replace("kafka.", "");
    private static JobLogger LOGGER = new JobLogger(IssueCertificate.class);

    public void issue(Map<String, Object> edata, MessageCollector collector) {
        String batchId = (String) edata.get(CourseCertificateParams.batchId.name());
        String courseId = (String) edata.get(CourseCertificateParams.courseId.name());
        Boolean reissue = (null != edata.get(CourseCertificateParams.reIssue.name()))
                ? (Boolean) edata.get(CourseCertificateParams.reIssue.name()) : false;

        if (StringUtils.isNotBlank(batchId) && StringUtils.isNotBlank(courseId)) {
            Map<String, Map<String, String>> templates = fetchTemplates(batchId, courseId);
            if (MapUtils.isNotEmpty(templates)) {
                fetchUsersAndIssueCertificates(batchId, courseId, reissue, templates, collector);
            }
        }
    }

    private Map<String, Map<String, String>> fetchTemplates(String batchId, String courseId) {
        Map<String, Object> dataToFetch = new HashMap<String, Object>() {{
            put(CourseCertificateParams.batchId.name(), batchId);
            put(CourseCertificateParams.courseId.name(), courseId);
        }};
        ResultSet resultSet = SunbirdCassandraUtil.read(KEYSPACE, COURSE_BATCH_TABLE, dataToFetch);
        Row row = resultSet.one();
        return row.getMap("cert_templates", TypeToken.of(String.class), TypeTokens.mapOf(String.class, String.class));
    }

    private void fetchUsersAndIssueCertificates(String batchId, String courseId, Boolean reIssue, Map<String, Map<String, String>> templates, MessageCollector collector) {
        try {
            for (String key : templates.keySet()) {
                Map<String, String> template = templates.get(key);
                String criteriaString = template.get("criteria");
                if (StringUtils.isNotBlank(criteriaString)) {
                    Map<String, Object> criteria = mapper.readValue(criteriaString, new TypeReference<Map<String, Object>>() {
                    });
                    List<String> enrolledUsers = getUserFromEnrolmentCriteria((Map<String, Object>) criteria.get("enrollment"), batchId, courseId);
                    List<String> usersAssessed = getUersFromAssessmentCriteria((Map<String, Object>) criteria.get("assessment"), batchId, courseId);
                    generateCertificatesForEnrollment(enrolledUsers, usersAssessed, batchId, courseId, reIssue, template, collector);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while fetching user and generatecertificates", e);
        }
    }

    private List<String> getUersFromAssessmentCriteria(Map<String, Object> assessmentCriteria, String batchId, String courseId) {
        List<String> assessedUsers = new ArrayList<>();
        if(MapUtils.isNotEmpty(assessmentCriteria)) {
            Map<String, Double> userScores = fetchAssesedUsersFromDB(batchId, courseId);
            Map<String, Double> criteria = getAssessmentOperation(assessmentCriteria);
            if(MapUtils.isNotEmpty(userScores)){
                for(String user: userScores.keySet()) {
                    if(isValidAssessUser(userScores.get(user), criteria)){
                        assessedUsers.add(user);
                    }
                }
            }
        }

        return assessedUsers;
    }

    private boolean isValidAssessUser(Double actualScore, Map<String, Double> criteria) {
        String operation = (String) criteria.keySet().toArray()[0];
        Double score = criteria.get(operation);
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

    private Map<String, Double> getAssessmentOperation(Map<String, Object> assessmentCriteria) {
        if (assessmentCriteria.get("score") instanceof Map) {
            return ((Map) assessmentCriteria.get("score"));
        } else {
            return new HashMap<String, Double>(){{put("EQ", ((Number)assessmentCriteria.get("score")).doubleValue());}};
        }
    }

    private Map<String, Double> fetchAssesedUsersFromDB(String batchId, String courseId) {
        String query = "SELECT user_id, max(total_score) as score, total_max_score FROM sunbird_courses.assessment_aggregator " +
                "where course_id=' " +courseId + "' AND batch_id='" + batchId + "' " +
                "GROUP BY course_id,batch_id,user_id,content_id ORDER BY batch_id,user_id,content_id;";
        ResultSet resultSet = SunbirdCassandraUtil.execute(query);
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
        Map<String, Double> result = userScore.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> ((entry.getValue().get("score") *100)/entry.getValue().get("maxScore"))));

        return result;
    }

    private List<String> getUserFromEnrolmentCriteria(Map<String, Object> enrollment, String batchId, String courseId) {
        List<String> enrolledUsers = new ArrayList<>();
        try {
            if(MapUtils.isNotEmpty(enrollment)){
                Map<String, Object> dataToFetch = new HashMap<String, Object>() {{
                    put(CourseCertificateParams.batchId.name(), batchId);
                    putAll(enrollment);
                }};
                ResultSet resultSet = SunbirdCassandraUtil.read(KEYSPACE, USER_COURSES_TABLE, dataToFetch);
                Iterator<Row> rowIterator = resultSet.iterator();
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    if (row.getBool("active")) {
                        enrolledUsers.add(row.getString("userid"));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while fetching users", e);
        }

        return enrolledUsers;
    }

    private void generateCertificatesForEnrollment(List<String> enrolledUsers, List<String> usersAssessed, String batchId, String courseId, Boolean reIssue, Map<String, String> template, MessageCollector collector) throws IOException {
        Map<String, Object> certTemplate = new HashMap<>();
        certTemplate.putAll(template);
        Map<String, Object> issuer = StringUtils.isNotBlank((String)certTemplate.get("issuer"))
                ? mapper.readValue((String)certTemplate.get("issuer"), new TypeReference<Map<String, Object>>(){}) : new HashMap<>();
        List<Map<String, Object>> signatoryList = StringUtils.isNotBlank((String)certTemplate.get("signatoryList"))
                ? mapper.readValue((String)certTemplate.get("signatoryList"), new TypeReference<List<Map<String, Object>>>(){}) : new ArrayList<>();
        certTemplate.remove("criteria");
        certTemplate.put("issuer", issuer);
        certTemplate.put("signatoryList", signatoryList);
        List<String> users = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(enrolledUsers) && CollectionUtils.isNotEmpty(usersAssessed)){
            users = enrolledUsers.stream().filter(usersAssessed::contains).collect(Collectors.toList());
        }
        else if(CollectionUtils.isNotEmpty(enrolledUsers) && CollectionUtils.isEmpty(usersAssessed)) {
            users = enrolledUsers;
        }
        else{
            users = usersAssessed;
        }
        if(CollectionUtils.isNotEmpty(users)){
            for(String userId: users){
                Map<String, Object> event = prepareCertificateEvent(batchId, courseId, userId, reIssue, certTemplate);
                collector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", topic), event));
            }
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
