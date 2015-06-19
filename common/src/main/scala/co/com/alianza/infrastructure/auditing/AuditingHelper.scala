package co.com.alianza.infrastructure.auditing

import akka.actor._
import co.com.alianza.infrastructure.auditing.AuditingEntities.{AudRequest, AudResponse}
import co.com.alianza.infrastructure.auditing.AuditingMessages.AuditRequest
import spray.http.{HttpRequest, HttpResponse}
import spray.routing._

object AuditingHelper extends AuditingHelper

trait AuditingHelper {

  def requestWithAuiditing(ctx: RequestContext, kafkaTopic: String, elasticIndex: String, ip: String, kafkaActor: ActorSelection): RequestContext = {
    ctx.withRouteResponseMapped {
      case response: HttpResponse =>

        val httpReq: HttpRequest = ctx.request

        val auditingMsg: AuditRequest =
          AuditRequest(
            AudRequest(
              httpReq.method.toString(),
              httpReq.uri.toRelative.toString(),
              httpReq.entity.asString,
              ip
            ),
            AudResponse(
              response.status.intValue.toString,
              response.status.reason
            ),
            kafkaTopic,
            elasticIndex,
            httpReq.uri.toRelative.toString().split("/")(1)
          )

        kafkaActor ! auditingMsg
        response
      case a => a
    }
  }

}
