package co.com.alianza.web

import spray.routing.Directives
import co.com.alianza.app.AlianzaCommons
import co.com.alianza.infrastructure.messages.{InvalidarToken, AutorizarUrl}
import co.com.alianza.infrastructure.cache.CacheHelper
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext
import com.typesafe.config.Config
import co.com.alianza.app.MainActors
import co.com.alianza.infrastructure.cache.CachingDirectiveAlianza

class AutorizacionService extends Directives with AlianzaCommons with CacheHelper{
  

  import CachingDirectiveAlianza._
  implicit val system: ActorSystem = MainActors.system
  implicit val contextAuthorization: ExecutionContext = MainActors.ex
  implicit val conf: Config= MainActors.conf

  def route = {
    path("validarToken" / Segment) {
      token =>
        get {
          
          parameters('url) {
            url =>
              respondWithMediaType(mediaType) {
                cacheAlianza(cacheRequest("fiduciaToken")) { cache =>
                requestExecute(AutorizarUrl(token,url), autorizacionActor, true)
                }
            }
          }
        }
    } ~ path( "invalidarToken" / Segment ){
      token =>
        get{
          respondWithMediaType(mediaType) {
            //cacheAlianza(cacheRequest("fiduciaToken")) { cache =>
              requestExecute(InvalidarToken(token), autorizacionActor)
            //}
          }
        }

    }
  }
}



