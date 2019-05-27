package org.ekstep.service

import org.ekstep.commons.Request
import org.ekstep.cassandra.connector.util.CassandraConnector
import org.ekstep.common.mgr.BaseManager
import org.ekstep.graph.cache.factory.JedisFactory
import org.ekstep.graph.engine.router.GraphEngineManagers
import org.ekstep.learning.router.LearningRequestRouterPool
import org.ekstep.util.{CommonUtil, JSONUtils}

object HealthCheckService extends BaseManager {

  def checkSystemHealth(request: Request) = {
    println(request.apiId);
    val responseMap = Map[String, AnyRef](
      "name" -> "org.ekstep.health.api",
      "healthy" -> Boolean.box(true),
      "checks" -> "Just Testing");
    println(getCassandraHealth())
    println(getRedisHealth())
    println(getNeo4jHealth())
    val response = CommonUtil.OK(request.apiId, responseMap)
    JSONUtils.serialize(response)
  }

  def getCassandraHealth(): Boolean = {
    try {
      val session = CassandraConnector.getSession();
      if (session != null && !session.isClosed) {
        session.execute("SELECT now() FROM system.local")
        true
      } else
        false
    } catch {
      case e: Exception => false
    }
  }

  def getNeo4jHealth(): Boolean = {
    LearningRequestRouterPool.init();
    try {
      val createReq = getRequest("domain", GraphEngineManagers.NODE_MANAGER, "upsertRootNode")
      val response = getResponse(createReq)
      if (checkError(response))
        false
      else
        true
    } catch {
      case e: Exception => false
    }
  }

  def getRedisHealth(): Boolean = {
    try {
      val jedis = JedisFactory.getRedisConncetion();
      jedis.close()
      true
    } catch {
      case e: Exception => false
    }
  }

}
