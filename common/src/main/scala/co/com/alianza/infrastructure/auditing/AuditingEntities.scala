package co.com.alianza.infrastructure.auditing

object AuditingEntities {

  case class AudRequest(metodoHttp: String, url: String, cuerpo: Any, ip: String)

  case class AudResponse(codigo: String, razon: String)

}
