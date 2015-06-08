package org.ekstep.ilimi.analytics.util

import javax.sql.DataSource
import java.sql.Connection
import org.apache.commons.dbutils.DbUtils
import java.sql.DriverManager
import org.ekstep.ilimi.analytics.conf.AppConf

object AppDBUtils {
    
    private var dataSource:DataSource = null;
    
    def getConnection() : Connection = {
        DriverManager.getConnection(AppConf.getConfig("db.url"), AppConf.getConfig("db.username"), AppConf.getConfig("db.password"));
    }
    
    def closeConnection(conn: Connection) = {
        DbUtils.commitAndCloseQuietly(conn);
    }

}