package co.com.alianza.domain.aggregates.contrasenas

import akka.actor.{ActorRef, ActorLogging, Actor}
import co.com.alianza.app.{MainActors, AlianzaActors}
import co.com.alianza.infrastructure.anticorruption.contrasenas.DataAccessAdapter
import co.com.alianza.infrastructure.messages._
import co.com.alianza.persistence.entities.ReglasContrasenas
import co.com.alianza.util.token.Token
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
  import scalaz.std.string._

  // to get `Monoid[String]`

  import scalaz.std.list._

  // to get `Traverse[List]`

  import scalaz.syntax.traverse._

  // to get the `sequence` method

  import scala.concurrent.ExecutionContext

  //implicit val _: ExecutionContext = context.dispatcher

  import co.com.alianza.util.json.MarshallableImplicits._
  import ValdiacionesUsuario._

  implicit val ex: ExecutionContext = MainActors.dataAccesEx


  def receive = {
    case message: InboxMessage => obtenerReglasContrasenas()
    case message: ActualizarReglasContrasenasMessage => actualizarReglasContrasenas(message.toEntityReglasContrasenas)

    case message: CambiarContrasenaMessage =>
      val currentSender = sender()
      val passwordActualAppend = message.pw_actual.concat(AppendPasswordUser.appendUsuariosFiducia)
      val passwordNewAppend = message.pw_nuevo.concat(AppendPasswordUser.appendUsuariosFiducia)
      val CambiarContrasenaFuture = (for {
        usuarioContrasenaActual <- ValidationT(validacionConsultaContrasenaActual(passwordActualAppend, message.idUsuario.get))
        idValReglasContra <- ValidationT(validacionReglasClave(message.pw_nuevo))
        idUsuario <- ValidationT(ActualizarContrasena(passwordNewAppend, usuarioContrasenaActual))
      } yield {
        idUsuario
      }).run

      //resolveFutureValidation(CambiarContrasenaFuture , (response: Int) => response.toJson, currentSender)
      resolveCambiarContrasenaFuture(CambiarContrasenaFuture, currentSender)

    case message: CambiarContrasenaCaducadaMessage =>

      val currentSender = sender()
      val tk_validation = Token.autorizarToken(message.token)

      tk_validation match {
        case true =>
          val us_id = Token.getToken(message.token).getJWTClaimsSet().getCustomClaim("us_id").toString.toInt

          val passwordActualAppend = message.pw_actual.concat(AppendPasswordUser.appendUsuariosFiducia)
          val passwordNewAppend = message.pw_nuevo.concat(AppendPasswordUser.appendUsuariosFiducia)

          DataAccessAdapter.consultarReglasContrasenas().onComplete {
            case sFailure(failure) => currentSender ! failure
            case sSuccess(value) =>
              value match {
                case zSuccess(response: List[ReglasContrasenas]) =>

                  val diasValida: Int = response.filter( r => r.llave == "DIAS_VALIDA").head.valor.toInt
                  val nuevaFecha = new org.joda.time.DateTime().plusDays(diasValida)

                  val CambiarContrasenaFuture = (for {
                    usuarioContrasenaActual <- ValidationT(validacionConsultaContrasenaActual(passwordActualAppend, us_id))
                    idValReglasContra <- ValidationT(validacionReglasClave(message.pw_nuevo))
                    idUsuario <- ValidationT(ActualizarContrasenaYCaducidad(usuarioContrasenaActual, passwordNewAppend, nuevaFecha.getMillis))
                  } yield {
                    idUsuario
                  }).run

                  resolveCambiarContrasenaFuture(CambiarContrasenaFuture, currentSender)

                case zFailure(error) => currentSender ! error
              }
          }

        case false => currentSender ! ResponseMessage(Conflict, tokenValidationFailure)
      }

  }

  private def ActualizarContrasena(pw_nuevo: String, usuario: Option[Usuario]): Future[Validation[ErrorValidacion, Int]] = {
    DataAccessAdapter.ActualizarContrasena(pw_nuevo, usuario.get.id.get).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
  }

  private def ActualizarContrasenaYCaducidad(usuario: Option[Usuario], pw_nuevo: String, caducidad: Long): Future[Validation[ErrorValidacion, Int]] = {
    DataAccessAdapter.ActualizarContrasenaYCaducidad(usuario.get.id.get, pw_nuevo, caducidad).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
  }

  def obtenerReglasContrasenas() = {
    val currentSender = sender()
    val result = DataAccessAdapter.consultarReglasContrasenas()

    result onComplete {
      case sFailure(failure) => currentSender ! failure
      case sSuccess(value) =>
        value match {
          case zSuccess(response: List[ReglasContrasenas]) =>
            currentSender ! ResponseMessage(OK, response.toJson)
          case zFailure(error) => currentSender ! error
        }
    }
  }

  def actualizarReglasContrasenas(reglasContrasenas: List[ReglasContrasenas]) = {
    val currentSender = sender()
    for (regla <- reglasContrasenas) {
      val result = DataAccessAdapter.actualizarReglasContrasenas(regla)
      result onComplete {
        case sFailure(failure) => currentSender ! failure
        case sSuccess(value) =>
          value match {
            case zSuccess(response: Int) =>
              currentSender ! ResponseMessage(OK, response.toJson)
            case zFailure(error) => currentSender ! error
          }
      }
    }

  }

  private def resolveCambiarContrasenaFuture(CambiarContrasenaFuture: Future[Validation[ErrorValidacion, Int]], currentSender: ActorRef) = {
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

  private val tokenValidationFailure = ErrorMessage("409.11", "Token invalido", "El token de caducidad es invalido").toJson

}
