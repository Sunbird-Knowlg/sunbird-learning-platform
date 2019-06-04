package config

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.routing.FromConfig
import org.ekstep.actor.{ContentActor, HealthActor}


object RequestRouter {

   var actorMap = Map[String, ActorRef]()

    def initActors(system: ActorSystem) = {
        actorMap = actorMap + ("contentActor" -> system.actorOf(Props(ContentActor).withRouter(FromConfig), name = "contentActor"))
        actorMap = actorMap + ("healthActor" -> system.actorOf(Props(HealthActor).withRouter(FromConfig), name = "healthActor"))
        println("Actors initialsed")
        println("Content Actor is  : " + actorMap.get("contentActor"))
        println("Health Actor is  : " + actorMap.get("healthActor").get)
    }

    def getActorRef(name: String) : ActorRef = {
        println("Fetching actor for " + name + " : " + actorMap.get(name))
        actorMap.get(name).get
    }

}
