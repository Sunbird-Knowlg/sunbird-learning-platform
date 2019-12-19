package controllers

import akka.actor.ActorSystem
import javax.inject.Inject
import play.api.mvc.ControllerComponents

import managers.PlaySearchManager
import scala.concurrent.ExecutionContext

class HealthCheckController@Inject()(cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends BaseController(cc) {

    val mgr = new PlaySearchManager

    def health() = Action.async { implicit request =>
        val apiId = "search-service.health"
        val internalReq = getRequest(apiId)
        getResult(mgr.health(internalReq))
    }
}
