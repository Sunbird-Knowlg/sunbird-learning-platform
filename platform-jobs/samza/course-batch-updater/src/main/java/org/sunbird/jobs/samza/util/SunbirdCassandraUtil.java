package org.sunbird.jobs.samza.util;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import org.ekstep.cassandra.connector.util.CassandraConnector;

import java.util.List;
import java.util.Map;

public class SunbirdCassandraUtil {

    public static void update(String keyspace, String table, Map<String, Object> propertiesToUpdate, Map<String, Object> propertiesToSelect) {
        Session session = CassandraConnector.getSession("platform-courses");
        Update.Where updateQuery = QueryBuilder.update(keyspace, table).where();
        propertiesToUpdate.entrySet().forEach(entry -> updateQuery.with(QueryBuilder.set(entry.getKey(), entry.getValue())));
        propertiesToSelect.entrySet().forEach(entry -> {
            if(entry.getValue() instanceof List)
                updateQuery.and(QueryBuilder.in(entry.getKey(), (List)entry.getValue()));
            else
                updateQuery.and(QueryBuilder.eq(entry.getKey(), entry.getValue()));
        });
        session.execute(updateQuery);
    }

    public static List<Row> read(String keyspace, String table, Map<String, Object> propertiesToSelect) {
        Session session = CassandraConnector.getSession("platform-courses");
        Select.Where selectQuery = QueryBuilder.select().all().from(keyspace, table).where();
        propertiesToSelect.entrySet().forEach(entry -> {
            if(entry.getValue() instanceof List)
                selectQuery.and(QueryBuilder.in(entry.getKey(), (List)entry.getValue()));
            else
                selectQuery.and(QueryBuilder.eq(entry.getKey(), entry.getValue()));
        });
        ResultSet results = session.execute(selectQuery);
        return results.all();
    }

}