package co.com.alianza.domain.aggregates.empresa

import java.util.Calendar

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.routing.RoundRobinPool

import co.com.alianza.domain.aggregates.usuarios.{ ErrorPersistence, ErrorValidacion }
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuariosClienteAdmin.{ DataAccessAdapter, DataAccessTranslator }
import co.com.alianza.infrastructure.dto.{ Configuracion, PinEmpresa, UsuarioEmpresarialAdmin }
import co.com.alianza.infrastructure.messages._
import co.com.alianza.infrastructure.messages.empresa.{ CambiarContrasenaCaducadaClienteAdminMessage, CambiarContrasenaClienteAdminMessage, UsuarioMessageCorreo }
import co.com.alianza.util.token.{ PinData, Token, TokenPin }
import co.com.alianza.util.transformers.ValidationT
import com.typesafe.config.Config
import enumerations.{ AppendPasswordUser, EstadosEmpresaEnum, PerfilesUsuario, UsoPinEmpresaEnum }

import scalaz.std.AllInstances._
import scala.util.{ Failure => sFailure, Success => sSuccess }
import scalaz.{ Failure => zFailure, Success => zSuccess }
import co.com.alianza.persistence.entities

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.Validation
import spray.http.StatusCodes._
import co.com.alianza.domain.aggregates.usuarios.ValidacionesUsuario._
import akka.routing.RoundRobinPool
import co.com.alianza.util.transformers.ValidationT
import co.com.alianza.util.clave.Crypto
import co.com.alianza.infrastructure.anticorruption.ultimasContrasenasClienteAdmin.{ DataAccessAdapter => dataAccessUltimasPwClienteAdmin }
import co.com.alianza.infrastructure.anticorruption.usuariosClienteAdmin.DataAccessAdapter
import co.com.alianza.persistence.entities.{ UltimaContrasena, UltimaContrasenaUsuarioEmpresarialAdmin }
import java.sql.Timestamp

/**
 * Created by S4N on 17/12/14.
 */
class ContrasenasClienteAdminActorSupervisor extends Actor with ActorLogging {

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  val contrasenasEmpresaActor = context.actorOf(Props[ContrasenasClienteAdminActor].withRouter(RoundRobinPool(nrOfInstances = 2)), "contrasenasClienteAdminActor")

  def receive = {

    case message: Any =>
      contrasenasEmpresaActor forward message

  }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }

}

/**
 * *
 * Actor que se encarga de procesar los mensajes relacionados con la administración de contraseñas de los usuarios emopresa (Cliente Administrador y Agente Empresarial)
 */
class ContrasenasClienteAdminActor extends Actor with ActorLogging {

  import context.dispatcher
  implicit val conf: Config = context.system.settings.config

  import scalaz._
  import scalaz.std.string._

  // to get `Monoid[String]`

  import scalaz.std.list._

  // to get `Traverse[List]`

  import scalaz.syntax.traverse._

  // to get the `sequence` method

  import scala.concurrent.ExecutionContext

  import co.com.alianza.util.json.MarshallableImplicits._

  import co.com.alianza.domain.aggregates.empresa.ValidacionesClienteAdmin._

  def receive = {

    case message: CambiarContrasenaClienteAdminMessage =>
      val currentSender = sender()
      val passwordActualAppend = message.pw_actual.concat(AppendPasswordUser.appendUsuariosFiducia)
      val passwordNewAppend = message.pw_nuevo.concat(AppendPasswordUser.appendUsuariosFiducia)
      val CambiarContrasenaFuture = (for {
        usuarioContrasenaActual <- ValidationT(validacionConsultaContrasenaActualClienteAdmin(passwordActualAppend, message.idUsuario.get))
        idValReglasContra <- ValidationT(validacionReglasClave(message.pw_nuevo, message.idUsuario.get, PerfilesUsuario.clienteAdministrador))
        idUsuario <- ValidationT(actualizarContrasena(passwordNewAppend, usuarioContrasenaActual))
        resultGuardarUltimasContrasenas <- ValidationT(guardarUltimaContrasena(message.idUsuario.get, Crypto.hashSha512(passwordNewAppend, message.idUsuario.get)))
      } yield {
        idUsuario
      }).run

      resolveCambiarContrasenaFuture(CambiarContrasenaFuture, currentSender)

    case message: CambiarContrasenaCaducadaClienteAdminMessage =>
      val currentSender = sender()
      val tk_validation = Token.autorizarToken(message.token)
      tk_validation match {
        case true =>
          val claim = Token.getToken(message.token).getJWTClaimsSet()
          val us_id = claim.getCustomClaim("us_id").toString.toInt
          val us_tipo = claim.getCustomClaim("us_tipo").toString
          val passwordActualAppend = message.pw_actual.concat(AppendPasswordUser.appendUsuariosFiducia)
          val passwordNewAppend = message.pw_nuevo.concat(AppendPasswordUser.appendUsuariosFiducia)
          val CambiarContrasenaFuture = (
            for {
              usuarioContrasenaActual <- ValidationT(validacionConsultaContrasenaActualClienteAdmin(passwordActualAppend, us_id))
              idValReglasContra <- ValidationT(validacionReglasClave(message.pw_nuevo, us_id, PerfilesUsuario.clienteAdministrador))
              idUsuario <- ValidationT(actualizarContrasena(passwordNewAppend, usuarioContrasenaActual))
              resultGuardarUltimasContrasenas <- ValidationT(guardarUltimaContrasena(us_id, Crypto.hashSha512(passwordNewAppend, message.idUsuario.get)))
            } yield {
              idUsuario
            }
          ).run

          resolveCambiarContrasenaFuture(CambiarContrasenaFuture, currentSender)

        case false => currentSender ! ResponseMessage(Conflict, tokenValidationFailure)
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

  private def guardarUltimaContrasena(idUsuario: Int, uContrasena: String): Future[Validation[ErrorValidacion, Int]] = {
    dataAccessUltimasPwClienteAdmin.guardarUltimaContrasena(UltimaContrasenaUsuarioEmpresarialAdmin(None, idUsuario, uContrasena, new Timestamp(System.currentTimeMillis()))).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
  }

  private def actualizarContrasena(pw_nuevo: String, usuario: Option[UsuarioEmpresarialAdmin]): Future[Validation[ErrorValidacion, Int]] = {
    DataAccessAdapter.actualizarContrasena(pw_nuevo, usuario.get.id).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
  }

  private val tokenValidationFailure = ErrorMessage("409.11", "Token invalido", "El token de caducidad es invalido").toJson

}
