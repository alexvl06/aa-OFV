package co.com.alianza.domain.aggregates.empresa

import java.sql.Timestamp
import java.util.Calendar

import akka.actor.{ActorRef, Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool
import co.com.alianza.app.{AlianzaActors, MainActors}
import co.com.alianza.domain.aggregates.usuarios.{ErrorPersistence, MailMessageUsuario, ErrorValidacion}
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial.DataAccessAdapter
import co.com.alianza.infrastructure.dto.PinEmpresa
import co.com.alianza.infrastructure.messages.{UsuarioMessage, ResponseMessage}
import co.com.alianza.infrastructure.messages.empresa.{CrearAgenteEMessage, UsuarioMessageCorreo, ReiniciarContrasenaAgenteEMessage}
import co.com.alianza.microservices.{MailMessage, SmtpServiceClient}
import co.com.alianza.persistence.entities.{UsuarioEmpresarialEmpresa, Empresa, IpsUsuario, UltimaContrasena}
import co.com.alianza.util.clave.Crypto
import co.com.alianza.util.token.{PinData, TokenPin}
import co.com.alianza.util.transformers.ValidationT
import com.typesafe.config.Config
import enumerations.EstadosEmpresaEnum
import scalaz.std.AllInstances._
import scala.util.{Failure => sFailure, Success => sSuccess}
import scalaz.{Failure => zFailure, Success => zSuccess}
import co.com.alianza.persistence.entities

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Validation
import spray.http.StatusCodes._

/**
 * Created by S4N on 17/12/14.
 */
class AgenteEmpresarialActorSupervisor extends Actor with ActorLogging {

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  val agenteEmpresarialActor = context.actorOf(Props[AgenteEmpresarialActor].withRouter(RoundRobinPool(nrOfInstances = 2)), "agenteEmpresarialActor")

  def receive = {
    case message: Any => agenteEmpresarialActor forward message
  }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }

}

class AgenteEmpresarialActor extends Actor with ActorLogging with AlianzaActors {

  import scala.concurrent.ExecutionContext


  implicit val ex: ExecutionContext = MainActors.dataAccesEx
  implicit val sys = context.system
  implicit private val config: Config = MainActors.conf

  def receive = {
//toUsuarioEmpresarialEmpresa(empresa, idUsuarioAgenteEmpresarial)
    case message: CrearAgenteEMessage => {
      val currentSender = sender()
      val usuarioCreadoFuture: Future[Validation[PersistenceException, Int]] = (for {
        idUsuarioAgenteEmpresarial <- ValidationT(DataAccessAdapter.crearAgenteEmpresarial(message.toEntityUsuarioAgenteEmpresarial()))
        empresa <- ValidationT(DataAccessAdapter.obtenerEmpresaPorNit(message.nit))
        resultAsociarEmpresa <- ValidationT(DataAccessAdapter.asociarAgenteEmpresarialConEmpresa(UsuarioEmpresarialEmpresa(empresa.get.id, idUsuarioAgenteEmpresarial)))
        resultCreacionIps <- ValidationT(DataAccessAdapter.crearIpsAgenteEmpresarial(toIpsUsuarioArray(message.ips, idUsuarioAgenteEmpresarial)))
      } yield {
        idUsuarioAgenteEmpresarial
      }).run
      resolveCrearAgenteEmpresarialFuture(usuarioCreadoFuture, currentSender)
    }
  }

  private def resolveCrearAgenteEmpresarialFuture(crearAgenteEmpresarialFuture: Future[Validation[PersistenceException, Int]], currentSender: ActorRef) {
    crearAgenteEmpresarialFuture onComplete {
      case sFailure(failure) => currentSender ! failure
      case sSuccess(value) =>
        value match {
          case zSuccess(idUsuarioAgenteEmpresarial: Int) =>

            currentSender ! ResponseMessage(OK, s"$idUsuarioAgenteEmpresarial")

          case zFailure(error) =>
            error match {
              case errorPersistence: ErrorPersistenceEmpresa => currentSender ! errorPersistence.exception
              case errorVal: ErrorValidacionEmpresa  =>
                currentSender ! ResponseMessage(Conflict, errorVal.msg)
            }
        }
    }
  }

  private def toIpsUsuarioArray(ips : Array[String], idUsuarioAgenteEmpresarial : Int) : Array[IpsUsuario] = ips.map(ip => IpsUsuario(idUsuarioAgenteEmpresarial, ip))

}
