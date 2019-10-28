package org.sunbird.jobs.samza.util;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.Delete;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.ekstep.cassandra.connector.util.CassandraConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SunbirdCassandraUtil {

    private static Map<String, String> COLUMN_MAPPING = SunbirdCassandraColumnMapper.getColumnMapping();

    public static void update(String keyspace, String table, Map<String, Object> propertiesToUpdate, Map<String, Object> propertiesToSelect) {
        Session session = CassandraConnector.getSession("platform-courses");
        Update.Where updateQuery = QueryBuilder.update(keyspace, table).where();
        propertiesToUpdate.entrySet().forEach(entry -> updateQuery.with(QueryBuilder.set(entry.getKey(), entry.getValue())));
        propertiesToSelect.entrySet().forEach(entry -> {
            if (entry.getValue() instanceof List)
                updateQuery.and(QueryBuilder.in(entry.getKey(), (List) entry.getValue()));
            else
                updateQuery.and(QueryBuilder.eq(entry.getKey(), entry.getValue()));
        });
        session.execute(updateQuery);
    }

    public static ResultSet read(String keyspace, String table, Map<String, Object> propertiesToSelect) {
        Session session = CassandraConnector.getSession("platform-courses");
        Select.Where selectQuery = QueryBuilder.select().all().from(keyspace, table).where();
        propertiesToSelect.entrySet().forEach(entry -> {
            if (entry.getValue() instanceof List)
                selectQuery.and(QueryBuilder.in(entry.getKey(), (List) entry.getValue()));
            else
                selectQuery.and(QueryBuilder.eq(entry.getKey(), entry.getValue()));
        });
        ResultSet results = session.execute(selectQuery);
        return results;
    }

    public static List<Map<String, Object>> readAsListOfMap(String keyspace, String table, Map<String, Object> propertiesToSelect) {
        ResultSet resultSet = read(keyspace, table, convertKeyCase(propertiesToSelect));
        List<Row> rows = resultSet.all();
        List<Map<String, Object>> response = new ArrayList<Map<String, Object>>();
        if (CollectionUtils.isNotEmpty(rows)) {
            for (Row row : rows) {
                Map<String, Object> rowMap = new HashMap<String, Object>();
                row.getColumnDefinitions().forEach(column -> rowMap.put(COLUMN_MAPPING.get(column.getName()), row.getObject(column.getName())));
                response.add(rowMap);
            }
        }
        return response;
    }

    public static void upsert(String keyspace, String table, Map<String, Object> properties) {
        Session session = CassandraConnector.getSession("platform-courses");
        Insert insertQuery = QueryBuilder.insertInto(keyspace, table);
        convertKeyCase(properties).entrySet().forEach(entry -> insertQuery.value(entry.getKey(), entry.getValue()));
        session.execute(insertQuery);
    }

    public static void delete(String keyspace, String table, Map<String, Object> properties) {
        Session session = CassandraConnector.getSession("platform-courses");
        Delete.Where deleteQuery = QueryBuilder.delete().from(keyspace, table).where();
        convertKeyCase(properties).entrySet().forEach(entry -> {
            deleteQuery.and(QueryBuilder.eq(entry.getKey(), entry.getValue()));
        });
        session.execute(deleteQuery);
    }

    private static Map<String, Object> convertKeyCase(Map<String, Object> properties) {
        Map<String, Object> keyLowerCaseMap = new HashMap<>();
        if (MapUtils.isNotEmpty(properties)) {
            properties.entrySet().forEach(entry -> {
                if (null != entry && null != entry.getKey()) {
                    keyLowerCaseMap.put(entry.getKey().toLowerCase(), entry.getValue());
                }
            });
        }
        return keyLowerCaseMap;
    }

    public static ResultSet execute(String query) {
        Session session = CassandraConnector.getSession("platform-courses");
        ResultSet results = session.execute(query);
        return results;
    }

}