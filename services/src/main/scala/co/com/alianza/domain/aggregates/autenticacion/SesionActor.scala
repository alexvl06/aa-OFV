package co.com.alianza.domain.aggregates.autenticacion

import java.security.MessageDigest

import akka.actor.{ Props, _ }
import akka.cluster.Cluster
import akka.pattern.ask
import akka.util.Timeout
import co.com.alianza.infrastructure.dto.{ Empresa, HorarioEmpresa }
import co.com.alianza.infrastructure.messages._
import co.com.alianza.util.token.AesUtil
import com.typesafe.config.Config
import enumerations.CryptoAesParameters

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{ Failure, Success }


case class SesionActorSupervisor() extends Actor
    with ActorLogging {

  import context.dispatcher

  implicit val conf: Config = context.system.settings.config
  implicit val timeout: Timeout = 10 seconds

  private val cluster = Cluster.get(context.system)

  def receive = {

    //
    // Auth events
    //

    // When an user authenticates
    case message: CrearSesionUsuario => crearSesion(message.token, message.tiempoExpiracion, message.empresa, message.horario)

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
      val currentSender = sender
      context.actorSelection("akka://alianza-fid-auth-service/user/sesionActorSupervisor/" + actorName).resolveOne().onComplete {
        case Success(actor) => currentSender ! ActorEncontrado(actor)
        case Failure(ex) => currentSender ! ActorNoEncontrado
      }

    case BuscarSesion(token) => buscarSesion(token)

    case CrearEmpresaActor(empresa, horario) =>
      log info "Creando empresa Actor: " + s"empresa${empresa.id}"
      sender ! context.actorOf(EmpresaActor.props(empresa, horario), s"empresa${empresa.id}")

    case ObtenerEmpresaSesionActorId(empresaId) => obtenerEmpresaSesionActorId(empresaId)
  }

  private def validarSesion(message: ValidarSesion): Unit = {
    var util = new AesUtil(CryptoAesParameters.KEY_SIZE, CryptoAesParameters.ITERATION_COUNT);
    var decryptedToken = util.decrypt(CryptoAesParameters.SALT, CryptoAesParameters.IV, CryptoAesParameters.PASSPHRASE, message.token);
    val currentSender = sender()
    val actorName = generarNombreSesionActor(decryptedToken)
    context.actorOf(Props(new BuscadorActorCluster("sesionActorSupervisor"))) ? BuscarActor(actorName) map {
      case Some(sesionActor: ActorRef) => sesionActor ? ActualizarSesion() onComplete { case _ => currentSender ! true }
      case None => currentSender ! false
    }
  }

  private def buscarSesion(token: String) = {
    var util = new AesUtil(CryptoAesParameters.KEY_SIZE, CryptoAesParameters.ITERATION_COUNT);
    var decryptedToken = util.decrypt(CryptoAesParameters.SALT, CryptoAesParameters.IV, CryptoAesParameters.PASSPHRASE, token);
    val currentSender = sender()
    val actorName = generarNombreSesionActor(decryptedToken)
    context.actorOf(Props(new BuscadorActorCluster("sesionActorSupervisor"))) ? BuscarActor(actorName) onComplete {
      case Failure(error) => log error ("Error al obtener la sesión", error)
      case Success(actor) => currentSender ! actor
    }
  }

  private def invalidarSesion(token: String): Unit = {
    var util = new AesUtil(CryptoAesParameters.KEY_SIZE, CryptoAesParameters.ITERATION_COUNT)
    var decryptedToken = util.decrypt(CryptoAesParameters.SALT, CryptoAesParameters.IV, CryptoAesParameters.PASSPHRASE, token)
    val actorName = generarNombreSesionActor(decryptedToken)
    ClusterUtil.obtenerNodos( cluster) foreach { member =>
      context.actorSelection(RootActorPath(member.address) / "user" / "sesionActorSupervisor") ! DeleteSession(actorName)
    }
  }

  private def crearSesion(token: String, expiration: Int, empresa: Option[Empresa], horario: Option[HorarioEmpresa]) = {
    var util = new AesUtil(CryptoAesParameters.KEY_SIZE, CryptoAesParameters.ITERATION_COUNT)
    var decryptedToken = util.decrypt(CryptoAesParameters.SALT, CryptoAesParameters.IV, CryptoAesParameters.PASSPHRASE, token)
    val name = generarNombreSesionActor(decryptedToken)
    context.actorOf(SesionActor.props(expiration, empresa, horario), name)
    log.info("Creando sesion de usuario. Tiempo de expiracion: " + expiration + " minutos.")
  }

  private def generarNombreSesionActor(token: String) =
    MessageDigest.getInstance("MD5").digest(token.split("\\.")(2).getBytes).map { b => String.format("%02X", java.lang.Byte.valueOf(b)) }.mkString("") // Actor's name

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

class SesionActor(expiracionSesion: Int, empresa: Option[Empresa], horario: Option[HorarioEmpresa]) extends Actor with ActorLogging {

  implicit val _: ExecutionContext = context dispatcher
  implicit val timeout: Timeout = 120 seconds

  private val cluster = Cluster.get(context.system)

  // System scheduler instance
  private val scheduler: Scheduler = context.system.scheduler

  // Kill task
  private var killTask: Cancellable = scheduler.scheduleOnce(expiracionSesion.minutes, self, ExpirarSesion())

  // PostStop function
  override def postStop(): Unit = killTask.cancel()

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
      if (empresaActor.isDefined)
        empresaActor.get ! RemoverSesion(self)
      context.stop(self)

    case empresaActor: ActorRef =>
      this.empresaActor = Some(empresaActor)
      empresaActor ! AgregarSesion(self)

    case ObtenerEmpresaActor => sender ! empresaActor

  }

  private def inicializaEmpresaActor() = if (empresa.isDefined) {
    context.actorOf(Props(new BuscadorActorCluster("sesionActorSupervisor"))) ? BuscarActor(s"empresa${empresa.get.id}") map {
      case Some(empresaActor: ActorRef) => self ! empresaActor
      case None =>
        context.parent ? CrearEmpresaActor(empresa.get, horario) map {
          case empresaActor: ActorRef => self ! empresaActor
          case None => log error s"Sesión empresa '${empresa.get.id}' no creada!!"
        }
    }
  }

}

object SesionActor {

  def props(expirationTime: Int, empresa: Option[Empresa], horario: Option[HorarioEmpresa]): Props = {

    Props(new SesionActor(expirationTime, empresa, horario))
  }

}

case class DeleteSession(actorName: String)

case class ActualizarEmpresa(empresa: Empresa)

case class CrearEmpresaActor(empresa: Empresa, horarioEmpresa: Option[HorarioEmpresa] = None)

case object ObtenerEmpresaActor

case object CerrarSesiones
