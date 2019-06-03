package org.ekstep.actor

import org.ekstep.actor.core.BaseAPIActor
import org.ekstep.commons.{APIIds, Request}
import akka.dispatch.Futures
import akka.pattern.Patterns
import org.ekstep.mgr.impl.ContentManagerImpl

object ContentActor extends BaseAPIActor {


  override def onReceive(request: Request) = {

    request.apiId match {
      case APIIds.READ_CONTENT =>
        readContent(request)

      //case APIIds.CREATE_CONTENT =>

      case APIIds.UPDATE_CONTENT =>
        updateContent(request)

      case _ =>
        invalidAPIResponseSerialized(request.apiId);
    }

  }


  private def readContent(request: Request) = {
    val readContentMgr = new ContentManagerImpl()
    val result = readContentMgr.read(request)

    val response = OK(request.apiId, result)
    Patterns.pipe(Futures.successful(response), getContext().dispatcher).to(sender())
  }

  private def updateContent(request: Request) = {
    val contentMgr = new ContentManagerImpl()
    val result = contentMgr.update(request)

    val response = OK(request.apiId, result)
    Patterns.pipe(Futures.successful(response), getContext().dispatcher).to(sender())
  }


}
