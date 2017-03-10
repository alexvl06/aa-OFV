package portal.transaccional.autenticacion.service.drivers.ip

import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import portal.transaccional.autenticacion.service.web.ip.IpResponse

import scala.concurrent.Future

/**
 * Created by alexandra on 3/08/16.
 */
trait IpRepository {

  def obtenerIps(usuario: UsuarioAuth): Future[Seq[IpResponse]]

  def agregarIp(usuario: UsuarioAuth, ip: String): Future[String]

  def eliminarIp(usuario: UsuarioAuth, ip: String): Future[Int]

}
