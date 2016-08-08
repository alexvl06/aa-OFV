package portal.transaccional.autenticacion.service.util.JsonFormatters

import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.dto.{ Pregunta, Respuesta }
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import portal.transaccional.autenticacion.service.web.autenticacion.{ AutenticarUsuarioComercialRequest, AutenticarRequest, AutenticarUsuarioEmpresarialRequest }
import portal.transaccional.autenticacion.service.web.autorizacion.InvalidarTokenRequest
import portal.transaccional.autenticacion.service.web.ip.AgregarIpRequest
import portal.transaccional.autenticacion.service.web.preguntasAutovalidacion.{ GuardarRespuestasRequest, ResponseObtenerPreguntas, ResponseObtenerPreguntasComprobar, RespuestasComprobacionRequest }

trait DomainJsonFormatters {

  this: CommonRESTFul =>

  //autenticacion
  implicit val autenticarFormatter = jsonFormat3(AutenticarRequest)
  implicit val autenticarUsuarioEmpresarialFormatter = jsonFormat3(AutenticarUsuarioEmpresarialRequest)
  implicit val autenticarUsuarioComercialRequestFormatter = jsonFormat3(AutenticarUsuarioComercialRequest)

  //autorizacion
  implicit val invalidarTokenFormatter = jsonFormat1(InvalidarTokenRequest)

  //validacion
  implicit val validacionExceptionFormatter = jsonFormat2(ValidacionException)

  //preguntasAutovalidacion
  implicit val preguntaFormatter = jsonFormat2(Pregunta)
  implicit val responseObtenerPreguntasFormatter = jsonFormat2(ResponseObtenerPreguntas)
  implicit val responseObtenerPreguntasComprobar = jsonFormat2(ResponseObtenerPreguntasComprobar)

  // respuestas
  implicit val respuestaFormatter = jsonFormat2(Respuesta)
  implicit val respuestasRequestFormatter = jsonFormat1(GuardarRespuestasRequest)
  implicit val respuestasComprobacionRequest = jsonFormat2(RespuestasComprobacionRequest)

  //ip
  implicit val ipFormatter = jsonFormat2(AgregarIpRequest)
}
