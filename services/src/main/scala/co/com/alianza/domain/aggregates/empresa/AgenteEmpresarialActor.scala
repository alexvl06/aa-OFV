package co.com.alianza.domain.aggregates.empresa

import java.sql.Timestamp
import java.util.Calendar

import akka.actor.{ActorRef, Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool
import co.com.alianza.app.{AlianzaActors, MainActors}
import co.com.alianza.domain.aggregates.empresa.ValidacionesAgenteEmpresarial._
import co.com.alianza.domain.aggregates.usuarios.{ErrorPersistence, MailMessageUsuario, ErrorValidacion}
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial.{DataAccessTranslator, DataAccessAdapter}
import co.com.alianza.infrastructure.dto.{UsuarioEmpresarialEstado, Configuracion, UsuarioEmpresarial, PinEmpresa}
import co.com.alianza.infrastructure.messages.{UsuarioMessage, ResponseMessage}
import co.com.alianza.infrastructure.messages.empresa.{GetAgentesEmpresarialesMessage, CrearAgenteEMessage, UsuarioMessageCorreo, ReiniciarContrasenaAgenteEMessage}
import co.com.alianza.microservices.{MailMessage, SmtpServiceClient}
import co.com.alianza.persistence.entities.{UsuarioEmpresarialEmpresa, Empresa, IpsUsuario, UltimaContrasena}
import co.com.alianza.util.clave.Crypto
import co.com.alianza.util.token.{PinData, TokenPin}
import co.com.alianza.util.transformers.ValidationT
import com.typesafe.config.Config
import enumerations.{TipoIdentificacion, UsoPinEmpresaEnum, EstadosEmpresaEnum, PerfilesAgente}
import scalaz.std.AllInstances._
import scala.util.{Failure => sFailure, Success => sSuccess}
import scalaz.{Failure => zFailure, Success => zSuccess}
import co.com.alianza.persistence.entities

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Validation
import spray.http.StatusCodes._
import co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial.DataAccessAdapter
import co.com.alianza.util.FutureResponse
import co.com.alianza.util.json.MarshallableImplicits._

/**
 * Created by S4N on 17/12/14.
 */
class AgenteEmpresarialActorSupervisor extends Actor with ActorLogging {

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  val agenteEmpresarialActor = context.actorOf(Props[AgenteEmpresarialActor].withRouter(RoundRobinPool(nrOfInstances = 2)), "agenteEmpresarialActor")

  def receive = {
    case message: Any => agenteEmpresarialActor forward message
  }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }

}

class AgenteEmpresarialActor extends Actor with ActorLogging with AlianzaActors with FutureResponse {

  import scala.concurrent.ExecutionContext


  implicit val ex: ExecutionContext = MainActors.dataAccesEx
  implicit val sys = context.system
  implicit private val config: Config = MainActors.conf

  def receive = {
//toUsuarioEmpresarialEmpresa(empresa, idUsuarioAgenteEmpresarial)
    case message: CrearAgenteEMessage => {
      val currentSender = sender()
      val usuarioCreadoFuture: Future[Validation[ErrorValidacion, Int]] = (for {
        clienteAdmin <- ValidationT(validarUsuarioClienteAdmin(message.nit, message.usuario))
        idUsuarioAgenteEmpresarial <- ValidationT(crearAgenteEmpresarial(message))
        resultAsociarPerfiles <- ValidationT(asociarPerfiles(idUsuarioAgenteEmpresarial))
        empresa <- ValidationT(obtenerEmpresaPorNit(message.nit))
        resultAsociarEmpresa <- ValidationT(asociarAgenteEmpresarialConEmpresa(empresa.get.id, idUsuarioAgenteEmpresarial))
      } yield {
        idUsuarioAgenteEmpresarial
      }).run
      resolveCrearAgenteEmpresarialFuture(usuarioCreadoFuture, message, currentSender)
    }

    case message: GetAgentesEmpresarialesMessage =>
      val currentSender = sender()
      val future: Future[Validation[PersistenceException, List[UsuarioEmpresarialEstado]]] = DataAccessAdapter.obtenerUsuariosBusqueda(message.toGetUsuariosEmpresaBusquedaRequest)
      resolveFutureValidation(future, (response: List[UsuarioEmpresarialEstado]) => response.toJson, currentSender)
  }

  private def crearAgenteEmpresarial(message: CrearAgenteEMessage) : Future[Validation[ErrorValidacion, Int]] =
    DataAccessAdapter.crearAgenteEmpresarial(message.toEntityUsuarioAgenteEmpresarial()).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))

  private def asociarPerfiles(idUsuarioAgenteEmpresarial: Int) : Future[Validation[ErrorValidacion, List[Int]]] =
    DataAccessAdapter.asociarPerfiles(idUsuarioAgenteEmpresarial, PerfilesAgente.agente.id :: Nil).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))

  private def obtenerEmpresaPorNit(nit: String) : Future[Validation[ErrorValidacion, Option[Empresa]]] =
    DataAccessAdapter.obtenerEmpresaPorNit(nit).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))

  private def asociarAgenteEmpresarialConEmpresa(idEmpresa: Int, idAgente: Int) : Future[Validation[ErrorValidacion, Int]] =
    DataAccessAdapter.asociarAgenteEmpresarialConEmpresa(UsuarioEmpresarialEmpresa(idEmpresa, idAgente)).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))

  private def resolveCrearAgenteEmpresarialFuture(crearAgenteEmpresarialFuture: Future[Validation[ErrorValidacion, Int]], message : CrearAgenteEMessage, currentSender: ActorRef) {
    crearAgenteEmpresarialFuture onComplete {
      case sFailure(failure) =>
        currentSender ! failure
      case sSuccess(value) =>
        value match {

          case zSuccess(idUsuarioAgenteEmpresarial: Int) =>{

            val validacionConsulta = validacionConsultaTiempoExpiracion()

            validacionConsulta onComplete {
              case sFailure(failure) => currentSender ! failure
              case sSuccess(value) =>
                value match {
                  case zSuccess(responseConf: Configuracion) =>

                    enviarCorreo(responseConf.valor.toInt, message, idUsuarioAgenteEmpresarial, currentSender)

                  case zFailure(error) =>
                    error match {
                      case errorPersistence: ErrorPersistence => currentSender ! errorPersistence.exception
                      case errorFail => currentSender ! ResponseMessage(Conflict, errorFail.msg)
                    }
                }
            }

          }
          case zFailure(error) =>
            error match {
              case errorValidacion: ErrorValidacion  =>
                currentSender ! ResponseMessage(Conflict, errorValidacion.msg)
              case errorPersistence: ErrorPersistence => {
                currentSender ! ResponseMessage(Conflict, s"Usuario ya registrado para el NIT: ${message.nit}")
              }
              case unknownError @ _  => {
                currentSender ! ResponseMessage(InternalServerError, "Se ha producido un error inesperado.")
              }
            }

        }
    }
  }

  private def toIpsUsuarioArray(ips : Array[String], idUsuarioAgenteEmpresarial : Int) : Array[IpsUsuario] = ips.map(ip => IpsUsuario(idUsuarioAgenteEmpresarial, ip))

  private def enviarCorreo( numHorasCaducidad: Int, message : CrearAgenteEMessage, idUsuarioAgenteEmpresarial : Int, currentSender: ActorRef) = {
    val fechaActual: Calendar = Calendar.getInstance()
    fechaActual.add(Calendar.HOUR_OF_DAY, numHorasCaducidad)
    val tokenPin: PinData = TokenPin.obtenerToken(fechaActual.getTime)

    val pin: PinEmpresa = PinEmpresa(None, idUsuarioAgenteEmpresarial, tokenPin.token, tokenPin.fechaExpiracion, tokenPin.tokenHash.get, UsoPinEmpresaEnum.creacionAgenteEmpresarial.id)
    val pinEmpresaAgenteEmpresarial: entities.PinEmpresa = DataAccessTranslator.translateEntityPinEmpresa(pin)

    val resultCrearPinEmpresaAgenteEmpresarial = for {
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
            if(intResult == 1){
              new SmtpServiceClient().send(buildMessage(numHorasCaducidad, pin, UsuarioMessageCorreo(message.correo, message.nit, TipoIdentificacion.NIT.id), "alianza.smtp.templatepin.creacionAgenteEmpresarial", "alianza.smtp.asunto.creacionAgenteEmpresarial", message.usuario), (_, _) => Unit)
              currentSender ! ResponseMessage(Created, idUsuarioAgenteEmpresarial.toString)
            }
        }
    }
  }

  private def buildMessage(numHorasCaducidad : Int, pinEmpresa: PinEmpresa, message: UsuarioMessageCorreo, templateBody: String, asuntoTemp: String, usuario : String) = {
    val body: String = new MailMessageEmpresa(templateBody).getMessagePinCreacionAgente(pinEmpresa, numHorasCaducidad, usuario)
    val asunto: String = config.getString(asuntoTemp)
    MailMessage(config.getString("alianza.smtp.from"), "luisaceleita@seven4n.com",  List(), asunto, body, "")
    //MailMessage(config.getString("alianza.smtp.from"), message.correo, List(), asunto, body, "")
  }

}
