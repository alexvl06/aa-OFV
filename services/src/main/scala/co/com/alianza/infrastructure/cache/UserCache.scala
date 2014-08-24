package co.com.alianza.infrastructure.cache

import spray.caching.Cache
import spray.caching.ExpiringLruCache
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import co.com.alianza.app.MainActors
import spray.util._
import spray.routing.Route
import co.com.alianza.app.AlianzaCommons
import co.com.alianza.infrastructure.messages.MessageService

object UserCache extends AlianzaCommons {



  	def cacheDef[T](maxCapacity: Int = 5000, initialCapacity: Int = 16,
                  timeToLive: Duration =  5 minutes, timeToIdle: Duration = Duration.Inf) =
    new ExpiringLruCache[T](maxCapacity, initialCapacity, timeToLive, timeToIdle)
    
    val cache = cacheDef[String]()
       

   def getUser[T](msg: MessageService) : Future[String] = {
    cache.get(msg) match {
      case None => Future{""}
      case Some(value) => value	
    }
  }
  
  def addUser[T](msg: T, value:String) = cache(msg) { value }
   
}