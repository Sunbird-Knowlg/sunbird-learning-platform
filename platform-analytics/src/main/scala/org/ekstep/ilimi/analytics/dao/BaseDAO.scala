package org.ekstep.ilimi.analytics.dao

import scala.collection.mutable.Buffer
import scala.collection.mutable.ListBuffer

import org.apache.commons.dbutils.QueryRunner
import org.ekstep.ilimi.analytics.util.AppDBUtils

class BaseDAO {

    def getFloatValue(x: Float): Float = {
        var retVal: Float = 0;

        if (x.isNaN() || x.isInfinite()) {
            Console.println("### Nan or Infinite ### - " + x);
            retVal = 0;
        } else {
            retVal = x;
        }

        return retVal;

    }

    def getValue(x: Float): AnyRef = {
        var retVal: AnyRef = null;

        if (x.isNaN() || x.isInfinite()) {
            Console.println("### Nan or Infinite ### - " + x);
            retVal = 0.asInstanceOf[AnyRef];
        } else {
            retVal = x.asInstanceOf[AnyRef];
        }

        return retVal;
    }

    def getValue(x: Double): AnyRef = {
        var retVal: AnyRef = null;
        if (x.isNaN() || x.isInfinite()) {
            Console.println("### Nan or Infinite ### - " + x);
            retVal = 0.asInstanceOf[AnyRef];
        } else {
            retVal = x.asInstanceOf[AnyRef];
        }

        return retVal;
    }

    def getValue(x: Long): AnyRef = {
        var retVal: AnyRef = null;
        if (x.toInt.isNaN() || x.toInt.isInfinite()) {
            Console.println("### Nan or Infinite ### - " + x);
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