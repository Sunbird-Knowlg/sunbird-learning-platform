package org.ekstep.job.samza.util;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Test {

    public static void main(String[] args) {
        Map<String, Object> obj= new HashMap<String, Object>() {{
            put("batchId", "as");
            put("courseId", "asasd");
        }};
        /*String courseId = "course-001";
        String batchId = "batch-001";
        List<String> userIds = Arrays.asList("user-001", "user-003");

        Session session = Cluster.builder().addContactPoint("11.2.3.63").withPort(9042).withQueryOptions(new QueryOptions().setConsistencyLevel(ConsistencyLevel.QUORUM)).build().connect();

        PreparedStatement statement = session.prepare("SELECT user_id, max(total_score) as score, total_max_score FROM " + "sunbird_courses" +"." + "assessment_aggregator" +
                " where course_id=? AND batch_id=? AND user_id in ? " +
                "GROUP BY user_id,course_id,batch_id,content_id");
        
        ResultSet results = session.execute(statement.bind(courseId, batchId, userIds));
        
        
        System.out.println(statement.getQueryString());
        System.out.println(results.one());
        
        session.close();*/
        
        List<String> eL = Arrays.asList("as");
        List<String> al = new ArrayList<>();

        System.out.println(CollectionUtils.intersection(eL, al).stream().distinct().collect(Collectors.toList()));
    }
}
