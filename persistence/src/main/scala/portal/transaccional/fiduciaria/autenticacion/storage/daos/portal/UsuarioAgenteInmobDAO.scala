package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import java.sql.Timestamp

import co.com.alianza.persistence.entities.{ UsuarioAgenteInmobiliario, UsuarioAgenteInmobiliarioTable }
import co.com.alianza.persistence.util.SlickExtensions
import enumerations.EstadosUsuarioEnumInmobiliario.estadoUsuarioInmobiliario
import enumerations.{ EstadosUsuarioEnumInmobiliario, OrdenamientoAgentesInmobEnum }
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Implementación del DAOde agentes inmobiliarios
 *
 * @param dcConfig Configuración de la base de datos
 */
case class UsuarioAgenteInmobDAO(implicit dcConfig: DBConfig) extends UsuarioAgenteDAO[UsuarioAgenteInmobiliarioTable, UsuarioAgenteInmobiliario](
  TableQuery[UsuarioAgenteInmobiliarioTable]
) with UsuarioAgenteInmobDAOs with SlickExtensions {

  import dcConfig.DB._
  import dcConfig.driver.api._

  override def create(usuarioInmob: UsuarioAgenteInmobiliario): Future[Int] = {
    run((table returning table.map(_.id)) += usuarioInmob)
  }

  override def exists(id: Int, identificacion: String, usuario: String): Future[Boolean] = {
    isExists(id, identificacion, usuario)
  }

  override def get(id: Int): Future[Option[UsuarioAgenteInmobiliario]] = {
    getById(id)
  }

  override def getByToken(token: String): Future[Option[UsuarioAgenteInmobiliario]] = {
    run(table.filter(_.token === token).result.headOption)
  }

  override def get(identificacion: String, usuario: String): Future[Option[UsuarioAgenteInmobiliario]] = {
    getByIdentityAndUser(identificacion, usuario)
  }

  override def getAll(identificacion: String): Future[Seq[UsuarioAgenteInmobiliario]] = {
    run(table.filter(_.identificacion === identificacion).result)
  }

  override def getAll(identificacion: String, nombre: Option[String],
    usuario: Option[String], correo: Option[String], estado: Option[String], pagina: Option[Int],
    itemsPorPagina: Option[Int], ordenarPor: Option[String])(implicit ec: ExecutionContext): Future[(Int, Int, Int, Int, Seq[UsuarioAgenteInmobiliario])] = {

    val query: Query[UsuarioAgenteInmobiliarioTable, UsuarioAgenteInmobiliario, Seq] =
      buildAgentesListQuery(identificacion, nombre, usuario, correo, estado, ordenarPor)

    run(
      for {
        totalAgentes <- query.length.result
        agentes <- query.paginate(pagina, itemsPorPagina).result
      } yield {
        (pagina.getOrElse(defaultPage), itemsPorPagina.getOrElse(defaultPageSize), agentes.length, totalAgentes, agentes)
      }
    )
  }

  override def update(identificacion: String, usuario: String,
    correo: String, nombre: Option[String],
    cargo: Option[String], descripcion: Option[String]): Future[Int] = {
    run(
      table
        .filter(agente => agente.identificacion === identificacion && agente.usuario === usuario)
        .map(agente => (agente.correo, agente.nombre, agente.cargo, agente.descripcion))
        .update((correo, nombre, cargo, descripcion))
    )
  }

  override def updateState(identificacion: String, usuario: String, estado: estadoUsuarioInmobiliario): Future[Int] = {
    run(
      table
        .filter(agente => agente.identificacion === identificacion && agente.usuario === usuario)
        .map(_.estado)
        .update(estado.id)
    )
  }

  override def getContrasena(contrasena: String, idUsuario: Int): Future[Option[UsuarioAgenteInmobiliario]] = {
    run(table.filter(_.id === idUsuario).filter(_.contrasena === contrasena).result.headOption)
  }

  override def updateContrasena(contrasena: String, idUsuario: Int): Future[Int] = {
    val query = table.filter(_.id === idUsuario).map(a => (a.contrasena, a.fechaActualizacion))
    run(query.update(Option(contrasena), new Timestamp(new org.joda.time.DateTime().getMillis)))
  }

  private def buildAgentesListQuery(identificacion: String, nombre: Option[String],
                                    usuario: Option[String], correo: Option[String],
                                    estado: Option[String], ordenarPor: Option[String]): Query[UsuarioAgenteInmobiliarioTable, UsuarioAgenteInmobiliario, Seq] = {
    val estados: Option[Seq[Int]] = estado match {
      case None => None
      case Some(e) => e match {
        case x if x.isEmpty => Some(Seq.empty)
        case x => Some(x.split(",").map(_.toInt).toSeq)
      }
    }

    val basequery: Query[UsuarioAgenteInmobiliarioTable, UsuarioAgenteInmobiliario, Seq] = MaybeFilter(table)
      .filter(Some(identificacion))(id => agente => agente.identificacion === id)
      .filter(nombre)(n => agente => agente.nombre.toLowerCase like s"%${n.toLowerCase}%")
      .filter(usuario)(u => agente => agente.usuario.toLowerCase like s"%${u.toLowerCase}%")
      .filter(correo)(c => agente => agente.correo.toLowerCase like s"%${c.toLowerCase}%")
      .filter(estados)(e => agente => agente.estado inSetBind e)
      .query

    ordenarPor match {
      case None => basequery
      case Some(ord) =>
        OrdenamientoAgentesInmobEnum.values.find(_.toString == ord).map {
          case OrdenamientoAgentesInmobEnum.ID => basequery.sortBy(_.id)
          case OrdenamientoAgentesInmobEnum.NOMBRE => basequery.sortBy(_.nombre)
          case OrdenamientoAgentesInmobEnum.USUARIO => basequery.sortBy(_.usuario)
          case OrdenamientoAgentesInmobEnum.CORREO => basequery.sortBy(_.correo)
          case OrdenamientoAgentesInmobEnum.ESTADO => basequery.sortBy(_.estado)
          case OrdenamientoAgentesInmobEnum.ESTADO_PENDIENTE_ACTIVACION => basequery.sortBy { agente =>
            Case
              .If(agente.estado === EstadosUsuarioEnumInmobiliario.pendienteActivacion.id).Then(0)
              .If(agente.estado === EstadosUsuarioEnumInmobiliario.pendienteReinicio.id).Then(1)
              .If(agente.estado === EstadosUsuarioEnumInmobiliario.activo.id).Then(2)
              .If(agente.estado === EstadosUsuarioEnumInmobiliario.bloqueContraseña.id).Then(3)
              .If(agente.estado === EstadosUsuarioEnumInmobiliario.inactivo.id).Then(4)
          }
          case _ => basequery
        }.getOrElse(basequery)
    }
  }
}
