package co.com.alianza.domain.aggregates.autenticacion

import akka.actor._
import akka.cluster.{Member, MemberStatus, Cluster}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.util.Timeout
import co.com.alianza.app.MainActors

import co.com.alianza.constants.TiposConfiguracion
import co.com.alianza.infrastructure.dto.Configuracion

import co.com.alianza.infrastructure.messages._

import scala.concurrent.duration._
import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Failure, Success}

class SesionActorSupervisor extends Actor with ActorLogging {

  implicit val _: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 10.seconds

  // Sessions variable
  var sessions = IndexedSeq.empty[ActorRef]

  def receive = {

    // When a new session joins the cluster
    case message: ClusterRegistration if !sessions.contains(sender()) =>
      log.info("Registrando sesion en el cluster ...")
      context watch sender()
      sessions = sessions :+ sender()

    // When a session died or is stopped
    case Terminated(a) =>
      log.info("Eliminando sesion del cluster ...")
      sessions = sessions.filterNot(_ == a)

    // When an user authenticates
    case message: CrearSesionUsuario =>
      val config = message.tiempoExpiracion.getOrElse(Configuracion(TiposConfiguracion.EXPIRACION_SESION.llave, "5"))
      crearSesion(message.token, config.valor.toInt)

    // When a user logout
    case message: InvalidarSesion =>

      buscarSesion(message.token).onComplete {
        case Success(actor) => actor ! ExpirarSesion()
        case Failure(ex) =>
      }

    // When system validate token
    case message: ValidarSesion =>
      val currentSender: ActorRef = sender()

      buscarSesion(message.token).onComplete {
        case Success(actor) =>
          actor ! ActualizarSesion()
          currentSender ! true
        case Failure(ex) =>
          currentSender ! false
      }
  }

  private def buscarSesion(token: String): Future[ActorRef] = {
    val actor: List[ActorRef] = sessions.toList.filter(act => act.path.name == token.split("\\.")(2))
    if (actor.nonEmpty) context.actorSelection(actor(0).path).resolveOne()
    else Future.failed(new Throwable)
  }

  private def crearSesion(token: String, expiration: Int) = {
    log.info("Creando sesion de usuario. Tiempo de expiracion: " + expiration + " minutos")
    context.actorOf(SesionActor.props(expiration), token.split("\\.")(2))
  }

}

class SesionActor(expiracionSesion: Int) extends Actor with ActorLogging {

  implicit val _: ExecutionContext = context.dispatcher

  // System scheduler instance
  private val scheduler: Scheduler = context.system.scheduler

  // Kill task
  private var killTask: Cancellable = scheduler.scheduleOnce(expiracionSesion.minutes, self, ExpirarSesion())

  // PreStart function
  override def preStart(): Unit = MainActors.cluster.subscribe(self, classOf[MemberUp])

  // PostStop function
  override def postStop(): Unit = MainActors.cluster.unsubscribe(self)

  // Receive function
  override def receive = {

    case msg: ActualizarSesion =>
      killTask.cancel()
      killTask = scheduler.scheduleOnce(expiracionSesion.minutes, self, ExpirarSesion())

    case msg: ExpirarSesion =>
      context.stop(self)

    case msg: MemberUp =>
      register(msg.member)

    case msg: CurrentClusterState =>
      msg.members.filter(_.status == MemberStatus.Up) foreach register
  }

  // Register itself
  def register(member: Member): Unit = {
    context.actorSelection(RootActorPath(member.address) / "user" / "sesionActorSupervisor") ! ClusterRegistration()
  }

}

object SesionActor {

  def props(expirationTime: Int) = {
    Props(new SesionActor(expirationTime))
  }

}

case class ClusterRegistration()
