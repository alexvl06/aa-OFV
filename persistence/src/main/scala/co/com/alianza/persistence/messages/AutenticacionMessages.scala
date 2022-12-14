package co.com.alianza.persistence.messages

/**
 *
 * @author seven4n
 */
case class AutenticacionRequest(tipoIdentificacion: Int, numeroIdentificacion: String, password: String, clientIp: Option[String])

case class AgregarIpHabitualRequest(idUsuario: Int, clientIp: Option[String])

case class ValidarTokenRequest(token: String)

case class InvalidarTokenRequest(token: String)
