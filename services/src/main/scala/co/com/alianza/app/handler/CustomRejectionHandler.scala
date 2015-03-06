package co.com.alianza.app.handler

import spray.routing.RejectionHandler
import spray.http.StatusCodes._
import co.com.alianza.infrastructure.security.AuthenticationFailedRejection
import co.com.alianza.infrastructure.security.AuthenticationFailedRejection.{CredentialsMissing, CredentialsRejected, ServiceErrorRejected}
import spray.http.HttpHeader

/**
 *
 * @author smontanez
 */
object CustomRejectionHandler extends  {

  import RejectionHandler._

  implicit val extended : RejectionHandler = apply{

    case AuthenticationFailedRejection(cause, challengeHeaders, Some(statusCode), bodyError ) :: _ =>
      ctx => ctx.complete((statusCode, challengeHeaders, bodyError getOrElse ""):(Int, List[HttpHeader], String))
  }
}