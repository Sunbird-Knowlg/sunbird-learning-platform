package org.ekstep.ilimi.analytics.dao

import org.ekstep.ilimi.analytics.util.AppDBUtils
import org.apache.commons.dbutils.QueryRunner
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Buffer

class BaseDAO {

    def getValue(x: Float): AnyRef = {
        var retVal: AnyRef = null;

        if (x.isNaN()) {
            retVal = 0.asInstanceOf[AnyRef];
        } else {
            retVal = x.asInstanceOf[AnyRef];
        }

        return retVal;
    }

    def getValue(x: Double): AnyRef = {
        var retVal: AnyRef = null;
        if (x.isNaN()) {
            retVal = 0.asInstanceOf[AnyRef];
        } else {
            retVal = x.asInstanceOf[AnyRef];
        }

        return retVal;
    }

    def getValue(x: Long): AnyRef = {
        var retVal: AnyRef = null;
        if (x.toInt.isNaN()) {
            retVal = 0.asInstanceOf[AnyRef];
        } else {
            retVal = x.asInstanceOf[AnyRef];
        }

        return retVal;
    }

    def batchMerge(insertSQL: String, updateSQL: String, data: Buffer[Array[AnyRef]]) = {

        val conn = AppDBUtils.getConnection;
        val qr = new QueryRunner();
        val ids: Array[Int] = qr.batch(conn, updateSQL, data.toArray);

        var insertBuffer: Buffer[Array[AnyRef]] = new ListBuffer[Array[AnyRef]]();
        for (i <- 0 until ids.length) {
            if (ids(i) == 0) {
                insertBuffer += data(i);
            }
        }

        if (insertBuffer.size > 0) {
            Console.println("### New data to insert - " + insertBuffer.size + " ###");
            qr.batch(conn, insertSQL, insertBuffer.toArray);
        }
        AppDBUtils.closeConnection(conn);
    }
}