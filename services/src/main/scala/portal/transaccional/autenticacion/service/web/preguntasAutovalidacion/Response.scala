package portal.transaccional.autenticacion.service.web.preguntasAutovalidacion

import co.com.alianza.infrastructure.dto.Pregunta;

case class ResponseObtenerPreguntas(preguntas: List[Pregunta], numeroPreguntas: Int)

case class ResponseObtenerPreguntasComprobar(preguntas: List[Pregunta], numeroIntentos: Int)
