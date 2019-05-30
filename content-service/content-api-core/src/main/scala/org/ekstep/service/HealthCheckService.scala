package org.ekstep.service

import org.ekstep.commons.Request
import org.ekstep.cassandra.connector.util.CassandraConnector
import org.ekstep.common.mgr.BaseManager
import org.ekstep.graph.cache.factory.JedisFactory
import org.ekstep.graph.engine.router.GraphEngineManagers
import org.ekstep.learning.router.LearningRequestRouterPool
import org.ekstep.telemetry.logger.TelemetryManager

import scala.collection.mutable.ListBuffer

object HealthCheckService extends BaseManager {

  val CONNECTION_SUCCESS: String = "connection check is successful"
  val CONNECTION_FAILED: String = "connection check has failed"
  var overAllHealth = true;

  def checkSystemHealth(request: Request) = {
    var status = false
    var allChecks = new ListBuffer[Map[String, Any]]()
    status = getNeo4jHealth()
    if (!status)
      overAllHealth = false
    allChecks += generateChecks("graph db", status)
    status = getRedisHealth()
    if (!status)
      overAllHealth = false
    allChecks += generateChecks("redis cache", status)
    status = getCassandraHealth()
    if (!status)
      overAllHealth = false
    allChecks += generateChecks("cassandra db", status)

    val responseMap = Map[String, AnyRef](
      "name" -> "org.ekstep.health.api",
      "healthy" -> Boolean.box(overAllHealth),
      "checks" -> allChecks)
    responseMap
  }

  def getCassandraHealth(): Boolean = {
    try {
      val session = CassandraConnector.getSession()
      if (session != null && !session.isClosed) {
        session.execute("SELECT now() FROM system.local")
        TelemetryManager.info("cassandra db " + CONNECTION_SUCCESS)
        true
      } else {
        TelemetryManager.error("cassandra db " + CONNECTION_FAILED)
        false
      }
    } catch {
      case e: Exception => {
        TelemetryManager.error("cassandra db" + CONNECTION_FAILED, e)
        false
      }
    }
  }

  def getNeo4jHealth(): Boolean = {
    LearningRequestRouterPool.init();
    try {
      val createReq = getRequest("domain", GraphEngineManagers.NODE_MANAGER, "upsertRootNode")
      val response = getResponse(createReq)
      if (checkError(response)) {
        TelemetryManager.error("graph db " + CONNECTION_FAILED)
        false
      }
      else {
        TelemetryManager.info("graph db " + CONNECTION_SUCCESS)
        true
      }
    } catch {
      case e: Exception => {
        TelemetryManager.error("redis cache" + CONNECTION_FAILED, e)
        false
      }
    }
  }

  def getRedisHealth(): Boolean = {
    try {
      val jedis = JedisFactory.getRedisConncetion()
      jedis.close()
      TelemetryManager.info("graph db " + CONNECTION_SUCCESS)
      true
    } catch {
      case e: Exception => {
        TelemetryManager.error("redis cache" + CONNECTION_FAILED, e)
        false
      }

    }
  }

  def generateChecks(db: String, status: Boolean): Map[String, Any] = {
    var check: Map[String, Any] = Map("name" -> db)
    if (status) {
      check += ("healthy" -> status)
    }
    else {
      check += ("healthy" -> status, "err" -> "503", "errmsg" -> (db + " connection unavailable"))
    }
    check
  }

}
