package co.com.alianza.domain.aggregates.empresa

import java.util.Calendar
import akka.actor.{ ActorRef, Actor, ActorLogging, Props }
import akka.routing.RoundRobinPool
import co.com.alianza.app.{ AlianzaActors, MainActors }
import co.com.alianza.domain.aggregates.usuarios.{ ErrorPersistence, MailMessageUsuario, ErrorValidacion }
import co.com.alianza.exceptions.{ LevelException, AlianzaException, PersistenceException }
import co.com.alianza.infrastructure.anticorruption.ultimasContrasenasAgenteEmpresarial.{ DataAccessAdapter => dataAccessUltimasContrasenasAgente }
import co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial.{ DataAccessAdapter, DataAccessTranslator }
import co.com.alianza.infrastructure.anticorruption.empresa.{ DataAccessAdapter => EmpresaDataAccessAdapter }
import co.com.alianza.infrastructure.dto.{ UsuarioEmpresarial, Configuracion, PinEmpresa }
import co.com.alianza.infrastructure.messages.{ ErrorMessage, UsuarioMessage, ResponseMessage }
import co.com.alianza.infrastructure.messages.empresa._
import co.com.alianza.microservices.{ MailMessage, SmtpServiceClient }
import co.com.alianza.util.token.{ Token, PinData, TokenPin }
import co.com.alianza.util.transformers.ValidationT
import com.typesafe.config.Config
import enumerations._
import enumerations.empresa.EstadosDeEmpresaEnum
import scalaz.std.AllInstances._
import scala.util.{ Failure => sFailure, Success => sSuccess }
import scalaz.{ Failure => zFailure, Success => zSuccess }
import co.com.alianza.persistence.entities
import scala.concurrent.{ ExecutionContext, Future }
import scalaz.Validation
import spray.http.StatusCodes._
import co.com.alianza.util.clave.Crypto
import co.com.alianza.persistence.entities.{ Empresa, ReglasContrasenas, UltimaContrasenaUsuarioAgenteEmpresarial }
import java.sql.Timestamp
import java.util.Date
import co.com.alianza.infrastructure.messages.empresa.CambiarContrasenaAgenteEmpresarialMessage
import co.com.alianza.infrastructure.dto.PinEmpresa
import co.com.alianza.util.transformers.ValidationT
import co.com.alianza.infrastructure.messages.empresa.BloquearDesbloquearAgenteEMessage
import co.com.alianza.infrastructure.dto.Configuracion
import co.com.alianza.microservices.MailMessage
import co.com.alianza.domain.aggregates.usuarios.ErrorPersistence
import akka.routing.RoundRobinPool
import co.com.alianza.infrastructure.messages.empresa.CambiarContrasenaCaducadaAgenteEmpresarialMessage
import co.com.alianza.infrastructure.dto.UsuarioEmpresarial
import co.com.alianza.util.token.PinData
import co.com.alianza.infrastructure.messages.ResponseMessage
import co.com.alianza.infrastructure.messages.empresa.UsuarioMessageCorreo
import co.com.alianza.infrastructure.messages.empresa.ReiniciarContrasenaAgenteEMessage

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
        idEjecucion <- ValidationT(cambiarEstadoAgenteEmpresarial(idUsuarioAgenteEmpresarial, EstadosEmpresaEnum.pendienteReiniciarContrasena))
        numHorasCaducidad <- ValidationT(validacionConsultaTiempoExpiracion())
      } yield {
        (idUsuarioAgenteEmpresarial, idEjecucion, numHorasCaducidad)
      }).run
      resolveReiniciarContrasenaAEFuture(reiniciarContrasenaAgenteEmpresarialFuture, currentSender, message)

    case message: BloquearDesbloquearAgenteEMessage =>
      val currentSender = sender()
      val bloquearDesbloquearAgenteEmpresarialFuture = (for {
        estadoAgenteEmpresarial <- ValidationT(validacionEstadoAgenteEmpresarial(message.numIdentificacionAgenteEmpresarial, message.correoUsuarioAgenteEmpresarial, message.tipoIdentiAgenteEmpresarial, message.idClienteAdmin.get))
        diasCaducidad <- ValidationT(diasCaducidadContrasena())
        idEjecucion <- ValidationT(BloquearDesbloquearAgenteEmpresarial(estadoAgenteEmpresarial, diasCaducidad))
        numHorasCaducidad <- ValidationT(validacionConsultaTiempoExpiracion())
      } yield {
        (estadoAgenteEmpresarial, idEjecucion, numHorasCaducidad)
      }).run
      resolveBloquearDesbloquearAgenteFuture(bloquearDesbloquearAgenteEmpresarialFuture, currentSender, message)

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

  private def guardarUltimaContrasena(idUsuario: Int, uContrasena: String): Future[Validation[ErrorValidacion, Unit]] = {
    dataAccessUltimasContrasenasAgente.guardarUltimaContrasena(UltimaContrasenaUsuarioAgenteEmpresarial(None, idUsuario, uContrasena, new Timestamp(System.currentTimeMillis()))).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
  }

  private def ActualizarContrasena(pw_nuevo: String, usuario: Option[UsuarioEmpresarial]): Future[Validation[ErrorValidacion, Int]] = {
    DataAccessAdapter.actualizarContrasenaAgenteEmpresarial(pw_nuevo, usuario.get.id).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
  }

  private def cambiarEstadoAgenteEmpresarial(idUsuarioAgenteEmpresarial: Int, estado: EstadosEmpresaEnum.estadoEmpresa): Future[Validation[ErrorValidacionEmpresa, Int]] = {
    DataAccessAdapter.cambiarEstadoAgenteEmpresarial(idUsuarioAgenteEmpresarial, estado).map(_.leftMap(pe => ErrorPersistenceEmpresa(pe.message, pe)))
  }

  private def diasCaducidadContrasena(): Future[Validation[ErrorValidacionEmpresa, Option[ReglasContrasenas]]] = {
    co.com.alianza.infrastructure.anticorruption.contrasenas.DataAccessAdapter.obtenerRegla("DIAS_VALIDA").map(_.leftMap(pe => ErrorPersistenceEmpresa(pe.message, pe)))
  }

  private def BloquearDesbloquearAgenteEmpresarial(idUsuarioAgenteEmpresarial: (Int, Int), diasCaducidad: Option[ReglasContrasenas]): Future[Validation[ErrorValidacionEmpresa, Int]] = {
    val fechaCaducada = Calendar.getInstance()
    fechaCaducada.setTime(new Date())
    fechaCaducada.add(Calendar.DAY_OF_YEAR, (diasCaducidad.getOrElse(ReglasContrasenas("", "0")).valor.toInt * -1))
    val timestamp = new Timestamp(fechaCaducada.getTimeInMillis)
    val estadoNuevo = if (idUsuarioAgenteEmpresarial._2 == EstadosEmpresaEnum.bloqueContraseña.id || idUsuarioAgenteEmpresarial._2 == EstadosEmpresaEnum.activo.id || idUsuarioAgenteEmpresarial._2 == EstadosEmpresaEnum.pendienteActivacion.id || idUsuarioAgenteEmpresarial._2 == EstadosEmpresaEnum.pendienteReiniciarContrasena.id) { EstadosEmpresaEnum.bloqueadoPorAdmin } else { EstadosEmpresaEnum.pendienteReiniciarContrasena }
    DataAccessAdapter.cambiarBloqueoDesbloqueoAgenteEmpresarial(idUsuarioAgenteEmpresarial._1, estadoNuevo, timestamp).map(_.leftMap(pe => ErrorPersistenceEmpresa(pe.message, pe)))
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
                      if (intResult == 1) {
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
              case errorVal: ErrorValidacionEmpresa =>
                currentSender ! ResponseMessage(Conflict, errorVal.msg)
            }
        }
    }
  }

  private def resolveBloquearDesbloquearAgenteFuture(bloquearDesbloquearAgenteFuture: Future[Validation[ErrorValidacionEmpresa, ((Int, Int), Int, Configuracion)]], currentSender: ActorRef, message: BloquearDesbloquearAgenteEMessage) = {
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

              val pin: PinEmpresa = PinEmpresa(None, responseFutureBloquearDesbloquearAgenteFuture._1._1, tokenPin.token, tokenPin.fechaExpiracion, tokenPin.tokenHash.get, UsoPinEmpresaEnum.usoReinicioContrasena.id)
              val pinEmpresaAgenteEmpresarial: entities.PinEmpresa = DataAccessTranslator.translateEntityPinEmpresa(pin)

              val resultCrearPinEmpresaAgenteEmpresarial = for {
                idResultEliminarPinesEmpresaAnteriores <- DataAccessAdapter.eliminarPinEmpresaReiniciarAnteriores(responseFutureBloquearDesbloquearAgenteFuture._1._1, UsoPinEmpresaEnum.usoReinicioContrasena.id)
                idResultGuardarPinEmpresa <- DataAccessAdapter.crearPinEmpresaAgenteEmpresarial(pinEmpresaAgenteEmpresarial)
                correoEnviado <- new SmtpServiceClient().send(buildMessage(responseFutureBloquearDesbloquearAgenteFuture._3.valor.toInt, pin, UsuarioMessageCorreo(message.correoUsuarioAgenteEmpresarial, message.numIdentificacionAgenteEmpresarial, message.tipoIdentiAgenteEmpresarial), "alianza.smtp.templatepin.reiniciarContrasenaEmpresa", "alianza.smtp.asunto.reiniciarContrasenaEmpresa"), (_, _) => Unit)
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

  private def buildMessage(numHorasCaducidad: Int, pinEmpresa: PinEmpresa, message: UsuarioMessageCorreo, templateBody: String, asuntoTemp: String) = {
    val body: String = new MailMessageEmpresa(templateBody).getMessagePin(pinEmpresa, numHorasCaducidad)
    val asunto: String = config.getString(asuntoTemp)
    MailMessage(config.getString("alianza.smtp.from"), "luisaceleita@seven4n.com", List(), asunto, body, "")
    //MailMessage(config.getString("alianza.smtp.from"), message.correo, List(), asunto, body, "")
  }

  private val tokenValidationFailure = ErrorMessage("409.11", "Token invalido", "El token de caducidad es invalido").toJson

}
