package co.com.alianza.infrastructure.anticorruption.preguntasAutovalidacion

import co.com.alianza.infrastructure.dto._
import co.com.alianza.infrastructure.messages.Respuesta
import co.com.alianza.persistence.entities.{RespuestasAutovalidacionUsuario, PreguntasAutovalidacion}


/**
 *
 * @author seven4n
 */
object DataAccessTranslator {

  def translatePregunta(pregunta: List[PreguntasAutovalidacion]) = {
    pregunta map (pre => Pregunta(pre.id, pre.pregunta))
  }

  def translateRespuesta(respuesta: List[RespuestasAutovalidacionUsuario]) = {
    respuesta map (res => Respuesta(res.idPregunta, res.respuesta))
  }

}