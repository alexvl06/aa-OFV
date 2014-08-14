package co.com.alianza.domain.aggregates.pin

import java.util.Date

import akka.actor.{ActorLogging, Actor}
import co.com.alianza.app.{MainActors, AlianzaActors}
import co.com.alianza.infrastructure.anticorruption.pin.DataAccessAdapter
import co.com.alianza.infrastructure.dto.PinUsuario
import co.com.alianza.infrastructure.messages.PinMessages._
import co.com.alianza.infrastructure.messages.{ErrorMessage, ResponseMessage}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import scalaz.{Success => zSuccess, Failure => zFailure}

import spray.http.StatusCodes._

class PinActor extends Actor with ActorLogging with AlianzaActors {

  implicit val ex: ExecutionContext = MainActors.dataAccesEx
  import co.com.alianza.util.json.MarshallableImplicits._

  def receive = {
    case message: ValidarPin => validarPin(message.tokenHash)
  }

  private def validarPin(tokenHash: String) = {
    val currentSender = sender()
    val result = DataAccessAdapter.obtenerPin(tokenHash)

    result onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response: Option[PinUsuario]) =>
            response match {
              case Some(valueResponse) =>
                val pinHash = PinUtil.deserializarPin(valueResponse.token, valueResponse.fechaExpiracion)
                if(pinHash == valueResponse.tokenHash) {
                  val fecha = new Date()
                  if(fecha.getTime < valueResponse.fechaExpiracion.getTime) currentSender ! ResponseMessage(OK)
                  else currentSender ! ResponseMessage(Conflict, errorCaducoPin)
                }
                else currentSender ! ResponseMessage(Conflict, errorPinInvalido)
              case None => currentSender ! ResponseMessage(NotFound, errorPinNoEncontrado)
            }
          case zFailure(error) => currentSender ! error
        }
    }
  }

  private val errorPinNoEncontrado = ErrorMessage("409.1", "No se ha encontrado el pin", "No se ha encontrado el pin").toJson
  private val errorCaducoPin = ErrorMessage("409.2", "El pin ha caducado", "El pin ha caducado").toJson
  private val errorPinInvalido = ErrorMessage("409.3", "El pin es invalido", "El pin es invalido").toJson
}
