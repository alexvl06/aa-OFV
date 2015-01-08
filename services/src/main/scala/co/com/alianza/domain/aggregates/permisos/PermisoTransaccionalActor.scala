package co.com.alianza.domain.aggregates.permisos

import akka.actor.{ActorLogging, Actor, ActorRef}
import akka.actor.Props
import akka.actor.SupervisorStrategy._
import akka.actor.OneForOneStrategy
import akka.routing.RoundRobinPool
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Validation
import spray.http.StatusCodes._
import scalaz.std.AllInstances._

import co.com.alianza.app.MainActors
import co.com.alianza.infrastructure.messages._
import co.com.alianza.infrastructure.dto.PermisoTransaccionalUsuarioEmpresarial
import co.com.alianza.util.FutureResponse
import co.com.alianza.util.transformers.ValidationT
import co.com.alianza.infrastructure.anticorruption.permisos.{PermisoTransaccionalDataAccessAdapter => DataAccessAdapter}
/**
 * Created by manuel on 8/01/15.
 */
class PermisoTransaccionalActorSupervisor extends Actor with ActorLogging {

//  val permisoTransaccionalActor = context.actorOf(Props[PermisoTransaccionalActor], "permisoTransaccionalActor")

  def receive = { case message: GuardarPermisosAgenteMessage =>  context actorOf(Props[PermisoTransaccionalActor]) forward message }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }

}

class PermisoTransaccionalActor extends Actor with ActorLogging with FutureResponse {

  implicit val _: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 120 seconds

  var numeroPermisos = 0

  def receive = {
    case GuardarPermisosAgenteMessage(permisos) =>
      log info ("Llegó mensaje de permisos: "+permisos.length)
      val currentSender = sender()
      numeroPermisos = permisos.length
      permisos foreach { p => self ! ((p, currentSender): (PermisoTransaccionalUsuarioEmpresarial, ActorRef)) }

    case (permiso: PermisoTransaccionalUsuarioEmpresarial, currentSender: ActorRef) =>
      log info ("Llegó permiso: "+permiso.toString)
      DataAccessAdapter guardaPermiso(permiso)
      numeroPermisos -= 1
      if (numeroPermisos==0) {
        val future = (for {
          result <- ValidationT(Future.successful(Validation.success(ResponseMessage(OK, "Guardado de permisos correcto"))))
        } yield {
          result
        }).run
        resolveFutureValidation(future, (x: ResponseMessage) => x, currentSender)
        context stop self
      }

  }
}