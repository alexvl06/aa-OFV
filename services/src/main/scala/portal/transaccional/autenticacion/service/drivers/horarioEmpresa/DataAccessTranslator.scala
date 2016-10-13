package portal.transaccional.autenticacion.service.drivers.horarioEmpresa

import co.com.alianza.persistence.entities.{ HorarioEmpresa => eHorarioEmpresa }
import portal.transaccional.autenticacion.service.web.horarioEmpresa.ResponseObtenerHorario

/**
 * Created by seven4n 2016
 */
object DataAccessTranslator {
  def entityToDto(e: eHorarioEmpresa): Option[ResponseObtenerHorario] =
    Option(ResponseObtenerHorario(e.diaHabil, e.sabado, e.horaInicio.toString, e.horaFin.toString))
}
