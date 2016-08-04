package portal.transaccional.autenticacion.service.drivers.pregunta

import co.com.alianza.infrastructure.dto.{Pregunta, RespuestaCompleta}
import co.com.alianza.persistence.entities.{PreguntaAutovalidacion => ePregunta, RespuestasAutovalidacionUsuario => eRespuesta}

/**
 * Created by seven4n 2016
 */
object DataAccessTranslator {

  def entityToDto(e: ePregunta): Pregunta = Pregunta(e.id, e.pregunta)

  def toPreguntaList(pregunta: Seq[(ePregunta, eRespuesta)]): List[Pregunta] = {
    pregunta.map(pre => Pregunta(pre._1.id, pre._1.pregunta)).toList
  }

  def toRespuestaCompletaList(pregunta: Seq[(ePregunta, eRespuesta)]): List[RespuestaCompleta] = {
    pregunta.map(pre => RespuestaCompleta(pre._1.id, pre._1.pregunta, pre._2.respuesta)).toList
  }

}
