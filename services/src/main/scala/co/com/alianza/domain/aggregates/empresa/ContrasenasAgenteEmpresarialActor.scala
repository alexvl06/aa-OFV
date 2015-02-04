package co.com.alianza.domain.aggregates.empresa

import java.util.Calendar

import akka.actor.{ActorRef, Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool
import co.com.alianza.app.{AlianzaActors, MainActors}
import co.com.alianza.domain.aggregates.usuarios.{ErrorPersistence, MailMessageUsuario, ErrorValidacion}
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.ultimasContrasenasAgenteEmpresarial.{DataAccessAdapter => dataAccessUltimasContrasenasAgente}
import co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial.{DataAccessTranslator, DataAccessAdapter}
import co.com.alianza.infrastructure.dto.{UsuarioEmpresarial, Configuracion, PinEmpresa}
import co.com.alianza.infrastructure.messages.{UsuarioMessage, ResponseMessage}
import co.com.alianza.infrastructure.messages.empresa.{CambiarContrasenaAgenteEmpresarialMessage, UsuarioMessageCorreo, ReiniciarContrasenaAgenteEMessage}
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
import co.com.alianza.util.clave.Crypto
import co.com.alianza.persistence.entities.UltimaContrasenaUsuarioAgenteEmpresarial
import java.sql.Timestamp

/**
 * Created by S4N on 17/12/14.
 */
class ContrasenasAgenteEmpresarialActorSupervisor extends Actor with ActorLogging {

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  val contrasenasEmpresaActor = context.actorOf(Props[ContrasenasAgenteEmpresarialActor].withRouter(RoundRobinPool(nrOfInstances = 2)), "contrasenasAgenteEmpresarialActor")

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
class ContrasenasAgenteEmpresarialActor extends Actor with ActorLogging with AlianzaActors {

  import scala.concurrent.ExecutionContext


  implicit val ex: ExecutionContext = MainActors.dataAccesEx
  implicit val sys = context.system
  implicit private val config: Config = MainActors.conf

  import co.com.alianza.domain.aggregates.empresa.ValidacionesAgenteEmpresarial._

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

  def receive = {

    case message: ReiniciarContrasenaAgenteEMessage =>

      val currentSender = sender()
      val reiniciarContrasenaAgenteEmpresarialFuture = (for {
        idUsuarioAgenteEmpresarial <- ValidationT(validacionAgenteEmpresarial(message.numIdentificacionAgenteEmpresarial, message.correoUsuarioAgenteEmpresarial, message.tipoIdentiAgenteEmpresarial, message.idClienteAdmin.get))
        idEjecucion <- ValidationT(CambiarEstadoAgenteEmpresarial(idUsuarioAgenteEmpresarial, EstadosEmpresaEnum.pendienteReiniciarContrasena))
        numHorasCaducidad <- ValidationT(validacionConsultaTiempoExpiracion())
      } yield {
        (idUsuarioAgenteEmpresarial, idEjecucion, numHorasCaducidad)
      }).run

      resolveReiniciarContrasenaAEFuture(reiniciarContrasenaAgenteEmpresarialFuture, currentSender, message)

    case message: CambiarContrasenaAgenteEmpresarialMessage =>
        val currentSender = sender()
        val passwordActualAppend = message.pw_actual.concat(AppendPasswordUser.appendUsuariosFiducia)
        val passwordNewAppend = message.pw_nuevo.concat(AppendPasswordUser.appendUsuariosFiducia)
        val CambiarContrasenaFuture = (for {
          usuarioContrasenaActual <- ValidationT(validacionConsultaContrasenaActualAgenteEmpresarial(passwordActualAppend, message.idUsuario.get))
          idValReglasContra <- ValidationT(validacionReglasClave(message.pw_nuevo, message.idUsuario.get, PerfilesUsuario.agenteEmpresarial))
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
    dataAccessUltimasContrasenasAgente.guardarUltimaContrasena(UltimaContrasenaUsuarioAgenteEmpresarial(None, idUsuario , uContrasena, new Timestamp(System.currentTimeMillis()))).map(_.leftMap( pe => ErrorPersistence(pe.message, pe)))
  }

  private def ActualizarContrasena(pw_nuevo: String, usuario: Option[UsuarioEmpresarial]): Future[Validation[ErrorValidacion, Int]] = {
    DataAccessAdapter.actualizarContrasenaAgenteEmpresarial(pw_nuevo, usuario.get.id).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
  }

  private def CambiarEstadoAgenteEmpresarial(idUsuarioAgenteEmpresarial: Int, estado: EstadosEmpresaEnum.estadoEmpresa): Future[Validation[ErrorValidacionEmpresa, Int]] = {
    DataAccessAdapter.CambiarEstadoAgenteEmpresarial(idUsuarioAgenteEmpresarial, estado).map(_.leftMap(pe => ErrorPersistenceEmpresa(pe.message, pe)))
  }

  private def resolveReiniciarContrasenaAEFuture(ReiniciarContrasenaAEFuture: Future[Validation[ErrorValidacionEmpresa, (Int, Int, Configuracion)]], currentSender: ActorRef, message: ReiniciarContrasenaAgenteEMessage) = {
    ReiniciarContrasenaAEFuture onComplete {
      case sFailure(failure) => currentSender ! failure
      case sSuccess(value) =>
        value match {
          case zSuccess(responseFutureReiniciarContraAE: (Int, Int, Configuracion)) =>
            if (responseFutureReiniciarContraAE._2 == 1) {

              val fechaActual: Calendar = Calendar.getInstance()
              fechaActual.add(Calendar.HOUR_OF_DAY, responseFutureReiniciarContraAE._3.valor.toInt)
              val tokenPin: PinData = TokenPin.obtenerToken(fechaActual.getTime)

              val pin: PinEmpresa = PinEmpresa(None, responseFutureReiniciarContraAE._1, tokenPin.token, tokenPin.fechaExpiracion, tokenPin.tokenHash.get, UsoPinEmpresaEnum.usoReinicioContrasena.id)
              val pinEmpresaAgenteEmpresarial: entities.PinEmpresa = DataAccessTranslator.translateEntityPinEmpresa(pin)

              val resultCrearPinEmpresaAgenteEmpresarial = for {
                idResultEliminarPinesEmpresaAnteriores <- DataAccessAdapter.eliminarPinEmpresaReiniciarAnteriores(responseFutureReiniciarContraAE._1, UsoPinEmpresaEnum.usoReinicioContrasena.id)
                idResultGuardarPinEmpresa <- DataAccessAdapter.crearPinEmpresaAgenteEmpresarial(pinEmpresaAgenteEmpresarial)
              } yield {
                idResultGuardarPinEmpresa
              }

              resultCrearPinEmpresaAgenteEmpresarial onComplete {
                case sFailure(fail) => currentSender ! fail
                case sSuccess(valueResult) =>
                  valueResult match {
                    case zFailure(fail) => currentSender ! fail
                    case zSuccess(intResult) =>
                      if(intResult == 1) {
                        new SmtpServiceClient().send(buildMessage(responseFutureReiniciarContraAE._3.valor.toInt, pin, UsuarioMessageCorreo(message.correoUsuarioAgenteEmpresarial, message.numIdentificacionAgenteEmpresarial, message.tipoIdentiAgenteEmpresarial), "alianza.smtp.templatepin.reiniciarContrasenaEmpresa", "alianza.smtp.asunto.reiniciarContrasenaEmpresa"), (_, _) => Unit)
                        currentSender ! ResponseMessage(Created, "Reinicio de contraseña agente empresarial OK")
                      } else {
                        log.info("Error... Al momento de guardar el pin empresa")
                      }
                  }
              }
            } else {
              log.info("Error... Al momento de cambiar el estado a pendiente de reinicio de contraseña")
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

  private def buildMessage(numHorasCaducidad : Int, pinEmpresa: PinEmpresa, message: UsuarioMessageCorreo, templateBody: String, asuntoTemp: String) = {
    val body: String = new MailMessageEmpresa(templateBody).getMessagePin(pinEmpresa, numHorasCaducidad)
    val asunto: String = config.getString(asuntoTemp)
    //MailMessage(config.getString("alianza.smtp.from"), "sergiopena@seven4n.com", List(), asunto, body, "")
    MailMessage(config.getString("alianza.smtp.from"), "luisaceleita@seven4n.com",  List(), asunto, body, "")
    //MailMessage(config.getString("alianza.smtp.from"), "davidmontano@seven4n.com", List(), asunto, body, "")
    //MailMessage(config.getString("alianza.smtp.from"), message.correo, List(), asunto, body, "")
  }

}
