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

      case APIIds.UPLOAD_CONTENT =>
        uploadContent(request)

      case APIIds.PUBLIC_PUBLISH_CONTENT =>
        publishContent(request, "public")

      case APIIds.UNLISTED_PUBLISH_CONTENT =>
        publishContent(request, "unlisted")

      case _ =>
        invalidAPIResponseSerialized(request.apiId);
    }

  }

  private def createContent(request: Request) = {
    try {
      val result = ContentManager.create(request)
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
        sender().tell(getErrorResponse(e, APIIds.CREATE_CONTENT), self)
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

  private def uploadContent(request: Request) = {
    val contentMgr = new ContentManagerImpl()
    val fileUrl = request.params.getOrElse("fileUrl","")

    if(fileUrl != None){

      val result = contentMgr.uploadUrl(request)

      val response = OK(request.apiId, result)
      Patterns.pipe(Futures.successful(response), getContext().dispatcher).to(sender())
    }


  }

  private def publishContent(request: Request, publishType: String) = {
    val contentMgr = new ContentManagerImpl()
    val result = contentMgr.publishByType(request, publishType)

    val response = OK(request.apiId, result)
    Patterns.pipe(Futures.successful(response), getContext().dispatcher).to(sender())
  }

}
