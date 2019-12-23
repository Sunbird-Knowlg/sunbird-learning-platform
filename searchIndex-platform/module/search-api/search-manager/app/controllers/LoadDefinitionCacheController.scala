package controllers

import akka.actor.{ActorRef, ActorSystem}
import javax.inject.{Inject, Named}
import play.api.mvc.ControllerComponents

import scala.concurrent.{ExecutionContext, Future}
import managers.PlaySearchManager

class LoadDefinitionCacheController@Inject()(@Named("SearchManager") actor: ActorRef, cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends BaseController(cc)  {

    private val mgr = new PlaySearchManager

    def loadDefinitionCache() = Action.async { implicit request =>
        val apiId = "composite-search.loadDefinitionCache"
        val internalReq = getRequest(apiId)
        val response = mgr.callResyncDefinition(internalReq, actor)
        getResult(Future(response))
    }
}
