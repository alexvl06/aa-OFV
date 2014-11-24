package co.com.alianza.domain.aggregates.pin

import java.security.MessageDigest
import java.util.Date

import co.com.alianza.app.MainActors
import co.com.alianza.constants.TiposConfiguracion
import co.com.alianza.domain.aggregates.usuarios.{ErrorPersistence, ErrorValidacion, ErrorPin}
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.configuraciones.{DataAccessTranslator => dataAccessTransConf, DataAccessAdapter => dataAccesAdaptarConf}
import co.com.alianza.infrastructure.dto.{Configuracion, PinUsuario}
import co.com.alianza.infrastructure.messages.{ErrorMessage, ResponseMessage}
import co.com.alianza.util.json.MarshallableImplicits._
import spray.http.StatusCodes._

import scala.util.{Success, Failure}
import scalaz.{Failure => zFailure, Success => zSuccess}
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Validation

object PinUtil {

  implicit val ex: ExecutionContext = MainActors.dataAccesEx

  def deserializarPin(pin: String, fechaExpiracion: Date): String = {
    val md = MessageDigest.getInstance("SHA-512")
    val hash = md.digest( s"""${pin} - ${fechaExpiracion}""".getBytes)
    val hexString = new StringBuffer()
    for (i <- hash) {
      hexString.append(Integer.toHexString(0xFF & i))
    }
    hexString.toString
  }

  def validarPin(caducidad: Int, response: Option[PinUsuario]) = {
   response match {
      case Some(valueResponse) =>
        val pinHash = deserializarPin(valueResponse.token, valueResponse.fechaExpiracion)
        if (pinHash == valueResponse.tokenHash) {
          val fecha = new Date()
          if (fecha.getTime < valueResponse.fechaExpiracion.getTime)
            ResponseMessage(OK)
          else
            ResponseMessage(Conflict, errorCaducoPin)
        }
        else {
          ResponseMessage(Conflict, errorPinInvalido)
        }
      case None => errorPinNoEncontrado(caducidad)
    }
  }

  def validarPinFuture(caducidad: Int, response: Option[PinUsuario]): Future[Validation[ErrorValidacion, PinUsuario]] = Future {
    response match {
      case Some(valueResponse) =>
        val pinHash = deserializarPin(valueResponse.token, valueResponse.fechaExpiracion)
        if (pinHash == valueResponse.tokenHash) {
          val fecha = new Date()
          if (fecha.getTime < valueResponse.fechaExpiracion.getTime) zSuccess(valueResponse)
          else zFailure(ErrorPin(errorCaducoPin))
        }
        else zFailure(ErrorPin(errorPinInvalido))
      case None => zFailure(ErrorPin(errorPinNoEncontrado(caducidad).toJson))
    }
  }

  def errorPinNoEncontrado(caducidad: Int) : ResponseMessage = {
    val plural = if (caducidad > 1)  "s" else ""
    val message = s"El proceso de definir contraseña se venció por las siguientes opciones: Paso mas de $caducidad hora$plural o ya fue utilizado."
    ResponseMessage(Conflict, ErrorMessage("409.1", "Pin invalido", message).toJson)
  }

  private val errorCaducoPin = ErrorMessage("409.2", "El pin ha caducado", "El pin ha caducado").toJson
  private val errorPinInvalido = ErrorMessage("409.3", "El pin es invalido", "El pin es invalido").toJson

}
