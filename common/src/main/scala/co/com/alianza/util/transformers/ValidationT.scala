package co.com.alianza.util.transformers

/**
 *
 * @author smontanez
 */
import scalaz.{ Validation , Failure , Success , Monad }
import scala.concurrent.ExecutionContext


case class ValidationT[F[_]:Monad, E,A](val run: F[Validation[E,A]]) {

  val monad = implicitly(Monad[F])

  def map[B](f: A => B)(implicit executor: ExecutionContext): ValidationT[F,E,B] = {
    ValidationT( monad.map(run)( _.map(f) ) )
  }

  def flatMap[B](f: A => ValidationT[F,E,B])(implicit executor: ExecutionContext): ValidationT[F,E,B] = {
    val newRun : F[Validation[E,B]] = monad.bind(run) {
      v =>
        v match {
          case Failure(e) => monad.point(Failure(e))
          case Success(a) => f(a).run
        }
    }
    ValidationT( newRun )
  }

}