package portal.transaccional.autenticacion.service.drivers.smtp

import scala.concurrent.Future

/**
 * Created by hernando on 11/11/16.
 */
trait SmtpRepository {

  def enviar(mensaje: Mensaje): Future[Boolean]

}

case class Mensaje(de: String, para: String, cc: List[String], asunto: String, contenido: String)