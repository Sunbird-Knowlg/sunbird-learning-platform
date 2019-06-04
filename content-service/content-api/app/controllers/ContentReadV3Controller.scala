package controllers

import akka.pattern.ask
import config.RequestRouter
import org.ekstep.common.dto.Response
import org.ekstep.commons.{APIIds, Request}
import play.api.mvc.{Action, AnyContent}
//TODO: Check the execution context.
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global

class ContentReadV3Controller extends BaseController {

    def read(identifier: String, mode: Option[String], fields: Option[List[String]]): Action[AnyContent] = Action.async {
        implicit request =>
            val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.READ_CONTENT, None, Some(Map("identifier" -> identifier, "objectType" ->
                    "Content", "mode" -> mode.getOrElse(""), "fields" -> fields.getOrElse(List()))), Some(mutable.Map())))
                    .mapTo[Response]
            result.map(response => sendResponse(response)
    }

    def readHierarchy(identifier: String, mode: Option[String], fields: Option[List[String]]): Action[AnyContent] = Action.async {
        implicit request =>
            val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.READ_HIERACHY, None, Some(Map("identifier" -> identifier, "objectType" ->
                    "Content", "mode" -> mode.getOrElse(""), "fields" -> fields.getOrElse(List()))), Some(mutable.Map())))
                    .mapTo[Response]
            result.map(response => sendResponse(response))
    }

    def readHierarchyWithBookmarkId(rootId: String, bookmarkId: String, mode: Option[String], fields: Option[List[String]]): Action[AnyContent] = Action.async {
        implicit request =>
            val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.READ_HIERACHY_WITH_BOOKMARK, None, Some(Map("identifier" -> rootId, "objectType" ->
                    "Content", "mode" -> mode.getOrElse(""), "fields" -> fields.getOrElse(List()))), Some(mutable.Map())))
                    .mapTo[Response]
            result.map(response => sendResponse(response))
    }
}
