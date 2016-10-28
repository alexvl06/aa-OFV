package co.com.alianza.domain.aggregates.pin

import java.security.MessageDigest
import java.util.Date

import co.com.alianza.domain.aggregates.usuarios.{ ErrorPin, ErrorValidacion }
import co.com.alianza.exceptions.{ BusinessLevel, PersistenceException }
import co.com.alianza.infrastructure.anticorruption.configuraciones.{ DataAccessAdapter => dataAccesAdaptarConf, DataAccessTranslator => dataAccessTransConf }
import co.com.alianza.infrastructure.anticorruption.usuarios.{ DataAccessAdapter => uDataAccessAdapter }
import co.com.alianza.infrastructure.dto.{ PinUsuario, PinUsuarioAgenteEmpresarial, PinUsuarioEmpresarialAdmin }
import co.com.alianza.infrastructure.messages.{ ErrorMessage, ResponseMessage }
import co.com.alianza.domain.aggregates.usuarios.{ErrorPin, ErrorValidacion}
import co.com.alianza.exceptions.{BusinessLevel, PersistenceException}
import co.com.alianza.infrastructure.anticorruption.configuraciones.{DataAccessAdapter => dataAccesAdaptarConf, DataAccessTranslator => dataAccessTransConf}
import co.com.alianza.infrastructure.anticorruption.usuarios.{DataAccessAdapter => uDataAccessAdapter}
import co.com.alianza.infrastructure.dto.{PinUsuario, PinUsuarioAgenteEmpresarial, PinUsuarioEmpresarialAdmin}
import co.com.alianza.infrastructure.messages.{ErrorMessage, ResponseMessage}
import co.com.alianza.persistence.entities.PinAgenteInmobiliario
import co.com.alianza.persistence.util.DataBaseExecutionContext
import co.com.alianza.util.json.MarshallableImplicits._
import enumerations.EstadosPin.EstadoPin
import enumerations.{ EstadosEmpresaEnum, EstadosPin, EstadosUsuarioEnum }
import org.joda.time.{ DateTime, DateTimeZone }
import spray.http.StatusCodes._

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }
import enumerations.{EstadosEmpresaEnum, EstadosPin, EstadosUsuarioEnum}
import org.joda.time.{DateTime, DateTimeZone}
import spray.http.StatusCodes._

import scala.concurrent.{ExecutionContext, Future}
import scalaz.{Validation, Failure => zFailure, Success => zSuccess}

object PinUtil {

  implicit val ec: ExecutionContext = DataBaseExecutionContext.executionContext

  def deserializarPin(pin: String, fechaExpiracion: Date): String = {
    val md = MessageDigest.getInstance("SHA-512")
    val hash = md.digest(s"""${pin} - ${fechaExpiracion}""".getBytes)
    val hexString = new StringBuffer()
    for (i <- hash) {
      hexString.append(Integer.toHexString(0xFF & i))
    }
    hexString.toString
  }

  def validarPin(response: Option[PinUsuario], funcionalidad: Int) = {
    response match {
      case Some(valueResponse) =>
        val pinHash = deserializarPin(valueResponse.token, valueResponse.fechaExpiracion)
        if (pinHash == valueResponse.tokenHash) {
          val fecha = new Date()
          if (fecha.getTime < valueResponse.fechaExpiracion.getTime) {
            //Se comprueba que la funcionalidad desde donde se genero el PIN es Olvido de Contrasena, para actualizar el estado del
            //Usuario
            val futureConsultaUsuarios: Future[Validation[PersistenceException, Int]] = funcionalidad match {
              case 1 => uDataAccessAdapter.actualizarEstadoUsuario(valueResponse.idUsuario, EstadosUsuarioEnum.pendienteReinicio.id)
              case _ => Future.successful(Validation.failure(PersistenceException(new Exception, BusinessLevel, "La funcionalidad no permite cambio de estado del usuario al que pertenece el PIN")))
            }
            ResponseMessage(OK)
          } else
            ResponseMessage(Conflict, errorPinNoEncontrado)
        } else {
          ResponseMessage(Conflict, errorPinNoEncontrado)
        }
      case None => ResponseMessage(Conflict, errorPinNoEncontrado)
    }
  }

  def validarPinUsuarioEmpresarialAdmin(response: Option[PinUsuarioEmpresarialAdmin], funcionalidad: Int) = {
    response match {
      case Some(valueResponse) =>
        val pinHash = deserializarPin(valueResponse.token, valueResponse.fechaExpiracion)
        if (pinHash == valueResponse.tokenHash) {
          val fecha = new Date()
          if (fecha.getTime < valueResponse.fechaExpiracion.getTime) {
            val futureConsultaUsuarios: Future[Validation[PersistenceException, Int]] = funcionalidad match {
              case 1 => uDataAccessAdapter.actualizarEstadoUsuarioEmpresarialAdmin(valueResponse.idUsuario, EstadosEmpresaEnum.pendienteReiniciarContrasena.id)
              case _ => Future.successful(Validation.failure(PersistenceException(new Exception, BusinessLevel, "La funcionalidad no permite cambio de estado del usuario al que pertenece el PIN")))
            }
            ResponseMessage(OK)
          } else
            ResponseMessage(Conflict, errorPinNoEncontradoClienteAdmin)
        } else {
          ResponseMessage(Conflict, errorPinNoEncontradoClienteAdmin)
        }
      case None => ResponseMessage(Conflict, errorPinNoEncontradoClienteAdmin)
    }
  }

  def validarPinAgenteEmpresarial(response: Option[PinUsuarioAgenteEmpresarial]) = {
    response match {
      case Some(valueResponse) =>
        val pinHash = deserializarPin(valueResponse.token, valueResponse.fechaExpiracion)
        if (pinHash == valueResponse.tokenHash) {
          val fecha = new Date()
          if (fecha.getTime < valueResponse.fechaExpiracion.getTime) {
            val futureConsultaUsuarios: Future[Validation[PersistenceException, Int]] = uDataAccessAdapter.actualizarEstadoUsuarioEmpresarialAgente(valueResponse.idUsuario, EstadosEmpresaEnum.pendienteReiniciarContrasena.id)
            ResponseMessage(OK)
          } else
            ResponseMessage(Conflict, errorPinNoEncontradoAgenteEmpresarial)
        } else {
          ResponseMessage(Conflict, errorPinNoEncontradoAgenteEmpresarial)
        }
      case None => ResponseMessage(Conflict, errorPinNoEncontradoAgenteEmpresarial)
    }
  }

  def validarPinFuture(response: Option[PinUsuario]): Future[Validation[ErrorValidacion, PinUsuario]] = Future {
    response match {
      case Some(valueResponse) =>
        val pinHash = deserializarPin(valueResponse.token, valueResponse.fechaExpiracion)
        if (pinHash == valueResponse.tokenHash) {
          val fecha = new Date()
          if (fecha.getTime < valueResponse.fechaExpiracion.getTime) zSuccess(valueResponse)
          else zFailure(ErrorPin(errorPinNoEncontrado))
        } else zFailure(ErrorPin(errorPinNoEncontrado))
      case None => zFailure(ErrorPin(errorPinNoEncontrado))
    }
  }

  def validarPinUsuarioEmpresarialAdminFuture(response: Option[PinUsuarioEmpresarialAdmin]): Future[Validation[ErrorValidacion, PinUsuarioEmpresarialAdmin]] = Future {
    response match {
      case Some(valueResponse) =>
        val pinHash = deserializarPin(valueResponse.token, valueResponse.fechaExpiracion)
        if (pinHash == valueResponse.tokenHash) {
          val fecha = new Date()
          if (fecha.getTime < valueResponse.fechaExpiracion.getTime) zSuccess(valueResponse)
          else zFailure(ErrorPin(errorPinNoEncontradoClienteAdmin))
        } else zFailure(ErrorPin(errorPinNoEncontradoClienteAdmin))
      case None => zFailure(ErrorPin(errorPinNoEncontradoClienteAdmin))
    }
  }

  def validarPinUsuarioAgenteEmpresarialFuture(response: Option[PinUsuarioAgenteEmpresarial]): Future[Validation[ErrorValidacion, PinUsuarioAgenteEmpresarial]] = Future {
    response match {
      case Some(valueResponse) =>
        val pinHash = deserializarPin(valueResponse.token, valueResponse.fechaExpiracion)
        if (pinHash == valueResponse.tokenHash) {
          val fecha = new Date()
          if (fecha.getTime < valueResponse.fechaExpiracion.getTime) zSuccess(valueResponse)
          else zFailure(ErrorPin(errorPinNoEncontradoAgenteEmpresarial))
        } else zFailure(ErrorPin(errorPinNoEncontradoAgenteEmpresarial))
      case None => zFailure(ErrorPin(errorPinNoEncontradoAgenteEmpresarial))
    }
  }

  /**
    * Verifica la validez de un pin generado para un agente empresaial
    *
    * @param pinOp Pin a validar
    * @return Un Either [CodigoError, Booleano] que indica si el pin es válido o no
    *
    */
  def validarPinAgenteInmobiliario(pinOp: Option[PinAgenteInmobiliario]): Either[EstadoPin, PinAgenteInmobiliario] = {
    pinOp.map { pin =>
      val hash: String = deserializarPin(pin.token, pin.fechaExpiracion.toDate)
      if (hash == pin.tokenHash) {
        val now: DateTime = DateTime.now(DateTimeZone.UTC)
        if (now.isBefore(pin.fechaExpiracion)) {
          Right(pin)
        } else {
          // el pin está caducado
          Left(EstadosPin.PIN_CADUCADO)
        }
      } else {
        // el hash del pin es diferente al hash calculado
        Left(EstadosPin.PIN_INVALIDO)
      }
    }.getOrElse {
      // no existe el pin
      Left(EstadosPin.PIN_NO_EXISTE)
    }
  }

  private val errorPinNoEncontrado = ErrorMessage("409.1", "Pin invalido", "El proceso para la definición de la contraseña está vencido. Si requiere una nueva contraseña solicítela <a href=\"/#!/olvidarContrasena\" target=\"_blank\" >AQUÍ</a>.").toJson
  private val errorPinNoEncontradoAgenteEmpresarial = ErrorMessage("409.1", "Pin invalido", "El proceso para la definición de la contraseña está vencido. Si requiere una nueva contraseña solicítela con su cliente administrador.").toJson
  private val errorPinNoEncontradoClienteAdmin = ErrorMessage("409.1", "Pin invalido", "El proceso para la definición de la contraseña está vencido. Si requiere una nueva contraseña solicítela <a href=\"/#!/olvidarContrasenaEmpresa\" target=\"_blank\" >AQUÍ</a>.").toJson

}