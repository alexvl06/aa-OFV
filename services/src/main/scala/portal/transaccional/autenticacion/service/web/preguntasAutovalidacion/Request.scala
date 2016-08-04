package portal.transaccional.autenticacion.service.web.preguntasAutovalidacion

import co.com.alianza.infrastructure.dto.Respuesta

case class GuardarRespuestasRequest(respuestas: List[Respuesta])

case class RespuestasComprobacionRequest(respuestas: List[Respuesta], numeroIntentos: Int)
