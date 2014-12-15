package co.com.alianza.app

import akka.util.Timeout
import scala.concurrent.duration.DurationInt

trait AlianzaActors {
  
  //Time Out del Request
  implicit val timeout = Timeout(120 seconds)
  
  //Definicion de actores
  val usuariosActor = MainActors.system.actorSelection(MainActors.usuariosActorSupervisor.path+"/usuariosActor")
  val confrontaActor = MainActors.system.actorSelection(MainActors.confrontaActorSupervisor.path + "/confrontaActor")
  val autenticacionActor = MainActors.system.actorSelection(MainActors.autenticacionActorSupervisor.path+"/autenticacionActor" )
  val autorizacionActor = MainActors.system.actorSelection(MainActors.autorizacionActorSupervisor.path+"/autorizacionActor" )
  val consultaClienteActor = MainActors.system.actorSelection(MainActors.consultaClienteActorSupervisor.path+"/consultaClienteActor")
  val contrasenasActor = MainActors.system.actorSelection(MainActors.contrasenasActorSupervisor.path+"/contrasenasActor")
  val ipsUsuarioActor = MainActors.system.actorSelection(MainActors.ipsUsuarioActorSupervisor.path+"/ipsUsuarioActor")
  val pinActor = MainActors.system.actorSelection(MainActors.pinActorSupervisor.path+"/pinActor")
  val sesionActor = MainActors.system.actorSelection(MainActors.sesionActorSupervisor.path+"/sesionActor")

}