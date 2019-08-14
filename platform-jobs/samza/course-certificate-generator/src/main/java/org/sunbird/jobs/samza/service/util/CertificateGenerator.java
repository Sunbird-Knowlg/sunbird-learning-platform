package org.sunbird.jobs.samza.service.util;

import com.datastax.driver.core.Row;
import org.apache.commons.collections.CollectionUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.learning.util.ControllerUtil;
import org.sunbird.job.samza.util.CourseCertificateParams;
import org.sunbird.job.samza.util.SunbirdCassandraUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CertificateGenerator {

    private static final String KEYSPACE = Platform.config.hasPath("") ? Platform.config.getString("") : "sunbird_courses";
    private static final String USER_COURSES_TABLE = "user_courses";
    private ControllerUtil util = new ControllerUtil();

    public void generateCertificate(Map<String, Object> edata) {
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
        Response courseMetadata = util.getDataNode("domain", courseId);
        String courseName = (String) courseMetadata.get("name");

        //Get Username from user get by Id.
        Map<String, Object> userResponse = null; // call user Service
        String name = (String) userResponse.get("name");

        // Prepare cert-service request.
        Map<String, Object> certServiceRequest = prepareCertServiceRequest(courseName, certificateName, batchId, userId, name);


        Response response = new Response();

        // Save certificate to user_courses table cassandra
        List<Map<String, Object>> certificates = new ArrayList<>();
        Map<String, Object> dataToUpdate = new HashMap<String, Object>() {{
            put(CourseCertificateParams.certificates.name(), certificates);
        }};
        SunbirdCassandraUtil.update(KEYSPACE, USER_COURSES_TABLE, dataToUpdate, dataToSelect);

        // Notification API

    }

    private Map<String,Object> prepareCertServiceRequest(String courseName, String certificateName, String batchId, String userId, String userName) {

        new HashMap<String, Object>() {{
           put(CourseCertificateParams.request.name(), new HashMap<String, Object>() {{
               put(CourseCertificateParams.certificate.name(), new HashMap<String, Object>() {{
                   put(CourseCertificateParams.data.name(), new ArrayList<Map<String, Object>>() {{
                        add(new HashMap<String, Object>() {{
                            put(CourseCertificateParams.recipientName.name(), userName);
                            put(CourseCertificateParams.recipientId.name(), userId);
                        }});
                   }});
                   put(CourseCertificateParams.courseName.name(), courseName);
                   put(CourseCertificateParams.name.name(), certificateName);
                   put(CourseCertificateParams.issuer.name(), getIssuerDetails());
                   put(CourseCertificateParams.signatoryList.name(), getSignatoryList());
                   put(CourseCertificateParams.tag.name(), batchId);
               }});
           }});
        }};

        return null;
    }


    private Map<String, Object> getIssuerDetails() {
        return null;
    }

    private List<Map<String, Object>> getSignatoryList() {
        return null;
    }

}
