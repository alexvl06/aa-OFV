package co.com.alianza.domain.aggregates.empresa

import java.sql.Timestamp
import java.util.Calendar

import akka.actor.{ Actor, ActorLogging, Props }
import akka.routing.RoundRobinPool

import co.com.alianza.domain.aggregates.usuarios.ErrorValidacion
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.dto.{ Configuracion, UsuarioEmpresarialEstado }
import co.com.alianza.infrastructure.messages.empresa._
import co.com.alianza.microservices.{ MailMessage, SmtpServiceClient }
import co.com.alianza.persistence.entities.{ UsuarioEmpresarial, PinAgente, UsuarioEmpresarialEmpresa }
import co.com.alianza.util.token.{ PinData, TokenPin }
import co.com.alianza.util.transformers.ValidationT
import com.typesafe.config.Config
import enumerations.{ PerfilesAgente, UsoPinEmpresaEnum }

import scalaz.std.AllInstances._
import scalaz.{ Success => zSuccess }
import scala.concurrent.Future
import scalaz.Validation
import co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial.DataAccessAdapter
import co.com.alianza.util.FutureResponse
import co.com.alianza.util.json.MarshallableImplicits._

/**
 * Created by S4N on 17/12/14.
 */
class AgenteEmpresarialActorSupervisor()(implicit config: Config) extends Actor with ActorLogging with FutureResponse {

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  val agenteEmpresarialActor = context.actorOf(Props(AgenteEmpresarialActor()).withRouter(RoundRobinPool(nrOfInstances = 2)), "agenteEmpresarialActor")

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

case class AgenteEmpresarialActor()(implicit config: Config) extends Actor with ActorLogging with FutureResponse {

  import co.com.alianza.domain.aggregates.usuarios.ValidacionesUsuario.errorValidacion
  import co.com.alianza.domain.aggregates.usuarios.ValidacionesUsuario.toErrorValidation
  import co.com.alianza.domain.aggregates.usuarios.ValidacionesUsuario.toErrorValidationCorreo
  import co.com.alianza.domain.aggregates.usuarios.ValidacionesUsuario.validacionConsultaTiempoExpiracion
  import co.com.alianza.domain.aggregates.empresa.ValidacionesAgenteEmpresarial.validarUsuarioClienteAdmin
  import co.com.alianza.domain.aggregates.empresa.ValidacionesAgenteEmpresarial.validarEstadoEmpresa
  import co.com.alianza.domain.aggregates.empresa.ValidacionesAgenteEmpresarial.validarUsuarioAgente
  import co.com.alianza.domain.aggregates.empresa.ValidacionesAgenteEmpresarial.validarUsuarioAgenteNit
  import co.com.alianza.domain.aggregates.empresa.ValidacionesAgenteEmpresarial.validacionEstadoActualizacionAgenteEmpresarial

  import context.dispatcher

  def receive = {

    case message: CrearAgenteMessage => crearAgente(message)

    case message: ActualizarAgenteMessage => actualizarAgente(message)

    case message: GetAgentesEmpresarialesMessage => obtenerAgentesEmpresariales(message)
  }

  private def crearAgente(message: CrearAgenteMessage) = {
    val currentSender = sender()
    val perfiles: List[Int] = PerfilesAgente.agente.id :: Nil
    val usuarioEntity: UsuarioEmpresarial = message.toEntityUsuarioAgenteEmpresarial()
    val asunto: String = "alianza.smtp.asunto.creacionAgenteEmpresarial"
    val plantilla: String = "alianza.smtp.templatepin.creacionAgenteEmpresarial"
    val future: Future[Validation[ErrorValidacion, Int]] = (for {
      _ <- ValidationT(validarUsuarioClienteAdmin(message.nit, message.usuario))
      _ <- ValidationT(validarUsuarioAgenteNit(message.nit, message.usuario))
      idAgente <- ValidationT(toErrorValidation(DataAccessAdapter.crearAgenteEmpresarial(usuarioEntity)))
      _ <- ValidationT(toErrorValidation(DataAccessAdapter.asociarPerfiles(idAgente, perfiles)))
      empresa <- ValidationT(toErrorValidation(DataAccessAdapter.obtenerEmpresaPorNit(message.nit)))
      _ <- ValidationT(toErrorValidation(DataAccessAdapter.asociarAgenteConEmpresa(UsuarioEmpresarialEmpresa(empresa.get.id, idAgente))))
      confTiempo <- ValidationT(validacionConsultaTiempoExpiracion())
      pinUsuario <- ValidationT(obtenerPinUsuario(confTiempo, idAgente))
      _ <- ValidationT(toErrorValidation(DataAccessAdapter.crearPinEmpresaAgenteEmpresarial(pinUsuario)))
      _ <- ValidationT(enviarCorreoPin(pinUsuario, confTiempo: Configuracion, plantilla, asunto, message.usuario, message.correo))
    } yield {
      idAgente
    }).run
    resolveFutureValidation(future, (response: Int) => response.toJson, errorValidacion, currentSender)
  }

  /**
   * Actualizar agente
   * @param message
   */
  private def actualizarAgente(message: ActualizarAgenteMessage) = {
    val currentSender = sender()
    val nit = message.nit.get
    val future = (for {
      estadoEmpresa <- ValidationT(validarEstadoEmpresa(nit))
      usuarioAdmin <- ValidationT(validarUsuarioClienteAdmin(nit, message.usuario))
      existeUsuario <- ValidationT(validarUsuarioAgente(message.id, nit, message.usuario))
      estadoAgente <- ValidationT(validacionEstadoActualizacionAgenteEmpresarial(message.id))
      actualizar <- ValidationT(toErrorValidation(DataAccessAdapter.actualizarAgenteEmpresarial(message.id, message.usuario,
        message.correo, message.nombreUsuario, message.cargo, message.descripcion)))
    } yield actualizar).run
    resolveFutureValidation(future, (response: Int) => response.toJson, errorValidacion, currentSender)
  }

  /**
   * Obtener agentes empresariales
   * @param message
   */
  private def obtenerAgentesEmpresariales(message: GetAgentesEmpresarialesMessage) = {
    val currentSender = sender()
    val future: Future[Validation[PersistenceException, List[UsuarioEmpresarialEstado]]] =
      DataAccessAdapter.obtenerUsuariosBusqueda(message.toGetUsuariosEmpresaBusquedaRequest)
    resolveFutureValidation(future, (response: List[UsuarioEmpresarialEstado]) => response.toJson, errorValidacion, currentSender)
  }

  /**
   * Obtener el pin
   * @param configuracionTiempo
   * @param idUsuario
   * @return
   */
  private def obtenerPinUsuario(configuracionTiempo: Configuracion, idUsuario: Int): Future[Validation[ErrorValidacion, PinAgente]] = Future {
    val fechaActual = Calendar.getInstance()
    val usoPin = UsoPinEmpresaEnum.creacionAgenteEmpresarial.id
    fechaActual.add(Calendar.HOUR_OF_DAY, configuracionTiempo.valor.toInt)
    val tokenPin: PinData = TokenPin.obtenerToken(fechaActual.getTime)
    val pin: PinAgente = PinAgente(None, idUsuario, tokenPin.token, new Timestamp(tokenPin.fechaExpiracion.getTime), tokenPin.tokenHash.get, usoPin)
    zSuccess(pin)
  }

  /**
   * Enviar correo
   * @param pin
   * @param confTiempo
   * @param plantilla
   * @param asunto
   * @param usuario
   * @param correo
   * @return
   */
  private def enviarCorreoPin(pin: PinAgente, confTiempo: Configuracion, plantilla: String, asunto: String, usuario: String,
    correo: String): Future[Validation[ErrorValidacion, Int]] = {
    val message = buildMessage(pin, confTiempo.valor.toInt, plantilla, asunto, usuario, correo)
    toErrorValidationCorreo(new SmtpServiceClient()(context.system).send(message, (_, _) => 1))
  }

  /**
   * Construir el mensaje de correo
   * @param pinEmpresa
   * @param numHorasCaducidad
   * @param templateBody
   * @param asuntoTemp
   * @param usuario
   * @param correo
   * @return
   */
  private def buildMessage(pinEmpresa: PinAgente, numHorasCaducidad: Int, templateBody: String, asuntoTemp: String, usuario: String, correo: String) = {
    val body: String = new MailMessageEmpresa(templateBody).getMessagePinCreacionAgente(pinEmpresa, numHorasCaducidad, usuario)
    val asunto: String = config.getString(asuntoTemp)
    MailMessage(config.getString("alianza.smtp.from"), correo, List(), asunto, body, "")
  }

}
