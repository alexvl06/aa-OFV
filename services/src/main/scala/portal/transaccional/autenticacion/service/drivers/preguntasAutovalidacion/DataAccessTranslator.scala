package portal.transaccional.autenticacion.service.drivers.preguntasAutovalidacion

import co.com.alianza.infrastructure.dto.Pregunta
import co.com.alianza.persistence.entities.{ PreguntaAutovalidacion => ePregunta }

/**
 * Created by seven4n 2016
 */
object DataAccessTranslator {

  def entityToDto(e: ePregunta): Pregunta = Pregunta(e.id, e.pregunta)

}
