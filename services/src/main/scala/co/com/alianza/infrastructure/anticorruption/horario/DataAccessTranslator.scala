package co.com.alianza.infrastructure.anticorruption.horario

import co.com.alianza.persistence.entities.{HorarioEmpresa => eHorarioEmpresa}
import co.com.alianza.infrastructure.dto.HorarioEmpresa

/**
 * Created by manuel on 9/03/15.
 */
object DataAccessTranslator {

  def translateHorarioEmpresa(e: eHorarioEmpresa) = {
    import e._
    HorarioEmpresa(diaHabil, sabado, horaInicio,  horaFin)
  }

}
