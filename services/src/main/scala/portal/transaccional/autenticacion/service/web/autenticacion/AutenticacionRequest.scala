package portal.transaccional.autenticacion.service.web.autenticacion

import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente

case class AutenticarRequest(tipoIdentificacion: Int, numeroIdentificacion: String, password: String)

case class AutenticarUsuarioEmpresarialRequest(nit: String, usuario: String, password: String)

case class AutenticarUsuarioComercialRequest(usuario: String, tipoUsuario: Int, contrasena: String, tipoCliente: Option[String])
/**OFV LOGIN FASE 1**/
case class UsuarioGenRequest(tipoIngreso: Int, usuario: Option[String], email: Option[String], numeroIdentificacion: Option[String], tipoIdentificacion: Option[Int], password: String)