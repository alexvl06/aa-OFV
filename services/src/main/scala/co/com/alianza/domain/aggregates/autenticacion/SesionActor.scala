package co.com.alianza.domain.aggregates.autenticacion

import akka.actor._
import akka.util.Timeout

import co.com.alianza.constants.TiposConfiguracion
import co.com.alianza.infrastructure.anticorruption.configuraciones.{DataAccessAdapter => confDataAdapter}
import co.com.alianza.infrastructure.dto.Configuracion

import co.com.alianza.infrastructure.messages._

import scala.concurrent.duration._
import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Failure, Success}

class SesionActorSupervisor extends Actor with ActorLogging {

  implicit val _: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 10.seconds

  def receive = {

    case message: CrearSesionUsuario =>

      confDataAdapter.obtenerConfiguracionPorLlave(TiposConfiguracion.EXPIRACION_SESION.llave).map {
        _.map {
          confOp =>

            val config = confOp.getOrElse(Configuracion(TiposConfiguracion.EXPIRACION_SESION.llave, "5"))
            buscarSesion(message.token).onComplete {
              case Success(actor) =>
                actor ! ActualizarSesion()
              case Failure(ex) =>
                crearSesion(message.token, config.valor.toInt)
            }
        }
      }

    case message: InvalidarSesion =>

      buscarSesion(message.token).onComplete {
        case Success(actor) => context.stop(actor)
        case Failure(ex) =>
      }

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
    val pathLocal = "akka://alianza-service/user/"
    context.system.actorSelection(pathLocal + "sesion_" + token).resolveOne()
  }

  private def crearSesion(token: String, expiration: Int) = {
    log.info("Creando sesion de usuario. Tiempo de expiracion: " + expiration + " minutos")
    context.system.actorOf(Props(new SesionActor(expiration)), "sesion_" + token)
  }

}

class SesionActor(expiracionSesion: Int) extends Actor with ActorLogging {

  implicit val _: ExecutionContext = context.dispatcher

  // System scheduler instance
  private val scheduler: Scheduler = context.system.scheduler

  // Kill task
  private var killTask: Cancellable = scheduler.scheduleOnce(expiracionSesion.minutes, self, ExpirarSesion())

  // Receive function
  override def receive = {

    case msg: ActualizarSesion =>
      killTask.cancel()
      killTask = scheduler.scheduleOnce(expiracionSesion.minutes, self, ExpirarSesion())

    case msg: ExpirarSesion =>
      context.stop(self)
  }

}
