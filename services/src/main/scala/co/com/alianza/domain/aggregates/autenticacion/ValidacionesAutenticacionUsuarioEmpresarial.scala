package co.com.alianza.domain.aggregates.autenticacion

import java.sql.{Time, Timestamp}
import java.util.{Calendar, Date}
import akka.actor.{Actor, ActorLogging}
import akka.util.Timeout

import co.com.alianza.domain.aggregates.autenticacion.errores._
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuarios.{DataAccessAdapter => UsDataAdapter}
import co.com.alianza.infrastructure.anticorruption.empresa.{DataAccessTranslator => EmpDataAccessTranslator}
import co.com.alianza.infrastructure.dto._
import enumerations.empresa.EstadosDeEmpresaEnum
import scala.concurrent.Future
import scala.concurrent.duration._

import scalaz.Validation
import scalaz.Validation.FlatMap._

/**
 * Created by manuel on 9/03/15.
 */
trait ValidacionesAutenticacionUsuarioEmpresarial {
  self: Actor with ActorLogging =>

  import context.dispatcher
  implicit private [this] val timeout: Timeout = 10 seconds

  def validaEstadoEmpresa(empresa: Empresa): Future[Validation[ErrorAutenticacion, Boolean]] = {
    log.info("Validando el estado de la empresa")
    val empresaActiva : Int = EstadosDeEmpresaEnum.activa.id
    empresa.estado match {
      case `empresaActiva` => Future.successful(Validation.success(true))
      case _ => Future.successful(Validation.failure(ErrorEmpresaAccesoDenegado()))
    }
  }

  def validarHorarioEmpresa(horarioEmpresa: Option[HorarioEmpresa]): Future[Validation[ErrorAutenticacion, Boolean]] =  {
    log.info("Validando el horario")
    def calendarToTime(c: Calendar): Time = {
      Time.valueOf(c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND))
    }
    def calendarToDate(c: Calendar): java.sql.Date = {
      java.sql.Date.valueOf(c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH))
    }
    horarioEmpresa match {
      case None => Future.successful(Validation.success(true))
      case Some(horario) => {
        //Obtener la hora actual
        val calendar = Calendar.getInstance()
        val horaActual = calendarToTime(calendar)
        val horarioNoPermitido = horario.horaInicio.after(horaActual) || horario.horaFin.before(horaActual)
        //1. Validar si es domingo
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
          Future.successful(Validation.failure(ErrorHorarioIngresoEmpresa()))
        //2. Si esta habilitado el sábado, validar
        else if (!horario.sabado && calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
          Future.successful(Validation.failure(ErrorHorarioIngresoEmpresa()))
        //3. Validar el día hábil
        else if (horario.diaHabil) {
          UsDataAdapter.existeDiaFestivo(calendarToDate(calendar)).map(
            _.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap {
              case true => Validation.failure(ErrorHorarioIngresoEmpresa())
              case _ => {
                if(horarioNoPermitido) Validation.failure(ErrorHorarioIngresoEmpresa())
                else Validation.success(true)
              }
            }
          )
        }
        //4. Validar la hora de inicio y de fin
        else if (horarioNoPermitido)
          Future.successful(Validation.failure(ErrorHorarioIngresoEmpresa()))
        else
          Future.successful(Validation.success(true))
      }
    }

  }

}
