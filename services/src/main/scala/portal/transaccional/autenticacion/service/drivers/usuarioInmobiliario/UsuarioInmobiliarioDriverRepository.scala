package portal.transaccional.autenticacion.service.drivers.usuarioInmobiliario

import java.sql.Timestamp

import co.com.alianza.persistence.entities.UsuarioAgenteInmobiliario
import enumerations.EstadosUsuarioEnum
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
    val agente = UsuarioAgenteInmobiliario(
      0, identificacion, tipoIdentificacion,
      usuario, correo, EstadosUsuarioEnum.pendienteActivacion.id,
      None, None, new Timestamp(System.currentTimeMillis()),
      0, None, nombre, cargo, descripcion, None
    )
    usuariosDao.create(agente)
  }
}
