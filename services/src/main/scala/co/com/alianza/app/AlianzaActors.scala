package co.com.alianza.app

import akka.util.Timeout
import scala.concurrent.duration.DurationInt

trait AlianzaActors {

  //Time Out del Request
  implicit val timeout = Timeout(120 seconds)

  //Definicion de actores
  val usuariosActor = MainActors.system.actorSelection(MainActors.usuariosActorSupervisor.path + "/usuariosActor")
  val usuariosEmpresarialActor = MainActors.system.actorSelection(MainActors.usuariosEmpresaActorSupervisor.path + "/usuariosEmpresaActor")
  val confrontaActor = MainActors.system.actorSelection(MainActors.confrontaActorSupervisor.path + "/confrontaActor")
  val autenticacionActor = MainActors.system.actorSelection(MainActors.autenticacionActorSupervisor.path + "/autenticacionActor")
  val autenticacionUsuarioEmpresaActor = MainActors.system.actorSelection(MainActors.autenticacionActorSupervisor.path + "/autenticacionUsuarioEmpresaActor")
  val autorizacionActor = MainActors.system.actorSelection(MainActors.autorizacionActorSupervisor.path + "/autorizacionActor")
  val autorizacionUsuarioEmpresarialActor = MainActors.system.actorSelection(MainActors.autorizacionActorSupervisor.path + "/autorizacionUsuarioEmpresarialActor")
  val consultaClienteActor = MainActors.system.actorSelection(MainActors.consultaClienteActorSupervisor.path + "/consultaClienteActor")
  val contrasenasActor = MainActors.system.actorSelection(MainActors.contrasenasActorSupervisor.path + "/contrasenasActor")
  val contrasenasEmpresaActor = MainActors.system.actorSelection(MainActors.contrasenasEmpresaActorSupervisor.path + "/contrasenasEmpresaActor")
  val ipsUsuarioActor = MainActors.system.actorSelection(MainActors.ipsUsuarioActorSupervisor.path + "/ipsUsuarioActor")
  val pinActor = MainActors.system.actorSelection(MainActors.pinActorSupervisor.path + "/pinActor")
  val sesionActor = MainActors.system.actorSelection(MainActors.sesionActorSupervisor.path + "/sesionActor")
  val agenteEmpresarialActor = MainActors.system.actorSelection(MainActors.agenteEmpresarialActorSupervisor.path + "/agenteEmpresarialActor")

}