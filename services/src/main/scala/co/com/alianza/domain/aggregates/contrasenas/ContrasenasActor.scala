package co.com.alianza.domain.aggregates.contrasenas

import akka.actor.{ActorLogging, Actor}
import co.com.alianza.app.AlianzaActors
import co.com.alianza.infrastructure.anticorruption.contrasenas.DataAccessAdapter
import co.com.alianza.infrastructure.messages.{ActualizarReglasContrasenasMessage, ResponseMessage, InboxMessage}
import co.com.alianza.persistence.entities.ReglasContrasenas
import spray.http.StatusCodes._

import scalaz.{Failure => zFailure, Success => zSuccess}
import scala.util.{Success, Failure}

/**
 * Created by david on 16/06/14.
 */

class ContrasenasActor extends Actor with ActorLogging with AlianzaActors {
  import scala.concurrent.ExecutionContext
  implicit val _: ExecutionContext = context.dispatcher
  import co.com.alianza.util.json.MarshallableImplicits._

  def receive = {
    case message: InboxMessage  => obtenerReglasContrasenas()
    case message: ActualizarReglasContrasenasMessage => actualizarReglasContrasenas(message.toEntityReglasContrasenas)
  }

  def obtenerReglasContrasenas() = {
    val currentSender = sender()
    val result = DataAccessAdapter.consultarReglasContrasenas()

    result  onComplete {
      case Failure(failure)  =>    currentSender ! failure
      case Success(value)    =>
        value match {
          case zSuccess(response: List[ReglasContrasenas]) =>
            currentSender !  ResponseMessage(OK, response.toJson)
          case zFailure(error)                 =>  currentSender !  error
        }
    }
  }

  def actualizarReglasContrasenas(reglasContrasenas: List[ReglasContrasenas]) = {
    val currentSender = sender()
    for(regla <- reglasContrasenas) {
      val result = DataAccessAdapter.actualizarReglasContrasenas(regla)
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

}
