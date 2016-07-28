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

  //date
  implicit val dateFormatter = new RootJsonFormat[Date] {
    private[this] val format = new java.text.SimpleDateFormat("dd-MM-yyyy")
    def read(json: JsValue) = format.parse(json.convertTo[String])
    def write(date: Date) = JsString(format.format(date))
  }

  //TiposCliente
  implicit val tipoClienteFormat = new RootJsonFormat[TiposCliente] {
    def read(json: JsValue): TiposCliente = json.convertTo[Int]
    def write(tipo: TiposCliente) = JsNumber(tipo.id)
  }

  //autenticacion
  implicit val autenticarFormatter = jsonFormat3(AutenticarRequest)
  implicit val AutenticarUsuarioEmpresarialFormatter = jsonFormat3(AutenticarUsuarioEmpresarialRequest)

  //autorizacion
  implicit val invalidarTokenFormatter = jsonFormat1(InvalidarTokenRequest)

  //ipsUsuarios
  implicit val agregarIpHabitualFormatter = jsonFormat2(AgregarIpHabitualRequest)

  //validacion
  implicit val validacionExceptionFormatter = jsonFormat2(ValidacionException)

  //usuario
  implicit val usuarioFormatter = jsonFormat10(Usuario)

}
