package co.com.alianza.infrastructure.auditing

/**
 * Created by david on 6/07/15.
 */
object AuditingUser {

  case class AuditingUserData(tipoIdentificacion: Int, identificacion: String, usuario: Option[String])

}
