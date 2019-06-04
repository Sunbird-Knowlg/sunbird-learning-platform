package controllers

import akka.pattern.ask
import config.RequestRouter
import org.ekstep.common.dto.Response
import org.ekstep.commons.{APIIds, Request}
import play.api.mvc.{Action, AnyContent}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global

class HealthController extends BaseController {
  implicit val className: String = "controllers.health.HealthController"

  def checkAPIhealth(): Action[AnyContent] = Action.async {
      implicit request =>
        val result = ask(RequestRouter.getActorRef("healthActor"), Request(APIIds.CHECK_HEALTH, None, None, Some(mutable.Map())))
          .mapTo[Response]
        result.map(response => sendResponse(response))
    }
}

