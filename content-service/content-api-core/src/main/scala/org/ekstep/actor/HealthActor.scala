package org.ekstep.actor


import akka.dispatch.Futures
import akka.pattern.Patterns
import org.ekstep.actor.core.BaseAPIActor
import org.ekstep.commons.{APIIds, Request}
import org.ekstep.service.HealthCheckService

import scala.collection.mutable


object HealthActor extends BaseAPIActor {
  val ERR_INVALID_REQUEST: String = "ERR_INVALID_REQUEST"


  override def preStart() = {
  }

  override def onReceive(request: Request): Unit = {
    val result = request.apiId match {
      case APIIds.CHECK_HEALTH =>
        checkHealth(request)
      case _ =>
        invalidAPIResponseSerialized(request.apiId);
    }
  }


  def checkHealth(request: Request) = {
    val responseMap = HealthCheckService.checkSystemHealth(Request(APIIds.CHECK_HEALTH, None, None, Some(mutable.Map())))
    val result = new org.ekstep.common.dto.Response() {
      put("result", responseMap)
    }
    val response = OK(request.apiId,result)
    Patterns.pipe(Futures.successful(response),getContext().dispatcher).to(sender())
  }
}