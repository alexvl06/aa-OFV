package co.com.alianza.web

import co.com.alianza.app.{CrossHeaders, AlianzaCommons}
import co.com.alianza.infrastructure.messages.PinMessages._
import co.com.alianza.infrastructure.messages.PinMarshallers._
import spray.routing.Directives

class PinService extends Directives with AlianzaCommons with CrossHeaders {

  def route = {
    path("validarPin" / Segment) {
      pin => {
        post {
          requestExecute(ValidarPin(pin), pinActor)
        }
      }
    } ~ path("validarPinClienteAdmin" / Segment) {
      pin => {
        post {
          requestExecute(ValidarPin(pin), pinUsuarioEmpresarialAdminActor)
        }
      }
    } ~ path("cambiarPw" / Segment) {
      pin => {
        entity(as[UserPw]) {
          userPw => {
            post {
              requestExecute(CambiarPw(pin, userPw.pw), pinActor)
            }
          }
        }
      }
    } ~ path("cambiarPwClienteAdmin" / Segment) {
      pin => {
        entity(as[UserPw]) {
          userPw => {
            post {
              requestExecute(CambiarPw(pin, userPw.pw), pinUsuarioEmpresarialAdminActor)
            }
          }
        }
      }
    }
  }

}
