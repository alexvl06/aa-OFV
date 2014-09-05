package co.com.alianza.domain.aggregates.ips

import akka.actor.{Actor, ActorLogging}
import co.com.alianza.app.AlianzaActors
import co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
import co.com.alianza.infrastructure.messages._
import co.com.alianza.persistence.entities.IpsUsuario
import spray.http.StatusCodes._

import scalaz.{Failure => zFailure, Success => zSuccess}
import scala.util.{Success, Failure}


import akka.actor.Props
import akka.routing.RoundRobinPool




class IpsUsuarioActorSupervisor extends Actor with ActorLogging {
  import akka.actor.SupervisorStrategy._
  import akka.actor.OneForOneStrategy

  val ipsUsuarioActor = context.actorOf(Props[IpsUsuarioActor].withRouter(RoundRobinPool(nrOfInstances = 2)), "ipsUsuarioActor")

  def receive = {

    case message: Any =>
      ipsUsuarioActor forward message

  }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }

}
/**
 * Created by david on 16/06/14.
 */

class IpsUsuarioActor extends Actor with ActorLogging with AlianzaActors {
  import scala.concurrent.ExecutionContext
  implicit val _: ExecutionContext = context.dispatcher
  import co.com.alianza.util.json.MarshallableImplicits._

  def receive = {
    case message: ObtenerIpsUsuarioMessage  => obtenerIpsUsuario(message.idUsuario)
    case message: AgregarIpsUsuarioMessage => agregarIpsUsuarioMessage(message.toEntityIpsUsuario)
    case message: EliminarIpsUsuarioMessage => eliminarIpsUsuarioMessage(message.toEntityIpsUsuario)
  }

  def obtenerIpsUsuario(idUsuario : Int) = {
    val currentSender = sender()
    val result = DataAccessAdapter.obtenerIpsUsuario(idUsuario)

    result  onComplete {
      case Failure(failure)  =>    currentSender ! failure
      case Success(value)    =>
        value match {
          case zSuccess(response: Vector[IpsUsuario]) =>
            currentSender !  ResponseMessage(OK, response.toJson)
          case zFailure(error)                 =>  currentSender !  error
        }
    }
  }

  def agregarIpsUsuarioMessage(ipUsuario : IpsUsuario ) = {
    val currentSender = sender()
    val result = DataAccessAdapter.agregarIpUsuario(ipUsuario)
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

  def eliminarIpsUsuarioMessage(ipUsuario : IpsUsuario ) = {
    val currentSender = sender()
    val result = DataAccessAdapter.eliminarIpUsuario(ipUsuario)
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

}
