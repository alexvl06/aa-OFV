package co.com.alianza.infrastructure.auditing

import co.com.alianza.infrastructure.auditing.AuditingEntities.{ AudRequest, AudResponse }

object AuditingMessages {

  case class AuditRequest(
    request: AudRequest,
    response: AudResponse,
    kafkaTopic: String,
    elasticIndex: String,
    elasticDocumentType: String
  )

}
