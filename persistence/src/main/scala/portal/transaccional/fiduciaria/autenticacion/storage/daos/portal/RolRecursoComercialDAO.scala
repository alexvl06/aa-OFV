package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities._
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import portal.transaccional.fiduciaria.autenticacion.storage.helpers.AlianzaStorageHelper
import slick.lifted.TableQuery

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by dfbaratov on 23/08/16.
 */
case class RolRecursoComercialDAO()(dcConfig: DBConfig) extends RolRecursoComercialDAOs {

  val recursos = TableQuery[RecursoComercialTable]
  val rolesRecurso = TableQuery[RolRecursoComercialTable]
  val roles = TableQuery[RolComercialTable]

  import dcConfig.DB._
  import dcConfig.driver.api._

  override def obtenerRolesPorRecurso(nombreRecurso: String): Future[Seq[RolComercial]] = {
    val rolRecursoJoin = for {
      ((recurso: RecursoComercialTable, rolRecurso: RolRecursoComercialTable),
        rol: RolComercialTable) <- recursos join rolesRecurso on (_.id === _.idRecurso) join roles on (_._2.idRol === _.id)
      if recurso.nombre === nombreRecurso
    } yield rol
    run(rolRecursoJoin.result)
  }

  /**
   * Inserts a new instance of RolRecurso
   *
   * @param rolRecurso
   * @return
   */
  def insertar(rolRecurso: RolRecursoComercial): Future[Int] = {
    val query = rolesRecurso += rolRecurso
    run(query)
  }

  /**
   * Updates the permissions of a determined rol over a resource
   *
   * @param permisos
   * @return
   */
  def actualizarPermisos(permisos: Seq[RolRecursoComercial]): Future[Option[Int]] = {
    val queryDelete = rolesRecurso.delete
    val queryInsert = rolesRecurso ++= permisos
    run(queryDelete andThen queryInsert)
  }

}
