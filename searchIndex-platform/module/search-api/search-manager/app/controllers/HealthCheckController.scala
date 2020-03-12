package controllers

import akka.actor.{ActorRef, ActorSystem}
import javax.inject.{Inject, Named}
import play.api.mvc.ControllerComponents
import managers.PlaySearchManager

import scala.concurrent.ExecutionContext

class HealthCheckController@Inject()(@Named("HealthCheckManager") actor: ActorRef, cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends BaseController(cc) {

    val mgr = new PlaySearchManager

    def health() = Action.async { implicit request =>
        val apiId = "search-service.health"
        val internalReq = getRequest(apiId)
        getResult(mgr.health(internalReq, actor))
    }
}
