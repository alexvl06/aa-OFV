package portal.transaccional.autenticacion.service.drivers.smtp

import co.com.alianza.util.ConfigApp
import com.typesafe.config.Config
import spray.client.pipelining._
import spray.http.HttpHeaders.`Content-Type`
import spray.http.{ ContentTypes, HttpHeader, HttpResponse, StatusCodes }
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by hernando on 11/11/16.
 */
case class SmtpDriverRepository()(implicit val ex: ExecutionContext) extends SmtpRepository {

  private val config: Config = ConfigApp.conf

  def enviar(mensaje: Mensaje): Future[Boolean] = {
    val endPoint = config.getString("autenticacion.service.smtp.location")
    val pipeline = sendReceive
    val header: HttpHeader = `Content-Type`(ContentTypes.`application/json`)
    val futureRequest: Future[HttpResponse] = pipeline(Post(s"$endPoint", mensaje) ~> header)
    val successStatusCodes = List(StatusCodes.OK)
    futureRequest.map {
      response =>
        successStatusCodes.contains(response.status)
    }
  }

}

object MailMessageJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val MensajeFormat = jsonFormat5(Mensaje)
}
