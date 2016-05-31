package co.com.alianza.util.transformers

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.{ Success, Failure, Validation }

object Validation {

  def sequence[E, A](x: Validation[E, Future[A]])(implicit ex: ExecutionContext): Future[Validation[E, A]] = {
    x match {
      case Failure(f) => Future.successful(Failure(f))
      case Success(fa) => fa.map(a => Success(a))
    }
  }

}
