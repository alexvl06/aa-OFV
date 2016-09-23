package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{UsuarioAgenteInmobiliario, UsuarioAgenteInmobiliarioTable}
import co.com.alianza.persistence.util.SlickExtensions
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.dbio.Effect.Read
import slick.lifted.TableQuery
import slick.profile.FixedSqlStreamingAction

import scala.concurrent.{ExecutionContext, Future}

/**
  * Implementación del DAOde agentes inmobiliarios
  *
  * @param dcConfig Configuración de la base de datos
  */
case class UsuarioAgenteInmobDAO(implicit dcConfig: DBConfig) extends UsuarioAgenteDAO[UsuarioAgenteInmobiliarioTable, UsuarioAgenteInmobiliario](
  TableQuery[UsuarioAgenteInmobiliarioTable]) with UsuarioAgenteInmobDAOs with SlickExtensions {

  import dcConfig.DB._
  import dcConfig.driver.api._

  override def create(usuarioInmob: UsuarioAgenteInmobiliario): Future[Int] = {
    run((table returning table.map(_.id)) += usuarioInmob)
  }

  override def exists(id: Int, identificacion: String, usuario: String): Future[Boolean] = {
    isExists(id, identificacion, usuario)
  }

  override def get(identificacion: String, usuario: String): Future[Option[UsuarioAgenteInmobiliario]] = {
    getByIdentityAndUser(identificacion, usuario)
  }

  override def getAll(identificacion: String, nombre: Option[String],
                      usuario: Option[String], correo: Option[String], pagina: Option[Int],
                      itemsPorPagina: Option[Int])(implicit ec: ExecutionContext): Future[(Int, Int, Int, Int, Seq[UsuarioAgenteInmobiliario])] = {

    val basequery: Query[UsuarioAgenteInmobiliarioTable, UsuarioAgenteInmobiliario, Seq] = table.filter(agente =>
      agente.identificacion === identificacion && ((agente.nombre like nombre) && (agente.correo like correo) && (agente.usuario like usuario))
    )

    run(
      for {
        totalAgentes <- basequery.length.result
        agentes <- basequery.paginate(pagina, itemsPorPagina).result
      } yield {
        (pagina.getOrElse(defaultPage), itemsPorPagina.getOrElse(defaultPageSize), agentes.length, totalAgentes, agentes)
      }
    )
  }

}
