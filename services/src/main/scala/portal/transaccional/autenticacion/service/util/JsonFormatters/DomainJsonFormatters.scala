package portal.transaccional.autenticacion.service.util.JsonFormatters

import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import portal.transaccional.autenticacion.service.web.autenticacion.AutenticacionRequest

trait DomainJsonFormatters {

  this: CommonRESTFul =>
  //autenticacion
  implicit val autenticacionFormatter = jsonFormat4(AutenticacionRequest)

}
