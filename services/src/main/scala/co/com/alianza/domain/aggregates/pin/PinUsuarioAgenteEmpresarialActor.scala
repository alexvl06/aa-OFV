package co.com.alianza.domain.aggregates.pin

import java.sql.Timestamp

import akka.actor.{Actor, ActorLogging, ActorRef}
import co.com.alianza.app.{AlianzaActors, MainActors}
import co.com.alianza.domain.aggregates.usuarios.{ErrorPersistence, ErrorValidacion}
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.pinagenteempresarial.{DataAccessAdapter => pDataAccessAdapter}
import co.com.alianza.infrastructure.anticorruption.ultimasContrasenasAgenteEmpresarial.{DataAccessAdapter => DataAccessAdapterUltimaContrasena}
import co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial.{DataAccessAdapter => uDataAccessAdapter}
import co.com.alianza.infrastructure.dto.{PinUsuario, PinUsuarioAgenteEmpresarial}
import co.com.alianza.infrastructure.messages.PinMessages._
import co.com.alianza.infrastructure.messages.ResponseMessage
import co.com.alianza.persistence.entities.{PerfilUsuario, UltimaContrasenaUsuarioAgenteEmpresarial}
import co.com.alianza.util.FutureResponse
import co.com.alianza.util.clave.Crypto
import co.com.alianza.util.transformers.ValidationT
import enumerations.{PerfilesUsuario, AppendPasswordUser}
import spray.http.StatusCodes._
import scalaz.std.AllInstances._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import scalaz.{Validation, Failure => zFailure, Success => zSuccess}

/**
 * Created by manuel on 6/01/15.
 */
class PinUsuarioAgenteEmpresarialActor extends Actor with ActorLogging with AlianzaActors with FutureResponse  {

  implicit val ex: ExecutionContext = MainActors.dataAccesEx
  import co.com.alianza.domain.aggregates.empresa.ValidacionesAgenteEmpresarial._

  def receive = {
    case message: ValidarPin => validarPin(message.tokenHash)

    case message: CambiarPw =>
      val currentSender = sender()
      cambiarPw(message.tokenHash, message.pw, currentSender, PerfilesUsuario.agenteEmpresarial)
  }

  private def validarPin(tokenHash: String) = {
    val currentSender = sender()

    val result = (for {
      pin                 <- ValidationT(obtenerPin(tokenHash))
      pinValidacion       <- ValidationT(PinUtil.validarPinUsuarioAgenteEmpresarialFuture(pin))
      agenteEmpresarial        <- ValidationT(validacionObtenerAgenteEmpId(pin.get.idUsuario))
      estadoEmpresa       <- ValidationT(validarEstadoEmpresa(agenteEmpresarial.identificacion))
      estadoUsuario       <- ValidationT(validacionEstadoAgenteEmp(agenteEmpresarial))
    } yield {
      pin
    }).run

    resolveOlvidoContrasenaFuture(result, currentSender)
  }

  private def resolveOlvidoContrasenaFuture(finalResultFuture: Future[Validation[ErrorValidacion, Option[PinUsuarioAgenteEmpresarial]]], currentSender: ActorRef) = {
    finalResultFuture onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response:  Option[PinUsuario]) => currentSender ! PinUtil.validarPinAgenteEmpresarial(response)
          case zFailure(error) =>
            error match {
              case errorPersistence: ErrorPersistence => currentSender ! errorPersistence.exception
              case errorVal: ErrorValidacion => currentSender ! ResponseMessage(Conflict, errorVal.msg)
            }
        }
    }
  }

  private def obtenerPin(tokenHash: String): Future[Validation[ErrorValidacion, Option[PinUsuarioAgenteEmpresarial]]]= {
    pDataAccessAdapter.obtenerPin(tokenHash).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
  }


  private def cambiarPw(tokenHash: String, pw: String, currentSender: ActorRef, perfilUsuario: PerfilesUsuario.perfilUsuario) = {

    val obtenerPinFuture: Future[Validation[ErrorValidacion, Option[PinUsuarioAgenteEmpresarial]]] = pDataAccessAdapter.obtenerPin(tokenHash).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
    val passwordAppend = pw.concat( AppendPasswordUser.appendUsuariosFiducia )

    //En la funcion los cambios: idUsuario y tokenHash que se encuentran en ROJO, no son realmente un error.
    val finalResultFuture = (for {
      pin <- ValidationT(obtenerPinFuture)
      pinValidacion <- ValidationT(PinUtil.validarPinUsuarioAgenteEmpresarialFuture(pin))
      clienteAdminOk <- ValidationT(validacionObtenerAgenteEmpId(pinValidacion.idUsuario))
      estadoEempresaOk <- ValidationT(co.com.alianza.domain.aggregates.empresa.ValidacionesClienteAdmin.validarEstadoEmpresa(clienteAdminOk.identificacion))
      rvalidacionClave <- ValidationT(co.com.alianza.domain.aggregates.usuarios.ValidacionesUsuario.validacionReglasClave(pw, pinValidacion.idUsuario, perfilUsuario))
      rCambiarPss <- ValidationT(cambiarPassword(pinValidacion.idUsuario, passwordAppend))
      resultGuardarUltimasContrasenas <- ValidationT(guardarUltimaContrasena(pinValidacion.idUsuario, Crypto.hashSha512(passwordAppend)))
      rCambiarEstado <- ValidationT(cambiarEstado(pinValidacion.idUsuario))
      idResult <- ValidationT(eliminarPin(pinValidacion.tokenHash))
      estadoUsuario       <- ValidationT(validacionEstadoAgenteEmp(clienteAdminOk))
    } yield {
      idResult
    }).run

    resolveCrearUsuarioFuture(finalResultFuture, currentSender)
  }

  private def guardarUltimaContrasena(idUsuario: Int, uContrasena: String): Future[Validation[ErrorValidacion, Unit]] = {
    DataAccessAdapterUltimaContrasena.guardarUltimaContrasena(UltimaContrasenaUsuarioAgenteEmpresarial(None, idUsuario , uContrasena, new Timestamp(System.currentTimeMillis()))).map(_.leftMap( pe => ErrorPersistence(pe.message, pe)))
  }

  private def cambiarPassword(idUsuario: Int, pw: String): Future[Validation[ErrorValidacion, Int]] =
    uDataAccessAdapter cambiarPasswordUsuarioAgenteEmpresarial (idUsuario, Crypto.hashSha512(pw)) map (_.leftMap(pe => ErrorPersistence(pe.message, pe)))

  private def cambiarEstado(idUsuario: Int): Future[Validation[ErrorValidacion, Int]] =
    uDataAccessAdapter actualizarEstadoUsuarioAgenteEmpresarial (idUsuario, 1) map (_.leftMap(pe => ErrorPersistence(pe.message, pe)))

  private def eliminarPin(tokenHash: String): Future[Validation[ErrorValidacion, Int]] =
    pDataAccessAdapter eliminarPin (tokenHash) map (_.leftMap(pe => ErrorPersistence(pe.message, pe)))

  private def resolveCrearUsuarioFuture(finalResultFuture: Future[Validation[ErrorValidacion, Int]], currentSender: ActorRef) = {
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

}