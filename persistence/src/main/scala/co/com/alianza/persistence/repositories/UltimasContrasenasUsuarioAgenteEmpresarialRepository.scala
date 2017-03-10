package co.com.alianza.persistence.repositories

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.CustomDriver.simple._
import co.com.alianza.persistence.entities._
import slick.lifted.TableQuery

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.Validation

/**
 * Created by manuel on 7/01/15.
 */
class UltimasContrasenasUsuarioAgenteEmpresarialRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val ultimasContrasenas = TableQuery[UltimaContrasenaAgenteTable]

  def guardarUltimaContrasena(nuevaUltimaContrasena: UltimaContrasena): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run((ultimasContrasenas returning ultimasContrasenas.map(_.id.get)) += nuevaUltimaContrasena)
      resolveTry(resultTry, "Guarda última contraseña registrada de un agente empresarial")
  }

  def obtenerUltimasContrasenas(numeroUltimasContrasenas: String, usuarioId: Int): Future[Validation[PersistenceException, Seq[UltimaContrasena]]] = loan {
    implicit session =>
      val resultTry = session.database.run(ultimasContrasenas.filter(_.idUsuario === usuarioId).sortBy(_.fechaUltimaContrasena.desc)
        .take(numeroUltimasContrasenas.toInt).result)
      resolveTry(resultTry, "Consulta las ultimas 'N' Contrasenas de un agente empresarial")
  }

}
