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

object UserCache {

  import MainActors.ex

  	def cacheDef[T](maxCapacity: Int = 5000, initialCapacity: Int = 16,
                  timeToLive: Duration =  3 minutes, timeToIdle: Duration = Duration.Inf) =
    new ExpiringLruCache[T](maxCapacity, initialCapacity, timeToLive, timeToIdle)
    
    val cache = cacheDef[Either[String, String]]()
       

   def getUser[T](msg: T): Future[Either[String, String]] = {

    cache.get(msg) match {
      case None => Future{Left("")}
      case Some(value) => value
    }
  }
  
  def addUser[T](msg: T, value:String) = cache(msg) { Right(value) }
   
}