package co.com.alianza.domain.aggregates.autenticacion

import java.security.MessageDigest

import akka.actor._
import akka.pattern.ask
import akka.cluster.{Member, MemberStatus}
import akka.util.Timeout
import co.com.alianza.app.MainActors

import co.com.alianza.infrastructure.dto.Empresa
import co.com.alianza.infrastructure.messages._

import scala.collection.immutable.SortedSet
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class SesionActorSupervisor extends Actor with ActorLogging {

  implicit val _: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 10.seconds

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

    case message: GetSession =>
      val currentSender: ActorRef = sender()
      context.actorSelection("akka://alianza-fid-auth-service/user/sesionActorSupervisor/" + message.actorName).resolveOne().onComplete {
        case Success(session) => currentSender ! SessionFound(session)
        case Failure(ex) => currentSender ! SessionNotFound
      }

    case message: DeleteSession =>
      context.actorSelection("akka://alianza-fid-auth-service/user/sesionActorSupervisor/" + message.actorName).resolveOne().onComplete {
        case Success(session) => session ! ExpirarSesion()
        case Failure(ex) =>
      }

    case OptenerEmpresaActorPorId(empresaId) =>
      val currentSender = sender
      context.actorSelection("akka://alianza-fid-auth-service/user/sesionActorSupervisor/empresa" + empresaId).resolveOne().onComplete {
        case Success(empresaActor) => currentSender ! empresaActor
        case Failure(ex) =>
      }

    case BuscarSesion(token) => buscarSesion(token)

    case OptenerEmpresaSesionActor(token) => obtenerEmpresaSesion(token)

    case CrearEmpresaActor(empresa) => sender ! context.actorOf(EmpresaActor.props(empresa), s"empresa${empresa.id}")


  }

  private def validarSesion(message: ValidarSesion): Unit = {
    val currentSender = sender()
    val actorName = generarNombreSesionActor(message.token)
    context.actorOf(Props(new BuscadorSesionActor)) ? BuscarSesionActor(actorName) map {
      case Some(sesionActor: ActorRef) => sesionActor ? ActualizarSesion() onComplete { case _ => currentSender ! true }
      case None => currentSender ! false
    }
  }

  private def buscarSesion(token: String) = {
    val currentSender = sender()
    val actorName = generarNombreSesionActor(token)
    context.actorOf(Props(new BuscadorSesionActor)) ? BuscarSesionActor(actorName) map {
      case _ =>  currentSender ! _
    }
  }

  private def invalidarSesion(token: String): Unit = {
    val actorName = generarNombreSesionActor(token)
    ClusterUtil.obtenerNodos foreach { member =>
      context.actorSelection(RootActorPath(member.address) / "user" / "sesionActorSupervisor") ! DeleteSession(actorName)
    }

  }

  private def crearSesion(token: String, expiration: Int, empresa: Option[Empresa]) = {
    val name = generarNombreSesionActor(token)
    context.actorOf(SesionActor.props(expiration, empresa), name)
    log.info("Creando sesion de usuario. Tiempo de expiracion: " + expiration + " minutos.")
  }

  private def generarNombreSesionActor(token: String) =
    MessageDigest.getInstance("MD5").digest(token.split("\\.")(2).getBytes).map { b => String.format("%02X", java.lang.Byte.valueOf(b))}.mkString("") // Actor's name

  private def obtenerEmpresaSesion(token: String) = {
    val currentSender = sender()
    val actorName = generarNombreSesionActor(token)
    context.actorOf(Props(new BuscadorSesionActor)) ? BuscarSesionActor(actorName) map {
      case Some(sesion: ActorRef) => sesion ! (OptenerEmpresaActor(), currentSender)
      case None => currentSender ! None
    }
  }
}

class SesionActor(expiracionSesion: Int, empresa: Option[Empresa]) extends Actor with ActorLogging {

  implicit val _: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = Timeout(120 seconds)

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
      context.stop(self)

    case empresaActor: ActorRef =>
      this.empresaActor = Some(empresaActor)
      empresaActor ! AgregarSesion(self)

    case ObtenerEmpresaActor() => sender ! empresaActor

  }

  def inicializaEmpresaActor() = if(empresa.isDefined){
    //TODO: Mejorar usando actor anónimo para entender mejor el código
    val iteradorNodos = ClusterUtil.obtenerNodos.iterator
    lazy val iteracion: () => Unit = () => {
      if(iteradorNodos.hasNext && empresaActor.isEmpty)
        context.actorSelection(RootActorPath(iteradorNodos.next.address) / "user" / "sesionActorSupervisor") ? OptenerEmpresaActorPorId(empresa.get.id) onComplete {
          case empresaActor: ActorRef => if(this.empresaActor.isEmpty) this.empresaActor = Some(empresaActor) else empresaActor ! AgregarSesion(self)
          case Failure(error) => log info ("Empresa no encontrada: "+error)
          case _ => iteracion()
        }
      else if (!iteradorNodos.hasNext && empresaActor.isEmpty)
        context.actorSelection(self.path.parent) ? CrearEmpresaActor(empresa.get) map {
          case empresaActor: ActorRef =>
            this.empresaActor = Some(empresaActor)
            empresaActor ! AgregarSesion(self)
          case Failure(error) => log info ("Empresa no encontrada: "+error)
          case _ =>
        }
    }
    iteracion()
  }

}

object SesionActor {

  def props(expirationTime: Int, empresa: Option[Empresa]) = {
    Props(new SesionActor(expirationTime, empresa))
  }

}

class EmpresaActor(var empresa: Empresa) extends Actor with ActorLogging {

  var sesionesActivas = List[ActorRef]()
  var ips = List[String]()

  def receive = {
    case ActualizarEmpresa(empresa) => this.empresa = empresa
    case AgregarSesion(sesion) =>
      sesionesActivas = if(!sesionesActivas.contains(sesion)) List(sesion) ::: sesionesActivas else sesionesActivas //TODO: Optimizar
    case AgregarIp(ip) => ips = if(!ips.contains(ip)) List(ip) ::: ips else ips //TODO: Optimizar
    case RemoverIp(ip) => ips = if(ips.contains(ip)) ips filterNot{_==ip} else ips //TODO: Optimizar
    case CerrarSesiones() => {
      sesionesActivas foreach { _ ! ExpirarSesion }
      context.stop(self)
    }
    case ObtenerIps() => sender ! ips
  }

}

object EmpresaActor {
  def props(empresa: Empresa) = Props(new EmpresaActor(empresa))
}

object ClusterUtil {
  def obtenerNodos = {
    val nodosUnreach: Set[Member] = MainActors.cluster.state.unreachable // Lista de nodos en estado unreachable
    val nodosUp: SortedSet[Member] = MainActors.cluster.state.members.filter(_.status == MemberStatus.up) // Lista de nodos en estado UP
    nodosUp.filter(m => !nodosUnreach.contains(m)) // Lista de nodos que estan en estado UP y no son estan unreachable
  }
}

class BuscadorSesionActor extends Actor {

  var numResp = 0
  var resp: Option[ActorRef] = None
  val nodosBusqueda: SortedSet[Member] = ClusterUtil.obtenerNodos
  var interesado: ActorRef = null

  def receive: Receive = {
    case BuscarSesionActor(actorName) =>
      interesado = sender;
      nodosBusqueda foreach { member =>
        this.context.actorSelection(RootActorPath(member.address) / "user" / "sesionActorSupervisor") ! GetSession(actorName)
      }
    case SessionFound(session) =>
      numResp += 1
      resp = Some(session)
      replyIfReady()
    case _ =>
      numResp += 1
      replyIfReady()
  }

  def replyIfReady() = {
    if(numResp == nodosBusqueda.size) {
      interesado ! resp
      this.context.stop(self)
    }
  }

}

case class GetSession(actorName: String)

case class BuscarSesionActor(actorName: String)

case class SessionFound(session: ActorRef)

case object SessionNotFound

case class DeleteSession(actorName: String)

case class ActualizarEmpresa(empresa: Empresa)

case class OptenerEmpresaActor()

case class AgregarSesion(sesion: ActorRef)

case class AgregarIp(ip: String)

case class RemoverIp(ip: String)

case class ObtenerIps()

case class CrearEmpresaActor(empresa: Empresa)

case class ObtenerEmpresaActor()

case class CerrarSesiones()