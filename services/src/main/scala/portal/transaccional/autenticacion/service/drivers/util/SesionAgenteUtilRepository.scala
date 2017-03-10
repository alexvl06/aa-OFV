package portal.transaccional.autenticacion.service.drivers.util

import co.com.alianza.infrastructure.messages.ResponseMessage

import scala.concurrent.Future

/**
 * Created by alexandra on 22/09/16.
 */
trait SesionAgenteUtilRepository {

  def invalidarToken(token: String, encriptedToken: String): Future[Int]

  def validarToken(token: String): Future[Boolean]

  def validarEstadoEmpresa(estado: Int): Future[ResponseMessage]

}
