package co.com.alianza.domain.aggregates.ips

import akka.actor.{Actor, ActorLogging, Props, OneForOneStrategy}
import akka.actor.SupervisorStrategy._
import akka.routing.RoundRobinPool
import co.com.alianza.app.AlianzaActors
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
import co.com.alianza.infrastructure.messages._
import co.com.alianza.persistence.entities.{IpsEmpresa, Empresa, IpsUsuario}
import co.com.alianza.util.transformers.ValidationT
import spray.http.StatusCodes._

import scala.util.{Failure, Success}
import scala.concurrent.Future
import scalaz.{Failure => zFailure, Success => zSuccess, Validation}
import scalaz.std.AllInstances._


class IpsUsuarioActorSupervisor extends Actor with ActorLogging {

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
    case message: ObtenerIpsUsuarioMessage  => obtenerIpsUsuario(message.idUsuario, message.tipoCliente)
    case message: AgregarIpsUsuarioMessage  => agregarIpsUsuarioMessage(message.idUsuario.get, message.ip, obtenerEnumTipoCliente(message.tipoCliente))
    case message: EliminarIpsUsuarioMessage => eliminarIpsUsuarioMessage(message.idUsuario.get, message.ip, obtenerEnumTipoCliente(message.tipoCliente))
  }

  def obtenerEnumTipoCliente(tipoCliente: Option[Int]) = {
    TiposCliente.apply(tipoCliente.get)
  }

  def obtenerIpsUsuario(idUsuario : Int, tipoCliente: TiposCliente) = {
    val currentSender = sender()
    val result = {
      if (tipoCliente.equals(TiposCliente.clienteIndividual))
        DataAccessAdapter.obtenerIpsUsuario(idUsuario)
      else{
        (for {
            idEmpresa <- ValidationT(DataAccessAdapter.obtenerIdEmpresa(idUsuario, tipoCliente))
            vectorIpsEmpresa <- ValidationT(DataAccessAdapter.obtenerIpsEmpresa(idEmpresa))
        } yield (vectorIpsEmpresa)).run
      }
    }
    result  onComplete {
      case Failure(failure)  =>
        currentSender ! failure
      case Success(value)    =>
        value match {
          case zSuccess(response: Vector[IpsUsuario]) =>
              currentSender !  ResponseMessage(OK, response.toJson)
          case zSuccess(response: Vector[IpsEmpresa]) =>
              currentSender !  ResponseMessage(OK, response.toJson)
          case zFailure(error)   =>
            currentSender !  error
        }
    }
  }

  def agregarIpsUsuarioMessage(idUsuario : Int, ip : String, tipoCliente: TiposCliente) = {
    val currentSender = sender()
    val result = {
      if (tipoCliente.equals(TiposCliente.clienteIndividual))
        DataAccessAdapter.agregarIpUsuario(new IpsUsuario(idUsuario, ip))
      else
        (for {
          idEmpresa <- ValidationT(DataAccessAdapter.obtenerIdEmpresa(idUsuario, tipoCliente))
          mensaje <- ValidationT(DataAccessAdapter.agregarIpEmpresa(new IpsEmpresa(idEmpresa, ip)))
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

  def eliminarIpsUsuarioMessage(idUsuario : Int, ip : String, tipoCliente: TiposCliente) = {
    val currentSender = sender()
    val result = {
      if (tipoCliente.equals(TiposCliente.clienteIndividual))
        DataAccessAdapter.eliminarIpUsuario(new IpsUsuario(idUsuario, ip))
      else
        (for {
          idEmpresa <- ValidationT(DataAccessAdapter.obtenerIdEmpresa(idUsuario, tipoCliente))
          mensaje <- ValidationT(DataAccessAdapter.eliminarIpEmpresa(new IpsEmpresa(idEmpresa, ip)))
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

}