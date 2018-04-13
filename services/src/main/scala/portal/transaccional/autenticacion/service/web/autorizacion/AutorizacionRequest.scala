package portal.transaccional.autenticacion.service.web.autorizacion

/**
 * Created by jonathan on 27/07/16.
 */
case class InvalidarTokenRequest(token: String)

case class ValidarTokenAgenteRequest(token: String)

case class AuditityUser(correo: String, documento: String, tipoDocuemnto: String, usuario: String, ultimaIp: String, ultimoIngreso: String,
  tipoCliente: String)

case class UserResponseAuthGen(perfil: String, idUsuario: String)

