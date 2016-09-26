package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import java.sql.Timestamp

import co.com.alianza.persistence.entities.{ UsuarioAgenteInmobiliario, UsuarioAgenteInmobiliarioTable }
import co.com.alianza.persistence.util.SlickExtensions
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.concurrent.{ ExecutionContext, Future }

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
                      usuario: Option[String], correo: Option[String], estado: Option[Int], pagina: Option[Int],
                      itemsPorPagina: Option[Int])(implicit ec: ExecutionContext): Future[(Int, Int, Int, Int, Seq[UsuarioAgenteInmobiliario])] = {

    val basequery: Query[UsuarioAgenteInmobiliarioTable, UsuarioAgenteInmobiliario, Seq] = MaybeFilter(table)
      .filter(Some(identificacion))(id => agente => agente.identificacion === id)
      .filter(nombre)(n => agente => agente.nombre like s"%$n%")
      .filter(usuario)(u => agente => agente.usuario like s"%$u%")
      .filter(correo)(c => agente => agente.correo like s"%$c%")
      .filter(estado)(e => agente => agente.estado === e)
      .query

    run(
      for {
        totalAgentes <- basequery.length.result
        agentes <- basequery.paginate(pagina, itemsPorPagina).result
      } yield {
        (pagina.getOrElse(defaultPage), itemsPorPagina.getOrElse(defaultPageSize), agentes.length, totalAgentes, agentes)
      }
    )
  }

  def getContrasena(contrasena: String , idUsuario : Int ): Future[Option[UsuarioAgenteInmobiliario]] = {
    run(table.filter(_.id === idUsuario).filter(_.contrasena === contrasena).result.headOption)
  }

  def updateContrasena(contrasena: String , idUsuario : Int ): Future[Int] = {
    val query = table.filter(_.id === idUsuario).map(a => (a.contrasena, a.fechaActualizacion))
    run(query.update(Option(contrasena), new Timestamp(new org.joda.time.DateTime().withYear(2015).getMillis)))
  }

}
