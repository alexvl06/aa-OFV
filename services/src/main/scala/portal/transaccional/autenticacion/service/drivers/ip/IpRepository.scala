package portal.transaccional.autenticacion.service.drivers.ip

import scala.concurrent.Future

/**
 * Created by alexandra on 3/08/16.
 */
trait IpRepository {

  def agregarIpHabitualUsuario(idUsuario: String, clientIp: String): Future[String]

  def agregarIPHabitualUsuarioEmpresarialAdmin(idUsuario: Int, clientIp: String, tipoIdentificacion: Option[Int]): Future[String]

}
