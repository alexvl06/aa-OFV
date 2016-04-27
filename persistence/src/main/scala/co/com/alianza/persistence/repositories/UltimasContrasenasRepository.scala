package co.com.alianza.persistence.repositories

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities._

import scala.concurrent.{ Future, ExecutionContext }
import scala.slick.lifted
import scala.slick.lifted.TableQuery
import CustomDriver.simple._

import scala.util.Try
import scalaz.Validation

/**
 * Created by S4N on 14/11/14.
 */
class UltimasContrasenasRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val ultimasContrasenas = TableQuery[UltimaContrasenaTable]

  def guardarUltimaContrasena(nuevaUltimaContrasena: UltimaContrasena): Future[Validation[PersistenceException, Unit]] = loan {
    implicit session =>
      val resultTry: Try[Unit] = Try { (ultimasContrasenas += nuevaUltimaContrasena) }
      resolveTry(resultTry, "Guarda ultima contraseña registrada")
  }

  def obtenerUltimasContrasenas(numeroUltimasContrasenas: String, usuarioId: Int): Future[Validation[PersistenceException, List[UltimaContrasena]]] = loan {
    implicit session =>
      val resultTry = Try { ultimasContrasenas.filter(_.idUsuario === usuarioId).sortBy(_.fechaUltimaContrasena.desc).take(numeroUltimasContrasenas.toInt).list }
      resolveTry(resultTry, "Consulta las ultimas 'N' Contrasenas")
  }

}