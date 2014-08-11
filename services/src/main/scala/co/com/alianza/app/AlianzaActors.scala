package co.com.alianza.app

import akka.util.Timeout
import scala.concurrent.duration.DurationInt

trait AlianzaActors {
  
  //Time Out del Request
  implicit val timeout = Timeout(120 seconds)
  
  //Definicion de actores
  val usuariosActor = MainActors.system.actorSelection(MainActors.usuariosActor.path)
  val confrontaActor = MainActors.system.actorSelection(MainActors.confrontaActorSupervisor.path + "/confrontaActor")
  val autenticacionActor = MainActors.system.actorSelection(MainActors.autenticacionActor.path )
  val autorizacionActor = MainActors.system.actorSelection(MainActors.autenticacionActor.path )
	
  val consultaClienteActor = MainActors.system.actorSelection(MainActors.consultaClienteActor.path)
  val contrasenasActor = MainActors.system.actorSelection(MainActors.contrasenasActor.path)
  val ipsUsuarioActor = MainActors.system.actorSelection(MainActors.ipsUsuarioActor.path)

}