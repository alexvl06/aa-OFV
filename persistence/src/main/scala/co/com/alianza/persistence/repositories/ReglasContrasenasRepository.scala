package co.com.alianza.persistence.repositories

import java.sql.Timestamp

import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}
import co.com.alianza.exceptions.PersistenceException

import scala.util.Try
import scalaz.Validation

import co.com.alianza.persistence.entities._

import scala.slick.lifted.TableQuery
import scala.slick.jdbc.JdbcBackend.Session
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
    println(reglasContrasenas)
    val result: Option[ReglasContrasenas] = reglasContrasenas.withFilter(x => x.llave.like(llave)).list.headOption
    result
  }

  def actualizar(reglaContrasena: ReglasContrasenas): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try {
        val row = for {elem <- reglasContrasenas if elem.llave === reglaContrasena.llave} yield elem.valor
        row.update(reglaContrasena.valor)
      }
      resolveTry(resultTry, "Actualiza Regla Contrasena")
  }

  def actualizarContrasena(pw_nuevo: String, idUsuario: Int): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val query = for {u <- usuarios if u.id === idUsuario} yield u.contrasena
      val act = Some(pw_nuevo)
      val resultTry = Try {
        query.update(act)
      }
      resolveTry(resultTry, "Actualizar Contrasena")
  }

  def actualizarContrasenaYCaducidad(idUsuario: Int, pw_nuevo: String, caducidad: Long): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val query = for {u <- usuarios if u.id === idUsuario} yield (u.contrasena, u.fechaActualizacion)
      val act = ( Some(pw_nuevo), new Timestamp(caducidad) )
      val resultTry = Try {
        query.update(act)
      }
      resolveTry(resultTry, "Actualizar Contrasena y caducidad")
  }

}
