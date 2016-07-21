package co.com.alianza.persistence.repositories

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.{ CustomDriver, PinUsuarioEmpresarialAdmin, PinUsuarioEmpresarialAdminTable }

import scala.concurrent.{ Future, ExecutionContext }

import slick.lifted.TableQuery
import slick.jdbc.JdbcBackend.SessionDef
import CustomDriver.simple._

import scala.util.Try
import scalaz.Validation

/**
 * Created by manuel on 6/01/15.
 */
class PinUsuarioEmpresarialAdminRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val pin = TableQuery[PinUsuarioEmpresarialAdminTable]

  def obtenerPin(tokenHash: String): Future[Validation[PersistenceException, Option[PinUsuarioEmpresarialAdmin]]] = loan {
    implicit session =>
      val resultTry: Try[Any] = Try { pin.filter(_.tokenHash === tokenHash).list.headOption }
      resolveTry(resultTry, "Consulta un pin de cliente administrador dado su hash")
  }

  def eliminarPin(tokenHash: String): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try { pin.filter(_.tokenHash === tokenHash).delete }
      resolveTry(resultTry, "Elimina un pin de cliente administrador dado su hash")
  }

}
