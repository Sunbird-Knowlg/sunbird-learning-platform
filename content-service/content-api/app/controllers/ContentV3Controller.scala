package controllers

import akka.actor.{ActorSystem, Props}
import akka.pattern._
import akka.routing.FromConfig
import javax.inject.{Inject, Singleton}
import org.ekstep.actor.ContentActor
import org.ekstep.common.dto.Response
import org.ekstep.commons.{APIIds, Request}
import org.ekstep.content.util.JSONUtils
import play.api.mvc.{Action, AnyContent}
import play.api.libs.json.Json

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global


@Singleton
class ContentV3Controller @Inject()(system: ActorSystem) extends BaseController {

  val contentActor = system.actorOf(Props(ContentActor).withRouter(FromConfig), name = "contentActor")

  def create() = Action.async {
    implicit request =>
      //val body: String = JSONUtils.serialize(request.body.asJson.getOrElse(""))
      val body: String = Json.stringify(request.body.asJson.get)
      val result = ask(contentActor, Request(APIIds.CREATE_CONTENT, Some(body), Some(Map()), Some(getContext(request))))
        .mapTo[Response]
      result.map(response => sendResponse(response))
  }

  def read(identifier: String, mode: Option[String], fields: Option[List[String]]): Action[AnyContent] = Action.async {
    implicit request =>
      val result = ask(contentActor, Request(APIIds.READ_CONTENT, None, Some(Map("identifier" -> identifier, "objectType" ->
              "Content", "mode" -> mode.getOrElse(""), "fields" -> fields.getOrElse(List()))), Some(mutable.Map())))
        .mapTo[Response]
      result.map(response => sendResponse(response))
  }

  def update(identifier: String): Action[AnyContent] = Action.async {
    implicit request =>
      val body: String = Json.stringify(request.body.asJson.get)
      val result = ask(contentActor, Request(APIIds.UPDATE_CONTENT, Some(body), Some(Map("identifier" -> identifier, "objectType" ->
        "Content")), Some(getContext(request))))
        .mapTo[Response]
      result.map(response => sendResponse(response))
  }

  def review(identifier: String): Action[AnyContent] = Action.async {
    implicit request =>
      val result = ask(contentActor, Request(APIIds.REVIEW_CONTENT, None, Some(Map("identifier" -> identifier, "objectType" ->
        "Content")), Some(getContext(request))))
        .mapTo[Response]
      result.map(response => sendResponse(response))
  }

  def upload(identifier: String, fileUrl: Option[String]): Action[AnyContent] = Action.async {
    implicit request =>
      val result = ask(contentActor, Request(APIIds.UPLOAD_CONTENT, None, Some(Map("identifier" -> identifier, "objectType" ->
        "Content", "fileUrl" -> fileUrl.getOrElse(""))), Some(getContext(request))))
        .mapTo[Response]
      result.map(response => sendResponse(response))
  }

  def publish(identifier: String): Action[AnyContent] = Action.async {
    implicit request =>
      val body: String = Json.stringify(request.body.asJson.get)
      val result = ask(contentActor, Request(APIIds.PUBLISH_PUBLIC_CONTENT, Some(body), Some(Map("identifier" -> identifier, "objectType" ->
        "Content")), Some(getContext(request))))
        .mapTo[Response]
      result.map(response => sendResponse(response))
  }

  def publishUnlisted(identifier: String): Action[AnyContent] = Action.async {
    implicit request =>
      val body: String = Json.stringify(request.body.asJson.get)
      val result = ask(contentActor, Request(APIIds.PUBLISH_UNLISTED_CONTENT, Some(body), Some(Map("identifier" -> identifier, "objectType" ->
        "Content")), Some(getContext(request))))
        .mapTo[Response]
      result.map(response => sendResponse(response))
  }

}

