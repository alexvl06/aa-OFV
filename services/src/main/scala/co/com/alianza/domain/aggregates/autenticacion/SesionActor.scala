package co.com.alianza.domain.aggregates.autenticacion

import java.security.MessageDigest

import akka.actor._
import akka.cluster.{Member, MemberStatus}
import akka.util.Timeout
import co.com.alianza.app.MainActors

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
    case message: CrearSesionUsuario => crearSesion(message.token, message.tiempoExpiracion)

    // When a user logout
    case message: InvalidarSesion => invalidarSesion(message.token)

    // When system validate token
    case message: ValidarSesion => validarSesion(message.token)

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
  }

  private def validarSesion(token: String): Unit = {

    val currentSender = sender()
    val actorName = MessageDigest.getInstance("MD5").digest(token.split("\\.")(2).getBytes).map { b => String.format("%02X", java.lang.Byte.valueOf(b))}.mkString("") // Actor's name
    val nodosUnreach: Set[Member] = MainActors.cluster.state.unreachable // Lista de nodos en estado unreachable
    val nodosUp: SortedSet[Member] = MainActors.cluster.state.members.filter(_.status == MemberStatus.up) // Lista de nodos en estado UP
    val nodosBusqueda: SortedSet[Member] = nodosUp.filter(m => !nodosUnreach.contains(m)) // Lista de nodos que estan en estado UP y no son estan unreachable

    context.actorOf(Props(new Actor {

      var numResp = 0
      var resp: Option[ActorRef] = None

      nodosBusqueda.foreach { member =>
        this.context.actorSelection(RootActorPath(member.address) / "user" / "sesionActorSupervisor") ! GetSession(actorName)
      }

      def receive: Receive = {
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
          resp match {
            case Some(_) =>
              currentSender ! true
              this.context.stop(self)
            case None =>
              currentSender ! false
              this.context.stop(self)
          }
        }
      }

    }))

  }

  private def invalidarSesion(token: String): Unit = {

    val actorName = MessageDigest.getInstance("MD5").digest(token.split("\\.")(2).getBytes).map { b => String.format("%02X", java.lang.Byte.valueOf(b))}.mkString("") // Actor's name
    val nodosUnreach: Set[Member] = MainActors.cluster.state.unreachable // Lista de nodos en estado unreachable
    val nodosUp: SortedSet[Member] = MainActors.cluster.state.members.filter(_.status == MemberStatus.up) // Lista de nodos en estado UP
    val nodosBusqueda: SortedSet[Member] = nodosUp.filter(m => !nodosUnreach.contains(m)) // Lista de nodos que estan en estado UP y no son estan unreachable

    nodosBusqueda.foreach { member =>
      context.actorSelection(RootActorPath(member.address) / "user" / "sesionActorSupervisor") ! DeleteSession(actorName)
    }

  }

  private def crearSesion(token: String, expiration: Int) = {
    val name = MessageDigest.getInstance("MD5").digest(token.split("\\.")(2).getBytes).map { b => String.format("%02X", java.lang.Byte.valueOf(b))}.mkString("")
    context.actorOf(SesionActor.props(expiration), name)
    log.info("Creando sesion de usuario. Tiempo de expiracion: " + expiration + " minutos.")
  }

}

class SesionActor(expiracionSesion: Int) extends Actor with ActorLogging {

  implicit val _: ExecutionContext = context.dispatcher

  // System scheduler instance
  private val scheduler: Scheduler = context.system.scheduler

  // Kill task
  private var killTask: Cancellable = scheduler.scheduleOnce(expiracionSesion.minutes, self, ExpirarSesion())

  // PostStop function
  override def postStop(): Unit = killTask.cancel()

  // Receive function
  override def receive = {

    case msg: ActualizarSesion =>
      killTask.cancel()
      killTask = scheduler.scheduleOnce(expiracionSesion.minutes, self, ExpirarSesion())

    case msg: ExpirarSesion =>
      log.info("Eliminando sesion de usuario: " + self.path.name)
      context.stop(self)
  }

}

object SesionActor {

  def props(expirationTime: Int) = {
    Props(new SesionActor(expirationTime))
  }

}

case class GetSession(actorName: String)

case class SessionFound(session: ActorRef)

case object SessionNotFound

case class DeleteSession(actorName: String)