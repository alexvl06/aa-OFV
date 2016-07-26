package co.com.alianza.domain.aggregates.pin

import java.sql.Timestamp

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import co.com.alianza.app.{ AlianzaActors, MainActors }
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente
import co.com.alianza.domain.aggregates.usuarios.{ ErrorPersistence, ErrorValidacion, ValidacionesUsuario }
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.pinclienteadmin.{ DataAccessAdapter => pDataAccessAdapter }
import co.com.alianza.infrastructure.anticorruption.ultimasContrasenasClienteAdmin.{ DataAccessAdapter => DataAccessAdapterUltimaContrasena }
import co.com.alianza.infrastructure.anticorruption.usuarios.{ DataAccessAdapter => uDataAccessAdapter }
import co.com.alianza.infrastructure.dto.{ PinUsuario, PinUsuarioEmpresarialAdmin, UsuarioEmpresarialAdmin }
import co.com.alianza.infrastructure.messages.PinMessages._
import co.com.alianza.infrastructure.messages.ResponseMessage
import co.com.alianza.persistence.entities.UltimaContrasenaUsuarioEmpresarialAdmin
import co.com.alianza.util.FutureResponse
import co.com.alianza.util.clave.Crypto
import co.com.alianza.util.transformers.ValidationT
import spray.http.StatusCodes._
import co.com.alianza.infrastructure.anticorruption.usuarios.{ DataAccessAdapter => DataAdapterUsuario }
import co.com.alianza.persistence.entities.IpsEmpresa

import scala.util.{ Failure, Success, Try }
import scalaz.std.AllInstances._
import scalaz.{ Failure => zFailure, Success => zSuccess }
import scala.concurrent.{ ExecutionContext, Future }
import scalaz.Validation
import akka.routing.RoundRobinPool
import enumerations.{ AppendPasswordUser, EstadosEmpresaEnum, PerfilesUsuario }
/**
 * Created by manuel on 6/01/15.
 */
class PinUsuarioEmpresarialAdminActor(implicit val system: ActorSystem) extends Actor with ActorLogging with AlianzaActors with FutureResponse {

  import system.dispatcher
  import co.com.alianza.domain.aggregates.empresa.ValidacionesClienteAdmin._
  import co.com.alianza.domain.aggregates.usuarios.ValidacionesUsuario._

  def receive = {
    case message: ValidarPin => validarPin(message.tokenHash, message.funcionalidad.get)
    case message: CambiarContrasena =>
      val currentSender = sender()
      cambiarPw(message.tokenHash, message.pw, currentSender, message.ip)
  }

  /**
   * Validaciones respectivas para la utilización
   * del pin que se esta utilizando para el cliente.
   *
   * @param tokenHash
   * @param funcionalidad
   */
  private def validarPin(tokenHash: String, funcionalidad: Int) = {
    val currentSender = sender()
    val result = (for {
      pin <- ValidationT(obtenerPin(tokenHash))
      pinValidacion <- ValidationT(PinUtil.validarPinUsuarioEmpresarialAdminFuture(pin))
      clienteAdmin <- ValidationT(validacionObtenerClienteAdminPorId(pin.get.idUsuario))
      estadoEmpresa <- ValidationT(validarEstadoEmpresa(clienteAdmin.identificacion))
      estadoUsuario <- ValidationT(validacionEstadoClienteAdmin(clienteAdmin))
      clienteAdminActivo <- ValidationT(validarClienteAdminExiste(clienteAdmin))
    } yield {
      pin
    }).run
    resolveOlvidoContrasenaFuture(result, funcionalidad, currentSender)
  }

  private def resolveOlvidoContrasenaFuture(finalResultFuture: Future[Validation[ErrorValidacion, Option[PinUsuarioEmpresarialAdmin]]], funcionalidad: Int, currentSender: ActorRef) = {
    finalResultFuture onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response: Option[PinUsuario]) => currentSender ! PinUtil.validarPinUsuarioEmpresarialAdmin(response, funcionalidad)
          case zFailure(error) =>
            error match {
              case errorPersistence: ErrorPersistence => currentSender ! errorPersistence.exception
              case errorVal: ErrorValidacion => currentSender ! ResponseMessage(Conflict, errorVal.msg)
            }
        }
    }
  }

  private def cambiarPw(tokenHash: String, pw: String, currentSender: ActorRef, ip: Option[String]) = {
    val passwordAppend = pw.concat(AppendPasswordUser.appendUsuariosFiducia)
    //En la funcion los cambios: idUsuario y tokenHash que se encuentran en ROJO, no son realmente un error.
    val finalResultFuture = (for {
      pin <- ValidationT(obtenerPin(tokenHash))
      pinValidacion <- ValidationT(PinUtil.validarPinUsuarioEmpresarialAdminFuture(pin))
      clienteAdminOk <- ValidationT(validacionObtenerClienteAdminPorId(pinValidacion.idUsuario))
      clienteAdminActivo <- ValidationT(validarClienteAdminExiste(clienteAdminOk))
      estadoEmpresaOk <- ValidationT(validarEstadoEmpresa(clienteAdminOk.identificacion))
      estadoUsuario <- ValidationT(validacionEstadoClienteAdmin(clienteAdminOk))
      rvalidacionClave <- ValidationT(co.com.alianza.domain.aggregates.usuarios.ValidacionesUsuario.validacionReglasClave(pw, pinValidacion.idUsuario, PerfilesUsuario.clienteAdministrador))
      rCambiarPss <- ValidationT(cambiarPassword(pinValidacion.idUsuario, passwordAppend))
      guardarUltimaContrasena <- ValidationT(guardarUltimaContrasena(pinValidacion.idUsuario, Crypto.hashSha512(passwordAppend, pinValidacion.idUsuario)))
      rCambiarEstado <- ValidationT(cambiarEstado(pinValidacion.idUsuario))
      idEmpresa <- ValidationT(obtenerIdEmpresa(pinValidacion.idUsuario, TiposCliente.clienteAdministrador))
      guardarIp <- ValidationT(guardarIpUsuarioEmpresarial(ip, idEmpresa))
      idResult <- ValidationT(eliminarPin(pinValidacion.tokenHash))
    } yield {
      idResult
    }).run
    resolveCambioPwFuture(finalResultFuture, currentSender)
  }

  private def guardarUltimaContrasena(idUsuario: Int, uContrasena: String): Future[Validation[ErrorValidacion, Int]] = {
    DataAccessAdapterUltimaContrasena.guardarUltimaContrasena(UltimaContrasenaUsuarioEmpresarialAdmin(None, idUsuario, uContrasena, new Timestamp(System.currentTimeMillis()))).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
  }

  private def obtenerPin(tokenHash: String): Future[Validation[ErrorValidacion, Option[PinUsuarioEmpresarialAdmin]]] = {
    pDataAccessAdapter.obtenerPin(tokenHash).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
  }

  private def cambiarPassword(idUsuario: Int, pw: String): Future[Validation[ErrorValidacion, Int]] =
    uDataAccessAdapter cambiarPasswordUsuarioEmpresarialAdmin (idUsuario, Crypto.hashSha512(pw, idUsuario)) map (_.leftMap(pe => ErrorPersistence(pe.message, pe)))

  private def cambiarEstado(idUsuario: Int): Future[Validation[ErrorValidacion, Int]] = {
    val estado = EstadosEmpresaEnum.activo.id
    uDataAccessAdapter actualizarEstadoUsuarioEmpresarialAdmin (idUsuario, estado) map (_.leftMap(pe => ErrorPersistence(pe.message, pe)))
  }

  private def validarClienteAdminExiste(usuario: UsuarioEmpresarialAdmin): Future[Validation[ErrorValidacion, Boolean]] = {
    val estadoActivo = EstadosEmpresaEnum.activo.id == usuario.estado
    estadoActivo match {
      case true => Future.successful(zSuccess(true))
      case false => validacionClienteAdminActivo(usuario.identificacion)
    }
  }

  private def eliminarPin(tokenHash: String): Future[Validation[ErrorValidacion, Int]] =
    pDataAccessAdapter eliminarPin (tokenHash) map (_.leftMap(pe => ErrorPersistence(pe.message, pe)))

  private def resolveCambioPwFuture(finalResultFuture: Future[Validation[ErrorValidacion, Int]], currentSender: ActorRef) = {
    finalResultFuture onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response: Int) => currentSender ! ResponseMessage(OK)
          case zFailure(error) =>
            error match {
              case errorPersistence: ErrorPersistence => currentSender ! errorPersistence.exception
              case errorVal: ErrorValidacion => currentSender ! ResponseMessage(Conflict, errorVal.msg)
            }
        }
    }
  }

  /**
   * Guardar ip equipo de confianza en empresa
   * @param ip
   * @param idEmpresa
   * @return
   */
  private def guardarIpUsuarioEmpresarial(ip: Option[String], idEmpresa: Int): Future[Validation[ErrorValidacion, String]] = {
    ip match {
      case Some(ip: String) =>

        val ipUsuario: IpsEmpresa = new IpsEmpresa(idEmpresa, ip)
        toErrorValidation(DataAdapterUsuario.agregarIpEmpresa(ipUsuario))
      case _ => Future(zSuccess("OK"))
    }
  }

  private def obtenerIdEmpresa(idUsuario: Int, tipoClienteAdmin: TiposCliente): Future[Validation[ErrorValidacion, Int]] = {
    toErrorValidation(DataAdapterUsuario.obtenerIdEmpresa(idUsuario, tipoClienteAdmin))
  }

}