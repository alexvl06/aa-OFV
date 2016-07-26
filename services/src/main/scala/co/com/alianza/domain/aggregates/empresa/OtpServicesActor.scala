package co.com.alianza.domain.aggregates.empresa

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.routing.RoundRobinPool
import co.com.alianza.util.FutureResponse
import com.typesafe.config.Config
import co.com.alianza.infrastructure.messages.empresa._
import co.com.alianza.infrastructure.messages.empresa.OtpMessagesJsonSupport._

import scalaz.std.AllInstances._
import spray.client.pipelining._

import scala.concurrent.Future
import spray.http.{ HttpResponse, StatusCodes }
import co.com.alianza.infrastructure.messages.ResponseMessage
import co.com.alianza.util.transformers.ValidationT
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.http.HttpResponse
import co.com.alianza.infrastructure.messages.empresa.HabilitarOTP
import akka.routing.RoundRobinPool
import co.com.alianza.infrastructure.messages.empresa.DeshabilitarOTP
import co.com.alianza.util.transformers.ValidationT
import co.com.alianza.infrastructure.messages.ResponseMessage
import co.com.alianza.infrastructure.messages.empresa.RegistrarOTP
import co.com.alianza.infrastructure.messages.empresa.RemoverOTP

class OtpServicesActorSupervisor extends Actor with ActorLogging {

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._
  val otpServicesActor = context.actorOf(Props[OtpServicesActor].withRouter(RoundRobinPool(nrOfInstances = 3)), "otpServicesActor")

  def receive = {
    case message: Any => otpServicesActor forward message
  }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }

}

class OtpServicesActor(implicit val system: ActorSystem) extends Actor with ActorLogging with FutureResponse {

  import system.dispatcher
  implicit val config: Config = system.settings.config

  val application = config.getInt("service.otp.application")

  def receive = {
    case message: RegistrarOTP => {
      val currentSender = sender
      val endPoint = config.getString("service.otp.registrar.endpoint")
      for {
        usr <- ValidationT(co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial.DataAccessAdapter.obtenerUsuarioEmpresarialPorId(message.usuario.id))
      } yield {
        enviarOTP(endPoint, message.toOperacionOTPDTO(application, usr.get.identificacion, usr.get.usuario), currentSender)
      }
    }

    case message: RemoverOTP => {
      val currentSender = sender
      val endPoint = config.getString("service.otp.remover.endpoint")
      for {
        usr <- ValidationT(co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial.DataAccessAdapter.obtenerUsuarioEmpresarialPorId(message.usuario.id))
      } yield {
        enviarOTP(endPoint, message.toOperacionOTPDTO(application, usr.get.identificacion, usr.get.usuario), currentSender)
      }
    }

    case message: HabilitarOTP => {
      val currentSender = sender
      val endPoint = config.getString("service.otp.habilitar.endpoint")
      for {
        usr <- ValidationT(co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial.DataAccessAdapter.obtenerUsuarioEmpresarialPorId(message.usuario.id))
      } yield {
        enviarOTP(endPoint, message.toOperacionOTPDTO(application, usr.get.identificacion, usr.get.usuario), currentSender)
      }
    }

    case message: DeshabilitarOTP => {
      val currentSender = sender
      val endPoint = config.getString("service.otp.deshabilitar.endpoint")
      for {
        usr <- ValidationT(co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial.DataAccessAdapter.obtenerUsuarioEmpresarialPorId(message.usuario.id))
      } yield {
        enviarOTP(endPoint, message.toOperacionOTPDTO(application, usr.get.identificacion, usr.get.usuario), currentSender)
      }
    }
  }

  def enviarOTP(endPoint: String, message: OperacionOTPDTO, sender: ActorRef) = {
    try {
      val pipeline = sendReceive
      val futureRequest: Future[HttpResponse] = pipeline(Post(endPoint, message))
      val successStatusCodes = List(StatusCodes.OK)
      futureRequest map {
        httpResponse =>
          successStatusCodes contains httpResponse.status match {
            case true =>
              if (httpResponse.entity.asString.toBoolean) {
                ResponseMessage(StatusCodes.OK, httpResponse.entity.asString)
              } else {
                ResponseMessage(StatusCodes.BadRequest, httpResponse.entity.asString)
              }
            case false =>
              ResponseMessage(StatusCodes.BadRequest, "Validación OTP no se realizó correctamente: " + httpResponse.entity.data.asString)
          }
      }
    } catch {
      case e: Exception =>
        Future(ResponseMessage(StatusCodes.BadRequest, "Validación OTP no se realizó correctamente, se presento el siguiente error: " + e.getMessage))
    }
  }
}
