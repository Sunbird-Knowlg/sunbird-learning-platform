package org.ekstep.ilimi.analytics.dao

import scala.collection.mutable.Buffer
import org.ekstep.ilimi.analytics.util.AppDBUtils
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.ResultSetHandler
import java.sql.ResultSet
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import scala.collection.mutable.HashMap

object LitScreenerLevelDAO extends BaseDAO {

    private val h = new ResultSetHandler[Map[String, String]]() {

        override def handle(rs: ResultSet): Map[String, String] = {
            if (!rs.next()) {
                return null;
            }

            var result = new HashMap[String, String]();
            var i = 0;
            while (rs.next()) {
                result(rs.getString(1)) = rs.getString(2);
            }

            result;
        }
    };

    def getUserMapping(): Map[String, String] = {
        val conn = AppDBUtils.getConnection;
        val qr = new QueryRunner();

        qr.query(conn, "SELECT encoded_id, ekstep_id FROM children", h);
    }

    def main(args: Array[String]): Unit = {
        getUserMapping().foreach(f => Console.println("Key:" + f._1 + " | Value:" + f._2));
    }

}