package co.com.alianza.domain.aggregates.contrasenas

import akka.actor.{ActorRef, ActorLogging, Actor}
import co.com.alianza.app.{MainActors, AlianzaActors}
import co.com.alianza.infrastructure.anticorruption.contrasenas.DataAccessAdapter
import co.com.alianza.infrastructure.messages.{CambiarContrasenaMessage, ActualizarReglasContrasenasMessage, ResponseMessage, InboxMessage}
import co.com.alianza.persistence.entities.ReglasContrasenas
import spray.http.StatusCodes._

import scalaz.{Failure => zFailure, Success => zSuccess}
import scala.util.{Failure => sFailure, Success => sSuccess}
import scala.util.{Success, Failure}
import co.com.alianza.util.transformers.ValidationT
import co.com.alianza.domain.aggregates.usuarios.{ErrorPersistence, ErrorValidacion, ValdiacionesUsuario}
import scala.concurrent.Future
import scalaz.std.AllInstances._
import co.com.alianza.infrastructure.dto.Usuario
import co.com.alianza.util.FutureResponse
import akka.actor.Props
import akka.routing.RoundRobinPool
import enumerations.AppendPasswordUser


class ContrasenasActorSupervisor extends Actor with ActorLogging {
  import akka.actor.SupervisorStrategy._
  import akka.actor.OneForOneStrategy

  val contrasenasActor = context.actorOf(Props[ContrasenasActor].withRouter(RoundRobinPool(nrOfInstances = 2)), "contrasenasActor")

  def receive = {

    case message: Any =>
      contrasenasActor forward message

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

class ContrasenasActor extends Actor with ActorLogging with AlianzaActors {

  import scalaz._
  import scalaz.std.string._ // to get `Monoid[String]`
  import scalaz.std.list._ // to get `Traverse[List]`
  import scalaz.syntax.traverse._ // to get the `sequence` method

  import scala.concurrent.ExecutionContext
  //implicit val _: ExecutionContext = context.dispatcher
  import co.com.alianza.util.json.MarshallableImplicits._
  import ValdiacionesUsuario._
  implicit val ex: ExecutionContext = MainActors.dataAccesEx


  def receive = {
    case message: InboxMessage  => obtenerReglasContrasenas()
    case message: ActualizarReglasContrasenasMessage => actualizarReglasContrasenas(message.toEntityReglasContrasenas)

    case message: CambiarContrasenaMessage =>
      val currentSender = sender()
      val passwordActualAppend = message.pw_actual.concat( AppendPasswordUser.appendUsuariosFiducia )
      val passwordNewAppend = message.pw_nuevo.concat( AppendPasswordUser.appendUsuariosFiducia )
      val CambiarContrasenaFuture = (for {
        usuarioContrasenaActual <- ValidationT(validacionConsultaContrasenaActual(passwordActualAppend, message.idUsuario.get))
        idValReglasContra <- ValidationT(validacionReglasClave(message.pw_nuevo))
        idUsuario <- ValidationT(ActualizarContrasena(passwordNewAppend, usuarioContrasenaActual ))
      } yield {
        idUsuario
      }).run

      //resolveFutureValidation(CambiarContrasenaFuture , (response: Int) => response.toJson, currentSender)
      resolveCambiarContrasenaFuture(CambiarContrasenaFuture, currentSender, message)

  }

  private def ActualizarContrasena(pw_nuevo: String, usuario:Option[Usuario]): Future[Validation[ErrorValidacion, Int]] = {
    DataAccessAdapter.ActualizarContrasena(pw_nuevo, usuario.get.id.get).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
  }

  def obtenerReglasContrasenas() = {
    val currentSender = sender()
    val result = DataAccessAdapter.consultarReglasContrasenas()

    result  onComplete {
      case sFailure(failure)  =>    currentSender ! failure
      case sSuccess(value)    =>
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
        case sFailure(failure)  =>    currentSender ! failure
        case sSuccess(value)    =>
          value match {
            case zSuccess(response: Int) =>
              currentSender !  ResponseMessage(OK, response.toJson)
            case zFailure(error)                 =>  currentSender !  error
          }
      }
    }

  }

  private def resolveCambiarContrasenaFuture(CambiarContrasenaFuture: Future[Validation[ErrorValidacion, Int]], currentSender: ActorRef, message: CambiarContrasenaMessage) = {
    CambiarContrasenaFuture onComplete {
      case sFailure(failure) =>
        currentSender ! failure
      case sSuccess(value) =>
        value match {
          case zSuccess(response: Int) =>
            currentSender ! ResponseMessage(OK, response.toJson)
          case zFailure(error) =>
            error match {
              case errorPersistence: ErrorPersistence => currentSender ! errorPersistence.exception
              case errorVal: ErrorValidacion =>
                currentSender ! ResponseMessage(Conflict, errorVal.msg)
            }
        }
    }
  }

}
