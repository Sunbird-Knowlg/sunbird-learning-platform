package controllers

import akka.actor.{ActorRef, ActorSystem}
import javax.inject.{Inject, Named}
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext
import managers.PlaySearchManager

class SearchController@Inject()(@Named("SearchManager") actor: ActorRef, cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends BaseController(cc) {

    private val mgr = new PlaySearchManager

    /**
     * Method to search for all indexed data in the platform
     * based on the request
     *
     * @return Future[Result]
     */
    def search() = Action.async { implicit request =>
        val apiId: String = "composite-search.search"
        val internalReq = getRequest(apiId)
        setHeaderContext(internalReq)
        getResult(mgr.search(internalReq, actor))
    }

    /**
     * Method to get the count of indexed data in the platform
     * based on the request
     *
     * @return Future[Result]
     */
    def count() = Action.async { implicit request =>
        val apiId = "composite-search.count"
        val internalReq = getRequest(apiId)
        getResult(mgr.count(internalReq, actor))
    }
}
