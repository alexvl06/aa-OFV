package co.com.alianza.util.captcha

/* Ini GP960 - Reemplazo de Captcha Autoregistro AF/AV*/

import scala.concurrent.{ ExecutionContext, Future }
import com.typesafe.config.Config

import scala.util.{ Failure, Success, Try }
import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }

import co.com.alianza.microservices.CaptchaValid
/* Fin GP960 - Reemplazo de Captcha Autoregistro AF/AV*/

/**
 *
 * @author smontanez
 */
class ValidarCaptcha(implicit val config: Config, val ex: ExecutionContext) {

  /* Ini GP960 - Reemplazo de Captcha Autoregistro AF/AV*/
  def validarCaptcha(ip: String, challenge: String, uresponse: String): Future[Validation[ErrorValidacionCaptcha, Boolean]] = Future {
    Try {
      val respuesta = CaptchaValid.isCaptchaValid(config.getString("alianza.captcha.privatekey"), uresponse);
      respuesta.isSuccess
    } match {
      case Success(value) =>
        zSuccess(value)
      case Failure(exception) =>
        zFailure(ErrorValidacionCaptchaInternal)
    }
  }
  /* Fin GP960 - Reemplazo de Captcha Autoregistro AF/AV*/
}

sealed trait ErrorValidacionCaptcha
case object ErrorValidacionCaptcha extends ErrorValidacionCaptcha
case object ErrorValidacionCaptchaInternal extends ErrorValidacionCaptcha