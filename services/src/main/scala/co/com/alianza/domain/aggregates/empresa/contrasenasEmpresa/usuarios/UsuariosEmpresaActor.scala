package co.com.alianza.domain.aggregates.empresa.contrasenasEmpresa.usuarios

import akka.actor.{Props, ActorRef, Actor, ActorLogging}

import co.com.alianza.app.{MainActors, AlianzaActors}
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.dto._
import co.com.alianza.infrastructure.anticorruption.empresa.usuarios.{DataAccessAdapter,DataAccessTranslator}
import co.com.alianza.infrastructure.messages._
import co.com.alianza.microservices.MailMessage
import co.com.alianza.microservices.SmtpServiceClient
import co.com.alianza.persistence.entities
import co.com.alianza.persistence.entities.PerfilUsuario
import co.com.alianza.util.FutureResponse
import co.com.alianza.util.json.MarshallableImplicits._
import co.com.alianza.util.token.PinData
import co.com.alianza.util.token.TokenPin
import co.com.alianza.util.transformers.ValidationT

import com.typesafe.config.Config

import java.util.Calendar

import scala.concurrent.Future
import scala.util.{Failure => sFailure, Success => sSuccess}
import scalaz.{Failure => zFailure, Success => zSuccess, Validation}

import spray.http.StatusCodes._
import co.com.alianza.infrastructure.messages.empresa.GetUsuariosEmpresaBusquedaMessage
import akka.routing.RoundRobinPool
import co.com.alianza.domain.aggregates.usuarios.UsuarioEmpresarialActor


class UsuariosEmpresaActorSupervisor extends Actor with ActorLogging {
  import akka.actor.SupervisorStrategy._
  import akka.actor.OneForOneStrategy

  val usuariosEmpresaActor = context.actorOf(Props[UsuariosEmpresaActor].withRouter(RoundRobinPool(nrOfInstances = 2)), "usuariosEmpresaActor")

  def receive = {

    case message: GetUsuariosEmpresaBusquedaMessage =>
      usuariosEmpresaActor forward message

    case message: Any =>
      usuariosEmpresaActor forward message

  }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }

}



/**
 *
 * Actor que tiene las acciones relacionadas con usuarios
 *
 * @author seven4n
 */
class UsuariosEmpresaActor extends Actor with ActorLogging with AlianzaActors with FutureResponse {

  import scala.concurrent.ExecutionContext

  implicit val ex: ExecutionContext = MainActors.dataAccesEx
  implicit val sys = context.system
  implicit private val config: Config = MainActors.conf

  def receive = {

    case message: GetUsuariosEmpresaBusquedaMessage =>
      val currentSender = sender()
      val future: Future[Validation[PersistenceException, List[UsuarioEmpresarial]]] = DataAccessAdapter.obtenerUsuariosBusqueda(message.toGetUsuariosEmpresaBusquedaRequest)
      resolveFutureValidation(future, (response: List[UsuarioEmpresarial]) => response.toJson, currentSender)
  }
}