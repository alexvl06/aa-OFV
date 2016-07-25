package portal.transaccional.autenticacion.service.drivers.ipusuario

import co.com.alianza.persistence.entities.IpsUsuario

import scala.concurrent.Future

/**
 * Created by hernando on 25/07/16.
 */
trait IpUsuarioRepository {

  def getIpsUsuarioById(idUsuario: Int): Future[Seq[IpsUsuario]]

  def validarControlIp(ip: String, ips: Seq[IpsUsuario], token: String, tieneRespuestas: Boolean): Future[String]

}
