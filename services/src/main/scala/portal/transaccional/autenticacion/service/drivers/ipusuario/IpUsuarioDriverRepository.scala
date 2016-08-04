package portal.transaccional.autenticacion.service.drivers.ipusuario

import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.persistence.entities.IpsUsuario
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.IpUsuarioDAOs

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by hernando on 25/07/16.
 */
case class IpUsuarioDriverRepository(ipDAO: IpUsuarioDAOs)(implicit val ex: ExecutionContext) extends IpUsuarioRepository {

  /**
   * Obtener las ip del usuario
   * @param idUsuario
   * @return
   */
  def getIpsUsuarioById(idUsuario: Int): Future[Seq[IpsUsuario]] = {
    ipDAO.getById(idUsuario)
  }

  /**
   * Valida si el usuario tiene alguna ip guardada, teniendo en cuenta si tiene respuestas de autovalidacion
   * @param ip
   * @param ips
   * @param token
   * @param tieneRespuestas
   * @return
   */
  def validarControlIp(ip: String, ips: Seq[IpsUsuario], token: String, tieneRespuestas: Boolean): Future[String] = {
    val tieneIp = ips.exists(_.ip == ip)
    if (tieneRespuestas) {
      if (tieneIp) Future.successful(token) else Future.failed(ValidacionException("401.4", token))
    } else if (tieneIp) {
      Future.failed(ValidacionException("401.18", token))
    } else {
      Future.failed(ValidacionException("401.17", token))
    }
  }

}
