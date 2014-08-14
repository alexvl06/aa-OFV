package co.com.alianza.web

import co.com.alianza.app.{CrossHeaders, AlianzaCommons}
import co.com.alianza.infrastructure.messages.PinMessages._
import spray.routing.Directives

class PinService extends Directives with AlianzaCommons with CrossHeaders {

  def route = {
    path("validarPin" / Segment) {
      pin => {
        post {
          requestExecute(ValidarPin(pin), pinActor)
        }
      }
    }
  }

}
