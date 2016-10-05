package co.com.alianza.domain.aggregates.empresa

import java.sql.{ Date, Time }

import akka.actor.SupervisorStrategy._
import akka.actor.{ Actor, ActorLogging, ActorRef, OneForOneStrategy, Props }
import akka.routing.RoundRobinPool
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.domain.aggregates.autenticacion.errores.{ ErrorAutenticacion, ErrorCredencialesInvalidas, ErrorPersistencia }
import co.com.alianza.domain.aggregates.autenticacion.{ ValidacionesAutenticacionUsuarioEmpresarial }
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
import co.com.alianza.infrastructure.dto.{ Empresa, HorarioEmpresa => HorarioEmpresaDTO }
import co.com.alianza.infrastructure.messages._
import co.com.alianza.infrastructure.messages.empresa.{ ValidarHorarioEmpresaMessage, AgregarHorarioEmpresaMessage, DiaFestivoMessage, ObtenerHorarioEmpresaMessage }
import co.com.alianza.persistence.entities.HorarioEmpresa
import co.com.alianza.util.transformers.ValidationT
import spray.http.StatusCodes._

import scala.concurrent.Future
import scala.util.{ Failure, Success }
import scalaz.Validation.FlatMap._
import scalaz.std.AllInstances._
import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }

class HorarioEmpresaActorSupervisor extends Actor with ActorLogging {

  val horarioEmpresaActor = context.actorOf(
    Props[HorarioEmpresaActor].withRouter(RoundRobinPool(nrOfInstances = 2)),
    "horarioEmpresaActor"
  )

  def receive = {
    case message: Any => horarioEmpresaActor forward message
  }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }

}

class HorarioEmpresaActor extends Actor
    with ActorLogging with ValidacionesAutenticacionUsuarioEmpresarial {

  import co.com.alianza.util.json.MarshallableImplicits._
  import context.dispatcher

  def receive = {
    case message: ObtenerHorarioEmpresaMessage => obtenerHorarioEmpresa(message)
    case message: AgregarHorarioEmpresaMessage => agregarHorarioEmpresa(message)
    case message: DiaFestivoMessage => esDiaFestivo(message)
    case message: ValidarHorarioEmpresaMessage => validarHorarioTipoCliente(message)
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
      case Failure(failure) =>
        currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response: Option[HorarioEmpresa]) =>
            currentSender ! ResponseMessage(OK, response.toJson)
          case zFailure(error) =>
            currentSender ! error
        }
    }
  }

  def toEntity(message: AgregarHorarioEmpresaMessage, idEmpresa: Int): HorarioEmpresa = {
    implicit def toTime(hora: String): Time = Time.valueOf(hora)
    new HorarioEmpresa(idEmpresa, message.diaHabil, message.sabado, message.horaInicio, message.horaFin)
  }

  def agregarHorarioEmpresa(message: AgregarHorarioEmpresaMessage) = {
    val currentSender = sender()
    val result = {
      (for {
        idEmpresa <- ValidationT(DataAccessAdapter.obtenerIdEmpresa(message.idUsuario.get, message.tipoCliente))
        existeHorario <- ValidationT(DataAccessAdapter.existeHorarioEmpresa(idEmpresa))
        horarioEmpresa <- ValidationT(DataAccessAdapter.agregarHorarioEmpresa(toEntity(message, idEmpresa), existeHorario))
      } yield horarioEmpresa).run
    }
    result onComplete {
      case Failure(failure) =>
        currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response: Boolean) =>
            currentSender ! ResponseMessage(OK, response.toJson)
          case zFailure(error) =>
            currentSender ! error
        }
    }
  }

  def esDiaFestivo(message: DiaFestivoMessage) = {
    implicit def toDate(fecha: String): Date = Date.valueOf(fecha)
    val currentSender = sender()
    val result = {
      (for {
        existe <- ValidationT(DataAccessAdapter.existeDiaFestivo(message.fecha))
      } yield (existe)).run
    }
    result onComplete {
      case Failure(failure) =>
        currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response: Boolean) =>
            currentSender ! ResponseMessage(OK, response.toJson)
          case zFailure(error) =>
            currentSender ! error
        }
    }
  }

  def validarHorarioTipoCliente(message: ValidarHorarioEmpresaMessage) = {
    val currentSender = sender()
    message.user.tipoCliente match {
      case TiposCliente.clienteIndividual => currentSender ! ResponseMessage(OK, true.toJson)
      case TiposCliente.comercialSAC =>
        val tipoId: Int = message.tipoIdentificacion.getOrElse(0)
        val tipoCliente = TiposCliente.tipoClientePorTipoIdentificacion(tipoId)
        tipoCliente match {
          case TiposCliente.clienteIndividual => currentSender ! ResponseMessage(OK, true.toJson)
          case _ => validarHorario(message.idUsuarioRecurso.get, currentSender)
        }
      case _ => validarHorario(message.user.identificacion, currentSender)
    }
  }

  def validarHorario(identificacionEmpresa: String, currentSender: ActorRef) = {

    println("identificacion del horario => " + identificacionEmpresa)

    val result = {
      (for {
        empresa <- ValidationT(obtenerEmpresaPorNit(identificacionEmpresa))
        horario <- ValidationT(obtenerHorarioEmpresa(empresa.id))
        horarioValido <- ValidationT(validarHorarioEmpresa(horario))
      } yield (horarioValido)).run
    }
    result onComplete {
      case Failure(failure) =>
        currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response: Boolean) =>
            currentSender ! ResponseMessage(OK, response.toJson)
          case zFailure(error) =>
            currentSender ! error
        }
    }
  }

  def obtenerEmpresaPorNit(nit: String): Future[Validation[ErrorAutenticacion, Empresa]] = {
    log.info("Obteniendo empresa por nit")
    val future: Future[Validation[PersistenceException, Option[Empresa]]] = DataAccessAdapter.obtenerEmpresaPorNit(nit)
    future.map(
      _.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap {
        case Some(empresa) => Validation.success(empresa)
        case None => Validation.failure(ErrorCredencialesInvalidas())
      }
    )
  }

  /**
   * Obtiene el horario
   * @param idEmpresa
   * @return
   */
  def obtenerHorarioEmpresa(idEmpresa: Int): Future[Validation[ErrorAutenticacion, Option[HorarioEmpresaDTO]]] = {
    log.info("Obteniendo horario empresa")
    val future: Future[Validation[PersistenceException, Option[HorarioEmpresaDTO]]] = DataAccessAdapter.obtenerHorarioEmpresa(idEmpresa)
    future.map(
      _.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap {
        case Some(horarioEmpresa) => Validation.success(Some(horarioEmpresa))
        case None => Validation.success(None)
      }
    )
  }

}
