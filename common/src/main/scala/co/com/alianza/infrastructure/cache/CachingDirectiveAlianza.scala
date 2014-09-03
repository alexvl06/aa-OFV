package co.com.alianza.infrastructure.cache

import scala.concurrent.{ Future, ExecutionContext }
import shapeless.{ HNil, :: }
import spray.routing.directives.{ AuthMagnet, RouteDirectives, BasicDirectives }
import BasicDirectives._
import spray.routing.directives.RouteDirectives._
import spray.routing.directives.FutureDirectives._
import spray.http.HttpResponse
import co.com.alianza.infrastructure.messages.CacheMessage
import co.com.alianza.infrastructure.messages.CacheMessage
import spray.routing.RequestContext
import akka.actor.{ ActorLogging, Actor, ActorSelection, Props }
import co.com.alianza.microservices.CacheServiceClient
import com.typesafe.config.Config
import akka.actor.ActorSystem
import co.com.alianza.infrastructure.messages.MessageService
import spray.http.StatusCodes
import spray.http.HttpMethods._

object CachingDirectiveAlianza extends CachingDirectiveAlianza

trait CachingDirectiveAlianza {
  def cacheAlianza[T](cacheHelper: CacheMagnet[T]): DirectiveCache[T] = cacheHelper.directive
}

class CacheMagnet[T](cacheDirective: DirectiveCache[Cache[T]])(implicit executor: ExecutionContext, system: ActorSystem, conf: Config) {

  val directive: DirectiveCache[T] = cacheDirective.flatMap {
    case Right(san) => complete(san.asInstanceOf[String])
    case Left(rejection) => routeRouteResponse {
      case response: HttpResponse =>
        val msgCache: CacheMessage = rejection.asInstanceOf[CacheMessage]
        if (msgCache.verb.equals(GET) && response.status.isSuccess && !response.entity.asString.equals("")) {
          val cacheActor = system.actorOf(CacheActor.props(executor, system, conf))
          val msg = SaveRecordCacheMsg(msgCache, response);
          cacheActor ! msg
        }
        complete(response)
    } & provide(rejection)
  }

}

object CacheMagnet {
  implicit def fromFutureCache[T](san: Future[Cache[T]])(implicit executor: ExecutionContext, system: ActorSystem, conf: Config): CacheMagnet[T] =
    new CacheMagnet(onSuccess(san))

  implicit def fromContextCache[T](san: ContextCache[T])(implicit executor: ExecutionContext, system: ActorSystem, conf: Config): CacheMagnet[T] =
    new CacheMagnet(extract(san).flatMap(onSuccess(_)))
}

case class SaveRecordCacheMsg(cacheMessage: CacheMessage, response: HttpResponse)

class CacheActor(implicit executor: ExecutionContext, actorSystem: ActorSystem, conf: Config) extends Actor with ActorLogging {

  def receive = {
    case response: SaveRecordCacheMsg =>
      {
        val serviceClient = new CacheServiceClient
        serviceClient.addRecord(response.cacheMessage, response.response.entity.asString)
        context.stop(self)
      }

  }
}

object CacheActor {
  /**
   * Crea Props para este actor.
   */
  def props(executor: ExecutionContext, actorSystem: ActorSystem, conf: Config): Props = Props(new CacheActor()(executor, actorSystem, conf))
}