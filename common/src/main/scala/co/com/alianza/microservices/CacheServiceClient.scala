package co.com.alianza.microservices

import scala.concurrent.{ExecutionContext, Future}
import spray.client.pipelining._
import spray.http._
import com.typesafe.config.Config
import scalaz.Validation
import co.com.alianza.exceptions.ServiceException
import akka.actor.ActorSystem
import co.com.alianza.infrastructure.messages.MessageService


class   CacheServiceClient(implicit  executor: ExecutionContext, conf: Config, system: ActorSystem ) extends ServiceClient {

  val context: ExecutionContext = executor

  def getKey[T](key:MessageService, functionSuccess:(HttpEntity, StatusCode, MessageService) => T): Future[Validation[ServiceException, T]] = {
    val endPoint = conf.getString("cache.fiducia.location")
    val pipeline = sendReceive
    val futureRequest: Future[HttpResponse] =  pipeline( Get( s"$endPoint/${key.hashCode()}") )
    val successStatusCodes = List(StatusCodes.OK, StatusCodes.Unauthorized)
    resolveFutureRequestCache[T](functionSuccess, key , futureRequest, successStatusCodes, "getKey")
  }
  
   def addRecord[T](key:MessageService, value : String) {
    val endPoint = conf.getString("cache.fiducia.location")
    val pipeline = sendReceive
    val futureRequest: Future[HttpResponse] =  pipeline( Post( s"$endPoint/${key.hashCode()}", value) )
    val successStatusCodes = List(StatusCodes.OK, StatusCodes.Unauthorized)
  }
   
   def deleteKey[T](key:String, functionSuccess:(HttpEntity, StatusCode) => T): Future[Validation[ServiceException, T]] = {
    val endPoint = conf.getString("cache.fiducia.location")
    val pipeline = sendReceive
    val futureRequest: Future[HttpResponse] =  pipeline( Delete( s"$endPoint/$key") )
    val successStatusCodes = List(StatusCodes.OK, StatusCodes.Unauthorized)
    resolveFutureRequest[T](functionSuccess, futureRequest, successStatusCodes, "deleteKey")
  }

}
