package controllers

import akka.pattern.ask
import config.RequestRouter
import org.ekstep.common.dto.Response
import org.ekstep.commons.{APIIds, Request}
import org.ekstep.content.util.JSONUtils
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.ExecutionContext.Implicits.global


class ContentV3Controller extends BaseController {

    def create():Action[AnyContent] = Action.async {
        implicit request =>
            val body: String = Json.stringify(request.body.asJson.get)//JSONUtils.serialize(request.body.asJson.getOrElse(""))
            val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.CREATE_CONTENT, Some(body), Some(Map()), Some(getContext(request))))
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

    def upload(identifier: String, fileUrl:Option[String]): Action[AnyContent] = Action.async {
        implicit request =>
            val body: String = Json.stringify(request.body.asJson.get)
            val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.UPLOAD_CONTENT, Some(body), Some(Map("identifier" -> identifier, "objectType" ->
                    "Content", "fileUrl" -> fileUrl.getOrElse(""))), Some(getContext(request))))
                    .mapTo[Response]
            result.map(response => sendResponse(response))
    }

    def preSignedUrl(identifier: String): Action[AnyContent] = Action.async {
        implicit request =>
            val body: String = Json.stringify(request.body.asJson.get)
            val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.GET_PRESIGNED_URL, Some(body), Some(Map()), Some(getContext(request))))
                    .mapTo[Response]
            result.map(response => sendResponse(response))
    }

    def bundle(): Action[AnyContent] = Action.async {
        implicit request =>
            val body: String = Json.stringify(request.body.asJson.get)
            val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.BUNDLE_CONTENT, Some(body), Some(Map()), Some(getContext(request))))
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

    def publish(identifier: String): Action[AnyContent] = Action.async {
        implicit request =>
            val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.PUBLIC_PUBLISH_CONTENT, None, Some(Map("identifier" -> identifier, "objectType" ->
                    "Content")), Some(getContext(request))))
                    .mapTo[Response]
            result.map(response => sendResponse(response))
    }

    def unlistedPublish(identifier: String): Action[AnyContent] = Action.async {
        implicit request =>
            val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.UNLISTED_PUBLISH_CONTENT, None, Some(Map("identifier" -> identifier, "objectType" ->
                    "Content")), Some(getContext(request))))
                    .mapTo[Response]
            result.map(response => sendResponse(response))
    }

    def updateHierarchy() = Action.async {
        implicit request =>
            val body: String = JSONUtils.serialize(request.body.asJson.getOrElse(""))
            val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.UPDATE_HIERARCHY, Some(body), Some(Map()), Some(getContext(request))))
                    .mapTo[Response]
            result.map(response => sendResponse(response))
    }

    def copy(identifier: String): Action[AnyContent] = Action.async {
        implicit request =>
            val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.COPY_CONTENT, None, Some(Map("identifier" -> identifier, "objectType" ->
                    "Content")), Some(getContext(request))))
                    .mapTo[Response]
            result.map(response => sendResponse(response))
    }

    def retire(identifier: String): Action[AnyContent] = Action.async {
        implicit request =>
            val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.RETIRE_CONTENT, None, Some(Map("identifier" -> identifier, "objectType" ->
                    "Content")), Some(getContext(request))))
                    .mapTo[Response]
            result.map(response => sendResponse(response))
    }

    //TODO: Update the api id, while migrating from orchestrator
    def flag(identifier: String): Action[AnyContent] = Action.async {
        implicit request =>
            val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.ACCEPT_FLAG_CONTENT, None, Some(Map("identifier" -> identifier, "objectType" ->
                    "Content")), Some(getContext(request))))
                    .mapTo[Response]
            result.map(response => sendResponse(response))
    }

    def acceptFlag(identifier: String): Action[AnyContent] = Action.async {
        implicit request =>
            val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.ACCEPT_FLAG_CONTENT, None, Some(Map("identifier" -> identifier, "objectType" ->
                    "Content")), Some(getContext(request))))
                    .mapTo[Response]
            result.map(response => sendResponse(response))
    }

    def linkDialCode(): Action[AnyContent] = Action.async {
        implicit request =>
            val body: String = Json.stringify(request.body.asJson.get)
            val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.DIALCODE_LINK, Some(body), Some(Map()), Some(getContext(request))))
                    .mapTo[Response]
            result.map(response => sendResponse(response))
    }

    def collectionLinkDialCode(identifier: String): Action[AnyContent] = Action.async {
        implicit request =>
            val body: String = Json.stringify(request.body.asJson.get)
            val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.DIALCODE_COLLECTION_LINK, Some(body), Some(Map()), Some(getContext(request))))
                    .mapTo[Response]
            result.map(response => sendResponse(response))
    }

    def reserveDialCode(identifier: String): Action[AnyContent] = Action.async {
        implicit request =>
            val body: String = Json.stringify(request.body.asJson.get)
            val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.DIALCODE_RESERVE, Some(body), Some(Map()), Some(getContext(request))))
                    .mapTo[Response]
            result.map(response => sendResponse(response))
    }

    def releaseDialCode(identifier: String): Action[AnyContent] = Action.async {
        implicit request =>
            val body: String = Json.stringify(request.body.asJson.get)
            val result = ask(RequestRouter.getActorRef("contentActor"), Request(APIIds.DIALCODE_RELEASE, Some(body), Some(Map()), Some(getContext(request))))
                    .mapTo[Response]
            result.map(response => sendResponse(response))
    }
}

