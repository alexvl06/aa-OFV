package co.com.alianza.infrastructure.auditing

import akka.actor._
import co.com.alianza.infrastructure.auditing.AuditingEntities.{AudRequest, AudResponse}
import co.com.alianza.infrastructure.auditing.AuditingMessages.AuditRequest
import co.com.alianza.util.json.JsonUtil
import spray.http.{HttpRequest, HttpResponse}
import spray.routing._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Try, Success, Failure}
import scalaz.{Validation, Success => zSuccess, Failure => zFailure}

object AuditingHelper extends AuditingHelper

trait AuditingHelper {

  def requestWithAuiditing(ctx: RequestContext, kafkaTopic: String, elasticIndex: String, ip: String, kafkaActor: ActorSelection, requestParameters : Any): RequestContext = {
    ctx.withRouteResponseMapped {
      case response: HttpResponse =>

        val httpReq: HttpRequest = ctx.request

        val auditingMsg: AuditRequest =
          AuditRequest(
            AudRequest(
              httpReq.method.toString(),
              httpReq.uri.toRelative.toString(),
              JsonUtil.toJson(requestParameters),
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

  def requestWithFutureAuditing[E,H](ctx: RequestContext, kafkaTopic: String, elasticIndex: String, ip: String, kafkaActor: ActorSelection, futureAuditParameters : Future[Validation[E,Option[H]]])(implicit executionContext : ExecutionContext): RequestContext = {
    ctx.withRouteResponseMapped {
      case response: HttpResponse =>
        futureAuditParameters onComplete {
          case Success(validationUsuario) => {
            validationUsuario match {
              case zSuccess(usuario) => {
                val httpReq: HttpRequest = ctx.request
                val auditingMsg: AuditRequest =
                  AuditRequest(
                    AudRequest(
                      httpReq.method.toString(),
                      httpReq.uri.toRelative.toString(),
                      JsonUtil.toJson(usuario),
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
              }
            }
          }
        }
        response
      case a => a
    }

  }

}
