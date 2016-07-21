package co.com.alianza.persistence.repositories

import java.sql.Timestamp

import org.joda.time.DateTime

import scala.concurrent.{ ExecutionContext, Future }
import co.com.alianza.exceptions.PersistenceException

import scala.util.Try
import scalaz.Validation

import co.com.alianza.persistence.entities._

import slick.lifted.TableQuery
import slick.jdbc.JdbcBackend.Session
import CustomDriver.simple._

/**
 * Created by david on 12/06/14.
 */
class ReglasContrasenasRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val reglasContrasenas = TableQuery[ReglasContrasenasTable]
  val usuarios = TableQuery[UsuarioTable]

  def obtenerReglas(): Future[Validation[PersistenceException, List[ReglasContrasenas]]] = loan {
    session =>
      val resultTry = obtenerReglasTry(session: Session)
      resolveTry(resultTry, "Consulta todas las reglas contraseñas")
  }

  private def obtenerReglasTry(implicit session: Session): Try[List[ReglasContrasenas]] = Try {
    reglasContrasenas.list
  }

  def obtenerRegla(llave: String): Future[Validation[PersistenceException, Option[ReglasContrasenas]]] = loan {
    session =>
      val resultTry = obtenerReglaTry(session: Session, llave)
      resolveTry(resultTry, "Consulta la regla contraseña solicitada")
  }

  private def obtenerReglaTry(implicit session: Session, llave: String): Try[Option[ReglasContrasenas]] = Try {
    val result: Option[ReglasContrasenas] = reglasContrasenas.withFilter(x => x.llave.like(llave)).list.headOption
    result
  }

  def actualizarContrasena(pw_nuevo: String, idUsuario: Int): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val query = for {
        u <- usuarios if u.id === idUsuario
      } yield (u.contrasena, u.fechaActualizacion, u.numeroIngresosErroneos)
      val fechaAct = new org.joda.time.DateTime().getMillis
      val act = (Some(pw_nuevo), new Timestamp(fechaAct), 0)
      val resultTry = Try { query.update(act) }
      resolveTry(resultTry, "Actualizar Contrasena y fecha de actualizacion")
  }

}