package co.com.alianza.domain.aggregates.empresa

import java.util.Calendar

import akka.actor.{ActorRef, Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool
import co.com.alianza.app.{AlianzaActors, MainActors}
import co.com.alianza.domain.aggregates.usuarios.{ErrorPersistence, MailMessageUsuario, ErrorValidacion}
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuariosClienteAdmin.{DataAccessAdapter, DataAccessTranslator }
import co.com.alianza.infrastructure.dto.{UsuarioEmpresarialAdmin, Configuracion, PinEmpresa}
import co.com.alianza.infrastructure.messages.{CambiarContrasenaMessage, UsuarioMessage, ResponseMessage}
import co.com.alianza.infrastructure.messages.empresa.{CambiarContrasenaClienteAdminMessage, UsuarioMessageCorreo, ReiniciarContrasenaAgenteEMessage}
import co.com.alianza.microservices.{MailMessage, SmtpServiceClient}
import co.com.alianza.util.token.{PinData, TokenPin}
import co.com.alianza.util.transformers.ValidationT
import com.typesafe.config.Config
import enumerations.{PerfilesUsuario, AppendPasswordUser, UsoPinEmpresaEnum, EstadosEmpresaEnum}
import scalaz.std.AllInstances._
import scala.util.{Failure => sFailure, Success => sSuccess}
import scalaz.{Failure => zFailure, Success => zSuccess}
import co.com.alianza.persistence.entities

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Validation
import spray.http.StatusCodes._
import co.com.alianza.domain.aggregates.usuarios.ValidacionesUsuario._
import co.com.alianza.infrastructure.messages.CambiarContrasenaMessage
import akka.routing.RoundRobinPool
import co.com.alianza.util.transformers.ValidationT
import co.com.alianza.util.clave.Crypto
import co.com.alianza.infrastructure.anticorruption.ultimasContrasenasClienteAdmin.{DataAccessAdapter => dataAccessUltimasPwClienteAdmin}
import co.com.alianza.infrastructure.anticorruption.usuariosClienteAdmin.DataAccessAdapter
import co.com.alianza.persistence.entities.{UltimaContrasenaUsuarioEmpresarialAdmin, UltimaContrasena}
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

/** *
  * Actor que se encarga de procesar los mensajes relacionados con la administración de contraseñas de los usuarios emopresa (Cliente Administrador y Agente Empresarial)
  */
class ContrasenasClienteAdminActor extends Actor with ActorLogging with AlianzaActors {

  import scala.concurrent.ExecutionContext


  implicit val ex: ExecutionContext = MainActors.dataAccesEx
  implicit val sys = context.system
  implicit private val config: Config = MainActors.conf

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

  import co.com.alianza.domain.aggregates.empresa.ValidacionesClienteAdmin._

  def receive = {

    case message: CambiarContrasenaClienteAdminMessage =>
      val currentSender = sender()
      val passwordActualAppend = message.pw_actual.concat(AppendPasswordUser.appendUsuariosFiducia)
      val passwordNewAppend = message.pw_nuevo.concat(AppendPasswordUser.appendUsuariosFiducia)
      val CambiarContrasenaFuture = (for {
        usuarioContrasenaActual <- ValidationT(validacionConsultaContrasenaActualClienteAdmin(passwordActualAppend, message.idUsuario.get))
        idValReglasContra <- ValidationT(validacionReglasClave(message.pw_nuevo, message.idUsuario.get, PerfilesUsuario.clienteIndividual))
        idUsuario <- ValidationT(ActualizarContrasena(passwordNewAppend, usuarioContrasenaActual))
        resultGuardarUltimasContrasenas <- ValidationT(guardarUltimaContrasena(message.idUsuario.get, Crypto.hashSha512(passwordNewAppend)))
      } yield {
        idUsuario
      }).run

      resolveCambiarContrasenaFuture(CambiarContrasenaFuture, currentSender)

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

  private def guardarUltimaContrasena(idUsuario: Int, uContrasena: String): Future[Validation[ErrorValidacion, Unit]] = {
    dataAccessUltimasPwClienteAdmin.guardarUltimaContrasena(UltimaContrasenaUsuarioEmpresarialAdmin(None, idUsuario , uContrasena, new Timestamp(System.currentTimeMillis()))).map(_.leftMap( pe => ErrorPersistence(pe.message, pe)))
  }

  private def ActualizarContrasena(pw_nuevo: String, usuario: Option[UsuarioEmpresarialAdmin]): Future[Validation[ErrorValidacion, Int]] = {
    DataAccessAdapter.actualizarContrasena(pw_nuevo, usuario.get.id).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
  }


}
