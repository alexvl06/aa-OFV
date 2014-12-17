package co.com.alianza.domain.aggregates.empresa.contrasenasEmpresa

import akka.actor.{Props, ActorLogging, Actor}
import akka.routing.RoundRobinPool
import co.com.alianza.app.{MainActors, AlianzaActors}
import co.com.alianza.domain.aggregates.usuarios.ValidacionesAgenteEmpresarial
import co.com.alianza.infrastructure.messages.empresa.ReiniciarContrasenaEmpresaMessage
import co.com.alianza.util.transformers.ValidationT
import scalaz.std.AllInstances._
import co.com.alianza.domain.aggregates.usuarios.{ErrorPersistence, ErrorValidacion, ValidacionesAgenteEmpresarial}

/**
 * Created by S4N on 17/12/14.
 */
class ContrasenasEmpresaActorSupervisor extends Actor with ActorLogging {

  import akka.actor.SupervisorStrategy._
  import akka.actor.OneForOneStrategy

  val contrasenasEmpresaActor = context.actorOf(Props[ContrasenasEmpresaActor].withRouter(RoundRobinPool(nrOfInstances = 2)), "contrasenasEmpresaActor")

  def receive = {

    case message: Any =>
      contrasenasEmpresaActor forward message

  }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }

}

class ContrasenasEmpresaActor extends Actor with ActorLogging with AlianzaActors {
  import scalaz._
  import scala.concurrent.ExecutionContext
  import co.com.alianza.util.json.MarshallableImplicits._
  import ValidacionesAgenteEmpresarial._
  implicit val ex: ExecutionContext = MainActors.dataAccesEx

  def receive = {

    case message: ReiniciarContrasenaEmpresaMessage =>

      val currentSender = sender()
      val ReiniciarContrasenaFuture = (for {
        idUsuarioAgenteEmpresarial <- ValidationT(validacionAgenteEmpresarial(message.numIdentificacionAgenteEmpresarial, message.correoUsuarioAgenteEmpresarial, message.tipoIdentiAgenteEmpresarial))
      } yield {
        idUsuarioAgenteEmpresarial
      }).run

  }
}
