package co.com.alianza.commons.enumerations

import spray.json.{ JsString, JsValue, RootJsonFormat }

import scalaz.Validation

object TiposCliente extends Enumeration {

  type TiposCliente = Value

  val clienteIndividual,
  agenteEmpresarial,
  clienteAdministrador,
  comercialAdmin,
  comercialFiduciaria,
  comercialValores,
  clienteAdminInmobiliario,
  agenteInmobiliario,
  agenteInmobiliarioInterno = Value

}

