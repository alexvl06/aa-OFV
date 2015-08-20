package co.com.alianza.microservices

import scala.concurrent.{ExecutionContext, Future}
import spray.client.pipelining._
import spray.http._
import com.typesafe.config.Config
import scalaz.Validation
import co.com.alianza.exceptions.ServiceException
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import spray.routing.Directives
import akka.actor.ActorSystem


case class MailMessage (from:String, to: String, cc: List[String], asunto:String, contenidoMensaje:String, urlServicioCallBack:String)

object MailMessageJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val MailMessageFormat = jsonFormat6(MailMessage)
}


/**
 *
 * @author smontanez
 */
class SmtpServiceClient(implicit  execctx: ExecutionContext, conf: Config, system: ActorSystem ) extends ServiceClient  with Directives   {
  import MailMessageJsonSupport._

  val context: ExecutionContext = execctx
  import spray.client.pipelining._
  import spray.http._
  import spray.http.HttpHeaders._

  def send[T](message:MailMessage, functionSuccess:(HttpEntity, StatusCode) => T): Future[Validation[ServiceException, T]] = {
    val endPoint = conf.getString("autenticacion.service.smtp.location")
    val pipeline = sendReceive

    val header:HttpHeader = `Content-Type`(ContentTypes.`application/json`)

    val futureRequest: Future[HttpResponse] =  pipeline(  Post( s"$endPoint", message ) ~> header  )
    val successStatusCodes = List(StatusCodes.OK)

    resolveFutureRequest[T](functionSuccess, futureRequest, successStatusCodes, "Enviar Correo")
  }

}



