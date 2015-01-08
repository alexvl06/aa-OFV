package co.com.alianza.persistence.repositories

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities._

import scala.concurrent.{Future, ExecutionContext}
import scala.slick.lifted
import scala.slick.lifted.TableQuery
import CustomDriver.simple._

import scala.util.Try
import scalaz.Validation

/**
 * Created by manuel on 7/01/15.
 */
class UltimasContrasenasUsuarioEmpresarialAdminRepository ( implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val ultimasContrasenas = TableQuery[UltimaContrasenaUsuarioEmpresarialAdminTable]

  def guardarUltimaContrasena(nuevaUltimaContrasena: UltimaContrasenaUsuarioEmpresarialAdmin): Future[Validation[PersistenceException, Unit]] = loan {
    implicit session =>
      val resultTry: Try[Unit] = Try{  (ultimasContrasenas += nuevaUltimaContrasena) }
      resolveTry(resultTry, "Guarda última contraseña registrada de un cliente admin")
  }

  def obtenerUltimasContrasenas(numeroUltimasContrasenas: String, usuarioId: Int): Future[Validation[PersistenceException, List[UltimaContrasenaUsuarioEmpresarialAdmin]]] = loan {
    implicit session =>
      val resultTry =  Try { ultimasContrasenas.filter(_.idUsuario === usuarioId).sortBy( _.fechaUltimaContrasena.desc ).take( numeroUltimasContrasenas.toInt ).list }
      resolveTry(resultTry, "Consulta las ultimas 'N' Contrasenas de un cliente admin")
  }

}
