package portal.transaccional.autenticacion.service.util.JsonFormatters

import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import portal.transaccional.autenticacion.service.web.Ipsuario.AgregarIpHabitualRequest
import portal.transaccional.autenticacion.service.web.autenticacion.{ AutenticarRequest, AutenticarUsuarioEmpresarialRequest }

trait DomainJsonFormatters {

  this: CommonRESTFul =>
  //autenticacion
  implicit val autenticarFormatter = jsonFormat4(AutenticarRequest)
  implicit val AutenticarUsuarioEmpresarialFormatter = jsonFormat5(AutenticarUsuarioEmpresarialRequest)

  //ipsUsuarios
  implicit val agregarIpHabitualFormatter = jsonFormat2(AgregarIpHabitualRequest)

}
