package org.ekstep.actor

import org.ekstep.actor.core.BaseAPIActor
import org.ekstep.commons.{APIIds, Request}
import org.ekstep.managers.{ContentMgr}
import akka.dispatch.Futures
import akka.pattern.Patterns

object ContentActor extends BaseAPIActor {


  override def onReceive(request: Request) = {

    request.apiId match {
      case APIIds.READ_CONTENT =>
        readContent(request)

      //case APIIds.CREATE_CONTENT =>

      //case APIIds.UPDATE_CONTENT =>

      case _ =>
        invalidAPIResponseSerialized(request.apiId);
    }

  }


  private def readContent(request: Request) = {
    val readContentMgr = new ContentMgr()
    val result = readContentMgr.read(request)

    val response = OK(request.apiId, result)
    Patterns.pipe(Futures.successful(response), getContext().dispatcher).to(sender())
  }


}
