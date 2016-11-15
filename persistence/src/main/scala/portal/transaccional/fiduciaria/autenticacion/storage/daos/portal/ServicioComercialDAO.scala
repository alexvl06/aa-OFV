package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities._
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by dfbaratov on 23/08/16.
 */
case class ServicioComercialDAO()(dcConfig: DBConfig) extends ServicioComercialDAOs {

  val recursos = TableQuery[RecursoComercialTable]
  val rolesRecurso = TableQuery[RolRecursoComercialTable]
  val roles = TableQuery[RolComercialTable]
  val servicios = TableQuery[ServicioComercialTable]
  val recursosServicio = TableQuery[RecursoServicioComercialTable]

  import dcConfig.DB._
  import dcConfig.driver.api._

  override def autorizadoServicio(rolId: Int, url: String): Future[Option[RolComercial]] = {
    val concat = SimpleBinaryOperator[String]("||")

    val query = for {
      servicio <- servicios if LiteralColumn(url) like concat(concat("%", servicio.url), "%")
      recServ <- recursosServicio if recServ.idServicio === servicio.id
      recurso <- recursos if recServ.idRecurso === recurso.id
      rolRec <- rolesRecurso if rolRec.idRecurso === recurso.id
      rol <- roles if rol.id === rolRec.idRol && rol.id === rolId
    } yield rol
    val resOption = query.result.headOption
    run(resOption)
  }

  override def existe(url: String): Future[Boolean] = {

    val concat = SimpleBinaryOperator[String]("||")
    val query = servicios.filter(s => LiteralColumn(url) like concat("%", s.url)).exists
    val resOption = query.result
    run(resOption)
  }
}
