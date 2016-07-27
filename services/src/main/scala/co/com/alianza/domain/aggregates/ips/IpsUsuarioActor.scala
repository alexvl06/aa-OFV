package co.com.alianza.domain.aggregates.ips

import akka.actor.SupervisorStrategy._
import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, OneForOneStrategy, Props }
import akka.pattern._
import akka.routing.RoundRobinPool
import akka.util.Timeout
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.domain.aggregates.autenticacion.{ AgregarIp, RemoverIp }
import co.com.alianza.exceptions.{ BusinessLevel, PersistenceException }
import co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
import co.com.alianza.infrastructure.messages._
import co.com.alianza.persistence.entities.{ IpsEmpresa, IpsUsuario }
import co.com.alianza.util.transformers.ValidationT
import com.typesafe.config.Config
import spray.http.StatusCodes._

import scala.concurrent.duration.DurationInt
import scala.util.{ Failure, Success }
import scalaz.std.AllInstances._
import scalaz.{ Failure => zFailure, Success => zSuccess }

class IpsUsuarioActorSupervisor extends Actor with ActorLogging {

  val ipsUsuarioActor = context.actorOf(
    Props[IpsUsuarioActor].withRouter(RoundRobinPool(nrOfInstances = 2)),
    "ipsUsuarioActor"
  )

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
class IpsUsuarioActor extends Actor with ActorLogging {
  import context.dispatcher
  implicit val config: Config = context.system.settings.config
  implicit val timeout = Timeout(120.seconds)
  import co.com.alianza.util.json.MarshallableImplicits._

  def receive = {
    case message: ObtenerIpsUsuarioMessage => obtenerIpsUsuario(message.idUsuario, message.tipoCliente)
    case message: AgregarIpsUsuarioMessage => agregarIpsUsuarioMessage(message.idUsuario.get, message.ip, obtenerEnumTipoCliente(message.tipoCliente))
    case message: EliminarIpsUsuarioMessage => eliminarIpsUsuarioMessage(message.idUsuario.get, message.ip, obtenerEnumTipoCliente(message.tipoCliente))
  }

  def obtenerEnumTipoCliente(tipoCliente: Option[Int]) = {
    TiposCliente.apply(tipoCliente.get)
  }

  def obtenerIpsUsuario(idUsuario: Int, tipoCliente: TiposCliente) = {
    val currentSender = sender()
    val result = {
      if (tipoCliente.equals(TiposCliente.clienteIndividual))
        DataAccessAdapter.obtenerIpsUsuario(idUsuario)
      else {
        (for {
          idEmpresa <- ValidationT(DataAccessAdapter.obtenerIdEmpresa(idUsuario, tipoCliente))
          vectorIpsEmpresa <- ValidationT(DataAccessAdapter.obtenerIpsEmpresa(idEmpresa))
        } yield vectorIpsEmpresa).run
      }
    }
    result onComplete {
      case Failure(failure) =>
        currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response: Vector[IpsUsuario]) =>
            currentSender ! ResponseMessage(OK, response.toJson)
          case zSuccess(response: Vector[IpsEmpresa]) =>
            currentSender ! ResponseMessage(OK, response.toJson)
          case zFailure(error) =>
            currentSender ! error
        }
    }
  }

  def agregarIpsUsuarioMessage(idUsuario: Int, ip: String, tipoCliente: TiposCliente) = {
    val currentSender = sender()
    val result = {
      if (tipoCliente.equals(TiposCliente.clienteIndividual))
        DataAccessAdapter.agregarIpUsuario(new IpsUsuario(idUsuario, ip))
      else
        (for {
          idEmpresa <- ValidationT(DataAccessAdapter.obtenerIdEmpresa(idUsuario, tipoCliente))
          mensaje <- ValidationT(DataAccessAdapter.agregarIpEmpresa(new IpsEmpresa(idEmpresa, ip)))
          agregarIpSesion <- ValidationT(agregarIpSesionEmpresa(idEmpresa, ip))
        } yield (mensaje)).run
    }
    result onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response: String) =>
            currentSender ! ResponseMessage(OK, response.toJson)
          case zFailure(error) => currentSender ! error
        }
    }
  }

  def eliminarIpsUsuarioMessage(idUsuario: Int, ip: String, tipoCliente: TiposCliente) = {
    val currentSender = sender()
    val result = {
      if (tipoCliente.equals(TiposCliente.clienteIndividual))
        DataAccessAdapter.eliminarIpUsuario(new IpsUsuario(idUsuario, ip))
      else
        (for {
          idEmpresa <- ValidationT(DataAccessAdapter.obtenerIdEmpresa(idUsuario, tipoCliente))
          mensaje <- ValidationT(DataAccessAdapter.eliminarIpEmpresa(new IpsEmpresa(idEmpresa, ip)))
          removerIpSesion <- ValidationT(removerIpSesionEmpresa(idEmpresa, ip))
        } yield (mensaje)).run
    }
    result onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response: Int) =>

            currentSender ! ResponseMessage(OK, response.toJson)
          case zFailure(error) => currentSender ! error
        }
    }
  }

  private def agregarIpSesionEmpresa(empresaId: Int, ip: String) =
    context.parent ? ObtenerEmpresaSesionActorId(empresaId) map {
      case Some(empresaSesionActor: ActorRef) =>
        empresaSesionActor ! AgregarIp(ip); zSuccess((): Unit)
      case None => zSuccess((): Unit)
      case _ => zFailure(PersistenceException(new Exception(), BusinessLevel, "Error"))
    }

  private def removerIpSesionEmpresa(empresaId: Int, ip: String) =
    context.parent ? ObtenerEmpresaSesionActorId(empresaId) map {
      case Some(empresaSesionActor: ActorRef) =>
        empresaSesionActor ! RemoverIp(ip); zSuccess((): Unit)
      case None => zSuccess((): Unit)
      case _ => zFailure(PersistenceException(new Exception(), BusinessLevel, "Error"))
    }

}