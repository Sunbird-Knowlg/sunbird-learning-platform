package controllers

import akka.pattern._
import config.RequestRouter
import org.ekstep.common.dto.Response
import org.ekstep.commons.{APIIds, Request}
import org.ekstep.content.util.JSONUtils
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global


class ContentV3Controller extends BaseController {

  def create() = Action.async {
    implicit request =>
      val body: String = Json.stringify(request.body.asJson.get)//JSONUtils.serialize(request.body.asJson.getOrElse(""))
      val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.CREATE_CONTENT, Some(body), Some(Map()), Some(getContext(request))))
        .mapTo[Response]
      result.map(response => sendResponse(response))
  }

  def read(identifier: String, mode: Option[String], fields: Option[List[String]]): Action[AnyContent] = Action.async {
    implicit request =>
      val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.READ_CONTENT, None, Some(Map("identifier" -> identifier, "objectType" ->
              "Content", "mode" -> mode.getOrElse(""), "fields" -> fields.getOrElse(List()))), Some(mutable.Map())))
        .mapTo[Response]
      result.map(response => sendResponse(response))
  }

  def update(identifier: String): Action[AnyContent] = Action.async {
    implicit request =>
      val body: String = Json.stringify(request.body.asJson.get)
      val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.UPDATE_CONTENT, Some(body), Some(Map("identifier" -> identifier, "objectType" ->
        "Content")), Some(getContext(request))))
        .mapTo[Response]
      result.map(response => sendResponse(response))
  }

  def review(identifier: String): Action[AnyContent] = Action.async {
    implicit request =>
      val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.REVIEW_CONTENT, None, Some(Map("identifier" -> identifier, "objectType" ->
        "Content")), Some(getContext(request))))
        .mapTo[Response]
      result.map(response => sendResponse(response))
  }

  def upload(identifier: String, fileUrl: Option[String]): Action[AnyContent] = Action.async {
    implicit request =>
      val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.UPLOAD_CONTENT, None, Some(Map("identifier" -> identifier, "objectType" ->
        "Content", "fileUrl" -> fileUrl.getOrElse(""))), Some(getContext(request))))
        .mapTo[Response]
      result.map(response => sendResponse(response))
  }

  def publish(identifier: String): Action[AnyContent] = Action.async {
    implicit request =>
      val body: String = Json.stringify(request.body.asJson.get)
      val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.PUBLISH_PUBLIC_CONTENT, Some(body), Some(Map("identifier" -> identifier, "objectType" ->
        "Content")), Some(getContext(request))))
        .mapTo[Response]
      result.map(response => sendResponse(response))
  }

  def publishUnlisted(identifier: String): Action[AnyContent] = Action.async {
    implicit request =>
      val body: String = Json.stringify(request.body.asJson.get)
      val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.PUBLISH_UNLISTED_CONTENT, Some(body), Some(Map("identifier" -> identifier, "objectType" ->
        "Content")), Some(getContext(request))))
        .mapTo[Response]
      result.map(response => sendResponse(response))
  }

}

