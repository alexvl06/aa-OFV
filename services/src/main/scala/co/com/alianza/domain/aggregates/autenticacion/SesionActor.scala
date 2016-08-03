package co.com.alianza.domain.aggregates.autenticacion

import java.security.MessageDigest

import akka.actor.{ Props, _ }
import akka.cluster.Cluster
import akka.pattern.ask
import akka.util.Timeout
import co.com.alianza.infrastructure.dto.Empresa
import co.com.alianza.infrastructure.messages._
import co.com.alianza.util.token.AesUtil
import com.typesafe.config.Config
import enumerations.CryptoAesParameters

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import scala.util.{ Failure, Success, Try }

/**
 * Define los mensajes generados por el actor que maneja las sessiones
 */
object SesionActorSupervisor {

  case class SesionUsuarioCreada()

  case class SesionUsuarioNoCreada()

  case class SesionUsuarioValidada()

  case class SesionUsuarioNoValidada()

}

case class SesionActorSupervisor() extends Actor with ActorLogging {

  import SesionActorSupervisor._
  import context.dispatcher

  implicit val conf: Config = context.system.settings.config
  implicit val timeout: Timeout = 10.seconds

  private val cluster = Cluster.get(context.system)

  def receive = {

    //
    // Auth events
    //

    // When an user authenticates
    case message: CrearSesionUsuario => crearSesion(message.token, message.tiempoExpiracion, message.empresa)

    // When a user logout
    case message: InvalidarSesion => invalidarSesion(message.token)

    // When system validate token
    case message: ValidarSesion => validarSesion(message)

    //
    // Internal messages
    //

    case message: DeleteSession =>
      context.actorSelection("akka://alianza-fid-auth-service/user/sesionActorSupervisor/" + message.actorName).resolveOne().onComplete {
        case Success(session) => session ! ExpirarSesion()
        case Failure(ex) =>
      }

    case EncontrarActor(actorName) =>
      val currentSender = sender()
      context.actorSelection("akka://alianza-fid-auth-service/user/sesionActorSupervisor/" + actorName).resolveOne().onComplete {
        case Success(actor) => currentSender ! ActorEncontrado(actor)
        case Failure(ex) => currentSender ! ActorNoEncontrado
      }

    case BuscarSesion(token) => buscarSesion(token)

    case CrearEmpresaActor(empresa) =>
      log info "Creando empresa Actor: " + s"empresa${empresa.id}"
      sender ! context.actorOf(EmpresaActor.props(empresa), s"empresa${empresa.id}")

    case ObtenerEmpresaSesionActorId(empresaId) => obtenerEmpresaSesionActorId(empresaId)
  }

  private def validarSesion(message: ValidarSesion): Unit = {
    val currentSender = sender()
    val decryptedToken = AesUtil.desencriptarToken(message.token)
    val actorName = generarNombreSesionActor(decryptedToken)
    val response = context.actorOf(Props(new BuscadorActorCluster("sesionActorSupervisor"))) ? BuscarActor(actorName)
    response.onComplete {
      case Some(sesionActor: ActorRef) =>
        val actualizacion = sesionActor ? ActualizarSesion(); actualizacion.map { case _ => currentSender ! SesionUsuarioValidada() }
      case None => currentSender ! SesionUsuarioNoValidada()
    }
  }

  private def buscarSesion(token: String): Unit = {
    var decryptedToken = AesUtil.desencriptarToken(token)
    val currentSender: ActorRef = sender()
    val actorName: String = generarNombreSesionActor(decryptedToken)
    val future: Future[Any] = context.actorOf(Props(new BuscadorActorCluster("sesionActorSupervisor"))) ? BuscarActor(actorName)
    future.onComplete {
      case Failure(error) => log error ("Error al obtener la sesión", error)
      case Success(actor) => currentSender ! actor
    }
  }

  private def invalidarSesion(token: String): Unit = {
    var decryptedToken = AesUtil.desencriptarToken(token)
    val actorName = generarNombreSesionActor(decryptedToken)
    ClusterUtil.obtenerNodos(cluster) foreach { member =>
      context.actorSelection(RootActorPath(member.address) / "user" / "sesionActorSupervisor") ! DeleteSession(actorName)
    }
  }

  def crearSesion(token: String, expiration: Int, empresa: Option[Empresa]): Unit = {
    var decryptedToken = AesUtil.desencriptarToken(token)
    val name: String = generarNombreSesionActor(decryptedToken)
    Try {
      context.actorOf(SesionActor.props(expiration, empresa), name)
      log.info("Creando sesion de usuario. Tiempo de expiracion: " + expiration + " minutos.")
      sender() ! SesionUsuarioCreada()
    }.recover {
      case e => e.printStackTrace(); sender() ! SesionUsuarioNoCreada()
    }.get
  }

  private def generarNombreSesionActor(token: String): String = {
    MessageDigest.getInstance("MD5").digest(token.split("\\.")(2).getBytes).map(b => String.format("%02X", java.lang.Byte.valueOf(b))).mkString("")
  } // Actor's name

  private def obtenerEmpresaSesionActorId(empresaId: Int) = {
    val currentSender = sender()
    context.actorOf(Props(new BuscadorActorCluster("sesionActorSupervisor"))) ? BuscarActor(s"empresa$empresaId") onComplete {
      case Failure(error) =>
        log error ("/*/ Error al obtener la sesion de empresa ", error)
      case Success(actor) =>
        currentSender ! actor
    }
  }
}

class SesionActor(expiracionSesion: Int, empresa: Option[Empresa]) extends Actor with ActorLogging {

  implicit val _: ExecutionContext = context dispatcher
  implicit val timeout: Timeout = 120 seconds

  private val cluster = Cluster.get(context.system)

  // System scheduler instance
  private val scheduler: Scheduler = context.system.scheduler

  // Kill task
  private var killTask: Cancellable = scheduler.scheduleOnce(expiracionSesion.minutes, self, ExpirarSesion())

  // PostStop function
  override def postStop(): Unit = {
    killTask.cancel()
  }

  var empresaActor: Option[ActorRef] = None

  inicializaEmpresaActor()

  // Receive function
  override def receive = {

    case msg: ActualizarSesion =>
      log.debug("Actualizando sesión de usuario: " + self.path.name)
      killTask.cancel()
      killTask = scheduler.scheduleOnce(expiracionSesion.minutes, self, ExpirarSesion())
      sender ! true

    case msg: ExpirarSesion =>
      log.info("Eliminando sesión de usuario: " + self.path.name)
      if (empresaActor.isDefined) {
        empresaActor.get ! RemoverSesion(self)
      }
      context.stop(self)

    case empresaActor: ActorRef =>
      this.empresaActor = Some(empresaActor)
      empresaActor ! AgregarSesion(self)

    case ObtenerEmpresaActor => sender ! empresaActor

  }

  private def inicializaEmpresaActor() = {
    if (empresa.isDefined) {
      context.actorOf(Props(new BuscadorActorCluster("sesionActorSupervisor"))) ? BuscarActor(s"empresa${empresa.get.id}") map {
        case Some(empresaActor: ActorRef) => self ! empresaActor
        case None =>
          context.parent ? CrearEmpresaActor(empresa.get) map {
            case empresaActor: ActorRef => self ! empresaActor
            case None => log error s"Sesión empresa '${empresa.get.id}' no creada!!"
          }
      }
    }
  }

}

object SesionActor {

  def props(expirationTime: Int, empresa: Option[Empresa]): Props = {

    Props(new SesionActor(expirationTime, empresa))
  }

}

case class DeleteSession(actorName: String)

case class ActualizarEmpresa(empresa: Empresa)

case class CrearEmpresaActor(empresa: Empresa)

case object ObtenerEmpresaActor

case object CerrarSesiones
