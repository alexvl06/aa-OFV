package portal.transaccional.autenticacion.service.drivers.smtp

import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future

/**
 * Created by hernando on 11/11/16.
 */
trait SmtpRepository {

  def enviar(mensaje: Mensaje): Future[Boolean]

}

object MensajeJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val MensajeFormatter = jsonFormat5(Mensaje)
}

case class Mensaje(from: String, to: String, cc: List[String], asunto: String, contenidoMensaje: String)