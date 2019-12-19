package controllers

import akka.actor.ActorSystem
import javax.inject.Inject
import play.api.mvc.ControllerComponents
import scala.concurrent.ExecutionContext

import managers.PlaySearchManager

class MetricsController@Inject()(cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends BaseController(cc) {

    private val mgr = new PlaySearchManager

    def search() = Action.async { implicit request =>
        val apiId: String = "composite-search.metrics"
        val internalReq = getRequest(apiId)
        getResult(mgr.metrics(internalReq))

    }
}
