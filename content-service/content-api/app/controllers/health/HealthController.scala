package controllers.health

import akka.actor.{ActorSystem, Props}
import play.api.mvc.{Action, AnyContent}
import controllers.BaseController
import javax.inject.{Inject, Singleton}
import org.ekstep.commons.{APIIds, Request}
import org.ekstep.service.HealthCheckService
@Singleton
class HealthController @Inject()(system: ActorSystem) extends BaseController {
  implicit val className: String = "controllers.health.HealthController"
  val healthService = HealthCheckService

  def checkAPIhealth() = Action {request =>
    val result = healthService.checkSystemHealth(Request(APIIds.CHECK_HEALTH,None,None))
      Ok(result).withHeaders(CONTENT_TYPE -> "application/json");
  }
}
