package controllers.health

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.routing.FromConfig
import controllers.BaseController
import javax.inject.{Inject, Singleton}
import org.ekstep.actor.HealthActor
import org.ekstep.common.dto.Response
import org.ekstep.commons.{APIIds, Request}
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class HealthController @Inject()(system: ActorSystem) extends BaseController {
  implicit val className: String = "controllers.health.HealthController"

  val healthActor = system.actorOf(Props(HealthActor).withRouter(FromConfig), name = "healthActor")

  def checkAPIhealth(): Action[AnyContent] = Action.async {
      implicit request =>
        val result = ask(healthActor, Request(APIIds.CHECK_HEALTH,None,None))
          .mapTo[Response]
        result.map(response => sendResponse(response))
    }
}

