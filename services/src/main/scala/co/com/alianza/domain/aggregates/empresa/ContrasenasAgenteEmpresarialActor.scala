package co.com.alianza.domain.aggregates.empresa

import java.sql.Timestamp
import java.util.{ Calendar, Date }

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.routing.RoundRobinPool
import co.com.alianza.domain.aggregates.usuarios.{ ErrorPersistence, ErrorValidacion }
import co.com.alianza.infrastructure.anticorruption.ultimasContrasenasAgenteEmpresarial.{ DataAccessAdapter => dataAccessUltimasContrasenasAgente }
import co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial.{ DataAccessAdapter, DataAccessTranslator }
import co.com.alianza.infrastructure.dto.{ Configuracion, UsuarioEmpresarial }
import co.com.alianza.infrastructure.messages.{ ErrorMessage, ResponseMessage }
import co.com.alianza.infrastructure.messages.empresa.{ BloquearDesbloquearAgenteEMessage, CambiarContrasenaAgenteEmpresarialMessage, CambiarContrasenaCaducadaAgenteEmpresarialMessage, ReiniciarContrasenaAgenteEMessage, UsuarioMessageCorreo }
import co.com.alianza.microservices.{ MailMessage, SmtpServiceClient }
import co.com.alianza.persistence.entities
import co.com.alianza.persistence.entities.{ UltimaContrasena, PinAgente, ReglaContrasena }
import co.com.alianza.util.clave.Crypto
import co.com.alianza.util.token.{ PinData, Token, TokenPin }
import co.com.alianza.util.transformers.ValidationT
import com.typesafe.config.Config
import enumerations._
import portal.transaccional.autenticacion.service.web.contrasena.{ CambiarEstadoAgente, ReiniciarContrasenaAgente }
import spray.http.StatusCodes._

import scala.concurrent.Future
import scala.util.{ Failure => sFailure, Success => sSuccess }
import scalaz.{ Failure => zFailure, Success => zSuccess }
import scalaz.std.AllInstances._
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

/**
 * *
 * Actor que se encarga de procesar los mensajes relacionados con la administración de contraseñas de los usuarios emopresa (Cliente Administrador y Agente Empresarial)
 */
class ContrasenasAgenteEmpresarialActor extends Actor with ActorLogging {
  implicit val config: Config = context.system.settings.config

  import co.com.alianza.domain.aggregates.empresa.ValidacionesAgenteEmpresarial._

  import scalaz._

  // to get `Monoid[String]`

  // to get `Traverse[List]`

  // to get the `sequence` method

  //implicit val _: ExecutionContext = context.dispatcher

  import co.com.alianza.util.json.MarshallableImplicits._

  def receive = {

    case message: CambiarEstadoAgente =>
      val currentSender = sender()
      val bloquearDesbloquearAgenteEmpresarialFuture = (for {
        estadoAgenteEmpresarial <- ValidationT(validacionEstadoAgenteEmpresarial(message.numIdentificacionAgenteEmpresarial,
          message.correoUsuarioAgenteEmpresarial, message.tipoIdentiAgenteEmpresarial, message.idClienteAdmin.get))
        diasCaducidad <- ValidationT(diasCaducidadContrasena())
        idEjecucion <- ValidationT(BloquearDesbloquearAgenteEmpresarial(estadoAgenteEmpresarial, diasCaducidad))
        numHorasCaducidad <- ValidationT(validacionConsultaTiempoExpiracion())
      } yield {
        (estadoAgenteEmpresarial, idEjecucion, numHorasCaducidad)
      }).run
      resolveBloquearDesbloquearAgenteFuture(bloquearDesbloquearAgenteEmpresarialFuture, currentSender, message)

      private def resolveBloquearDesbloquearAgenteFuture(bloquearDesbloquearAgenteFuture: Future[Validation[ErrorValidacionEmpresa, ((Int, Int), Int, Configuracion)]], currentSender: ActorRef, message: CambiarEstadoAgente) = {
      bloquearDesbloquearAgenteFuture onComplete {
        case sFailure(failure) => currentSender ! failure
        case sSuccess(value) =>
          value match {
            case zSuccess(responseFutureBloquearDesbloquearAgenteFuture: ((Int, Int), Int, Configuracion)) =>
              if (responseFutureBloquearDesbloquearAgenteFuture._1._2 != EstadosEmpresaEnum.bloqueadoPorAdmin.id) {
                currentSender ! ResponseMessage(OK, "La operacion de Bloqueo sobre el Agente empresarial se ha realizado con exio")
              } else {
                val fechaActual: Calendar = Calendar.getInstance()
                fechaActual.add(Calendar.HOUR_OF_DAY, responseFutureBloquearDesbloquearAgenteFuture._3.valor.toInt)
                val tokenPin: PinData = TokenPin.obtenerToken(fechaActual.getTime)
                val fecha = new Timestamp(tokenPin.fechaExpiracion.getTime)
                val pin: PinAgente = PinAgente(None, responseFutureBloquearDesbloquearAgenteFuture._1._1, tokenPin.token, fecha, tokenPin.tokenHash.get, UsoPinEmpresaEnum.usoReinicioContrasena.id)
                val resultCrearPinEmpresaAgenteEmpresarial = for {
                  idResultEliminarPinesEmpresaAnteriores <- DataAccessAdapter.eliminarPinEmpresaReiniciarAnteriores(responseFutureBloquearDesbloquearAgenteFuture._1._1, UsoPinEmpresaEnum.usoReinicioContrasena.id)
                  idResultGuardarPinEmpresa <- DataAccessAdapter.crearPinEmpresaAgenteEmpresarial(pin)
                  correoEnviado <- new SmtpServiceClient()(context.system).send(buildMessage(responseFutureBloquearDesbloquearAgenteFuture._3.valor.toInt, pin,
                    UsuarioMessageCorreo(message.correoUsuarioAgenteEmpresarial, message.numIdentificacionAgenteEmpresarial, message.tipoIdentiAgenteEmpresarial),
                    "alianza.smtp.templatepin.reiniciarContrasenaEmpresa", "alianza.smtp.asunto.reiniciarContrasenaEmpresa"), (_, _) => Unit)
                } yield { correoEnviado }

                resultCrearPinEmpresaAgenteEmpresarial onComplete {
                  case sFailure(fail) => currentSender ! fail
                  case sSuccess(valueResult) =>
                    valueResult match {
                      case zFailure(fail) => currentSender ! fail
                      case zSuccess(intResult) =>
                        currentSender ! ResponseMessage(OK, "La operacion de Desbloqueo sobre el Agente empresarial se ha realizado con exio")
                    }
                }
              }
            case zFailure(error) =>
              error match {
                case errorPersistence: ErrorPersistenceEmpresa => currentSender ! errorPersistence.exception
                case errorVal: ErrorValidacionEmpresa =>
                  currentSender ! ResponseMessage(Conflict, errorVal.msg)
              }
          }
      }
    }

    case message: CambiarContrasenaAgenteEmpresarialMessage =>
      val currentSender = sender()
      val passwordActualAppend = message.pw_actual.concat(AppendPasswordUser.appendUsuariosFiducia)
      val passwordNewAppend = message.pw_nuevo.concat(AppendPasswordUser.appendUsuariosFiducia)
      val CambiarContrasenaFuture = (for {
        usuarioContrasenaActual <- ValidationT(validacionConsultaContrasenaActualAgenteEmpresarial(passwordActualAppend, message.idUsuario.get))
        idValReglasContra <- ValidationT(validacionReglasClave(message.pw_nuevo, message.idUsuario.get, PerfilesUsuario.agenteEmpresarial))
        idUsuario <- ValidationT(ActualizarContrasena(passwordNewAppend, usuarioContrasenaActual))
        resultGuardarUltimasContrasenas <- ValidationT(guardarUltimaContrasena(message.idUsuario.get, Crypto.hashSha512(passwordNewAppend, message.idUsuario.get)))
      } yield {
        idUsuario
      }).run
      resolveCambiarContrasenaFuture(CambiarContrasenaFuture, currentSender)

    case message: CambiarContrasenaCaducadaAgenteEmpresarialMessage =>
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
            usuarioContrasenaActual <- ValidationT(validacionConsultaContrasenaActualAgenteEmpresarial(passwordActualAppend, us_id))
            idValReglasContra <- ValidationT(validacionReglasClave(message.pw_nuevo, us_id, PerfilesUsuario.agenteEmpresarial))
            idUsuario <- ValidationT(ActualizarContrasena(passwordNewAppend, usuarioContrasenaActual))
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
              case errorVal: ErrorValidacion => currentSender ! ResponseMessage(Conflict, errorVal.msg)
            }
        }
    }
  }

  private def guardarUltimaContrasena(idUsuario: Int, uContrasena: String): Future[Validation[ErrorValidacion, Int]] = {
    dataAccessUltimasContrasenasAgente.guardarUltimaContrasena(UltimaContrasena(None, idUsuario, uContrasena, new Timestamp(System.currentTimeMillis()))).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
  }

  private def ActualizarContrasena(pw_nuevo: String, usuario: Option[UsuarioEmpresarial]): Future[Validation[ErrorValidacion, Int]] = {
    DataAccessAdapter.actualizarContrasenaAgenteEmpresarial(pw_nuevo, usuario.get.id).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
  }

  private def cambiarEstadoAgenteEmpresarial(idUsuarioAgenteEmpresarial: Int, estado: EstadosEmpresaEnum.estadoEmpresa): Future[Validation[ErrorValidacionEmpresa, Int]] = {
    DataAccessAdapter.cambiarEstadoAgenteEmpresarial(idUsuarioAgenteEmpresarial, estado).map(_.leftMap(pe => ErrorPersistenceEmpresa(pe.message, pe)))
  }

  private def diasCaducidadContrasena(): Future[Validation[ErrorValidacionEmpresa, Option[ReglaContrasena]]] = {
    co.com.alianza.infrastructure.anticorruption.contrasenas.DataAccessAdapter.obtenerRegla("DIAS_VALIDA").map(_.leftMap(pe => ErrorPersistenceEmpresa(pe.message, pe)))
  }

  private def BloquearDesbloquearAgenteEmpresarial(idUsuarioAgenteEmpresarial: (Int, Int), diasCaducidad: Option[ReglaContrasena]): Future[Validation[ErrorValidacionEmpresa, Int]] = {
    val fechaCaducada = Calendar.getInstance()
    fechaCaducada.setTime(new Date())
    fechaCaducada.add(Calendar.DAY_OF_YEAR, (diasCaducidad.getOrElse(ReglaContrasena("", "0")).valor.toInt * -1))
    val timestamp = new Timestamp(fechaCaducada.getTimeInMillis)
    val estadoNuevo = if (idUsuarioAgenteEmpresarial._2 == EstadosEmpresaEnum.bloqueContraseña.id || idUsuarioAgenteEmpresarial._2 == EstadosEmpresaEnum.activo.id || idUsuarioAgenteEmpresarial._2 == EstadosEmpresaEnum.pendienteActivacion.id || idUsuarioAgenteEmpresarial._2 == EstadosEmpresaEnum.pendienteReiniciarContrasena.id) { EstadosEmpresaEnum.bloqueadoPorAdmin } else { EstadosEmpresaEnum.pendienteReiniciarContrasena }
    DataAccessAdapter.cambiarBloqueoDesbloqueoAgenteEmpresarial(idUsuarioAgenteEmpresarial._1, estadoNuevo, timestamp).map(_.leftMap(pe => ErrorPersistenceEmpresa(pe.message, pe)))
  }

  private def buildMessage(numHorasCaducidad: Int, pinEmpresa: PinAgente, message: UsuarioMessageCorreo, templateBody: String, asuntoTemp: String) = {
    val body: String = new MailMessageEmpresa(templateBody).getMessagePin(pinEmpresa, numHorasCaducidad)
    val asunto: String = config.getString(asuntoTemp)
    MailMessage(config.getString("alianza.smtp.from"), "luisaceleita@seven4n.com", List(), asunto, body, "")
    //MailMessage(config.getString("alianza.smtp.from"), message.correo, List(), asunto, body, "")
  }

  private val tokenValidationFailure = ErrorMessage("409.11", "Token invalido", "El token de caducidad es invalido").toJson

}
