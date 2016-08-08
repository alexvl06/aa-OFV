package portal.transaccional.autenticacion.service.web.autorizacion

import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente

/**
 * Created by jonathan on 27/07/16.
 */
case class InvalidarTokenRequest(token: String)

case class AuditityUser(correo: String, documento: String, tipoDocuemnto: String, usuario: String, ultimaIp: String, ultimoIngreso: String,
  tipoCliente: String)

