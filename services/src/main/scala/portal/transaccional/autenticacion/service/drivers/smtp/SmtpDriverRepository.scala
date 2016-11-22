package portal.transaccional.autenticacion.service.drivers.smtp

import akka.actor.ActorSystem
import co.com.alianza.util.ConfigApp
import com.typesafe.config.Config
import spray.client.pipelining._
import spray.http.HttpHeaders.`Content-Type`
import spray.http.{ ContentTypes, HttpHeader, HttpResponse, StatusCodes }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by hernando on 11/11/16.
 */
case class SmtpDriverRepository()(implicit val ex: ExecutionContext, system: ActorSystem) extends SmtpRepository {

  private val config: Config = ConfigApp.conf

  import portal.transaccional.autenticacion.service.drivers.smtp.MensajeJsonSupport._

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
