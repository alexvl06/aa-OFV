package co.com.alianza.domain.aggregates.ips

import akka.actor.{Actor, ActorRef, ActorLogging, Props, OneForOneStrategy}
import akka.actor.SupervisorStrategy._
import akka.routing.RoundRobinPool
import akka.pattern._
import co.com.alianza.app.AlianzaActors
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.actualizacion.DataAccessAdapter
import co.com.alianza.infrastructure.dto.Pais
import co.com.alianza.infrastructure.messages.ActualizacionMessagesJsonSupport._
import co.com.alianza.infrastructure.messages._
import co.com.alianza.util.transformers.ValidationT
import co.com.alianza.app.MainActors
import co.com.alianza.domain.aggregates.autenticacion.{RemoverIp, AgregarIp}
import co.com.alianza.exceptions.BusinessLevel
import spray.http.StatusCodes._


import scala.util.{Failure, Success}
import scala.concurrent.Future
import scalaz.{Failure => zFailure, Success => zSuccess, Validation}
import scalaz.std.AllInstances._


class ActualizacionActorSupervisor extends Actor with ActorLogging {

  val actualizacionActor = context.actorOf(Props[ActualizacionActor], "actualizacionActor")

  def receive = {
    case message: Any => actualizacionActor forward message
  }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }

}

class ActualizacionActor extends Actor with ActorLogging with AlianzaActors {
  import scala.concurrent.ExecutionContext
  implicit val _: ExecutionContext = context.dispatcher
  import co.com.alianza.util.json.MarshallableImplicits._

  def receive = {
    case message: ObtenerPaises  => obtenerPaises
    case message: ObtenerCiudades  => obtenerCiudades
    case _ => println("no encontro mensaje")
  }

  def obtenerPaises = {
    val currentSender = sender()
    val paises = DataAccessAdapter.consultaPaises
    paises onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response) => currentSender !  ResponseMessage(OK, response.toJson)
          case zFailure(error) =>  currentSender !  error
        }
    }
    //currentSender ! ResponseMessage(OK, paises)
  }

  def obtenerCiudades = {
    val currentSender = sender()
    currentSender ! ResponseMessage(OK, "soy una lista de ciudades")
  }

/*
  def agregarActualizacionMessage(idUsuario : Int, ip : String, tipoCliente: TiposCliente) = {
    val currentSender = sender()
    val result = {
      if (tipoCliente.equals(TiposCliente.clienteIndividual))
        DataAccessAdapter.agregarIpUsuario(new Actualizacion(idUsuario, ip))
      else
        (for {
          idEmpresa <- ValidationT(DataAccessAdapter.obtenerIdEmpresa(idUsuario, tipoCliente))
          mensaje <- ValidationT(DataAccessAdapter.agregarIpEmpresa(new IpsEmpresa(idEmpresa, ip)))
          agregarIpSesion <- ValidationT(agregarIpSesionEmpresa(idEmpresa, ip))
        } yield (mensaje)).run
    }
    result  onComplete {
      case Failure(failure)  =>    currentSender ! failure
      case Success(value)    =>
        value match {
          case zSuccess(response: String) =>
            currentSender !  ResponseMessage(OK, response.toJson)
          case zFailure(error)                 =>  currentSender !  error
        }
    }
  }

  def eliminarActualizacionMessage(idUsuario : Int, ip : String, tipoCliente: TiposCliente) = {
    val currentSender = sender()
    val result = {
      if (tipoCliente.equals(TiposCliente.clienteIndividual))
        DataAccessAdapter.eliminarIpUsuario(new Actualizacion(idUsuario, ip))
      else
        (for {
          idEmpresa <- ValidationT(DataAccessAdapter.obtenerIdEmpresa(idUsuario, tipoCliente))
          mensaje <- ValidationT(DataAccessAdapter.eliminarIpEmpresa(new IpsEmpresa(idEmpresa, ip)))
          removerIpSesion <- ValidationT(removerIpSesionEmpresa(idEmpresa, ip))
        } yield (mensaje)).run
    }
    result  onComplete {
      case Failure(failure)  =>    currentSender ! failure
      case Success(value)    =>
        value match {
          case zSuccess(response: Int) =>

            currentSender !  ResponseMessage(OK, response.toJson)
          case zFailure(error)                 =>  currentSender !  error
        }
    }
  }

  private def agregarIpSesionEmpresa(empresaId: Int, ip: String) =
    MainActors.sesionActorSupervisor ? ObtenerEmpresaSesionActorId (empresaId) map {
      case Some(empresaSesionActor: ActorRef) => empresaSesionActor ! AgregarIp(ip); zSuccess(():Unit)
      case _ => zFailure(PersistenceException(new Exception(),BusinessLevel,"Error"))
    }

  private def removerIpSesionEmpresa(empresaId: Int, ip: String) =
    MainActors.sesionActorSupervisor ? ObtenerEmpresaSesionActorId (empresaId) map {
      case Some(empresaSesionActor: ActorRef) => empresaSesionActor ! RemoverIp(ip); zSuccess(():Unit)
      case None => zSuccess(():Unit)
      case _ => zFailure(PersistenceException(new Exception(),BusinessLevel,"Error"))
    }
*/
}