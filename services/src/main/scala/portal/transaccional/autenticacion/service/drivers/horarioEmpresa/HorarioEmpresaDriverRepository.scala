package portal.transaccional.autenticacion.service.drivers.horarioEmpresa

import java.sql.{ Date, Time }
import java.util.Calendar

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.persistence.entities.HorarioEmpresa
import portal.transaccional.autenticacion.service.drivers.empresa.EmpresaDriverRepository
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.{ HorarioEmpresaDAOs, DiaFestivoDAOs, EmpresaDAO, HorarioEmpresaDAO }
import portal.transaccional.autenticacion.service.drivers.horarioEmpresa.{ DataAccessTranslator => horarioTranslator }
import portal.transaccional.autenticacion.service.web.horarioEmpresa.ResponseObtenerHorario

import scala.concurrent.{ ExecutionContext, Future }

case class HorarioEmpresaDriverRepository(
    empresaRepo: EmpresaDriverRepository,
    horarioEmpresaDAO: HorarioEmpresaDAOs,
    diaFestivoDAO: DiaFestivoDAOs
)(implicit val ex: ExecutionContext) extends HorarioEmpresaRepository {

  def obtener(identificacion: String): Future[Option[ResponseObtenerHorario]] = {
    for {
      empresa <- empresaRepo.getByIdentity(identificacion)
      _ <- empresaRepo.validarEmpresa(empresa)
      horarioEmpresa <- horarioEmpresaDAO.obtenerHorarioEmpresa(empresa.get.id)
    } yield {
      horarioEmpresa match {
        case Some(horario) => horarioTranslator.entityToDto(horario)
        case _ => None
      }
    }
  }

  private def obtenerHorario(identificacion: String): Future[Option[HorarioEmpresa]] = {
    for {
      empresa <- empresaRepo.getByIdentity(identificacion)
      _ <- empresaRepo.validarEmpresa(empresa)
      optionHorario <- horarioEmpresaDAO.obtenerHorarioEmpresa(empresa.get.id)
    } yield optionHorario
  }

  def agregar(usuario: UsuarioAuth, diaHabil: Boolean, sabado: Boolean, horaInicio: String, horaFin: String): Future[Int] = {
    for {
      empresa <- empresaRepo.getByIdentity(usuario.identificacion)
      _ <- empresaRepo.validarEmpresa(empresa)
      optionHorario <- horarioEmpresaDAO.obtenerHorarioEmpresa(empresa.get.id)
      agregar <- agregarHorario(optionHorario, empresa.get.id, diaHabil, sabado, horaInicio, horaFin)
    } yield agregar
  }

  private def agregarHorario(optionHorario: Option[HorarioEmpresa], idEmpresa: Int, diaHabil: Boolean, sabado: Boolean,
    horaInicio: String, horaFin: String): Future[Int] = {
    val horarioEntity: HorarioEmpresa = horarioTranslator.toEntity(idEmpresa, diaHabil, sabado, horaInicio, horaFin)
    optionHorario match {
      case Some(_) => horarioEmpresaDAO.update(horarioEntity)
      case _ => horarioEmpresaDAO.create(horarioEntity)
    }
  }

  def esDiaFestivo(fecha: String): Future[Boolean] = {
    diaFestivoDAO.obtener(Date.valueOf(fecha)).map(_.isDefined)
  }

  def validar(user: UsuarioAuth, idUsuarioRecurso: Option[String], tipoIdentificacion: Option[Int]): Future[Boolean] = {
    user.tipoCliente match {
      case TiposCliente.agenteEmpresarial | TiposCliente.clienteAdministrador =>
        for {
          horario <- obtenerHorario(user.identificacion)
          valido <- validarHorario(horario)
        } yield valido
      case TiposCliente.comercialSAC =>
        val tipoCliente: TiposCliente = TiposCliente.tipoClientePorTipoIdentificacion(tipoIdentificacion.getOrElse(0))
        tipoCliente match {
          case TiposCliente.clienteAdministrador =>
            for {
              horario <- obtenerHorario(idUsuarioRecurso.getOrElse(""))
              valido <- validarHorario(horario)
            } yield valido
          case _ => Future.successful(true)
        }
      case _ => Future.successful(true)
    }
  }

  def validarHorario(optionHorario: Option[HorarioEmpresa]): Future[Boolean] = {
    def calendarToTime(c: Calendar): Time = {
      Time.valueOf(c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND))
    }
    def calendarToDate(c: Calendar): java.sql.Date = {
      java.sql.Date.valueOf(c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH))
    }
    optionHorario match {
      case Some(horario) => {
        //Obtener la hora actual de la fecha real
        val calendar = Calendar.getInstance()
        val horaActual = calendarToTime(calendar)
        val horarioNoPermitido = horario.horaInicio.after(horaActual) || horario.horaFin.before(horaActual)
        //1. Validar si es domingo
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
          Future.successful(false)
        } //2. Si esta habilitado el sábado, validar
        else if (!horario.sabado && calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
          Future.successful(false)
        } //3. Validar el día hábil
        else if (horario.diaHabil) {
          diaFestivoDAO.obtener(calendarToDate(calendar)).map {
            _ match {
              case Some(diaFestivo) => false
              case _ => !horarioNoPermitido
            }
          }
        } //4. Validar la hora de inicio y de fin
        else if (horarioNoPermitido) {
          Future.successful(false)
        } else {
          Future.successful(true)
        }
      }
      case _ => Future.successful(true)
    }
  }

}
