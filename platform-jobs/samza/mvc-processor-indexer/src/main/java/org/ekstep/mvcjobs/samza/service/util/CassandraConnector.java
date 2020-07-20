package org.ekstep.mvcjobs.samza.service.util;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Set;

public class CassandraConnector {
  static  String arr[],serverIP= "127.0.0.1",keyspace = "sunbirddev_content_store",table = "content_data";
   static Session session;
    static ObjectMapper mapper = new ObjectMapper();
    static Session getSession() {
        Cluster cluster = Cluster.builder()
                .addContactPoints(serverIP)
                .build();

        session = cluster.connect(keyspace);
        return session;
    }
    public static void updateContentProperties(String contentId, Map<String, Object> map) {
        Session session = getSession();
        if (null == map || map.isEmpty())
            return;
        String query = getUpdateQuery(map.keySet());
        if(query == null)
            return;
        PreparedStatement ps = session.prepare(query);
        Object[] values = new Object[map.size() + 1];
        try {
            int i = 0;
            for (Map.Entry<String, Object> entry : map.entrySet()) {

                if (null == entry.getValue()) {
                    continue;
                }  else {
                    values[i] = entry.getValue();
                }

                i += 1;
            }
            values[i] = contentId;
            BoundStatement bound = ps.bind(values);

            session.execute(bound);
        } catch (Exception e) {
          System.out.println("Exception " + e);
        }
    }
    private static String getUpdateQuery(Set<String> properties) {
        StringBuilder sb = new StringBuilder();
        if (null != properties && !properties.isEmpty()) {
            sb.append("UPDATE " + table + " SET last_updated_on = dateOf(now()), ");
            StringBuilder updateFields = new StringBuilder();
            for (String property : properties) {
                if (StringUtils.isBlank(property))
                    return null;
                updateFields.append(property.trim()).append(" = ?, ");
            }
            sb.append(StringUtils.removeEnd(updateFields.toString(), ", "));
            sb.append(" where content_id = ?");
        }
        return sb.toString();
    }
}
