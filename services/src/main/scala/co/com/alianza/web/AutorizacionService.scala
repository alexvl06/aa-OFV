package co.com.alianza.web

import spray.routing.Directives
import co.com.alianza.app.AlianzaCommons
import co.com.alianza.infrastructure.messages.AutorizarUrl


class AutorizacionService extends Directives with AlianzaCommons{


  def route = {
    path("validarToken" / Segment) {
      token =>
        get {
          parameters('url) {
            url =>
              respondWithMediaType(mediaType) {
                println(" url " + url )
                requestExecute(AutorizarUrl(token,url), autorizacionActor, true)
            }
          }
        }
    }
  }
}



