package co.com.alianza.persistence.repositories

import scala.concurrent.{ExecutionContext, Future}
import scala.slick.lifted.TableQuery
import scala.util.Try
import scalaz.Validation

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities._
import CustomDriver.simple._

/**
 * Created by manuel on 3/02/15.
 */
class RecursoPerfilAgenteRepository ( implicit executionContext: ExecutionContext) extends AlianzaRepository  {

  val recursosPerfilesAgentes = TableQuery[RecursoPerfilAgenteTable]
  val agentes = TableQuery[UsuarioEmpresarialTable]
  val perfilesAgentes = TableQuery[PerfilAgenteAgenteTable]

  /**
   * Obtiene los recursos relacionados a los perfiles del agente
   *
   * @param idUsuario Id del usuario para obtener los recursos
   * @return
   */
  def obtenerRecursosPerfiles(idUsuario: Int): Future[Validation[PersistenceException, List[RecursoPerfilAgente]]] = loan {
    implicit session =>

      val usuariosRecursosJoin = for {
        ((usu: UsuarioTable, per:PerfilUsuarioTable), rec:RecursoPerfilTable) <- agentes innerJoin perfilesAgentes on (_.id === _.idUsuario) innerJoin recursosPerfilesAgentes on(_._2.idPerfil === _.idPerfil)
        if usu.id === idUsuario
      } yield rec

      val resultTry =  Try { usuariosRecursosJoin.list }
      resolveTry(resultTry, "Consulta todos los Recursos por Listado de Id de Perfiles")
  }

}
