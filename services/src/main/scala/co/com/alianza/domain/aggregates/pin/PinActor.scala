package co.com.alianza.domain.aggregates.pin

import java.sql.Timestamp

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }

import co.com.alianza.domain.aggregates.usuarios.{ ErrorPersistence, ErrorValidacion, ValidacionesUsuario }
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuarios.{ DataAccessAdapter => DataAdapterUsuario }
import co.com.alianza.infrastructure.anticorruption.pin.{ DataAccessAdapter => pDataAccessAdapter }
import co.com.alianza.infrastructure.anticorruption.ultimasContrasenas.{ DataAccessAdapter => DataAdapterContrasena }
import co.com.alianza.infrastructure.anticorruption.contrasenas.{ DataAccessAdapter => DataAccessAdapterContrasena }
import co.com.alianza.infrastructure.anticorruption.usuarios.{ DataAccessAdapter => uDataAccessAdapter }
import co.com.alianza.infrastructure.dto.PinUsuario
import co.com.alianza.infrastructure.messages.PinMessages._
import co.com.alianza.infrastructure.messages.ResponseMessage
import co.com.alianza.persistence.entities.{ IpsUsuario, UltimaContrasena }
import co.com.alianza.util.FutureResponse
import co.com.alianza.util.clave.Crypto
import co.com.alianza.util.transformers.ValidationT
import spray.http.StatusCodes._

import scala.util.{ Failure, Success, Try }
import scalaz.std.AllInstances._
import scalaz.{ Failure => zFailure, Success => zSuccess }
import scala.concurrent.{ ExecutionContext, Future }
import scalaz.Validation
import akka.routing.RoundRobinPool
import enumerations.{ AppendPasswordUser, EstadosUsuarioEnum, PerfilesUsuario }

class PinActorSupervisor extends Actor with ActorLogging {
  import akka.actor.SupervisorStrategy._
  import akka.actor.OneForOneStrategy

  val pinActor = context.actorOf(Props[PinActor].withRouter(RoundRobinPool(nrOfInstances = 2)), "pinActor")
  val pinUsuarioAgenteEmpresarialActor = context.actorOf(Props[PinUsuarioAgenteEmpresarialActor].withRouter(RoundRobinPool(nrOfInstances = 2)), "pinUsuarioAgenteEmpresarialActor")
  val pinUsuarioEmpresarialAdminActor = context.actorOf(Props[PinUsuarioEmpresarialAdminActor].withRouter(RoundRobinPool(nrOfInstances = 2)), "pinUsuarioEmpresarialAdminActor")

  def receive = {
    case message: Any =>
      pinActor forward message
  }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }
}

class PinActor(implicit val system: ActorSystem) extends Actor with ActorLogging with AlianzaActors with FutureResponse {

  import system.dispatcher

  import ValidacionesUsuario._

  def receive = {

    case message: ValidarPin => validarPin(message)

    case message: CambiarContrasena => cambiarContrasena(message)
  }

  /**
   * Validar pin
   * @param message
   */
  private def validarPin(message: ValidarPin) = {
    val currentSender = sender()
    val tokenHash: String = message.tokenHash
    val funcionalidad: Int = message.funcionalidad.get
    val future: Future[Validation[PersistenceException, Option[PinUsuario]]] = pDataAccessAdapter.obtenerPin(tokenHash)
    resolveFutureValidation(future, (response: Option[PinUsuario]) => PinUtil.validarPin(response, funcionalidad), errorValidacion, currentSender)
  }

  /**
   * Cambiar contraseña
   * @param message
   */
  private def cambiarContrasena(message: CambiarContrasena) = {
    val currentSender = sender()
    val ip: Option[String] = message.ip
    val contrasena: String = message.pw
    val tokenHash: String = message.tokenHash
    val estadoActivo: Int = EstadosUsuarioEnum.activo.id
    val passwordAppend: String = contrasena.concat(AppendPasswordUser.appendUsuariosFiducia)
    val future = (for {
      pin <- ValidationT(toErrorValidation(pDataAccessAdapter.obtenerPin(tokenHash)))
      pinValidacion <- ValidationT(PinUtil.validarPinFuture(pin))
      rvalidacionClave <- ValidationT(validacionReglasClave(message.pw, pinValidacion.idUsuario, PerfilesUsuario.clienteIndividual))
      rCambiarPss <- ValidationT(toErrorValidation(DataAccessAdapterContrasena.actualizarContrasena(passwordAppend, pinValidacion.idUsuario)))
      resultGuardarUltimasContrasenas <- ValidationT(toErrorValidation(DataAdapterContrasena.guardarUltimaContrasena(ultimaContrasena(contrasena, pinValidacion.idUsuario))))
      rCambiarEstado <- ValidationT(toErrorValidation(uDataAccessAdapter.actualizarEstadoUsuario(pinValidacion.idUsuario, estadoActivo)))
      guardarIp <- ValidationT(guardarIpUsuario(ip, pinValidacion.idUsuario))
      idResult <- ValidationT(toErrorValidation(pDataAccessAdapter.eliminarPin(pinValidacion.tokenHash)))
    } yield idResult).run
    resolveFutureValidation(future, (response: Int) => ResponseMessage(OK), errorValidacion, currentSender)
  }

  /**
   * Obtener el objeto ultimacontrasena
   * @param contrasena
   * @param idUsuario
   * @return
   */
  private def ultimaContrasena(contrasena: String, idUsuario: Int) = {
    val contrasenaCrypto: String = Crypto.hashSha512(contrasena, idUsuario)
    UltimaContrasena(None, idUsuario, contrasenaCrypto, new Timestamp(System.currentTimeMillis()))
  }

  /**
   * Guardar ip equipo de confianza
   * @param ip
   * @param idUsuario
   * @return
   */
  private def guardarIpUsuario(ip: Option[String], idUsuario: Int): Future[Validation[ErrorValidacion, String]] = {
    ip match {
      case Some(ip: String) =>
        val ipUsuario: IpsUsuario = new IpsUsuario(idUsuario, ip)
        toErrorValidation(DataAdapterUsuario.agregarIpUsuario(ipUsuario))
      case _ => Future(zSuccess("OK"))
    }
  }

}
