package co.com.alianza.infrastructure.security

import spray.http.HttpHeader
import spray.routing.Rejection

case class AuthenticationFailedRejection(cause: AuthenticationFailedRejection.Cause, challengeHeaders: List[HttpHeader], statusCode: Option[Int] = None, bodyError: Option[String] = None) extends Rejection

object AuthenticationFailedRejection {
  /**
   * Signals the cause of the failed authentication.
   */
  sealed trait Cause

  /**
   * Signals the cause of the rejecting was that the user could not be authenticated, because the `WWW-Authenticate`
   * header was not supplied.
   */
  case object CredentialsMissing extends Cause

  /**
   * Signals the cause of the rejecting was that the user could not be authenticated, because the supplied credentials
   * are invalid.
   */
  case object CredentialsRejected extends Cause

  /**
   * Indica que la causa del fallo se presentó por un error consumiendo el servicio de autenticación
   */
  case object ServiceErrorRejected extends Cause
}
