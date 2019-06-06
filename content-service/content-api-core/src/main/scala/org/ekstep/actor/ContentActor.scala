package org.ekstep.actor

import org.ekstep.actor.core.BaseAPIActor
import org.ekstep.commons.{APIIds, Request}
import akka.dispatch.Futures
import akka.pattern.Patterns
import org.ekstep.managers.ContentManager
import org.ekstep.mgr.impl.ContentManagerImpl

object ContentActor extends BaseAPIActor {

  override def onReceive(request: Request) = {

    request.apiId match {
      case APIIds.CREATE_CONTENT =>
        createContent(request)

      case APIIds.READ_CONTENT =>
        readContent(request)

      case APIIds.UPDATE_CONTENT =>
        updateContent(request)

      case APIIds.REVIEW_CONTENT =>
        reviewContent(request)

      case _ =>
        invalidAPIResponseSerialized(request.apiId);
    }

  }

  private def createContent(request: Request) = {
    try {
      val newContentMgr = new ContentManager()
      val result = newContentMgr.create(request)
      val response = setResponseEnvelope(result, request.apiId, null)
      Patterns.pipe(Futures.successful(response), getContext().dispatcher).to(sender())
    } catch {
      case e: Exception =>
        sender().tell(getErrorResponse(e, request.apiId), self)
    }
  }


  private def readContent(request: Request) = {
    try{
      val contentMgr = new ContentManagerImpl()
      val result = contentMgr.read(request)
      val response = setResponseEnvelope(result, request.apiId, null)
      Patterns.pipe(Futures.successful(response), getContext().dispatcher).to(sender())
    } catch {
      case e: Exception =>
        sender().tell(getErrorResponse(e, request.apiId), self)
    }
  }

  private def updateContent(request: Request) = {
    try{
      val contentMgr = new ContentManagerImpl()
    val result = contentMgr.update(request)
    val response = setResponseEnvelope(result, request.apiId, null)
    Patterns.pipe(Futures.successful(response), getContext().dispatcher).to(sender())
    } catch {
      case e: Exception =>
        sender().tell(getErrorResponse(e, request.apiId), self)
    }
  }

  private def reviewContent(request: Request) = {
    try{
      val contentMgr = new ContentManagerImpl()
      val result = contentMgr.review(request)
      val response = setResponseEnvelope(result, request.apiId, null)
      Patterns.pipe(Futures.successful(response), getContext().dispatcher).to(sender())
    } catch {
      case e: Exception =>
        sender().tell(getErrorResponse(e, request.apiId), self)
    }

  }


}
