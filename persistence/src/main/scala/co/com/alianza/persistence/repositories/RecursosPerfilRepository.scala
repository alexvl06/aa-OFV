package co.com.alianza.persistence.repositories

import scala.concurrent.{ ExecutionContext, Future }
import co.com.alianza.exceptions.PersistenceException

import scala.util.Try
import scalaz.Validation

import co.com.alianza.persistence.entities._

import scala.slick.lifted.TableQuery
import CustomDriver.simple._
import co.com.alianza.persistence.entities.RecursoPerfil

/**
 * Repositorio para acceder a [[RecursoPerfilTable]]
 *
 * @author seven4n
 */
class RecursosPerfilRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val recursos = TableQuery[RecursoPerfilTable]
  val usuarios = TableQuery[UsuarioTable]
  val perfilesUsuario = TableQuery[PerfilUsuarioTable]

  /**
   * Obtiene los recursos relacionados a los perfiles del usuario
   *
   * @param idUsuario Id del usuario para obtener los recursos
   * @return
   */
  def obtenerRecursos(idUsuario: Int): Future[Validation[PersistenceException, List[RecursoPerfil]]] = loan {
    implicit session =>

      val usuariosRecursosJoin = for {
        ((usu: UsuarioTable, per: PerfilUsuarioTable), rec: RecursoPerfilTable) <- usuarios innerJoin perfilesUsuario on (_.id === _.idUsuario) innerJoin recursos on (_._2.idPerfil === _.idPerfil)
        if usu.id === idUsuario
      } yield rec

      val resultTry = Try { usuariosRecursosJoin.list }
      resolveTry(resultTry, "Consulta todos los Recursos por Listado de Id de Perfiles")
  }

}
