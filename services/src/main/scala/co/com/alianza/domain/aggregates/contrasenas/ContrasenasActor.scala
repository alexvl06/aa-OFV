package co.com.alianza.domain.aggregates.contrasenas

import java.sql.Timestamp

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }

import co.com.alianza.infrastructure.anticorruption.contrasenas.DataAccessAdapter
import co.com.alianza.infrastructure.anticorruption.ultimasContrasenas.{ DataAccessAdapter => DataAccessAdapterUltimaContrasena }
import co.com.alianza.infrastructure.messages._
import co.com.alianza.persistence.entities.{ ReglaContrasena, UltimaContrasena }
import co.com.alianza.util.clave.Crypto
import co.com.alianza.util.token.Token
import spray.http.StatusCodes._

import scalaz.{ Failure => zFailure, Success => zSuccess }
import scala.util.{ Failure => sFailure, Success => sSuccess }
import scala.util.{ Failure, Success }
import co.com.alianza.util.transformers.ValidationT
import co.com.alianza.domain.aggregates.usuarios.{ ErrorPersistence, ErrorValidacion, ValidacionesUsuario }

import scala.concurrent.Future
import scalaz.std.AllInstances._
import co.com.alianza.infrastructure.dto.Usuario
import co.com.alianza.util.FutureResponse
import akka.routing.RoundRobinPool
import com.typesafe.config.Config
import enumerations.{ AppendPasswordUser, PerfilesUsuario }

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

case class ContrasenasActor() extends Actor with ActorLogging {

  import scalaz._
  import scalaz.std.string._

  // to get `Monoid[String]`

  import scalaz.std.list._

  // to get `Traverse[List]`

  import scalaz.syntax.traverse._

  // to get the `sequence` method

  import scala.concurrent.ExecutionContext

  import co.com.alianza.util.json.MarshallableImplicits._
  import ValidacionesUsuario._

  import context.dispatcher

  def receive = {

    //TODO: Verificar si el metodo CambiarContrasenaMessage se está utilizando
    case message: CambiarContrasenaMessage =>
      val currentSender = sender()
      val passwordActualAppend = message.pw_actual.concat(AppendPasswordUser.appendUsuariosFiducia)
      val passwordNewAppend = message.pw_nuevo.concat(AppendPasswordUser.appendUsuariosFiducia)
      val CambiarContrasenaFuture = (for {
        usuarioContrasenaActual <- ValidationT(validacionConsultaContrasenaActual(passwordActualAppend, message.idUsuario.get))
        idValReglasContra <- ValidationT(validacionReglasClave(message.pw_nuevo, message.idUsuario.get, PerfilesUsuario.clienteIndividual))
        idUsuario <- ValidationT(actualizarContrasena(passwordNewAppend, usuarioContrasenaActual))
        resultGuardarUltimasContrasenas <- ValidationT(guardarUltimaContrasena(message.idUsuario.get, Crypto.hashSha512(passwordNewAppend, message.idUsuario.get)))
      } yield {
        idUsuario
      }).run
      resolveCambiarContrasenaFuture(CambiarContrasenaFuture, currentSender)

    //TODO: Verificar si el metodo CambiarContrasenaCaducadaMessage se está utilizando
    case message: CambiarContrasenaCaducadaMessage =>
      val currentSender = sender()
      val tk_validation = Token.autorizarToken(message.token)
      tk_validation match {
        case true =>
          val claim = Token.getToken(message.token).getJWTClaimsSet()
          val us_id = claim.getCustomClaim("us_id").toString.toInt
          val us_tipo = claim.getCustomClaim("us_tipo").toString
          val passwordActualAppend = message.pw_actual.concat(AppendPasswordUser.appendUsuariosFiducia)
          val passwordNewAppend = message.pw_nuevo.concat(AppendPasswordUser.appendUsuariosFiducia)
          val CambiarContrasenaFuture = (for {
            usuarioContrasenaActual <- ValidationT(validacionConsultaContrasenaActual(passwordActualAppend, us_id))
            idValReglasContra <- ValidationT(validacionReglasClave(message.pw_nuevo, us_id, PerfilesUsuario.clienteIndividual))
            idUsuario <- ValidationT(actualizarContrasena(passwordNewAppend, usuarioContrasenaActual))
            resultGuardarUltimasContrasenas <- ValidationT(guardarUltimaContrasena(us_id, Crypto.hashSha512(passwordNewAppend, message.us_id)))
          } yield {
            idUsuario
          }).run
          resolveCambiarContrasenaFuture(CambiarContrasenaFuture, currentSender)
        case false => currentSender ! ResponseMessage(Conflict, tokenValidationFailure)
      }

  }

  private def guardarUltimaContrasena(idUsuario: Int, uContrasena: String): Future[Validation[ErrorValidacion, Int]] = {
    DataAccessAdapterUltimaContrasena.guardarUltimaContrasena(UltimaContrasena(None, idUsuario, uContrasena, new Timestamp(System.currentTimeMillis()))).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
  }

  private def actualizarContrasena(pw_nuevo: String, usuario: Option[Usuario]): Future[Validation[ErrorValidacion, Int]] = {
    DataAccessAdapter.actualizarContrasena(pw_nuevo, usuario.get.id.get).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
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
