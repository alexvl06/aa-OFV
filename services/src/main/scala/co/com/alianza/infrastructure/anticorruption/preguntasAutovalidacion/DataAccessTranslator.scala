package co.com.alianza.infrastructure.anticorruption.preguntasAutovalidacion

import co.com.alianza.infrastructure.dto._
import co.com.alianza.persistence.entities.PreguntasConfrontacionAutovalidacion


/**
 *
 * @author seven4n
 */
object DataAccessTranslator {

  def translatePregunta(pregunta: List[PreguntasConfrontacionAutovalidacion]) = {
    pregunta map (pre => Pregunta(pre.id, pre.pregunta))
  }

}