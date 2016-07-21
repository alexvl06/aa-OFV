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

  def obtenerReglas(): Future[Validation[PersistenceException, Seq[ReglasContrasenas]]] = loan {
    session =>
      val resultTry = session.database.run(reglasContrasenas.result)
      resolveTry(resultTry, "Consulta todas las reglas contraseñas")
  }

  def obtenerRegla(llave: String): Future[Validation[PersistenceException, Option[ReglasContrasenas]]] = loan {
    session =>
      val resultTry = session.database.run(reglasContrasenas.withFilter(x => x.llave.like(llave)).result.headOption)
      resolveTry(resultTry, "Consulta la regla contraseña solicitada")
  }

  def actualizarContrasena(pw_nuevo: String, idUsuario: Int): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val query = for {
        u <- usuarios if u.id === idUsuario
      } yield (u.contrasena, u.fechaActualizacion, u.numeroIngresosErroneos)
      val fechaAct = new org.joda.time.DateTime().getMillis
      val act = (Some(pw_nuevo), new Timestamp(fechaAct), 0)
      val resultTry = session.database.run(query.update(act))
      resolveTry(resultTry, "Actualizar Contrasena y fecha de actualizacion")
  }

}