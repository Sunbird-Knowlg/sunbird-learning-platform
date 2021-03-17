package org.sunbird.jobs.samza.util;

import com.datastax.driver.core.Session;
import org.sunbird.cassandra.connector.util.CassandraConnector;

public class QRCodeCassandraConnector {

    public static void updateDownloadUrl(String id, String downloadUrl) {
        String query = "update dialcodes.dialcode_images set status=2, url='"+downloadUrl+"' where filename='"+id+"'";
        executeQuery(query);
    }

    public static void updateDownloadZIPUrl(String id, String downloadZIPUrl) {
        String query = "update dialcodes.dialcode_batch set status=2, url='"+downloadZIPUrl+"' where processid="+id;
        executeQuery(query);
    }

    public static void updateFailure(String id, String errMsg) {
        String query = "update dialcodes.dialcode_batch set status=3, url='' where processid="+id;
        executeQuery(query);
    }

    private static void executeQuery(String query) {
        Session session = CassandraConnector.getSession("sunbird");
        session.execute(query);
    }
}
