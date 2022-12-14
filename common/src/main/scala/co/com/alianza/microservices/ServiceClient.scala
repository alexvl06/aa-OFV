package co.com.alianza.microservices

import akka.actor.ActorSystem
import spray.http.{ HttpEntity, HttpResponse, StatusCode }

import scala.concurrent.{ Future }
import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }
import co.com.alianza.exceptions.{ ServiceException, TechnicalLevel, TimeoutLevel }
import spray.can.Http.RequestTimeoutException
import co.com.alianza.infrastructure.messages.MessageService
import com.typesafe.config.Config

/**
 *
 * @author smontanez
 */
trait ServiceClient {

  implicit val system: ActorSystem
  import system.dispatcher
  implicit val conf: Config = system.settings.config

  /**
   *
   * Resuelve un futuro de un HttpResponse, cuando los status code se encuentran en la lista successStatusCodes
   * hace el llamado a la funcion sf
   *
   * Cualquier otro Status code lo interpreta como error y genera una excepción Técnica
   *
   * @param successF Función ejecutada cuando el StatusCode esta en la lista successStatusCodes
   * @param serviceName Nombre del servicio ejecutado
   * @param futureRequest el fututuro que contiene el HttpResponse
   * @tparam T Tipo de dato que se espera que retorne la función sf
   * @return
   */
  def resolveFutureRequest[T](successF: (HttpEntity, StatusCode) => T, futureRequest: Future[HttpResponse], successStatusCodes: List[StatusCode], serviceName: String): Future[Validation[ServiceException, T]] = {
    futureRequest map {
      httpResponse =>
        successStatusCodes contains httpResponse.status match {
          case true => zSuccess(successF(httpResponse.entity, httpResponse.status))
          case false =>
            val ex = ServiceException(new Exception(s"${httpResponse.status} - ${httpResponse.entity.asString}"), TechnicalLevel, s"Error consumiendo el servicio $serviceName. StatusCode no interpretado.", httpResponse.status.intValue, httpResponse.entity.asString)
            zFailure(ex)
        }
    } recover {
      case ex: RequestTimeoutException =>
        zFailure(ServiceException(ex, TimeoutLevel, s"Error consumiendo el servicio $serviceName. Timeout."))
      case ex =>
        zFailure(ServiceException(ex, TechnicalLevel, s"Error consumiendo el servicio $serviceName."))
    }
  }

  def resolveFutureRequestCache[T](successF: (HttpEntity, StatusCode, MessageService) => T, message: MessageService, futureRequest: Future[HttpResponse], successStatusCodes: List[StatusCode], serviceName: String): Future[Validation[ServiceException, T]] = {
    futureRequest map {
      httpResponse =>
        successStatusCodes contains httpResponse.status match {
          case true => zSuccess(successF(httpResponse.entity, httpResponse.status, message))
          case false =>
            val ex = ServiceException(new Exception(s"${httpResponse.status} - ${httpResponse.entity.asString}"), TechnicalLevel, s"Error consumiendo el servicio $serviceName. StatusCode no interpretado.", httpResponse.status.intValue, httpResponse.entity.asString)
            zFailure(ex)
        }
    } recover {
      case ex: RequestTimeoutException =>
        zFailure(ServiceException(ex, TimeoutLevel, s"Error consumiendo el servicio $serviceName. Timeout."))
      case ex =>
        zFailure(ServiceException(ex, TechnicalLevel, s"Error consumiendo el servicio $serviceName."))
    }
  }

}
