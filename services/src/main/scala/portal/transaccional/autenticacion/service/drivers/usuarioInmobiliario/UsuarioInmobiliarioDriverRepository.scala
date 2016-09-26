package portal.transaccional.autenticacion.service.drivers.usuarioInmobiliario

import java.sql.Timestamp

import co.com.alianza.persistence.entities.UsuarioAgenteInmobiliario
import enumerations.EstadosUsuarioEnum
import portal.transaccional.autenticacion.service.web.agenteInmobiliario.{ConsultarAgenteInmobiliarioListResponse, ConsultarAgenteInmobiliarioResponse, PaginacionMetadata}
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.UsuarioAgenteInmobDAOs

import scala.concurrent.{ExecutionContext, Future}

/**
  * Implementación del repositorio de agentes inmobiliarios
  *
  * @param ex Contexto de ejecución
  */
case class UsuarioInmobiliarioDriverRepository(usuariosDao: UsuarioAgenteInmobDAOs)
                                              (implicit val ex: ExecutionContext) extends UsuarioInmobiliarioRepository {

  override def createAgenteInmobiliario(tipoIdentificacion: Int, identificacion: String,
                                        correo: String, usuario: String,
                                        nombre: Option[String], cargo: Option[String], descripcion: Option[String]): Future[Int] = {
    usuariosDao.exists(0, identificacion, usuario).flatMap({
      case true => Future.successful(0)
      case false =>
        val agente = UsuarioAgenteInmobiliario(
          0, identificacion, tipoIdentificacion,
          usuario, correo, EstadosUsuarioEnum.pendienteActivacion.id,
          None, None, new Timestamp(System.currentTimeMillis()),
          0, None, nombre, cargo, descripcion, None
        )
        usuariosDao.create(agente)
    })
  }

  override def getAgenteInmobiliario(identificacion: String,
                                     usuario: String): Future[Option[ConsultarAgenteInmobiliarioResponse]] = {
    usuariosDao.get(identificacion, usuario).map(_.map(agente => ConsultarAgenteInmobiliarioResponse(
      agente.id,
      agente.correo,
      agente.usuario,
      agente.estado,
      agente.nombre,
      agente.cargo,
      agente.descripcion
    )))
  }

  override def getAgenteInmobiliarioList(identificacion: String, nombre: Option[String], usuario: Option[String],
                                         correo: Option[String], estado: Option[Int],
                                         pagina: Option[Int], itemsPorPagina: Option[Int]): Future[ConsultarAgenteInmobiliarioListResponse] = {
    usuariosDao
      .getAll(identificacion, nombre, usuario, correo, estado, pagina, itemsPorPagina)
      .map(res => {
        val agentes: Seq[ConsultarAgenteInmobiliarioResponse] = res._5.map(agente => ConsultarAgenteInmobiliarioResponse(
          agente.id, agente.correo, agente.usuario, agente.estado,
          agente.nombre, agente.cargo, agente.descripcion
        ))
        ConsultarAgenteInmobiliarioListResponse(
          PaginacionMetadata(res._1, res._2, res._3, res._4),
          agentes
        )
      })
  }

  override def updateAgenteInmobiliario(identificacion: String, usuario: String,
                                        correo: String, nombre: Option[String],
                                        cargo: Option[String], descripcion: Option[String]): Future[Int] = {
    usuariosDao.update(identificacion, usuario, correo, nombre, cargo, descripcion)
  }

  override def activateOrDeactivateAgenteInmobiliario(identificacion: String, usuario: String): Future[Int] = {
    usuariosDao.get(identificacion, usuario).flatMap {
      case None => Future.successful(0)
      case Some(agente) =>
        if (agente.estado == EstadosUsuarioEnum.activo.id) {
          usuariosDao.updateState(identificacion, usuario, EstadosUsuarioEnum.pendienteActivacion) // deactivate
        } else if (agente.estado == EstadosUsuarioEnum.pendienteActivacion.id) {
          usuariosDao.updateState(identificacion, usuario, EstadosUsuarioEnum.activo) // activate
        } else {
          Future.successful(0)
        }
    }
  }
}
