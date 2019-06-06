package org.ekstep.actor

import akka.dispatch.Futures
import akka.pattern.Patterns
import org.ekstep.actor.core.BaseAPIActor
import org.ekstep.commons.{APIIds, Request}
import org.ekstep.managers.{ContentDialCodeManagerImpl, ContentManager}
import org.ekstep.mgr.impl.ContentManagerImpl

object ContentActor extends BaseAPIActor {

  override def onReceive(request: Request) = {
    request.apiId match {
      case APIIds.CREATE_CONTENT => createContent(request)
      case APIIds.READ_CONTENT => readContent(request)
      case APIIds.UPDATE_CONTENT => updateContent(request)
      case APIIds.REVIEW_CONTENT => reviewContent(request)
      case APIIds.DIALCODE_LINK => linkDialCode(request)
      case APIIds.DIALCODE_COLLECTION_LINK => collectionLinkDialCode(request)
      case APIIds.DIALCODE_RESERVE => reserveDialCode(request)
      case APIIds.DIALCODE_RELEASE => releaseDialCode(request)
      case _ => invalidAPIResponseSerialized(request.apiId);
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

  /**
    * Link DIAL Code to Neo4j Object
    *
    * @param request
    * @return
    */
  private def linkDialCode(request: Request) = {
    try {
      println("contentActor:: start")
      val result = ContentDialCodeManagerImpl.linkDialCode(request)
      //TODO: Review Below Code to make a single method.
      println("result :: "+result)
      val response = setResponseEnvelope(result, request.apiId, null)
      Patterns.pipe(Futures.successful(response), getContext().dispatcher).to(sender())
    } catch {
      case e: Exception =>
        sender().tell(getErrorResponse(e, request.apiId), self)
    }
  }

  /**
    * Link DIAL Code to Collection Objects
    *
    * @param request
    * @return
    */
  private def collectionLinkDialCode(request: Request) = {
    try {
      val result = ContentDialCodeManagerImpl.collectionLinkDialCode(request)
      val response = setResponseEnvelope(result, request.apiId, null)
      Patterns.pipe(Futures.successful(response), getContext().dispatcher).to(sender())
    } catch {
      case e: Exception =>
        sender().tell(getErrorResponse(e, request.apiId), self)
    }
  }

  /**
    * Reserve DIAL Codes for Textbook
    *
    * @param request
    * @return
    */
  private def reserveDialCode(request: Request) = {
    try {
      val result = ContentDialCodeManagerImpl.reserveDialCode(request)
      val response = setResponseEnvelope(result, request.apiId, null)
      Patterns.pipe(Futures.successful(response), getContext().dispatcher).to(sender())
    } catch {
      case e: Exception =>
        sender().tell(getErrorResponse(e, request.apiId), self)
    }
  }

  /**
    * Release DIAL Codes for Textbook
    *
    * @param request
    * @return
    */
  private def releaseDialCode(request: Request) = {
    try {
      val result = ContentDialCodeManagerImpl.releaseDialCode(request)
      val response = setResponseEnvelope(result, request.apiId, null)
      Patterns.pipe(Futures.successful(response), getContext().dispatcher).to(sender())
    } catch {
      case e: Exception =>
        sender().tell(getErrorResponse(e, request.apiId), self)
    }
  }


}
