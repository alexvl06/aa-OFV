package portal.transaccional.autenticacion.service.util.JsonFormatters

import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.dto.Pregunta
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import portal.transaccional.autenticacion.service.web.autenticacion.{ AutenticarRequest, AutenticarUsuarioEmpresarialRequest }
import portal.transaccional.autenticacion.service.web.autorizacion.InvalidarTokenRequest
import portal.transaccional.autenticacion.service.web.preguntasAutovalidacion.PreguntasResponse

trait DomainJsonFormatters {

  this: CommonRESTFul =>

  //autenticacion
  implicit val autenticarFormatter = jsonFormat3(AutenticarRequest)
  implicit val AutenticarUsuarioEmpresarialFormatter = jsonFormat3(AutenticarUsuarioEmpresarialRequest)

  //autorizacion
  implicit val invalidarTokenFormatter = jsonFormat1(InvalidarTokenRequest)

  //validacion
  implicit val validacionExceptionFormatter = jsonFormat2(ValidacionException)

  //preguntasAutovalidacion
  implicit val preguntaFormatter = jsonFormat2(Pregunta)
  implicit val preguntasResponseFormatter = jsonFormat2(PreguntasResponse)

}
