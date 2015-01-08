package co.com.alianza.domain.aggregates.pin

import java.sql.Timestamp

import akka.actor.{ActorRef, ActorLogging, Actor}

import co.com.alianza.app.{MainActors, AlianzaActors}
import co.com.alianza.domain.aggregates.usuarios.{ErrorPersistence, ErrorValidacion, ValidacionesUsuario}
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.pinclienteadmin.{DataAccessAdapter => pDataAccessAdapter}
import co.com.alianza.infrastructure.anticorruption.ultimasContrasenasClienteAdmin.{ DataAccessAdapter => DataAccessAdapterUltimaContrasena }
import co.com.alianza.infrastructure.anticorruption.usuarios.{DataAccessAdapter => uDataAccessAdapter}
import co.com.alianza.infrastructure.dto.PinUsuarioEmpresarialAdmin
import co.com.alianza.infrastructure.messages.PinMessages._
import co.com.alianza.infrastructure.messages.ResponseMessage
import co.com.alianza.persistence.entities.UltimaContrasenaUsuarioEmpresarialAdmin
import co.com.alianza.util.FutureResponse
import co.com.alianza.util.clave.Crypto
import co.com.alianza.util.transformers.ValidationT
import spray.http.StatusCodes._

import scala.util.{Success, Failure, Try}
import scalaz.std.AllInstances._
import scalaz.{Failure => zFailure, Success => zSuccess}

import scala.concurrent.{Future, ExecutionContext}
import scalaz.Validation

import akka.actor.Props
import akka.routing.RoundRobinPool
import enumerations.AppendPasswordUser
/**
 * Created by manuel on 6/01/15.
 */
class PinUsuarioEmpresarialAdminActor extends Actor with ActorLogging with AlianzaActors with FutureResponse  {

  implicit val ex: ExecutionContext = MainActors.dataAccesEx
  import ValidacionesUsuario._

  def receive = {
    case message: ValidarPin => validarPin(message.tokenHash)
    case message: CambiarPw =>
      val currentSender = sender()
      cambiarPw(message.tokenHash, message.pw, currentSender)
  }

  private def validarPin(tokenHash: String) = {
    val currentSender = sender()
    val result: Future[Validation[PersistenceException, Option[PinUsuarioEmpresarialAdmin]]] = pDataAccessAdapter.obtenerPin(tokenHash)
    resolveFutureValidation(result, PinUtil.validarPinUsuarioEmpresarialAdmin, currentSender)
  }


  private def cambiarPw(tokenHash: String, pw: String, currentSender: ActorRef) = {

    val obtenerPinFuture: Future[Validation[ErrorValidacion, Option[PinUsuarioEmpresarialAdmin]]] = pDataAccessAdapter.obtenerPin(tokenHash).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
    val passwordAppend = pw.concat( AppendPasswordUser.appendUsuariosFiducia )

    //En la funcion los cambios: idUsuario y tokenHash que se encuentran en ROJO, no son realmente un error.
    val finalResultFuture = (for {
      pin <- ValidationT(obtenerPinFuture)
      pinValidacion <- ValidationT(PinUtil.validarPinUsuarioEmpresarialAdminFuture(pin))
      rvalidacionClave <- ValidationT(validacionReglasClave(pw, pinValidacion.idUsuario))
      rCambiarPss <- ValidationT(cambiarPassword(pinValidacion.idUsuario, passwordAppend))
      resultGuardarUltimasContrasenas <- ValidationT(guardarUltimaContrasena(pinValidacion.idUsuario, Crypto.hashSha512(passwordAppend)))
      rCambiarEstado <- ValidationT(cambiarEstado(pinValidacion.idUsuario))
      idResult <- ValidationT(eliminarPin(pinValidacion.tokenHash))
    } yield {
      idResult
    }).run

    resolveCrearUsuarioFuture(finalResultFuture, currentSender)
  }

  private def guardarUltimaContrasena(idUsuario: Int, uContrasena: String): Future[Validation[ErrorValidacion, Unit]] = {
    DataAccessAdapterUltimaContrasena.guardarUltimaContrasena(UltimaContrasenaUsuarioEmpresarialAdmin(None, idUsuario , uContrasena, new Timestamp(System.currentTimeMillis()))).map(_.leftMap( pe => ErrorPersistence(pe.message, pe)))
  }

  private def cambiarPassword(idUsuario: Int, pw: String): Future[Validation[ErrorValidacion, Int]] =
    uDataAccessAdapter cambiarPasswordUsuarioEmpresarialAdmin (idUsuario, Crypto.hashSha512(pw)) map (_.leftMap(pe => ErrorPersistence(pe.message, pe)))

  private def cambiarEstado(idUsuario: Int): Future[Validation[ErrorValidacion, Int]] =
    uDataAccessAdapter actualizarEstadoUsuarioEmpresarialAdmin (idUsuario, 1) map (_.leftMap(pe => ErrorPersistence(pe.message, pe)))

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
