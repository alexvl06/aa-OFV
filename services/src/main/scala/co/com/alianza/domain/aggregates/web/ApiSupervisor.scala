package co.com.alianza.domain.aggregates.web

import akka.actor._
import akka.actor.SupervisorStrategy._
import spray.http.StatusCodes._
import spray.routing.RequestContext
import akka.actor.OneForOneStrategy
import scala.concurrent.duration._
import spray.http.StatusCode
import java.util.Date
import spray.http.HttpHeaders.RawHeader
import java.util.TimeZone
import co.com.alianza.infrastructure.messages.{ ErrorMessage, ResponseMessage, MessageService }
import co.com.alianza.app.AlianzaCommons
import co.com.alianza.exceptions._
import co.com.alianza.util.json.MarshallableImplicits._
import co.com.alianza.infrastructure.messages.ErrorMessage
import spray.routing.RequestContext
import spray.http.HttpHeaders.RawHeader
import co.com.alianza.infrastructure.messages.ResponseMessage
import scala.Some
import akka.actor.OneForOneStrategy

class ApiSupervisor(r: RequestContext, props: Props, message: MessageService) extends Actor with AlianzaCommons {

  import context._

  implicit def actorRefFactory = context

  setReceiveTimeout(120.seconds)

  lazy val target = context.actorOf(props)

  target ! message

  def receive = {
    case Some(value: String) => complete(OK, value)
    case Some(value: ResponseMessage) => complete(value.statusCode, value.responseBody)
  }

  def complete[T <: AnyRef](status: StatusCode, obj: String) = {
    r.complete((status, setHeadersNoCache(), obj): (StatusCode, List[spray.http.HttpHeader], String))
    stop(self)
  }

  def setETag(etag: String): List[spray.http.HttpHeader] = {
    val cacheAge = 60 * 3
    val duration = new Date()
    duration.setTime(duration.getTime + cacheAge * 1000)

    val df = new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
    df.setTimeZone(TimeZone.getTimeZone("GMT"))

    RawHeader("Etag", etag) :: Nil

  }

  def setHeadersNoCache(): List[spray.http.HttpHeader] = {
    RawHeader("Cache-Control", "no-store") ::
      RawHeader("Pragma", "no-cache") :: Nil

  }
  //TODO:Agregar Logs de los errores
  override val supervisorStrategy =
    OneForOneStrategy() {

      case error: AlianzaException => {

        error.level match {
          case TimeoutLevel =>
            complete(GatewayTimeout, ErrorMessage("504.1", "Error Timeout", s"Se supero el tiempo de espera").toJson)
            Stop
          case TechnicalLevel =>
            complete(InternalServerError, ErrorMessage("500.1", "Error Tecnico", s"Se genero un error al ejecutar la operación").toJson)
            Stop
          case BusinessLevel =>
            complete(InternalServerError, ErrorMessage("500.2", "Error de Negocio", s"Se genero un error al ejecutar la operación").toJson)
            Stop
          case NetworkLevel =>
            complete(InternalServerError, ErrorMessage("500.3", "Error Tecnico - Red ", s"Se genero un error al ejecutar la operación. Error estableciendo conexión con los servicios").toJson)
            Stop
        }
      }

      case error: Exception =>
        complete(InternalServerError, ErrorMessage("500.3", "Error Interno", s"Error no controlado").toJson)
        Stop
    }
}

trait ApiRequestCreator {

  import co.com.alianza.app.MainActors._

  def apiRequest(r: RequestContext, props: Props, message: MessageService) =
    system.actorOf(Props(new ApiSupervisor(r, props, message)))
}