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
//Ya esta en el refactor ! AlianzaDAO.getAdminResources
class RecursoPerfilAgenteRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val recursosPerfilesAgentes = TableQuery[RecursoPerfilAgenteTable]
  val agentes = TableQuery[UsuarioEmpresarialTable]
  val perfilesAgentes = TableQuery[PerfilAgenteAgenteTable]

  /**
   * Obtiene los recursos relacionados a los perfiles del agente
   *
   * @param idUsuario Id del usuario para obtener los recursos
   * @return
   */
  def obtenerRecursosPerfiles(idUsuario: Int): Future[Validation[PersistenceException, Seq[RecursoPerfilAgente]]] = loan {
    implicit session =>

      val usuariosRecursosJoin = for {
        ((usu, per), rec) <- agentes join perfilesAgentes on (_.id === _.idUsuario) join recursosPerfilesAgentes on (_._2.idPerfil === _.idPerfil)
        if usu.id === idUsuario
      } yield rec

      val resultTry = session.database.run(usuariosRecursosJoin.result)
      resolveTry(resultTry, "Consulta todos los Recursos por Listado de Id de Perfiles")
  }

}
