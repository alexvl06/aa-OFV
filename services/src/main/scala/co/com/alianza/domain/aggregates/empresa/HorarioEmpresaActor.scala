package co.com.alianza.domain.aggregates.empresa

import akka.actor.{Actor, ActorRef, ActorLogging, Props, OneForOneStrategy}
import akka.actor.SupervisorStrategy._
import akka.pattern._
import akka.routing.RoundRobinPool
import co.com.alianza.app.AlianzaActors
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
import co.com.alianza.infrastructure.anticorruption.horario.{DataAccessTranslator => HorarioTrans}
import co.com.alianza.infrastructure.dto.{HorarioEmpresa => HorarioEmpresaDTO}
import co.com.alianza.infrastructure.messages._
import co.com.alianza.infrastructure.messages.empresa.{DiaFestivoMessage, AgregarHorarioEmpresaMessage, ObtenerHorarioEmpresaMessage}
import co.com.alianza.persistence.entities.HorarioEmpresa
import co.com.alianza.util.transformers.ValidationT
import co.com.alianza.app.MainActors
import co.com.alianza.exceptions.BusinessLevel
import co.com.alianza.domain.aggregates.autenticacion.ActualizarHorarioEmpresa
import spray.http.StatusCodes._

import java.sql.{Date, Time}
import scala.util.{Failure, Success}
import scala.concurrent.Future
import scalaz.{Failure => zFailure, Success => zSuccess, Validation}
import scalaz.std.AllInstances._

class HorarioEmpresaActorSupervisor extends Actor with ActorLogging {

  val horarioEmpresaActor = context.actorOf(Props[HorarioEmpresaActor].withRouter(RoundRobinPool(nrOfInstances = 2)), "horarioEmpresaActor")

  def receive = {
    case message: Any =>horarioEmpresaActor forward message
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
    case message: DiaFestivoMessage => esDiaFestivo(message)
  }

  implicit def obtenerEnumTipoCliente(tipoCliente: Option[Int]) = {
    TiposCliente.apply(tipoCliente.get)
  }

  def obtenerHorarioEmpresa(message: ObtenerHorarioEmpresaMessage) = {
    val currentSender = sender()
    val result = {
      (for {
        idEmpresa <- ValidationT(DataAccessAdapter.obtenerIdEmpresa(message.idUsuario, message.tipoCliente))
        horarioEmpresa <- ValidationT(DataAccessAdapter.obtenerHorarioEmpresa(idEmpresa))
      } yield (horarioEmpresa)).run
    }
    result onComplete {
      case Failure(failure)  =>
        currentSender ! failure
      case Success(value)    =>
        value match {
          case zSuccess(response: Option[HorarioEmpresa]) =>
            currentSender !  ResponseMessage(OK, response.toJson)
          case zFailure(error)   =>
            currentSender !  error
        }
    }
  }

  def toEntity(message: AgregarHorarioEmpresaMessage, idEmpresa: Int): HorarioEmpresa = {
    implicit def toTime(hora: String) : Time = Time.valueOf(hora)
    new HorarioEmpresa(idEmpresa, message.diaHabil, message.sabado, message.horaInicio, message.horaFin)
  }

  def agregarHorarioEmpresa(message: AgregarHorarioEmpresaMessage) = {
    val currentSender = sender()
    val result = {
      (for {
        idEmpresa <- ValidationT(DataAccessAdapter.obtenerIdEmpresa(message.idUsuario.get, message.tipoCliente))
        horarioEmpresa <- ValidationT(DataAccessAdapter.agregarHorarioEmpresa(toEntity(message, idEmpresa)))
        horarioEmpresa <- ValidationT(agregarHorarioSesionEmpresa (idEmpresa, HorarioTrans translateHorarioEmpresa(toEntity(message, idEmpresa))))
      } yield (horarioEmpresa)).run
    }
    result onComplete {
      case Failure(failure)  =>
        currentSender ! failure
      case Success(value)    =>
        value match {
          case zSuccess(response: Boolean) =>
            currentSender !  ResponseMessage(OK, response.toJson)
          case zFailure(error)   =>
            currentSender !  error
        }
    }
  }

  def agregarHorarioSesionEmpresa(empresaId: Int, horario: HorarioEmpresaDTO) : Future[Validation[PersistenceException, Boolean]] = {
    MainActors.sesionActorSupervisor ? ObtenerEmpresaSesionActorId (empresaId) map {
      case Some(empresaSesionActor: ActorRef) => empresaSesionActor ! ActualizarHorarioEmpresa(horario); zSuccess(true)
      case None => zSuccess(false)
      case _ => zFailure(PersistenceException(new Exception(),BusinessLevel,"Error"))
    }
  }

  def esDiaFestivo(message: DiaFestivoMessage) = {
    implicit def toDate(fecha: String) : Date = Date.valueOf(fecha)
    val currentSender = sender()
    val result = {
      (for {
        existe <- ValidationT(DataAccessAdapter.existeDiaFestivo(message.fecha))
      } yield (existe)).run
    }
    result onComplete {
      case Failure(failure)  =>
        currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response: Boolean) =>
            currentSender !  ResponseMessage(OK, response.toJson)
          case zFailure(error)   =>
            currentSender !  error
        }
    }
  }

}