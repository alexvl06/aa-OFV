package co.com.alianza.domain.aggregates.autenticacion

import java.security.MessageDigest

import akka.actor._
import akka.pattern.ask
import akka.cluster.{Member, MemberStatus}
import akka.util.Timeout

import co.com.alianza.app.MainActors
import co.com.alianza.infrastructure.dto.{Empresa, HorarioEmpresa}
import co.com.alianza.infrastructure.messages._
import co.com.alianza.util.token.AesUtil
import enumerations.CryptoAesParameters

import scala.collection.immutable.SortedSet
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class SesionActorSupervisor extends Actor with ActorLogging {

  implicit val _: ExecutionContext = context dispatcher
  implicit val timeout: Timeout = 10 seconds

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

    case ObtenerEmpresaSesionActorToken(token) => obtenerEmpresaSesion(token)

    case CrearEmpresaActor(empresa, horario) =>
      log info "Creando empresa Actor: "+s"empresa${empresa.id}"
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
    val actorName = generarNombreSesionActor(token)
    ClusterUtil.obtenerNodos foreach { member =>
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
    MessageDigest.getInstance("MD5").digest(token.split("\\.")(2).getBytes).map { b => String.format("%02X", java.lang.Byte.valueOf(b))}.mkString("") // Actor's name

  private def obtenerEmpresaSesion(token: String) = {
    val currentSender = sender()
    val actorName = generarNombreSesionActor(token)
    context.actorOf(Props(new BuscadorActorCluster("sesionActorSupervisor"))) ? BuscarActor(actorName) map {
      case Some(sesion: ActorRef) => sesion tell (ObtenerEmpresaActor, currentSender)
      case None => currentSender ! None
    }
  }

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
      if(empresaActor.isDefined)
        empresaActor.get ! RemoverSesion(self)
      context.stop(self)

    case empresaActor: ActorRef =>
      this.empresaActor = Some(empresaActor)
      empresaActor ! AgregarSesion(self)

    case ObtenerEmpresaActor => sender ! empresaActor

  }

  private def inicializaEmpresaActor() = if(empresa.isDefined) {
    context.actorOf(Props(new BuscadorActorCluster("sesionActorSupervisor"))) ? BuscarActor(s"empresa${empresa.get.id}") map {
      case Some(empresaActor: ActorRef) => self ! empresaActor
      case None =>
        MainActors.sesionActorSupervisor ? CrearEmpresaActor(empresa.get, horario) map {
          case empresaActor: ActorRef => self ! empresaActor
          case None => log error s"Sesión empresa '${empresa.get.id}' no creada!!"
        }
    }
  }

}

object SesionActor {

  def props(expirationTime: Int, empresa: Option[Empresa], horario: Option[HorarioEmpresa]) = {
    Props(new SesionActor(expirationTime, empresa, horario))
  }

}

case class DeleteSession(actorName: String)

case class ActualizarEmpresa(empresa: Empresa)

case class CrearEmpresaActor(empresa: Empresa, horarioEmpresa: Option[HorarioEmpresa] = None)

case object ObtenerEmpresaActor

case object CerrarSesiones