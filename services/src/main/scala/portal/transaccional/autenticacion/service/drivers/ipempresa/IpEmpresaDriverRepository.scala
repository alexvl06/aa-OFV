package portal.transaccional.autenticacion.service.drivers.ipempresa

import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.persistence.entities.{ IpsEmpresa, IpsUsuario }
import co.com.alianza.util.token.AesUtil
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.IpEmpresaDAOs

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by hernando on 25/07/16.
 */
case class IpEmpresaDriverRepository(ipDAO: IpEmpresaDAOs)(implicit val ex: ExecutionContext) extends IpEmpresaRepository {

  /**
   * Obtener las ip del usuario
   * @param idEmpresa
   * @return
   */
  def getIpsByEmpresaId(idEmpresa: Int): Future[Seq[IpsEmpresa]] = {
    ipDAO.getById(idEmpresa)
  }

  def guardarIp(ipEmpresa: IpsEmpresa): Future[String] = {
    ipDAO.create(ipEmpresa)
  }

  /**
   * Valida si el agente tiene alguna ip guardada
   * @param ip
   * @param ips
   * @return
   */
  def validarControlIpAgente(ip: String, ips: Seq[IpsEmpresa], token: String): Future[Boolean] = {
    val tieneIp = ips.exists(_.ip.equals(ip))
    if (tieneIp) Future.successful(true)
    else Future.failed(ValidacionException("401.4", "Error ip"))
  }

  /**
   * Valida si el admin tiene alguna ip guardada, teniendo en cuenta si tiene respuestas de autovalidacion
   * @param ip
   * @param ips
   * @param token
   * @param tieneRespuestas
   * @return
   */
  def validarControlIpAdmin(ip: String, ips: Seq[IpsEmpresa], token: String, tieneRespuestas: Boolean): Future[String] = {
    val tieneIp = ips.exists(_.ip.equals(ip))
    val encryptedToken: String = AesUtil.encriptarToken(token)
    if (tieneRespuestas) {
      if (tieneIp) Future.successful(token)
      else Future.failed(ValidacionException("401.4", encryptedToken))
    } else if (tieneIp) {
      Future.failed(ValidacionException("401.18", encryptedToken))
    } else {
      Future.failed(ValidacionException("401.17", encryptedToken))
    }
  }

}
