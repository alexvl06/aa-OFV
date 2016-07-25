package portal.transaccional.autenticacion.service.drivers.autenticacion

import scala.concurrent.{ Future, ExecutionContext }

/**
 * Created by hernando on 25/07/16.
 */
case class AutenticacionDriverRepository(implicit val ex: ExecutionContext) extends AutenticacionRepository {

  def autenticar(tipoIdentificacion: Int, numeroIdentificacion: String, password: String, clientIp: String): Future[String] = {
    Future { "" }
  }

}
