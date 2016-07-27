package portal.transaccional.autenticacion.service.util.JsonFormatters

import co.com.alianza.exceptions.ValidacionException
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import portal.transaccional.autenticacion.service.web.Ipsuario.AgregarIpHabitualRequest
import portal.transaccional.autenticacion.service.web.autenticacion.{ AutenticarRequest, AutenticarUsuarioEmpresarialRequest }

trait DomainJsonFormatters {

  this: CommonRESTFul =>
  //autenticacion
  implicit val autenticarFormatter = jsonFormat3(AutenticarRequest)
  implicit val AutenticarUsuarioEmpresarialFormatter = jsonFormat4(AutenticarUsuarioEmpresarialRequest)

  //ipsUsuarios
  implicit val agregarIpHabitualFormatter = jsonFormat2(AgregarIpHabitualRequest)

  //validacion
  implicit val validacionExceptionFormatter = jsonFormat2(ValidacionException)

}
