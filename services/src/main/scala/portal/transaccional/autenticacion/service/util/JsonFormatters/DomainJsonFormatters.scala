package portal.transaccional.autenticacion.service.util.JsonFormatters

import java.util.Date

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.dto.Usuario
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import portal.transaccional.autenticacion.service.web.Ipsuario.AgregarIpHabitualRequest
import portal.transaccional.autenticacion.service.web.autenticacion.{ AutenticarRequest, AutenticarUsuarioEmpresarialRequest }
import portal.transaccional.autenticacion.service.web.autorizacion.InvalidarTokenRequest
import spray.json.{ JsNumber, JsString, JsValue, RootJsonFormat }

trait DomainJsonFormatters {

  this: CommonRESTFul =>

  //autenticacion
  implicit val autenticarFormatter = jsonFormat3(AutenticarRequest)
  implicit val AutenticarUsuarioEmpresarialFormatter = jsonFormat3(AutenticarUsuarioEmpresarialRequest)

  //autorizacion
  implicit val invalidarTokenFormatter = jsonFormat1(InvalidarTokenRequest)

  //ipsUsuarios
  implicit val agregarIpHabitualFormatter = jsonFormat2(AgregarIpHabitualRequest)

  //validacion
  implicit val validacionExceptionFormatter = jsonFormat2(ValidacionException)

}
