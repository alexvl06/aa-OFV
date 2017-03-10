package co.com.alianza.infrastructure.cache
import akka.actor.{ ActorSystem }
import com.typesafe.config.Config
import scala.concurrent.Future
import co.com.alianza.infrastructure.messages.MessageService
import spray.routing.RequestContext
import co.com.alianza.microservices.CacheServiceClient

import co.com.alianza.exceptions.ServiceException
import spray.http.StatusCode
import spray.http.HttpEntity
import spray.http.StatusCodes
import spray.http.HttpMethods._

import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }
import co.com.alianza.infrastructure.messages.CacheMessage
trait CacheHelper {

  implicit val system: ActorSystem
  import system.dispatcher
  implicit val conf: Config = system.settings.config

  def cacheRequest(message: String): RequestContext => Future[Either[MessageService, String]] = {
    ctx =>
      {
        val serviceClient = new CacheServiceClient
        val newMessage = CacheMessage(message + ctx.request.uri.path.toString + ctx.request.uri.query, ctx.request.method)
        if (ctx.request.method.equals(GET)) {
          val serviceClientFuture: Future[Validation[ServiceException, Either[MessageService, String]]] = serviceClient.getKey[Either[MessageService, String]](newMessage, resolveCacheSuccess)
          val resultFuture: Future[Either[MessageService, String]] = {
            serviceClientFuture map {
              case zFailure(error) => Left(newMessage)
              case zSuccess(value) => value
            }
          }
          resultFuture
        } else {
          Future { Left(newMessage) }
        }

      }
  }
  private def resolveCacheSuccess(httpEntity: HttpEntity, statusCode: StatusCode, message: MessageService): Either[MessageService, String] = {
    statusCode match {
      case StatusCodes.OK => Right(httpEntity.asString)
      case _ => Left(message)
    }
  }

}