package co.com.alianza.domain.aggregates.ips

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, OneForOneStrategy, Props }
import akka.actor.SupervisorStrategy._
import akka.routing.RoundRobinPool
import akka.pattern._
import co.com.alianza.app.AlianzaActors
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
import co.com.alianza.infrastructure.messages._
import co.com.alianza.persistence.entities.{ Empresa, IpsEmpresa, IpsUsuario }
import co.com.alianza.util.transformers.ValidationT
import co.com.alianza.app.MainActors
import co.com.alianza.domain.aggregates.autenticacion.{ AgregarIp, RemoverIp }
import co.com.alianza.exceptions.BusinessLevel
import com.typesafe.config.Config
import spray.http.StatusCodes._

import scala.util.{ Failure, Success }
import scala.concurrent.Future
import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }
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
class IpsUsuarioActor(implicit val system: ActorSystem) extends Actor with ActorLogging{
  import system.dispatcher
  implicit val config: Config = system.settings.config
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
        } yield (vectorIpsEmpresa)).run
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
    MainActors.sesionActorSupervisor ? ObtenerEmpresaSesionActorId(empresaId) map {
      case Some(empresaSesionActor: ActorRef) =>
        empresaSesionActor ! AgregarIp(ip); zSuccess((): Unit)
      case None => zSuccess((): Unit)
      case _ => zFailure(PersistenceException(new Exception(), BusinessLevel, "Error"))
    }

  private def removerIpSesionEmpresa(empresaId: Int, ip: String) =
    MainActors.sesionActorSupervisor ? ObtenerEmpresaSesionActorId(empresaId) map {
      case Some(empresaSesionActor: ActorRef) =>
        empresaSesionActor ! RemoverIp(ip); zSuccess((): Unit)
      case None => zSuccess((): Unit)
      case _ => zFailure(PersistenceException(new Exception(), BusinessLevel, "Error"))
    }

}