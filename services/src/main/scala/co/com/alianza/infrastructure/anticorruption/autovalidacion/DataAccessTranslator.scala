package co.com.alianza.infrastructure.anticorruption.autovalidacion

import co.com.alianza.infrastructure.dto._

import co.com.alianza.persistence.entities.{ RespuestasAutovalidacionUsuario, PreguntaAutovalidacion }

/**
 *
 * @author seven4n
 */
object DataAccessTranslator {

  def translatePregunta(pregunta: Seq[PreguntaAutovalidacion]) = {
    pregunta map (pre => Pregunta(pre.id, pre.pregunta))
  }

  def translateRespuesta(respuesta: List[RespuestasAutovalidacionUsuario]) = {
    respuesta map (res => Respuesta(res.idPregunta, res.respuesta))
  }

  def toPreguntaList(pregunta: Seq[(PreguntaAutovalidacion, RespuestasAutovalidacionUsuario)]) = {
    pregunta.map(pre => Pregunta(pre._1.id, pre._1.pregunta)).toList
  }

  def toRespuestaList(pregunta: Seq[(PreguntaAutovalidacion, RespuestasAutovalidacionUsuario)]) = {
    pregunta.map(pre => Respuesta(pre._1.id, pre._2.respuesta)).toList
  }

  def toRespuestaCompletaList(pregunta: Seq[(PreguntaAutovalidacion, RespuestasAutovalidacionUsuario)]): List[RespuestaCompleta] = {
    pregunta.map(pre => RespuestaCompleta(pre._1.id, pre._1.pregunta, pre._2.respuesta)).toList
  }

}