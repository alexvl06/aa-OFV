package co.com.alianza.domain.aggregates.ips

import akka.actor.{Actor, ActorLogging}
import co.com.alianza.app.AlianzaActors
import co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
import co.com.alianza.infrastructure.messages.{ResponseMessage, ObtenerIpsUsuarioMessage, InboxMessage}
import co.com.alianza.persistence.entities.IpsUsuario
import spray.http.StatusCodes._

import scalaz.{Failure => zFailure, Success => zSuccess}
import scala.util.{Success, Failure}

/**
 * Created by david on 16/06/14.
 */

class IpsUsuarioActor extends Actor with ActorLogging with AlianzaActors {
  import scala.concurrent.ExecutionContext
  implicit val _: ExecutionContext = context.dispatcher
  import co.com.alianza.util.json.MarshallableImplicits._

  def receive = {
    case message: ObtenerIpsUsuarioMessage  => obtenerIpsUsuario(message.idUsuario)
    //case message: ActualizarReglasContrasenasMessage => actualizarReglasContrasenas(message.toEntityReglasContrasenas)
  }

  def obtenerIpsUsuario(idUsuario : Int) = {
    val currentSender = sender()
    val result = DataAccessAdapter.obtenerIpsUsuario(idUsuario)

    result  onComplete {
      case Failure(failure)  =>    currentSender ! failure
      case Success(value)    =>
        value match {
          case zSuccess(response: Vector[IpsUsuario]) =>
            currentSender !  ResponseMessage(OK, response.toJson)
          case zFailure(error)                 =>  currentSender !  error
        }
    }
  }

  /*def actualizarReglasContrasenas(reglasContrasenas: List[ReglasContrasenas]) = {
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

  }*/

}
