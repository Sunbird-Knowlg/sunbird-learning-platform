package org.ekstep.actor

import org.ekstep.actor.ContentActor.{retireContent, sender}
import org.ekstep.actor.core.BaseAPIActor
import org.ekstep.common.dto.Response
import org.ekstep.commons.{APIIds, Request}
import org.ekstep.managers.ContentManager
import org.ekstep.mgr.impl.ContentManagerImpl

object ContentActor extends BaseAPIActor {

  override def onReceive(request: Request) = {

    request.apiId match {
      case APIIds.CREATE_CONTENT =>
        sender() ! createContent(request)

      case APIIds.READ_CONTENT =>
        sender() ! readContent(request)

      case APIIds.UPDATE_CONTENT =>
        sender() ! updateContent(request)

      case APIIds.REVIEW_CONTENT =>
        sender() ! reviewContent(request)

      case APIIds.UPLOAD_CONTENT =>
        sender() ! uploadContent(request)

      case APIIds.PUBLIC_PUBLISH_CONTENT =>
        sender() ! publishContent(request, "public")

      case APIIds.UNLISTED_PUBLISH_CONTENT =>
        sender() ! publishContent(request, "unlisted")

      case APIIds.RETIRE_CONTENT =>
        sender() ! retireContent(request)

      case APIIds.ACCEPT_FLAG_CONTENT =>
        sender() ! acceptFlagContent(request)

      case _ =>
        invalidAPIResponseSerialized(request.apiId);
    }

  }

  private def createContent(request: Request) : Response = {
    try {
        val result = ContentManager.create(request)
        OK(request.apiId, result)
    } catch {
      case e: Exception =>
        getErrorResponse(e, APIIds.CREATE_CONTENT)
    }
  }


  private def readContent(request: Request) : Response  = {
    try{
      val contentMgr = new ContentManagerImpl()
      val result = contentMgr.read(request)
      //val response =
      OK(request.apiId, result)
    } catch {
      case e: Exception =>
        getErrorResponse(e, APIIds.READ_CONTENT)
    }
  }

  private def updateContent(request: Request) : Response  = {
    try{
      val contentMgr = new ContentManagerImpl()
      val result = contentMgr.update(request)
      OK(request.apiId, result)
    } catch {
      case e: Exception => getErrorResponse(e, APIIds.UPDATE_CONTENT)
    }
  }

  private def reviewContent(request: Request) : Response  = {
    try{
      val result = ContentManager.review(request)
      OK(request.apiId, result)
    } catch {
      case e: Exception => getErrorResponse(e, APIIds.REVIEW_CONTENT)
    }

  }

  private def retireContent(request: Request) : Response  = {
    try{
      val result = ContentManager.retire(request)
      OK(request.apiId, result)
    } catch {
      case e: Exception => getErrorResponse(e, APIIds.REVIEW_CONTENT)
    }

  }

  private def acceptFlagContent(request: Request) : Response  = {
    try{
      val result = ContentManager.acceptFlag(request)
      OK(request.apiId, result)
    } catch {
      case e: Exception => getErrorResponse(e, APIIds.REVIEW_CONTENT)
    }

  }

  private def publishContent(request: Request, publishType: String) : Response  = {
    val apiId = request.apiId
    try {
      val result = ContentManager.publishByType(request, publishType)
      OK(request.apiId, result)
    } catch {
      case e: Exception => getErrorResponse(e, apiId)
    }
  }

  private def uploadContent(request: Request) : Response  = {
    val contentMgr = new ContentManagerImpl()
    val fileUrl = request.params.getOrElse("fileUrl","")
    val result = contentMgr.uploadUrl(request)
    OK(request.apiId, result)
  }

}
