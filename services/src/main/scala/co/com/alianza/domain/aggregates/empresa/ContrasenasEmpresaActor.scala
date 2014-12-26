package co.com.alianza.domain.aggregates.empresa

import java.util.Calendar

import akka.actor.{ActorRef, Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool
import co.com.alianza.app.{AlianzaActors, MainActors}
import co.com.alianza.domain.aggregates.usuarios.{MailMessageUsuario, ErrorValidacion}
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial.{DataAccessTranslator, DataAccessAdapter}
import co.com.alianza.infrastructure.dto.PinEmpresa
import co.com.alianza.infrastructure.messages.{UsuarioMessage, ResponseMessage}
import co.com.alianza.infrastructure.messages.empresa.{UsuarioMessageCorreo, ReiniciarContrasenaAgenteEMessage}
import co.com.alianza.microservices.{MailMessage, SmtpServiceClient}
import co.com.alianza.util.token.{PinData, TokenPin}
import co.com.alianza.util.transformers.ValidationT
import com.typesafe.config.Config
import enumerations.EstadosEmpresaEnum
import scalaz.std.AllInstances._
import scala.util.{Failure => sFailure, Success => sSuccess}
import scalaz.{Failure => zFailure, Success => zSuccess}
import co.com.alianza.persistence.entities

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Validation
import spray.http.StatusCodes._

/**
 * Created by S4N on 17/12/14.
 */
class ContrasenasEmpresaActorSupervisor extends Actor with ActorLogging {

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  val contrasenasEmpresaActor = context.actorOf(Props[ContrasenasEmpresaActor].withRouter(RoundRobinPool(nrOfInstances = 2)), "contrasenasEmpresaActor")

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

class ContrasenasEmpresaActor extends Actor with ActorLogging with AlianzaActors {

  import scala.concurrent.ExecutionContext


  implicit val ex: ExecutionContext = MainActors.dataAccesEx
  implicit val sys = context.system
  implicit private val config: Config = MainActors.conf

  import co.com.alianza.domain.aggregates.empresa.ValidacionesAgenteEmpresarial._

  def receive = {

    case message: ReiniciarContrasenaAgenteEMessage =>

      val currentSender = sender()
      val ReiniciarContrasenaAgenteEmpresarialFuture = (for {
        idUsuarioAgenteEmpresarial <- ValidationT(validacionAgenteEmpresarial(message.numIdentificacionAgenteEmpresarial, message.correoUsuarioAgenteEmpresarial, message.tipoIdentiAgenteEmpresarial, message.idClienteAdmin.get))
        idEjecucion <- ValidationT(CambiarEstadoAgenteEmpresarial(idUsuarioAgenteEmpresarial, EstadosEmpresaEnum.pendienteReiniciarContrasena))
      } yield {
        (idUsuarioAgenteEmpresarial, idEjecucion)
      }).run

      resolveReiniciarContrasenaAEFuture(ReiniciarContrasenaAgenteEmpresarialFuture, currentSender, message)

  }

  private def CambiarEstadoAgenteEmpresarial(idUsuarioAgenteEmpresarial: Int, estado: EstadosEmpresaEnum.estadoEmpresa): Future[Validation[ErrorValidacionEmpresa, Int]] = {
    DataAccessAdapter.CambiarEstadoAgenteEmpresarial(idUsuarioAgenteEmpresarial, estado).map(_.leftMap(pe => ErrorPersistenceEmpresa(pe.message, pe)))
  }

  private def resolveReiniciarContrasenaAEFuture(ReiniciarContrasenaAEFuture: Future[Validation[ErrorValidacionEmpresa, (Int, Int)]], currentSender: ActorRef, message: ReiniciarContrasenaAgenteEMessage) = {
    ReiniciarContrasenaAEFuture onComplete {
      case sFailure(failure) => currentSender ! failure
      case sSuccess(value) =>
        value match {
          case zSuccess(responseFutureReiniciarContraAE: (Int, Int)) =>
            if (responseFutureReiniciarContraAE._2 == 1) {

              val fechaActual: Calendar = Calendar.getInstance()
              val tokenPin: PinData = TokenPin.obtenerToken(fechaActual.getTime)

              val pin: PinEmpresa = PinEmpresa(None, responseFutureReiniciarContraAE._1, tokenPin.token, tokenPin.fechaExpiracion, tokenPin.tokenHash.get)
              val pinEmpresaAgenteEmpresarial: entities.PinEmpresa = DataAccessTranslator.translateEntityPinEmpresa(pin)

              val resultCrearPinEmpresaAgenteEmpresarial: Future[Validation[PersistenceException, Int]] = DataAccessAdapter.crearPinEmpresaAgenteEmpresarial(pinEmpresaAgenteEmpresarial)

              resultCrearPinEmpresaAgenteEmpresarial onComplete {
                case sFailure(fail) => currentSender ! fail
                case sSuccess(valueResult) =>
                  valueResult match {
                    case zFailure(fail) => currentSender ! fail
                    case zSuccess(intResult) =>
                      if(intResult == 1){
                        new SmtpServiceClient().send(buildMessage(pin, UsuarioMessageCorreo(message.correoUsuarioAgenteEmpresarial, message.numIdentificacionAgenteEmpresarial, message.tipoIdentiAgenteEmpresarial), "alianza.smtp.templatepin.reiniciarContrasenaEmpresa", "alianza.smtp.asunto.reiniciarContrasenaEmpresa"), (_, _) => Unit)
                        currentSender ! ResponseMessage(Created, "Reinicio de contraseña agente empresarial OK")
                      }
                  }
              }
            }

          case zFailure(error) =>
            error match {
              case errorPersistence: ErrorPersistenceEmpresa => currentSender ! errorPersistence.exception
              case errorVal: ErrorValidacionEmpresa  =>
                currentSender ! ResponseMessage(Conflict, errorVal.msg)
            }
        }
    }
  }

  private def buildMessage(pinEmpresa: PinEmpresa, message: UsuarioMessageCorreo, templateBody: String, asuntoTemp: String) = {
    val body: String = new MailMessageEmpresa(templateBody).getMessagePin(pinEmpresa)
    val asunto: String = config.getString(asuntoTemp)
    //MailMessage(config.getString("alianza.smtp.from"), "sergiopena@seven4n.com", List(), asunto, body, "")
    MailMessage(config.getString("alianza.smtp.from"), "luisaceleita@seven4n.com", List(), asunto, body, "")
    //MailMessage(config.getString("alianza.smtp.from"), message.correo, List(), asunto, body, "")
  }

}
