package co.com.alianza.persistence.repositories

import scala.concurrent.{ ExecutionContext, Future }
import slick.lifted.TableQuery
import scala.util.Try
import scalaz.Validation

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities._
import CustomDriver.simple._

/**
 * Created by manuel on 3/02/15.
 */
class RecursoPerfilClienteAdminRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val recursosPerfilesClientesAdmins = TableQuery[RecursoPerfilClienteAdminTable]
  val clientesAdmins = TableQuery[UsuarioEmpresarialAdminTable]
  val perfilesClientesAdmins = TableQuery[PerfilClienteAdminClienteAdminTable]

  /**
   * Obtiene los recursos relacionados a los perfiles del cliente administrador
   *
   * @param idUsuario Id del usuario para obtener los recursos
   * @return
   */
  def obtenerRecursosPerfiles(idUsuario: Int): Future[Validation[PersistenceException, List[RecursoPerfilClienteAdmin]]] = loan {
    implicit session =>

      val usuariosRecursosJoin = for {
        ((usu, per), rec) <- clientesAdmins innerJoin perfilesClientesAdmins on (_.id === _.idUsuario) innerJoin recursosPerfilesClientesAdmins on (_._2.idPerfil === _.idPerfil)
        if usu.id === idUsuario
      } yield rec

      val resultTry = Try { usuariosRecursosJoin.list }
      resolveTry(resultTry, "Consulta todos los Recursos por Listado de Id de Perfiles")
  }

}
