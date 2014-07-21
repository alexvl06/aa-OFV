package co.com.alianza.app

import spray.routing._
import akka.actor.Props

trait Api extends RouteConcatenation with Core with CoreActors {

  implicit val _ = system.dispatcher
  val rootService = system.actorOf(Props(new AlianzaRouter), name = "api-AlianzaRouter")
}