package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities._
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by dfbaratov on 23/08/16.
 */
case class RolRecursoComercialDAO()(implicit dcConfig: DBConfig) extends TableQuery(new RolRecursoComercialTable(_)) with RolRecursoComercialDAOs {

  val recursos = TableQuery[RecursoComercialTable]
  val rolesRecurso = TableQuery[RolRecursoComercialTable]
  val roles = TableQuery[RolComercialTable]

  import dcConfig.DB._
  import dcConfig.driver.api._

  override def obtenerRolesPorRecurso(nombreRecurso: String): Future[Seq[RolComercial]] = {
    val rolRecursoJoin = for {
      ((recurso: RecursoComercialTable, rolRecurso: RolRecursoComercialTable), rol: RolComercialTable) <- recursos join rolesRecurso on (_.id === _.idRecurso) join roles on (_._2.idRol === _.id)
      if recurso.nombre === nombreRecurso
    } yield rol
    run(rolRecursoJoin.result)
  }

  /**
    * Inserts a new instance of RolRecurso
    * @param rolRecurso
    * @return
    */
  def insertar( rolRecurso: RolRecursoComercial ) = {
    val query = rolesRecurso += rolRecurso
    run( query )
  }

  /**
    * Updates the permissions of a determined rol over a resource
    * @param permissions
    * @return
    */
  def actualizarPermisos( permissions: Seq[ RolRecursoComercial ] ) = {
    val queryDelete = rolesRecurso.delete
    val queryInsert = rolesRecurso ++= permissions
    run( ( queryDelete andThen queryInsert ).transactionally )
  }

}
