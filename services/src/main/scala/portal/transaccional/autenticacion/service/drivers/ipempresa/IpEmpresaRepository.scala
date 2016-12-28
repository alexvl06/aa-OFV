package portal.transaccional.autenticacion.service.drivers.ipempresa

import co.com.alianza.persistence.entities.IpsEmpresa

import scala.concurrent.Future

/**
 * Created by hernando on 25/07/16.
 */
trait IpEmpresaRepository {

  def getIpsByEmpresaId(idEmpresa: Int): Future[Seq[IpsEmpresa]]

  def guardarIp(ipEmpresa: IpsEmpresa): Future[String]

  def validarControlIpAgente(ip: String, ips: Seq[IpsEmpresa], token: String): Future[Boolean]

  def validarControlIpAdmin(ip: String, ips: Seq[IpsEmpresa], token: String, tieneRespuestas: Boolean): Future[String]

}
