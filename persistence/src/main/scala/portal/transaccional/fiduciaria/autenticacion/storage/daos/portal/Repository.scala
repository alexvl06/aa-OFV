package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import java.net.SocketTimeoutException

import co.com.alianza.exceptions.{ LevelException, PersistenceException, TechnicalLevel, TimeoutLevel }
import oracle.net.ns.NetException
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig

import scala.concurrent.{ ExecutionContext, Future }
import shapeless.syntax.typeable._

/**
 * Crea la conexión con la configuración del driver que le estemos pasando y el contecto de ejecución.
 */
class Repository()(implicit context: ExecutionContext, dcConfig: DBConfig) {

  import dcConfig.driver.api._
  import dcConfig.DB

  def tryDB[T](f: Session => Future[T]): Future[T] = {
    try {
      val session = DB.createSession()
      val result: Future[T] = f(session)
      session.close()
      result
    } catch {
      case exception: Throwable => Future.failed(PersistenceException(exception, getLevelException(exception), exception.getMessage))
    }
  }

  private def getLevelException(exception: Throwable): LevelException = {
    Option(exception.getCause)
      .collect { case e: NetException if e.getCause.cast[SocketTimeoutException].nonEmpty => TimeoutLevel }
      .getOrElse(TechnicalLevel)
  }
}
