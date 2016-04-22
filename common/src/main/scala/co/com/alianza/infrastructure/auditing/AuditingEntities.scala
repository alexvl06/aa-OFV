package co.com.alianza.infrastructure.auditing

import co.com.alianza.infrastructure.auditing.AuditingUser.AuditingUserData

object AuditingEntities {

  case class AudRequest(metodoHttp: String, url: String, cuerpo: Any, ip: String, usuario: Option[AuditingUserData] = None)

  case class AudResponse(codigo: String, razon: String, body: String)

}
