package co.com.alianza.util

import scala.concurrent.{ExecutionContext, Future}
import scalaz.{Failure => zFailure, Success => zSuccess, Validation}
import akka.actor.ActorRef
import scala.util.{Failure, Success}

/**
 *
 * @author seven4n
 */
trait FutureResponse {
  /**
   * Resuelve un futuro Future Validation, recibe una función para ejecutar en case de exito.
   *
   * Al actor referenciado en currentSender se le envía el resultado de la función o la excepción
   * del Validation o del Futuro
   *
   * @param future
   * @param f
   * @param currentSender
   * @param ex
   * @tparam F
   * @tparam S
   * @tparam R
   * @return
   */
  def resolveFutureValidation[F,S,R](future:Future[Validation[F,S]], f:S => R, currentSender:ActorRef)(implicit ex: ExecutionContext ) = {
    future onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response) =>
            try{
              val result = f(response)
              currentSender ! result
            }catch {
              case error:Exception =>
                currentSender ! error
            }
          case zFailure(error) =>
            currentSender ! error
        }
    }
  }

}
