package co.com.alianza.domain.aggregates.usuarios

import akka.actor.{ActorRef, Actor, ActorLogging}
import scalaz.{Failure => zFailure, Success => zSuccess, Validation}
import scala.util.{Success, Failure}
import co.com.alianza.app.{MainActors, AlianzaActors}
import co.com.alianza.infrastructure.messages.{ResponseMessage, UsuarioMessage}
import spray.http.StatusCodes._
import scala.concurrent.Future
import co.com.alianza.infrastructure.dto.Cliente
import co.com.alianza.infrastructure.anticorruption.usuarios.{DataAccessAdapter => DataAccessAdapterUsuario }
import co.com.alianza.util.transformers.ValidationT
import scalaz.std.AllInstances._
import co.com.alianza.util.clave.Crypto
import com.typesafe.config.{ConfigFactory, Config}
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.PerfilUsuario
import enumerations.{AppendPasswordUser, EstadosCliente, PerfilesUsuario}

import akka.actor.Props
import akka.routing.RoundRobinPool




class UsuariosActorSupervisor extends Actor with ActorLogging {
  import akka.actor.SupervisorStrategy._
  import akka.actor.OneForOneStrategy

  val usuariosActor = context.actorOf(Props[UsuariosActor].withRouter(RoundRobinPool(nrOfInstances = 2)), "usuariosActor")

  def receive = {

    case message: Any =>
      usuariosActor forward message

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
 */
class UsuariosActor extends Actor with ActorLogging with AlianzaActors {
  import scala.concurrent.ExecutionContext
  implicit val ex: ExecutionContext = MainActors.dataAccesEx
  implicit private val config: Config = MainActors.conf
  import ValdiacionesUsuario._

  def receive = {

    case message:UsuarioMessage  =>

      val currentSender = sender()

      val crearUsuarioFuture = (for{
        captchaVal <-  ValidationT(validaCaptcha(message))
        cliente <- ValidationT(validaSolicitud(message))
        //idUsuario <- ValidationT(guardarUsuario(message))
      }yield{
        cliente
      }).run

      resolveCrearUsuarioFuture(crearUsuarioFuture,currentSender,message)


  }

  private def validaSolicitud(message:UsuarioMessage): Future[Validation[ErrorValidacion, Cliente]] = {

    val consultaNumDocFuture = validacionConsultaNumDoc(message)
    val consultaCorreoFuture: Future[Validation[ErrorValidacion, Unit.type]] = validacionConsultaCorreo(message)
    val consultaClienteFuture: Future[Validation[ErrorValidacion, Cliente]] = validacionConsultaCliente(message)
    val validacionClave: Future[Validation[ErrorValidacion, Unit.type]] = validacionReglasClave(message)

    (for{
      resultValidacionClave <- ValidationT(validacionClave)
      resultConsultaNumDoc <- ValidationT(consultaNumDocFuture)
      resultConsultaCorreo <- ValidationT(consultaCorreoFuture)
      cliente <- ValidationT(consultaClienteFuture)
    }yield {
      cliente
    }).run
  }

  private def guardarUsuario(message:UsuarioMessage): Future[Validation[ErrorValidacion, Int]] = {
    val passwordUserWithAppend = message.contrasena.concat( AppendPasswordUser.appendUsuariosFiducia );
    println("Password User Final***->"+passwordUserWithAppend)
    DataAccessAdapterUsuario.crearUsuario(message.toEntityUsuario( Crypto.hashSha256(passwordUserWithAppend))).map(_.leftMap( pe => ErrorPersistence(pe.message,pe)))
  }

  private def resolveCrearUsuarioFuture(crearUsuarioFuture: Future[Validation[ErrorValidacion, Cliente]], currentSender: ActorRef,message:UsuarioMessage) = {
    crearUsuarioFuture onComplete {
      case Failure(failure)  =>    currentSender ! failure
      case Success(value)    =>
        value match {
          case zSuccess(response) =>
            currentSender !  ResponseMessage(Created, response.toString)
            //DataAccessAdapterUsuario.asociarPerfiles(PerfilUsuario(response,PerfilesUsuario.clienteIndividual.id)::Nil)

            //if(message.activarIP && message.clientIp.isDefined){
            //  DataAccessAdapterUsuario.relacionarIp(response,message.clientIp.get)
            //}
          case zFailure(error)  =>
            error match {
              case errorPersistence:ErrorPersistence  => currentSender !  errorPersistence.exception
              case errorVal:ErrorValidacion =>
                currentSender !  ResponseMessage(Conflict, errorVal.msg)
            }
        }
    }
  }

}