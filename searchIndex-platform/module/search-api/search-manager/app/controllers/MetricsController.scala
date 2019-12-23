package controllers

import akka.actor.{ActorRef, ActorSystem}
import javax.inject.{Inject, Named}
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext
import managers.PlaySearchManager

class MetricsController@Inject()(@Named("SearchManager") actor: ActorRef, cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends BaseController(cc) {

    private val mgr = new PlaySearchManager

    def search() = Action.async { implicit request =>
        val apiId: String = "composite-search.metrics"
        val internalReq = getRequest(apiId)
        getResult(mgr.metrics(internalReq, actor))

    }
}
