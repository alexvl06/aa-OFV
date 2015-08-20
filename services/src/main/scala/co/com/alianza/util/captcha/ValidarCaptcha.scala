package co.com.alianza.util.captcha
import net.tanesha.recaptcha.ReCaptchaImpl
import scala.concurrent.{ExecutionContext, Future}
import com.typesafe.config.Config
import scala.util.{Failure, Success, Try}
import scalaz.{Failure => zFailure, Success => zSuccess, Validation}
/**
 *
 * @author smontanez
 */
class ValidarCaptcha(implicit val config:Config, val ex:ExecutionContext) {


  def validarCaptcha(ip:String, challenge:String, uresponse:String): Future[Validation[ErrorValidacionCaptcha, Boolean]] = Future{
    Try{
      val reCaptcha = new ReCaptchaImpl()
      reCaptcha.setPrivateKey(config.getString("alianza.captcha.privatekey"))
      val reCaptchaResponse = reCaptcha.checkAnswer(ip, challenge, uresponse)
      reCaptchaResponse.isValid
    } match {
      case Success(value)      =>
        zSuccess(value)
      case Failure(exception)  =>
        zFailure(ErrorValidacionCaptchaInternal)
    }
  }
}


sealed trait ErrorValidacionCaptcha
case object  ErrorValidacionCaptcha extends ErrorValidacionCaptcha
case object  ErrorValidacionCaptchaInternal extends ErrorValidacionCaptcha