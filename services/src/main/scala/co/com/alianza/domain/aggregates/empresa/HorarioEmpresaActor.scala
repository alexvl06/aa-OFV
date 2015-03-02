package co.com.alianza.domain.aggregates.empresa

import akka.actor.{Actor, ActorLogging, Props, OneForOneStrategy}
import akka.actor.SupervisorStrategy._
import akka.routing.RoundRobinPool
import co.com.alianza.app.AlianzaActors
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
import co.com.alianza.infrastructure.messages._
import co.com.alianza.infrastructure.messages.empresa.{AgregarHorarioEmpresaMessage, ObtenerHorarioEmpresaMessage}
import co.com.alianza.persistence.entities.{HorarioEmpresa}
import co.com.alianza.util.transformers.ValidationT
import spray.http.StatusCodes._

import java.sql.Time
import scala.util.{Failure, Success}
import scala.concurrent.Future
import scalaz.{Failure => zFailure, Success => zSuccess, Validation}
import scalaz.std.AllInstances._

class HorarioEmpresaActorSupervisor extends Actor with ActorLogging {

  val horarioEmpresaActor = context.actorOf(Props[HorarioEmpresaActor].withRouter(RoundRobinPool(nrOfInstances = 2)), "horarioEmpresaActor")

  def receive = {
    case message: Any =>
      horarioEmpresaActor forward message
  }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }

}

class HorarioEmpresaActor extends Actor with ActorLogging with AlianzaActors {
  import scala.concurrent.ExecutionContext
  implicit val _: ExecutionContext = context.dispatcher
  import co.com.alianza.util.json.MarshallableImplicits._

  def receive = {
    case message: ObtenerHorarioEmpresaMessage  => obtenerHorarioEmpresa(message)
    case message: AgregarHorarioEmpresaMessage  => agregarHorarioEmpresa(message)
  }

  implicit def obtenerEnumTipoCliente(tipoCliente: Option[Int]) = {
    TiposCliente.apply(tipoCliente.get)
  }

  def obtenerHorarioEmpresa(message: ObtenerHorarioEmpresaMessage) = {
    val result = {
      (for {
        idEmpresa <- ValidationT(DataAccessAdapter.obtenerIdEmpresa(message.idUsuario, message.tipoCliente))
        horarioEmpresa <- ValidationT(DataAccessAdapter.obtenerHorarioEmpresa(idEmpresa))
      } yield (horarioEmpresa)).run
    }
    result onComplete {
      case Failure(failure)  =>
        sender() ! failure
      case Success(value)    =>
        value match {
          case zSuccess(response: Option[HorarioEmpresa]) =>
            sender() !  ResponseMessage(OK, response.toJson)
          case zFailure(error)   =>
            sender() !  error
        }
    }
  }

  implicit def toTime(hora: String) : Time = Time.valueOf(hora)

  implicit def toEntity(message: AgregarHorarioEmpresaMessage): HorarioEmpresa ={
    new HorarioEmpresa(0, message.diaHabil, message.sabado, message.horaInicio, message.horaFin)
  }

  def agregarHorarioEmpresa(message: AgregarHorarioEmpresaMessage) = {
    val result = {
      (for {
        idEmpresa <- ValidationT(DataAccessAdapter.obtenerIdEmpresa(message.idUsuario.get, message.tipoCliente))
        horarioEmpresa <- ValidationT(DataAccessAdapter.agregarHorarioEmpresa(message))
      } yield (horarioEmpresa)).run
    }
    result onComplete {
      case Failure(failure)  =>
        sender() ! failure
      case Success(value)    =>
        value match {
          case zSuccess(response: Int) =>
            sender() !  ResponseMessage(OK, response.toJson)
          case zFailure(error)   =>
            sender() !  error
        }
    }
  }

}