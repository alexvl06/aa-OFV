package co.com.alianza.persistence.repositories

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.{ CustomDriver, PinUsuario, PinUsuarioTable }

import scala.concurrent.{ Future, ExecutionContext }

import scala.slick.lifted.TableQuery
import scala.slick.jdbc.JdbcBackend.SessionDef
import CustomDriver.simple._

import scala.util.Try
import scalaz.Validation

class PinRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val pin = TableQuery[PinUsuarioTable]

  def obtenerPin(tokenHash: String): Future[Validation[PersistenceException, Option[PinUsuario]]] = loan {
    implicit session =>
      val resultTry = Try { pin.filter(_.tokenHash === tokenHash).list.headOption }
      resolveTry(resultTry, "Consulta un pin dado su hash")
  }

  def eliminarPin(tokenHash: String): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try { pin.filter(_.tokenHash === tokenHash).delete }
      resolveTry(resultTry, "Elimina un pin dado su hash")
  }

}
