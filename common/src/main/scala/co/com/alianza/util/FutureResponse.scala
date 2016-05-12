package co.com.alianza.util

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.{ Failure => zFailure, Success => zSuccess, Validation }
import akka.actor.ActorRef
import scala.util.{ Failure, Success }

/**
 *
 * @author seven4n
 */
trait FutureResponse {

  def resolveFutureValidation[F, S, R](future: Future[Validation[F, S]], f: S => R, fe: Any => Any, currentSender: ActorRef)(implicit ex: ExecutionContext) = {
    future onComplete {
      case Failure(failure) =>
        currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response) =>
            try {
              val result = f(response)
              currentSender ! result
            } catch {
              case error: Exception =>
                currentSender ! error
            }
          case zFailure(error) =>
            val response = fe(error)
            currentSender ! response
        }
    }
  }

}
