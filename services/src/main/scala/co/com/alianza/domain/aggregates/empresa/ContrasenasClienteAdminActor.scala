package co.com.alianza.domain.aggregates.empresa

import java.sql.Timestamp

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.routing.RoundRobinPool
import co.com.alianza.domain.aggregates.usuarios.{ ErrorPersistence, ErrorValidacion }
import co.com.alianza.infrastructure.anticorruption.ultimasContrasenasClienteAdmin.{ DataAccessAdapter => dataAccessUltimasPwClienteAdmin }
import co.com.alianza.infrastructure.anticorruption.usuariosClienteAdmin.DataAccessAdapter
import co.com.alianza.infrastructure.dto.UsuarioEmpresarialAdmin
import co.com.alianza.infrastructure.messages._
import co.com.alianza.infrastructure.messages.empresa.{ CambiarContrasenaCaducadaClienteAdminMessage, CambiarContrasenaClienteAdminMessage }
import co.com.alianza.persistence.entities.UltimaContrasena
import co.com.alianza.util.clave.Crypto
import co.com.alianza.util.token.Token
import co.com.alianza.util.transformers.ValidationT
import com.typesafe.config.Config
import enumerations.{ AppendPasswordUser, PerfilesUsuario }
import spray.http.StatusCodes._

import scala.concurrent.Future
import scala.util.{ Failure => sFailure, Success => sSuccess }
import scalaz.{ Failure => zFailure, Success => zSuccess }
import scalaz.std.AllInstances._

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
  implicit val conf: Config = context.system.settings.config

  import scalaz._

  // to get `Monoid[String]`

  // to get `Traverse[List]`

  // to get the `sequence` method

  import co.com.alianza.domain.aggregates.empresa.ValidacionesClienteAdmin._
  import co.com.alianza.util.json.MarshallableImplicits._

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
          val CambiarContrasenaFuture = (for {
            usuarioContrasenaActual <- ValidationT(validacionConsultaContrasenaActualClienteAdmin(passwordActualAppend, us_id))
            idValReglasContra <- ValidationT(validacionReglasClave(message.pw_nuevo, us_id, PerfilesUsuario.clienteAdministrador))
            idUsuario <- ValidationT(actualizarContrasena(passwordNewAppend, usuarioContrasenaActual))
            resultGuardarUltimasContrasenas <- ValidationT(guardarUltimaContrasena(us_id, Crypto.hashSha512(passwordNewAppend, message.idUsuario.get)))
          } yield {
            idUsuario
          }).run

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
    dataAccessUltimasPwClienteAdmin.guardarUltimaContrasena(UltimaContrasena(None, idUsuario, uContrasena, new Timestamp(System.currentTimeMillis()))).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
  }

  private def actualizarContrasena(pw_nuevo: String, usuario: Option[UsuarioEmpresarialAdmin]): Future[Validation[ErrorValidacion, Int]] = {
    DataAccessAdapter.actualizarContrasena(pw_nuevo, usuario.get.id).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
  }

  private val tokenValidationFailure = ErrorMessage("409.11", "Token invalido", "El token de caducidad es invalido").toJson

}
